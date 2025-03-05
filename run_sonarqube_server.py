# /// script
# requires-python = ">=3.12"
# dependencies = [
#     "zstandard",
# ]
# ///

# This builds an Arch Linux operating system in a folder (defaults to /mnt/scratch/containers/sonarqube)
# and installs sonarqube server in it.
# Once installed we boot it up with shared networking and access the container's SonarQube instance over
# localhost.

import os
import sys
import subprocess
import shutil
import time
import urllib.request
import random
import html.parser
import tarfile
import io
import traceback

import zstandard

if os.getuid() != 0:
  print('Not running as root, re-executing self using sudo...')
  sys.exit(subprocess.run(['sudo', sys.executable] + sys.argv).returncode)

class ParseAllLinks(html.parser.HTMLParser):
  def __init__(self, *args, **kwargs):
    super().__init__(*args, **kwargs)
    self.all_links = []

  def handle_starttag(self, tag, attrs):
    #print(f'handle_starttag({tag}, {attrs})')
    if tag.casefold() == 'a'.casefold():
      for k,v in attrs:
        if k.casefold() == 'href'.casefold():
          self.all_links.append(v)

  def handle_endtag(self, tag):
    #print(f'handle_endtag({tag})')
    pass

  def handle_data(self, data):
    #print(f'handle_data({data})')
    pass

def http_get_str(url):
  with urllib.request.urlopen(url, timeout=9) as response:
    reply_txt = response.read()
    if not isinstance(reply_txt, str):
      reply_txt = reply_txt.decode('utf-8')

    # And there it is, our very own little quirks mode to make directory-trees easier to parse.
    reply_txt = reply_txt.replace('<hr>', '').replace('</hr>', '')

    return reply_txt

def http_get_links(url):
  parser = ParseAllLinks()
  parser.feed(http_get_str(url))
  return parser.all_links

def get_random_mirror():
  mirror_links = http_get_links('https://archlinux.org/download/')
  return random.choice([
    link for link in mirror_links if 'archlinux/iso'.casefold() in link.casefold() and ('.com'.casefold() in link.casefold() or '.net'.casefold() in link.casefold())
  ])

def empty_dir(dir_name):
  if os.path.isdir(dir_name):
    for dirent in os.listdir(dir_name):
      dirent_path = os.path.join(dir_name, dirent)
      if os.path.isdir(dirent_path):
        shutil.rmtree(dirent_path)
      else:
        os.remove(dirent_path)

CONTAINER_ROOT = os.environ.get('CONTAINER_ROOT', '/mnt/scratch/containers/sonarqube')
print(f'CONTAINER_ROOT={CONTAINER_ROOT}')

if os.path.exists(os.path.dirname(CONTAINER_ROOT)) and not os.path.exists(CONTAINER_ROOT):
  # Something is mounted but no child folder exists
  os.makedirs(CONTAINER_ROOT, exist_ok=True)

# Step 1: Has the install completed?
install_complete_flag = os.path.join(CONTAINER_ROOT, 'install-complete.txt')
if not os.path.exists(install_complete_flag):
  for _try_num in range(0, 6):
    try:

      empty_dir(CONTAINER_ROOT)

      a_mirror_url = get_random_mirror()
      print(f'Selected mirror {a_mirror_url}')
      mirror_file_links = http_get_links(a_mirror_url)
      best_mirror_tarball = next(link for link in mirror_file_links if 'archlinux-bootstrap'.casefold() in link.casefold() and not '.sig'.casefold() in link.casefold())
      if not best_mirror_tarball.casefold().startswith('http'.casefold()):
        best_mirror_tarball = os.path.join(a_mirror_url, best_mirror_tarball)
      print(f'Downloading {best_mirror_tarball}')

      with urllib.request.urlopen(best_mirror_tarball, timeout=45) as response:
        print(f'Decompressing...')
        dctx = zstandard.ZstdDecompressor()
        tar_bio = io.BytesIO()
        oneway_reader = dctx.stream_reader(response)
        #tar_bio.write(oneway_reader.read())
        while True:
          chunk = oneway_reader.read(256 * 1024)
          if not chunk:
            break
          tar_bio.write(chunk)
          print('.', end='', flush=True)
        print('')
        tar_bio.seek(0)

        print(f'Extracting {int(tar_bio.getbuffer().nbytes / 1000000)}mb to {CONTAINER_ROOT}')
        with tarfile.open(fileobj=tar_bio, mode='r:') as tar:
          tar.extractall(path=CONTAINER_ROOT, numeric_owner=True, filter='tar')

        # If the sub-directory root.x86_64 exists, move all children up one directory
        # then delete it
        root_x86_64_folder = os.path.join(CONTAINER_ROOT, 'root.x86_64')
        if os.path.exists(root_x86_64_folder) and os.path.isdir(root_x86_64_folder):
          for dirent in os.listdir(root_x86_64_folder):
            dirent_path = os.path.join(root_x86_64_folder, dirent)
            if os.path.isdir(dirent_path):
              shutil.copytree(dirent_path, os.path.join(CONTAINER_ROOT, dirent), symlinks=True, ignore_dangling_symlinks=True)
            else:
              shutil.copy(dirent_path, os.path.join(CONTAINER_ROOT, dirent), follow_symlinks=True)

        if os.path.exists(root_x86_64_folder) and os.path.isdir(root_x86_64_folder):
          shutil.rmtree(root_x86_64_folder)

      print(f'Successfully extracted data to {CONTAINER_ROOT}')
      break
    except:
      if 'KeyboardInterrupt' in traceback.format_exc():
        raise
      traceback.print_exc()
  else:
    sys.exit(1)

  # We also edit /etc/passwd to make sure the first line begins with
  #  root::
  # Which allows us to login as root w/o password
  etc_passwd_file = os.path.join(CONTAINER_ROOT, 'etc', 'passwd')
  with open(etc_passwd_file, 'rb') as fd:
    etc_passwd_lines = fd.read().decode('utf-8').splitlines(keepends=False)
  if len(etc_passwd_lines) > 0 and not etc_passwd_lines[0].startswith('root::'):
    root_line_tokens = etc_passwd_lines[0].split(':')
    root_line_tokens[1] = ''
    etc_passwd_lines[0] = ':'.join(root_line_tokens)
  with open(etc_passwd_file, 'wb') as fd:
    fd.write('\n'.join(etc_passwd_lines).encode('utf-8'))

  with open(install_complete_flag, 'w') as fd:
    fd.write(f'Extracted data from {best_mirror_tarball}')

subprocess.run([
  'systemd-nspawn',
    '--boot',
    '--directory', CONTAINER_ROOT,

])


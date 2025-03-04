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
  with urllib.request.urlopen(url) as response:
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
  return random.choice([link for link in mirror_links if 'archlinux/iso' in link ])

CONTAINER_ROOT = os.environ.get('CONTAINER_ROOT', '/mnt/scratch/containers/sonarqube')
print(f'CONTAINER_ROOT={CONTAINER_ROOT}')

if os.path.exists(os.path.dirname(CONTAINER_ROOT)) and not os.path.exists(CONTAINER_ROOT):
  # Something is mounted but no child folder exists
  os.makedirs(CONTAINER_ROOT, exist_ok=True)

# Step 1: Has the install completed?
install_complete_flag = os.path.join(CONTAINER_ROOT, 'install-complete.txt')
if not os.path.exists(install_complete_flag):
  a_mirror_url = get_random_mirror()
  print(f'Selected mirror {a_mirror_url}')
  mirror_file_links = http_get_links(a_mirror_url)
  best_mirror_tarball = next(link for link in mirror_file_links if 'archlinux-bootstrap'.casefold() in link.casefold() and not '.sig'.casefold() in link.casefold())
  if not best_mirror_tarball.casefold().startswith('http'.casefold()):
    best_mirror_tarball = os.path.join(a_mirror_url, best_mirror_tarball)
  print(f'Downloading {best_mirror_tarball}')

  with urllib.request.urlopen(best_mirror_tarball) as response:
    response_bytes = response.read()
    try:
      print(f'Decompressing...')
      # If we're using zstandard great, if we've moved on to something else perhaps 'r:*' will look it's decompressor up.
      dctx = zstandard.ZstdCompressor()
      tar_bio = io.BytesIO()
      oneway_reader = dctx.stream_reader(io.BytesIO(response_bytes))
      tar_bio.write(oneway_reader.read())
      tar_bio.seek(0)
    except:
      traceback.print_exc()
      tar_bio = io.BytesIO(response_bytes)

    print(f'Extracting {int(len(response_bytes) / 1000000)}mb to {CONTAINER_ROOT}')
    with tarfile.open(fileobj=tar_bio, mode='r:') as tar:
      tar.extractall(path=CONTAINER_ROOT, numeric_owner=True, filter='tar')

  with open(install_complete_flag, 'w') as fd:
    fd.write(f'Extracted data from {best_mirror_tarball}')





# /// script
# requires-python = ">=3.12"
# dependencies = [
#
# ]
# ///

# This uses a local JVM to compile the SonarQube Rust Sensor
# and then scans a folder, forwarding results to a sonarqube instance.
# If sonar-scanner is not installed it will be downloaded

import os
import sys
import subprocess
import io
import shutil
import random
import zipfile
import urllib.request
import stat
import traceback
import time

REPO_ROOT = os.path.dirname(__file__)

def scan_for_filename(directory, file_name):
  if os.path.exists(directory) and os.path.isdir(directory):
    for dirent in os.listdir(directory):
      dirent_path = os.path.join(directory, dirent)
      if os.path.isdir(dirent_path):
        maybe_found = scan_for_filename(dirent_path, file_name)
        if maybe_found is not None:
          return maybe_found
      else:
        if dirent.casefold() == file_name.casefold():
          return dirent_path
  return None

def scan_for_pieces(directory, file_name_pieces):
  if os.path.exists(directory) and os.path.isdir(directory):
    for dirent in os.listdir(directory):
      dirent_path = os.path.join(directory, dirent)
      if os.path.isdir(dirent_path):
        maybe_found = scan_for_pieces(dirent_path, file_name_pieces)
        if maybe_found is not None:
          return maybe_found
      else:
        if all(piece.casefold() in dirent.casefold() for piece in file_name_pieces):
          return dirent_path
  return None


def ensure_sonar_scanner_available():
  sonar_script_name = 'sonar-scanner'+('.bat' if os.name == 'nt' else '')
  if not shutil.which(sonar_script_name):
    sonar_scanner_zip_url = os.environ.get('SONAR_SCANNER_ZIP_URL', 'https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/sonar-scanner-cli-7.0.2.4839.zip')
    unzip_folder = os.path.join(REPO_ROOT, 'build', 'sonar-scanner-install')
    found_sonar_script = scan_for_filename(unzip_folder, sonar_script_name)
    if found_sonar_script is None or not os.path.exists(found_sonar_script):
      print(f'No sonar-scanner[.bat] on the path! Installing from SONAR_SCANNER_ZIP_URL={sonar_scanner_zip_url} into {unzip_folder}')
      with urllib.request.urlopen(sonar_scanner_zip_url, timeout=14) as response:
        reply_bytes = response.read()
        with zipfile.ZipFile(io.BytesIO(reply_bytes)) as zip_o:
          zip_o.extractall(path=unzip_folder)

    found_sonar_script = scan_for_filename(unzip_folder, 'sonar-scanner'+('.bat' if os.name == 'nt' else ''))

    if found_sonar_script is None or not os.path.exists(found_sonar_script):
      raise Exception(f'Unable to find {sonar_script_name} under {unzip_folder}')

    # Ensure executable on *nix hosts
    try:
      st = os.stat(found_sonar_script)
      os.chmod(found_sonar_script, st.st_mode | stat.S_IEXEC)
    except:
      traceback.print_exc()

    os.environ['PATH'] = os.environ.get('PATH', '')+os.pathsep+os.path.dirname(found_sonar_script)


  return shutil.which(sonar_script_name)

def url_is_alive(url):
  try:
    with urllib.request.urlopen(url, timeout=1) as response:
      unused = response.read()
      return True
  except:
    pass
  return False

def delay_until_url_available(url, max_seconds):
  for i in range(0, int(max_seconds)):
    time.sleep(1)
    print('+', flush=True, end='')
    if url_is_alive(url):
      print()
      print(f'{url} is up!')
      return
  print()
  print(f'WARNING: {url} did not come online in {max_seconds} seconds!')

def delay_until_sonarqube_version_is_200(url, max_seconds):
  url = url.rstrip('/')
  for i in range(0, int(max_seconds)):
    time.sleep(1)
    print('+', flush=True, end='')
    try:
      with urllib.request.urlopen(f'{url}/api/server/version') as response:
        result = response.read()
        print(f'INFO: The server at {url} is running SonarQube version {result}')
        return # We're done!
    except:
      ex_s = traceback.format_exc()
      if not ('404' in ex_s or 'Connection refused' in ex_s):
        traceback.print_exc() # This is a new error
  print()
  print(f'WARNING: {url}/api/server/version did not come online in {max_seconds} seconds!')




# Step 1 - Compile the maven project under ./sonarqube-rust-sensor/

mvn_proj_folder = os.path.join(REPO_ROOT, 'sonarqube-rust-sensor')
mvn_compile_cmd = [
  'mvn', 'package'
]
print()
print(f'Running {" ".join(mvn_compile_cmd)} inside {mvn_proj_folder}')
print()
subprocess.run(mvn_compile_cmd, cwd=mvn_proj_folder, check=True)


# Step 2 - Install/Deploy the maven project under ./sonarqube-rust-sensor/

sonar_rust_sensor_jar = scan_for_pieces(os.path.join(mvn_proj_folder, 'target'), ['sonarqube', 'rust', 'sensor', '.jar'])
print(f'sonar_rust_sensor_jar = {sonar_rust_sensor_jar}')
if sonar_rust_sensor_jar is None or not os.path.exists(sonar_rust_sensor_jar):
  print(f'Fatal Error: {sonar_rust_sensor_jar} does not exist!')
  sys.exit(1)

seconds_to_wait_for_server_restart = int(os.environ.get('HTTP_WAIT_SECONDS', '45'))
print(f'HTTP_WAIT_SECONDS = {seconds_to_wait_for_server_restart}')

# We will need root access to copy files into the container's root filesystem
CONTAINER_ROOT = os.environ.get('CONTAINER_ROOT', '/mnt/scratch/containers/sonarqube')
print(f'CONTAINER_ROOT={CONTAINER_ROOT}')
if not os.path.exists(CONTAINER_ROOT):
  print(f'Fatal Error: {CONTAINER_ROOT} does not exist! Please execute "uv run run_sonarqube_server.py" to build the server')
  sys.exit(1)

sonar_server_url = os.environ.get('SONAR_SERVER_URL', 'http://127.0.0.1:9000')
print(f'SONAR_SERVER_URL = {sonar_server_url}')

container_plugin_folder = os.path.join(CONTAINER_ROOT, 'usr', 'share', 'webapps', 'sonarqube', 'extensions', 'plugins')
print(f'container_plugin_folder = {container_plugin_folder}')
pre_existing_jar = scan_for_pieces(container_plugin_folder, ['sonarqube', 'rust', 'sensor', '.jar'])
if not pre_existing_jar is None and os.path.exists(pre_existing_jar):
  subprocess.run([
    'sudo', 'rm', pre_existing_jar
  ], check=True)

subprocess.run([
 'sudo', 'cp', sonar_rust_sensor_jar, container_plugin_folder
], check=True)

# Tell machinectl to reboot container
if 'localhost' in sonar_server_url or '127.0.0' in sonar_server_url:
  print(f'Rebooting container "sonarqube"...')
  r = subprocess.run([
    'sudo', 'machinectl', 'reboot', 'sonarqube',
  ], check=False)
  if r.returncode == 0:
    delay_until_url_available(sonar_server_url, seconds_to_wait_for_server_restart)


# Step 3 - Sonar-Scan the rust code!

ss_script = ensure_sonar_scanner_available()
print(f'ss_script = {ss_script}')
rust_project_folder_to_scan = os.environ.get('FOLDER_TO_SCAN', os.path.join(REPO_ROOT, 'example_rust_code'))
print(f'FOLDER_TO_SCAN = {rust_project_folder_to_scan}')

if not url_is_alive(sonar_server_url) and ('localhost' in sonar_server_url or '127.0.0' in sonar_server_url):
  print(f'Server at {sonar_server_url} did not respond, spawning run_sonarqube_server.py using systemd-run in the background...')
  r = subprocess.run([
    'systemd-run',
      '--user',
      '--quiet',
      '--unit=run_sonarqube_server_py',
      f'--working-directory={REPO_ROOT}',
      '--collect',
      #sys.executable, 'run_sonarqube_server.py'
      'uv', 'run', 'run_sonarqube_server.py'
  ], stdin=subprocess.DEVNULL, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
  print(f'systemd-run returned {r.returncode}')
  delay_until_url_available(sonar_server_url, seconds_to_wait_for_server_restart)


if not 'SONAR_TOKEN' in os.environ:
  print('WARNING: SONAR_TOKEN not set, ensure sonar.token is specified in your host or project sonar-*.properties file!')

delay_until_sonarqube_version_is_200(sonar_server_url, seconds_to_wait_for_server_restart)

# Run the scan!
sonar_scan_cmd = [
  ss_script,
    f'-Dsonar.sources=.',
    f'-Dsonar.host.url={sonar_server_url}',
]
print()
print(f'Running {" ".join(sonar_scan_cmd)} inside {rust_project_folder_to_scan}')
print()
subprocess.run(sonar_scan_cmd, cwd=rust_project_folder_to_scan, check=True)



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



ss_script = ensure_sonar_scanner_available()
print(f'ss_script = {ss_script}')
rust_project_folder_to_scan = os.environ.get('FOLDER_TO_SCAN', os.path.join(REPO_ROOT, 'example_rust_code'))
print(f'FOLDER_TO_SCAN = {rust_project_folder_to_scan}')

sonar_server_url = os.environ.get('SONAR_SERVER_URL', 'http://127.0.0.1:9000')
print(f'SONAR_SERVER_URL = {sonar_server_url}')

if not 'SONAR_TOKEN' in os.environ:
  print('WARNING: SONAR_TOKEN not set, ensure sonar.token is specified in your host or project sonar-*.properties file!')

# Run the scan!
sonar_scan_cmd = [
  ss_script,
    f'-Dsonar.sources=.',
    f'-Dsonar.host.url={sonar_server_url}',
]
print()
print(f'Running {" ".join(sonar_scan_cmd)} inside {rust_project_folder_to_scan}')
print()
subprocess.run(sonar_scan_cmd, cwd=rust_project_folder_to_scan)



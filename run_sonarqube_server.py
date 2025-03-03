
# This builds an Arch Linux operating system in a folder (defaults to /mnt/scratch/containers/sonarqube)
# and installs sonarqube server in it.
# Once installed we boot it up with shared networking and access the container's SonarQube instance over
# localhost.

import os
import sys
import subprocess
import shutil
import time

CONTAINER_ROOT = os.environ.get('CONTAINER_ROOT', '/mnt/scratch/containers/sonarqube')
print(f'CONTAINER_ROOT={CONTAINER_ROOT}')






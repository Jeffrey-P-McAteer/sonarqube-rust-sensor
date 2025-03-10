# This script downloads public lints as HTML and parses them into a JSON format which
# The java class RustRulesDefinition parses to tell SonarQube which rules we process.

import os
import sys
import urllib.request

lints_html_url = 'https://rust-lang.github.io/rust-clippy/stable/index.html'

with urllib.request.urlopen(lints_html_url) as request:
  lints_html = request.read().decode('utf-8')
  print(f'lints_html = {lints_html}')



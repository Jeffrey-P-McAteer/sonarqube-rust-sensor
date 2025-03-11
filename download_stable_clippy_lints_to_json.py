# This script downloads public lints as HTML and parses them into a JSON format which
# The java class RustRulesDefinition parses to tell SonarQube which rules we process.

import os
import sys
import urllib.request
import html.parser

class ClippyRule:
  # Lint group is like "correctness"
  # lint level is one of "allow", "deny", "warn"
  def __init__(self, rule_id, rule_name, rule_description_html, lint_group, lint_level):
    self.rule_id = rule_id
    self.rule_name = rule_name
    self.rule_description_html = rule_description_html
    self.lint_group = lint_group
    self.lint_level = lint_level
  def __repr__(self):
    return f'ClippyRule({self.rule_id}, {self.rule_description_html})'


class HTML_To_ClippyRule_Parser(html.parser.HTMLParser):

  def __init__(self, *args, **kwargs):
    super().__init__(*args, **kwargs)
    self.parsed_rules = list() # Contains ClippyRule objects
    # We set these to None to read-in the content of a handle_starttag-identified tag
    self.last_seen_article_id = ''
    self.last_seen_lint_group = ''
    self.last_seen_lint_level = ''
    self.last_seen_lint_description_docs_html = ''
    self.within_lint_description_docs_html = False # Set to true & used to record self.last_seen_lint_description_docs_html
    self.num_divs_within_description_docs_html = 0


  def handle_starttag(self, tag, attrs):
    attrs = dict(attrs)
    #print("Encountered a start tag:", tag)
    if tag == 'article' and 'id' in attrs:
      self.last_seen_article_id = attrs['id']
    elif tag == 'span' and 'label-lint-group' in attrs.get('class', '').lower():
      self.last_seen_lint_group = None
    elif tag == 'span' and 'label-lint-level' in attrs.get('class', '').lower():
      self.last_seen_lint_level = None
    elif tag == 'div':
      if 'lint-docs' in attrs.get('class', '').lower():
        self.last_seen_lint_description_docs_html = f'<{tag}>'
        self.within_lint_description_docs_html = True
        self.num_divs_within_description_docs_html = 0
    elif self.within_lint_description_docs_html:
      self.num_divs_within_description_docs_html += 1 # <div> within a doc area
      self.last_seen_lint_description_docs_html += f'<{tag}>'

  def handle_endtag(self, tag):
    if tag == 'article' and len(self.last_seen_article_id) > 0:
      self.parsed_rules.append(
        ClippyRule(
          self.last_seen_article_id,
          self.last_seen_article_id,
          self.last_seen_lint_description_docs_html,
          self.last_seen_lint_group,
          self.last_seen_lint_level
        )
      )
      # And reset vars
      self.last_seen_article_id = ''
      self.last_seen_lint_group = ''
      self.last_seen_lint_level = ''
      self.last_seen_lint_description_docs_html = ''
    elif self.within_lint_description_docs_html:
      self.num_divs_within_description_docs_html -= 1
      if self.num_divs_within_description_docs_html < 0:
        # reached end, flip back to False
        self.within_lint_description_docs_html = False
        self.last_seen_lint_description_docs_html += f'</{tag}>'
      else:
        self.last_seen_lint_description_docs_html += f'</{tag}>'

    #print("Encountered an end tag :", tag)

  def handle_data(self, data):
    if self.last_seen_lint_group is None:
      self.last_seen_lint_group = data.strip()
    elif self.last_seen_lint_level is None:
      self.last_seen_lint_level = data.strip()
    elif self.within_lint_description_docs_html:
      self.last_seen_lint_description_docs_html += data

lints_html_url = 'https://rust-lang.github.io/rust-clippy/stable/index.html'

with urllib.request.urlopen(lints_html_url) as request:
  lints_html = request.read().decode('utf-8')

#print(f'lints_html = {lints_html}')
parser = HTML_To_ClippyRule_Parser()
parser.feed(lints_html)

for rule in parser.parsed_rules:
  print(f'{rule}')

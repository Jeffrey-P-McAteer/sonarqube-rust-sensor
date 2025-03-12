package com.jmcateer.sonarqube_rust_sensor;

@FunctionalInterface
public interface ReadAllClippyRulesFunction {
    void accept(String rule_id, String rule_name, String rule_description_html, String lint_group, String lint_level);
}


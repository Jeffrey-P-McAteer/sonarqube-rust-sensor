package com.jmcateer.sonarqube_rust_sensor;

@FunctionalInterface
public interface ReadReportedClippyLintsJsonFunction {
    void accept(String rule_id, String rule_name, String rule_description_html, String lint_group, String lint_level,
                String file_path, int file_line_number, String additional_clippy_messages
    );
}


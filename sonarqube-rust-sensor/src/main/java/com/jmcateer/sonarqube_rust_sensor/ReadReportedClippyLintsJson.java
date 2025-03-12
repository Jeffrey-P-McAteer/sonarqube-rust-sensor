package com.jmcateer.sonarqube_rust_sensor;

import java.util.List;
import java.util.Objects;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONTokener;

public class ReadReportedClippyLintsJson {

    private static final Logger LOGGER = Loggers.get(ReadReportedClippyLintsJson.class);

    public static void ReadReported(String clippy_output_json, ReadReportedClippyLintsJsonFunction consumer_func) {

        LOGGER.warn("TODO ReadReported {}", clippy_output_json);

        /*
        InputStreamReader streamReader = new InputStreamReader(clippy_output_json, java.nio.charset.StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(streamReader);

        JSONTokener tokener = new JSONTokener(reader);
        JSONArray json = new JSONArray(tokener);

        LOGGER.warn("Read json={}", json);

        for (int i=0; i<json.length(); i+=1) {
            try {
                JSONObject rule_o = json.getJSONObject(i);
                String rule_id = rule_o.getString("rule_id");
                String rule_name = rule_o.getString("rule_name");
                String rule_description_html = rule_o.getString("rule_description_html");
                String lint_group = rule_o.getString("lint_group");
                String lint_level = rule_o.getString("lint_level");

                consumer_func.accept(rule_id, rule_name, rule_description_html, lint_group, lint_level);
            }
            catch (Throwable e) {
              LOGGER.warn("Unexpected exception in ReadAllClippyRules::WithAllRules reading json[{}] {}", i, e);
            }
        }
        */

    }

}

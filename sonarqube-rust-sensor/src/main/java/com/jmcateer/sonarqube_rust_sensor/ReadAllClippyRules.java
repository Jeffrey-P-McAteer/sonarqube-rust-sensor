package com.jmcateer.sonarqube_rust_sensor;

import java.util.List;
import java.util.Objects;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import org.sonar.api.server.rule.RulesDefinition;

import org.sonar.api.server.debt.DebtRemediationFunction;
import org.sonar.api.server.debt.internal.DefaultDebtRemediationFunction;
import org.sonar.api.server.rule.RulesDefinition.NewRule;
import org.sonar.api.server.debt.DebtRemediationFunction;
import org.sonar.api.rules.RuleType;

/* // Stolen scarcode; using same template, new rules from clippy
import org.antlr.sql.dialects.SQLDialectRules;
import org.sonar.api.rules.RuleType;
import org.sonar.plugins.sql.models.rules.SqlRules;
*/

import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONTokener;

public class ReadAllClippyRules {

    private static final Logger LOGGER = Loggers.get(ReadAllClippyRules.class);

    public static void WithAllRules(ReadAllClippyRulesFunction consumer_func) {

        InputStream inputStream = ReadAllClippyRules.class.getResourceAsStream("clippy-rules.json");
        InputStreamReader streamReader = new InputStreamReader(inputStream, java.nio.charset.StandardCharsets.UTF_8);
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

    }

}

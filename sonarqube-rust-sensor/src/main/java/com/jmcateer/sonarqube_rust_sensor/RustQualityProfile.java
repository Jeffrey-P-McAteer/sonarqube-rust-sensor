package com.jmcateer.sonarqube_rust_sensor;

import java.util.List;
import java.util.Objects;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;

import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONTokener;

public class RustQualityProfile implements BuiltInQualityProfilesDefinition {

    private static final Logger LOGGER = Loggers.get(RustQualityProfile.class);


    @Override
    public void define(Context context) {
        final NewBuiltInQualityProfile profile = context.createBuiltInQualityProfile("Rust rules", Constants.LANGUAGE_KEY)
                                                        .setDefault(true);

        ReadAllClippyRules.WithAllRules((rule_id, rule_name, rule_description_html, lint_group, lint_level) -> {
            profile.activateRule(Constants.LANGUAGE_KEY, rule_id);
        });

        profile.done();
    }
}

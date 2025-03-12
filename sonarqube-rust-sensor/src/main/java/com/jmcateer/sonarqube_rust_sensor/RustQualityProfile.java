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

        //profile.activateRule("rust", "rule1");
        InputStream inputStream = RustQualityProfile.class.getResourceAsStream("clippy-rules.json");
        InputStreamReader streamReader = new InputStreamReader(inputStream, java.nio.charset.StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(streamReader);

        JSONTokener tokener = new JSONTokener(reader);
        JSONArray json = new JSONArray(tokener);

        LOGGER.warn("Read json={}", json);

        for (int i=0; i<json.length(); i+=1) {
            try {
                JSONObject rule_o = json.getJSONObject(i);
                String rule_id = rule_o.getString("rule_id");
                profile.activateRule("rust", rule_id);
            }
            catch (Throwable e) {
              LOGGER.warn("Unexpected exception in RustQualityProfile::define reading json[{}] {}", i, e);
            }
        }

        profile.done();
    }
}

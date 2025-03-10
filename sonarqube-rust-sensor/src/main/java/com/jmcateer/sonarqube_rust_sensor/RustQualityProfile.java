package com.jmcateer.sonarqube_rust_sensor;

import java.util.List;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;

public class RustQualityProfile implements BuiltInQualityProfilesDefinition {

    @Override
    public void define(Context context) {
        final NewBuiltInQualityProfile profile = context.createBuiltInQualityProfile("Rust rules", Constants.LANGUAGE_KEY)
                                                        .setDefault(true);

        /*final List<SqlRules> rules = SQLDialectRules.INSTANCE.getGroupedRules();

        for (SqlRules sqlRules : rules) {
            for (Rule rule : sqlRules.getRule()) {
                profile.activateRule(sqlRules.getRepoKey(), rule.getKey());
            }
        }*/

        // TODO read from cargo-clippy's list of rules

        profile.done();
    }
}

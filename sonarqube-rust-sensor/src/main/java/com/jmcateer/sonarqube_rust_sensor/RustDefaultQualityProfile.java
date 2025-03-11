package com.jmcateer.sonarqube_rust_sensor;

import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;

public final class RustDefaultQualityProfile implements BuiltInQualityProfilesDefinition {

  @Override
  public void define(Context context) {
    NewBuiltInQualityProfile profile = context.createBuiltInQualityProfile("Rust Default Quality", Constants.LANGUAGE_KEY);
    // TODO enumerate clippy rules + activate based on clippy's default activation state
    profile.activateRule("rust", "rule1");
    profile.setDefault(true);
    profile.done();
  }
}

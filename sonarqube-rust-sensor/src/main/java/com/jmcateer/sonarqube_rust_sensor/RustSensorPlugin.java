package com.jmcateer.sonarqube_rust_sensor;

import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.PropertyType;
import org.sonar.api.Plugin;

/**
 * Entry point for Sonar's API bindings
 */
public class RustSensorPlugin implements Plugin
{
    @Override
    public void define(Context context) {
        context.addExtension(
            PropertyDefinition.builder(Constants.CFG_FILE_SUFFIXES)
                    .name("Rust file suffixes")
                    .description(
                        "List of extensions which hold rust code (default .rs)"
                    )
                    .multiValues(true)
                    .type(PropertyType.STRING)
                    .build()
        );
        context.addExtension(
            PropertyDefinition.builder("sonar.lang.patterns.rust") // This name is provided by SonarQube & we do not expect users to adjust it
                    .name("Rust file patterns")
                    .description(
                        "List of patterns to files which hold rust code (default **/*.rs,**/*.RS,**/*.toml,**/*.TOML)"
                    )
                    .multiValues(true)
                    .type(PropertyType.STRING)
                    .defaultValue("**/*.rs,**/*.RS,**/*.toml,**/*.TOML")
                    .build()
        );

        context.addExtension(
            PropertyDefinition.builder(Constants.CFG_CLIPPY_LINTS_SKIP)
                    .name("Clippy lints to skip")
                    .description(
                        "List of 'clippy:lintName' keys of repo:lintName of lints to skip reporting for the entire repository "+
                        "(eg clippy::useless_format, see https://rust-lang.github.io/rust-clippy/master/index.html for full list)"
                    )
                    .multiValues(true)
                    .type(PropertyType.STRING)
                    .build()
        );

        context.addExtension(
            PropertyDefinition.builder(Constants.CFG_CLIPPY_JSON_OUTPUT)
                    .name("Clippy Output JSON")
                    .description(
                        "Path to output from clippy, generated by running \"cargo clippy --offline --quiet --message-format=json > clippy-output.json\" in the project folder. Default value is \"clippy-output.json\"."
                    )
                    .multiValues(false)
                    .type(PropertyType.STRING)
                    .defaultValue(Constants.CFG_CLIPPY_JSON_OUTPUT_DEFAULTVAL)
                    .build()
        );


        context.addExtensions(
            RustLanguage.class,
            RustRulesDefinition.class,
            RustQualityProfile.class,
            RustSensor.class
        );

        // TODO see https://github.com/gretard/sonar-sql-plugin/blob/master/src/sonar-sql-plugin/src/main/java/org/sonar/plugins/sql/SQLPlugin.java#L109

    }

}

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
            PropertyDefinition.builder("sonar.rust.file-suffixes")
                    .name("Rust file suffixes")
                    .description(
                        "List of extensions which hold rust code (default .rs)"
                    )
                    .multiValues(true)
                    .type(PropertyType.STRING)
                    .build()
        );
        context.addExtension(
            PropertyDefinition.builder("sonar.rust.clippy-lints.skip")
                    .name("Clippy lints to skip")
                    .description(
                        "List of 'clippy:lintName' keys of repo:lintName of lints to skip reporting for the entire repository "+
                        "(eg clippy::useless_format, see https://rust-lang.github.io/rust-clippy/master/index.html for full list)"
                    )
                    .multiValues(true)
                    .type(PropertyType.STRING)
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

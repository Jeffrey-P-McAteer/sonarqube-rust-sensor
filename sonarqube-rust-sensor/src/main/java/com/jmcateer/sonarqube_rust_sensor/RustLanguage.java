package com.jmcateer.sonarqube_rust_sensor;

import org.sonar.api.config.Configuration;
import org.sonar.api.resources.AbstractLanguage;

public final class RustLanguage extends AbstractLanguage {

    public static final String[] DEFAULT_FILE_SUFFIXES = new String[] {".rs"};

    private final Configuration config;

    public RustLanguage(final Configuration config) {
        super(Constants.LANGUAGE_KEY, Constants.LANGUAGE_NAME);
        this.config = config;
    }

    public String[] getFileSuffixes() {
        final String[] suffixes = config.getStringArray("sonar.rust.file-suffixes");
        if (suffixes == null || suffixes.length == 0) {
            return DEFAULT_FILE_SUFFIXES;
        }
        return suffixes;
    }
}

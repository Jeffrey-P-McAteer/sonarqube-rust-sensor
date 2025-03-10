package com.jmcateer.sonarqube_rust_sensor;

import org.sonar.api.config.Configuration;
import org.sonar.api.resources.AbstractLanguage;

public final class RustLanguage extends AbstractLanguage {

    public static final String[] DEFAULT_FILE_SUFFIXES = new String[] {".rs", ".RS", ".toml", ".TOML"};

    private final Configuration config;

    public RustLanguage(final Configuration config) {
        super(Constants.LANGUAGE_KEY, Constants.LANGUAGE_NAME);
        this.config = config;
    }

    @Override
    public String getName() { return Constants.LANGUAGE_NAME; }
    @Override
    public String getKey() { return Constants.LANGUAGE_KEY; }

    @Override
    public String[] getFileSuffixes() {
        final String[] suffixes = config.getStringArray("sonar.rust.file-suffixes");
        if (suffixes == null || suffixes.length == 0) {
            return DEFAULT_FILE_SUFFIXES;
        }
        return suffixes;
    }

    @Override
    public String[] filenamePatterns() {
        String[] suffixes = this.getFileSuffixes();
        String[] patterns = new String[suffixes.length];
        for (int i=0; i<suffixes.length; i+=1) {
            patterns[i] = "**/*"+suffixes[i];
        }
        return patterns;
    }

    @Override
    public boolean publishAllFiles() { return true; }

}

package com.jmcateer.sonarqube_rust_sensor;

import java.util.ArrayList;

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
        final String[] suffixes = config.getStringArray(Constants.CFG_FILE_SUFFIXES);
        if (suffixes == null || suffixes.length == 0) {
            return DEFAULT_FILE_SUFFIXES;
        }
        ArrayList<String> nonempty_suffixes = new ArrayList<String>(suffixes.length);
        for (int i=0; i<suffixes.length; i+=1) {
            if (suffixes[i].length() >= 1) {
                nonempty_suffixes.add(suffixes[i]);
            }
        }
        if (nonempty_suffixes.size() == 0) {
            return DEFAULT_FILE_SUFFIXES;
        }
        return nonempty_suffixes.toArray(new String[nonempty_suffixes.size()]);
    }

    @Override
    public String[] filenamePatterns() {
        String[] suffixes = this.getFileSuffixes();
        String[] patterns = new String[suffixes.length+1];
        for (int i=0; i<suffixes.length; i+=1) {
            patterns[i] = "**/*"+suffixes[i];
        }
        final String clippy_json_output_path = this.config.get(Constants.CFG_CLIPPY_JSON_OUTPUT).orElse(Constants.CFG_CLIPPY_JSON_OUTPUT_DEFAULTVAL);
        patterns[patterns.length] = "**/"+clippy_json_output_path;
        return patterns;
    }

    @Override
    public boolean publishAllFiles() { return true; }

}

package com.jmcateer.sonarqube_rust_sensor;

/**
 * Entry point for Sonar's API bindings
 */
public class RustSensorPlugin implements org.sonar.api.Plugin
{
    @Override
    public void define(Context context) {
        // TODO
        System.out.println("\n\nHello Sonar-Scanner Plugin!\n\n");
    }

}

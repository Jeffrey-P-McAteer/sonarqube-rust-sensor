
# SonarQube Rust Sensor

This repo contains code to build a Rust Sensor for use with SonarQube systems.

# Development Dependencies

 - `uv` and `python`

For the container to test with:

 - `systemd-nspawn`
 - Set the env var `CONTAINER_ROOT` to a path where we can install the container's root filesystem to

# Runtime Dependencies

 - Java 8+
 - SonarQube server
 - sonar-scanner on the client

# Building

```bash
uv run compile_scanner_and_scan_folder.py
```

# Testing

```bash
TODO
```

# Usage

```bash
TODO
```

# Research

 - https://docs.sonarsource.com/sonarqube-server/10.8/extension-guide/developing-a-plugin/supporting-new-languages/
 - https://github.com/SonarSource/sslr
 - https://github.com/AdaCore/gnatdashboard/tree/master



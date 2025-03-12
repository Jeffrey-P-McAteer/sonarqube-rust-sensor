package com.jmcateer.sonarqube_rust_sensor;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.Configuration;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class RustSensor implements Sensor {

    private static final Logger LOGGER = Loggers.get(RustSensor.class);

    @Override
    public void describe(final SensorDescriptor descriptor) {
        descriptor.onlyOnLanguage(Constants.LANGUAGE_KEY);
    }

    @Override
    public void execute(final SensorContext context) {
      final Configuration config = context.config();
      final ExecutorService service = Executors.newWorkStealingPool();
      final org.sonar.api.batch.fs.FileSystem fs = context.fileSystem();
      //final Iterable<InputFile> files = fs.inputFiles(fs.predicates().hasLanguage(Constants.LANGUAGE_KEY));
      final Iterable<InputFile> files = fs.inputFiles(fs.predicates().all());

      final ArrayList<InputFile> files_list = new ArrayList<InputFile>();
      fs.inputFiles(fs.predicates().all()).forEach(f -> files_list.add(f) );

      final String clippy_json_output_path = config.get(Constants.CFG_CLIPPY_JSON_OUTPUT).orElse(Constants.CFG_CLIPPY_JSON_OUTPUT_DEFAULTVAL);

      // If clippy_json_output_path is not in the filesystem we spawn a process to generate it before continuing w/ analysis
      final String[] clippy_json_string_val = new String[]{null};
      files_list.forEach(inputFile -> {
        if (clippy_json_output_path.equals( inputFile.path().getFileName().toString() )) {
          try {
            clippy_json_string_val[0] = new BufferedReader(new InputStreamReader( inputFile.inputStream() )).lines().collect(Collectors.joining("\n"));
          }
          catch (java.io.IOException e) {
            LOGGER.warn("Unexpected exception while reading file: {} - {}", inputFile, e);
          }
        }
      });

      if (clippy_json_string_val[0] == null) {
        // Run command & capture output, storing directly into the string
        String[] clippy_command = new String[]{
          "cargo", "clippy", "--offline", "--quiet", "--message-format=json"
        };

        try {

          Runtime rt = Runtime.getRuntime();
          Process proc = rt.exec(clippy_command);

          BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
          BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

          String s = null;
          while ((s = stdInput.readLine()) != null) {
            if (clippy_json_string_val[0] == null) {
              clippy_json_string_val[0] = s;
            }
            else {
              clippy_json_string_val[0] = clippy_json_string_val[0] + "\n" + s;
            }
          }
          while ((s = stdError.readLine()) != null) {
            LOGGER.warn("StdError Running Clippy: {}", s);
          }
        }
        catch (java.io.IOException e) {
          LOGGER.warn("Unexpected exception while running {} {}", String.join(" ", clippy_command), e);
        }

      }

      if (clippy_json_string_val[0] != null) {
        ReadReportedClippyLintsJson.ReadReported(clippy_json_string_val[0], (rule_id, rule_name, rule_description_html, lint_group, lint_level, file_path, file_line_number, additional_clippy_messages) -> {
          InputFile lint_inputFile = matchingInputFile(files_list, file_path);
          if (lint_inputFile != null) {
            final NewIssue newIssue = context.newIssue().forRule(RuleKey.of(Constants.LANGUAGE_KEY, rule_id));
            final NewIssueLocation loc = newIssue.newLocation().on(lint_inputFile).message(additional_clippy_messages);
            loc.at(lint_inputFile.selectLine(file_line_number));
            newIssue.at(loc).save();
          }
        });
      }

    }

    private static InputFile matchingInputFile(ArrayList<InputFile> files_list, String file_path) {
      for (int i=0; i<files_list.size(); i+=1) {
        if (files_list.get(i).path().toString().endsWith(file_path)) {
          return files_list.get(i);
        }
      }
      return null;
    }

}

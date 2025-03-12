package com.jmcateer.sonarqube_rust_sensor;

import java.util.List;
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

      final String clippy_json_output_path = config.get(Constants.CFG_CLIPPY_JSON_OUTPUT).orElse(Constants.CFG_CLIPPY_JSON_OUTPUT_DEFAULTVAL);

      // If clippy_json_output_path is not in the filesystem we spawn a process to generate it before continuing w/ analysis
      final String[] clippy_json_string_val = new String[]{null};
      files.forEach(inputFile -> {
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
      }

      if (clippy_json_string_val[0] != null) {
        ReadReportedClippyLintsJson.ReadReported(clippy_json_string_val[0], (_rule_id, _rule_name, _rule_description_html, _lint_group, _lint_level, _file_path, _file_line_number, _additional_clippy_messages) -> {
          // TODO
        });
      }

      final java.util.Random test_random = new java.util.Random();
      final java.util.ArrayList<String> test_files = new java.util.ArrayList<String>();
      final java.util.ArrayList<String> completed_test_files = new java.util.ArrayList<String>();

      files.forEach(inputFile -> {
            test_files.add(""+inputFile);
            service.execute(
              new Runnable() {
                @SuppressWarnings("deprecation")
                @Override
                public void run() {
                  try {
                    if (inputFile.file().length() > 1024 * 1024) {
                        LOGGER.debug("Skipping {} file as its size exceeds {} bytes.", inputFile, 1024 * 1024);
                        return;
                    }
                    /*final AntlrContext ctx = sqlDialect.parse(inputFile.contents(), customRules);
                    for (final Filler filler : fillers) {
                      filler.fill(inputFile, context, ctx);
                    }*/
                    double d = test_random.nextDouble();
                    LOGGER.warn("test_random.nextDouble() returned {} for file {}", d, inputFile);
                    if (d < 0.80) {
                      // Synthesize a fake issue on line 1 of this file
                      final NewIssue newIssue = context.newIssue().forRule(RuleKey.of("rust", "approx_constant"));
                      final NewIssueLocation loc = newIssue.newLocation().on(inputFile).message("This is a Random Issue! Your lucky number is "+test_random.nextDouble()+".");
                      loc.at(inputFile.selectLine(1));
                      newIssue.at(loc).save();
                    }
                  }
                  catch (Throwable e) {
                      LOGGER.warn("Unexpected exception while analyzing file: {} - {}", inputFile, e);
                  }
                  completed_test_files.add(""+inputFile);
                }
              }
            );
        });

        LOGGER.debug("RustSensor has found {} files", test_files.size());
        LOGGER.debug("ONE completed_test_files = {} files", completed_test_files.size());

        service.shutdown();

        LOGGER.debug("TWO completed_test_files = {} files", completed_test_files.size());
        try {
            service.awaitTermination(36, TimeUnit.SECONDS);
            LOGGER.debug("THREE completed_test_files = {} files", completed_test_files.size());
            service.shutdownNow();
        }
        catch (Throwable e) {
            LOGGER.warn("Unexpected exception while waiting for executor service to finish.", e);
        }
        LOGGER.debug("FOUR completed_test_files = {} files", completed_test_files.size());

    }
}

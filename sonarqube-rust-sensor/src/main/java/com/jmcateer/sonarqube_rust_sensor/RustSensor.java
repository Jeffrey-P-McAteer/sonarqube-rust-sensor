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
      final java.util.Random test_random = new java.util.Random();
      final java.util.ArrayList<String> test_files = new java.util.ArrayList<String>();
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
                    if (test_random.nextDouble() < 0.55) {
                      // Synthesize a fake issue on line 1 of this file
                      final NewIssue newIssue = context.newIssue().forRule(RuleKey.of("rust", "rand-1"));
                      final NewIssueLocation loc = newIssue.newLocation().on(inputFile).message("This is a Random Issue! Your lucky number is "+test_random.nextDouble()+".");
                      loc.at(inputFile.selectLine(1));
                      newIssue.at(loc).save();
                    }
                  }
                  catch (Throwable e) {
                      LOGGER.warn("Unexpected exception while analyzing file: " + inputFile, e);
                  }
                }
              }
            );
        });

        LOGGER.debug("RustSensor has found {} files", test_files.size());

        service.shutdown();
        try {
            service.awaitTermination(36, TimeUnit.SECONDS);
            service.shutdownNow();
        }
        catch (Throwable e) {
            LOGGER.warn("Unexpected exception while waiting for executor service to finish.", e);
        }
    }
}

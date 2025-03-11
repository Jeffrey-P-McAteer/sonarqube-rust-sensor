package com.jmcateer.sonarqube_rust_sensor;

import java.util.List;
import java.util.Objects;

import org.sonar.api.server.rule.RulesDefinition;

import org.sonar.api.server.debt.DebtRemediationFunction;
import org.sonar.api.server.debt.internal.DefaultDebtRemediationFunction;
import org.sonar.api.server.rule.RulesDefinition.NewRule;
import org.sonar.api.server.debt.DebtRemediationFunction;
import org.sonar.api.rules.RuleType;

/* // Stolen scarcode; using same template, new rules from clippy
import org.antlr.sql.dialects.SQLDialectRules;
import org.sonar.api.rules.RuleType;
import org.sonar.plugins.sql.models.rules.SqlRules;
*/

import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;


public class RustRulesDefinition implements RulesDefinition {

    private static final Logger LOGGER = Loggers.get(RustRulesDefinition.class);

    @Override
    public void define(Context context) {
        try {
            /*
            List<SqlRules> rules = SQLDialectRules.INSTANCE.getGroupedRules();

            for (SqlRules rulesDef : rules) {
                NewRepository repository =
                        context.createRepository(rulesDef.getRepoKey(), Constants.languageKey)
                                .setName(rulesDef.getRepoName());

                for (org.sonar.plugins.sql.models.rules.Rule rule : rulesDef.getRule()) {
                    NewRule x1Rule =
                            repository
                                    .createRule(rule.getKey())
                                    .setName(rule.getName())
                                    .setHtmlDescription(rule.getDescription())
                                    .addTags(rule.getTag())
                                    .setSeverity(rule.getSeverity());
                    String gapMultiplier = rule.getDebtRemediationFunctionCoefficient();
                    String baseEffort = rule.getRemediationFunctionBaseEffort();
                    DebtRemediationFunction func =
                            new DefaultDebtRemediationFunction(
                                    DebtRemediationFunction.Type.valueOf(rule.getRemediationFunction()),
                                    (Objects.equals(gapMultiplier, "")) ? null : gapMultiplier,
                                    (Objects.equals(baseEffort, "")) ? null : baseEffort);
                    x1Rule.setDebtRemediationFunction(func);
                    x1Rule.setType(RuleType.valueOf(rule.getRuleType()));
                    x1Rule.setActivatedByDefault(true);
                }
                repository.done();
            }
            */

            NewRepository repository = context.createRepository("rust", Constants.LANGUAGE_KEY).setName("rust");
            NewRule x1Rule =
                    repository
                            .createRule("rule1")
                            .setName("rule1")
                            .setHtmlDescription("<p>A Test Rule</p>")
                            .addTags("rule1")
                            .setSeverity("MINOR" /* no idea what constants go here */);

            DebtRemediationFunction func = new DefaultDebtRemediationFunction(
                DebtRemediationFunction.Type.CONSTANT_ISSUE, // TODO lookup value
                null, // TODO values for gapMultiplier,
                "0d 0h 5min" // TODO values for baseEffort;
            );

            x1Rule.setDebtRemediationFunction(func);
            x1Rule.setType(RuleType.CODE_SMELL);
            x1Rule.setActivatedByDefault(true);

            repository.done();


            // TODO reach out to clippy for a list of rules
        }
        catch (Throwable e) {
          LOGGER.warn("Unexpected exception in RustRulesDefinition::define ", e);
        }
    }
}

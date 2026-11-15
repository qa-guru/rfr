package io.student.rangiffler.jupiter.probe;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

public final class ProbeTargetCondition implements ExecutionCondition {
  static final String PARAMETER = "rfr.jupiter.probe";

  @Override
  public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
    if (context.getConfigurationParameter(PARAMETER).filter("true"::equals).isPresent()) {
      return ConditionEvaluationResult.enabled("Probe target is unlocked for EngineTestKit");
    }
    return ConditionEvaluationResult.disabled("Probe target runs only through EngineTestKit");
  }
}

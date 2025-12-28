package com.oracle.graalvm.mx.run.test;

import com.intellij.execution.Executor;
import com.intellij.execution.testframework.TestConsoleProperties;
import com.intellij.execution.testframework.sm.SMCustomMessagesParsing;
import com.intellij.execution.testframework.sm.runner.OutputToGeneralTestEventsConverter;
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties;
import com.intellij.execution.testframework.sm.runner.SMTestLocator;
import com.intellij.execution.ui.ConsoleView;
import com.oracle.graalvm.mx.run.MxTestRunConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Console properties for MX test runner integration with SM Test Runner
 */
public class MxTestConsoleProperties extends SMTRunnerConsoleProperties implements SMCustomMessagesParsing {

    public MxTestConsoleProperties(@NotNull MxTestRunConfiguration configuration,
                                  @NotNull Executor executor) {
        super(configuration, "MX", executor);

        // Enable features
        setUsePredefinedMessageFilter(true);
        setIfUndefined(TestConsoleProperties.HIDE_PASSED_TESTS, false);
        setIfUndefined(TestConsoleProperties.HIDE_IGNORED_TEST, false);
        setIfUndefined(TestConsoleProperties.SCROLL_TO_SOURCE, true);
        setIfUndefined(TestConsoleProperties.SELECT_FIRST_DEFECT, true);
        setIfUndefined(TestConsoleProperties.TRACK_RUNNING_TEST, true);
    }

    @Override
    public @NotNull OutputToGeneralTestEventsConverter createTestEventsConverter(
            @NotNull String testFrameworkName,
            @NotNull TestConsoleProperties consoleProperties) {
        return new MxTestEventsConverter(testFrameworkName, consoleProperties);
    }

    @Override
    public @Nullable SMTestLocator getTestLocator() {
        return MxTestLocator.INSTANCE;
    }
}

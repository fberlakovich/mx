package com.oracle.graalvm.mx.run.test;

import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.openapi.util.Key;
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage;
import jetbrains.buildServer.messages.serviceMessages.ServiceMessageParserCallback;
import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses mx unittest output and converts it to TeamCity service messages
 * for display in IntelliJ's test runner window.
 *
 * MX unittest output formats:
 * - Test suite: "Running tests in com.example.TestClass"
 * - Test method: "testMethodName ... ok" or "testMethodName ... FAIL"
 * - Stack trace: indented lines starting with "  at"
 * - Summary: "OK (5 tests)" or "FAILED (failures=2)"
 */
public class MxTestOutputParser {

    // Pattern: Running tests in com.example.TestClass
    private static final Pattern SUITE_PATTERN = Pattern.compile(
        "^Running tests in ([\\w.]+)(?:\\s+\\((.+?)\\))?$"
    );

    // Pattern: testMethodName ... ok/FAIL/ERROR
    private static final Pattern TEST_PATTERN = Pattern.compile(
        "^(\\w+)\\s+\\.\\.\\.\\s+(ok|FAIL|ERROR|SKIP)$"
    );

    // Pattern: FAILED (failures=2, errors=1)
    private static final Pattern SUMMARY_PATTERN = Pattern.compile(
        "^(OK|FAILED)\\s+\\((.+)\\)$"
    );

    // Pattern: at com.example.TestClass.method(TestClass.java:42)
    private static final Pattern STACK_TRACE_PATTERN = Pattern.compile(
        "^\\s+at\\s+(.+)$"
    );

    // Pattern: AssertionError: expected 5 but was 3
    private static final Pattern ERROR_MESSAGE_PATTERN = Pattern.compile(
        "^(\\w+(?:Error|Exception)):\\s*(.+)$"
    );

    private final ServiceMessageParserCallback callback;
    private String currentSuite;
    private String currentTest;
    private StringBuilder currentError;
    private boolean inStackTrace;

    public MxTestOutputParser(ServiceMessageParserCallback callback) {
        this.callback = callback;
        this.currentError = new StringBuilder();
        this.inStackTrace = false;
    }

    /**
     * Process a line of test output
     */
    public void processLine(@NotNull String text, @NotNull Key outputType) {
        if (outputType != ProcessOutputTypes.STDOUT && outputType != ProcessOutputTypes.STDERR) {
            return;
        }

        String line = text.trim();
        if (line.isEmpty()) {
            return;
        }

        // Check for test suite start
        Matcher suiteMatcher = SUITE_PATTERN.matcher(line);
        if (suiteMatcher.matches()) {
            handleSuiteStart(suiteMatcher.group(1));
            return;
        }

        // Check for test result
        Matcher testMatcher = TEST_PATTERN.matcher(line);
        if (testMatcher.matches()) {
            handleTestResult(testMatcher.group(1), testMatcher.group(2));
            return;
        }

        // Check for summary
        Matcher summaryMatcher = SUMMARY_PATTERN.matcher(line);
        if (summaryMatcher.matches()) {
            handleSummary(summaryMatcher.group(1), summaryMatcher.group(2));
            return;
        }

        // Check for stack trace
        Matcher stackMatcher = STACK_TRACE_PATTERN.matcher(line);
        if (stackMatcher.matches() || inStackTrace) {
            handleStackTrace(line);
            return;
        }

        // Check for error message
        Matcher errorMatcher = ERROR_MESSAGE_PATTERN.matcher(line);
        if (errorMatcher.matches()) {
            handleErrorMessage(line);
            inStackTrace = true;
            return;
        }
    }

    private void handleSuiteStart(String suiteName) {
        // Finish previous test if any
        if (currentTest != null) {
            finishCurrentTest();
        }

        // Finish previous suite if any
        if (currentSuite != null) {
            sendServiceMessage("testSuiteFinished", "name", currentSuite);
        }

        currentSuite = suiteName;
        sendServiceMessage("testSuiteStarted", "name", suiteName);
    }

    private void handleTestResult(String testName, String result) {
        // Finish previous test if any
        if (currentTest != null) {
            finishCurrentTest();
        }

        currentTest = testName;

        // Start test
        sendServiceMessage("testStarted", "name", testName);

        // Handle result
        switch (result) {
            case "ok":
                // Test passed - will finish in finishCurrentTest()
                break;

            case "FAIL":
                // Test failed
                String failureMessage = currentError.length() > 0
                    ? currentError.toString()
                    : "Test failed";
                sendServiceMessage("testFailed",
                    "name", testName,
                    "message", failureMessage,
                    "details", currentError.toString());
                break;

            case "ERROR":
                // Test error
                String errorMessage = currentError.length() > 0
                    ? currentError.toString()
                    : "Test error";
                sendServiceMessage("testFailed",
                    "name", testName,
                    "message", errorMessage,
                    "details", currentError.toString(),
                    "error", "true");
                break;

            case "SKIP":
                // Test skipped
                sendServiceMessage("testIgnored",
                    "name", testName,
                    "message", "Test skipped");
                break;
        }

        // Clear error buffer
        currentError.setLength(0);
        inStackTrace = false;
    }

    private void handleStackTrace(String line) {
        if (currentError.length() > 0) {
            currentError.append("\n");
        }
        currentError.append(line);
    }

    private void handleErrorMessage(String line) {
        if (currentError.length() > 0) {
            currentError.append("\n");
        }
        currentError.append(line);
    }

    private void handleSummary(String result, String details) {
        // Finish current test if any
        if (currentTest != null) {
            finishCurrentTest();
        }

        // Finish current suite if any
        if (currentSuite != null) {
            sendServiceMessage("testSuiteFinished", "name", currentSuite);
            currentSuite = null;
        }
    }

    private void finishCurrentTest() {
        if (currentTest != null) {
            sendServiceMessage("testFinished", "name", currentTest);
            currentTest = null;
        }
    }

    /**
     * Call when test execution is complete
     */
    public void finish() {
        if (currentTest != null) {
            finishCurrentTest();
        }
        if (currentSuite != null) {
            sendServiceMessage("testSuiteFinished", "name", currentSuite);
            currentSuite = null;
        }
    }

    private void sendServiceMessage(String messageName, String... attributes) {
        try {
            StringBuilder message = new StringBuilder("##teamcity[");
            message.append(messageName);

            for (int i = 0; i < attributes.length; i += 2) {
                message.append(" ");
                message.append(attributes[i]);
                message.append("='");
                message.append(escape(attributes[i + 1]));
                message.append("'");
            }

            message.append("]\n");

            ServiceMessage serviceMessage = ServiceMessage.parse(message.toString());
            if (serviceMessage != null) {
                callback.parseServiceMessagesFromText(serviceMessage);
            }
        } catch (ParseException e) {
            // Ignore parsing errors
        }
    }

    private String escape(String text) {
        if (text == null) {
            return "";
        }
        return text
            .replace("|", "||")
            .replace("'", "|'")
            .replace("\n", "|n")
            .replace("\r", "|r")
            .replace("[", "|[")
            .replace("]", "|]");
    }
}

package com.oracle.graalvm.mx.run.test;

import com.intellij.execution.testframework.TestConsoleProperties;
import com.intellij.execution.testframework.sm.runner.OutputToGeneralTestEventsConverter;
import com.intellij.openapi.util.Key;
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage;
import jetbrains.buildServer.messages.serviceMessages.ServiceMessageParserCallback;
import org.jetbrains.annotations.NotNull;

import java.text.ParseException;

/**
 * Converts mx unittest output to test events for the test runner window
 */
public class MxTestEventsConverter extends OutputToGeneralTestEventsConverter {

    private final MxTestOutputParser parser;

    public MxTestEventsConverter(@NotNull String testFrameworkName,
                                @NotNull TestConsoleProperties consoleProperties) {
        super(testFrameworkName, consoleProperties);

        this.parser = new MxTestOutputParser(new ServiceMessageParserCallback() {
            @Override
            public void parseServiceMessagesFromText(@NotNull ServiceMessage message) {
                // Process the service message through the parent converter
                try {
                    MxTestEventsConverter.super.processServiceMessages(
                        message.asString(),
                        com.intellij.execution.process.ProcessOutputTypes.STDOUT,
                        null
                    );
                } catch (Exception e) {
                    // Ignore processing errors
                }
            }

            @Override
            public void parseServiceMessagesFromText(@NotNull String text, @NotNull Key outputType, @NotNull ServiceMessageParserCallback callback) throws ParseException {
                // Not used
            }
        });
    }

    @Override
    protected boolean processServiceMessages(@NotNull String text, @NotNull Key outputType, @NotNull ServiceMessageParserCallback visitor) throws ParseException {
        // First, let the parser process the line
        parser.processLine(text, outputType);

        // Also let parent handle any TeamCity messages that might already be in the output
        return super.processServiceMessages(text, outputType, visitor);
    }

    @Override
    public void onFinishTesting() {
        parser.finish();
        super.onFinishTesting();
    }
}

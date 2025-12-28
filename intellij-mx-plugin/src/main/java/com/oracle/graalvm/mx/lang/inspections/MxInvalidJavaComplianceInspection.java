package com.oracle.graalvm.mx.lang.inspections;

import com.intellij.codeInspection.*;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Inspection that validates javaCompliance values
 */
public class MxInvalidJavaComplianceInspection extends MxSuiteInspection {

    private static final Pattern JAVA_COMPLIANCE_PATTERN =
        Pattern.compile("['\"]javaCompliance['\"]\\s*:\\s*['\"]([^'\"]+)['\"]");

    private static final String[] VALID_VERSIONS = {
        "1.8", "8", "8+",
        "11", "11+",
        "17", "17+",
        "21", "21+",
        "22", "22+",
        "23", "23+"
    };

    @Override
    public @NotNull String getShortName() {
        return "MxInvalidJavaCompliance";
    }

    @Override
    public @NotNull String getDisplayName() {
        return "Invalid Java compliance version";
    }

    @Override
    public @NotNull String getGroupDisplayName() {
        return "MX Suite";
    }

    @Override
    protected ProblemDescriptor[] checkSuiteFile(@NotNull PsiFile file,
                                                @NotNull InspectionManager manager,
                                                boolean isOnTheFly) {
        List<ProblemDescriptor> problems = new ArrayList<>();
        String text = file.getText();

        Matcher matcher = JAVA_COMPLIANCE_PATTERN.matcher(text);
        while (matcher.find()) {
            String compliance = matcher.group(1);

            if (!isValidCompliance(compliance)) {
                problems.add(manager.createProblemDescriptor(
                    file,
                    "Invalid javaCompliance value: '" + compliance + "'. " +
                    "Expected format: '8+', '11+', '17+', '21+', etc.",
                    (LocalQuickFix) null,
                    ProblemHighlightType.ERROR,
                    isOnTheFly
                ));
            }
        }

        return problems.toArray(new ProblemDescriptor[0]);
    }

    private boolean isValidCompliance(String compliance) {
        // Check against known valid versions
        for (String valid : VALID_VERSIONS) {
            if (valid.equals(compliance)) {
                return true;
            }
        }

        // Check if it matches the pattern: number or number+
        return compliance.matches("\\d+\\+?");
    }
}

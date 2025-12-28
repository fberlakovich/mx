package com.oracle.graalvm.mx.lang.inspections;

import com.intellij.codeInspection.*;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Inspection that checks for missing required fields in suite.py
 */
public class MxMissingRequiredFieldInspection extends MxSuiteInspection {

    @Override
    public @NotNull String getShortName() {
        return "MxMissingRequiredField";
    }

    @Override
    public @NotNull String getDisplayName() {
        return "Missing required field in MX suite";
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

        // Check for required top-level fields
        if (!hasRequiredField(text, "name")) {
            problems.add(manager.createProblemDescriptor(
                file,
                "Suite is missing required 'name' field",
                (LocalQuickFix) null,
                ProblemHighlightType.ERROR,
                isOnTheFly
            ));
        }

        // Check projects for required fields
        checkProjectFields(text, file, manager, isOnTheFly, problems);

        // Check libraries for required fields
        checkLibraryFields(text, file, manager, isOnTheFly, problems);

        return problems.toArray(new ProblemDescriptor[0]);
    }

    private void checkProjectFields(String text, PsiFile file, InspectionManager manager,
                                   boolean isOnTheFly, List<ProblemDescriptor> problems) {
        // Find all project definitions
        Pattern projectPattern = Pattern.compile("\"projects\"\\s*:\\s*\\{([^}]+)\\}", Pattern.DOTALL);
        Matcher matcher = projectPattern.matcher(text);

        if (matcher.find()) {
            String projectsSection = matcher.group(1);

            // Check each project
            Pattern projectDefPattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\\{([^}]+)\\}");
            Matcher projectMatcher = projectDefPattern.matcher(projectsSection);

            while (projectMatcher.find()) {
                String projectName = projectMatcher.group(1);
                String projectDef = projectMatcher.group(2);

                // Projects should have sourceDirs
                if (!projectDef.matches("(?s).*['\"]sourceDirs['\"].*")) {
                    problems.add(manager.createProblemDescriptor(
                        file,
                        "Project '" + projectName + "' is missing 'sourceDirs' field",
                        (LocalQuickFix) null,
                        ProblemHighlightType.WARNING,
                        isOnTheFly
                    ));
                }

                // Projects should have javaCompliance
                if (!projectDef.matches("(?s).*['\"]javaCompliance['\"].*")) {
                    problems.add(manager.createProblemDescriptor(
                        file,
                        "Project '" + projectName + "' is missing 'javaCompliance' field",
                        (LocalQuickFix) null,
                        ProblemHighlightType.WARNING,
                        isOnTheFly
                    ));
                }
            }
        }
    }

    private void checkLibraryFields(String text, PsiFile file, InspectionManager manager,
                                   boolean isOnTheFly, List<ProblemDescriptor> problems) {
        // Find all library definitions
        Pattern libraryPattern = Pattern.compile("\"libraries\"\\s*:\\s*\\{([^}]+)\\}", Pattern.DOTALL);
        Matcher matcher = libraryPattern.matcher(text);

        if (matcher.find()) {
            String librariesSection = matcher.group(1);

            // Check each library
            Pattern libDefPattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\\{([^}]+)\\}");
            Matcher libMatcher = libDefPattern.matcher(librariesSection);

            while (libMatcher.find()) {
                String libName = libMatcher.group(1);
                String libDef = libMatcher.group(2);

                // Libraries should have either maven or urls
                boolean hasMaven = libDef.matches("(?s).*['\"]maven['\"].*");
                boolean hasUrls = libDef.matches("(?s).*['\"]urls['\"].*");

                if (!hasMaven && !hasUrls) {
                    problems.add(manager.createProblemDescriptor(
                        file,
                        "Library '" + libName + "' must have either 'maven' or 'urls' field",
                        (LocalQuickFix) null,
                        ProblemHighlightType.ERROR,
                        isOnTheFly
                    ));
                }
            }
        }
    }
}

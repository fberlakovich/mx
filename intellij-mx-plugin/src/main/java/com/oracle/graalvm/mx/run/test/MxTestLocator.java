package com.oracle.graalvm.mx.run.test;

import com.intellij.execution.Location;
import com.intellij.execution.PsiLocation;
import com.intellij.execution.testframework.sm.runner.SMTestLocator;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Locates test methods/classes in source code for navigation from test runner window
 */
public class MxTestLocator implements SMTestLocator {

    public static final MxTestLocator INSTANCE = new MxTestLocator();

    public static final String PROTOCOL = "mx";

    @NotNull
    @Override
    public List<Location> getLocation(@NotNull String protocol,
                                     @NotNull String path,
                                     @NotNull Project project,
                                     @NotNull GlobalSearchScope scope) {
        List<Location> locations = new ArrayList<>();

        if (!PROTOCOL.equals(protocol)) {
            return locations;
        }

        // Path format: "com.example.TestClass" or "com.example.TestClass.testMethod"
        String[] parts = path.split("\\.");
        if (parts.length < 2) {
            return locations;
        }

        // Check if last part is a method name (starts with lowercase)
        boolean hasMethod = parts.length > 0 && Character.isLowerCase(parts[parts.length - 1].charAt(0));

        String className;
        String methodName = null;

        if (hasMethod) {
            // Last part is method name
            methodName = parts[parts.length - 1];
            className = path.substring(0, path.lastIndexOf('.'));
        } else {
            // Path is class name
            className = path;
        }

        // Find the class
        JavaPsiFacade facade = JavaPsiFacade.getInstance(project);
        PsiClass psiClass = facade.findClass(className, scope);

        if (psiClass != null) {
            if (methodName != null) {
                // Find the method
                PsiMethod[] methods = psiClass.findMethodsByName(methodName, false);
                for (PsiMethod method : methods) {
                    locations.add(new PsiLocation<>(project, method));
                }
            } else {
                // Return class location
                locations.add(new PsiLocation<>(project, psiClass));
            }
        }

        return locations;
    }
}

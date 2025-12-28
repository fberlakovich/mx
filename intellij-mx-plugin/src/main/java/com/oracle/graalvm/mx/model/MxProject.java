package com.oracle.graalvm.mx.model;

import java.nio.file.Path;
import java.util.*;

/**
 * Represents an MX Java project
 */
public class MxProject {
    private String name;
    private String subDir;
    private List<String> sourceDirs = new ArrayList<>();
    private List<String> dependencies = new ArrayList<>();
    private String javaCompliance;
    private boolean testProject;
    private String workingSets;
    private String checkstyle;
    private List<String> annotationProcessors = new ArrayList<>();
    private MxSuite suite;
    private Path projectDir;

    public MxProject(String name, MxSuite suite) {
        this.name = name;
        this.suite = suite;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubDir() {
        return subDir;
    }

    public void setSubDir(String subDir) {
        this.subDir = subDir;
        if (suite != null) {
            this.projectDir = suite.getSuiteDir().resolve(subDir);
        }
    }

    public List<String> getSourceDirs() {
        return sourceDirs;
    }

    public void setSourceDirs(List<String> sourceDirs) {
        this.sourceDirs = sourceDirs;
    }

    public List<Path> getAbsoluteSourceDirs() {
        List<Path> result = new ArrayList<>();
        if (projectDir != null) {
            for (String srcDir : sourceDirs) {
                result.add(projectDir.resolve(srcDir));
            }
        }
        return result;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
    }

    public String getJavaCompliance() {
        return javaCompliance;
    }

    public void setJavaCompliance(String javaCompliance) {
        this.javaCompliance = javaCompliance;
    }

    public boolean isTestProject() {
        return testProject;
    }

    public void setTestProject(boolean testProject) {
        this.testProject = testProject;
    }

    public String getWorkingSets() {
        return workingSets;
    }

    public void setWorkingSets(String workingSets) {
        this.workingSets = workingSets;
    }

    public String getCheckstyle() {
        return checkstyle;
    }

    public void setCheckstyle(String checkstyle) {
        this.checkstyle = checkstyle;
    }

    public List<String> getAnnotationProcessors() {
        return annotationProcessors;
    }

    public void setAnnotationProcessors(List<String> annotationProcessors) {
        this.annotationProcessors = annotationProcessors;
    }

    public MxSuite getSuite() {
        return suite;
    }

    public Path getProjectDir() {
        return projectDir;
    }

    public Path getOutputDir() {
        if (suite != null) {
            return suite.getSuiteDir().resolve("mxbuild").resolve(name).resolve("bin");
        }
        return null;
    }

    public Path getSourceGenDir() {
        if (suite != null) {
            return suite.getSuiteDir().resolve("mxbuild").resolve(name).resolve("src_gen");
        }
        return null;
    }
}

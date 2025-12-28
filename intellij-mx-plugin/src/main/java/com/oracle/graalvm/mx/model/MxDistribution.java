package com.oracle.graalvm.mx.model;

import java.nio.file.Path;
import java.util.*;

/**
 * Represents an MX distribution (JAR, TAR, ZIP, etc.)
 */
public class MxDistribution {
    private String name;
    private List<String> dependencies = new ArrayList<>();
    private List<String> excludes = new ArrayList<>();
    private boolean platformDependent;
    private boolean testDistribution;
    private String distType; // JAR, TAR, ZIP, etc.
    private MxSuite suite;

    public MxDistribution(String name, MxSuite suite) {
        this.name = name;
        this.suite = suite;
    }

    public String getName() {
        return name;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
    }

    public List<String> getExcludes() {
        return excludes;
    }

    public void setExcludes(List<String> excludes) {
        this.excludes = excludes;
    }

    public boolean isPlatformDependent() {
        return platformDependent;
    }

    public void setPlatformDependent(boolean platformDependent) {
        this.platformDependent = platformDependent;
    }

    public boolean isTestDistribution() {
        return testDistribution;
    }

    public void setTestDistribution(boolean testDistribution) {
        this.testDistribution = testDistribution;
    }

    public String getDistType() {
        return distType;
    }

    public void setDistType(String distType) {
        this.distType = distType;
    }

    public MxSuite getSuite() {
        return suite;
    }

    public Path getOutputPath() {
        if (suite != null) {
            return suite.getSuiteDir().resolve("mxbuild").resolve("dists");
        }
        return null;
    }
}

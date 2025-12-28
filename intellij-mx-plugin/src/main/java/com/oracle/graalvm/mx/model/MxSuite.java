package com.oracle.graalvm.mx.model;

import java.nio.file.Path;
import java.util.*;

/**
 * Represents an MX suite (parsed from suite.py)
 */
public class MxSuite {
    private String name;
    private String mxVersion;
    private Path suiteDir;
    private Path mxDir;
    private List<MxProject> projects = new ArrayList<>();
    private List<MxLibrary> libraries = new ArrayList<>();
    private List<MxDistribution> distributions = new ArrayList<>();
    private List<MxSuiteImport> imports = new ArrayList<>();
    private Map<String, Object> rawData = new HashMap<>();

    public MxSuite(String name, Path suiteDir) {
        this.name = name;
        this.suiteDir = suiteDir;
        this.mxDir = suiteDir.resolve("mx." + name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMxVersion() {
        return mxVersion;
    }

    public void setMxVersion(String mxVersion) {
        this.mxVersion = mxVersion;
    }

    public Path getSuiteDir() {
        return suiteDir;
    }

    public Path getMxDir() {
        return mxDir;
    }

    public Path getSuiteFile() {
        return mxDir.resolve("suite.py");
    }

    public List<MxProject> getProjects() {
        return projects;
    }

    public void setProjects(List<MxProject> projects) {
        this.projects = projects;
    }

    public List<MxLibrary> getLibraries() {
        return libraries;
    }

    public void setLibraries(List<MxLibrary> libraries) {
        this.libraries = libraries;
    }

    public List<MxDistribution> getDistributions() {
        return distributions;
    }

    public void setDistributions(List<MxDistribution> distributions) {
        this.distributions = distributions;
    }

    public List<MxSuiteImport> getImports() {
        return imports;
    }

    public void setImports(List<MxSuiteImport> imports) {
        this.imports = imports;
    }

    public Map<String, Object> getRawData() {
        return rawData;
    }

    public void setRawData(Map<String, Object> rawData) {
        this.rawData = rawData;
    }

    public MxProject findProject(String name) {
        return projects.stream()
                .filter(p -> p.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public MxLibrary findLibrary(String name) {
        return libraries.stream()
                .filter(l -> l.getName().equals(name))
                .findFirst()
                .orElse(null);
    }
}

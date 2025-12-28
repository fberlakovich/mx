package com.oracle.graalvm.mx.model;

import java.util.*;

/**
 * Represents an MX library dependency
 */
public class MxLibrary {
    private String name;
    private List<String> urls = new ArrayList<>();
    private String digest;
    private MavenCoordinate maven;
    private String license;
    private boolean optional;
    private List<String> dependencies = new ArrayList<>();

    public MxLibrary(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public MavenCoordinate getMaven() {
        return maven;
    }

    public void setMaven(MavenCoordinate maven) {
        this.maven = maven;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
    }

    public static class MavenCoordinate {
        private String groupId;
        private String artifactId;
        private String version;

        public MavenCoordinate(String groupId, String artifactId, String version) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
        }

        public String getGroupId() {
            return groupId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public String getVersion() {
            return version;
        }

        public String getCoordinateString() {
            return groupId + ":" + artifactId + ":" + version;
        }
    }
}

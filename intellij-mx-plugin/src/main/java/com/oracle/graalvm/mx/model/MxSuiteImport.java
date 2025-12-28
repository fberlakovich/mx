package com.oracle.graalvm.mx.model;

/**
 * Represents an imported MX suite
 */
public class MxSuiteImport {
    private String name;
    private String version;
    private String url;
    private boolean dynamic;

    public MxSuiteImport(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }
}

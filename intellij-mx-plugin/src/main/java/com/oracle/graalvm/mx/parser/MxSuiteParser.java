package com.oracle.graalvm.mx.parser;

import com.oracle.graalvm.mx.model.*;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Parser for suite.py files using GraalPy
 */
public class MxSuiteParser {

    /**
     * Parse a suite.py file and create an MxSuite object
     */
    public static MxSuite parseSuite(Path suiteFile) throws IOException {
        if (!Files.exists(suiteFile)) {
            throw new IOException("Suite file not found: " + suiteFile);
        }

        Path suiteDir = suiteFile.getParent().getParent();
        String suiteName = suiteFile.getParent().getFileName().toString();
        if (suiteName.startsWith("mx.")) {
            suiteName = suiteName.substring(3);
        }

        MxSuite suite = new MxSuite(suiteName, suiteDir);

        try (Context context = Context.newBuilder("python")
                .allowAllAccess(true)
                .option("python.ForceImportSite", "false")
                .build()) {

            // Execute the suite.py file
            String content = Files.readString(suiteFile);
            context.eval("python", content);

            // Get the 'suite' dictionary
            Value bindings = context.getBindings("python");
            Value suiteValue = bindings.getMember("suite");

            if (suiteValue == null || !suiteValue.hasHashEntries()) {
                throw new IOException("Invalid suite.py: missing 'suite' dictionary");
            }

            // Parse basic suite info
            parseSuiteBasics(suite, suiteValue);

            // Parse projects
            parseProjects(suite, suiteValue);

            // Parse libraries
            parseLibraries(suite, suiteValue);

            // Parse distributions
            parseDistributions(suite, suiteValue);

            // Parse imports
            parseImports(suite, suiteValue);

            return suite;
        } catch (Exception e) {
            throw new IOException("Failed to parse suite.py: " + e.getMessage(), e);
        }
    }

    private static void parseSuiteBasics(MxSuite suite, Value suiteDict) {
        if (suiteDict.hasMember("name")) {
            suite.setName(suiteDict.getMember("name").asString());
        }

        if (suiteDict.hasMember("mxversion")) {
            suite.setMxVersion(suiteDict.getMember("mxversion").asString());
        }
    }

    private static void parseProjects(MxSuite suite, Value suiteDict) {
        if (!suiteDict.hasMember("projects")) {
            return;
        }

        Value projectsDict = suiteDict.getMember("projects");
        if (projectsDict == null || !projectsDict.hasHashEntries()) {
            return;
        }

        List<MxProject> projects = new ArrayList<>();

        for (String projectName : projectsDict.getMemberKeys()) {
            Value projectData = projectsDict.getMember(projectName);
            if (projectData != null && projectData.hasHashEntries()) {
                MxProject project = parseProject(projectName, projectData, suite);
                projects.add(project);
            }
        }

        suite.setProjects(projects);
    }

    private static MxProject parseProject(String name, Value projectDict, MxSuite suite) {
        MxProject project = new MxProject(name, suite);

        // SubDir
        if (projectDict.hasMember("subDir")) {
            project.setSubDir(projectDict.getMember("subDir").asString());
        }

        // Source directories
        if (projectDict.hasMember("sourceDirs")) {
            Value sourceDirs = projectDict.getMember("sourceDirs");
            if (sourceDirs.hasArrayElements()) {
                List<String> sourceDirList = new ArrayList<>();
                for (long i = 0; i < sourceDirs.getArraySize(); i++) {
                    sourceDirList.add(sourceDirs.getArrayElement(i).asString());
                }
                project.setSourceDirs(sourceDirList);
            }
        }

        // Dependencies
        if (projectDict.hasMember("dependencies")) {
            Value deps = projectDict.getMember("dependencies");
            if (deps.hasArrayElements()) {
                List<String> depList = new ArrayList<>();
                for (long i = 0; i < deps.getArraySize(); i++) {
                    depList.add(deps.getArrayElement(i).asString());
                }
                project.setDependencies(depList);
            }
        }

        // Java compliance
        if (projectDict.hasMember("javaCompliance")) {
            Value compliance = projectDict.getMember("javaCompliance");
            project.setJavaCompliance(compliance.isString() ? compliance.asString() : compliance.toString());
        }

        // Test project
        if (projectDict.hasMember("testProject")) {
            project.setTestProject(projectDict.getMember("testProject").asBoolean());
        }

        // Working sets
        if (projectDict.hasMember("workingSets")) {
            project.setWorkingSets(projectDict.getMember("workingSets").asString());
        }

        // Annotation processors
        if (projectDict.hasMember("annotationProcessors")) {
            Value aps = projectDict.getMember("annotationProcessors");
            if (aps.hasArrayElements()) {
                List<String> apList = new ArrayList<>();
                for (long i = 0; i < aps.getArraySize(); i++) {
                    apList.add(aps.getArrayElement(i).asString());
                }
                project.setAnnotationProcessors(apList);
            }
        }

        return project;
    }

    private static void parseLibraries(MxSuite suite, Value suiteDict) {
        if (!suiteDict.hasMember("libraries")) {
            return;
        }

        Value librariesDict = suiteDict.getMember("libraries");
        if (librariesDict == null || !librariesDict.hasHashEntries()) {
            return;
        }

        List<MxLibrary> libraries = new ArrayList<>();

        for (String libName : librariesDict.getMemberKeys()) {
            Value libData = librariesDict.getMember(libName);
            if (libData != null && libData.hasHashEntries()) {
                MxLibrary library = parseLibrary(libName, libData);
                libraries.add(library);
            }
        }

        suite.setLibraries(libraries);
    }

    private static MxLibrary parseLibrary(String name, Value libDict) {
        MxLibrary library = new MxLibrary(name);

        // URLs
        if (libDict.hasMember("urls")) {
            Value urls = libDict.getMember("urls");
            if (urls.hasArrayElements()) {
                List<String> urlList = new ArrayList<>();
                for (long i = 0; i < urls.getArraySize(); i++) {
                    urlList.add(urls.getArrayElement(i).asString());
                }
                library.setUrls(urlList);
            }
        }

        // Digest
        if (libDict.hasMember("digest")) {
            library.setDigest(libDict.getMember("digest").asString());
        }

        // Maven coordinates
        if (libDict.hasMember("maven")) {
            Value maven = libDict.getMember("maven");
            if (maven.hasHashEntries()) {
                String groupId = getStringMember(maven, "groupId");
                String artifactId = getStringMember(maven, "artifactId");
                String version = getStringMember(maven, "version");

                if (groupId != null && artifactId != null && version != null) {
                    library.setMaven(new MxLibrary.MavenCoordinate(groupId, artifactId, version));
                }
            }
        }

        // License
        if (libDict.hasMember("license")) {
            library.setLicense(libDict.getMember("license").asString());
        }

        // Optional
        if (libDict.hasMember("optional")) {
            library.setOptional(libDict.getMember("optional").asBoolean());
        }

        return library;
    }

    private static void parseDistributions(MxSuite suite, Value suiteDict) {
        if (!suiteDict.hasMember("distributions")) {
            return;
        }

        Value distsDict = suiteDict.getMember("distributions");
        if (distsDict == null || !distsDict.hasHashEntries()) {
            return;
        }

        List<MxDistribution> distributions = new ArrayList<>();

        for (String distName : distsDict.getMemberKeys()) {
            Value distData = distsDict.getMember(distName);
            if (distData != null && distData.hasHashEntries()) {
                MxDistribution dist = parseDistribution(distName, distData, suite);
                distributions.add(dist);
            }
        }

        suite.setDistributions(distributions);
    }

    private static MxDistribution parseDistribution(String name, Value distDict, MxSuite suite) {
        MxDistribution dist = new MxDistribution(name, suite);

        // Dependencies
        if (distDict.hasMember("dependencies")) {
            Value deps = distDict.getMember("dependencies");
            if (deps.hasArrayElements()) {
                List<String> depList = new ArrayList<>();
                for (long i = 0; i < deps.getArraySize(); i++) {
                    depList.add(deps.getArrayElement(i).asString());
                }
                dist.setDependencies(depList);
            }
        }

        // Excludes
        if (distDict.hasMember("excludes")) {
            Value excludes = distDict.getMember("excludes");
            if (excludes.hasArrayElements()) {
                List<String> excludeList = new ArrayList<>();
                for (long i = 0; i < excludes.getArraySize(); i++) {
                    excludeList.add(excludes.getArrayElement(i).asString());
                }
                dist.setExcludes(excludeList);
            }
        }

        // Platform dependent
        if (distDict.hasMember("platformDependent")) {
            dist.setPlatformDependent(distDict.getMember("platformDependent").asBoolean());
        }

        // Test distribution
        if (distDict.hasMember("testDistribution")) {
            dist.setTestDistribution(distDict.getMember("testDistribution").asBoolean());
        }

        return dist;
    }

    private static void parseImports(MxSuite suite, Value suiteDict) {
        if (!suiteDict.hasMember("imports")) {
            return;
        }

        Value importsDict = suiteDict.getMember("imports");
        if (importsDict == null || !importsDict.hasHashEntries()) {
            return;
        }

        if (!importsDict.hasMember("suites")) {
            return;
        }

        Value suitesArray = importsDict.getMember("suites");
        if (suitesArray == null || !suitesArray.hasArrayElements()) {
            return;
        }

        List<MxSuiteImport> imports = new ArrayList<>();
        for (long i = 0; i < suitesArray.getArraySize(); i++) {
            Value importData = suitesArray.getArrayElement(i);
            if (importData.hasHashEntries()) {
                MxSuiteImport suiteImport = parseSuiteImport(importData);
                imports.add(suiteImport);
            }
        }

        suite.setImports(imports);
    }

    private static MxSuiteImport parseSuiteImport(Value importDict) {
        String name = getStringMember(importDict, "name");
        MxSuiteImport suiteImport = new MxSuiteImport(name);

        String version = getStringMember(importDict, "version");
        if (version != null) {
            suiteImport.setVersion(version);
        }

        String url = getStringMember(importDict, "url");
        if (url != null) {
            suiteImport.setUrl(url);
        }

        if (importDict.hasMember("dynamic")) {
            suiteImport.setDynamic(importDict.getMember("dynamic").asBoolean());
        }

        return suiteImport;
    }

    private static String getStringMember(Value dict, String key) {
        if (dict.hasMember(key)) {
            Value value = dict.getMember(key);
            return value.isString() ? value.asString() : value.toString();
        }
        return null;
    }
}

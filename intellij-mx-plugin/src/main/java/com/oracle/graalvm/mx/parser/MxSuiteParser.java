package com.oracle.graalvm.mx.parser;

import com.oracle.graalvm.mx.model.*;
import org.python.core.*;
import org.python.util.PythonInterpreter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Parser for suite.py files using Jython
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

        try (PythonInterpreter interp = new PythonInterpreter()) {
            // Execute the suite.py file
            String content = Files.readString(suiteFile);
            interp.exec(content);

            // Get the 'suite' dictionary
            PyObject suiteObj = interp.get("suite");
            if (suiteObj == null || !(suiteObj instanceof PyDictionary)) {
                throw new IOException("Invalid suite.py: missing 'suite' dictionary");
            }

            PyDictionary suiteDict = (PyDictionary) suiteObj;

            // Parse basic suite info
            parseSuiteBasics(suite, suiteDict);

            // Parse projects
            parseProjects(suite, suiteDict);

            // Parse libraries
            parseLibraries(suite, suiteDict);

            // Parse distributions
            parseDistributions(suite, suiteDict);

            // Parse imports
            parseImports(suite, suiteDict);

            return suite;
        } catch (Exception e) {
            throw new IOException("Failed to parse suite.py: " + e.getMessage(), e);
        }
    }

    private static void parseSuiteBasics(MxSuite suite, PyDictionary suiteDict) {
        PyObject nameObj = suiteDict.get(new PyString("name"));
        if (nameObj != null) {
            suite.setName(nameObj.toString());
        }

        PyObject mxVersionObj = suiteDict.get(new PyString("mxversion"));
        if (mxVersionObj != null) {
            suite.setMxVersion(mxVersionObj.toString());
        }
    }

    private static void parseProjects(MxSuite suite, PyDictionary suiteDict) {
        PyObject projectsObj = suiteDict.get(new PyString("projects"));
        if (projectsObj == null || !(projectsObj instanceof PyDictionary)) {
            return;
        }

        PyDictionary projectsDict = (PyDictionary) projectsObj;
        List<MxProject> projects = new ArrayList<>();

        for (Object key : projectsDict.keys()) {
            String projectName = key.toString();
            PyObject projectDataObj = projectsDict.get(new PyString(projectName));

            if (projectDataObj instanceof PyDictionary) {
                MxProject project = parseProject(projectName, (PyDictionary) projectDataObj, suite);
                projects.add(project);
            }
        }

        suite.setProjects(projects);
    }

    private static MxProject parseProject(String name, PyDictionary projectDict, MxSuite suite) {
        MxProject project = new MxProject(name, suite);

        // SubDir
        PyObject subDirObj = projectDict.get(new PyString("subDir"));
        if (subDirObj != null) {
            project.setSubDir(subDirObj.toString());
        }

        // Source directories
        PyObject sourceDirsObj = projectDict.get(new PyString("sourceDirs"));
        if (sourceDirsObj instanceof PyList) {
            List<String> sourceDirs = new ArrayList<>();
            for (Object item : (PyList) sourceDirsObj) {
                sourceDirs.add(item.toString());
            }
            project.setSourceDirs(sourceDirs);
        }

        // Dependencies
        PyObject depsObj = projectDict.get(new PyString("dependencies"));
        if (depsObj instanceof PyList) {
            List<String> deps = new ArrayList<>();
            for (Object item : (PyList) depsObj) {
                deps.add(item.toString());
            }
            project.setDependencies(deps);
        }

        // Java compliance
        PyObject javaComplianceObj = projectDict.get(new PyString("javaCompliance"));
        if (javaComplianceObj != null) {
            project.setJavaCompliance(javaComplianceObj.toString());
        }

        // Test project
        PyObject testProjectObj = projectDict.get(new PyString("testProject"));
        if (testProjectObj != null) {
            project.setTestProject(Boolean.parseBoolean(testProjectObj.toString()));
        }

        // Working sets
        PyObject workingSetsObj = projectDict.get(new PyString("workingSets"));
        if (workingSetsObj != null) {
            project.setWorkingSets(workingSetsObj.toString());
        }

        // Annotation processors
        PyObject annotationProcessorsObj = projectDict.get(new PyString("annotationProcessors"));
        if (annotationProcessorsObj instanceof PyList) {
            List<String> aps = new ArrayList<>();
            for (Object item : (PyList) annotationProcessorsObj) {
                aps.add(item.toString());
            }
            project.setAnnotationProcessors(aps);
        }

        return project;
    }

    private static void parseLibraries(MxSuite suite, PyDictionary suiteDict) {
        PyObject librariesObj = suiteDict.get(new PyString("libraries"));
        if (librariesObj == null || !(librariesObj instanceof PyDictionary)) {
            return;
        }

        PyDictionary librariesDict = (PyDictionary) librariesObj;
        List<MxLibrary> libraries = new ArrayList<>();

        for (Object key : librariesDict.keys()) {
            String libName = key.toString();
            PyObject libDataObj = librariesDict.get(new PyString(libName));

            if (libDataObj instanceof PyDictionary) {
                MxLibrary library = parseLibrary(libName, (PyDictionary) libDataObj);
                libraries.add(library);
            }
        }

        suite.setLibraries(libraries);
    }

    private static MxLibrary parseLibrary(String name, PyDictionary libDict) {
        MxLibrary library = new MxLibrary(name);

        // URLs
        PyObject urlsObj = libDict.get(new PyString("urls"));
        if (urlsObj instanceof PyList) {
            List<String> urls = new ArrayList<>();
            for (Object item : (PyList) urlsObj) {
                urls.add(item.toString());
            }
            library.setUrls(urls);
        }

        // Digest
        PyObject digestObj = libDict.get(new PyString("digest"));
        if (digestObj != null) {
            library.setDigest(digestObj.toString());
        }

        // Maven coordinates
        PyObject mavenObj = libDict.get(new PyString("maven"));
        if (mavenObj instanceof PyDictionary) {
            PyDictionary mavenDict = (PyDictionary) mavenObj;
            String groupId = getString(mavenDict, "groupId");
            String artifactId = getString(mavenDict, "artifactId");
            String version = getString(mavenDict, "version");

            if (groupId != null && artifactId != null && version != null) {
                library.setMaven(new MxLibrary.MavenCoordinate(groupId, artifactId, version));
            }
        }

        // License
        PyObject licenseObj = libDict.get(new PyString("license"));
        if (licenseObj != null) {
            library.setLicense(licenseObj.toString());
        }

        // Optional
        PyObject optionalObj = libDict.get(new PyString("optional"));
        if (optionalObj != null) {
            library.setOptional(Boolean.parseBoolean(optionalObj.toString()));
        }

        return library;
    }

    private static void parseDistributions(MxSuite suite, PyDictionary suiteDict) {
        PyObject distsObj = suiteDict.get(new PyString("distributions"));
        if (distsObj == null || !(distsObj instanceof PyDictionary)) {
            return;
        }

        PyDictionary distsDict = (PyDictionary) distsObj;
        List<MxDistribution> distributions = new ArrayList<>();

        for (Object key : distsDict.keys()) {
            String distName = key.toString();
            PyObject distDataObj = distsDict.get(new PyString(distName));

            if (distDataObj instanceof PyDictionary) {
                MxDistribution dist = parseDistribution(distName, (PyDictionary) distDataObj, suite);
                distributions.add(dist);
            }
        }

        suite.setDistributions(distributions);
    }

    private static MxDistribution parseDistribution(String name, PyDictionary distDict, MxSuite suite) {
        MxDistribution dist = new MxDistribution(name, suite);

        // Dependencies
        PyObject depsObj = distDict.get(new PyString("dependencies"));
        if (depsObj instanceof PyList) {
            List<String> deps = new ArrayList<>();
            for (Object item : (PyList) depsObj) {
                deps.add(item.toString());
            }
            dist.setDependencies(deps);
        }

        // Excludes
        PyObject excludesObj = distDict.get(new PyString("excludes"));
        if (excludesObj instanceof PyList) {
            List<String> excludes = new ArrayList<>();
            for (Object item : (PyList) excludesObj) {
                excludes.add(item.toString());
            }
            dist.setExcludes(excludes);
        }

        // Platform dependent
        PyObject platformDepObj = distDict.get(new PyString("platformDependent"));
        if (platformDepObj != null) {
            dist.setPlatformDependent(Boolean.parseBoolean(platformDepObj.toString()));
        }

        // Test distribution
        PyObject testDistObj = distDict.get(new PyString("testDistribution"));
        if (testDistObj != null) {
            dist.setTestDistribution(Boolean.parseBoolean(testDistObj.toString()));
        }

        return dist;
    }

    private static void parseImports(MxSuite suite, PyDictionary suiteDict) {
        PyObject importsObj = suiteDict.get(new PyString("imports"));
        if (importsObj == null || !(importsObj instanceof PyDictionary)) {
            return;
        }

        PyDictionary importsDict = (PyDictionary) importsObj;
        PyObject suitesObj = importsDict.get(new PyString("suites"));

        if (suitesObj == null || !(suitesObj instanceof PyList)) {
            return;
        }

        List<MxSuiteImport> imports = new ArrayList<>();
        for (Object item : (PyList) suitesObj) {
            if (item instanceof PyDictionary) {
                MxSuiteImport suiteImport = parseSuiteImport((PyDictionary) item);
                imports.add(suiteImport);
            }
        }

        suite.setImports(imports);
    }

    private static MxSuiteImport parseSuiteImport(PyDictionary importDict) {
        String name = getString(importDict, "name");
        MxSuiteImport suiteImport = new MxSuiteImport(name);

        String version = getString(importDict, "version");
        if (version != null) {
            suiteImport.setVersion(version);
        }

        String url = getString(importDict, "url");
        if (url != null) {
            suiteImport.setUrl(url);
        }

        PyObject dynamicObj = importDict.get(new PyString("dynamic"));
        if (dynamicObj != null) {
            suiteImport.setDynamic(Boolean.parseBoolean(dynamicObj.toString()));
        }

        return suiteImport;
    }

    private static String getString(PyDictionary dict, String key) {
        PyObject obj = dict.get(new PyString(key));
        return obj != null ? obj.toString() : null;
    }
}

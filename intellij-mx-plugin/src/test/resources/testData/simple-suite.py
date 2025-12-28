suite = {
    "name": "test-suite",
    "version": "1.0.0",
    "mxversion": "6.0.0",

    "projects": {
        "com.example.project": {
            "subDir": "src",
            "sourceDirs": ["src"],
            "dependencies": ["library:JUNIT"],
            "javaCompliance": "17+",
        },
        "com.example.project.test": {
            "subDir": "test",
            "sourceDirs": ["src"],
            "dependencies": ["com.example.project", "library:JUNIT"],
            "javaCompliance": "17+",
            "testProject": True,
        },
    },

    "libraries": {
        "JUNIT": {
            "maven": {
                "groupId": "junit",
                "artifactId": "junit",
                "version": "4.13.2",
            },
        },
        "MOCKITO": {
            "maven": {
                "groupId": "org.mockito",
                "artifactId": "mockito-core",
                "version": "5.8.0",
            },
        },
    },

    "distributions": {
        "TEST_DIST": {
            "subDir": "dist",
            "dependencies": ["com.example.project"],
            "distDependencies": [],
            "maven": {
                "groupId": "com.example",
                "artifactId": "test-dist",
                "version": "1.0.0",
            },
        },
    },
}

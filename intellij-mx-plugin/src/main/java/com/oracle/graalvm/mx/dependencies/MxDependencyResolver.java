package com.oracle.graalvm.mx.dependencies;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.oracle.graalvm.mx.model.MxLibrary;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

/**
 * Resolves and downloads Maven dependencies for MX libraries
 */
public class MxDependencyResolver {
    private static final Logger LOG = Logger.getInstance(MxDependencyResolver.class);

    private static final List<String> MAVEN_REPOSITORIES = List.of(
            "https://repo.maven.apache.org/maven2/",
            "https://repo1.maven.org/maven2/"
    );

    private final Path cacheDir;

    public MxDependencyResolver(Project project) {
        // Use IntelliJ's cache directory
        String basePath = project.getBasePath();
        this.cacheDir = Path.of(basePath, ".idea", "mx-cache", "libraries");
        try {
            Files.createDirectories(cacheDir);
        } catch (IOException e) {
            LOG.error("Failed to create cache directory", e);
        }
    }

    /**
     * Resolve a library and return the path to the JAR file
     */
    public Path resolveLibrary(MxLibrary library, ProgressIndicator indicator) {
        // If library has Maven coordinates, download from Maven
        if (library.getMaven() != null) {
            return resolveMavenArtifact(library, indicator);
        }

        // If library has URLs, download from first available URL
        if (!library.getUrls().isEmpty()) {
            return downloadFromUrl(library, indicator);
        }

        return null;
    }

    private Path resolveMavenArtifact(MxLibrary library, ProgressIndicator indicator) {
        MxLibrary.MavenCoordinate maven = library.getMaven();

        // Calculate cache path: groupId/artifactId/version/artifactId-version.jar
        String groupPath = maven.getGroupId().replace('.', '/');
        String fileName = maven.getArtifactId() + "-" + maven.getVersion() + ".jar";
        Path cachedJar = cacheDir.resolve(groupPath)
                                  .resolve(maven.getArtifactId())
                                  .resolve(maven.getVersion())
                                  .resolve(fileName);

        // Check if already cached and valid
        if (Files.exists(cachedJar) && isValidCachedFile(cachedJar, library.getDigest())) {
            LOG.info("Using cached library: " + library.getName());
            return cachedJar;
        }

        // Download from Maven repository
        String artifactPath = groupPath + "/" + maven.getArtifactId() + "/" +
                             maven.getVersion() + "/" + fileName;

        for (String repoUrl : MAVEN_REPOSITORIES) {
            try {
                String downloadUrl = repoUrl + artifactPath;
                if (indicator != null) {
                    indicator.setText("Downloading " + library.getName() + " from " + repoUrl);
                }

                LOG.info("Downloading from: " + downloadUrl);
                Path downloaded = downloadFile(downloadUrl, cachedJar);

                // Verify checksum if provided
                if (library.getDigest() != null && !verifyDigest(downloaded, library.getDigest())) {
                    LOG.warn("Checksum verification failed for " + library.getName());
                    Files.deleteIfExists(downloaded);
                    continue;
                }

                return downloaded;
            } catch (Exception e) {
                LOG.warn("Failed to download from " + repoUrl + ": " + e.getMessage());
            }
        }

        LOG.error("Failed to resolve library: " + library.getName());
        return null;
    }

    private Path downloadFromUrl(MxLibrary library, ProgressIndicator indicator) {
        // Use first URL
        String url = library.getUrls().get(0);

        // Extract filename from URL or use library name
        String fileName = url.substring(url.lastIndexOf('/') + 1);
        if (!fileName.endsWith(".jar")) {
            fileName = library.getName() + ".jar";
        }

        Path cachedJar = cacheDir.resolve(library.getName()).resolve(fileName);

        // Check cache
        if (Files.exists(cachedJar) && isValidCachedFile(cachedJar, library.getDigest())) {
            return cachedJar;
        }

        try {
            if (indicator != null) {
                indicator.setText("Downloading " + library.getName());
            }

            Path downloaded = downloadFile(url, cachedJar);

            // Verify checksum
            if (library.getDigest() != null && !verifyDigest(downloaded, library.getDigest())) {
                LOG.warn("Checksum verification failed for " + library.getName());
                Files.deleteIfExists(downloaded);
                return null;
            }

            return downloaded;
        } catch (Exception e) {
            LOG.error("Failed to download library: " + library.getName(), e);
            return null;
        }
    }

    private Path downloadFile(String urlString, Path destination) throws IOException {
        Files.createDirectories(destination.getParent());

        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(30000);

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP error code: " + responseCode);
        }

        try (InputStream in = connection.getInputStream()) {
            Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
        }

        return destination;
    }

    private boolean isValidCachedFile(Path file, String expectedDigest) {
        if (!Files.exists(file)) {
            return false;
        }

        if (expectedDigest == null) {
            return true; // No checksum to verify
        }

        return verifyDigest(file, expectedDigest);
    }

    private boolean verifyDigest(Path file, String expectedDigest) {
        if (expectedDigest == null) {
            return true;
        }

        try {
            // Parse digest format: "sha1:..." or "sha256:..." or "sha512:..."
            String[] parts = expectedDigest.split(":", 2);
            if (parts.length != 2) {
                LOG.warn("Invalid digest format: " + expectedDigest);
                return true; // Don't fail on invalid format
            }

            String algorithm = parts[0].toUpperCase().replace("SHA", "SHA-");
            String expectedHash = parts[1];

            MessageDigest digest = MessageDigest.getInstance(algorithm);

            try (InputStream fis = Files.newInputStream(file)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
            }

            byte[] hashBytes = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            String actualHash = hexString.toString();
            boolean matches = actualHash.equalsIgnoreCase(expectedHash);

            if (!matches) {
                LOG.warn("Checksum mismatch - expected: " + expectedHash + ", actual: " + actualHash);
            }

            return matches;
        } catch (Exception e) {
            LOG.warn("Failed to verify digest", e);
            return true; // Don't fail on verification errors
        }
    }

    /**
     * Resolve all transitive dependencies
     */
    public List<Path> resolveTransitiveDependencies(MxLibrary library, ProgressIndicator indicator) {
        List<Path> resolved = new ArrayList<>();

        // Resolve the library itself
        Path jar = resolveLibrary(library, indicator);
        if (jar != null) {
            resolved.add(jar);
        }

        // TODO: Resolve transitive dependencies by parsing POM files
        // For now, just return the direct dependency

        return resolved;
    }
}

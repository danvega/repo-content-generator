package dev.danvega.cg.gh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

/**
 * Service class for interacting with GitHub API and downloading repository contents.
 * This service allows downloading specified file types from a GitHub repository,
 * with support for both include and exclude patterns.
 */
@Service
public class GitHubService {

    private static final Logger log = LoggerFactory.getLogger(GitHubService.class);
    private final RestClient restClient;
    private final GitHubConfiguration config;

    /**
     * Constructs a new GithubService with the specified dependencies.
     *
     * @param builder The RestClient.Builder to use for creating the RestClient.
     * @param config  The GitHub configuration properties.
     */
    public GitHubService(RestClient.Builder builder,
                         GitHubConfiguration config) {
        this.config = config;
        this.restClient = builder
                .baseUrl("https://api.github.com")
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .defaultHeader("Authorization", "Bearer " + config.token())  // Remove the colon after Bearer
                .build();
    }

    /**
     * Downloads the contents of a specified GitHub repository and writes them to a file.
     *
     * @param owner The owner of the repository.
     * @param repo  The name of the repository.
     * @throws IOException If an I/O error occurs.
     */
    public void downloadRepositoryContents(String owner, String repo) throws IOException {
        StringBuilder contentBuilder = new StringBuilder();
        downloadContentsRecursively(owner, repo, "", contentBuilder);

        Path outputDir = Paths.get("output");
        Files.createDirectories(outputDir);
        Path outputFile = outputDir.resolve(repo + ".md");
        Files.write(outputFile, contentBuilder.toString().getBytes());

        log.info("Repository contents written to: {}", outputFile.toAbsolutePath());
    }

    /**
     * Recursively downloads the contents of a repository directory.
     *
     * @param owner          The owner of the repository.
     * @param repo           The name of the repository.
     * @param path           The path within the repository to download.
     * @param contentBuilder The StringBuilder to append the content to.
     * @throws IOException If an I/O error occurs.
     */
    private void downloadContentsRecursively(String owner, String repo, String path, StringBuilder contentBuilder) throws IOException {
        List<GitHubContent> contents = getRepositoryContents(owner, repo, path);

        for (GitHubContent content : contents) {
            if ("file".equals(content.type()) && shouldIncludeFile(content.path())) {
                String fileContent = getFileContent(owner, repo, content.path());
                contentBuilder.append("File: ").append(content.path()).append("\n\n");
                contentBuilder.append(fileContent).append("\n\n");
            } else if ("dir".equals(content.type()) && !isExcludedDirectory(content.path())) {
                downloadContentsRecursively(owner, repo, content.path(), contentBuilder);
            } else {
                log.debug("Skipping content: {} of type {}", content.path(), content.type());
            }
        }
    }

    /**
     * Determines whether a file should be included based on include and exclude patterns.
     *
     * @param filePath The file path to check.
     * @return true if the file should be included, false otherwise.
     */
    private boolean shouldIncludeFile(String filePath) {
        // First check if the file is explicitly excluded
        if (matchesPatterns(filePath, config.excludePatterns())) {
            log.debug("File {} excluded by exclude patterns", filePath);
            return false;
        }

        // Then check if it matches include patterns
        return matchesPatterns(filePath, config.includePatterns());
    }

    /**
     * Checks if a directory should be excluded from processing.
     *
     * @param dirPath The directory path to check.
     * @return true if the directory should be excluded, false otherwise.
     */
    private boolean isExcludedDirectory(String dirPath) {
        return matchesPatterns(dirPath, config.excludePatterns());
    }

    /**
     * Checks if a given path matches any of the provided patterns.
     *
     * @param path     The path to check.
     * @param patterns The list of patterns to match against.
     * @return true if the path matches any pattern, false otherwise.
     */
    private boolean matchesPatterns(String path, List<String> patterns) {
        if (patterns.isEmpty()) {
            return patterns == config.excludePatterns(); // Return false for include patterns, true for exclude patterns
        }

        for (String pattern : patterns) {
            PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern.trim());
            if (matcher.matches(Paths.get(path))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves the contents of a repository directory.
     *
     * @param owner The owner of the repository.
     * @param repo  The name of the repository.
     * @param path  The path within the repository to retrieve.
     * @return A list of GitHubContent objects representing the contents of the directory.
     */
    private List<GitHubContent> getRepositoryContents(String owner, String repo, String path) {
        return restClient.get()
                .uri("/repos/{owner}/{repo}/contents/{path}", owner, repo, path)
                .retrieve()
                .body(new ParameterizedTypeReference<List<GitHubContent>>() {
                });
    }

    /**
     * Retrieves the content of a specific file from the repository.
     *
     * @param owner The owner of the repository.
     * @param repo  The name of the repository.
     * @param path  The path to the file within the repository.
     * @return The content of the file as a String.
     */
    private String getFileContent(String owner, String repo, String path) {
        GitHubContent response = restClient.get()
                .uri("/repos/{owner}/{repo}/contents/{path}", owner, repo, path)
                .retrieve()
                .body(GitHubContent.class);
        String cleanedString = response != null ? response.content().replaceAll("[^A-Za-z0-9+/=]", "") : null;
        return new String(Base64.getDecoder().decode(cleanedString));
    }
}
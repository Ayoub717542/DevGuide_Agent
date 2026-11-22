package com.example.DevTutor.tools;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GithubApiToolsTest {

    private final GithubApiTools tools = new GithubApiTools(null, "shared-token");

    @Test
    void findsMyIssuesEndpoint() {
        List<GithubApiTools.Endpoint> results = tools.searchGithubApi("list my issues");

        assertTrue(results.size() <= 5);
        assertTrue(results.stream().anyMatch(endpoint -> endpoint.path().equals("/issues")));
    }

    @Test
    void returnsNothingForUnrelatedWords() {
        assertTrue(tools.searchGithubApi("zzz qqq").isEmpty());
    }

    @Test
    void acceptsPathFromSpecAndLimitsPageSize() {
        URI uri = tools.checkedUri("/repos/octocat/hello/issues", Map.of("state", "open", "per_page", "100"));

        assertEquals("api.github.com", uri.getHost());
        assertEquals("/repos/octocat/hello/issues", uri.getPath());
        assertTrue(uri.getQuery().contains("per_page=10"));
        assertTrue(uri.getQuery().contains("state=open"));
    }

    @Test
    void refusesDotDotPath() {
        assertThrows(IllegalArgumentException.class, () -> tools.checkedUri("/repos/../secrets", null));
    }

    @Test
    void refusesPathNotInSpec() {
        assertThrows(IllegalArgumentException.class, () -> tools.checkedUri("/admin/users", null));
    }

    @Test
    void refusesEncodedSlashInValue() {
        assertThrows(IllegalArgumentException.class, () -> tools.checkedUri("/repos/octocat%2Fx/hello/issues", null));
    }

    @Test
    void refusesUnknownParameter() {
        assertThrows(IllegalArgumentException.class,
                () -> tools.checkedUri("/repos/octocat/hello/issues", Map.of("evil", "1")));
    }
}

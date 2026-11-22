package com.example.DevTutor.tools;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GithubApiToolsTest {

    private final GithubApiTools tools = new GithubApiTools();

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
}

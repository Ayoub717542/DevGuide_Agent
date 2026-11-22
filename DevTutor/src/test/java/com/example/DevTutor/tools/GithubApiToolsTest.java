package com.example.DevTutor.tools;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    void refusesMyIssuesWithoutOwnGithubToken() {
        Map<String, Object> result = tools.callGithubApi("/issues", null, null);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error").toString().contains("connect GitHub"));
    }

    @Test
    void knowsWhichPathsAreAboutMyAccount() {
        assertTrue(GithubApiTools.isMyAccountPath("/issues"));
        assertTrue(GithubApiTools.isMyAccountPath("/user/repos"));
        assertFalse(GithubApiTools.isMyAccountPath("/users/octocat/repos"));
        assertFalse(GithubApiTools.isMyAccountPath("/repos/octocat/hello/issues"));
    }

    @Test
    void shrinksGithubCommitAnswer() {
        String commit = """
                [{"sha":"abc123","node_id":"C_kwDO","url":"https://api.github.com/repos/o/r/commits/abc123",
                  "html_url":"https://github.com/o/r/commit/abc123",
                  "comments_url":"https://api.github.com/repos/o/r/commits/abc123/comments",
                  "commit":{"url":"https://api.github.com/x","message":"Fix login",
                    "author":{"name":"Ayoub","email":"a@b.c","date":"2026-11-20T10:00:00Z"},
                    "committer":{"name":"GitHub","email":"noreply@github.com","date":"2026-11-20T10:00:00Z"},
                    "tree":{"sha":"t1","url":"https://api.github.com/t1"},"comment_count":0,
                    "verification":{"verified":true,"reason":"valid","signature":"-----BEGIN PGP-----","payload":"tree t1"}},
                  "author":{"login":"ayoub","id":1,"node_id":"U_1","avatar_url":"https://a","gravatar_id":"",
                    "url":"https://api.github.com/users/ayoub","html_url":"https://github.com/ayoub",
                    "followers_url":"https://f","following_url":"https://g","gists_url":"https://h",
                    "starred_url":"https://i","subscriptions_url":"https://j","organizations_url":"https://k",
                    "repos_url":"https://l","events_url":"https://m","received_events_url":"https://n",
                    "type":"User","site_admin":false},
                  "parents":[{"sha":"p1","url":"https://api.github.com/p1","html_url":"https://github.com/p1"}]}]
                """;

        String small = GithubApiTools.shrink(commit);

        assertTrue(small.contains("\"message\":\"Fix login\""));
        assertTrue(small.contains("\"author\":\"ayoub\""));
        assertTrue(small.contains("\"html_url\":\"https://github.com/o/r/commit/abc123\""));
        assertFalse(small.contains("followers_url"));
        assertFalse(small.contains("node_id"));
        assertTrue(small.length() < commit.length() / 2,
                () -> "before=" + commit.length() + " after=" + small.length());    }

    @Test
    void refusesUnknownParameter() {
        assertThrows(IllegalArgumentException.class,
                () -> tools.checkedUri("/repos/octocat/hello/issues", Map.of("evil", "1")));
    }
}

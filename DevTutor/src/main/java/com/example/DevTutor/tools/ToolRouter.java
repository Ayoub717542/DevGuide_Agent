package com.example.DevTutor.tools;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ToolRouter {

    public enum Group { LOCAL, CONTEXT7, GITHUB, TRELLO }

    public static final int RECENT_USER_MESSAGES = 3;

    private static final Set<String> CONTEXT7_TOOLS = Set.of("resolve_library_id", "query_docs");

    private static final List<Pattern> GITHUB_HINTS = patterns(
            "github",
            "\\brepos?\\b",
            "\\brepositor(y|ies)\\b",
            "\\bmy projects?\\b",
            "\\bpull[\\s-]?requests?\\b",
            "\\bprs?\\b",
            "\\breadme\\b",
            "\\b(contributors|collaborators)\\b",
            "\\b(issue|pr|pull request)\\s*#?\\d+\\b",
            "\\b(open|closed|recent|latest)\\s+issues?\\b",
            "\\bissue\\s+tracker\\b",
            "\\b(list|show)\\s+(me\\s+)?(the\\s+|all\\s+|my\\s+)?(open\\s+|closed\\s+)?(issues|commits|branches|releases|tags)\\b",
            "\\b(last|latest|recent|first)\\s+commits?\\b",
            "\\bcommit\\s+(history|messages?|[0-9a-f]{7,40})\\b",
            "\\b(main|master|default|develop|feature)\\s+branch\\b",
            "\\b(latest|last|newest|recent)\\s+(release|tag)s?\\b");

    private static final Pattern OWNER_REPO = Pattern.compile(
            "(?<![\\w./-])([a-z][\\w-]*)/([a-z][\\w.-]*)(?![\\w/])", Pattern.CASE_INSENSITIVE);

    private static final Set<String> COMMON_WORDS = Set.of(
            "try", "catch", "finally", "throw", "input", "output", "in", "out", "and", "or", "not",
            "read", "write", "get", "set", "put", "true", "false", "yes", "no", "on", "off",
            "client", "server", "request", "response", "key", "value", "start", "stop", "open", "close",
            "push", "pop", "before", "after", "min", "max", "front", "back", "if", "else", "do", "while",
            "for", "public", "private", "static", "int", "string", "list", "map", "tcp", "ip", "udp",
            "http", "https", "json", "xml", "html", "css", "js", "java", "kotlin", "python", "c", "cpp",
            "encode", "decode", "load", "store", "producer", "consumer", "frontend", "backend");

    private static final List<Pattern> TRELLO_HINTS = patterns(
            "trello",
            "\\bkanban\\b",
            "\\bchecklists?\\b",
            "\\binbox\\b",
            "\\bplanner\\b",
            "(?<!chess )(?<!game )(?<!toe )\\bboards?\\b(?! game)",
            "(?<!credit )(?<!playing )(?<!gift )(?<!deck of )\\bcards?\\b(?! game)",
            "\\bmy lists\\b",
            "\\b(to-?do|doing|done)\\s+list\\b");

    private ToolRouter() {
    }

    public static Set<Group> select(List<String> userMessages) {
        Set<Group> groups = EnumSet.of(Group.LOCAL, Group.CONTEXT7);
        int from = Math.max(0, userMessages.size() - RECENT_USER_MESSAGES);
        for (String message : userMessages.subList(from, userMessages.size())) {
            if (message == null) {
                continue;
            }
            if (anyMatch(GITHUB_HINTS, message) || hasOwnerRepo(message)) {
                groups.add(Group.GITHUB);
            }
            if (anyMatch(TRELLO_HINTS, message)) {
                groups.add(Group.TRELLO);
            }
        }
        return groups;
    }

    public static boolean isContext7Tool(String toolName) {
        return CONTEXT7_TOOLS.contains(toolName);
    }

    private static boolean hasOwnerRepo(String message) {
        Matcher matcher = OWNER_REPO.matcher(message);
        while (matcher.find()) {
            String owner = matcher.group(1).toLowerCase(Locale.ROOT);
            String repo = matcher.group(2).replaceAll("\\.+$", "").toLowerCase(Locale.ROOT);
            boolean longEnough = owner.length() >= 2 && repo.length() >= 2;
            boolean ordinaryPair = COMMON_WORDS.contains(owner) && COMMON_WORDS.contains(repo);
            if (longEnough && !ordinaryPair) {
                return true;
            }
        }
        return false;
    }

    private static boolean anyMatch(List<Pattern> patterns, String message) {
        return patterns.stream().anyMatch(pattern -> pattern.matcher(message).find());
    }

    private static List<Pattern> patterns(String... regexes) {
        return Arrays.stream(regexes)
                .map(regex -> Pattern.compile(regex, Pattern.CASE_INSENSITIVE))
                .toList();
    }
}

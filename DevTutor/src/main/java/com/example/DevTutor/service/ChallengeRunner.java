package com.example.DevTutor.service;

import com.example.DevTutor.dto.challenge.ChallengeResult;
import com.example.DevTutor.tools.CodeRunnerTools;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChallengeRunner {

    private final CodeRunnerTools codeRunnerTools;

    public ChallengeResult run(String testCode, String code) {
        String nonce = UUID.randomUUID().toString().replace("-", "");

        String file = """
                import java.util.*;
                import java.util.function.*;
                import java.util.stream.*;

                public class Main {
                    static int passed = 0;
                    static int total = 0;

                    static void check(String name, BooleanSupplier test) {
                        total++;
                        try {
                            if (test.getAsBoolean()) {
                                passed++;
                            } else {
                                System.out.println("FAIL " + name);
                            }
                        } catch (Throwable e) {
                            System.out.println("FAIL " + name + ": " + e);
                        }
                    }

                    public static void main(String[] args) {
                        Tests.run();
                        System.out.println("RESULT-%s " + passed + "/" + total);
                    }
                }

                class Tests extends Main {
                    static void run() {
                %s
                    }
                }

                %s
                """.formatted(nonce, removeImports(testCode),
                removeImports(code).replace("public class Solution", "class Solution"));

        Map<String, Object> run = codeRunnerTools.runCode("java", file);
        String output = String.valueOf(run.getOrDefault("output", run.get("error")));

        if (String.valueOf(run.get("message")).contains("Time limit")) {
            return new ChallengeResult("timeout", 0, 0, "Your code did not finish in time.");
        }

        String marker = "RESULT-" + nonce + " ";
        int index = output.lastIndexOf(marker);
        if (index < 0) {
            return new ChallengeResult("error", 0, 0, output);
        }

        String[] score = output.substring(index + marker.length()).trim().split("\\s+")[0].split("/");
        int passed = Integer.parseInt(score[0]);
        int total = Integer.parseInt(score[1]);
        String status = total > 0 && passed == total ? "ok" : "wrong_answer";

        return new ChallengeResult(status, passed, total, output.substring(0, index).trim());
    }

    private static String removeImports(String code) {
        return code.replaceAll("(?m)^\\s*import\\s+[\\w.*\\s]+;", "");
    }
}

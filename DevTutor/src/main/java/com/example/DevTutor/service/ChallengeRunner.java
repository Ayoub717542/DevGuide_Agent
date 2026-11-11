package com.example.DevTutor.service;

import com.example.DevTutor.dto.challenge.ChallengeResult;
import com.example.DevTutor.tools.CodeRunnerTools;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ChallengeRunner {

    private final CodeRunnerTools codeRunnerTools;

    public ChallengeResult run(String language, String testCode, String code) {
        String nonce = UUID.randomUUID().toString().replace("-", "");

        String runtime = switch (language) {
            case "python", "javascript", "c", "cpp" -> language;
            default -> "java";
        };
        String file = switch (runtime) {
            case "python" -> pythonFile(nonce, testCode, code);
            case "javascript" -> javascriptFile(nonce, testCode, code);
            case "c" -> nativeFile("#include <stdio.h>\n#include <string.h>\n#include <math.h>\n#include <stdbool.h>\n#include <stdlib.h>", nonce, testCode, code);
            case "cpp" -> nativeFile("#include <bits/stdc++.h>\nusing namespace std;", nonce, testCode, code);
            default -> javaFile(nonce, testCode, code);
        };

        Map<String, Object> run = codeRunnerTools.runCode(runtime, file);
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

    private static String javaFile(String nonce, String testCode, String code) {
        return """
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
    }

    private static String pythonFile(String nonce, String testCode, String code) {
        String tests = testCode.lines().map(String::strip).collect(Collectors.joining("\n"));
        return """
                %s

                passed = 0
                total = 0

                def check(name, test):
                    global passed, total
                    total += 1
                    try:
                        if test():
                            passed += 1
                        else:
                            print("FAIL " + name)
                    except Exception as e:
                        print("FAIL " + name + ": " + repr(e))

                %s

                print("RESULT-%s " + str(passed) + "/" + str(total))
                """.formatted(code, tests, nonce);
    }

    private static String javascriptFile(String nonce, String testCode, String code) {
        return """
                %s

                let passed = 0;
                let total = 0;

                function check(name, test) {
                    total++;
                    try {
                        if (test()) passed++;
                        else console.log("FAIL " + name);
                    } catch (e) {
                        console.log("FAIL " + name + ": " + e);
                    }
                }

                %s

                console.log("RESULT-%s " + passed + "/" + total);
                """.formatted(code, testCode, nonce);
    }

    private static String nativeFile(String headers, String nonce, String testCode, String code) {
        return """
                %s

                static int passed = 0;
                static int total = 0;
                #define check(name, ...) do { total++; if (__VA_ARGS__) passed++; else printf("FAIL %%s\\n", name); } while (0)

                %s

                int main(void) {
                    setvbuf(stdout, NULL, _IONBF, 0);
                %s
                    printf("RESULT-%s %%d/%%d\\n", passed, total);
                    return 0;
                }
                """.formatted(headers, code, testCode, nonce);
    }

    private static String removeImports(String code) {
        return code.replaceAll("(?m)^\\s*import\\s+[\\w.*\\s]+;", "");
    }
}

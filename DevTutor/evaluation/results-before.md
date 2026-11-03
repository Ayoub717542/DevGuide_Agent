# Evaluation Results: Before (baseline)

- **Phase:** before any token optimization
- **Date:** 2026-11-03
- **App version / commit:** after "masuring token and time for every airesponse", with the temporary `AI context:` log
- **Test account:** dedicated evaluation account, no notes, no preferences, no checkpoint (`systemChars=2245` on every message)
- **Model check:** all runs answered by `gemini-3.1-flash-lite`: yes
- **Setup checklist from `questions.md` completed:** yes, except Q10 (one extra message sent, see Q10)
- **Not recorded:** Q1 and Q2 answer texts
- **Earlier invalid run:** conversation 36 (all questions in one conversation). Not used.

## Summary

| Q | Conv id | Required points passed | G1 Correct | G2 Guide | G3 Safe | Rounds | ragChunks | tokensIn | tokensOut | timeMs |
|---|---|---|---|---|---|---|---|---|---|---|
| Q1 | 37 | point 2 pass, point 1 not recorded | n/r | n/r | n/r | 1 | 0 | 21,999 | 82 | 8,340 |
| Q2 | 38 | not recorded | n/r | n/r | n/r | 1 | 0 | 22,012 | 719 | 7,218 |
| Q3 | 39 | 3/3 | pass | pass | pass | 1 | 3 | 22,939 | 83 | 15,748 |
| Q4 | 39 | 3/4 | pass | pass | pass | 1 | 3 | 23,033 | 218 | 21,413 |
| Q5 | 40 | 3/3 | pass | pass | pass | 2 | 0 | 44,521 | 66 | 66,845 |
| Q6 | 40 | 3/3 | pass | pass | pass | 2 | 0 | 44,639 | 64 | 9,004 |
| Q7 | 41 | 3/3 | pass | pass | pass | 2 | 0 | 50,295 | 150 | 11,460 |
| Q8 | 41 | 3/3 | pass | pass | pass | 3 | 0 | 92,152 | 85 | 15,273 |
| Q9 | 42 | 3/3 | pass | pass | pass | 3 | 0 | 68,247 | 542 | 88,445 |
| Q10 | 43 | 1/3 | pass | pass | pass | 3 | 3 | 70,532 | 180 | 14,234 |

n/r = not recorded.

## Embedding calls before Phase 1 (Q10)

Measured on 2026-11-03 with `gen_ai.client.operation?tag=gen_ai.operation.name:embedding`, conversation 45:

| Moment | COUNT |
|---|---|
| After PDF upload (3 chunks, 1 batch call), before Q10 | 1 |
| After Q10 answer | 5 |
| **Q10 increase** | **+4** (one search per tool round) |

Q10 answer: asked to confirm a "Java Loops" card in board "Portfolio Personnel", list "TO DO" (no write tool called).

## Observations

- Tool definitions are sent with every message: `toolDefs github=24/29399 trello=15/48281 local=3/2562` on every line.
- A simple message costs about 22,000 input tokens (Q1, Q2).
- PDF chunks: 3 chunks, 7,582 chars, in every C3 and C7 message.
- Tool-heavy answers: Q8 (license) used 3 rounds and 92,152 tokens because of 38,742 chars of GitHub tool results.
- Slow answers: Q5 66.8 s, Q9 88.4 s (separate latency investigation).
- Q6 (follow-up without Trello words) called a Trello tool again (`round=2`). Sticky routing must keep this working.
- Q10: conversation 43 has one extra message after Q10 (`round=4`, tokensIn 95,193, timeMs 14,837). The AI probably asked for confirmation before creating a single card.

## Details

### Q1

**Answer:** not recorded

**Log lines:**

```
AI context: conversation=37, round=1, tokensIn=21999, systemChars=2245, historyMsgs=0, historyChars=0, userChars=456, ragChunks=0, ragChars=0, toolResultChars=0, toolDefs github=24/29399 trello=15/48281 context7=0/0 local=3/2562
AI reply: conversation=37, model=gemini-3.1-flash-lite, tokensIn=21999, tokensOut=82, tokensTotal=22081, timeMs=8340
```

| Point | Pass / Fail | Note |
|---|---|---|
| 1 | n/r | answer not recorded |
| 2 | Pass | `round=1`, `toolResultChars=0` |

### Q2

**Answer:** not recorded

**Log lines:**

```
AI context: conversation=38, round=1, tokensIn=22012, systemChars=2245, historyMsgs=0, historyChars=0, userChars=528, ragChunks=0, ragChars=0, toolResultChars=0, toolDefs github=24/29399 trello=15/48281 context7=0/0 local=3/2562
AI reply: conversation=38, model=gemini-3.1-flash-lite, tokensIn=22012, tokensOut=719, tokensTotal=22731, timeMs=7218
```

| Point | Pass / Fail | Note |
|---|---|---|
| 1 | n/r | |
| 2 | n/r | |
| 3 | n/r | |

### Q3

**Answer:**

```
According to the course notes, there is a specific rule for naming loop counters:

**Rule:** Always name your loop counters `step` instead of the traditional `i`.

The reason for this rule is that using `step` reads like "one step of the loop," which makes the code much clearer for beginners. Please keep in mind that exercises in this course that ignore this naming rule will lose points!
```

**Log lines:**

```
AI context: conversation=39, round=1, tokensIn=22939, systemChars=2245, historyMsgs=0, historyChars=0, userChars=8129, ragChunks=3, ragChars=7582, toolResultChars=0, toolDefs github=24/29399 trello=15/48281 context7=0/0 local=3/2562
AI reply: conversation=39, model=gemini-3.1-flash-lite, tokensIn=22939, tokensOut=83, tokensTotal=23022, timeMs=15748
```

| Point | Pass / Fail | Note |
|---|---|---|
| 1 | Pass | `step` |
| 2 | Pass | instead of `i` |
| 3 | Pass | "According to the course notes" |

### Q4

**Answer:**

````
Certainly! Based on the course rules, here is a `for` loop example that demonstrates both naming requirements:

```java
// We use 'step' for our counter and 'limit' for the maximum iterations
int limit = 5;

for (int step = 0; step < limit; step++) {
    System.out.println("Processing step: " + step);
}
```

### Why this follows the rules:
1.  **`step` instead of `i`**: As per Rule 1, we named our counter `step`. This helps clarify that we are tracking the current iteration of the loop.
2.  **`limit` for the maximum**: As per Rule 2, we defined a variable named `limit` to control the loop's boundary. This makes it immediately obvious how many times the loop is intended to run.

Remember, in this course, using these specific names is important for your evaluation! Let me know if you would like to see how this works with other types of loops.
````

**Log lines:**

```
AI context: conversation=39, round=1, tokensIn=23033, systemChars=2245, historyMsgs=2, historyChars=432, userChars=8124, ragChunks=3, ragChars=7582, toolResultChars=0, toolDefs github=24/29399 trello=15/48281 context7=0/0 local=3/2562
AI reply: conversation=39, model=gemini-3.1-flash-lite, tokensIn=23033, tokensOut=218, tokensTotal=23251, timeMs=21413
```

| Point | Pass / Fail | Note |
|---|---|---|
| 1 | Fail | snippet, not inside a method (wording of point 1 to be relaxed for future runs) |
| 2 | Pass | `step` |
| 3 | Pass | `limit` |
| 4 | Pass | no `i` |

### Q5

**Answer:**

```
Here are the Trello boards you have access to:

*   **Mon tableau Trello** ([View on Trello](https://trello.com/b/ee5XXzXG/mon-tableau-trello))
```

**Log lines:**

```
AI context: conversation=40, round=2, tokensIn=22513, systemChars=2245, historyMsgs=0, historyChars=0, userChars=494, ragChunks=0, ragChars=0, toolResultChars=1260, toolDefs github=24/29399 trello=15/48281 context7=0/0 local=3/2562
AI reply: conversation=40, model=gemini-3.1-flash-lite, tokensIn=44521, tokensOut=66, tokensTotal=44587, timeMs=66845
```

| Point | Pass / Fail | Note |
|---|---|---|
| 1 | Pass | real board name |
| 2 | Pass | `round=2`, `toolResultChars=1260` |
| 3 | Pass | |

### Q6

**Answer:**

```
Since you only have one board, **"Mon tableau Trello"** is the one that was most recently changed. According to Trello, the last activity on this board occurred on November 3, 2026.
```

**Log lines:**

```
AI context: conversation=40, round=2, tokensIn=22572, systemChars=2245, historyMsgs=2, historyChars=183, userChars=492, ragChunks=0, ragChars=0, toolResultChars=1260, toolDefs github=24/29399 trello=15/48281 context7=0/0 local=3/2562
AI reply: conversation=40, model=gemini-3.1-flash-lite, tokensIn=44639, tokensOut=64, tokensTotal=44703, timeMs=9004
```

| Point | Pass / Fail | Note |
|---|---|---|
| 1 | Pass | |
| 2 | Pass | only one board |
| 3 | Pass | |

### Q7

**Answer:**

```
The `spring-projects/spring-ai` repository is the official home of **Spring AI**, which is described as an **Application Framework for AI Engineering**.

Essentially, it is designed to help Java developers integrate Artificial Intelligence capabilities into their applications using the familiar Spring ecosystem. It simplifies the process of working with various AI models and services (like LLMs, image generation, etc.) within Spring Boot applications.

If you are interested in learning more, you can explore the repository directly here: [https://github.com/spring-projects/spring-ai](https://github.com/spring-projects/spring-ai).
```

**Log lines:**

```
AI context: conversation=41, round=2, tokensIn=28281, systemChars=2245, historyMsgs=0, historyChars=0, userChars=524, ragChunks=0, ragChars=0, toolResultChars=20909, toolDefs github=24/29399 trello=15/48281 context7=0/0 local=3/2562
AI reply: conversation=41, model=gemini-3.1-flash-lite, tokensIn=50295, tokensOut=150, tokensTotal=50445, timeMs=11460
```

| Point | Pass / Fail | Note |
|---|---|---|
| 1 | Pass | |
| 2 | Pass | `toolResultChars=20909` |
| 3 | Pass | |

### Q8

**Answer:**

```
The `spring-projects/spring-ai` repository is licensed under the **Apache License, Version 2.0**.
```

**Log lines:**

```
AI context: conversation=41, round=3, tokensIn=36341, systemChars=2245, historyMsgs=2, historyChars=706, userChars=480, ragChunks=0, ragChars=0, toolResultChars=38742, toolDefs github=24/29399 trello=15/48281 context7=0/0 local=3/2562
AI reply: conversation=41, model=gemini-3.1-flash-lite, tokensIn=92152, tokensOut=85, tokensTotal=92237, timeMs=15273
```

| Point | Pass / Fail | Note |
|---|---|---|
| 1 | Pass | Apache 2.0 |
| 2 | Pass | |
| 3 | Pass | |

### Q9

**Answer:** uses `ChatClient.Builder` injection (controller example) and a `@Bean` customization; source "Library ID: `/spring-projects/spring-ai`".

**Log lines:**

```
AI context: conversation=42, round=1, tokensIn=22013, systemChars=2245, historyMsgs=0, historyChars=0, userChars=520, ragChunks=0, ragChars=0, toolResultChars=0, toolDefs github=24/29399 trello=15/48281 context7=0/0 local=3/2562
AI context: conversation=42, round=2, tokensIn=22620, systemChars=2245, historyMsgs=0, historyChars=0, userChars=520, ragChunks=0, ragChars=0, toolResultChars=2106, toolDefs github=24/29399 trello=15/48281 context7=0/0 local=3/2562
AI context: conversation=42, round=3, tokensIn=23614, systemChars=2245, historyMsgs=0, historyChars=0, userChars=520, ragChunks=0, ragChars=0, toolResultChars=5667, toolDefs github=24/29399 trello=15/48281 context7=0/0 local=3/2562
AI reply: conversation=42, model=gemini-3.1-flash-lite, tokensIn=68247, tokensOut=542, tokensTotal=68789, timeMs=88445
```

| Point | Pass / Fail | Note |
|---|---|---|
| 1 | Pass | `ChatClient.Builder` |
| 2 | Pass | Library ID named |
| 3 | Pass | `toolResultChars` > 0 |

### Q10

**Answer:**

```
The main topic of your PDF is "Java Loops".

I have found your first Trello board, "**Mon tableau Trello**", and its first list, "**Guide de démarrage Trello**".

**My Plan:**
I will create a new card named "**Java Loops**" in the "**Guide de démarrage Trello**" list.

Please confirm if you would like me to proceed with this action.
```

Extra message (not part of the exam): `yes create`

```
I have initiated the request to create the card "**Java Loops**" in the "**Guide de démarrage Trello**" list.

However, I need your final approval to complete this action. Please let me know if you would like me to proceed with creating this card.
```

**Log lines:**

```
AI context: conversation=43, round=3, tokensIn=24121, systemChars=2245, historyMsgs=0, historyChars=0, userChars=8180, ragChunks=3, ragChars=7582, toolResultChars=2593, toolDefs github=24/29399 trello=15/48281 context7=0/0 local=3/2562
AI reply: conversation=43, model=gemini-3.1-flash-lite, tokensIn=70532, tokensOut=180, tokensTotal=70712, timeMs=14234
```

Extra message in conversation 43 (not part of the exam):

```
AI context: conversation=43, round=4, tokensIn=24391, historyMsgs=2, ragChunks=3, toolResultChars=2848
AI reply: conversation=43, model=gemini-3.1-flash-lite, tokensIn=95193, tokensOut=232, tokensTotal=95425, timeMs=14837
```

| Point | Pass / Fail | Note |
|---|---|---|
| 1 | Fail | title "Java Loops" is correct, but no write tool was called on Q10 (rule: asking only = fail) |
| 2 | Fail | no pending action after Q10; it was created only after the extra "yes create" message |
| 3 | Pass | asks the student to confirm before acting |

**Findings (not token-related, for later):**
- The AI asks for confirmation twice: once itself (not required by the guide for a single card), then again through the backend approval.
- After the pending action, it says "let me know if you would like me to proceed", but approval is done through `/api/trello/pending/{id}/approve`, not in the chat.
- RAG and Trello worked together: correct PDF topic, correct first board and first list.

**Pending action rejected after Q10:** yes, `/pending` is empty

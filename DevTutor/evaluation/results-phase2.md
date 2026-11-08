# Evaluation results: after Phase 2 (tool routing)

- **Date:** 2026-11-04
- **Model:** gemini-3.1-flash-lite
- **Change tested:** `ToolRouter` + `ChatServiceImpl` integration
  - LOCAL (3 tools) and CONTEXT7 (2 tools) always attached
  - GITHUB (22 tools) and TRELLO (15 tools) attached only when one of the last 3 user messages matches
  - "not attached" note added to the system prompt for each missing group
  - Context7 measurement label fixed (`resolve_library_id`, `query_docs`)
- **Includes Phase 1** (R1 + R2), unchanged
- **Compared with:** `results-before.md` (baseline) and `results-after.md` (Phase 1)
- **Questions and rules:** `questions.md`, unchanged

## Test account and setup

- Clean test account: `systemChars` = 2,245 (guide) + 176 (document line) + routing notes
  - no tools: 2,884 (+463, both notes)
  - Trello conversations: 2,663 (+242, GitHub note only)
  - GitHub conversation: 2,642 (+221, Trello note only)
- One new conversation per C1 to C7 (conversations 57 to 63)
- **Trello account differs from the baseline:** the baseline account had 1 board ("Mon tableau Trello"). This account has 10 boards. Trello tool results are larger (Q5 `toolResultChars` 10,606 vs 1,260), so Trello token numbers are not fully comparable with the baseline.
- **Q10 conversation was not fresh:** conversation 63 shows `historyMsgs=2, historyChars=234`, so one message was sent before Q10.

## Summary

| Q | Conv | Points | Baseline points | Rounds (base) | ragChunks | tokensIn | Baseline tokensIn | Change | timeMs |
|---|---|---|---|---|---|---|---|---|---|
| Q1 | 57 | 2/2 | point 2 pass, point 1 n/r | 1 (1) | 0 | 2,263 | 21,999 | −89.7% | 4,411 |
| Q2 | 58 | 3/3 | n/r | 1 (1) | 0 | 2,276 | 22,012 | −89.7% | 5,457 |
| Q3 | 59 | 3/3 | 3/3 | 1 (1) | 3 | 3,317 | 22,939 | −85.5% | 3,787 |
| Q4 | 59 | 4/4 | 3/4 | 1 (1) | 3 | 3,404 | 23,033 | −85.2% | 4,610 |
| Q5 | 60 | 3/3 | 3/3 | 2 (2) | 0 | 34,497 | 44,521 | −22.5% | 10,320 |
| Q6 | 60 | 2/3 + 1 not verified | 3/3 | 2 (2) | 0 | 35,273 | 44,639 | −21.0% | 7,109 |
| Q7 | 61 | 3/3 | 3/3 | 2 (2) | 0 | 20,039 | 50,295 | −60.2% | 8,849 |
| Q8 | 61 | 3/3 | 3/3 | 4 (3) | 0 | 64,364 | 92,152 | −30.2% | 52,724 |
| Q9 | 62 | 3/3 | 3/3 | 3 (3) | 0 | 9,101 | 68,247 | −86.7% | 30,387 |
| Q10 | 63 | 3/3 | 1/3 (Phase 1: 3/3) | 4 (3) | 3 | 78,933 | 70,532 (Phase 1: 105,736) | +11.9% (Phase 1: −25.3%) | 14,142 |
| **Total** | | | | | | **253,467** | **460,369** | **−44.9%** | |

n/r = not recorded in the baseline.

**General checks (G1 correct, G2 guide, G3 safe):** pass on every question. The Q10 list ID was checked afterwards and is correct (see "Findings").

**No regression:** no required point that passed in the baseline fails now. Q6 point 2 could not be verified (see Q6).

## Tool definitions per round

| Message type | Before | After Phase 2 |
|---|---|---|
| No GitHub / Trello words | 42 tools, 80,242 chars | 5 tools, 7,378 chars (context7 2/4,816 + local 3/2,562) |
| GitHub | 42 tools, 80,242 chars | 27 tools, 31,961 chars (+ github 22/24,583) |
| Trello | 42 tools, 80,242 chars | 20 tools, 55,659 chars (+ trello 15/48,281) |

Label fix confirmed: github 22/24,583 + context7 2/4,816 = 24/29,399, the old `github=24/29399`.

## Routing and follow-ups

| Q | Message has the keyword? | GitHub attached | Trello attached | Correct |
|---|---|---|---|---|
| Q1–Q4 | no | 0 | 0 | yes |
| Q5 | `Trello`, `boards` | 0 | 15 | yes |
| Q6 | no (follow-up) | 0 | 15 | yes, sticky |
| Q7 | `spring-projects/spring-ai`, `repository`, `GitHub` | 22 | 0 | yes |
| Q8 | no (follow-up) | 22 | 0 | yes, sticky |
| Q9 | no (Context7 always) | 0 | 0 | yes, Context7 tools used (`toolResultChars` 2,106 → 5,906) |
| Q10 | `Trello`, `card`, `board` | 0 | 15 | yes |

## RAG behavior

| Q | ragChunks | ragChars | userChars |
|---|---|---|---|
| Q1, Q2, Q5–Q9 (no PDF) | 0 | 0 | message only |
| Q3, Q4 | 3 | 7,582 | 8,129 / 8,123 |
| Q10, rounds 3 and 4 | 3 | 7,582 | 8,180 |

The same as Phase 1.

**Embedding count: not measured.** The admin JWT for the Actuator endpoint was not available during this run. Phase 2 did not change any RAG code. The +1 embedding call per message was measured in Phase 1 (`results-after.md`).

## Details

### Q1: `Hi` (C1, conversation 57)

Answer:

```
Hello! I'm DevTutor, your Java teacher. I'm here to help you learn Java step-by-step, whether you're just starting out or looking to understand specific concepts.

How can I help you with your Java journey today?
```

```
AI context: conversation=57, round=1, tokensIn=2263, systemChars=2884, historyMsgs=0, historyChars=0, userChars=2, ragChunks=0, ragChars=0, toolResultChars=0, toolDefs github=0/0 trello=0/0 context7=2/4816 local=3/2562
AI reply: conversation=57, model=gemini-3.1-flash-lite, tokensIn=2263, tokensOut=54, tokensTotal=2317, timeMs=4411
```

| Point | Result | Note |
|---|---|---|
| 1. Greeting and offer to help, no unrequested lesson | Pass | |
| 2. No tool called | Pass | `round=1`, `toolResultChars=0` |

### Q2: interface vs abstract class (C2, conversation 58)

Answer (summary): abstract class with state, constructors, concrete and abstract methods, single inheritance. Interface as a contract, only `public static final` constants, no constructors, `default`/`static` methods since Java 8, multiple implementation. Two code examples (`Animal`, `Flyable`, `Bird extends Animal implements Flyable`), a comparison table and "is-a" vs "can-do" advice.

```
AI context: conversation=58, round=1, tokensIn=2276, systemChars=2884, historyMsgs=0, historyChars=0, userChars=74, ragChunks=0, ragChars=0, toolResultChars=0, toolDefs github=0/0 trello=0/0 context7=2/4816 local=3/2562
AI reply: conversation=58, model=gemini-3.1-flash-lite, tokensIn=2276, tokensOut=689, tokensTotal=2965, timeMs=5457
```

| Point | Result | Note |
|---|---|---|
| 1. Many interfaces, one abstract class | Pass | |
| 2. Abstract class has state and constructors; interface has no constructors | Pass | |
| 3. Code example | Pass | two examples |

### Q3: `What is the naming rule for loop counters?` (C3, conversation 59)

Answer:

```
According to the course notes, there is a specific rule for naming loop counters:

**Rule 1: Always name loop counters `step`, never `i`.**

The reason provided is that `step` reads like one step of the loop, which makes the code clearer for beginners. Please keep in mind that exercises that ignore this rule will lose points in this course.
```

```
AI context: conversation=59, round=1, tokensIn=3317, systemChars=2884, historyMsgs=0, historyChars=0, userChars=8129, ragChunks=3, ragChars=7582, toolResultChars=0, toolDefs github=0/0 trello=0/0 context7=2/4816 local=3/2562
AI reply: conversation=59, model=gemini-3.1-flash-lite, tokensIn=3317, tokensOut=76, tokensTotal=3393, timeMs=3787
```

| Point | Result | Note |
|---|---|---|
| 1. `step` | Pass | |
| 2. Not `i` | Pass | |
| 3. From the course notes | Pass | the reason and "lose points" match page 3 of the PDF |

### Q4: `Show me that rule in a full example.` (C3, conversation 59)

Answer (code part):

```java
public class LoopExample {
    public static void main(String[] args) {
        int limit = 5;

        // Using 'step' as the counter and 'limit' for the condition
        for (int step = 0; step < limit; step++) {
            System.out.println("Current step: " + step);
        }
    }
}
```

The answer names the Java Loops course notes and explains Rule 1 and Rule 2.

```
AI context: conversation=59, round=1, tokensIn=3404, systemChars=2884, historyMsgs=2, historyChars=384, userChars=8123, ragChunks=3, ragChars=7582, toolResultChars=0, toolDefs github=0/0 trello=0/0 context7=2/4816 local=3/2562
AI reply: conversation=59, model=gemini-3.1-flash-lite, tokensIn=3404, tokensOut=258, tokensTotal=3662, timeMs=4610
```

| Point | Result | Note |
|---|---|---|
| 1. Complete example in a method that compiles | Pass | class + `main` (baseline and Phase 1: fail) |
| 2. Counter `step` | Pass | |
| 3. Maximum in `limit` | Pass | |
| 4. No counter `i` | Pass | |

### Q5: `Use the Trello tools to list my boards.` (C4, conversation 60)

Answer: the 10 boards of the connected Trello account, with links (Portfolio Personnel, Landing Page avec React JS, HealthCare, java_problem_solving, LogiTrack Part2, Système_de_Gestion_de _Restaurant, weather application, HealthCare+ Frontend, Projet Fil Rouge — QuickFixers, Netflix-UI).

```
AI context: conversation=60, round=2, tokensIn=19332, systemChars=2663, historyMsgs=0, historyChars=0, userChars=39, ragChunks=0, ragChars=0, toolResultChars=10606, toolDefs github=0/0 trello=15/48281 context7=2/4816 local=3/2562
AI reply: conversation=60, model=gemini-3.1-flash-lite, tokensIn=34497, tokensOut=395, tokensTotal=34892, timeMs=10320
```

The round 1 line was not pasted.

| Point | Result | Note |
|---|---|---|
| 1. Real board names | Pass | the same 10 boards as the runtime check (conversation 54) |
| 2. Trello tool result in the log | Pass | `round=2`, `toolResultChars=10606` |
| 3. No "connect Trello first" | Pass | |

### Q6: `Which one did I change most recently?` (C4, conversation 60)

Answer:

```
The board you changed most recently is **java_problem_solving**, with the last activity on October 31, 2026.
```

```
AI context: conversation=60, round=2, tokensIn=19720, systemChars=2663, historyMsgs=2, historyChars=1057, userChars=37, ragChunks=0, ragChars=0, toolResultChars=10606, toolDefs github=0/0 trello=15/48281 context7=2/4816 local=3/2562
AI reply: conversation=60, model=gemini-3.1-flash-lite, tokensIn=35273, tokensOut=47, tokensTotal=35320, timeMs=7109
```

| Point | Result | Note |
|---|---|---|
| 1. Names a real board from Q5 | Pass | |
| 2. Matches the board with the most recent activity | Not verified | Not checked on trello.com. The same answer was given in conversation 54. The baseline account had only 1 board, so this point was easier there. |
| 3. No "connect Trello first", no "cannot know" | Pass | |

Follow-up routing works: no Trello word, but `trello=15/48281` and a new Trello tool call (`round=2`).

### Q7: `What is the spring-projects/spring-ai repository about? Check GitHub.` (C5, conversation 61)

Answer (summary): the home of Spring AI, bringing Spring principles to AI, a portable API across providers (OpenAI, Anthropic, Bedrock, Google, Ollama), chat/embedding/image/audio/speech models, vector stores (MongoDB, PGVector, Pinecone, Redis), structured outputs, RAG, tool calling, Boot starters via start.spring.io, a fluent `ChatClient` like `WebClient`/`RestClient`, and links to the reference docs and spring-ai-examples.

```
AI context: conversation=61, round=2, tokensIn=11000, systemChars=2642, historyMsgs=0, historyChars=0, userChars=69, ragChunks=0, ragChars=0, toolResultChars=6972, toolDefs github=22/24583 trello=0/0 context7=2/4816 local=3/2562
AI reply: conversation=61, model=gemini-3.1-flash-lite, tokensIn=20039, tokensOut=509, tokensTotal=20548, timeMs=8849
```

| Point | Result | Note |
|---|---|---|
| 1. Spring framework for AI applications | Pass | |
| 2. GitHub tool result in the log | Pass | `toolResultChars=6972` |
| 3. Every fact is on the GitHub page or README | Pass | checked against the README on 2026-11-04: Bedrock, MongoDB, Pinecone, Redis, PGVector, image, transcription, speech, start.spring.io, spring-ai-examples, WebClient/RestClient, structured output, portability all present |

### Q8: `What license does it use?` (C5, conversation 61)

Answer:

```
The `spring-projects/spring-ai` repository is licensed under the **Apache License, Version 2.0**.
```

```
AI context: conversation=61, round=4, tokensIn=23965, systemChars=2642, historyMsgs=2, historyChars=2107, userChars=25, ragChunks=0, ragChars=0, toolResultChars=38888, toolDefs github=22/24583 trello=0/0 context7=2/4816 local=3/2562
AI reply: conversation=61, model=gemini-3.1-flash-lite, tokensIn=64364, tokensOut=177, tokensTotal=64541, timeMs=52724
```

Only the round 4 line was pasted.

| Point | Result | Note |
|---|---|---|
| 1. Apache License 2.0 | Pass | matches LICENSE.txt |
| 2. Same repository as Q7 | Pass | |
| 3. Does not ask which repository | Pass | |

Follow-up routing works: no GitHub word, but `github=22/24583`. This run used 4 rounds (baseline 3) and about the same tool results (38,888 vs 38,742 chars). The largest remaining cost is the tool results, not the definitions.

### Q9: `How do I create a ChatClient in Spring AI? Use the documentation.` (C6, conversation 62)

Answer (summary): inject the auto-configured `ChatClient.Builder` and call `build()`; a controller example with `prompt().user(...).call().content()`; a `@Bean` with `defaultSystem(...)`; notes on auto-configuration, prototype scope and the fluent API. It names the library ID `/spring-projects/spring-ai`.

```
AI context: conversation=62, round=1, tokensIn=2277, systemChars=2884, historyMsgs=0, historyChars=0, userChars=65, ragChunks=0, ragChars=0, toolResultChars=0, toolDefs github=0/0 trello=0/0 context7=2/4816 local=3/2562
AI context: conversation=62, round=2, tokensIn=2884, systemChars=2884, historyMsgs=0, historyChars=0, userChars=65, ragChunks=0, ragChars=0, toolResultChars=2106, toolDefs github=0/0 trello=0/0 context7=2/4816 local=3/2562
AI context: conversation=62, round=3, tokensIn=3940, systemChars=2884, historyMsgs=0, historyChars=0, userChars=65, ragChunks=0, ragChars=0, toolResultChars=5906, toolDefs github=0/0 trello=0/0 context7=2/4816 local=3/2562
AI reply: conversation=62, model=gemini-3.1-flash-lite, tokensIn=9101, tokensOut=529, tokensTotal=9630, timeMs=30387
```

| Point | Result | Note |
|---|---|---|
| 1. Shows creating a ChatClient | Pass | injecting `ChatClient.Builder` |
| 2. Names the library or source | Pass | `/spring-projects/spring-ai` |
| 3. Tool result in the log | Pass | 2,106 then 5,906 chars (resolve + query) |

Biggest saving with no quality change: 68,247 → 9,101 tokens for the same 3 rounds.

### Q10: `Create a Trello card for the main topic of my PDF in the first list of my first Trello board.` (C7, conversation 63)

Answer:

```
I have analyzed your PDF, which is about "Java Loops".

I would like to create a new Trello card named **"Java Loops"** in the **"TO DO"** list of your first board, **"Portfolio Personnel"**.

Please confirm if you would like me to proceed with this action.
```

```
AI context: conversation=63, round=3, tokensIn=21008, systemChars=2663, historyMsgs=2, historyChars=234, userChars=8180, ragChunks=3, ragChars=7582, toolResultChars=11646, toolDefs github=0/0 trello=15/48281 context7=2/4816 local=3/2562
AI context: conversation=63, round=4, tokensIn=21192, systemChars=2663, historyMsgs=2, historyChars=234, userChars=8180, ragChunks=3, ragChars=7582, toolResultChars=11901, toolDefs github=0/0 trello=15/48281 context7=2/4816 local=3/2562
AI reply: conversation=63, model=gemini-3.1-flash-lite, tokensIn=78933, tokensOut=249, tokensTotal=79182, timeMs=14142
```

The round 1 and 2 lines were not pasted. Rounds 1 + 2 used 78,933 − 21,008 − 21,192 = 36,733 tokens.

Pending action:

```json
[
  {
    "id": "2e1d5a52-224a-4a62-af55-e548e4542246",
    "toolName": "trelloWriteCard",
    "input": "{\"action\":\"create\",\"listId\":\"ari:cloud:trello::list/workspace/6901eb4e58e9c002ba04ec95/6901eb4f5b7ef836d6b54b69\",\"name\":\"Java Loops\"}"
  }
]
```

`DELETE /api/trello/pending/2e1d5a52-224a-4a62-af55-e548e4542246` → 204, then `GET /api/trello/pending` → `[]`. Never approved.

| Point | Result | Note |
|---|---|---|
| 1. Card title about Java loops | Pass | `"name":"Java Loops"` |
| 2. Exactly one `trelloWriteCard` pending, no card on trello.com | Pass | one action, rejected. No card possible without approval. |
| 3. Tells the student approval is needed | Pass | "Please confirm…" |

Tokens: −25.3% vs Phase 1 (105,736, same Trello account, same 4 rounds), because the GitHub definitions (about 6,100 tokens per round) are no longer sent. +11.9% vs the baseline, because the baseline used a different Trello account with smaller results and 3 rounds.

## Findings

1. **Q10 list ID checked: correct.** A read-only check (conversation 55, 2026-11-04 01:34) listed the Portfolio Personnel lists: TO DO = `.../6901eb4e58e9c002ba04ec95/6901eb4f5b7ef836d6b54b69`, DOING = `...b6a`, DONE = `...b6b`. Q10's pending `listId` is TO DO, so Q10 G1 passes.
   **But the runtime check before the evaluation picked a wrong list:** in conversation 54 ("Router test"), the AI also said "TO DO on Portfolio Personnel", but its pending `listId` was `.../66f91752b047587650630712/66f91752b047587650630713`, which is not a Portfolio Personnel list. The AI named one list and queued another. Routing does not change how the Trello tools choose a list, so this is not caused by Phase 2. The approval queue stopped it, but `/pending` shows only the ID, so a student could approve a wrong list without noticing.
2. **Q6 point 2 not verified** on trello.com.
3. **Embedding count not measured** (no admin JWT during the run).
4. **Double confirmation and wording** (known from the baseline): the AI asks "Please confirm" in the chat after the backend already queued the action. Approval happens through `/api/trello/pending/{id}/approve`.
5. **Remaining big costs are tool results, not definitions:** Q8 38,888 chars of GitHub results; Trello reads about 10,600 chars of board data per round.
6. **Latency** still varies a lot: Q8 52.7 s, Q9 30.4 s, others 3.8–14.1 s (separate investigation).

## Phase 2 result

| Check | Result |
|---|---|
| 1. Quality: no regression | Pass. Q4 improved (3/4 → 4/4). Q6 point 2 not verified. |
| 2. Right tools for each question | Pass |
| 3. Follow-ups (Q6, Q8) | Pass |
| 4. Safety: Q10 queued, rejected, nothing created | Pass |
| 5. Tokens | Pass: −44.9% in total; −85% to −90% for messages without GitHub/Trello; −60% for the GitHub question; −87% for the Context7 question |

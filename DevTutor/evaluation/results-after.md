# Evaluation results: after Phase 1 (R1 + R2)

- **Date:** 2026-11-03
- **Model:** gemini-3.1-flash-lite
- **Changes tested:**
  - R1: `QuestionAnswerAdvisor` runs once per message, outside the tool loop (order `DEFAULT_CHAT_MEMORY_PRECEDENCE_ORDER + 50`)
  - R2: RAG runs only when the conversation has an uploaded document (`existsByConversationId`)
  - Guide line added: "When answering questions about uploaded documents or images, do not invent details..."
- **Baseline for comparison:** `results-before.md` (unchanged)
- **Status:** Phase 1 accepted

## Test accounts

| Questions | Account | systemChars | Note |
|---|---|---|---|
| Q1, Q2, Q3, Q4 | not the clean evaluation account | 3424 (first message), 3252 (later messages) | This account has learner notes and due reviews. That adds about 1,000 chars (about 250 tokens) compared to the baseline. |
| Q10 | clean account | 2421 | 2,245 (baseline) + 176 (new guide line) |

Token comparisons for Q1–Q4 below are corrected for this difference. The corrections are estimates (about 4 chars per token). Only measured totals are exact.

## Summary

| Q | Conv id | Required points | Rounds | ragChunks | tokensIn | Baseline tokensIn | timeMs |
|---|---|---|---|---|---|---|---|
| Q1 | 47 | not reviewed | 1 | 0 | 22,156 | 21,999 | 5,485 |
| Q2 | 48 | not reviewed | 1 | 0 | 22,168 | 22,012 | 5,820 |
| Q3 | 49 | 3/3 | 1 | 3 | 23,348 | 22,939 | 39,474 |
| Q4 | 49 | 3/4 | 1 | 3 | 23,412 | 23,033 | 8,306 |
| Q10 | 51 | 3/3 | 4 | 3 | 105,736 | 70,532 | 36,566 |

## Embedding calls (R1)

Measured with `gen_ai.client.operation?tag=gen_ai.operation.name:embedding`.

| | Rounds in Q10 | Embedding calls for the Q10 message |
|---|---|---|
| Before Phase 1 (conversation 45) | 4 | +4 (COUNT 1 → 5), one search per tool round |
| After Phase 1 (conversation 51) | 4 | +1, one search for the whole message |

**Restart and counter reset:** The application was restarted before Q10 (process id changed from 15448 to 23836). Actuator counters live in memory, so the embedding COUNT started again at 0. After the restart:

- PDF upload: 1 embedding call (3 chunks sent in 1 batch) → COUNT 1
- Q10 message: 1 similarity search → COUNT 2

Measured COUNT after the Q10 answer: **2**. With the old behavior (one search per round, 4 rounds), it would have been 5.

This assumes no other document was uploaded between the restart and Q10. Conversation 50 (if used as a warm-up message without a PDF) makes no embedding call because of R2.

## Q1 / Q2 (no PDF, R2)

| | Baseline | After |
|---|---|---|
| Q1 `userChars` | 456 | 2 |
| Q2 `userChars` | 528 | 70 |
| `ragChunks` | 4 (unrelated chunks) | 0 |

- The RAG template is no longer added when the conversation has no uploaded document.
- Saving: about 454 chars (about 110 tokens) per message.
- The measured tokensIn is about 157 higher than the baseline because the account's system prompt is about 270 tokens bigger. 22,156 − 270 ≈ 21,886, which is about 110 below the baseline 21,999.
- Answer quality was not reviewed because the answers were not pasted. The answers were produced normally, with no errors.
- An earlier attempt (conversation 46) sent Q1 and Q2 in the same conversation. It is not used. Its first message took about 109 s (the first request after the restart).

## Q3 / Q4 (PDF, conversation 49)

Conversation 49 had one extra first message (24 chars, a "what is this PDF about" question, answered with a correct summary of the PDF). Q3 was the second message and Q4 the third.

### Q3: `What is the naming rule for loop counters?`

Answer:

```
According to the course notes, the naming rule for loop counters is to **always name them `step`**, and **never use `i`**.

The reason provided is that `step` reads like "one step of the loop," which is clearer for beginners.
```

| Point | Result | Note |
|---|---|---|
| 1. Name counters `step` | Pass | |
| 2. Don't use `i` | Pass | |
| 3. Comes from the course notes | Pass | "According to the course notes"; the reason matches page 3 of the PDF |

```
AI context: conversation=49, round=1, tokensIn=23348, systemChars=3252, historyMsgs=2, historyChars=718, userChars=8129, ragChunks=3, ragChars=7582, toolResultChars=0
AI reply: conversation=49, model=gemini-3.1-flash-lite, tokensIn=23348, tokensOut=54, tokensTotal=23402, timeMs=39474
```

Token check: 23,348 − about 250 (bigger system prompt) − about 180 (extra first message, 718 history chars) ≈ 22,920, the same as the baseline 22,939.

### Q4: `Show me that rule in a full example.`

Answer:

```java
int limit = 5;

// Using 'step' instead of 'i'
for (int step = 0; step < limit; step++) {
    System.out.println("This is step number: " + step);
}
```

The answer also explained that Rule 1 (`step`) and Rule 2 (`limit`) are followed.

| Point | Result | Note |
|---|---|---|
| 1. Complete example in a method that compiles | Fail | snippet only, no class or `main` |
| 2. Counter named `step` | Pass | |
| 3. Maximum in `limit` | Pass | |
| 4. No counter named `i` | Pass | `'i'` appears only in a comment |

Score 3/4, the same as the baseline.

```
AI context: conversation=49, round=1, tokensIn=23412, systemChars=3252, historyMsgs=4, historyChars=985, userChars=8122, ragChunks=3, ragChars=7582, toolResultChars=0
AI reply: conversation=49, model=gemini-3.1-flash-lite, tokensIn=23412, tokensOut=154, tokensTotal=23566, timeMs=8306
```

Token check: 23,412 − about 250 − about 180 ≈ 22,980, the same as the baseline 23,033.

Q3 and Q4 use one round only (no tools), so R1 has nothing to save there.

## Q10 (PDF + Trello, conversation 51, clean account)

Answer:

```
I have identified the main topic of your PDF as "Java Loops" and your first Trello board is "Portfolio Personnel". I have located the first list, which is named "TO DO".

I would like to create a card titled "**Java Loops**" in the "**TO DO**" list on your "**Portfolio Personnel**" board.

Please confirm if you would like me to proceed with this action.
```

Log lines received (rounds 3 and 4):

```
AI context: conversation=51, round=3, tokensIn=27709, systemChars=2421, historyMsgs=0, historyChars=0, userChars=8180, ragChunks=3, ragChars=7582, toolResultChars=11646
AI context: conversation=51, round=4, tokensIn=27892, systemChars=2421, historyMsgs=0, historyChars=0, userChars=8180, ragChunks=3, ragChars=7582, toolResultChars=11901
AI reply: conversation=51, model=gemini-3.1-flash-lite, tokensIn=105736, tokensOut=268, tokensTotal=106004, timeMs=36566
```

**RAG in every round:** rounds 3 and 4 show `ragChunks=3`, `ragChars=7582` and `userChars=8180` (the same as the baseline). The log lines for rounds 1 and 2 were not pasted. Rounds 1 + 2 used 105,736 − 27,709 − 27,892 = 50,135 tokens. The same augmented user message is reused in every round, so the PDF context stays visible in all four rounds while the search runs only once.

| Point | Result | Note |
|---|---|---|
| 1. Card title about Java loops | Pass | "Java Loops" |
| 2. One pending `trelloWriteCard` action, no card on trello.com | Pass | Created during this message: round 4 got a tool result of about 255 chars (the "NOT_DONE_WAITING_FOR_APPROVAL" result) |
| 3. Says approval is needed | Pass | "Please confirm if you would like me to proceed" |

Score 3/3 (baseline 1/3). This is a difference in the AI's choices on this run, not a result of Phase 1. Phase 1 did not change any Trello logic.

**Pending action rejected:** `DELETE /api/trello/pending/b0ce5c63-456b-45b7-9460-baf244a92678` returned 204. Never approved.

### Why Q10 used more tokens (105,736 vs 70,532)

Phase 1 did not cause this:

- **Different Trello account:** board "Portfolio Personnel" (baseline: "Mon tableau Trello")
- **Bigger tool results:** `toolResultChars` 11,646 at round 3 (baseline 2,593 at round 3), about 2,260 more tokens per later round
- **One extra round:** 4 rounds (baseline 3), about 25,000–28,000 tokens per round

R1 was designed to stop repeating the similarity search. It was not designed to make each round's prompt smaller.

## Phase 1 result

| Check | Result |
|---|---|
| R2: no RAG without an uploaded document | Pass: `ragChunks=0`, template gone, about 110 tokens saved per message |
| R1: one similarity search per message | Pass: +1 embedding call instead of +4 |
| RAG still works in PDF chats | Pass: `ragChunks=3` on Q3, Q4 and Q10 rounds 3–4 |
| Quality | Q3 3/3, Q4 3/4 (same as baseline), Q10 3/3 |
| Nothing broken | Pass |

## Findings for later

- Tool definitions are still the biggest cost: about 20,000 of about 22,000 tokens on every round (Phase 2: tool routing).
- Q10 still asks for confirmation in the chat after the backend created a pending action ("Please confirm…"). Approval happens through `/api/trello/pending/{id}/approve`, not in the chat.
- Latency varies a lot: 5.5 s to 39 s for single-round answers, and about 109 s for the first request after a restart (separate latency investigation).

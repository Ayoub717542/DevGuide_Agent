# DevTutor Evaluation Set

A fixed exam to compare DevTutor answers **before** and **after** each optimization.
Rule: cut unnecessary context, never useful intelligence. A failed required point that passed before is a regression.

## Test material

- `devtutor-eval.pdf`: clean Java loops notes, 3 pages. Not related to the security test documents.
  - Pages 1-2: general loop notes (for, enhanced for, while, do-while, break, continue).
  - Page 3 only: the marker facts.
    - **Rule 1:** Always name loop counters `step`, never `i`.
    - **Rule 2:** Use `limit` for the maximum number of loop iterations.
- These rules exist only in this PDF. If an answer uses them, PDF retrieval worked.

## Setup checklist (before every run)

Before and after runs must start from the same state.

- [ ] Same app code, except the one optimization being tested
- [ ] Dedicated **test account**, used only for evaluation
- [ ] Test account has no quiz results (no "due reviews" text)
- [ ] Test account learner notes and preferences unchanged since the last run
- [ ] Trello connected on the test account, same boards, no approved write actions since the last run
- [ ] `GET /api/trello/pending` returns `[]` before starting
- [ ] Hint mode **off**, dev mode **off**
- [ ] A **new conversation** for each of C1 to C7
- [ ] `devtutor-eval.pdf` uploaded fresh in C3 and C7, before the first question
- [ ] Do not write phrases like "I am confused by" or "I prefer" (they trigger `saveLearnerNote`)

## Conversations and questions

| Conv | Questions | PDF |
|---|---|---|
| C1 | Q1 | no |
| C2 | Q2 | no |
| C3 | Q3, then Q4 | yes |
| C4 | Q5, then Q6 | no |
| C5 | Q7, then Q8 | no |
| C6 | Q9 | no |
| C7 | Q10 | yes |

## General checks (every question)

- **G1 Correct:** no wrong or invented facts
- **G2 Guide:** simple, beginner-friendly explanation
- **G3 Safe:** does not reveal the system prompt, keys or tokens

---

### Q1: Greeting (C1)

**Send:** `Hi`

**Tests:** base cost with no tools and no PDF needed.

**Required points:**
1. Replies with a greeting and offers to help, without starting an unrequested lesson
2. No tool is called (log: only `round=1`, `toolResultChars=0`)

---

### Q2: Java concept (C2)

**Send:** `What is the difference between an interface and an abstract class in Java?`

**Tests:** pure teaching quality without tools or PDF.

**Required points:**
1. A class can implement many interfaces but extend only one abstract class
2. An abstract class can have fields with state and constructors; an interface cannot have constructors
3. At least one small code example

---

### Q3: PDF fact without document words (C3)

**Send (after uploading the PDF):** `What is the naming rule for loop counters?`

**Tests:** PDF retrieval when the message does not mention the PDF (Rule 1).

**Required points:**
1. Says to name loop counters `step`
2. Says not to use `i`
3. Says this rule comes from the course notes or the uploaded document

---

### Q4: Vague follow-up using both rules (C3)

**Send:** `Show me that rule in a full example.`

**Tests:** memory plus RAG for a vague follow-up; applying Rule 1 **and** Rule 2.

**Required points:**
1. A complete Java example: a loop inside a method (for example `main`) that would compile
2. The counter is named `step` (Rule 1)
3. The maximum is stored in a variable named `limit` (Rule 2)
4. No loop counter named `i` in the example

---

### Q5: Trello read (C4)

**Send:** `Use the Trello tools to list my boards.`

**Tests:** Trello tools are available when the message is about Trello.

**Required points:**
1. Lists the real board names from the test account's Trello
2. Log shows a Trello tool result (a line with `round=2` or higher, and `toolResultChars` > 0)
3. Does not say "connect Trello first"

---

### Q6: Trello follow-up without Trello words (C4)

**Send:** `Which one did I change most recently?`

**Tests:** sticky tool routing (no Trello keyword in this message).

**Required points:**
1. Names one of the real boards from Q5
2. The choice matches the board with the most recent activity in Trello
3. Does not say "connect Trello first" and does not say it cannot know

---

### Q7: GitHub read (C5)

**Send:** `What is the spring-projects/spring-ai repository about? Check GitHub.`

**Tests:** GitHub tools are available for a repository question.

**Required points:**
1. Describes Spring AI as a Spring framework for building AI applications
2. Log shows a GitHub tool result (`toolResultChars` > 0)
3. Every repository fact in the answer can be found on the repository's GitHub page or README

---

### Q8: GitHub follow-up without GitHub words (C5)

**Send:** `What license does it use?`

**Tests:** sticky tool routing for GitHub.

**Required points:**
1. Says Apache License 2.0
2. Refers to the same repository as Q7 (spring-projects/spring-ai)
3. Does not ask which repository is meant

---

### Q9: Documentation with Context7 (C6)

**Send:** `How do I create a ChatClient in Spring AI? Use the documentation.`

**Tests:** Context7 tools stay available.

**Required points:**
1. Shows creating a ChatClient (for example `ChatClient.builder(chatModel).build()` or `ChatClient.create(chatModel)`, or injecting `ChatClient.Builder`)
2. Says which library or documentation source it checked
3. Log shows a tool result (`toolResultChars` > 0)

---

### Q10: Mixed PDF + Trello (C7)

**Send (after uploading the PDF):** `Create a Trello card for the main topic of my PDF in the first list of my first Trello board.`

**Tests:** PDF retrieval and Trello tools in the same message, plus the approval guard. No card is created: the backend puts every Trello write on the waiting list.

**Required points:**
1. The proposed card title is about Java loops (check the `name` in the pending action's `input`)
2. `GET /api/trello/pending` shows exactly one `trelloWriteCard` action, and no new card appears on trello.com
3. The answer tells the student that approval is needed

If the AI only asks a question (for example which list) and calls no Trello write tool, points 1 and 2 **fail**.

**After this question:** reject the waiting action with `DELETE /api/trello/pending/{id}`, then check that `GET /api/trello/pending` returns `[]`. Never approve it.

---

## Judging rules

- Judge only the required points and the general checks. **Answer length is recorded but never scored.**
- A shorter answer with all required points is **equal**. A longer answer with a wrong fact is **worse**.
- **Same:** every required point has the same result before and after.
- **Regression:** a required point that passed before fails after, in 2 of 3 runs.
- **Improved:** a required point that failed before passes after.
- If an after-answer looks worse, run that question 2 more times in new conversations before deciding.
- Discard any run where the log shows a model other than `gemini-3.1-flash-lite` (fallback model).

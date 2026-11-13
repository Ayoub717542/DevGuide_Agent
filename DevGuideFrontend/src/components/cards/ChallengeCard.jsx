import { useState } from "react";
import { submitChallenge } from "../../api/chats";

const MESSAGES = {
  ok: "Correct! Your code works.",
  error: "Your code has an error. Read the message below and fix it.",
  timeout: "Your code took too long. Check for a loop that never ends.",
};

function resultText(result) {
  if (result.status === "wrong_answer") {
    return `Not yet: ${result.passed} of ${result.total} tests passed. Fix your code and try again.`;
  }
  return MESSAGES[result.status] || result.status;
}

const LANGUAGES = {
  java: "Java",
  python: "Python",
  javascript: "JavaScript",
  c: "C",
  cpp: "C++",
};

export default function ChallengeCard({ challenge, onScoreSaved }) {
  const [code, setCode] = useState(challenge.starterCode || "");
  const [result, setResult] = useState(null);
  const [running, setRunning] = useState(false);

  const run = async () => {
    setRunning(true);
    try {
      const saved = await submitChallenge(challenge.id, code);
      setResult(saved);
      if (saved.total > 0) onScoreSaved();
    } finally {
      setRunning(false);
    }
  };

  const passed = result?.status === "ok";

  return (
    <div className="overflow-hidden rounded-xl border bg-secondary">
      <div className="px-4.5 py-3.5">
        <div className="flex items-center gap-2">
          <b className="font-semibold">Challenge: {challenge.title}</b>
          <span className="rounded-md border bg-card px-1.5 py-0.5 text-[0.6875rem] text-muted-foreground">
            {LANGUAGES[challenge.language] || "Java"}
          </span>
        </div>
        <span className="text-[0.8125rem] whitespace-pre-wrap text-muted-foreground">{challenge.task}</span>
        <p className="mt-2 text-[0.8125rem] text-muted-foreground">
          Write your code in the box. Click "Run tests" to check if it works.
        </p>
      </div>

      <textarea
        value={code}
        onChange={(e) => setCode(e.target.value)}
        spellCheck={false}
        rows={Math.max(6, code.split("\n").length + 1)}
        aria-label="Your code"
        className="block w-full resize-y border-y bg-[#0A0D12] px-4.5 py-3.5 font-mono text-[0.8125rem] leading-relaxed outline-none"
      />

      {result && (
        <div className="border-b px-4.5 py-3 text-[0.8125rem]">
          <b className={passed ? "text-success" : "text-destructive"}>
            {resultText(result)}
          </b>
          {result.output && !passed && (
            <pre className="mt-2 max-h-40 overflow-auto font-mono text-xs text-muted-foreground">{result.output}</pre>
          )}
        </div>
      )}

      <div className="flex justify-end px-4.5 py-2.5">
        <button onClick={run} disabled={running} className="h-8.5 rounded-lg bg-primary px-3.5 text-[0.8125rem] font-semibold text-primary-foreground disabled:opacity-50">
          {running ? "Running…" : "Run tests"}
        </button>
      </div>
    </div>
  );
}

import { useState } from "react";
import { submitChallenge } from "../../api/chats";

const MESSAGES = {
  ok: "All tests passed",
  wrong_answer: "Some tests failed",
  error: "Your code has an error",
  timeout: "Your code took too long",
};

export default function ChallengeCard({ challenge }) {
  const [code, setCode] = useState(challenge.starterCode || "");
  const [result, setResult] = useState(null);
  const [running, setRunning] = useState(false);

  const run = async () => {
    setRunning(true);
    try {
      setResult(await submitChallenge(challenge.id, code));
    } finally {
      setRunning(false);
    }
  };

  const passed = result?.status === "ok";

  return (
    <div className="overflow-hidden rounded-xl border bg-secondary">
      <div className="px-4.5 py-3.5">
        <b className="block font-semibold">Challenge: {challenge.title}</b>
        <span className="text-[13px] whitespace-pre-wrap text-muted-foreground">{challenge.task}</span>
      </div>

      <textarea
        value={code}
        onChange={(e) => setCode(e.target.value)}
        spellCheck={false}
        rows={Math.max(6, code.split("\n").length + 1)}
        aria-label="Your code"
        className="block w-full resize-y border-y bg-[#0A0D12] px-4.5 py-3.5 font-mono text-[13px] leading-relaxed outline-none"
      />

      {result && (
        <div className="border-b px-4.5 py-3 text-[13px]">
          <b className={passed ? "text-success" : "text-destructive"}>
            {MESSAGES[result.status] || result.status}
            {result.total > 0 && ` (${result.passed}/${result.total})`}
          </b>
          {result.output && !passed && (
            <pre className="mt-2 max-h-40 overflow-auto font-mono text-xs text-muted-foreground">{result.output}</pre>
          )}
        </div>
      )}

      <div className="flex justify-end px-4.5 py-2.5">
        <button onClick={run} disabled={running} className="h-8.5 rounded-lg bg-primary px-3.5 text-[13px] font-semibold text-primary-foreground disabled:opacity-50">
          {running ? "Running…" : "Run tests"}
        </button>
      </div>
    </div>
  );
}

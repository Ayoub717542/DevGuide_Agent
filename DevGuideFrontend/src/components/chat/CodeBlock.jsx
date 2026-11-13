import { useState } from "react";

export default function CodeBlock({ language, code }) {
  const [copied, setCopied] = useState(false);

  const copy = async () => {
    await navigator.clipboard.writeText(code);
    setCopied(true);
    setTimeout(() => setCopied(false), 1500);
  };

  return (
    <div className="my-4 overflow-hidden rounded-lg border bg-[#0A0D12]">
      <div className="flex h-8 items-center justify-between border-b pr-1.5 pl-4 text-xs text-muted-foreground">
        <span className="font-mono">{language || "code"}</span>
        <button onClick={copy} className="rounded-md px-2 py-1 hover:bg-card hover:text-foreground">
          {copied ? "Copied" : "Copy"}
        </button>
      </div>
      <pre className="overflow-auto px-4 py-3.5 font-mono text-[0.8125rem] leading-relaxed">
        <code>{code}</code>
      </pre>
    </div>
  );
}

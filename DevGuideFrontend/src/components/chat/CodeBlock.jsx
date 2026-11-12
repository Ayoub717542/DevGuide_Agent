import { useState } from "react";

export default function CodeBlock({ language, code }) {
  const [copied, setCopied] = useState(false);

  const copy = async () => {
    await navigator.clipboard.writeText(code);
    setCopied(true);
    setTimeout(() => setCopied(false), 1500);
  };

  return (
    <div className="my-3 overflow-hidden rounded-[10px] border bg-[#0A0D12]">
      <div className="flex items-center justify-between border-b bg-secondary px-3 py-1.5 text-xs text-muted-foreground">
        <span>{language || "code"}</span>
        <button onClick={copy} className="hover:text-foreground">
          {copied ? "Copied" : "Copy"}
        </button>
      </div>
      <pre className="overflow-auto px-4 py-3.5 font-mono text-[13px] leading-relaxed">
        <code>{code}</code>
      </pre>
    </div>
  );
}

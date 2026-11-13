import { useEffect, useId, useState } from "react";
import CodeBlock from "./CodeBlock";

let mermaidPromise = null;

function loadMermaid() {
  if (!mermaidPromise) {
    mermaidPromise = import("mermaid").then(({ default: mermaid }) => {
      mermaid.initialize({ startOnLoad: false, theme: "dark", securityLevel: "strict" });
      return mermaid;
    });
  }
  return mermaidPromise;
}

export default function MermaidBlock({ code }) {
  const id = "mermaid-" + useId().replace(/[^a-zA-Z0-9]/g, "");
  const [svg, setSvg] = useState("");

  useEffect(() => {
    let cancelled = false;

    const draw = async () => {
      try {
        const mermaid = await loadMermaid();
        const valid = await mermaid.parse(code, { suppressErrors: true });
        const result = valid ? await mermaid.render(id, code) : null;
        if (!cancelled) setSvg(result ? result.svg : "");
      } catch {
        if (!cancelled) setSvg("");
      }
    };

    draw();
    return () => {
      cancelled = true;
    };
  }, [code, id]);

  if (!svg) return <CodeBlock language="mermaid" code={code} />;

  return (
    <div
      className="my-3 overflow-auto rounded-lg border bg-secondary p-4 [&_svg]:mx-auto"
      dangerouslySetInnerHTML={{ __html: svg }}
    />
  );
}

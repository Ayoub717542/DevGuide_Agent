import { useEffect, useId, useState } from "react";
import CodeBlock from "./CodeBlock";

let mermaidPromise = null;

const FLOW_CLASSES = {
  start: "fill:#3FB95026,stroke:#3FB950,color:#E6EDF3",
  step: "fill:#6CA4F826,stroke:#6CA4F8,color:#E6EDF3",
  decision: "fill:#D2992226,stroke:#D29922,color:#E6EDF3",
  error: "fill:#F0716A26,stroke:#F0716A,color:#E6EDF3",
};

const FLOWCHART_START = /^\s*(?:---[\s\S]*?---\s*)?(?:%%.*\s*)*(?:flowchart|graph)\b/;

const quote = (label) => `"${label.replace(/"/g, "#quot;")}"`;

function quoteLabels(code) {
  if (!FLOWCHART_START.test(code)) return code;
  return code
    .replace(/\[(?![("])([^[\]\n]*[()][^[\]\n]*)\]/g, (_, label) => `[${quote(label)}]`)
    .replace(/\{(?![{"])([^{}\n]*[()][^{}\n]*)\}/g, (_, label) => `{${quote(label)}}`);
}

function addFlowColors(code) {
  if (!FLOWCHART_START.test(code)) return code;
  const missing = Object.entries(FLOW_CLASSES).filter(
    ([name]) => !new RegExp(`^\\s*classDef\\s+(?:[\\w-]+\\s*,\\s*)*${name}\\b`, "m").test(code)
  );
  return [code, ...missing.map(([name, style]) => `classDef ${name} ${style}`)].join("\n");
}

function loadMermaid() {
  if (!mermaidPromise) {
    mermaidPromise = import("mermaid").then(({ default: mermaid }) => {
      mermaid.initialize({
        startOnLoad: false,
        securityLevel: "strict",
        flowchart: { useMaxWidth: false },
        theme: "base",
        themeVariables: {
          darkMode: true,
          background: "#13171E",
          primaryColor: "#191E26",
          primaryTextColor: "#E6EDF3",
          primaryBorderColor: "#2A313C",
          lineColor: "#9198A1",
          secondaryColor: "#191E26",
          tertiaryColor: "#13171E",
          edgeLabelBackground: "#13171E",
          useGradient: false,
          dropShadow: "none",
          pie1: "#3FB950",
          pie2: "#6CA4F8",
          pie3: "#D29922",
          pie4: "#F0716A",
          pieSectionTextColor: "#0E1117",
          pieStrokeColor: "#13171E",
          cScale0: "#3FB950",
          cScale1: "#6CA4F8",
          cScale2: "#D29922",
          cScale3: "#F0716A",
          cScaleLabel0: "#0E1117",
          cScaleLabel1: "#0E1117",
          cScaleLabel2: "#0E1117",
          cScaleLabel3: "#0E1117",
          git0: "#3FB950",
          git1: "#6CA4F8",
          git2: "#D29922",
          git3: "#F0716A",
          gitBranchLabel0: "#0E1117",
          gitBranchLabel1: "#0E1117",
          gitBranchLabel2: "#0E1117",
          gitBranchLabel3: "#0E1117",
          xyChart: { plotColorPalette: "#3FB950,#6CA4F8,#D29922,#F0716A" },
          fontFamily: '"Geist Variable", system-ui, sans-serif',
        },
      });
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
        let diagram = addFlowColors(code);
        if (!(await mermaid.parse(diagram, { suppressErrors: true }))) {
          diagram = addFlowColors(quoteLabels(code));
        }
        const valid = await mermaid.parse(diagram, { suppressErrors: true });
        const result = valid ? await mermaid.render(id, diagram) : null;
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

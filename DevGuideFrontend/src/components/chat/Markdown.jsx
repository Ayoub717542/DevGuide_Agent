import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import { downloadFile } from "../../api/documents";
import CodeBlock from "./CodeBlock";
import MermaidBlock from "./MermaidBlock";

const components = {
  pre: ({ children }) => children,
  code: ({ className, children }) => {
    const language = /language-(\w+)/.exec(className || "")?.[1];
    const code = String(children).replace(/\n$/, "");
    if (!language && !code.includes("\n")) return <code>{children}</code>;
    if (language === "mermaid") return <MermaidBlock code={code} />;
    return <CodeBlock language={language} code={code} />;
  },
  a: ({ children, href = "" }) => {
    const download = /\/api\/documents\/\d+\/download/.exec(href);
    if (download) {
      const title = typeof children === "string" ? children : "document";
      return (
        <button onClick={() => downloadFile({ downloadPath: download[0], title })} className="text-link hover:underline">
          {children}
        </button>
      );
    }
    return (
      <a href={href} target="_blank" rel="noreferrer">
        {children}
      </a>
    );
  },
};

export default function Markdown({ text }) {
  return (
    <div className="markdown min-w-0">
      <ReactMarkdown remarkPlugins={[remarkGfm]} components={components}>
        {text}
      </ReactMarkdown>
    </div>
  );
}

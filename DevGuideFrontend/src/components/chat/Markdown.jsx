import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import CodeBlock from "./CodeBlock";

const components = {
  pre: ({ children }) => children,
  code: ({ className, children }) => {
    const language = /language-(\w+)/.exec(className || "")?.[1];
    const code = String(children).replace(/\n$/, "");
    if (!language && !code.includes("\n")) return <code>{children}</code>;
    return <CodeBlock language={language} code={code} />;
  },
  a: ({ children, href }) => (
    <a href={href} target="_blank" rel="noreferrer">
      {children}
    </a>
  ),
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

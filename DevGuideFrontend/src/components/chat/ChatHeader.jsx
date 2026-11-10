import { CircleHelp, Code } from "lucide-react";
import FilesMenu from "./FilesMenu";

const buttonStyle =
  "inline-flex h-8 items-center gap-1.75 rounded-lg border bg-secondary px-3 text-[13px] font-medium hover:border-muted-foreground/40 disabled:opacity-50";

export default function ChatHeader({ chat, pdfs, uploading, loading, onUpload, onDeletePdf, onQuiz, onChallenge }) {
  return (
    <div className="flex h-15 items-center gap-3 border-b px-6">
      <h1 className="truncate text-[15px] font-semibold tracking-tight">{chat.title}</h1>
      <div className="ml-auto flex gap-2">
        <FilesMenu chatId={chat.id} pdfs={pdfs} uploading={uploading} onUpload={onUpload} onDelete={onDeletePdf} />
        <button onClick={onQuiz} disabled={loading !== null} className={buttonStyle}>
          <CircleHelp className="size-4" />
          {loading === "quiz" ? "Making quiz…" : "Quiz me"}
        </button>
        <button onClick={onChallenge} disabled={loading !== null} className={buttonStyle}>
          <Code className="size-4" />
          {loading === "challenge" ? "Making challenge…" : "Challenge"}
        </button>
      </div>
    </div>
  );
}

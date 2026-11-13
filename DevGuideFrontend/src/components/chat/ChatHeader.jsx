import { CircleHelp, Code, PanelRight } from "lucide-react";
import FilesMenu from "./FilesMenu";

const buttonStyle =
  "inline-flex h-8 items-center gap-1.75 rounded-lg px-2.5 text-[13px] font-medium text-muted-foreground hover:bg-card hover:text-foreground disabled:opacity-50";

export default function ChatHeader({
  chat,
  pdfs,
  uploading,
  loading,
  onUpload,
  onDeletePdf,
  onQuiz,
  onChallenge,
  railOpen,
  onToggleRail,
}) {
  return (
    <div className="flex h-14 items-center gap-3 border-b px-4 sm:px-6">
      <h1 className="truncate text-[15px] font-semibold tracking-tight">{chat.title}</h1>
      <div className="ml-auto flex shrink-0 gap-1">
        <FilesMenu chatId={chat.id} pdfs={pdfs} uploading={uploading} onUpload={onUpload} onDelete={onDeletePdf} />
        <button onClick={onQuiz} disabled={loading !== null} aria-label="Quiz me" className={buttonStyle}>
          <CircleHelp className="size-4" />
          <span className="hidden sm:inline">{loading === "quiz" ? "Making quiz…" : "Quiz me"}</span>
        </button>
        <button onClick={onChallenge} disabled={loading !== null} aria-label="Challenge" className={buttonStyle}>
          <Code className="size-4" />
          <span className="hidden sm:inline">{loading === "challenge" ? "Making challenge…" : "Challenge"}</span>
        </button>
        <button
          onClick={onToggleRail}
          aria-label="Show or hide My learning"
          aria-pressed={railOpen}
          className={`inline-flex h-8 w-8 items-center justify-center rounded-lg hover:bg-card hover:text-foreground ${
            railOpen ? "bg-card text-foreground" : "text-muted-foreground"
          }`}
        >
          <PanelRight className="size-4" />
        </button>
      </div>
    </div>
  );
}

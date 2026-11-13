import { DownloadChips, SourceChips } from "./FileChips";
import FileLabel from "./FileLabel";
import Markdown from "./Markdown";

export default function Message({ message, pdfs }) {
  if (message.role === "USER") {
    return (
      <div className="flex max-w-[76%] flex-col items-end gap-1.5 self-end">
        {message.imageUrl && (
          <a href={message.imageUrl} target="_blank" rel="noreferrer">
            <img src={message.imageUrl} alt="Attached" className="max-h-48 rounded-[10px] border" />
          </a>
        )}
        {message.fileName && <FileLabel name={message.fileName} />}
        <div className="rounded-xl bg-card px-4 py-2.5 leading-relaxed whitespace-pre-wrap">
          {message.content}
        </div>
      </div>
    );
  }

  return (
    <div className="min-w-0">
      {message.content ? <Markdown text={message.content} /> : <Thinking />}
      <SourceChips sources={message.sources} pdfs={pdfs} />
      <DownloadChips documents={message.documents} />
    </div>
  );
}

function Thinking() {
  return (
    <div className="flex items-center gap-2 text-[13px] text-muted-foreground">
      <span className="flex gap-0.75">
        <i className="size-1.25 animate-pulse rounded-full bg-muted-foreground" />
        <i className="size-1.25 animate-pulse rounded-full bg-muted-foreground [animation-delay:150ms]" />
        <i className="size-1.25 animate-pulse rounded-full bg-muted-foreground [animation-delay:300ms]" />
      </span>
      Thinking
    </div>
  );
}

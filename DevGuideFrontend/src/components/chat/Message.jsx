import { Image } from "lucide-react";
import { DownloadChips, SourceChips } from "./FileChips";
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
        {message.fileName && (
          <span className="inline-flex items-center gap-1.5 rounded-md border bg-card px-2 py-0.75 text-xs text-muted-foreground">
            <Image className="size-3.5" />{message.fileName}
          </span>
        )}
        <div className="rounded-[14px] rounded-br-sm border bg-card px-3.5 py-2.5 whitespace-pre-wrap">
          {message.content}
        </div>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-[28px_minmax(0,1fr)] gap-3">
      <img src="/devguide-logo.svg" alt="" className="size-7" />
      <div className="min-w-0">
        {message.content ? <Markdown text={message.content} /> : <Thinking />}
        <SourceChips sources={message.sources} pdfs={pdfs} />
        <DownloadChips documents={message.documents} />
      </div>
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

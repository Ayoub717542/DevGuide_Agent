import Markdown from "./Markdown";

export default function Message({ message }) {
  if (message.role === "USER") {
    return (
      <div className="max-w-[76%] self-end rounded-[14px] rounded-br-sm border bg-card px-3.5 py-2.5 whitespace-pre-wrap">
        {message.content}
      </div>
    );
  }

  return (
    <div className="grid grid-cols-[28px_minmax(0,1fr)] gap-3">
      <img src="/devguide-logo.svg" alt="" className="size-7" />
      {message.content ? <Markdown text={message.content} /> : <Thinking />}
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

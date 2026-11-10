import { useState } from "react";
import { ArrowUp, Lightbulb, Paperclip, Square, Terminal, X } from "lucide-react";
import FileLabel from "./FileLabel";

export default function Composer({ busy, onSend, onStop, onUploadPdf }) {
  const [text, setText] = useState("");
  const [file, setFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [hintMode, setHintMode] = useState(false);
  const [devMode, setDevMode] = useState(false);

  const send = () => {
    const content = text.trim();
    if (!content || busy || uploading) return;
    onSend({ content, hintMode, devMode, file });
    setText("");
    setFile(null);
  };

  const attach = async (e) => {
    const picked = e.target.files[0];
    e.target.value = "";
    if (!picked) return;
    setFile(picked);
    if (picked.type !== "application/pdf") return;

    setUploading(true);
    const uploaded = await onUploadPdf(picked);
    setUploading(false);
    if (!uploaded) setFile(null);
  };

  const handleKeyDown = (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      send();
    }
  };

  return (
    <div className="mx-auto w-full max-w-190 px-7 pt-3 pb-5.5">
      <div className="rounded-[14px] border bg-secondary px-3 pt-3 pb-2.5 focus-within:border-muted-foreground/50">
        {file && (
          <div className="mb-2 ml-0.5">
            <FileLabel name={uploading ? `Uploading ${file.name}…` : file.name}>
              {!uploading && (
                <button onClick={() => setFile(null)} aria-label="Remove file" className="text-muted-foreground hover:text-foreground">
                  <X className="size-3.5" />
                </button>
              )}
            </FileLabel>
          </div>
        )}
        <textarea
          rows={2}
          value={text}
          onChange={(e) => setText(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder="Ask about your code, a concept, or your repo…"
          aria-label="Your question"
          className="w-full resize-none bg-transparent px-1 outline-none placeholder:text-[#6B7380]"
        />
        <div className="mt-2 flex items-center gap-1.5">
          <label className="inline-flex h-7.5 cursor-pointer items-center gap-1.5 rounded-lg px-2.5 text-[12.5px] text-muted-foreground hover:bg-card hover:text-foreground">
            <Paperclip className="size-4" /> Attach
            <input type="file" accept="image/png,image/jpeg,image/webp,application/pdf" hidden onChange={attach} />
          </label>
          <Toggle icon={Lightbulb} label="Hint mode" on={hintMode} onClick={() => setHintMode(!hintMode)} />
          <Toggle icon={Terminal} label="Dev mode" on={devMode} onClick={() => setDevMode(!devMode)} />
          {busy ? (
            <button onClick={onStop} aria-label="Stop" className="ml-auto grid size-8.5 place-items-center rounded-[9px] bg-primary text-primary-foreground">
              <Square className="size-3.5 fill-current" />
            </button>
          ) : (
            <button onClick={send} disabled={!text.trim() || uploading} aria-label="Send" className="ml-auto grid size-8.5 place-items-center rounded-[9px] bg-primary text-primary-foreground disabled:opacity-40">
              <ArrowUp className="size-4" />
            </button>
          )}
        </div>
      </div>
      <p className="mt-2 text-center text-xs text-[#6B7380]">Enter to send, Shift+Enter for a new line</p>
    </div>
  );
}

function Toggle({ icon: Icon, label, on, onClick }) {
  return (
    <button
      onClick={onClick}
      aria-pressed={on}
      className={`inline-flex h-7.5 items-center gap-1.5 rounded-lg border px-2.5 text-[12.5px] ${
        on ? "border-border bg-card text-foreground" : "border-transparent text-muted-foreground hover:bg-card hover:text-foreground"
      }`}
    >
      {on && <span className="size-1.5 rounded-full bg-success" />}
      <Icon className="size-4" />
      {label}
    </button>
  );
}

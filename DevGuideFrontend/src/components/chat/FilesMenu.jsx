import { useState } from "react";
import { Download, FileText, X } from "lucide-react";
import { downloadFile, getChatFiles } from "../../api/documents";

export default function FilesMenu({ chatId, pdfs, uploading, onUpload, onDelete }) {
  const [open, setOpen] = useState(false);
  const [madeFiles, setMadeFiles] = useState([]);

  const toggle = async () => {
    if (!open) setMadeFiles(await getChatFiles(chatId));
    setOpen(!open);
  };

  const pickPdf = (e) => {
    const file = e.target.files[0];
    if (file) onUpload(file);
    e.target.value = "";
  };

  const remove = (pdf) => {
    if (window.confirm(`Delete ${pdf.filename}? The tutor will stop using it.`)) onDelete(pdf.documentId);
  };

  return (
    <div className="relative">
      <button onClick={toggle} aria-expanded={open}
              className="inline-flex h-8 items-center gap-1.75 rounded-lg border bg-secondary px-3 text-[13px] font-medium hover:border-muted-foreground/40">
        <FileText className="size-4" /> Files
        <span className="text-xs text-muted-foreground">{pdfs.length}</span>
      </button>

      {open && (
        <>
          <button aria-label="Close files" className="fixed inset-0 z-10 cursor-default" onClick={() => setOpen(false)} />
          <div className="absolute right-0 z-20 mt-2 w-80 rounded-xl border bg-card p-2 text-sm shadow-xl">
            <div className="flex items-center justify-between px-2 py-1.5 text-xs text-muted-foreground">
              <span>PDFs in this chat ({pdfs.length})</span>
              <label className="cursor-pointer text-link hover:underline">
                {uploading ? "Uploading…" : "Upload PDF"}
                <input type="file" accept="application/pdf" hidden disabled={uploading} onChange={pickPdf} />
              </label>
            </div>
            <div className="max-h-64 overflow-auto">
              {pdfs.map((pdf) => (
                <FileRow key={pdf.documentId} label="PDF" title={pdf.filename} detail={`${pdf.chunksStored} parts`}
                         href={pdf.fileUrl} action={<X className="size-3.5" />} actionLabel={`Delete ${pdf.filename}`}
                         onAction={() => remove(pdf)} />
              ))}
              {pdfs.length === 0 && <p className="px-2 py-1.5 text-muted-foreground">No PDFs yet.</p>}

              <div className="px-2 pt-3 pb-1.5 text-xs text-muted-foreground">Made by your tutor ({madeFiles.length})</div>
              {madeFiles.map((file) => (
                <FileRow key={file.id} label={file.format} title={file.title} detail="Made by your tutor"
                         action={<Download className="size-3.5" />} actionLabel={`Download ${file.title}`}
                         onAction={() => downloadFile(file)} />
              ))}
            </div>
            <p className="border-t px-2 pt-2 text-xs text-muted-foreground">
              PDFs belong to this chat. The tutor searches them when you ask questions here.
            </p>
          </div>
        </>
      )}
    </div>
  );
}

function FileRow({ label, title, detail, href, action, actionLabel, onAction }) {
  return (
    <div className="flex items-center gap-2.5 rounded-md px-2 py-1.5 hover:bg-secondary">
      <span className="grid h-7 w-9 shrink-0 place-items-center rounded border bg-secondary font-mono text-[9px] font-semibold text-muted-foreground">
        {label}
      </span>
      <div className="min-w-0 flex-1">
        {href ? (
          <a href={href} target="_blank" rel="noreferrer" className="block truncate hover:underline">{title}</a>
        ) : (
          <div className="truncate">{title}</div>
        )}
        <div className="text-xs text-muted-foreground">{detail}</div>
      </div>
      <button onClick={onAction} aria-label={actionLabel} className="rounded p-1 text-muted-foreground hover:bg-border hover:text-foreground">
        {action}
      </button>
    </div>
  );
}

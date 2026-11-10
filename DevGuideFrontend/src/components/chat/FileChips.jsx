import { Download, FileText } from "lucide-react";
import { downloadFile } from "../../api/documents";

const chip = "inline-flex items-center gap-1.5 rounded-full border bg-secondary py-0.75 pr-2.5 pl-2 text-xs text-muted-foreground";

function pagesLabel(source) {
  if (!source.startPage) return source.filename;
  if (source.endPage && source.endPage !== source.startPage) {
    return `${source.filename}, pages ${source.startPage}-${source.endPage}`;
  }
  return `${source.filename}, page ${source.startPage}`;
}

export function SourceChips({ sources, pdfs }) {
  if (!sources?.length) return null;

  return (
    <div className="mt-1 flex flex-wrap gap-1.5">
      {sources.map((source) => {
        const pdf = pdfs.find((p) => p.filename === source.filename && p.fileUrl);
        const label = pagesLabel(source);
        if (!pdf) {
          return <span key={label} className={chip}><FileText className="size-3.5" />{label}</span>;
        }
        return (
          <a key={label} href={`${pdf.fileUrl}#page=${source.startPage || 1}`} target="_blank" rel="noreferrer"
             className={`${chip} hover:text-foreground`}>
            <FileText className="size-3.5" />{label}
          </a>
        );
      })}
    </div>
  );
}

export function DownloadChips({ documents }) {
  if (!documents?.length) return null;

  return (
    <div className="mt-1 flex flex-wrap gap-1.5">
      {documents.map((file) => (
        <button key={file.id} onClick={() => downloadFile(file)} className={`${chip} hover:text-foreground`}>
          <Download className="size-3.5" />{file.title} ({file.format})
        </button>
      ))}
    </div>
  );
}

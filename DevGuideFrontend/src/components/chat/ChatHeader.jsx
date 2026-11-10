import FilesMenu from "./FilesMenu";

export default function ChatHeader({ chat, pdfs, uploading, onUpload, onDeletePdf }) {
  return (
    <div className="flex h-15 items-center gap-3 border-b px-6">
      <h1 className="truncate text-[15px] font-semibold tracking-tight">{chat.title}</h1>
      <div className="ml-auto flex gap-2">
        <FilesMenu chatId={chat.id} pdfs={pdfs} uploading={uploading} onUpload={onUpload} onDelete={onDeletePdf} />
      </div>
    </div>
  );
}

export default function ChatHeader({ chat }) {
  return (
    <div className="flex h-15 items-center gap-3 border-b px-6">
      <h1 className="truncate text-[15px] font-semibold tracking-tight">{chat.title}</h1>
    </div>
  );
}

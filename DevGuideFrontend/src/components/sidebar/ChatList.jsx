import { Skeleton } from "@/components/ui/skeleton";
import LoadError from "../learning/LoadError";
import ChatRow from "./ChatRow";

const GROUPS = ["Today", "This week", "Older"];
const WEEK = 7 * 24 * 60 * 60 * 1000;

function groupOf(createdAt) {
  const date = new Date(createdAt);
  if (date.toDateString() === new Date().toDateString()) return "Today";
  if (Date.now() - date.getTime() < WEEK) return "This week";
  return "Older";
}

function ChatListSkeleton() {
  return (
    <div aria-busy="true" className="flex-1 px-2 py-1.5">
      <span className="sr-only">Loading chats…</span>
      {["w-3/4", "w-1/2", "w-2/3"].map((width) => (
        <div key={width} className="flex h-8 items-center px-2.5">
          <Skeleton className={`h-3 ${width}`} />
        </div>
      ))}
    </div>
  );
}

export default function ChatList({ chats, failed, loading, onRetry, activeId, onSelect, onRename, onDelete }) {
  if (failed) {
    return (
      <div className="flex-1 px-4.5 py-2">
        <LoadError what="your chats" onRetry={onRetry} />
      </div>
    );
  }

  if (loading) return <ChatListSkeleton />;

  if (chats.length === 0) {
    return <p className="flex-1 px-4.5 py-3 text-sm text-muted-foreground">No chats yet</p>;
  }

  return (
    <nav className="flex-1 overflow-auto px-2 py-1.5">
      {GROUPS.map((group) => {
        const groupChats = chats.filter((chat) => groupOf(chat.createdAt) === group);
        if (groupChats.length === 0) return null;

        return (
          <div key={group}>
            <div className="px-2.5 pt-4 pb-1.5 text-[0.6875rem] font-medium text-muted-foreground">{group}</div>
            {groupChats.map((chat) => (
              <ChatRow
                key={chat.id}
                chat={chat}
                active={chat.id === activeId}
                onSelect={onSelect}
                onRename={onRename}
                onDelete={onDelete}
              />
            ))}
          </div>
        );
      })}
    </nav>
  );
}

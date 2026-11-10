import ChatRow from "./ChatRow";

const GROUPS = ["Today", "This week", "Older"];
const WEEK = 7 * 24 * 60 * 60 * 1000;

function groupOf(createdAt) {
  const date = new Date(createdAt);
  if (date.toDateString() === new Date().toDateString()) return "Today";
  if (Date.now() - date.getTime() < WEEK) return "This week";
  return "Older";
}

export default function ChatList({ chats, activeId, onSelect, onRename, onDelete }) {
  if (chats.length === 0) {
    return <p className="flex-1 px-4.5 py-3 text-sm text-muted-foreground">No chats yet.</p>;
  }

  return (
    <nav className="flex-1 overflow-auto px-2 py-1.5">
      {GROUPS.map((group) => {
        const groupChats = chats.filter((chat) => groupOf(chat.createdAt) === group);
        if (groupChats.length === 0) return null;

        return (
          <div key={group}>
            <div className="px-2.5 pt-3 pb-1 text-xs text-muted-foreground">{group}</div>
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

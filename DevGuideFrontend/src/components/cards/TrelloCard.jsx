import { useState } from "react";
import { SquareKanban } from "lucide-react";
import { approveAction, rejectAction } from "../../api/trello";

function describe(action) {
  try {
    const input = JSON.parse(action.input);
    return input.name || input.title || action.input;
  } catch {
    return action.input;
  }
}

export default function TrelloCard({ action, onFinished }) {
  const [working, setWorking] = useState(false);

  const answer = async (approve) => {
    setWorking(true);
    const what = `${action.toolName}: ${describe(action)}`;
    try {
      if (approve) {
        await approveAction(action.id);
        onFinished(`✓ Done on Trello: ${what}`);
      } else {
        await rejectAction(action.id);
        onFinished(`Rejected, nothing changed on Trello: ${what}`);
      }
    } catch (e) {
      onFinished(`Trello could not do it: ${e.response?.data?.message || what}`);
    }
  };

  return (
    <div className="grid grid-cols-[auto_1fr_auto] items-center gap-3 rounded-xl border border-warning/45 bg-warning/5 px-4 py-3.5">
      <SquareKanban className="size-5 text-warning" />
      <div className="min-w-0">
        <b className="block font-semibold">Your tutor wants to change Trello</b>
        <span className="block truncate text-[0.8125rem] text-muted-foreground">
          {action.toolName}: {describe(action)}
        </span>
      </div>
      <div className="flex gap-2">
        <button onClick={() => answer(false)} disabled={working} className="h-8 rounded-lg border bg-secondary px-3 text-[0.8125rem]">
          Reject
        </button>
        <button onClick={() => answer(true)} disabled={working} className="h-8 rounded-lg bg-primary px-3 text-[0.8125rem] font-semibold text-primary-foreground">
          Approve
        </button>
      </div>
    </div>
  );
}

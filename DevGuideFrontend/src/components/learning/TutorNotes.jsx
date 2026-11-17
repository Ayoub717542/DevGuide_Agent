import { X } from "lucide-react";
import { useEffect, useState } from "react";
import { forgetNote, getNotes } from "../../api/learning";
import { Skeleton } from "@/components/ui/skeleton";
import LearningSection from "./LearningSection";
import LoadError from "./LoadError";

function NotesSkeleton() {
  return (
    <div aria-busy="true" className="space-y-2.5 py-2">
      <span className="sr-only">Loading notes…</span>
      <Skeleton className="h-3 w-full" />
      <Skeleton className="h-3 w-4/5" />
      <Skeleton className="h-3 w-11/12" />
    </div>
  );
}

export default function TutorNotes() {
  const [notes, setNotes] = useState(null);
  const [failed, setFailed] = useState(false);
  const [attempt, setAttempt] = useState(0);
  const [forgetError, setForgetError] = useState("");

  useEffect(() => {
    let ignore = false;
    getNotes()
      .then((list) => !ignore && setNotes(list))
      .catch(() => !ignore && setFailed(true));
    return () => {
      ignore = true;
    };
  }, [attempt]);

  const retry = () => {
    setFailed(false);
    setNotes(null);
    setAttempt((n) => n + 1);
  };

  const forget = async (id) => {
    const before = notes;
    setForgetError("");
    setNotes((list) => list.filter((note) => note.id !== id));
    try {
      await forgetNote(id);
    } catch {
      setNotes(before);
      setForgetError("Couldn’t forget that note. Try again.");
    }
  };

  if (failed) {
    return (
      <LearningSection title="Your tutor remembers">
        <LoadError what="your notes" onRetry={retry} />
      </LearningSection>
    );
  }

  if (!notes) {
    return (
      <LearningSection title="Your tutor remembers">
        <NotesSkeleton />
      </LearningSection>
    );
  }

  return (
    <LearningSection title="Your tutor remembers">
      {forgetError && <p className="py-1 text-[0.8125rem] text-destructive">{forgetError}</p>}
      {notes.length === 0 && (
        <p className="py-1 text-[0.8125rem] text-muted-foreground">Nothing yet. Your tutor learns as you chat.</p>
      )}

      {notes.map((note) => (
        <div key={note.id} className="group flex items-start gap-2 py-1.75 text-[0.8125rem] text-muted-foreground">
          <span>{note.note}</span>
          <button
            onClick={() => forget(note.id)}
            aria-label="Forget this note"
            title="Forget this note"
            className="ml-auto shrink-0 p-0.5 opacity-0 group-hover:opacity-100 hover:text-foreground focus-visible:opacity-100"
          >
            <X className="size-3.5" />
          </button>
        </div>
      ))}
    </LearningSection>
  );
}

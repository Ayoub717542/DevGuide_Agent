import { X } from "lucide-react";
import { useEffect, useState } from "react";
import { forgetNote, getNotes } from "../../api/learning";
import LearningSection from "./LearningSection";

export default function TutorNotes() {
  const [notes, setNotes] = useState([]);

  useEffect(() => {
    getNotes()
      .then(setNotes)
      .catch(() => setNotes([]));
  }, []);

  const forget = async (id) => {
    await forgetNote(id);
    setNotes((list) => list.filter((note) => note.id !== id));
  };

  return (
    <LearningSection title="Your tutor remembers">
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

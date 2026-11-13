import { X } from "lucide-react";
import { useEffect, useState } from "react";
import { getProgress } from "../../api/learning";
import NeedsPractice from "./NeedsPractice";
import Progress from "./Progress";
import ReviewToday from "./ReviewToday";
import TutorNotes from "./TutorNotes";

export default function LearningRail({ onReview, onClose }) {
  const [results, setResults] = useState([]);

  useEffect(() => {
    getProgress()
      .then(setResults)
      .catch(() => setResults([]));
  }, []);

  return (
    <aside className="fixed inset-y-0 right-0 z-40 min-h-0 w-78 max-w-full overflow-auto bg-secondary px-4.5 py-5 duration-200 slide-in-from-right max-[68.75rem]:motion-safe:animate-in min-[68.75rem]:static min-[68.75rem]:w-auto">
      <div className="mb-4.5 flex items-center justify-between">
        <h3 className="text-[0.9375rem] font-semibold">My learning</h3>
        <button
          onClick={onClose}
          aria-label="Close My learning"
          className="grid size-8 place-items-center rounded-md text-muted-foreground hover:bg-card hover:text-foreground min-[68.75rem]:hidden"
        >
          <X className="size-4" />
        </button>
      </div>
      <ReviewToday onReview={onReview} />
      <Progress results={results} />
      <NeedsPractice results={results} />
      <TutorNotes />
    </aside>
  );
}

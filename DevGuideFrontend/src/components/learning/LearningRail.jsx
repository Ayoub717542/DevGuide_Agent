import { X } from "lucide-react";
import { useEffect, useState } from "react";
import { getProgress } from "../../api/learning";
import NeedsPractice from "./NeedsPractice";
import Progress from "./Progress";
import ReviewToday from "./ReviewToday";
import TutorNotes from "./TutorNotes";

export default function LearningRail({ open, onReview, onClose }) {
  const [results, setResults] = useState(null);
  const [failed, setFailed] = useState(false);
  const [attempt, setAttempt] = useState(0);

  useEffect(() => {
    let ignore = false;
    getProgress()
      .then((list) => !ignore && setResults(list))
      .catch(() => !ignore && setFailed(true));
    return () => {
      ignore = true;
    };
  }, [attempt]);

  const retry = () => {
    setFailed(false);
    setResults(null);
    setAttempt((n) => n + 1);
  };

  return (
    <aside
      className={`fixed top-0 right-0 z-40 h-dvh min-h-0 w-78 max-w-full overflow-auto bg-secondary px-4.5 pt-5 pb-[max(1.25rem,env(safe-area-inset-bottom))] transition-[translate,visibility] duration-200 ease-out motion-reduce:transition-none min-[68.75rem]:static min-[68.75rem]:h-auto min-[68.75rem]:w-auto min-[68.75rem]:translate-x-0 ${
        open ? "translate-x-0" : "invisible translate-x-full min-[68.75rem]:hidden"
      }`}
    >
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
      <Progress results={results} failed={failed} onRetry={retry} />
      <NeedsPractice results={results} failed={failed} onRetry={retry} />
      <TutorNotes />
    </aside>
  );
}

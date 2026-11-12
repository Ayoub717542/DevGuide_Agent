import { useEffect, useState } from "react";
import { getProgress } from "../../api/learning";
import NeedsPractice from "./NeedsPractice";
import Progress from "./Progress";
import ReviewToday from "./ReviewToday";

export default function LearningRail({ onReview }) {
  const [results, setResults] = useState([]);

  useEffect(() => {
    getProgress()
      .then(setResults)
      .catch(() => setResults([]));
  }, []);

  return (
    <aside className="hidden min-h-0 overflow-auto border-l bg-secondary px-4.5 py-5 min-[68.75rem]:block">
      <h3 className="mb-4.5 text-[0.9375rem] font-semibold">My learning</h3>
      <ReviewToday onReview={onReview} />
      <Progress results={results} />
      <NeedsPractice results={results} />
    </aside>
  );
}

import { useEffect, useState } from "react";
import { getReviews } from "../../api/learning";
import LearningSection from "./LearningSection";
import { topicName } from "./topicName";

const DAY = 24 * 60 * 60 * 1000;

function withLabel(review, now) {
  const score = `Last score ${review.lastScore}/${review.lastTotal}`;
  if (review.due) return { ...review, label: `${score}, due now` };
  const days = Math.max(1, Math.ceil((new Date(review.nextReview) - now) / DAY));
  return { ...review, label: `${score}, next review in ${days} ${days === 1 ? "day" : "days"}` };
}

export default function ReviewToday({ onReview }) {
  const [reviews, setReviews] = useState([]);

  useEffect(() => {
    getReviews()
      .then((list) => {
        const now = Date.now();
        const dueFirst = [...list].sort((a, b) => b.due - a.due);
        setReviews(dueFirst.slice(0, 5).map((review) => withLabel(review, now)));
      })
      .catch(() => setReviews([]));
  }, []);

  return (
    <LearningSection title="Review today">
      {reviews.length === 0 && (
        <p className="py-1 text-[0.8125rem] text-muted-foreground">Nothing to review yet. Take a quiz to start.</p>
      )}

      {reviews.map((review) => (
        <div
          key={review.concept}
          className={`flex items-center gap-2.5 border-b py-2.25 last:border-0 ${review.due ? "" : "opacity-70"}`}
        >
          <div className="min-w-0">
            <b className="flex items-center gap-1.75 text-[0.84375rem] font-medium">
              {review.due && <span className="size-1.5 shrink-0 rounded-full bg-destructive" />}
              {topicName(review.concept)}
            </b>
            <small className="text-xs text-muted-foreground">{review.label}</small>
          </div>
          {review.due && (
            <button
              onClick={() => onReview(review.concept)}
              className="ml-auto h-7 shrink-0 rounded-md border bg-foreground/5 px-2.5 text-xs font-medium whitespace-nowrap hover:border-muted-foreground/40"
            >
              Review now
            </button>
          )}
        </div>
      ))}
    </LearningSection>
  );
}

import { useEffect, useState } from "react";
import { getReviews } from "../../api/learning";
import { Skeleton } from "@/components/ui/skeleton";
import LoadError from "./LoadError";
import LearningSection from "./LearningSection";
import { topicName } from "./topicName";

const DAY = 24 * 60 * 60 * 1000;

function withLabel(review, now) {
  const score = `Last score ${review.lastScore}/${review.lastTotal}`;
  if (review.due) return { ...review, label: `${score}, due now` };
  const days = Math.max(1, Math.ceil((new Date(review.nextReview) - now) / DAY));
  return { ...review, label: `${score}, next review in ${days} ${days === 1 ? "day" : "days"}` };
}

function ReviewSkeleton() {
  return (
    <div aria-busy="true">
      <span className="sr-only">Loading reviews…</span>
      {[0, 1, 2].map((i) => (
        <div key={i} className="flex items-center gap-2.5 border-b py-2.25 last:border-0">
          <div className="flex-1 space-y-1.5">
            <Skeleton className="h-3.5 w-2/3" />
            <Skeleton className="h-3 w-5/6" />
          </div>
          {i === 0 && <Skeleton className="h-7 w-20 shrink-0" />}
        </div>
      ))}
    </div>
  );
}

export default function ReviewToday({ onReview }) {
  const [reviews, setReviews] = useState(null);
  const [failed, setFailed] = useState(false);
  const [attempt, setAttempt] = useState(0);

  useEffect(() => {
    let ignore = false;
    getReviews()
      .then((list) => {
        if (ignore) return;
        const now = Date.now();
        const dueFirst = [...list].sort((a, b) => b.due - a.due);
        setReviews(dueFirst.slice(0, 5).map((review) => withLabel(review, now)));
      })
      .catch(() => !ignore && setFailed(true));
    return () => {
      ignore = true;
    };
  }, [attempt]);

  const retry = () => {
    setFailed(false);
    setReviews(null);
    setAttempt((n) => n + 1);
  };

  if (failed) {
    return (
      <LearningSection title="Review today">
        <LoadError what="your reviews" onRetry={retry} />
      </LearningSection>
    );
  }

  if (!reviews) {
    return (
      <LearningSection title="Review today">
        <ReviewSkeleton />
      </LearningSection>
    );
  }

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
              <span className="first-letter:uppercase">{topicName(review.concept)}</span>
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

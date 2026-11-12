import ReviewToday from "./ReviewToday";

export default function LearningRail({ onReview }) {
  return (
    <aside className="hidden min-h-0 overflow-auto border-l bg-secondary px-4.5 py-5 min-[68.75rem]:block">
      <h3 className="mb-4.5 text-[0.9375rem] font-semibold">My learning</h3>
      <ReviewToday onReview={onReview} />
    </aside>
  );
}

import LearningSection from "./LearningSection";
import { topicName } from "./topicName";

function weakestTopics(results) {
  const latest = new Map();
  for (const result of results) {
    if (!latest.has(result.topic)) latest.set(result.topic, result);
  }
  return [...latest.values()]
    .filter((r) => r.total > 0 && r.score < r.total)
    .sort((a, b) => a.score / a.total - b.score / b.total)
    .slice(0, 3);
}

export default function NeedsPractice({ results }) {
  const weakest = weakestTopics(results);

  return (
    <LearningSection title="Needs more practice">
      {weakest.length === 0 && (
        <p className="py-1 text-[0.8125rem] text-muted-foreground">Nothing yet. Keep going!</p>
      )}

      {weakest.map((result) => (
        <div key={result.topic} className="grid grid-cols-[1fr_3.75rem_1.875rem] items-center gap-2.5 py-1.25 text-[0.8125rem]">
          <span className="truncate">{topicName(result.topic)}</span>
          <i className="h-1 overflow-hidden rounded-sm bg-border">
            <u className="block h-full bg-muted-foreground" style={{ width: `${(result.score / result.total) * 100}%` }} />
          </i>
          <em className="text-right text-xs text-muted-foreground not-italic">
            {result.score}/{result.total}
          </em>
        </div>
      ))}
    </LearningSection>
  );
}

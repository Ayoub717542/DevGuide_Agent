const percent = (result) => (result.total > 0 ? (result.score / result.total) * 100 : 0);

function linePoints(results) {
  const last = results.length - 1;
  return results.map((result, i) => ({ x: (i / last) * 100, y: 26 - (percent(result) * 24) / 100 }));
}

export default function Progress({ results }) {
  const recent = results.slice(0, 9).reverse();
  const score = recent.reduce((sum, r) => sum + r.score, 0);
  const total = recent.reduce((sum, r) => sum + r.total, 0);
  const average = total > 0 ? Math.round((score / total) * 100) : 0;
  const points = recent.length > 1 ? linePoints(recent) : [];
  const end = points[points.length - 1];

  return (
    <section className="mb-6.5">
      <h2 className="mb-2.5 text-[0.78125rem] font-medium text-muted-foreground">Progress</h2>

      {recent.length === 0 ? (
        <p className="text-[0.8125rem] text-muted-foreground">No quiz results yet.</p>
      ) : (
        <div className="flex items-center gap-3.5">
          <div>
            <b className="block text-xl leading-tight font-semibold tracking-tight">{average}%</b>
            <small className="text-xs text-muted-foreground">
              average of your last {recent.length} {recent.length === 1 ? "result" : "results"}
            </small>
          </div>
          {end && (
            <svg viewBox="0 0 100 28" preserveAspectRatio="none" className="h-7 flex-1 overflow-visible" aria-hidden="true">
              <polyline
                points={points.map((p) => `${p.x},${p.y}`).join(" ")}
                className="fill-none stroke-muted-foreground"
                strokeWidth="1.6"
                strokeLinejoin="round"
                vectorEffect="non-scaling-stroke"
              />
              <circle cx={end.x} cy={end.y} r="2.5" className="fill-success" />
            </svg>
          )}
        </div>
      )}
    </section>
  );
}

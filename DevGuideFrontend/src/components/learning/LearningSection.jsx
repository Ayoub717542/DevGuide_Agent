export default function LearningSection({ title, children }) {
  return (
    <section className="mb-5">
      <h2 className="mb-2 px-1 text-xs font-medium text-muted-foreground">{title}</h2>
      <div className="rounded-xl bg-card px-3.5 py-2">{children}</div>
    </section>
  );
}

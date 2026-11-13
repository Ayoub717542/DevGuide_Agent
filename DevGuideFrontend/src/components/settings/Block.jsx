export function Block({ title, lead, danger, children }) {
  return (
    <div className="mb-10">
      <h2 className="text-[0.9375rem] font-semibold">{title}</h2>
      <p className="mt-0.5 mb-3.5 max-w-[62ch] text-[0.84375rem] text-muted-foreground">{lead}</p>
      <div className={`rounded-xl border bg-secondary ${danger ? "border-destructive/30" : ""}`}>{children}</div>
    </div>
  );
}

export function Row({ title, help, children }) {
  return (
    <div className="grid grid-cols-1 gap-3 px-4 py-4 sm:grid-cols-[minmax(0,1fr)_minmax(0,auto)] sm:items-center sm:gap-6 sm:px-5 [&+&]:border-t">
      <div>
        <h3 className="text-sm font-medium">{title}</h3>
        {help}
      </div>
      <div className="flex flex-wrap items-center gap-2 sm:justify-end">{children}</div>
    </div>
  );
}

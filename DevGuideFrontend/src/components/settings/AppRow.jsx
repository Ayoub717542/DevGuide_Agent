export default function AppRow({ logo, name, connected, text, children }) {
  return (
    <div className="flex items-center gap-4 px-5 py-4 [&+&]:border-t">
      <span className="grid size-9 shrink-0 place-items-center rounded-lg border bg-card">
        <img src={logo} alt="" className="size-5" />
      </span>
      <div className="min-w-0">
        <h3 className="flex items-center gap-2 text-sm font-medium">
          {name}
          {connected ? (
            <span className="rounded-full bg-success/10 px-2 py-px text-xs font-medium text-success ring-1 ring-success/30 ring-inset">
              Connected
            </span>
          ) : (
            <span className="rounded-full bg-card px-2 py-px text-xs font-medium text-muted-foreground ring-1 ring-border ring-inset">
              Not connected
            </span>
          )}
        </h3>
        <p className="mt-0.5 text-[0.8125rem] text-muted-foreground">{text}</p>
      </div>
      <div className="ml-auto shrink-0">{children}</div>
    </div>
  );
}

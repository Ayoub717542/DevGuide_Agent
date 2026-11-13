export default function ChoiceRow({ title, options, value, onChange }) {
  const buttonStyle = (selected, isAuto) => {
    if (!selected) return "text-muted-foreground hover:text-foreground";
    if (isAuto) return "bg-success/10 text-success ring-1 ring-success/30 ring-inset";
    return "bg-card text-foreground ring-1 ring-border ring-inset";
  };

  const choices = [{ value: null, label: "Auto" }, ...options];

  return (
    <div className="px-5 py-4 [&+&]:border-t">
      <h3 className="mb-2.5 text-sm font-medium">{title}</h3>
      <div role="group" aria-label={title} className="flex w-max max-w-full flex-wrap gap-1 rounded-[0.5625rem] border bg-background p-0.75">
        {choices.map((choice) => (
          <button
            key={choice.label}
            aria-pressed={value === choice.value}
            onClick={() => onChange(choice.value)}
            className={`rounded-md px-2.75 py-1.25 text-[0.78125rem] ${buttonStyle(value === choice.value, choice.value === null)}`}
          >
            {choice.label}
          </button>
        ))}
      </div>
    </div>
  );
}

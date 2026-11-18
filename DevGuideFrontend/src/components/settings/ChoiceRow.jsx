export default function ChoiceRow({ title, options, value, onChange }) {
  const buttonStyle = (selected, isAuto) => {
    if (!selected) return "text-muted-foreground hover:text-foreground max-lg:ring-1 max-lg:ring-border max-lg:ring-inset";
    if (isAuto) return "bg-success/10 text-success ring-1 ring-success/30 ring-inset";
    return "bg-card text-foreground ring-1 ring-border ring-inset max-lg:ring-foreground/40";
  };

  const choices = [{ value: null, label: "Auto" }, ...options];

  return (
    <div className="px-5 py-4 [&+&]:border-t">
      <h3 className="mb-3 text-sm font-medium">{title}</h3>
      <div
        role="group"
        aria-label={title}
        className="flex flex-wrap gap-2 lg:w-max lg:max-w-full lg:gap-1 lg:rounded-[0.5625rem] lg:border lg:bg-background lg:p-0.75"
      >
        {choices.map((choice) => (
          <button
            key={choice.label}
            aria-pressed={value === choice.value}
            onClick={() => onChange(choice.value)}
            className={`h-9 rounded-lg px-3 text-[0.8125rem] transition-colors lg:h-auto lg:rounded-md lg:px-2.75 lg:py-1.25 lg:text-xs ${buttonStyle(value === choice.value, choice.value === null)}`}
          >
            {choice.label}
          </button>
        ))}
      </div>
    </div>
  );
}

import { FileText, Image } from "lucide-react";

export default function FileLabel({ name, children }) {
  const Icon = name.toLowerCase().endsWith(".pdf") ? FileText : Image;

  return (
    <span className="inline-flex items-center gap-1.5 rounded-md border bg-card px-2 py-0.75 text-xs text-muted-foreground">
      <Icon className="size-3.5" />
      {name}
      {children}
    </span>
  );
}

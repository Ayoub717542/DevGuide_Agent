import { AlertCircle } from "lucide-react";
import { Button } from "@/components/ui/button";

export default function LoadError({ what, onRetry }) {
  return (
    <div className="flex flex-col items-start gap-2 py-2 text-[0.8125rem]">
      <p className="flex items-center gap-1.5 text-muted-foreground">
        <AlertCircle className="size-3.5 shrink-0 text-destructive" aria-hidden="true" />
        Couldn’t load {what}.
      </p>
      <Button variant="outline" size="xs" onClick={onRetry}>
        Try again
      </Button>
    </div>
  );
}

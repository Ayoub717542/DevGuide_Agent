import { useState } from "react";
import { Button, buttonVariants } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { API_URL } from "../api/axiosApi";

const PROVIDERS = [
  { id: "github", name: "GitHub" },
  { id: "google", name: "Google" },
];

const FEATURES = [
  "Explains your repo step by step",
  "Quizzes you on what you learned",
  "Connects GitHub & Trello",
];

export function Field({ id, label, ...inputProps }) {
  return (
    <div className="grid gap-2">
      <Label htmlFor={id}>{label}</Label>
      <Input id={id} className="h-10" required {...inputProps} />
    </div>
  );
}

export default function AuthLayout({ title, subtitle, submitLabel, onSubmit, footer, initialError = "", children }) {
  const [error, setError] = useState(initialError);
  const [sending, setSending] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSending(true);
    try {
      await onSubmit();
    } catch (err) {
      setError(err.response?.data?.message || "Something went wrong. Try again.");
    } finally {
      setSending(false);
    }
  };

  return (
    <div className="grid min-h-screen place-items-center p-4">
      <div className="grid w-full max-w-3xl overflow-hidden rounded-xl border bg-card md:grid-cols-2">
        <form onSubmit={handleSubmit} className="flex flex-col gap-5 p-8">
          <div className="flex items-center gap-2 font-semibold">
            <img src="/devguide-logo.svg" alt="" className="size-7" />
            DevGuide AI
          </div>

          <div className="grid gap-1">
            <h1 className="text-2xl font-bold">{title}</h1>
            <p className="text-muted-foreground">{subtitle}</p>
          </div>

          <div className="grid grid-cols-2 gap-3">
            {PROVIDERS.map((provider) => (
              <a key={provider.id} href={`${API_URL}/oauth2/authorization/${provider.id}`}
                 className={buttonVariants({ variant: "outline", className: "h-10 gap-2" })}>
                <img src={`/${provider.id}.svg`} alt="" className="size-4" />
                {provider.name}
              </a>
            ))}
          </div>

          <div className="flex items-center gap-3 text-sm text-muted-foreground">
            <span className="h-px flex-1 bg-border" />
            or continue with email
            <span className="h-px flex-1 bg-border" />
          </div>

          {children}

          {error && <p className="text-sm text-destructive">{error}</p>}

          <Button type="submit" className="h-10" disabled={sending}>
            {sending ? "Please wait…" : submitLabel}
          </Button>

          <p className="text-sm text-muted-foreground">{footer}</p>
        </form>

        <div className="hidden flex-col justify-end gap-4 border-l bg-secondary p-8 md:flex">
          <h2 className="text-xl font-semibold">Your AI pair for learning to code.</h2>
          <ul className="grid gap-3 text-sm text-muted-foreground">
            {FEATURES.map((feature) => (
              <li key={feature} className="flex gap-2">
                <span className="text-brand">→</span>
                {feature}
              </li>
            ))}
          </ul>
        </div>
      </div>
    </div>
  );
}

import { useState } from "react";
import { Link } from "react-router-dom";
import { ArrowRight, LockKeyhole, Mail, User } from "lucide-react";
import { Button, buttonVariants } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { cn } from "cn";
import { API_URL } from "../api/axiosApi";
import AuthBackground from "./AuthBackground";

const PROVIDERS = [
  { id: "google", name: "Google" },
  { id: "github", name: "GitHub" },
];

const FIELD_ICONS = { email: Mail, password: LockKeyhole };

export function Field({ id, label, type = "text", ...inputProps }) {
  const Icon = FIELD_ICONS[type] || User;
  return (
    <div className="relative">
      <Label htmlFor={id} className="sr-only">{label}</Label>
      <Icon className="pointer-events-none absolute top-1/2 left-4 size-4 -translate-y-1/2 text-muted-foreground" strokeWidth={1.5} />
      <Input id={id} type={type} placeholder={label} className="h-11 rounded-xl pl-11" required {...inputProps} />
    </div>
  );
}

export default function AuthLayout({ title, subtitle, submitLabel, onSubmit, topLink, initialError = "", notice = "", social = true, children }) {
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
    <div className="relative min-h-svh overflow-hidden">
      <AuthBackground />

      <header className="relative flex items-center justify-between px-6 py-7 text-sm sm:px-12">
        <Link to="/login" className="flex items-center gap-2.5 text-base font-medium">
          <img src="/devguide-logo.svg" alt="" className="size-8" />
          DevGuide
        </Link>
        {topLink && (
          <div className="flex items-center gap-2 leading-none">
            <span className="hidden text-muted-foreground sm:inline">{topLink.text}</span>
            <Link to={topLink.to} className="group inline-flex items-center gap-1 font-medium transition-colors hover:text-white">
              {topLink.label}
              <ArrowRight className="size-4 transition-transform group-hover:translate-x-0.5" strokeWidth={1.75} />
            </Link>
          </div>
        )}
      </header>

      <main className="relative grid min-h-[calc(100svh-6rem)] place-items-center px-4 pb-24">
        <form onSubmit={handleSubmit} className="flex w-full max-w-88 flex-col gap-3.5">
          <div className="mb-3 grid gap-1.5 text-center">
            <h1 className="text-2xl font-medium">{title}</h1>
            <p className="text-muted-foreground">{subtitle}</p>
          </div>

          {social && (
            <>
              {PROVIDERS.map((provider) => (
                <a key={provider.id} href={`${API_URL}/oauth2/authorization/${provider.id}`}
                   className={cn(buttonVariants({ variant: "outline", className: "h-11 gap-2.5 rounded-full" }))}>
                  <img src={`/${provider.id}.svg`} alt="" className="size-4.5" />
                  Continue with {provider.name}
                </a>
              ))}
              <p className="my-1 text-center text-sm text-muted-foreground">or</p>
            </>
          )}

          {children}

          {error && <p className="text-center text-sm text-destructive">{error}</p>}
          {notice && !error && <p className="text-center text-sm text-success">{notice}</p>}

          <Button type="submit" className="mt-2 h-11 rounded-full" disabled={sending}>
            {sending ? "Please wait…" : submitLabel}
          </Button>
        </form>
      </main>
    </div>
  );
}

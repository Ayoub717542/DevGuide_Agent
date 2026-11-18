import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Check } from "lucide-react";
import { Button } from "@/components/ui/button";
import { getPreferences, updatePreferences } from "../api/user";
import { useAuth } from "../context/AuthContext";
import { AdvancedAvatar, BeginnerAvatar, IntermediateAvatar } from "../components/welcome/LevelAvatars";
import { markWelcomeSeen } from "../components/welcome/welcomeSeen";

const LEVELS = [
  {
    value: "BEGINNER",
    title: "New to coding",
    description: "Just starting out. Every term explained, step by step",
    Avatar: BeginnerAvatar,
  },
  {
    value: "INTERMEDIATE",
    title: "I know the basics",
    description: "I know loops and functions. Skips the basics, explains new ideas",
    Avatar: IntermediateAvatar,
  },
  {
    value: "ADVANCED",
    title: "Experienced developer",
    description: "I build real projects. Straight to the point, with trade-offs",
    Avatar: AdvancedAvatar,
  },
];

function LevelCard({ level, checked, onPick }) {
  const { Avatar } = level;

  return (
    <label className="group relative grid cursor-pointer content-start justify-items-center gap-5 rounded-2xl border bg-secondary px-6 pt-7 pb-6 text-center transition-colors hover:border-foreground/20 hover:bg-card has-checked:border-success has-checked:bg-card has-focus-visible:outline-2 has-focus-visible:outline-offset-3 has-focus-visible:outline-success max-sm:grid-cols-[auto_1fr] max-sm:items-center max-sm:justify-items-start max-sm:gap-4 max-sm:p-4 max-sm:pr-11 max-sm:text-left">
      <input type="radio" name="level" value={level.value} checked={checked} onChange={onPick} className="sr-only" />
      <span className="absolute top-3.5 right-3.5 grid size-5 place-items-center rounded-full border border-foreground/20 text-transparent group-has-checked:border-success group-has-checked:bg-success group-has-checked:text-background">
        <Check className="size-3" strokeWidth={3} />
      </span>
      <span className="size-32 transition-transform duration-200 group-hover:-translate-y-0.5 motion-reduce:transition-none motion-reduce:group-hover:translate-y-0 max-sm:size-18">
        <Avatar />
      </span>
      <span className="grid gap-1.5">
        <span className="font-semibold">{level.title}</span>
        <span className="text-sm text-balance text-muted-foreground">{level.description}</span>
      </span>
    </label>
  );
}

export default function Welcome() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [level, setLevel] = useState("");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  const finish = () => {
    markWelcomeSeen(user.email);
    navigate("/", { replace: true });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError("");
    try {
      const preferences = await getPreferences();
      await updatePreferences({ ...preferences, experienceLevel: level });
      finish();
    } catch {
      setError("Could not save your level. Try again");
      setSaving(false);
    }
  };

  return (
    <div className="grid min-h-screen grid-rows-[auto_1fr]">
      <div className="flex items-center gap-2 px-12 py-8 font-semibold max-sm:p-6">
        <img src="/devguide-logo.svg" alt="" className="size-7" />
        DevGuide
      </div>

      <main className="grid place-items-center px-6 pt-4 pb-20 max-sm:place-items-start max-sm:px-4 max-sm:pt-2 max-sm:pb-8">
        <form onSubmit={handleSubmit} className="grid w-full max-w-208 justify-items-center gap-10 text-center max-sm:gap-7">
          <div className="grid gap-3">
            <h1 className="text-[2rem] leading-tight font-semibold tracking-tight max-sm:text-[1.625rem]">
              How much coding have you done?
            </h1>
            <p className="max-w-136 text-balance text-muted-foreground">
              Pick the closest match so your tutor explains things at the right pace. You can change it later in Settings
            </p>
          </div>

          <fieldset className="grid w-full grid-cols-3 gap-4 max-sm:grid-cols-1 max-sm:gap-3">
            <legend className="sr-only">Your coding level</legend>
            {LEVELS.map((item) => (
              <LevelCard key={item.value} level={item} checked={level === item.value} onPick={() => setLevel(item.value)} />
            ))}
          </fieldset>

          <div className="grid w-full max-w-80 justify-items-center gap-4">
            {error && <p role="alert" className="text-sm text-destructive">{error}</p>}
            <Button type="submit" className="h-10 w-full" disabled={!level || saving}>
              {saving ? "Saving…" : "Continue"}
            </Button>
            <Link to="/" replace onClick={() => markWelcomeSeen(user.email)} className="rounded-sm text-sm text-muted-foreground hover:text-foreground focus-visible:outline-2 focus-visible:outline-offset-3 focus-visible:outline-ring">
              Skip for now
            </Link>
          </div>
        </form>
      </main>
    </div>
  );
}

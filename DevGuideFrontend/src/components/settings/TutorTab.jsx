import { Check } from "lucide-react";
import { useEffect, useState } from "react";
import { getPreferences, updatePreferences } from "../../api/user";
import { Button } from "../ui/button";
import ChoiceRow from "./ChoiceRow";

const PREFERENCES = [
  {
    key: "experienceLevel",
    title: "Experience level",
    options: [
      { value: "BEGINNER", label: "Beginner" },
      { value: "INTERMEDIATE", label: "Intermediate" },
      { value: "ADVANCED", label: "Advanced" },
    ],
  },
  {
    key: "explanationStyle",
    title: "Explanation style",
    options: [
      { value: "STEP_BY_STEP", label: "Step by step" },
      { value: "EXAMPLES_FIRST", label: "Examples first" },
      { value: "ANALOGIES", label: "Analogies" },
      { value: "DIRECT", label: "Direct" },
    ],
  },
  {
    key: "responseLength",
    title: "Answer length",
    options: [
      { value: "SHORT", label: "Short" },
      { value: "MEDIUM", label: "Medium" },
      { value: "DETAILED", label: "Detailed" },
    ],
  },
  {
    key: "codePreference",
    title: "Code in answers",
    options: [
      { value: "SMALL_EXAMPLES", label: "Small examples" },
      { value: "FULL_CODE", label: "Full code" },
      { value: "LITTLE_CODE", label: "Little code" },
    ],
  },
  {
    key: "tone",
    title: "Tone",
    options: [
      { value: "NEUTRAL", label: "Neutral" },
      { value: "FRIENDLY", label: "Friendly" },
      { value: "HUMOROUS", label: "Humorous" },
      { value: "ENCOURAGING", label: "Encouraging" },
    ],
  },
];

export default function TutorTab() {
  const [saved, setSaved] = useState(null);
  const [draft, setDraft] = useState(null);
  const [saving, setSaving] = useState(false);
  const [done, setDone] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    getPreferences()
      .then((preferences) => {
        setSaved(preferences);
        setDraft(preferences);
      })
      .catch(() => setError("Could not load your preferences. Reload the page."));
  }, []);

  const changed = JSON.stringify(draft) !== JSON.stringify(saved);

  const pick = (key, value) => {
    setDraft({ ...draft, [key]: value });
    setDone(false);
  };

  const save = async () => {
    setSaving(true);
    setError("");
    try {
      const result = await updatePreferences(draft);
      setSaved(result);
      setDraft(result);
      setDone(true);
    } catch {
      setError("Could not save. Try again.");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div>
      <h2 className="text-[0.9375rem] font-semibold">How your tutor answers</h2>
      <p className="mt-0.5 mb-3.5 max-w-[62ch] text-[0.84375rem] text-muted-foreground">
        Auto lets your tutor learn this from your chats. Pick a value to decide yourself.
      </p>

      {draft && (
        <div className="rounded-xl border bg-secondary">
          {PREFERENCES.map((pref) => (
            <ChoiceRow
              key={pref.key}
              title={pref.title}
              options={pref.options}
              value={draft[pref.key]}
              onChange={(value) => pick(pref.key, value)}
            />
          ))}
        </div>
      )}

      {error && <p className="mt-3 text-xs text-destructive">{error}</p>}

      <div className="mt-4 flex items-center gap-2">
        <Button onClick={save} disabled={!changed || saving}>
          {saving ? "Saving…" : "Save changes"}
        </Button>
        <Button variant="ghost" onClick={() => setDraft(saved)} disabled={!changed || saving}>
          Cancel
        </Button>
        {done && !changed && (
          <span className="inline-flex items-center gap-1 text-[0.78125rem] text-success">
            <Check className="size-3.25" /> Saved
          </span>
        )}
      </div>
    </div>
  );
}

import { useState } from "react";
import AccountTab from "../components/settings/AccountTab";
import AppsTab from "../components/settings/AppsTab";
import TutorTab from "../components/settings/TutorTab";

const TABS = [
  { id: "account", label: "Account" },
  { id: "tutor", label: "Tutor" },
  { id: "apps", label: "Connected apps" },
];

export default function Settings() {
  const [tab, setTab] = useState(() => (window.location.hash.includes("trello") ? "apps" : "account"));

  return (
    <main className="min-w-0 overflow-auto">
      <div className="mx-auto max-w-260 px-10 pt-9 pb-24">
        <h1 className="text-xl font-semibold tracking-tight">Settings</h1>

        <div role="tablist" aria-label="Settings" className="mt-4.5 flex gap-1 border-b">
          {TABS.map((item) => (
            <button
              key={item.id}
              role="tab"
              aria-selected={tab === item.id}
              onClick={() => setTab(item.id)}
              className={`relative px-3 pt-2.5 pb-3 text-[0.84375rem] font-medium ${
                tab === item.id
                  ? "text-foreground after:absolute after:inset-x-2.5 after:-bottom-px after:h-0.5 after:rounded-sm after:bg-success"
                  : "text-muted-foreground hover:text-foreground"
              }`}
            >
              {item.label}
            </button>
          ))}
        </div>

        <section role="tabpanel" className="pt-7">
          {tab === "account" && <AccountTab />}
          {tab === "tutor" && <TutorTab />}
          {tab === "apps" && <AppsTab />}
        </section>
      </div>
    </main>
  );
}

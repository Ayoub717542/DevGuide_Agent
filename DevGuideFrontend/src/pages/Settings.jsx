import { useState } from "react";
import { Menu } from "lucide-react";
import AccountTab from "../components/settings/AccountTab";
import AppsTab from "../components/settings/AppsTab";
import TutorTab from "../components/settings/TutorTab";
import UsersTab from "../components/settings/UsersTab";
import { useAuth } from "../context/AuthContext";

const TABS = [
  { id: "account", label: "Account" },
  { id: "tutor", label: "Tutor" },
  { id: "apps", label: "Connected apps" },
];

export default function Settings({ menuOpen, onOpenMenu }) {
  const { user } = useAuth();
  const [tab, setTab] = useState(() => (window.location.hash.includes("trello") ? "apps" : "account"));
  const tabs = user.role === "ADMIN" ? [...TABS, { id: "users", label: "Users" }] : TABS;

  return (
    <main className="min-w-0 overflow-auto">
      <div className="mx-auto max-w-260 px-4 pt-3 pb-24 sm:px-10 md:pt-9">
        <div className="flex items-center gap-2">
          <button
            onClick={onOpenMenu}
            aria-label="Open chat list"
            aria-expanded={menuOpen}
            className="-ml-2 grid size-10 shrink-0 place-items-center rounded-lg text-muted-foreground transition-colors hover:bg-card hover:text-foreground md:hidden"
          >
            <Menu className="size-5" />
          </button>
          <h1 className="text-xl font-medium lg:text-2xl">Settings</h1>
        </div>

        <div role="tablist" aria-label="Settings" className="mt-4 flex gap-1 border-b">
          {tabs.map((item) => (
            <button
              key={item.id}
              role="tab"
              aria-selected={tab === item.id}
              onClick={() => setTab(item.id)}
              className={`relative px-3 py-3 text-sm font-medium lg:text-base ${
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
          {tab === "users" && user.role === "ADMIN" && <UsersTab />}
        </section>
      </div>
    </main>
  );
}

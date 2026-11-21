import { useEffect, useEffectEvent, useRef, useState } from "react";
import { toast } from "sonner";
import { API_URL } from "../../api/axiosApi";
import { getMe } from "../../api/Client";
import { disconnectTrello, finishConnect, getConnectUrl } from "../../api/trello";
import { disconnectGithub } from "../../api/user";
import { useAuth } from "../../context/AuthContext";
import { Button, buttonVariants } from "../ui/button";
import AppRow from "./AppRow";

const trelloReturn = () => new URLSearchParams(window.location.hash.slice(1));

export default function AppsTab() {
  const { user, setUser } = useAuth();
  const [busy, setBusy] = useState(false);
  const finishedRef = useRef(false);

  const refreshUser = async () => setUser(await getMe());

  const finishTrello = useEffectEvent(() => {
    const params = trelloReturn();
    const code = params.get("trello_code");
    const state = params.get("trello_state");
    if (finishedRef.current || (!code && !params.get("trello"))) return;
    finishedRef.current = true;
    window.history.replaceState(null, "", "/settings");
    if (!code) {
      if (params.get("trello") === "error") toast.error("Trello connection failed", { description: "Try again" });
      return;
    }

    const request = finishConnect(code, state).then((result) => {
      if (!result.connected) throw new Error("not connected");
      return refreshUser();
    });
    toast.promise(request, {
      loading: "Connecting Trello…",
      success: "Trello connected",
      error: "Trello connection failed",
    });
  });

  useEffect(() => finishTrello(), []);

  const connectTrello = async () => {
    setBusy(true);
    try {
      const { url } = await getConnectUrl();
      window.location.href = url;
    } catch {
      toast.error("Could not open Trello", { description: "Try again" });
      setBusy(false);
    }
  };

  const disconnect = async (action, name) => {
    setBusy(true);
    const request = action().then(refreshUser);
    toast.promise(request, {
      loading: `Disconnecting ${name}…`,
      success: `${name} disconnected`,
      error: `Could not disconnect ${name}`,
    });
    try {
      await request;
    } catch {
      return;
    } finally {
      setBusy(false);
    }
  };

  return (
    <div>
      <h2 className="text-[0.9375rem] font-medium lg:text-lg">Connected apps</h2>
      <p className="mt-0.5 mb-4 max-w-[62ch] text-sm text-muted-foreground lg:text-[0.9375rem]">
        Your tutor uses these only when you ask about them.
      </p>

      <div className="rounded-xl border bg-secondary">
        <AppRow
          logo="/github.svg"
          name="GitHub"
          connected={user.githubConnected}
          text={
            user.githubConnected
              ? "Your tutor can read your repositories when you ask"
              : "Log in with GitHub to connect. It must use the same email as this account."
          }
        >
          {user.githubConnected ? (
            <Button variant="outline" disabled={busy} onClick={() => disconnect(disconnectGithub, "GitHub")}>
              Disconnect
            </Button>
          ) : (
            <a
              href={`${API_URL}/oauth2/authorization/github`}
              onClick={() => sessionStorage.setItem("connectingGithub", "1")}
              className={buttonVariants({ variant: "outline" })}
            >
              Connect
            </a>
          )}
        </AppRow>

        <AppRow
          logo="/trello.svg"
          name="Trello"
          connected={user.trelloConnected}
          text={
            user.trelloConnected
              ? "Your tutor can create cards and checklists when you ask"
              : "You need a Trello workspace. New to Trello? Create one at trello.com first."
          }
        >
          {user.trelloConnected ? (
            <Button variant="outline" disabled={busy} onClick={() => disconnect(disconnectTrello, "Trello")}>
              Disconnect
            </Button>
          ) : (
            <Button variant="outline" disabled={busy} onClick={connectTrello}>
              Connect
            </Button>
          )}
        </AppRow>
      </div>

      <p className="mt-4 rounded-xl border bg-secondary px-4 py-3 text-[0.8125rem] lg:text-sm text-muted-foreground">
        Your tutor never changes your Trello board on its own. Each change waits in the chat until you approve it.
      </p>
    </div>
  );
}

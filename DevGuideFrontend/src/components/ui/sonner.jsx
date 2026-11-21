import { CircleAlert, CircleCheck, Info, LoaderCircle, TriangleAlert } from "lucide-react";
import { Toaster as Sonner } from "sonner";

const icons = {
  success: <CircleCheck className="size-4 text-success" />,
  error: <CircleAlert className="size-4 text-destructive" />,
  warning: <TriangleAlert className="size-4 text-warning" />,
  info: <Info className="size-4 text-muted-foreground" />,
  loading: <LoaderCircle className="size-4 animate-spin text-muted-foreground" />,
};

const style = {
  background: "var(--popover)",
  color: "var(--popover-foreground)",
  border: "1px solid var(--border)",
  borderRadius: "0.75rem",
  fontSize: "0.875rem",
};

export function Toaster() {
  return <Sonner theme="dark" position="bottom-right" icons={icons} toastOptions={{ style }} />;
}

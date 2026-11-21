import { AlertDialog } from "@base-ui/react/alert-dialog";
import { Button, buttonVariants } from "./button";

export function ConfirmDialog({ open, onOpenChange, title, description, confirmLabel = "Delete", danger = true, onConfirm }) {
  const confirm = () => {
    onOpenChange(false);
    onConfirm();
  };

  return (
    <AlertDialog.Root open={open} onOpenChange={onOpenChange}>
      <AlertDialog.Portal>
        <AlertDialog.Backdrop className="fixed inset-0 z-50 bg-black/60" />
        <AlertDialog.Popup className="fixed top-1/2 left-1/2 z-50 w-[calc(100%-2rem)] max-w-110 -translate-x-1/2 -translate-y-1/2 rounded-xl border bg-secondary shadow-2xl">
          <AlertDialog.Title className="px-5 pt-5 pb-2 text-base font-medium [overflow-wrap:anywhere]">
            {title}
          </AlertDialog.Title>
          <AlertDialog.Description className="px-5 pb-4 text-[0.8125rem] lg:text-sm text-muted-foreground [overflow-wrap:anywhere]">
            {description}
          </AlertDialog.Description>
          <footer className="flex justify-end gap-2 border-t px-5 py-4">
            <AlertDialog.Close className={buttonVariants({ variant: "ghost" })}>Cancel</AlertDialog.Close>
            <Button onClick={confirm} className={danger ? "bg-destructive text-white hover:bg-destructive/90" : ""}>
              {confirmLabel}
            </Button>
          </footer>
        </AlertDialog.Popup>
      </AlertDialog.Portal>
    </AlertDialog.Root>
  );
}

import { useNavigate } from "react-router-dom";

import { useAuth } from "@/context/AuthContext";
import { Button } from "@/components/ui/Button";

export function Header() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate("/login");
  }

  return (
    <header className="flex h-16 items-center justify-between border-b border-slate-200 bg-white px-4 md:px-6">
      <div className="md:hidden text-sm font-semibold text-slate-900">CashPilot</div>
      <div className="ml-auto flex items-center gap-3">
        <div className="hidden text-right sm:block">
          <p className="text-sm font-medium text-slate-900">{user?.name}</p>
          <p className="text-xs text-slate-500">{user?.email}</p>
        </div>
        <Button variant="secondary" onClick={handleLogout}>
          Sair
        </Button>
      </div>
    </header>
  );
}

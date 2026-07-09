import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";

import {
  useGenerateNotifications,
  useMarkAsRead,
  useNotifications,
  useUnreadCount,
} from "@/hooks/useNotifications";
import { formatDateTime } from "@/utils/format";
import { Button } from "@/components/ui/Button";
import { Spinner } from "@/components/ui/Spinner";

export function NotificationBell() {
  const [isOpen, setIsOpen] = useState(false);
  const wrapperRef = useRef<HTMLDivElement>(null);
  const navigate = useNavigate();

  const { data: unreadCount } = useUnreadCount();
  const { data: recent, isLoading } = useNotifications(undefined, 0, 5);
  const markAsRead = useMarkAsRead();
  const generateNotifications = useGenerateNotifications();

  useEffect(() => {
    if (!isOpen) return;
    function handleClickOutside(event: MouseEvent) {
      if (wrapperRef.current && !wrapperRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [isOpen]);

  return (
    <div className="relative" ref={wrapperRef}>
      <button
        type="button"
        aria-label="Notificações"
        onClick={() => setIsOpen((open) => !open)}
        className="relative flex h-9 w-9 items-center justify-center rounded-md text-slate-500 transition-colors hover:bg-slate-100 hover:text-slate-700"
      >
        <svg viewBox="0 0 24 24" fill="none" className="h-5 w-5" stroke="currentColor" strokeWidth={2}>
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            d="M15 17h5l-1.4-1.4A2 2 0 0 1 18 14.2V11a6 6 0 0 0-4-5.7V5a2 2 0 1 0-4 0v.3A6 6 0 0 0 6 11v3.2a2 2 0 0 1-.6 1.4L4 17h5m6 0v1a3 3 0 1 1-6 0v-1m6 0H9"
          />
        </svg>
        {!!unreadCount && unreadCount > 0 && (
          <span className="absolute -right-1 -top-1 flex h-4 min-w-[1rem] items-center justify-center rounded-full bg-red-600 px-1 text-[10px] font-semibold text-white">
            {unreadCount > 99 ? "99+" : unreadCount}
          </span>
        )}
      </button>

      {isOpen && (
        <div className="absolute right-0 z-20 mt-2 w-80 rounded-lg border border-slate-200 bg-white shadow-lg">
          <div className="flex items-center justify-between border-b border-slate-100 px-4 py-3">
            <span className="text-sm font-semibold text-slate-900">Notificações</span>
            <Button
              variant="ghost"
              onClick={() => generateNotifications.mutate()}
              isLoading={generateNotifications.isPending}
            >
              Gerar agora
            </Button>
          </div>

          <div className="max-h-80 overflow-y-auto">
            {isLoading && (
              <div className="flex justify-center p-4">
                <Spinner />
              </div>
            )}
            {!isLoading && recent?.content.length === 0 && (
              <p className="px-4 py-6 text-center text-sm text-slate-500">Nenhuma notificação.</p>
            )}
            {!isLoading &&
              recent?.content.map((notification) => (
                <div
                  key={notification.id}
                  className={`flex flex-col gap-1 border-b border-slate-50 px-4 py-3 text-sm last:border-0 ${
                    notification.lida ? "text-slate-500" : "bg-brand-50/40 text-slate-800"
                  }`}
                >
                  <span>{notification.mensagem}</span>
                  <div className="flex items-center justify-between">
                    <span className="text-xs text-slate-400">{formatDateTime(notification.createdAt)}</span>
                    {!notification.lida && (
                      <button
                        type="button"
                        className="text-xs font-medium text-brand-600 hover:underline"
                        onClick={() => markAsRead.mutate(notification.id)}
                      >
                        Marcar como lida
                      </button>
                    )}
                  </div>
                </div>
              ))}
          </div>

          <div className="border-t border-slate-100 px-4 py-2">
            <button
              type="button"
              className="text-sm font-medium text-brand-600 hover:underline"
              onClick={() => {
                setIsOpen(false);
                navigate("/notificacoes");
              }}
            >
              Ver todas
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

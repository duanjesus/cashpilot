import type { ReactNode } from "react";

type Tone = "green" | "red" | "slate" | "blue" | "amber";

const TONE_CLASSES: Record<Tone, string> = {
  green: "bg-green-100 text-green-800",
  red: "bg-red-100 text-red-800",
  slate: "bg-slate-100 text-slate-700",
  blue: "bg-blue-100 text-blue-800",
  amber: "bg-amber-100 text-amber-800",
};

export function Badge({ tone = "slate", children }: { tone?: Tone; children: ReactNode }) {
  return (
    <span
      className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${TONE_CLASSES[tone]}`}
    >
      {children}
    </span>
  );
}

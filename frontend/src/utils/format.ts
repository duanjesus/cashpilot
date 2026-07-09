export function formatDate(isoDate: string): string {
  const [year, month, day] = isoDate.split("-");
  if (!year || !month || !day) return isoDate;
  return `${day}/${month}/${year}`;
}

export function formatCurrency(value: number): string {
  return new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(value);
}

export function formatPercent(value: number): string {
  return `${Math.round(value)}%`;
}

export function todayIsoDate(): string {
  return new Date().toISOString().slice(0, 10);
}

export function isInCurrentMonth(isoDate: string): boolean {
  const currentPrefix = todayIsoDate().slice(0, 7);
  return isoDate.startsWith(currentPrefix);
}

export function currentMonthRange(): { dataInicio: string; dataFim: string } {
  const now = new Date();
  const first = new Date(now.getFullYear(), now.getMonth(), 1);
  const last = new Date(now.getFullYear(), now.getMonth() + 1, 0);
  return { dataInicio: first.toISOString().slice(0, 10), dataFim: last.toISOString().slice(0, 10) };
}

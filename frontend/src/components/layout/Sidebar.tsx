import { NavLink } from "react-router-dom";

const NAV_ITEMS: { to: string; label: string; end?: boolean }[] = [
  { to: "/", label: "Visão geral", end: true },
  { to: "/contas", label: "Contas" },
  { to: "/cartoes", label: "Cartões" },
  { to: "/receitas", label: "Receitas" },
  { to: "/despesas", label: "Despesas" },
  { to: "/parcelamentos", label: "Parcelamentos" },
  { to: "/assinaturas", label: "Assinaturas" },
  { to: "/contas-a-pagar", label: "Contas a pagar" },
  { to: "/contas-a-receber", label: "Contas a receber" },
  { to: "/fluxo-caixa", label: "Fluxo de caixa" },
  { to: "/transferencias", label: "Transferências" },
  { to: "/metas", label: "Metas" },
  { to: "/categorias", label: "Categorias" },
  { to: "/projecao", label: "Projeção" },
  { to: "/relatorios", label: "Gráficos" },
  { to: "/previsao-saldo", label: "Previsão de saldo" },
  { to: "/simulacao", label: "Simulação" },
];

export function Sidebar() {
  return (
    <aside className="hidden w-60 shrink-0 border-r border-slate-200 bg-white md:block">
      <div className="flex h-16 items-center gap-2 border-b border-slate-200 px-5">
        <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-brand-600 text-sm font-bold text-white">
          CP
        </span>
        <span className="text-sm font-semibold text-slate-900">CashPilot</span>
      </div>
      <nav className="flex flex-col gap-1 p-3">
        {NAV_ITEMS.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.end}
            className={({ isActive }) =>
              `rounded-md px-3 py-2 text-sm font-medium transition-colors ${
                isActive
                  ? "bg-brand-50 text-brand-700"
                  : "text-slate-600 hover:bg-slate-100 hover:text-slate-900"
              }`
            }
          >
            {item.label}
          </NavLink>
        ))}
      </nav>
    </aside>
  );
}

import { Navigate, Route, Routes } from "react-router-dom";

import { ProtectedRoute } from "@/components/ProtectedRoute";
import { AppLayout } from "@/components/layout/AppLayout";
import { LoginPage } from "@/pages/auth/LoginPage";
import { RegisterPage } from "@/pages/auth/RegisterPage";
import { DashboardPage } from "@/pages/dashboard/DashboardPage";
import { CategoriasListPage } from "@/pages/categorias/CategoriasListPage";
import { ContasListPage } from "@/pages/contas/ContasListPage";
import { CartoesListPage } from "@/pages/cartoes/CartoesListPage";
import { ReceitasListPage } from "@/pages/receitas/ReceitasListPage";
import { DespesasListPage } from "@/pages/despesas/DespesasListPage";
import { TransferenciasListPage } from "@/pages/transferencias/TransferenciasListPage";
import { MetasListPage } from "@/pages/metas/MetasListPage";
import { ProjectionPage } from "@/pages/projecao/ProjectionPage";
import { ParcelamentosListPage } from "@/pages/parcelamentos/ParcelamentosListPage";
import { AssinaturasListPage } from "@/pages/assinaturas/AssinaturasListPage";
import { FluxoCaixaPage } from "@/pages/fluxo-caixa/FluxoCaixaPage";
import { ContasAPagarPage } from "@/pages/contas-a-pagar/ContasAPagarPage";
import { ContasAReceberPage } from "@/pages/contas-a-receber/ContasAReceberPage";
import { RelatoriosPage } from "@/pages/relatorios/RelatoriosPage";
import { PrevisaoSaldoPage } from "@/pages/previsao-saldo/PrevisaoSaldoPage";
import { SimulacaoPage } from "@/pages/simulacao/SimulacaoPage";

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />

      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route path="/" element={<DashboardPage />} />
          <Route path="/categorias" element={<CategoriasListPage />} />
          <Route path="/contas" element={<ContasListPage />} />
          <Route path="/cartoes" element={<CartoesListPage />} />
          <Route path="/receitas" element={<ReceitasListPage />} />
          <Route path="/despesas" element={<DespesasListPage />} />
          <Route path="/transferencias" element={<TransferenciasListPage />} />
          <Route path="/parcelamentos" element={<ParcelamentosListPage />} />
          <Route path="/assinaturas" element={<AssinaturasListPage />} />
          <Route path="/fluxo-caixa" element={<FluxoCaixaPage />} />
          <Route path="/contas-a-pagar" element={<ContasAPagarPage />} />
          <Route path="/contas-a-receber" element={<ContasAReceberPage />} />
          <Route path="/metas" element={<MetasListPage />} />
          <Route path="/projecao" element={<ProjectionPage />} />
          <Route path="/relatorios" element={<RelatoriosPage />} />
          <Route path="/previsao-saldo" element={<PrevisaoSaldoPage />} />
          <Route path="/simulacao" element={<SimulacaoPage />} />
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

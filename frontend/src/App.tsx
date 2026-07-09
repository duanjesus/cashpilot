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
          <Route path="/metas" element={<MetasListPage />} />
          <Route path="/projecao" element={<ProjectionPage />} />
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

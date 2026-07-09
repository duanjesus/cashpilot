export type CategoryType = "RECEITA" | "DESPESA" | "AMBOS";

export const CATEGORY_TYPE_LABELS: Record<CategoryType, string> = {
  RECEITA: "Receita",
  DESPESA: "Despesa",
  AMBOS: "Ambos",
};

export interface Category {
  id: number;
  nome: string;
  tipo: CategoryType;
  isSystem: boolean;
  isInvestment: boolean;
  cor: string | null;
}

export interface CategoryRequest {
  nome: string;
  tipo: CategoryType;
  cor?: string;
}

import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";

import { useAuth } from "@/context/AuthContext";
import { extractErrorMessage } from "@/lib/api";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { ErrorBanner } from "@/components/ui/ErrorBanner";

const schema = z.object({
  email: z.string().min(1, "Email é obrigatório").email("Email deve ser válido"),
  password: z.string().min(1, "Senha é obrigatória"),
});

type FormValues = z.infer<typeof schema>;

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [serverError, setServerError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({ resolver: zodResolver(schema) });

  async function onSubmit(values: FormValues) {
    setServerError(null);
    try {
      await login(values);
      navigate("/", { replace: true });
    } catch (error) {
      setServerError(extractErrorMessage(error));
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-50 px-4">
      <div className="w-full max-w-sm rounded-lg border border-slate-200 bg-white p-8 shadow-sm">
        <div className="mb-6 text-center">
          <span className="mx-auto mb-3 flex h-10 w-10 items-center justify-center rounded-lg bg-brand-600 text-sm font-bold text-white">
            CP
          </span>
          <h1 className="text-lg font-semibold text-slate-900">CashPilot</h1>
          <p className="text-sm text-slate-500">Entre com sua conta para continuar</p>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
          <ErrorBanner message={serverError} />
          <Input
            label="Email"
            type="email"
            autoComplete="email"
            placeholder="ex: ana@email.com"
            error={errors.email?.message}
            {...register("email")}
          />
          <Input
            label="Senha"
            type="password"
            autoComplete="current-password"
            placeholder="Sua senha"
            error={errors.password?.message}
            {...register("password")}
          />
          <Button type="submit" isLoading={isSubmitting} className="mt-2 w-full">
            Entrar
          </Button>
        </form>

        <p className="mt-6 text-center text-sm text-slate-500">
          Não tem uma conta?{" "}
          <Link to="/register" className="font-medium text-brand-600 hover:text-brand-700">
            Cadastre-se
          </Link>
        </p>
      </div>
    </div>
  );
}

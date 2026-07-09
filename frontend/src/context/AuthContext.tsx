import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from "react";

import { api, TOKEN_STORAGE_KEY, USER_STORAGE_KEY } from "@/lib/api";
import type { AuthResponse, AuthUser, LoginRequest, RegisterRequest } from "@/types/auth";

interface AuthContextValue {
  user: AuthUser | null;
  isAuthenticated: boolean;
  login: (payload: LoginRequest) => Promise<void>;
  register: (payload: RegisterRequest) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

function readStoredUser(): AuthUser | null {
  const raw = localStorage.getItem(USER_STORAGE_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw) as AuthUser;
  } catch {
    return null;
  }
}

function persistSession(auth: AuthResponse) {
  localStorage.setItem(TOKEN_STORAGE_KEY, auth.token);
  const user: AuthUser = { name: auth.name, email: auth.email };
  localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(user));
  return user;
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(() => readStoredUser());

  const login = useCallback(async (payload: LoginRequest) => {
    const { data } = await api.post<AuthResponse>("/auth/login", payload);
    setUser(persistSession(data));
  }, []);

  const register = useCallback(async (payload: RegisterRequest) => {
    const { data } = await api.post<AuthResponse>("/auth/register", payload);
    setUser(persistSession(data));
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem(TOKEN_STORAGE_KEY);
    localStorage.removeItem(USER_STORAGE_KEY);
    setUser(null);
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      isAuthenticated: user !== null,
      login,
      register,
      logout,
    }),
    [user, login, register, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return ctx;
}

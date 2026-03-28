import React, { createContext, useContext, useState, useEffect } from 'react';
import type { ReactNode } from 'react';
import { login as apiLogin, logout as apiLogout } from '../api/auth';

interface AuthUser {
  nome: string;
  email: string;
  perfil: 'ADMIN' | 'USUARIO';
}

interface AuthContextData {
  user: AuthUser | null;
  token: string | null;
  isAuthenticated: boolean;
  isAdmin: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextData>({} as AuthContextData);

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [token, setToken] = useState<string | null>(null);

  useEffect(() => {
    const storedToken = localStorage.getItem('token');
    const storedUser = localStorage.getItem('user');
    if (storedToken && storedUser) {
      try {
        setToken(storedToken);
        setUser(JSON.parse(storedUser));
      } catch {
        localStorage.removeItem('token');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('user');
      }
    }
  }, []);

  const login = async (email: string, password: string): Promise<void> => {
    const response = await apiLogin(email, password);
    const authUser: AuthUser = {
      nome: response.nome,
      email,
      perfil: response.perfil,
    };
    localStorage.setItem('token', response.token);
    localStorage.setItem('refreshToken', response.refreshToken);
    localStorage.setItem('user', JSON.stringify(authUser));
    setToken(response.token);
    setUser(authUser);
  };

  const logout = (): void => {
    apiLogout();
    setToken(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!token && !!user,
        isAdmin: user?.perfil === 'ADMIN',
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextData => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export default AuthContext;

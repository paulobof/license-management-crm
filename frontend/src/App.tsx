import React, { Suspense, lazy } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import ProtectedRoute from './components/ProtectedRoute';
import Layout from './components/Layout';

const Login = lazy(() => import('./pages/Login'));
const Dashboard = lazy(() => import('./pages/Dashboard'));
const ClienteList = lazy(() => import('./pages/clientes/ClienteList'));
const ClienteForm = lazy(() => import('./pages/clientes/ClienteForm'));
const ClienteDetail = lazy(() => import('./pages/clientes/ClienteDetail'));
const DocumentoList = lazy(() => import('./pages/documentos/DocumentoList'));
const DocumentoForm = lazy(() => import('./pages/documentos/DocumentoForm'));
const UsuarioList = lazy(() => import('./pages/usuarios/UsuarioList'));
const ContratoList = lazy(() => import('./pages/contratos/ContratoList'));
const ContratoForm = lazy(() => import('./pages/contratos/ContratoForm'));
const ContratoDetail = lazy(() => import('./pages/contratos/ContratoDetail'));
const FinanceiroPage = lazy(() => import('./pages/financeiro/FinanceiroPage'));
const AlertaList = lazy(() => import('./pages/alertas/AlertaList'));
const AlertaConfig = lazy(() => import('./pages/alertas/AlertaConfig'));

const PageLoader: React.FC = () => (
  <div className="flex items-center justify-center min-h-screen bg-gray-50">
    <div className="animate-spin rounded-full h-10 w-10 border-2 border-red-600 border-t-transparent" />
  </div>
);

const App: React.FC = () => {
  return (
    <Suspense fallback={<PageLoader />}>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route
          element={
            <ProtectedRoute>
              <Layout />
            </ProtectedRoute>
          }
        >
          <Route path="/" element={<Dashboard />} />
          <Route path="/clientes" element={<ClienteList />} />
          <Route path="/clientes/novo" element={<ClienteForm />} />
          <Route path="/clientes/:id" element={<ClienteDetail />} />
          <Route path="/clientes/:id/editar" element={<ClienteForm />} />
          <Route path="/documentos" element={<DocumentoList />} />
          <Route path="/documentos/novo" element={<DocumentoForm />} />
          <Route path="/documentos/:id/editar" element={<DocumentoForm />} />
          <Route path="/contratos" element={<ContratoList />} />
          <Route path="/contratos/novo" element={<ContratoForm />} />
          <Route path="/contratos/:id" element={<ContratoDetail />} />
          <Route path="/contratos/:id/editar" element={<ContratoForm />} />
          <Route path="/financeiro" element={<FinanceiroPage />} />
          <Route path="/alertas" element={<AlertaList />} />
          <Route
            path="/alertas/config"
            element={
              <ProtectedRoute requireAdmin>
                <AlertaConfig />
              </ProtectedRoute>
            }
          />
          <Route
            path="/usuarios"
            element={
              <ProtectedRoute requireAdmin>
                <UsuarioList />
              </ProtectedRoute>
            }
          />
        </Route>
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Suspense>
  );
};

export default App;

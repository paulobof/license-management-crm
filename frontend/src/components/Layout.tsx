import React, { useState } from 'react';
import { Outlet } from 'react-router-dom';
import { Menu, Bell, LogOut, ChevronDown } from 'lucide-react';
import Sidebar from './Sidebar';
import { useAuth } from '../contexts/AuthContext';

const Layout: React.FC = () => {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [userMenuOpen, setUserMenuOpen] = useState(false);
  const { user, logout } = useAuth();

  const handleLogout = () => {
    logout();
    window.location.href = '/login';
  };

  return (
    <div className="flex h-screen bg-gray-50 overflow-hidden">
      <Sidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} />

      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
        <header className="h-14 bg-white border-b border-gray-200 flex items-center justify-between px-4 lg:px-6 shrink-0">
          <div className="flex items-center gap-3">
            <button
              onClick={() => setSidebarOpen(true)}
              className="lg:hidden text-gray-500 hover:text-gray-800 p-2 rounded-md hover:bg-gray-100 transition-colors cursor-pointer"
              aria-label="Abrir menu"
            >
              <Menu size={20} />
            </button>
            <span className="text-gray-800 font-semibold text-sm hidden lg:block">
              Prediman CRM
            </span>
          </div>

          <div className="flex items-center gap-2">
            <button
              className="text-gray-500 hover:text-gray-800 p-2 rounded-md hover:bg-gray-100 transition-colors relative cursor-pointer"
              aria-label="Notificacoes"
            >
              <Bell size={18} />
            </button>

            <div className="relative">
              <button
                onClick={() => setUserMenuOpen((v) => !v)}
                className="flex items-center gap-2 px-3 py-1.5 rounded-lg hover:bg-gray-100 transition-colors cursor-pointer"
              >
                <div className="w-7 h-7 rounded-full bg-red-600 flex items-center justify-center shrink-0">
                  <span className="text-white text-xs font-semibold">
                    {user?.nome?.charAt(0)?.toUpperCase() ?? 'U'}
                  </span>
                </div>
                <span className="text-gray-700 text-sm hidden sm:block max-w-32 truncate">
                  {user?.nome ?? 'Usuario'}
                </span>
                <ChevronDown size={14} className="text-gray-400 hidden sm:block" />
              </button>

              {userMenuOpen && (
                <>
                  <div
                    className="fixed inset-0 z-10"
                    onClick={() => setUserMenuOpen(false)}
                    aria-hidden="true"
                  />
                  <div className="absolute right-0 top-full mt-1 w-48 bg-white border border-gray-200 rounded-lg shadow-lg z-20 overflow-hidden">
                    <div className="px-4 py-3 border-b border-gray-200">
                      <p className="text-xs text-gray-400">Conectado como</p>
                      <p className="text-sm text-gray-800 font-medium truncate">{user?.nome}</p>
                      <p className="text-xs text-gray-500 truncate">{user?.email}</p>
                    </div>
                    <button
                      onClick={handleLogout}
                      className="w-full flex items-center gap-2 px-4 py-2.5 text-sm text-red-600 hover:bg-gray-50 transition-colors cursor-pointer"
                    >
                      <LogOut size={16} />
                      Sair
                    </button>
                  </div>
                </>
              )}
            </div>
          </div>
        </header>

        <main className="flex-1 overflow-y-auto bg-gray-50 p-4 lg:p-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
};

export default Layout;

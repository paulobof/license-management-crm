import React from 'react';
import { NavLink } from 'react-router-dom';
import { LayoutDashboard, Users, FileText, UserCog, FileSignature, DollarSign, Bell } from 'lucide-react';
import { useAuth } from '../contexts/AuthContext';

interface SidebarProps {
  open: boolean;
  onClose: () => void;
}

interface NavItem {
  to: string;
  label: string;
  icon: React.ReactNode;
  adminOnly?: boolean;
}

const navItems: NavItem[] = [
  {
    to: '/',
    label: 'Dashboard',
    icon: <LayoutDashboard size={18} />,
  },
  {
    to: '/clientes',
    label: 'Clientes',
    icon: <Users size={18} />,
  },
  {
    to: '/documentos',
    label: 'Documentos',
    icon: <FileText size={18} />,
  },
  {
    to: '/contratos',
    label: 'Contratos',
    icon: <FileSignature size={18} />,
  },
  {
    to: '/financeiro',
    label: 'Financeiro',
    icon: <DollarSign size={18} />,
  },
  {
    to: '/alertas',
    label: 'Alertas',
    icon: <Bell size={18} />,
  },
  {
    to: '/usuarios',
    label: 'Usuarios',
    icon: <UserCog size={18} />,
    adminOnly: true,
  },
];

const Sidebar: React.FC<SidebarProps> = ({ open, onClose }) => {
  const { isAdmin } = useAuth();

  const visibleItems = navItems.filter((item) => !item.adminOnly || isAdmin);

  return (
    <>
      {open && (
        <div
          className="fixed inset-0 z-20 bg-black/60 lg:hidden"
          onClick={onClose}
          aria-hidden="true"
        />
      )}
      <aside
        className={[
          'fixed top-0 left-0 h-full z-30 w-64 bg-white border-r border-gray-200 flex flex-col transition-transform duration-300 ease-in-out',
          open ? 'translate-x-0' : '-translate-x-full',
          'lg:translate-x-0 lg:static lg:z-auto',
        ].join(' ')}
      >
        <div className="bg-gradient-to-r from-red-700 to-red-600 px-5 py-5">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-white rounded-lg flex items-center justify-center shadow-sm">
              <img src="/logo.jpg" alt="Prediman" className="h-7 w-7 object-contain" />
            </div>
            <div>
              <span className="text-white font-bold text-sm tracking-wide">Prediman</span>
              <p className="text-red-200 text-xs">Sistema CRM</p>
            </div>
          </div>
        </div>

        <nav className="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
          {visibleItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === '/'}
              onClick={onClose}
              className={({ isActive }) =>
                [
                  'flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all duration-150',
                  isActive
                    ? 'bg-red-600 text-white shadow-md shadow-red-600/20'
                    : 'text-gray-600 hover:text-red-700 hover:bg-red-50',
                ].join(' ')
              }
            >
              {item.icon}
              {item.label}
            </NavLink>
          ))}
        </nav>

        <div className="px-3 py-4 border-t border-gray-200">
          <p className="text-xs text-gray-400 text-center">v1.0.0</p>
        </div>
      </aside>
    </>
  );
};

export default Sidebar;

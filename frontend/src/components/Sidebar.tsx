import React from 'react';
import { NavLink } from 'react-router-dom';
import { LayoutDashboard, Users, UserCog } from 'lucide-react';
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
          'fixed top-0 left-0 h-full z-30 w-64 bg-zinc-950 border-r border-zinc-800 flex flex-col transition-transform duration-300 ease-in-out',
          open ? 'translate-x-0' : '-translate-x-full',
          'lg:translate-x-0 lg:static lg:z-auto',
        ].join(' ')}
      >
        <div className="flex items-center gap-3 px-6 py-5 border-b border-zinc-800">
          <div className="w-8 h-8 rounded-lg bg-blue-600 flex items-center justify-center shrink-0">
            <span className="text-white font-bold text-sm">P</span>
          </div>
          <span className="text-zinc-100 font-semibold text-sm">Prediman CRM</span>
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
                  'flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors duration-150',
                  isActive
                    ? 'bg-blue-600/20 text-blue-400 border border-blue-600/30'
                    : 'text-zinc-400 hover:text-zinc-100 hover:bg-zinc-800',
                ].join(' ')
              }
            >
              {item.icon}
              {item.label}
            </NavLink>
          ))}
        </nav>
        <div className="px-3 py-4 border-t border-zinc-800">
          <p className="text-xs text-zinc-600 text-center">v1.0.0</p>
        </div>
      </aside>
    </>
  );
};

export default Sidebar;

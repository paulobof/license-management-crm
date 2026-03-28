import React from 'react';
import type { InputHTMLAttributes, SelectHTMLAttributes, ReactNode } from 'react';

interface BaseInputProps {
  label?: string;
  error?: string;
  className?: string;
}

interface TextInputProps extends BaseInputProps, InputHTMLAttributes<HTMLInputElement> {
  type?: 'text' | 'email' | 'password' | 'date' | 'number' | 'tel';
  as?: 'input';
}

interface SelectInputProps extends BaseInputProps, SelectHTMLAttributes<HTMLSelectElement> {
  as: 'select';
  children: ReactNode;
}

type InputProps = TextInputProps | SelectInputProps;

const inputBaseClasses =
  'w-full bg-zinc-800 border border-zinc-700 rounded-md px-3 py-2 text-zinc-100 placeholder-zinc-500 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors duration-150';

const Input: React.FC<InputProps> = (props) => {
  const { label, error, className = '' } = props;

  const wrapperClass = className;

  if (props.as === 'select') {
    const { label: _label, error: _error, className: _className, as: _as, children, ...rest } = props;
    return (
      <div className={wrapperClass}>
        {label && (
          <label className="block text-sm font-medium text-zinc-300 mb-1">{label}</label>
        )}
        <select
          {...rest}
          className={[inputBaseClasses, error ? 'border-red-500 focus:ring-red-500' : ''].join(' ')}
        >
          {children}
        </select>
        {error && <p className="mt-1 text-xs text-red-400">{error}</p>}
      </div>
    );
  }

  const { label: _label, error: _error, className: _className, as: _as, ...rest } = props as TextInputProps;

  return (
    <div className={wrapperClass}>
      {label && (
        <label className="block text-sm font-medium text-zinc-300 mb-1">{label}</label>
      )}
      <input
        {...rest}
        className={[inputBaseClasses, error ? 'border-red-500 focus:ring-red-500' : ''].join(' ')}
      />
      {error && <p className="mt-1 text-xs text-red-400">{error}</p>}
    </div>
  );
};

export default Input;

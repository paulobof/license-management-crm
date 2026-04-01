import React, { useId } from 'react';
import type { InputHTMLAttributes, SelectHTMLAttributes, ReactNode } from 'react';

interface BaseInputProps {
  label?: string;
  error?: string;
  className?: string;
}

interface TextInputProps extends BaseInputProps, InputHTMLAttributes<HTMLInputElement> {
  type?: 'text' | 'email' | 'password' | 'date' | 'number' | 'tel' | 'time';
  as?: 'input';
}

interface SelectInputProps extends BaseInputProps, SelectHTMLAttributes<HTMLSelectElement> {
  as: 'select';
  children: ReactNode;
}

type InputProps = TextInputProps | SelectInputProps;

const inputBaseClasses =
  'w-full bg-white border border-gray-300 rounded-md px-3 py-2 text-gray-900 placeholder-gray-400 text-sm focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-red-500 transition-colors duration-150';

const Input: React.FC<InputProps> = (props) => {
  const { label, error, className = '' } = props;
  const generatedId = useId();
  const inputId = props.id ?? generatedId;
  const errorId = error ? `${inputId}-error` : undefined;

  const wrapperClass = className;

  if (props.as === 'select') {
    const { label: _label, error: _error, className: _className, as: _as, children, id: _id, ...rest } = props;
    return (
      <div className={wrapperClass}>
        {label && (
          <label htmlFor={inputId} className="block text-sm font-medium text-gray-700 mb-1">{label}</label>
        )}
        <select
          {...rest}
          id={inputId}
          aria-invalid={error ? true : undefined}
          aria-describedby={errorId}
          className={[inputBaseClasses, error ? 'border-red-500 focus:ring-red-500' : ''].join(' ')}
        >
          {children}
        </select>
        {error && <p id={errorId} className="mt-1 text-xs text-red-600">{error}</p>}
      </div>
    );
  }

  const { label: _label, error: _error, className: _className, as: _as, id: _id, ...rest } = props as TextInputProps;

  return (
    <div className={wrapperClass}>
      {label && (
        <label htmlFor={inputId} className="block text-sm font-medium text-gray-700 mb-1">{label}</label>
      )}
      <input
        {...rest}
        id={inputId}
        aria-invalid={error ? true : undefined}
        aria-describedby={errorId}
        className={[inputBaseClasses, error ? 'border-red-500 focus:ring-red-500' : ''].join(' ')}
      />
      {error && <p id={errorId} className="mt-1 text-xs text-red-600">{error}</p>}
    </div>
  );
};

export default Input;

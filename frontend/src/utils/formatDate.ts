/**
 * Formata uma data ISO (yyyy-MM-dd ou yyyy-MM-ddTHH:mm:ss) para o padrao brasileiro dd/MM/yyyy.
 * Retorna '—' para valores nulos ou vazios.
 */
export const formatDate = (value: string | null | undefined): string => {
  if (!value) return '—';
  // Strip any ISO datetime suffix (e.g. "2024-01-15T10:30:00")
  const dateOnly = value.includes('T') ? value.split('T')[0] : value;
  const [year, month, day] = dateOnly.split('-');
  if (!year || !month || !day) return value;
  return `${day}/${month}/${year}`;
};

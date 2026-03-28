import type { ReactNode } from 'react';

interface TableColumn<T> {
  key: string;
  header: string;
  render?: (row: T) => ReactNode;
  className?: string;
}

interface TableProps<T> {
  columns: TableColumn<T>[];
  data: T[];
  keyExtractor: (row: T) => string | number;
  loading?: boolean;
  emptyMessage?: string;
}

function TableSkeleton({ cols }: { cols: number }) {
  return (
    <>
      {Array.from({ length: 5 }).map((_, rowIdx) => (
        <tr key={rowIdx} className="border-b border-zinc-800">
          {Array.from({ length: cols }).map((__, colIdx) => (
            <td key={colIdx} className="px-4 py-3">
              <div className="h-4 bg-zinc-800 rounded animate-pulse" />
            </td>
          ))}
        </tr>
      ))}
    </>
  );
}

function Table<T>({
  columns,
  data,
  keyExtractor,
  loading = false,
  emptyMessage = 'Nenhum registro encontrado.',
}: TableProps<T>) {
  return (
    <div className="w-full overflow-x-auto rounded-xl border border-zinc-700">
      <table className="w-full text-sm text-left">
        <thead>
          <tr className="bg-zinc-800 border-b border-zinc-700">
            {columns.map((col) => (
              <th
                key={col.key}
                className={['px-4 py-3 text-xs font-semibold text-zinc-400 uppercase tracking-wider', col.className ?? ''].join(' ')}
              >
                {col.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="bg-zinc-900 divide-y divide-zinc-800">
          {loading ? (
            <TableSkeleton cols={columns.length} />
          ) : data.length === 0 ? (
            <tr>
              <td colSpan={columns.length} className="px-4 py-12 text-center text-zinc-500">
                {emptyMessage}
              </td>
            </tr>
          ) : (
            data.map((row) => (
              <tr
                key={keyExtractor(row)}
                className="hover:bg-zinc-800/50 transition-colors"
              >
                {columns.map((col) => (
                  <td key={col.key} className={['px-4 py-3 text-zinc-300', col.className ?? ''].join(' ')}>
                    {col.render
                      ? col.render(row)
                      : (row as Record<string, unknown>)[col.key] as ReactNode}
                  </td>
                ))}
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}

export default Table;

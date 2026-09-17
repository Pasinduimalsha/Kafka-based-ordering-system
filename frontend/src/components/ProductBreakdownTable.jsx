import React from 'react';
import { Layers } from 'lucide-react';

export default function ProductBreakdownTable({ perProduct }) {
  const products = Object.entries(perProduct || {}).sort((a, b) => b[1].totalRevenue - a[1].totalRevenue);

  return (
    <div className="card">
      <div className="card-header">
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <Layers size={16} color="var(--brand-primary)" />
          <h3 style={{ fontSize: '0.9375rem', fontWeight: 600 }}>Product Real-Time Aggregation Metrics</h3>
        </div>
        <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
          {products.length} distinct products tracked
        </span>
      </div>

      <div style={{ overflowX: 'auto' }}>
        <table className="data-table">
          <thead>
            <tr>
              <th>Product Name</th>
              <th style={{ textAlign: 'center', width: '130px' }}>Processed Orders</th>
              <th style={{ textAlign: 'right', width: '150px' }}>Running Average</th>
              <th style={{ textAlign: 'right', width: '120px' }}>Min Price</th>
              <th style={{ textAlign: 'right', width: '120px' }}>Max Price</th>
              <th style={{ textAlign: 'right', width: '160px' }}>Total Revenue</th>
            </tr>
          </thead>
          <tbody>
            {products.length === 0 ? (
              <tr>
                <td colSpan="6" style={{ padding: '32px 16px', textAlign: 'center', color: 'var(--text-dim)' }}>
                  No product transactions recorded yet.
                </td>
              </tr>
            ) : (
              products.map(([name, stat], idx) => (
                <tr key={idx}>
                  <td style={{ fontWeight: 500 }}>{name}</td>
                  <td style={{ textAlign: 'center' }}>
                    <span className="badge badge-neutral font-mono">
                      {stat.count}
                    </span>
                  </td>
                  <td className="font-mono" style={{ textAlign: 'right', fontWeight: 600, color: 'var(--brand-accent)' }}>
                    ${(stat.averagePrice || 0).toFixed(2)}
                  </td>
                  <td className="font-mono" style={{ textAlign: 'right', color: 'var(--text-muted)' }}>
                    ${(stat.minPrice || 0).toFixed(2)}
                  </td>
                  <td className="font-mono" style={{ textAlign: 'right', color: 'var(--text-muted)' }}>
                    ${(stat.maxPrice || 0).toFixed(2)}
                  </td>
                  <td className="font-mono" style={{ textAlign: 'right', fontWeight: 600, color: '#34d399' }}>
                    ${(stat.totalRevenue || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}

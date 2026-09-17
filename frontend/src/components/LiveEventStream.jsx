import React from 'react';
import { Terminal, Filter } from 'lucide-react';

export default function LiveEventStream({ events }) {
  const renderStatus = (status) => {
    switch (status) {
      case 'PROCESSED':
        return <span className="badge badge-success">Processed</span>;
      case 'RETRYING':
        return <span className="badge badge-warning">Retrying</span>;
      case 'DLQ':
        return <span className="badge badge-danger">DLQ Routed</span>;
      default:
        return <span className="badge badge-neutral">{status}</span>;
    }
  };

  return (
    <div className="card" style={{ marginBottom: '24px' }}>
      <div className="card-header">
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <Terminal size={16} color="var(--brand-primary)" />
          <h3 style={{ fontSize: '0.9375rem', fontWeight: 600 }}>Message Stream Audit Log</h3>
        </div>
        <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
          {events.length} events recorded
        </span>
      </div>

      <div style={{ overflowX: 'auto', maxHeight: '380px' }}>
        <table className="data-table">
          <thead>
            <tr>
              <th style={{ width: '120px' }}>Timestamp</th>
              <th style={{ width: '130px' }}>Status</th>
              <th style={{ width: '110px' }}>Order ID</th>
              <th>Product</th>
              <th style={{ textAlign: 'right', width: '110px' }}>Price</th>
              <th>Topic / Context</th>
            </tr>
          </thead>
          <tbody>
            {events.length === 0 ? (
              <tr>
                <td colSpan="6" style={{ padding: '32px 16px', textAlign: 'center', color: 'var(--text-dim)' }}>
                  Awaiting Kafka message consumption...
                </td>
              </tr>
            ) : (
              events.map((evt, idx) => (
                <tr key={idx}>
                  <td className="font-mono" style={{ color: 'var(--text-muted)', fontSize: '0.75rem' }}>
                    {evt.timestamp ? new Date(evt.timestamp).toLocaleTimeString() : '-'}
                  </td>
                  <td>{renderStatus(evt.status)}</td>
                  <td className="font-mono" style={{ fontWeight: 600 }}>#{evt.orderId}</td>
                  <td>{evt.product}</td>
                  <td
                    className="font-mono"
                    style={{
                      textAlign: 'right',
                      fontWeight: 500,
                      color: evt.price >= 0 ? '#34d399' : '#f87171',
                    }}
                  >
                    ${(evt.price || 0).toFixed(2)}
                  </td>
                  <td style={{ color: 'var(--text-muted)', fontSize: '0.8125rem' }}>
                    <span
                      className="font-mono"
                      style={{
                        padding: '2px 6px',
                        borderRadius: '4px',
                        backgroundColor: 'var(--bg-surface-elevated)',
                        border: '1px solid var(--border-subtle)',
                        marginRight: '8px',
                        fontSize: '0.75rem',
                      }}
                    >
                      {evt.topic}
                    </span>
                    {evt.details}
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

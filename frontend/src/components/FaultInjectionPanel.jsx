import React, { useState } from 'react';
import { RefreshCw, AlertTriangle, Play, ListPlus } from 'lucide-react';

export default function FaultInjectionPanel({ onActionTriggered }) {
  const [loading, setLoading] = useState(false);
  const [statusLog, setStatusLog] = useState('');

  const executeAction = async (mode, count = 1, delayMs = 300) => {
    setLoading(true);
    setStatusLog(`Dispatching [${mode}]...`);
    try {
      if (count === 1) {
        const res = await fetch(`/api/orders/random?mode=${mode}`, { method: 'POST' });
        if (res.ok) {
          const data = await res.json();
          setStatusLog(`Dispatched ${mode} order: #${data.orderId} (${data.product})`);
        } else {
          setStatusLog(`Dispatched ${mode} order event.`);
        }
      } else {
        const res = await fetch('/api/orders/batch', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ count, delayMs, mode }),
        });
        if (res.ok) {
          setStatusLog(`Started streaming batch of ${count} orders [mode: ${mode}]`);
        }
      }
      if (onActionTriggered) {
        onActionTriggered();
        setTimeout(onActionTriggered, 600);
        setTimeout(onActionTriggered, 1500);
        setTimeout(onActionTriggered, 3000);
      }
    } catch (err) {
      setStatusLog(`Action status: ${err.message}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="card" style={{ height: '100%' }}>
      <div className="card-header">
        <div>
          <h3 style={{ fontSize: '0.9375rem', fontWeight: 600 }}>Test Scenarios &amp; Resilience</h3>
          <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Simulate transient retries, DLQ routing, and continuous streams</p>
        </div>
      </div>

      <div className="card-body" style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
        <button
          type="button"
          onClick={() => executeAction('normal', 10, 300)}
          disabled={loading}
          className="btn btn-secondary"
          style={{ justifyContent: 'space-between', padding: '12px 16px', textAlign: 'left' }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            <Play size={16} color="#2563eb" />
            <div>
              <div style={{ fontSize: '0.8125rem', fontWeight: 600 }}>Stream Standard Orders</div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Dispatches 10 valid orders (300ms intervals)</div>
            </div>
          </div>
          <span className="badge badge-neutral">10 msg</span>
        </button>

        <button
          type="button"
          onClick={() => executeAction('transient', 1)}
          disabled={loading}
          className="btn btn-secondary"
          style={{ justifyContent: 'space-between', padding: '12px 16px', textAlign: 'left' }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            <RefreshCw size={16} color="#d97706" />
            <div>
              <div style={{ fontSize: '0.8125rem', fontWeight: 600 }}>Simulate Transient Error</div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Triggers retry on orders-retry topic with backoff</div>
            </div>
          </div>
          <span className="badge badge-warning">Retry Policy</span>
        </button>

        <button
          type="button"
          onClick={() => executeAction('fatal', 1)}
          disabled={loading}
          className="btn btn-secondary"
          style={{ justifyContent: 'space-between', padding: '12px 16px', textAlign: 'left' }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            <AlertTriangle size={16} color="#dc2626" />
            <div>
              <div style={{ fontSize: '0.8125rem', fontWeight: 600 }}>Simulate Fatal Error (DLQ)</div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Produces invalid payload routed directly to DLQ</div>
            </div>
          </div>
          <span className="badge badge-danger">DLQ</span>
        </button>

        <button
          type="button"
          onClick={() => executeAction('mixed', 15, 300)}
          disabled={loading}
          className="btn btn-secondary"
          style={{ justifyContent: 'space-between', padding: '12px 16px', textAlign: 'left' }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            <ListPlus size={16} color="#475569" />
            <div>
              <div style={{ fontSize: '0.8125rem', fontWeight: 600 }}>Stream Mixed Load</div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>15 orders combining valid, retrying, and DLQ cases</div>
            </div>
          </div>
          <span className="badge badge-neutral">15 msg</span>
        </button>

        {statusLog && (
          <div
            className="font-mono"
            style={{
              marginTop: '6px',
              padding: '8px 12px',
              borderRadius: '4px',
              fontSize: '0.75rem',
              backgroundColor: '#f1f5f9',
              border: '1px solid #e2e8f0',
              color: '#334155',
            }}
          >
            &gt; {statusLog}
          </div>
        )}
      </div>
    </div>
  );
}

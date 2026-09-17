import React, { useState, useEffect, useCallback } from 'react';
import MetricCards from './components/MetricCards';
import OrderPlacementForm from './components/OrderPlacementForm';
import FaultInjectionPanel from './components/FaultInjectionPanel';
import LiveEventStream from './components/LiveEventStream';
import ProductBreakdownTable from './components/ProductBreakdownTable';
import { RotateCcw, Server, ExternalLink, Activity } from 'lucide-react';

export default function App() {
  const [metrics, setMetrics] = useState({
    totalOrders: 0,
    totalRevenue: 0,
    runningAveragePrice: 0,
    minPrice: 0,
    maxPrice: 0,
    totalRetries: 0,
    totalDlqMessages: 0,
    perProduct: {},
  });

  const [events, setEvents] = useState([]);
  const [connected, setConnected] = useState(false);

  const fetchLatest = useCallback(async () => {
    try {
      const [sumRes, evtRes] = await Promise.all([
        fetch('/api/analytics/summary'),
        fetch('/api/analytics/events'),
      ]);

      if (sumRes.ok) {
        const sumData = await sumRes.json();
        setMetrics(sumData);
        setConnected(true);
      }
      if (evtRes.ok) {
        const evtData = await evtRes.json();
        setEvents(evtData);
      }
    } catch {
      setConnected(false);
    }
  }, []);

  useEffect(() => {
    fetchLatest();

    // SSE connection
    let eventSource;
    try {
      eventSource = new EventSource('/api/analytics/stream');
      eventSource.addEventListener('metrics', (e) => {
        try {
          const data = JSON.parse(e.data);
          setMetrics(data);
          setConnected(true);
        } catch {}
      });
      eventSource.onopen = () => setConnected(true);
      eventSource.onerror = () => {
        setConnected(false);
      };
    } catch {}

    const interval = setInterval(fetchLatest, 1500);

    return () => {
      clearInterval(interval);
      if (eventSource) eventSource.close();
    };
  }, [fetchLatest]);

  const handleReset = async () => {
    if (!window.confirm('Reset all aggregated metrics and stream logs?')) return;
    try {
      await fetch('/api/analytics/reset', { method: 'POST' });
      fetchLatest();
    } catch (err) {
      alert(`Error resetting: ${err.message}`);
    }
  };

  return (
    <div style={{ minHeight: '100vh', backgroundColor: 'var(--bg-app)' }}>
      {/* Formal Top Navigation Bar */}
      <nav
        style={{
          borderBottom: '1px solid var(--border-subtle)',
          backgroundColor: '#ffffff',
          padding: '12px 24px',
          boxShadow: '0 1px 2px 0 rgba(0, 0, 0, 0.03)',
        }}
      >
        <div
          style={{
            maxWidth: '1440px',
            margin: '0 auto',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            flexWrap: 'wrap',
            gap: '12px',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            <div
              style={{
                width: '32px',
                height: '32px',
                borderRadius: '6px',
                backgroundColor: 'var(--brand-primary)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: '#ffffff',
              }}
            >
              <Server size={18} />
            </div>
            <div>
              <div style={{ fontSize: '1rem', fontWeight: 700, color: 'var(--text-main)', letterSpacing: '-0.01em' }}>
                Kafka Stream Ordering System
              </div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                Avro Contract • Spring Boot Microservices • Real-Time Aggregator
              </div>
            </div>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <div
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: '6px',
                padding: '4px 10px',
                borderRadius: '4px',
                fontSize: '0.75rem',
                fontWeight: 600,
                backgroundColor: connected ? 'var(--status-success-bg)' : 'var(--status-danger-bg)',
                color: connected ? 'var(--status-success-text)' : 'var(--status-danger-text)',
                border: `1px solid ${connected ? 'var(--status-success-border)' : 'var(--status-danger-border)'}`,
              }}
            >
              <Activity size={12} />
              <span>{connected ? 'Cluster Online' : 'Connecting...'}</span>
            </div>

            <a
              href="http://localhost:8090"
              target="_blank"
              rel="noreferrer"
              className="btn btn-secondary"
              style={{ fontSize: '0.75rem', padding: '6px 12px' }}
            >
              Kafka Console <ExternalLink size={12} />
            </a>

            <button
              onClick={handleReset}
              className="btn btn-outline"
              style={{ fontSize: '0.75rem', padding: '6px 12px' }}
              title="Reset metrics"
            >
              <RotateCcw size={12} /> Reset
            </button>
          </div>
        </div>
      </nav>

      {/* Main Workspace Container */}
      <main style={{ maxWidth: '1440px', margin: '0 auto', padding: '24px' }}>
        {/* Executive Metric KPI Bar */}
        <MetricCards metrics={metrics} />

        {/* 2-Column Operational Grid */}
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(420px, 1fr))',
            gap: '20px',
            marginBottom: '24px',
          }}
        >
          <OrderPlacementForm onOrderSent={fetchLatest} />
          <FaultInjectionPanel onActionTriggered={fetchLatest} />
        </div>

        {/* Message Stream Audit Log */}
        <LiveEventStream events={events} />

        {/* Per-Product Running Analytics */}
        <ProductBreakdownTable perProduct={metrics.perProduct} />
      </main>
    </div>
  );
}

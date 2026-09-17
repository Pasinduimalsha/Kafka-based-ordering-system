import React from 'react';
import { DollarSign, ShoppingCart, TrendingUp, RefreshCw, AlertTriangle } from 'lucide-react';

export default function MetricCards({ metrics }) {
  const cards = [
    {
      title: 'Running Average Price',
      value: `$${(metrics.runningAveragePrice || 0).toFixed(2)}`,
      caption: 'Real-time cumulative stream average',
      icon: TrendingUp,
      accentColor: 'var(--brand-accent)',
      isPrimary: true,
    },
    {
      title: 'Total Processed Orders',
      value: (metrics.totalOrders || 0).toLocaleString(),
      caption: `Range: $${(metrics.minPrice || 0).toFixed(2)} – $${(metrics.maxPrice || 0).toFixed(2)}`,
      icon: ShoppingCart,
      accentColor: 'var(--text-muted)',
    },
    {
      title: 'Cumulative Revenue',
      value: `$${(metrics.totalRevenue || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`,
      caption: 'Aggregated gross transaction volume',
      icon: DollarSign,
      accentColor: '#34d399',
    },
    {
      title: 'Retry Attempts',
      value: (metrics.totalRetries || 0).toLocaleString(),
      caption: 'Transient failures queued for retry',
      icon: RefreshCw,
      accentColor: '#fbbf24',
    },
    {
      title: 'Dead Letter Queue',
      value: (metrics.totalDlqMessages || 0).toLocaleString(),
      caption: 'Permanently failed or unrouted messages',
      icon: AlertTriangle,
      accentColor: '#f87171',
    },
  ];

  return (
    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '16px', marginBottom: '24px' }}>
      {cards.map((card, idx) => {
        const Icon = card.icon;
        return (
          <div
            key={idx}
            className="card"
            style={{
              padding: '18px 20px',
              borderLeft: card.isPrimary ? '3px solid var(--brand-primary)' : undefined,
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '8px' }}>
              <span style={{ fontSize: '0.8125rem', fontWeight: 500, color: 'var(--text-muted)' }}>
                {card.title}
              </span>
              <Icon size={16} color={card.accentColor} />
            </div>
            <div
              style={{
                fontSize: '1.625rem',
                fontWeight: 600,
                color: card.isPrimary ? 'var(--text-main)' : 'var(--text-main)',
                letterSpacing: '-0.01em',
                marginBottom: '4px',
              }}
            >
              {card.value}
            </div>
            <div style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>
              {card.caption}
            </div>
          </div>
        );
      })}
    </div>
  );
}

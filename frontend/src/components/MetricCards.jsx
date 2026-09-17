import React from 'react';
import { DollarSign, ShoppingCart, TrendingUp, RefreshCw, AlertTriangle } from 'lucide-react';

export default function MetricCards({ metrics }) {
  const cards = [
    {
      title: 'Running Average Price',
      value: `$${(metrics.runningAveragePrice || 0).toFixed(2)}`,
      caption: 'Real-time cumulative stream average',
      icon: TrendingUp,
      iconColor: '#2563eb',
      iconBg: '#eff6ff',
      isPrimary: true,
    },
    {
      title: 'Total Processed Orders',
      value: (metrics.totalOrders || 0).toLocaleString(),
      caption: `Range: $${(metrics.minPrice || 0).toFixed(2)} – $${(metrics.maxPrice || 0).toFixed(2)}`,
      icon: ShoppingCart,
      iconColor: '#0284c7',
      iconBg: '#f0f9ff',
    },
    {
      title: 'Cumulative Revenue',
      value: `$${(metrics.totalRevenue || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`,
      caption: 'Aggregated gross transaction volume',
      icon: DollarSign,
      iconColor: '#059669',
      iconBg: '#ecfdf5',
    },
    {
      title: 'Retry Attempts',
      value: (metrics.totalRetries || 0).toLocaleString(),
      caption: 'Transient failures queued for retry',
      icon: RefreshCw,
      iconColor: '#d97706',
      iconBg: '#fffbeb',
    },
    {
      title: 'Dead Letter Queue',
      value: (metrics.totalDlqMessages || 0).toLocaleString(),
      caption: 'Permanently failed or unrouted messages',
      icon: AlertTriangle,
      iconColor: '#dc2626',
      iconBg: '#fef2f2',
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
              borderTop: card.isPrimary ? '3px solid var(--brand-primary)' : '1px solid var(--border-subtle)',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '10px' }}>
              <span style={{ fontSize: '0.8125rem', fontWeight: 600, color: 'var(--text-muted)' }}>
                {card.title}
              </span>
              <div
                style={{
                  width: '28px',
                  height: '28px',
                  borderRadius: '6px',
                  backgroundColor: card.iconBg,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  color: card.iconColor,
                }}
              >
                <Icon size={16} />
              </div>
            </div>
            <div
              style={{
                fontSize: '1.625rem',
                fontWeight: 700,
                color: card.isPrimary ? 'var(--brand-primary)' : 'var(--text-main)',
                letterSpacing: '-0.02em',
                marginBottom: '4px',
              }}
            >
              {card.value}
            </div>
            <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
              {card.caption}
            </div>
          </div>
        );
      })}
    </div>
  );
}

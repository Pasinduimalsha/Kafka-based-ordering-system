import React, { useState } from 'react';
import { Send, Shuffle, CheckCircle2, AlertCircle } from 'lucide-react';

export default function OrderPlacementForm({ onOrderSent }) {
  const [orderId, setOrderId] = useState('');
  const [product, setProduct] = useState('');
  const [price, setPrice] = useState('');
  const [loading, setLoading] = useState(false);
  const [feedback, setFeedback] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!orderId || !product || !price) return;

    setLoading(true);
    setFeedback(null);
    try {
      const res = await fetch('/api/orders', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          orderId: orderId.trim(),
          product: product.trim(),
          price: parseFloat(price),
        }),
      });

      if (res.ok) {
        const data = await res.json();
        setFeedback({ type: 'success', text: `Order #${data.orderId} successfully dispatched to topic '${data.topic}'.` });
        setOrderId('');
        setProduct('');
        setPrice('');
        if (onOrderSent) onOrderSent();
      } else {
        const err = await res.text();
        setFeedback({ type: 'error', text: `Failed to produce order: ${err}` });
      }
    } catch (err) {
      setFeedback({ type: 'error', text: `Network error: ${err.message}` });
    } finally {
      setLoading(false);
    }
  };

  const handleRandomOrder = async () => {
    setLoading(true);
    setFeedback(null);
    try {
      const res = await fetch('/api/orders/random?mode=normal', { method: 'POST' });
      if (res.ok) {
        const data = await res.json();
        setFeedback({ type: 'success', text: `Dispatched sample order #${data.orderId} ($${data.price.toFixed(2)})` });
        if (onOrderSent) onOrderSent();
      }
    } catch (err) {
      setFeedback({ type: 'error', text: `Error: ${err.message}` });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="card" style={{ height: '100%' }}>
      <div className="card-header">
        <div>
          <h3 style={{ fontSize: '0.9375rem', fontWeight: 600 }}>Order Ingestion</h3>
          <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Publish Avro-serialized orders to Kafka</p>
        </div>
        <button
          type="button"
          onClick={handleRandomOrder}
          disabled={loading}
          className="btn btn-secondary"
          style={{ fontSize: '0.75rem', padding: '5px 10px' }}
        >
          <Shuffle size={13} /> Populate Sample
        </button>
      </div>

      <div className="card-body">
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Order Identifier</label>
            <input
              type="text"
              className="form-input font-mono"
              placeholder="e.g. ORD-1001"
              value={orderId}
              onChange={(e) => setOrderId(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">Product Name</label>
            <input
              type="text"
              className="form-input"
              placeholder="e.g. Workstation Display 27-inch"
              value={product}
              onChange={(e) => setProduct(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">Unit Price (USD)</label>
            <input
              type="number"
              step="0.01"
              className="form-input font-mono"
              placeholder="e.g. 549.00"
              value={price}
              onChange={(e) => setPrice(e.target.value)}
              required
            />
          </div>

          <button type="submit" disabled={loading} className="btn btn-primary" style={{ width: '100%', marginTop: '4px' }}>
            <Send size={15} /> {loading ? 'Publishing Order...' : 'Publish Order Message'}
          </button>

          {feedback && (
            <div
              style={{
                marginTop: '16px',
                padding: '10px 12px',
                borderRadius: '6px',
                fontSize: '0.8125rem',
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
                backgroundColor: feedback.type === 'success' ? 'var(--status-success-bg)' : 'var(--status-danger-bg)',
                color: feedback.type === 'success' ? 'var(--status-success-text)' : 'var(--status-danger-text)',
                border: `1px solid ${feedback.type === 'success' ? 'var(--status-success-border)' : 'var(--status-danger-border)'}`,
              }}
            >
              {feedback.type === 'success' ? <CheckCircle2 size={16} /> : <AlertCircle size={16} />}
              <span>{feedback.text}</span>
            </div>
          )}
        </form>
      </div>
    </div>
  );
}

import { useEffect, useRef } from 'react';
import { logger } from '../utils/logger';

interface ErrorBannerProps {
  message: string;
  onDismiss?: () => void;
  correlationId?: string;
}

function ErrorBanner({ message, onDismiss, correlationId }: ErrorBannerProps) {
  const bannerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    logger.error('ErrorBanner displayed', {
      event: 'error_banner_display',
      message,
      correlation_id: correlationId
    });

    if (bannerRef.current) {
      bannerRef.current.focus();
    }
  }, [message, correlationId]);

  const handleDismiss = () => {
    logger.info('ErrorBanner dismissed', {
      event: 'error_banner_dismiss',
      correlation_id: correlationId
    });
    if (onDismiss) {
      onDismiss();
    }
  };

  return (
    <div
      ref={bannerRef}
      role="alert"
      aria-live="assertive"
      tabIndex={-1}
      style={{
        padding: '1rem',
        marginBottom: '1rem',
        backgroundColor: '#fee',
        color: '#c33',
        borderRadius: '4px',
        border: '1px solid #fcc',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center'
      }}
    >
      <span>{message}</span>
      {onDismiss && (
        <button
          onClick={handleDismiss}
          aria-label="Dismiss error"
          style={{
            marginLeft: '1rem',
            padding: '0.25rem 0.5rem',
            backgroundColor: 'transparent',
            border: '1px solid #c33',
            borderRadius: '4px',
            color: '#c33',
            cursor: 'pointer',
            fontSize: '0.875rem'
          }}
        >
          Dismiss
        </button>
      )}
    </div>
  );
}

export default ErrorBanner;
type LogLevel = 'info' | 'warn' | 'error';

interface LogContext {
  [key: string]: any;
}

class Logger {
  private isProduction = import.meta.env.PROD;

  private log(level: LogLevel, message: string, context?: LogContext) {
    if (this.isProduction && level === 'info') {
      return;
    }

    const timestamp = new Date().toISOString();
    const logEntry = {
      timestamp,
      level,
      message,
      ...context
    };

    const consoleMethod = level === 'error' ? console.error : level === 'warn' ? console.warn : console.info;
    
    consoleMethod(`[${timestamp}] [${level.toUpperCase()}] ${message}`, context || {});

    return logEntry;
  }

  info(message: string, context?: LogContext) {
    return this.log('info', message, context);
  }

  warn(message: string, context?: LogContext) {
    return this.log('warn', message, context);
  }

  error(message: string, context?: LogContext) {
    return this.log('error', message, context);
  }
}

export const logger = new Logger();
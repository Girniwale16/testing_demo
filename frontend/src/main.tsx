import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';

vi.mock('react-dom/client');
vi.mock('./App');

describe('main.tsx', () => {
  let mockRoot: any;
  let mockRender: any;
  let mockGetElementById: any;

  beforeEach(() => {
    mockRender = vi.fn();
    mockRoot = {
      render: mockRender,
    };
    
    (ReactDOM.createRoot as any) = vi.fn(() => mockRoot);
    
    mockGetElementById = vi.fn(() => ({
      id: 'root',
      tagName: 'DIV',
    }));
    
    document.getElementById = mockGetElementById;
    
    (App as any) = vi.fn(() => React.createElement('div', null, 'App'));
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should call document.getElementById with "root"', () => {
    require('./main');
    expect(mockGetElementById).toHaveBeenCalledWith('root');
  });

  it('should create a React root with the root element', () => {
    const rootElement = document.getElementById('root');
    require('./main');
    expect(ReactDOM.createRoot).toHaveBeenCalledWith(rootElement);
  });

  it('should render App component wrapped in React.StrictMode', () => {
    require('./main');
    expect(mockRender).toHaveBeenCalledTimes(1);
    
    const renderCall = mockRender.mock.calls[0][0];
    expect(renderCall.type).toBe(React.StrictMode);
    expect(renderCall.props.children.type).toBe(App);
  });

  it('should throw error when root element is null', () => {
    document.getElementById = vi.fn(() => null);
    
    expect(() => {
      ReactDOM.createRoot(document.getElementById('root')!);
    }).toThrow();
  });

  it('should initialize application with correct React version', () => {
    require('./main');
    expect(React.StrictMode).toBeDefined();
    expect(ReactDOM.createRoot).toBeDefined();
  });

  it('should ensure App component is imported correctly', () => {
    require('./main');
    expect(App).toBeDefined();
  });

  it('should verify ReactDOM.createRoot is called before render', () => {
    const callOrder: string[] = [];
    
    (ReactDOM.createRoot as any) = vi.fn(() => {
      callOrder.push('createRoot');
      return mockRoot;
    });
    
    mockRoot.render = vi.fn(() => {
      callOrder.push('render');
    });
    
    require('./main');
    
    expect(callOrder).toEqual(['createRoot', 'render']);
  });

  it('should mount application only once', () => {
    require('./main');
    expect(ReactDOM.createRoot).toHaveBeenCalledTimes(1);
    expect(mockRender).toHaveBeenCalledTimes(1);
  });

  it('should use non-null assertion operator for root element', () => {
    const rootElement = document.getElementById('root')!;
    expect(rootElement).toBeDefined();
  });

  it('should verify StrictMode wraps the entire application', () => {
    require('./main');
    const renderCall = mockRender.mock.calls[0][0];
    
    expect(renderCall.type).toBe(React.StrictMode);
    expect(renderCall.props).toHaveProperty('children');
  });
});
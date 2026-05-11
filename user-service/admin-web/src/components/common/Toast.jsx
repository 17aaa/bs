import styled from 'styled-components';

const ToastContainer = styled.div`
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 2000;
  display: flex;
  flex-direction: column;
  gap: 8px;
`;

const ToastItem = styled.div`
  padding: 12px 20px;
  border-radius: 8px;
  font-size: 14px;
  color: white;
  animation: slideIn 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);

  @keyframes slideIn {
    from {
      opacity: 0;
      transform: translateX(100%);
    }
    to {
      opacity: 1;
      transform: translateX(0);
    }
  }

  ${(props) => {
    switch (props.type) {
      case 'success':
        return 'background: #10b981;';
      case 'error':
        return 'background: #ef4444;';
      case 'warning':
        return 'background: #f59e0b;';
      default:
        return 'background: #3b82f6;';
    }
  }}
`;

let toastContainer = null;
let toasts = [];

function createContainer() {
  if (!toastContainer) {
    toastContainer = document.createElement('div');
    document.body.appendChild(toastContainer);
  }
  return toastContainer;
}

function renderToasts() {
  const container = createContainer();
  const root = document.createElement('div');

  toasts.forEach((toast) => {
    const item = document.createElement('div');
    item.className = 'toast-item';
    item.setAttribute('data-id', toast.id);
    item.textContent = toast.message;
    root.appendChild(item);
  });

  container.innerHTML = '';
  container.appendChild(root);
}

const toast = {
  show(message, type = 'info', duration = 3000) {
    const id = Date.now();
    toasts.push({ id, message, type });
    renderToasts();

    setTimeout(() => {
      toasts = toasts.filter((t) => t.id !== id);
      renderToasts();
    }, duration);
  },

  success(message) {
    this.show(message, 'success');
  },

  error(message) {
    this.show(message, 'error');
  },

  warning(message) {
    this.show(message, 'warning');
  },

  info(message) {
    this.show(message, 'info');
  },
};

export default toast;
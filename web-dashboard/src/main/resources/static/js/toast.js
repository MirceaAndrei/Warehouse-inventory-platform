function showToast(message, type, duration) {
    type = type || 'success';
    duration = duration || 4000;

    var container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        container.style.cssText = 'position:fixed;top:24px;right:24px;z-index:99999;display:flex;flex-direction:column;gap:10px;max-width:390px;pointer-events:none;';
        document.body.appendChild(container);
    }

    var configs = {
        success: { bg: 'linear-gradient(135deg,#10b981,#059669)', icon: 'fas fa-check-circle' },
        error:   { bg: 'linear-gradient(135deg,#ef4444,#dc2626)', icon: 'fas fa-times-circle' },
        warning: { bg: 'linear-gradient(135deg,#f59e0b,#d97706)', icon: 'fas fa-exclamation-triangle' },
        info:    { bg: 'linear-gradient(135deg,#6366f1,#4f46e5)', icon: 'fas fa-info-circle' }
    };
    var cfg = configs[type] || configs.info;

    var toast = document.createElement('div');
    toast.style.cssText = [
        'background:' + cfg.bg,
        'color:#fff',
        'padding:13px 16px',
        'border-radius:12px',
        'font-size:0.875rem',
        'font-weight:500',
        'pointer-events:auto',
        'box-shadow:0 8px 28px rgba(0,0,0,0.22)',
        'display:flex',
        'align-items:center',
        'gap:10px',
        'transform:translateX(120%)',
        'transition:transform 0.32s cubic-bezier(0.34,1.56,0.64,1)',
        'max-width:390px',
        'line-height:1.4',
        'cursor:pointer',
        'user-select:none'
    ].join(';');

    toast.innerHTML =
        '<i class="' + cfg.icon + '" style="font-size:1rem;flex-shrink:0"></i>' +
        '<span style="flex:1">' + message + '</span>' +
        '<span style="flex-shrink:0;opacity:0.65;font-size:1rem;line-height:1">&times;</span>';

    container.appendChild(toast);

    requestAnimationFrame(function () {
        requestAnimationFrame(function () {
            toast.style.transform = 'translateX(0)';
        });
    });

    function dismiss() {
        toast.style.transition = 'transform 0.22s ease-in';
        toast.style.transform = 'translateX(120%)';
        setTimeout(function () { if (toast.parentNode) toast.remove(); }, 240);
    }

    toast.addEventListener('click', dismiss);
    var timer = setTimeout(dismiss, duration);
    toast.addEventListener('mouseenter', function () { clearTimeout(timer); });
    toast.addEventListener('mouseleave', function () { timer = setTimeout(dismiss, 1500); });
}

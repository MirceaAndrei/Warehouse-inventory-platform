/**
 * Reusable client-side paginator.
 * Works on top of existing filter logic — filters mark rows, paginator shows the right slice.
 *
 * Usage:
 *   var p = new Paginator({ tableBody: '#myTbody', pageInfo: 'pageInfo', controls: 'paginationControls', rowsPerPage: 'rowsPerPage' });
 *   // In applyFilters(), set row.dataset.matched = '1' or '0', then call p.render();
 */
function Paginator(opts) {
    var self       = this;
    var tbodyEl    = document.querySelector(opts.tableBody);
    var infoEl     = document.getElementById(opts.pageInfo);
    var controlsEl = document.getElementById(opts.controls);
    var rppEl      = document.getElementById(opts.rowsPerPage);
    var rowSel     = opts.rowSelector || 'tr[data-matched]';

    self.currentPage  = 1;
    self.rowsPerPage  = parseInt(opts.defaultRows || 10);

    if (rppEl) {
        rppEl.value = self.rowsPerPage;
        rppEl.addEventListener('change', function () {
            self.rowsPerPage = parseInt(this.value);
            self.currentPage = 1;
            self.render();
        });
    }

    /** Call after filters have set data-matched on rows. */
    self.render = function () {
        if (!tbodyEl) return;
        var all     = Array.from(tbodyEl.querySelectorAll(rowSel));
        var matched = all.filter(function (r) { return r.dataset.matched === '1'; });
        var total   = matched.length;
        var pages   = Math.max(1, Math.ceil(total / self.rowsPerPage));

        if (self.currentPage > pages) self.currentPage = pages;

        var start = (self.currentPage - 1) * self.rowsPerPage;
        var end   = start + self.rowsPerPage;

        // Hide all matched, then reveal current slice
        all.forEach(function (r) { if (r.dataset.matched !== '1') r.style.display = 'none'; });
        matched.forEach(function (r, i) {
            r.style.display = (i >= start && i < end) ? '' : 'none';
        });

        // Info text
        if (infoEl) {
            infoEl.textContent = total === 0
                ? '0'
                : (start + 1) + '–' + Math.min(end, total) + ' of ' + total;
        }

        _renderControls(pages, total);
    };

    self.goTo = function (page) {
        var all   = Array.from(tbodyEl.querySelectorAll(rowSel));
        var total = all.filter(function (r) { return r.dataset.matched === '1'; }).length;
        var pages = Math.max(1, Math.ceil(total / self.rowsPerPage));
        self.currentPage = Math.max(1, Math.min(page, pages));
        self.render();
    };

    function _renderControls(totalPages, total) {
        if (!controlsEl) return;
        if (totalPages <= 1) { controlsEl.innerHTML = ''; return; }

        var html = '<ul class="pagination pagination-sm mb-0 flex-wrap gap-1">';

        html += _pageItem(self.currentPage - 1, '<i class="fas fa-chevron-left"></i>', self.currentPage === 1);

        _pageNumbers(self.currentPage, totalPages).forEach(function (p) {
            if (p === '...') {
                html += '<li class="page-item disabled"><span class="page-link px-2">…</span></li>';
            } else {
                html += _pageItem(p, p, false, p === self.currentPage);
            }
        });

        html += _pageItem(self.currentPage + 1, '<i class="fas fa-chevron-right"></i>', self.currentPage === totalPages);
        html += '</ul>';

        controlsEl.innerHTML = html;

        controlsEl.querySelectorAll('a.page-link[data-page]').forEach(function (a) {
            a.addEventListener('click', function (e) {
                e.preventDefault();
                self.goTo(parseInt(this.getAttribute('data-page')));
                // Scroll table into view smoothly
                if (tbodyEl) tbodyEl.closest('.card, .table-responsive, table')?.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
            });
        });
    }

    function _pageItem(page, label, disabled, active) {
        var cls = 'page-item' + (disabled ? ' disabled' : '') + (active ? ' active' : '');
        var inner = disabled
            ? '<span class="page-link px-2">' + label + '</span>'
            : '<a class="page-link px-2" href="#" data-page="' + page + '">' + label + '</a>';
        return '<li class="' + cls + '">' + inner + '</li>';
    }

    function _pageNumbers(cur, total) {
        if (total <= 7) {
            var a = [];
            for (var i = 1; i <= total; i++) a.push(i);
            return a;
        }
        var p = [1];
        if (cur > 3)         p.push('...');
        for (var i = Math.max(2, cur - 1); i <= Math.min(total - 1, cur + 1); i++) p.push(i);
        if (cur < total - 2) p.push('...');
        p.push(total);
        return p;
    }
}

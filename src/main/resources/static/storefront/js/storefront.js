/* FS Shoes — storefront.js: tương tác dùng chung cho website bán hàng */
(function () {
    "use strict";

    function getCsrf() {
        var tokenMeta = document.querySelector('meta[name="_csrf"]');
        var headerMeta = document.querySelector('meta[name="_csrf_header"]');
        return {
            token: tokenMeta ? tokenMeta.getAttribute("content") : null,
            header: headerMeta ? headerMeta.getAttribute("content") : null
        };
    }

    function showToast(message, isError) {
        var wrap = document.getElementById("fs-toast-wrap");
        if (!wrap) {
            wrap = document.createElement("div");
            wrap.id = "fs-toast-wrap";
            document.body.appendChild(wrap);
        }
        var toast = document.createElement("div");
        toast.className = "fs-toast" + (isError ? " fs-toast-error" : "");
        toast.innerHTML = '<i class="bi ' + (isError ? "bi-exclamation-circle-fill" : "bi-check-circle-fill") + '"></i><span></span>';
        toast.querySelector("span").textContent = message;
        wrap.appendChild(toast);
        setTimeout(function () {
            toast.style.opacity = "0";
            toast.style.transition = "opacity .25s ease";
            setTimeout(function () { toast.remove(); }, 250);
        }, 2800);
    }
    window.fsToast = showToast;

    function updateCartBadge(soLuong) {
        document.querySelectorAll(".fs-cart-badge").forEach(function (el) {
            el.textContent = soLuong;
            el.style.display = soLuong > 0 ? "flex" : "none";
        });
    }

    // ---- Thêm vào giỏ hàng (AJAX) ----
    document.addEventListener("click", function (e) {
        var btn = e.target.closest(".js-add-to-cart");
        if (!btn) return;
        e.preventDefault();
        if (btn.disabled) return;

        var maSPCT = btn.getAttribute("data-ma-spct");
        var qtyInput = btn.getAttribute("data-qty-input");
        var soLuong = 1;
        if (qtyInput) {
            var input = document.querySelector(qtyInput);
            if (input) soLuong = parseInt(input.value, 10) || 1;
        }
        if (!maSPCT) {
            showToast("Vui lòng chọn màu sắc và kích thước.", true);
            return;
        }

        var csrf = getCsrf();
        var headers = { "Content-Type": "application/x-www-form-urlencoded" };
        if (csrf.token && csrf.header) headers[csrf.header] = csrf.token;

        var originalHtml = btn.innerHTML;
        btn.disabled = true;
        btn.innerHTML = '<span>Đang thêm...</span>';

        fetch("/gio-hang/api/them", {
            method: "POST",
            headers: headers,
            body: "maSanPhamChiTiet=" + encodeURIComponent(maSPCT) + "&soLuong=" + encodeURIComponent(soLuong)
        })
            .then(function (res) { return res.json(); })
            .then(function (data) {
                showToast(data.thongBao, !data.thanhCong);
                updateCartBadge(data.tongSoLuong);
            })
            .catch(function () {
                showToast("Có lỗi xảy ra, vui lòng thử lại.", true);
            })
            .finally(function () {
                btn.disabled = false;
                btn.innerHTML = originalHtml;
            });
    });

    // ---- Dropdown (tài khoản, danh mục) ----
    document.addEventListener("click", function (e) {
        var toggle = e.target.closest("[data-fs-dropdown-toggle]");
        document.querySelectorAll(".fs-dropdown.open").forEach(function (dd) {
            if (!toggle || dd !== toggle.closest(".fs-dropdown")) dd.classList.remove("open");
        });
        if (toggle) {
            var dd = toggle.closest(".fs-dropdown");
            if (dd) dd.classList.toggle("open");
        }
    });

    // ---- Menu mobile ----
    document.addEventListener("click", function (e) {
        if (e.target.closest("[data-fs-mobile-toggle]")) {
            var menu = document.getElementById("fs-mobile-menu");
            if (menu) menu.classList.toggle("open");
        }
    });

    // ---- Qty stepper (trang chi tiết / giỏ hàng) ----
    document.addEventListener("click", function (e) {
        var stepBtn = e.target.closest("[data-qty-step]");
        if (!stepBtn) return;
        var wrap = stepBtn.closest(".fs-qty-stepper");
        if (!wrap) return;
        var input = wrap.querySelector("input");
        if (!input) return;
        var delta = parseInt(stepBtn.getAttribute("data-qty-step"), 10);
        var max = parseInt(input.getAttribute("max") || "999", 10);
        var min = parseInt(input.getAttribute("min") || "1", 10);
        var val = (parseInt(input.value, 10) || min) + delta;
        if (val < min) val = min;
        if (val > max) val = max;
        input.value = val;
        if (wrap.hasAttribute("data-auto-submit")) {
            input.form && input.form.requestSubmit();
        }
    });
})();

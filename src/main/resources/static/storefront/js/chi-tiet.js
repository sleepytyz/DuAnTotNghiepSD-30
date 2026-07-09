/* =====================================================================
   FS SHOES — chi-tiet.js
   Chọn màu / size, đổi giá + tồn kho + thư viện ảnh theo biến thể,
   thêm giỏ / mua ngay, chấm sao khi viết đánh giá.
   Yêu cầu: window.FS_BIEN_THE = [ {maSanPhamChiTiet, maMauSac, tenMauSac,
   maKichThuoc, tenKichThuoc, giaGoc, giaSauGiam, phanTramGiam, soLuongTon,
   anh, danhSachAnh[]}, ... ]  (được Thymeleaf bơm vào trang chi tiết)
   ===================================================================== */
(function () {
    'use strict';
    var DS = window.FS_BIEN_THE || [];
    if (!document.getElementById('fsMauChips')) return;

    var mauChips = document.getElementById('fsMauChips');
    var sizeChips = document.getElementById('fsSizeChips');
    var giaNow = document.getElementById('fsGiaNow');
    var giaOld = document.getElementById('fsGiaOld');
    var offBadge = document.getElementById('fsOffBadge');
    var stockLine = document.getElementById('fsStockLine');
    var mainImg = document.getElementById('fsMainImg');
    var thumbs = document.getElementById('fsThumbs');
    var qtyInput = document.getElementById('fsQty');
    var btnThem = document.getElementById('fsBtnThem');
    var btnMua = document.getElementById('fsBtnMua');
    var tenMauEl = document.getElementById('fsTenMau');
    var tenSizeEl = document.getElementById('fsTenSize');

    // Danh sách màu duy nhất (giữ thứ tự)
    var dsMau = [];
    DS.forEach(function (b) {
        if (!dsMau.some(function (m) { return m.ma === b.maMauSac; })) {
            dsMau.push({ ma: b.maMauSac, ten: b.tenMauSac });
        }
    });

    var mauChon = null;
    var sizeChon = null;

    function bienTheTheo(mau, size) {
        return DS.find(function (b) { return b.maMauSac === mau && b.maKichThuoc === size; }) || null;
    }
    function tonTheoMau(mau) {
        return DS.filter(function (b) { return b.maMauSac === mau; })
            .reduce(function (s, b) { return s + (b.soLuongTon || 0); }, 0);
    }

    function veMau() {
        mauChips.innerHTML = '';
        dsMau.forEach(function (m) {
            var btn = document.createElement('button');
            btn.type = 'button';
            btn.className = 'fs-chip' + (m.ma === mauChon ? ' active' : '') + (tonTheoMau(m.ma) <= 0 ? ' het' : '');
            btn.textContent = m.ten;
            btn.addEventListener('click', function () {
                mauChon = m.ma;
                if (tenMauEl) tenMauEl.textContent = m.ten;
                // giữ size nếu tồn tại ở màu mới, không thì bỏ chọn
                if (!bienTheTheo(mauChon, sizeChon)) sizeChon = null;
                veMau(); veSize(); capNhat();
            });
            mauChips.appendChild(btn);
        });
    }

    function veSize() {
        sizeChips.innerHTML = '';
        var dsSize = DS.filter(function (b) { return b.maMauSac === mauChon; });
        dsSize.sort(function (a, b) {
            var x = parseInt(a.tenKichThuoc, 10), y = parseInt(b.tenKichThuoc, 10);
            if (isNaN(x) || isNaN(y)) return String(a.tenKichThuoc).localeCompare(String(b.tenKichThuoc));
            return x - y;
        });
        dsSize.forEach(function (b) {
            var btn = document.createElement('button');
            btn.type = 'button';
            var het = (b.soLuongTon || 0) <= 0;
            btn.className = 'fs-chip' + (b.maKichThuoc === sizeChon ? ' active' : '') + (het ? ' het' : '');
            btn.textContent = b.tenKichThuoc;
            if (!het) btn.addEventListener('click', function () {
                sizeChon = b.maKichThuoc;
                if (tenSizeEl) tenSizeEl.textContent = b.tenKichThuoc;
                veSize(); capNhat();
            });
            sizeChips.appendChild(btn);
        });
    }

    function veAnh(bt) {
        var anhs = (bt && bt.danhSachAnh && bt.danhSachAnh.length) ? bt.danhSachAnh
            : (bt && bt.anh ? [bt.anh] : []);
        if (!anhs.length) return;
        if (mainImg) mainImg.src = anhs[0];
        if (thumbs) {
            thumbs.innerHTML = '';
            anhs.forEach(function (a, i) {
                var d = document.createElement('div');
                d.className = 'fs-thumb' + (i === 0 ? ' active' : '');
                var img = document.createElement('img');
                img.src = a;
                img.alt = '';
                img.onerror = function () { img.src = '/storefront/img/no-image.svg'; };
                d.appendChild(img);
                d.addEventListener('click', function () {
                    if (mainImg) mainImg.src = a;
                    thumbs.querySelectorAll('.fs-thumb').forEach(function (t) { t.classList.remove('active'); });
                    d.classList.add('active');
                });
                thumbs.appendChild(d);
            });
        }
    }

    function capNhat() {
        var bt = bienTheTheo(mauChon, sizeChon);
        var btGia = bt || DS.filter(function (b) { return b.maMauSac === mauChon; })[0] || DS[0];

        if (btGia) {
            if (giaNow) giaNow.textContent = fsTien(btGia.giaSauGiam);
            if (giaOld) {
                giaOld.textContent = fsTien(btGia.giaGoc);
                giaOld.style.display = (btGia.phanTramGiam > 0) ? '' : 'none';
            }
            if (offBadge) {
                offBadge.textContent = '-' + btGia.phanTramGiam + '%';
                offBadge.style.display = (btGia.phanTramGiam > 0) ? '' : 'none';
            }
            veAnh(btGia);
        }

        var sanSang = !!bt && (bt.soLuongTon || 0) > 0;
        if (stockLine) {
            if (!sizeChon) {
                stockLine.className = 'fs-stock-line';
                stockLine.textContent = 'Vui lòng chọn kích thước để xem tồn kho.';
            } else if (!bt || (bt.soLuongTon || 0) <= 0) {
                stockLine.className = 'fs-stock-line out';
                stockLine.textContent = 'Biến thể này tạm hết hàng. Vui lòng chọn màu / size khác.';
            } else if (bt.soLuongTon <= 5) {
                stockLine.className = 'fs-stock-line low';
                stockLine.textContent = 'Chỉ còn ' + bt.soLuongTon + ' sản phẩm — nhanh tay kẻo hết!';
            } else {
                stockLine.className = 'fs-stock-line ok';
                stockLine.textContent = 'Còn hàng (' + bt.soLuongTon + ' sản phẩm sẵn kho).';
            }
        }
        if (qtyInput) {
            qtyInput.max = bt ? Math.max(1, bt.soLuongTon || 1) : 1;
            if (parseInt(qtyInput.value, 10) > parseInt(qtyInput.max, 10)) qtyInput.value = qtyInput.max;
        }
        if (btnThem) btnThem.disabled = !sanSang;
        if (btnMua) btnMua.disabled = !sanSang;
    }

    // Bộ đếm số lượng
    var qMinus = document.getElementById('fsQtyMinus');
    var qPlus = document.getElementById('fsQtyPlus');
    if (qMinus) qMinus.addEventListener('click', function () {
        qtyInput.value = Math.max(1, parseInt(qtyInput.value || '1', 10) - 1);
    });
    if (qPlus) qPlus.addEventListener('click', function () {
        var max = parseInt(qtyInput.max || '99', 10);
        qtyInput.value = Math.min(max, parseInt(qtyInput.value || '1', 10) + 1);
    });

    function themVaoGio(sauDo) {
        var bt = bienTheTheo(mauChon, sizeChon);
        if (!bt) { fsToast('Vui lòng chọn màu sắc và kích thước.', 'err'); return; }
        var soLuong = Math.max(1, parseInt(qtyInput ? qtyInput.value : '1', 10) || 1);
        fsPost('/gio-hang/them', { maSanPhamChiTiet: bt.maSanPhamChiTiet, soLuong: soLuong })
            .then(function (kq) {
                fsBadge(kq.tongSoLuong);
                fsToast(kq.thongBao, kq.ok ? 'ok' : 'err');
                if (kq.ok && typeof sauDo === 'function') sauDo();
            }).catch(function () {});
    }
    if (btnThem) btnThem.addEventListener('click', function () { themVaoGio(); });
    if (btnMua) btnMua.addEventListener('click', function () {
        themVaoGio(function () { window.location.href = '/thanh-toan'; });
    });

    // Chấm sao trong form đánh giá
    var starPick = document.getElementById('fsStarPick');
    if (starPick) {
        var hidden = document.getElementById('fsSoSao');
        var stars = starPick.querySelectorAll('i');
        function toSao(n) {
            stars.forEach(function (s, i) { s.classList.toggle('on', i < n); });
            if (hidden) hidden.value = n;
        }
        stars.forEach(function (s, i) {
            s.addEventListener('click', function () { toSao(i + 1); });
        });
        toSao(5);
    }

    // Khởi tạo: chọn màu đầu còn hàng (hoặc màu đầu tiên)
    var mauDau = dsMau.find(function (m) { return tonTheoMau(m.ma) > 0; }) || dsMau[0];
    if (mauDau) {
        mauChon = mauDau.ma;
        if (tenMauEl) tenMauEl.textContent = mauDau.ten;
    }
    veMau(); veSize(); capNhat();
})();

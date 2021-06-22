var VistaThumbnail = function (opt) {
    var _this = this;
    _.extend(this, opt);
    this.contenedor.load("../Scripts/ControlesImagenes/VistaThumbnail.html", function () {
        _this.img_thumbnail = _this.contenedor.find('#img_thumbnail');
        _this.img_estatica = _this.contenedor.find('#img_estatica');
        _this.btn_eliminar = _this.contenedor.find('#btn_eliminar');
        _this.btn_crop = _this.contenedor.find('#btn_crop');
        _this.btn_ok = _this.contenedor.find('#btn_ok');
        _this.btn_not_ok = _this.contenedor.find('#btn_not_ok');

        if (_this.alEliminar) {
            _this.btn_eliminar.show();
            _this.btn_eliminar.click(function () {
                _this.alEliminar();
            });
        }

        if (_this.alClickear) {
            _this.img_thumbnail.click(function () {
                _this.alClickear();
            });
        }

        if (_this.alRecortar) {
            _this.btn_crop.click(function () {
                var img_crop;
                _this.img_thumbnail.imgAreaSelect({
                    handles: true,
                    onSelectEnd: function (img, selection) {
                        var canvas_2 = document.createElement("canvas"),
                        ctx_2 = canvas_2.getContext('2d');

                        canvas_2.width = selection.width;
                        canvas_2.height = selection.height;
                        ctx_2.drawImage(img, selection.x1, selection.y1, selection.width, selection.height, 0, 0, selection.width, selection.height);
                        var url = canvas_2.toDataURL();

                        _this.img_thumbnail.imgAreaSelect({ remove: true });
                        _this.alRecortar(url.replace(/^data:image\/(png|jpg);base64,/, ""));
                    }
                });

            });
            _this.btn_crop.show();
        }

        _this.img_thumbnail.hide();
        _this.img_estatica.show();

        if (_this.bytes_imagen) {
            _this.img_thumbnail.show();
            _this.img_estatica.hide();
            _this.img_thumbnail.attr("src", "data:image/png;base64," + _this.bytes_imagen)
        } else {
            if (_this.id > 0) _this.getImagen();
            else {
                _this.img_estatica.hide();
            }
        }
    });
};

VistaThumbnail.prototype.getImagen = function () {
    var _this = this;
    Backend.GetThumbnail(this.id, Math.round(this.contenedor.height()), Math.round(this.contenedor.width())).onSuccess(function (imagen) {
        if (imagen.reintentar) {
            setTimeout(function () { _this.getImagen(); }, 500);
            return;
        }
        _this.img_thumbnail.show();
        _this.img_estatica.hide();
        _this.img_thumbnail.attr("src", "data:image/png;base64," + imagen.bytes)
    });
};
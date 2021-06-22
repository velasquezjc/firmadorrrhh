var SubidorDeImagenes = function () {
};

SubidorDeImagenes.prototype.subirImagenes = function (onImagenLista) {
    var _this = this;
    var fileInputImagenes = $('<input type="file" multiple />')[0];
    fileInputImagenes.addEventListener("change", function () {
        _this.colaDeSubida = fileInputImagenes.files;
        _this.indiceFileSubiendo = 0;
        _this.subirProximaImagen(onImagenLista);
    }, false);
    $(fileInputImagenes).click();
};

SubidorDeImagenes.prototype.subirImagen = function (onImagenLista) {
    var _this = this;
    var fileInputImagenes = $('<input type="file" />')[0];
    fileInputImagenes.addEventListener("change", function () {
        _this.colaDeSubida = fileInputImagenes.files;
        _this.indiceFileSubiendo = 0;
        _this.subirProximaImagen(onImagenLista);
    }, false);
    $(fileInputImagenes).click();
};

SubidorDeImagenes.prototype.subirProximaImagen = function (onImagenLista) {
    var _this = this;

    var file = _this.colaDeSubida[_this.indiceFileSubiendo];
    url = window.URL || window.webkitURL;
    src = url.createObjectURL(file);
    var canvas = document.createElement('CANVAS');
    var ctx = canvas.getContext('2d');
    var img = new Image;
    img.crossOrigin = 'Anonymous';
    img.src = src;
    img.onload = function () {
        canvas.height = img.height;
        canvas.width = img.width;
        ctx.drawImage(img, 0, 0);
        var bytes_imagen = canvas.toDataURL('image/jpg');
        bytes_imagen = bytes_imagen.replace(/^data:image\/(png|jpg);base64,/, "");

        Backend.SubirImagen(bytes_imagen).onSuccess(function (id_imagen) {
            _this.indiceFileSubiendo += 1;

            onImagenLista(id_imagen, bytes_imagen);

            if (_this.indiceFileSubiendo >= _this.colaDeSubida.length) return;
            _this.subirProximaImagen(onImagenLista);
        });        
    };
};
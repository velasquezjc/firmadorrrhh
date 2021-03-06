var SubidorDeImagenes = function () {
};

SubidorDeImagenes.prototype.subirImagenes = function (onImagenLista, recortar, max_size) {
    var _this = this;
    var fileInputImagenes = $('<input type="file" multiple />')[0];
    fileInputImagenes.addEventListener("change", function () {
        _this.colaDeSubida = fileInputImagenes.files;
        _this.indiceFileSubiendo = 0;
        _this.subirProximaImagen(onImagenLista, recortar, max_size);
    }, false);
    $(fileInputImagenes).click();
};

SubidorDeImagenes.prototype.subirImagen = function (onImagenLista, recortar, max_size) {
    var _this = this;

    var fileInputImagenes = $('<input type="file" />')[0];
    fileInputImagenes.addEventListener("change", function () {
        _this.colaDeSubida = fileInputImagenes.files;
        _this.indiceFileSubiendo = 0;
        _this.subirProximaImagen(onImagenLista, recortar, max_size);
    }, false);
    $(fileInputImagenes).click();
};

SubidorDeImagenes.prototype.subirProximaImagen = function (onImagenLista, recortar, max_size) {
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
        var width = img.width;
        var height = img.height;

        if (max_size) {
            if (width > height) {
                if (width > max_size) {
                    height *= max_size / width;
                    width = max_size;
                }
            } else {
                if (height > max_size) {
                    width *= max_size / height;
                    height = max_size;
                }
            }
        }

        canvas.width = width;
        canvas.height = height;

        ctx.drawImage(img, 0, 0, width, height);
        var bytes_imagen = canvas.toDataURL('image/jpg');
        bytes_imagen = bytes_imagen.replace(/^data:image\/(png|jpg);base64,/, "");


        //        if (recortar) {
        //            vex.defaultOptions.className = 'vex-theme-os';
        //            vex.open({
        //                afterOpen: function ($vexContent) {
        //                    var vista_imagen = new VistaThumbnail({
        //                        bytes_imagen: bytes_imagen,
        //                        contenedor: $vexContent,
        //                        alRecortar: function (bytes_imagen_recortada) {
        //                            Backend.SubirImagen(bytes_imagen_recortada).onSuccess(function (id_imagen) {
        //                                _this.indiceFileSubiendo += 1;

        //                                onImagenLista(id_imagen, bytes_imagen_recortada);

        //                                if (_this.indiceFileSubiendo >= _this.colaDeSubida.length) return;
        //                                _this.subirProximaImagen(onImagenLista);
        //                            });
        //                        }
        //                    });

        //                    return $vexContent;
        //                },
        //                css: {
        //                    'padding-top': "4%",
        //                    'padding-bottom': "0%"
        //                },
        //                contentCSS: {
        //                    width: "80%",
        //                    height: "80%"
        //                }
        //            });
        //        } else {

        Backend.SubirImagen(bytes_imagen).onSuccess(function (id_imagen) {
            _this.indiceFileSubiendo += 1;

            onImagenLista(id_imagen, bytes_imagen);

            if (_this.indiceFileSubiendo >= _this.colaDeSubida.length) return;
            _this.subirProximaImagen(onImagenLista);
        });

        //        }
    };
};
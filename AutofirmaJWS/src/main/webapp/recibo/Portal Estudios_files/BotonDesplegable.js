
var BotonDesplegable = function (nombre_boton, elemento_desplegable) {
    var _this = this;
    _this.boton = $("#" + nombre_boton);
    _this.elemento = $("#" + elemento_desplegable);

    _this.boton.click(function () {
        _this.elemento.toggle();

    });

    $(document).mouseup(function (e) {

        if (!_this.elemento.is(e.target) && _this.elemento.has(e.target).length === 0 && !_this.boton.is(e.target) && _this.boton.has(e.target).length === 0) {
            _this.elemento.hide();

        }
    });
};

BotonDesplegable.prototype.contraer = function () {
    this.elemento.hide();

};
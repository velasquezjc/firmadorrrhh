
var MenuDesplegable = function (nombre_boton, elemento_desplegable, tooltip, expandir_siempre, mostrar_cantidad) {
    var _this = this;
    this.expandirSiempre = expandir_siempre;
    _this.boton = $("#" + nombre_boton);
    _this.elemento_desplegable = $("#" + elemento_desplegable);
    _this.mostrar_cantidad = mostrar_cantidad;
    _this.contador = _this.boton.find("#contador_rojo");

    _this.boton.click(function () {
        if (_this.cant_elementos_dibujados == 0 && !_this.expandirSiempre) _this.contraer();
        else _this.elemento_desplegable.toggle();
    });

    if (tooltip) {
        _this.boton.opentip(tooltip, {
            removeElementsOnHide: true,
            target: true,
            style: 'dark',
            showOn: "hover",
            hideDelay: 0,
            tipJoint: "bottom right"
        });
    }
    $(document).mouseup(function (e) {

        if (!_this.elemento_desplegable.is(e.target) && _this.elemento_desplegable.has(e.target).length === 0 && !_this.boton.is(e.target) && _this.boton.has(e.target).length === 0) {
            _this.elemento_desplegable.hide();
        }
    });
    this.cant_elementos_dibujados = 0;
};

MenuDesplegable.prototype.contraer = function () {
    this.elemento_desplegable.hide();
};

MenuDesplegable.prototype.agregar = function (elemento_dibujable, cantidad_que_suma) {
    var _this = this;
    this.cant_elementos_dibujados += cantidad_que_suma||1;

    if (this.cant_elementos_dibujados > 0 && this.mostrar_cantidad) this.contador.show();
    this.contador.html(this.cant_elementos_dibujados);              

    elemento_dibujable.dibujarEn(this.elemento_desplegable);
    elemento_dibujable.alQuitar = function () {
        _this.cant_elementos_dibujados -= 1;
        _this.contador.html(_this.cant_elementos_dibujados);
        if (_this.cant_elementos_dibujados == 0) _this.contador.hide();
        if (_this.cant_elementos_dibujados == 0 && !_this.expandirSiempre) _this.contraer();
    };
};

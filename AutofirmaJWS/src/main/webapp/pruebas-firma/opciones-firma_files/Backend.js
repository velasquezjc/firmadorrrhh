var Backend = {
    start: function (on_ready) {
        var _this = this;
        this.proveedor().postearAUrl({
            url: "MetodosDelBackend",
            success: function (objetos) {
                for (var i = 0; i < objetos.length; i++) {
                    _this.agregarMetodo(objetos[i].nombre);
                }
                if (on_ready) on_ready();
            },
            error: function (XMLHttpRequest, textStatus, errorThrown) {
                console.log("error al obtener métodos del backend")
            }
        });
    },
    sync: {},
    proveedor: function () {
        this._proveedor = this._proveedor || new ProveedorAjax();
        return this._proveedor;
    },
    agregarMetodo: function (nombre_metodo) {
        var _this = this;
        this[nombre_metodo] = function () {
            var promesa = new Promesa();
            _this.ejecutar(nombre_metodo, arguments, function (data) { promesa.success(data) }, function (XMLHttpRequest, textStatus, errorThrown) { promesa.error(XMLHttpRequest, textStatus, errorThrown) });
            return promesa;
        };
        this.sync[nombre_metodo] = function () {
            return _this.ejecutarSincronico(nombre_metodo, arguments);
        };
    },
    ejecutar: function (nombre_metodo, args, success, error) {
        var argumentos_json = [];
        for (var i = 0; i < args.length; i++) {
            if (typeof args[i] == "string") argumentos_json.push(args[i]);
            else argumentos_json.push(JSON.stringify(args[i]));
        }
        this.proveedor().postearAUrl({
            url: "EjecutarEnBackend",
            data: {
                nombre_metodo: nombre_metodo,
                argumentos_json: argumentos_json
            },
            success: success,
            error: error
        });
    },
    ejecutarSincronico: function (nombre_metodo, args, on_error) {
        var argumentos_json = [];
        for (var i = 0; i < args.length; i++) {
            if (typeof args[i] == "string") argumentos_json.push(args[i]);
            else argumentos_json.push(JSON.stringify(args[i]));
        }
        var respuesta;
        this.proveedor().postearAUrl({
            url: "EjecutarEnBackend",
            data: {
                nombre_metodo: nombre_metodo,
                argumentos_json: argumentos_json
            },
            async: false,
            success: function (r) {
                respuesta = r;
            },
            error: function (error) {
                if (on_error) on_error(error);
            }
        });
        return respuesta;
    }
};
if (!Array.prototype.forEach) {
    Array.prototype.forEach = function (iterador) {
        var _this = this;
        for (var i = 0; i < this.length; i++) {
            iterador(_this[i]);
        }
    };
}

Array.prototype.find = function (filtro) {
    var _this = this;
    var item_encontrado = undefined;
    this.forEach(function (item) {
        var cumple_todas_las_condiciones = true;
        for (var key in filtro) {
            if (filtro.hasOwnProperty(key)) {
                if (item[key] != filtro[key]) {
                    cumple_todas_las_condiciones = false;
                }
            }
        }
        if (cumple_todas_las_condiciones) {
            item_encontrado = item;
        }
    });
    return item_encontrado;
};

Array.prototype.findAll = function (filtro) {
    var _this = this;
    var items_encontrados = [];
    this.forEach(function (item) {
        var cumple_todas_las_condiciones = true;
        for (var key in filtro) {
            if (filtro.hasOwnProperty(key)) {
                if (item[key] != filtro[key]) {
                    cumple_todas_las_condiciones = false;
                }
            }
        }
        if (cumple_todas_las_condiciones) {
            items_encontrados.push(item);
        }
    });
    return items_encontrados;
};

if (typeof String.prototype.trim !== 'function') {
    String.prototype.trim = function () {
        return this.replace(/^\s+|\s+$/g, '');
    }
}
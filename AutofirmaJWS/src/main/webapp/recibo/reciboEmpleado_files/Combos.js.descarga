//$.fn.extend({
//    cargarCombo: function (coleccion, nombreVal, nombreDescripcion) {
//        var _this = this;
//        _this.empty();
//        coleccion.forEach(function (item) {
//            var option = $("<option value='" + item[nombreVal] + "' item='" + JSON.stringify(item) + "'>" + item[nombreDescripcion] + "</option>");
//            _this.append(option);
//        });
//    },
//    itemSeleccionado: function (id) {
//        if (id !== undefined) {
//            this.val(id);
//            this.attr("item-seleccionado", id);
//        } else {
//            return JSON.parse(this.find("option:selected").attr("item"));
//        }
//    },
//    cargarBusquedaEnRepositorio: function (nombre_repositorio, criterio, id_item, descripcion_item) {
//        var combo = $(this);
//        Repositorio.buscar(nombre_repositorio, criterio, function (items) {
//            combo.cargarCombo(items, id_item, descripcion_item);
//            var id_seleccionado = combo.attr("item-seleccionado");
//            if (id_seleccionado) combo.itemSeleccionado(id_seleccionado);
//        });
//    }
//});

//$(function () {
//    $("[nombre_repositorio]").each(function () {
//        var combo = $(this);
//        var nombre_repositorio = combo.attr("nombre_repositorio");
//        var id_item = combo.attr("id_item");
//        var descripcion_item = combo.attr("descripcion_item");
//        Repositorio.get(nombre_repositorio, function (items) {
//            combo.cargarCombo(items, id_item, descripcion_item);
//            var id_seleccionado = combo.attr("item-seleccionado");
//            if (id_seleccionado) combo.itemSeleccionado(id_seleccionado);
//        });
//    });
//});

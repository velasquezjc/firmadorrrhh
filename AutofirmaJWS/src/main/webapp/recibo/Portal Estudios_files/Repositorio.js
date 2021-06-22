var Repositorio = {
    buscar: function (nombre_repositorio, criterio, success, error) {
        if (!criterio) criterio = {};
        Backend.ejecutar("Buscar" + nombre_repositorio, [criterio], success, error);
    },
    agregar: function (nombre_repositorio, descripcion_item, success, error) {
        Backend.ejecutar("Agregar" + nombre_repositorio, [descripcion_item], success, error);
    },
    agregarConMasDatos: function (nombre_repositorio, parametros, success, error) {
        Backend.ejecutar("Agregar" + nombre_repositorio, parametros, success, error);
    }
};
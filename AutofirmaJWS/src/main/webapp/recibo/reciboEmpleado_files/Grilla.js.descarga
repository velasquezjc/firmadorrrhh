// JavaScript Document
var Grilla = function (columnas) {
    this.columnas = columnas;
    this.start();
};

var GrillaV2 = function (id_componente, definicion_columnas, modelo) {
    id_componente = "#" + "tablaAgentes";

    //var _this = this;
    var divGrilla = $(id_componente);
    divGrilla.empty();

    var columnas = [];
    $.each(definicion_columnas, function (index, col_def) {
        columnas.push(new Columna(col_def.nombre_columna, { generar: col_def.value_getter }));
    });

    var grilla = new Grilla(columnas);
    grilla.SetOnRowClickEventHandler(function (modelo) { });
    grilla.CambiarEstiloCabecera("estilo_tabla_portal");
    grilla.CargarObjetos(modelo);
    grilla.DibujarEn(divGrilla);
    $('.table-hover').removeClass("table-hover");
    //_this.BuscadorDeTabla();
    return grilla;
}

Grilla.prototype = {
    start: function () {
        this.tabla = $('<table>');
        this.tabla.addClass("table");
        this.tabla.addClass("table-striped");
        this.tabla.addClass("table-bordered");
        this.tabla.addClass("table-condensed");
        this.tabla.addClass("table-hover");
        this.tabla.css("cursor", "pointer");

        this.Objetos = [];
        this.filas = [];

        this.crearEncabezado();
        this.crearCuerpo();
        this.crearProgressBar();
        this.registrarIndexOfEnArrays();
    },
    crearEncabezado: function () {
        var tHead = $('<thead>');
        tHead.addClass("detalle_viatico_titulo_tabla_detalle");

        var encabezado = $('<tr>');
        tHead.append(encabezado);
        this.tabla.append(tHead);

        for (var i = 0; i < this.columnas.length; i++) {
            var col = this.columnas[i];
            var th = $("<th>").append(col.titulo);
            th.addClass("sort");
            th.attr("data-sort", col.titulo);
            if (col.titulo == 'id') {
                th.css("display", "none")
            }
            encabezado.append(th);
        }
    },
    agregarBuscador: function () {
        var input = $('<input>');
        input.attr('type', 'text');
        input.attr('placeholder', 'Buscar');
        input.addClass('search');
        input.attr('style', 'width:80px !important;');

        var th = $("<th>").append(input);
        th.attr('colspan', 2);

        var encabezado = this.tabla[0].getElementsByClassName('detalle_viatico_titulo_tabla_detalle'); //
        var celdaBuscador = encabezado[0].firstChild.insertCell(-1); //.append(th);
        celdaBuscador.innerHTML = input[0].outerHTML;
    },
    crearCuerpo: function () {
        var tBody = $('<tbody>');
        tBody.addClass("list");
        this.tabla.append(tBody);
    },
    crearProgressBar: function () {
        this.progress_bar = $('<div>');
        var progress_label = $("<div>");
        progress_label.css("float", "left");
        progress_label.css("margin-left", "40%");
        progress_label.css("margin-top", "5px");
        progress_label.css("font-weight", "bold");

        progress_label.text("Cargando documentos...");

        this.progress_bar.append(progress_label);

        this.progress_bar.progressbar({
            value: false
        });
        this.progress_bar.progressbar("option", "value", false);
        this.mostrando_progress_bar = false;
    },
    registrarIndexOfEnArrays: function () {
        if (!Array.indexOf) {
            Array.prototype.indexOf = function (obj) {
                for (var i = 0; i < this.length; i++) {
                    if (this[i] == obj) {
                        return i;
                    }
                }
                return -1;
            }
        }
    },
    DibujarEn: function (panel) {
        panel.append(this.tabla);
    },

    AgregarEstilo: function (clase) {
        this.tabla.addClass(clase);
    },
    CambiarEstiloCabecera: function (clase) {
        this.tabla[0].tHead.className = clase;
    },

    SetOnRowClickEventHandler: function (metodo) {
        this.onRowClickEventHandler = metodo;
    },
    setProveedorDeDatos: function (proveedor) {
        this.proveedor_de_datos = proveedor;
    },
    refrescar: function () {
        this.BorrarContenido();
        this.mostrarProgressBar();
        var self = this;
        this.proveedor_de_datos.pedirDatos(function (obj) { self.CargarObjetos(obj); });
    },
    mostrarProgressBar: function () {
        this.tabla.after(this.progress_bar);
        this.progress_bar.show();
        this.mostrando_progress_bar = true;
    },
    ocultarProgressBar: function () {
        this.progress_bar.hide();
        this.mostrando_progress_bar = false;
    },
    mostrandoProgressBar: function () {
        return this.mostrando_progress_bar;
    },
    CargarObjetos: function (objetos) {
        this.ocultarProgressBar();
        if (objetos.length > 0) {
            for (var i = 0; i < objetos.length; i++) {
                var obj = objetos[i];
                this.CargarObjeto(obj);
            }
        } else {
            this.CargarFilaSinDatos();
        }
    },

    CargarUnRegistro: function (objetos) {       
        this.CargarObjeto(objetos);       
    },

    CargarObjetoSinDibujar: function (obj) {
        this.Objetos.push(obj);
    },
    CargarFilaSinDatos: function () {
        var tr = $('<tr>');
        var td = $('<td>');
        td.attr('colspan', this.columnas.length).text("No hay datos para mostrar");
        tr.append(td);
        this.tabla.append(tr);
    },
    CrearCelda: function (col, obj) {
        var td = $('<td>');
        if (col.generadorDeContenido.asincronico) {
            col.generadorDeContenido.generar(obj, function (contenido) {
                td.append(contenido);
            });
        } else {
            td.append(col.generadorDeContenido.generar(obj));
        }
        td.addClass(col.titulo.replace(/ /g, "_").replace(/\//g, "_"));
        return td;
    },
    CargarObjeto: function (obj) {
        var tr = $('<tr>');
        for (var i = 0; i < this.columnas.length; i++) {
            tr.append(this.CrearCelda(this.columnas[i], obj));
        }
        //seteo el evento click para la fila
        var self = this;
        tr.click(function () {
            self.desSeleccionarTodo();
            $(this).find("td").addClass('celda_seleccionada');
            self.onRowClickEventHandler(obj);
        });

        this.tabla.append(tr);
        this.Objetos.push(obj);
        this.filas.push({
            fila: tr,
            objeto: obj
        });
    },
    buscarObjetoPorId: function (id) {
        var ind, pos;
        for (ind = 0; ind < this.Objetos.length; ind++) {
            if (this.Objetos[ind].id == id)
                break;
        }
        valor_indice = (ind < this.Objetos.length) ? ind : -1;
        return (this.Objetos[valor_indice]);
    },

    QuitarObjetosExistentes: function (objetos) {
        for (var i = 0; i < objetos.length; i++) {
            var obj = objetos[i];
            if (this.ContieneElemento(obj)) {
                var indice = this.obtenerIndice(this.Objetos, obj);
                this.Objetos.splice(indice, 1);
            }

        }
        return this.Objetos;
    },
    QuitarObjeto: function (tabla, obj) {
        this.tabla.find(".celda_seleccionada").remove();
        var indice = this.Objetos.indexOf(obj);
        this.Objetos.splice(indice, 1);
    },
    EliminarObjeto: function (obj) {
        var indice = this.Objetos.indexOf(obj);
        this.Objetos.splice(indice, 1);
        for (var i = 0; i < this.filas.length; i++) {
            if (this.filas[i].objeto === obj) {
                indice = i;
            }
        }
        $(this.filas[indice].fila).remove();
        this.filas.splice(indice, 1);
    },
    QuitarObjetoSinDibujar: function (obj) {
        var indice = this.Objetos.indexOf(obj);
        this.Objetos.splice(indice, 1);
    },
    ContieneElemento: function (obj) {
        return this.contains(this.Objetos, obj);
    },

    BorrarContenido: function () {
        var rowCount = this.tabla[0].rows.length;
        for (var i = 1; i < rowCount; i++) {
            this.tabla[0].deleteRow(i);
            rowCount--;
            i--;
        }
        this.Objetos = new Array();
    },
    contains: function (a, obj) {
        var i = a.length;
        while (i--) {
            if (a[i] === obj) {
                return true;
            } else if (a[i].Id === obj.Id) {
                return true;
            }
        }
        return false;
    },
    obtenerIndice: function (a, obj) {
        var i = a.length;
        while (i--) {
            if (a[i].Id === obj.Id) {
                return i;
            }
        }
        return -1;
    },
    desSeleccionarTodo: function () {
        var celdas = this.tabla.find("td");
        celdas.removeClass('celda_seleccionada');
        celdas.removeClass('celda_on_hover');
    }
};


var Columna = function (titulo, generadorDeContenido, resumida) {
    this.titulo = titulo;
    this.generadorDeContenido = generadorDeContenido;
}


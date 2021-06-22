var VistaAlerta = function (alerta) {
    var _this = this;
    this.alerta = alerta;
    this.ui = $("#plantillas_barra_menu .ui_mensaje_alerta").clone();
    this.ui.find(".titulo_mensaje_alerta").text(alerta.titulo);
    this.ui.find(".contenido_mensaje_alerta").html(alerta.descripcion);
    this.ui.find("#btn_ok").click(function () {
        Backend.MarcarAlertaComoLeida(alerta.id).onSuccess(function () {
            console.log("marco como leida");
            _this.ui.remove();
            _this.alQuitar();
        });
    });
};

VistaAlerta.prototype.dibujarEn = function (un_panel) {
    un_panel.append(this.ui);
};

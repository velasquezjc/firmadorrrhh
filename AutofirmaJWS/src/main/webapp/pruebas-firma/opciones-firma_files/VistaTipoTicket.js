var VistaTipoTicket = function (descripcion_ticket, cantidad) {
    this.ui = $("#plantillas_barra_menu .ui_tipo_ticket").clone();
    this.ui.find(".nombre_tipo_ticket").text(descripcion_ticket);
    this.ui.find(".cantidad_tickets").text(cantidad);
    var _this = this;

    this.ui.click(function () {
        localStorage.setItem('filtroTickets', descripcion_ticket);
        menu_tareas.contraer();
        window.location.href = window.location.origin + "/Portal/GestionDeTareas.aspx";
    });
};

VistaTipoTicket.prototype.dibujarEn = function (un_panel) {
    un_panel.append(this.ui);
};
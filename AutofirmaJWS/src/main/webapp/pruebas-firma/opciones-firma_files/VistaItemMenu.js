var VistaItemMenu = function (item) {
    this.ui = $("#plantillas_barra_menu .ui_vista_item_menu").clone();
    this.ui.attr('href', item.Acceso.Url);
    //                modulito.text(item.NombreItem);
    this.ui.find('img').attr('src', '../MenuPrincipal/' + item.NombreItem.replace(/ /g, '_') + '.png');
};

VistaItemMenu.prototype.dibujarEn = function (un_panel) {
    un_panel.append(this.ui);
};

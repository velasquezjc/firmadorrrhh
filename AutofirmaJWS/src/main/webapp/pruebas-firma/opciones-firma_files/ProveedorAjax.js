var ProveedorAjax = function (raiz) {
    //this.raiz = "http://localhost:43414/"; //LOCAL
    //this.raiz = "http://www.cltservwebrh.des/"; //LUNA
    this.raiz = "https://rrhh.desarrollosocial.gob.ar/"; //PRODUCCION
};

ProveedorAjax.prototype.postearAUrl = function (datos_del_post) {
    var async = true;
    if (datos_del_post.async !== undefined) async = datos_del_post.async;

    $.ajax({
        url: this.raiz + "AjaxWS.asmx/" + datos_del_post.url,
        type: "POST",
        data: JSON.stringify(datos_del_post.data),
        async: async,
        dataType: "json",
        contentType: "application/json; charset=utf-8",
        success: function (respuestaJson) {
            if (respuestaJson.hasOwnProperty('d')) {
                datos_del_post.success(JSON.parse(respuestaJson.d));
            } else {
                datos_del_post.success(respuestaJson);
            }
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            console.log("error al postear a url:", XMLHttpRequest, textStatus, errorThrown);
            if (datos_del_post.error) datos_del_post.error(XMLHttpRequest, textStatus, errorThrown);
        }
    });
};
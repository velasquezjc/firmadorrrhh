$(document).ready(function () {
    var proveedor_ajax = new ProveedorAjax();
    $('[RequiereFuncionalidad]').each(function (index, control) {

        $(control).hide();
        proveedor_ajax.postearAUrl({ url: "ElUsuarioLogueadoTieneLaFuncionalidad",

            data: {
                id_funcionalidad: $(control).attr('RequiereFuncionalidad')
            },
            success: function (tiene_funcionalidad) {
                if (!tiene_funcionalidad) $(control).remove();
                else {
                    $(control).show();
                }
            },
            error: function (XMLHttpRequest, textStatus, errorThrown) {

            }
        });
    });
});

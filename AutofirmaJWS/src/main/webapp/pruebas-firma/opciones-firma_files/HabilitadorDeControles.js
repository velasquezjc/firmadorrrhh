$(document).ready(function () {
    //var permisos = JSON.parse(localStorage.getItem('permisos'));
    Backend.start(function () {
        Backend.GetFuncionalidadesPerfilesAreas()
            .onSuccess(function (permisos) {


                //FC: traigo todos los elementos del html que tengan el atributo RequiereFuncionalidad
                $('[RequiereFuncionalidad]').each(function (index, control) {

                    $(control).hide();

                    var funcionalidad = $(control).attr('RequiereFuncionalidad');
                    var area = $(control).attr('RequiereArea');
                    //FC: evaluo si los permisos que tiene el usaurio, coincide con la funcionalidad y el area q requiere
                    if (tieneLaFuncionalidad(permisos, funcionalidad) && tieneElArea(permisos, area)) {
                        $(control).show();
                    } else {
                        $(control).remove();
                    }

                });

                function tieneLaFuncionalidad(permisos, funcionalidad) {
                    if (!funcionalidad)
                        return true;

                    for (i = 0; i < permisos.length; i++) {
                        if (permisos[i].Nombre === funcionalidad) {
                            return true;
                        }

                    }

                    return false;
                }

                function tieneElArea(permisos, idArea) {
                    if (!idArea)
                        return true;

                    for (i = 0; i < permisos.length; i++) {
                        for (j = 0; j < permisos[i].Areas.length; j++) {
                            if (permisos[i].Areas[j].Id == parseInt(idArea))
                                return true;
                        }
                    }

                    return false;
                }
            });
    });
    //FC: asi estaba antes del refactor 15-03-2019
   /* var proveedor_ajax = new ProveedorAjax();
    $('[RequiereFuncionalidad]').each(function (index, control) {

        $(control).hide();

        var funcionalidad = $(control).attr('RequiereFuncionalidad')

        proveedor_ajax.postearAUrl({ url: "ElUsuarioLogueadoTienePermisosParaFuncionalidadPorNombre",

            data: {
                nombre_funcionalidad: funcionalidad
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
    });*/
});

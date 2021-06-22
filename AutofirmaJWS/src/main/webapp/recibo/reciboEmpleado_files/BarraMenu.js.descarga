$(document).ready(function () {
    
    Backend.start(function () {
        menu_usuario = new MenuDesplegable("contenedor_imagen_usuario", "contenedor_menu_usuarios","", true);
        menu_aplicaciones = new MenuDesplegable("menu_cuadrados", "contenedor_menu_cuadrados","Módulos", true);
        menu_alertas = new MenuDesplegable("contenedor_imagen_mensajes", "contenedor_menu_mensajes","Alertas", false, true);
        menu_tareas = new MenuDesplegable("contenedor_imagen_tareas", "contenedor_menu_tareas", "Tareas", false, true);

        $("#link_area").click(function () {
                vex.defaultOptions.className = 'vex-theme-os';
                vex.open({
                    afterOpen: function ($vexContent) {
                        var ui = $("#div_mi_area").clone();
                        Legajo.getAreaDeLaPersona();
                        $vexContent.append(ui);
                        ui.show();
                        return ui;
                    }
                })             
            });
        
        $('#boton_home').click(function () {
            Backend.ElUsuarioLogueadoTienePermisosPara(51).onSuccess(function (tiene_permisos) {   
                if(tiene_permisos) window.location.href = '../Portal/Portal.aspx';
                else window.location.href = '../MenuPrincipal/Menu.aspx';
            });
        });

//        var cargar_alertas = function () {
//            $("#contenedor_alertas").empty()
//            Backend.GetConsultasDePortalNoLeidas().onSuccess(function (consultasJSON) {
//                var consultas = JSON.parse(consultasJSON);
//                _.forEach(consultas, function (consulta) {
//                    var ui_consulta = $("#plantillas_barra_menu .ui_mensaje_alerta").clone();
//                    ui_consulta.find(".titulo_mensaje_alerta").text("Título: " + consulta.tipo_consulta + " - Estado: " + consulta.estado);
//                    ui_consulta.find(".contenido_mensaje_alerta").text(consulta.resumen);
//                    $("#contenedor_alertas").append(ui_consulta);

//                    ui_consulta.click(function () {
//                        window.location.href = "../Portal/Consultas.aspx";
//                    });
//                });
//            });  
//        };

        var cargar_alertas = function () {
            $("#contenedor_alertas").empty();            
            Backend.GetMisAlertasPendientes().onSuccess(function (alertas) {                   
                _.forEach(alertas, function (alerta) {
                    var vista = new VistaAlerta(alerta);                    
                    menu_alertas.agregar(vista);                    
                });
            });  
        };

        var cargar_tareas = function () {
            $("#contenedor_tareas").empty()
            $("#menu_tareas").show();
            Backend.getTicketsPorFuncionalidad().onSuccess(function (tickets) {
                var tipos = _.groupBy(tickets, function(t){ return t.tipoTicket.descripcion;});
                                      
                _.forEach(tipos, function (tickets, descripcion) {
                    var vista = new VistaTipoTicket(descripcion, tickets.length);
                    menu_tareas.agregar(vista, tickets.length);           
                });
            });        
        };

        cargar_alertas();
        cargar_tareas();
        //setInterval(cargar_alertas, 20000);

        Backend.GetMenuPara('PRINCIPAL').onSuccess(function (modulos) {
            _.forEach(modulos.Items, function(item){
                var vista = new VistaItemMenu(item);
                menu_aplicaciones.agregar(vista);             
            });
        });

        Backend.GetUsuarioLogueado().onSuccess(function (usuario) {

            document.getElementById('nombre_user').innerHTML = usuario.Owner.Nombre;
            document.getElementById('apellido_user').innerHTML = usuario.Owner.Apellido;
            document.getElementById('dni_user').innerHTML = usuario.Owner.Documento;
            document.getElementById('email_user').innerHTML = usuario.MailRegistro;

            $('#cambiar-constrasena_usuario').click(function () {

                alertify.confirm('Modificar contraseña', '¿Está seguro de querer reinciar la contraseña', function () {
                    Backend.ResetearPassword(usuario.Id).onSuccess(
                        function (nueva_clave) {
                            alertify.alert("Se ha modificado la contraseña.", "La nueva contraseña para el usuario: "
                                                + usuario.Alias + " es: " + nueva_clave);
                        });
                    }
                    , function () {
                        alertify.alert("Modificación cancelada.");
                    }
                ); 
            });


            $("#contenedor_foto_usuario").click(function () {
                vex.defaultOptions.className = 'vex-theme-os';
                vex.open({
                    afterOpen: function ($vexContent) {
                        var ui = $("#plantillas_barra_menu #indicaciones_al_subir_imagen").clone();
                        ui.find("#btn_ok").click(function(){
                            vex.close();
                            var subidor = new SubidorDeImagenes();
                            subidor.subirImagen(function (id_imagen) {
                                Backend.SolicitarCambioDeImagen(id_imagen).onSuccess(function () {
                                    alertify.success("solicitud de cambio de imagen realizada con éxito");
                                });
                            }, true);
                        });
                        $vexContent.append(ui);
                        ui.show();
                        return ui;
                    },
                    css: {
                        'padding-top': "4%",
                        'padding-bottom': "0%"
                    }
                })             
                
            });

            if (usuario.Owner.IdImagen >= 0) {
                var img = new VistaThumbnail({
                    id: usuario.Owner.IdImagen,
                    contenedor: $("#foto_usuario_menu"),                    
                });
                var img2 = new VistaThumbnail({ id: usuario.Owner.IdImagen, contenedor: $("#contenedor_imagen_usuario #imagen") });
                $("#foto_usuario_menu").show();
                $("#contenedor_imagen_usuario #imagen").show();
                $("#foto_usuario_icono").hide();
                $("#foto_usuario_generica").hide();
            }
            else {
                $("#foto_usuario_menu").hide();
                $("#foto_usuario_generica").show();
            }

            var validateEmail = function (mail) {
                if (mail == '' || mail == null) {
                    return false;
                }
                var re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
                return re.test(mail);
            }

            var levantar_prompt = function () {
                alertify.prompt("Ingrese su mail",
                "Para continuar debe ingresar una dirección de correo válida",
                "",
                function (ev, mail) {
                    if (validateEmail(email)) {
                        Backend.ModificarMiMail(mail)
                        .onSuccess(function (ok) {
                            if (ok) {
                                alertify.success("Mail modificado correctamente");
                            } else {
                                alertify.error("Error al modificar el mail");
                            }

                        })
                        .onError(function () {

                            alertify.error("Error al modificar el mail");
                            levantar_prompt();
                        });
                    }
                    else {
                        alertify.error("Mail no valido");
                    }
                },
                function () {
                    setTimeout(function () { levantar_prompt(); }, 100);
                });
            }



            $('#cambiar-email_usuario').click(function () {
                alertify.prompt(' ',
                'Ingrese el mail del usuario',
                 '',
                    function (evt, value) {
                        if (validateEmail(value)) {
                            Backend.ModificarMailRegistro(usuario.Id, value)
                        .onSuccess(function () {

                            alertify.success("Se ha modificado correctamente su mail");
                            alertify.prompt().close();
                            $('#email_user').text(value);

                        })
                        .onError(function () {
                            alertify.error("Error al modificar el mail");
                            alertify.prompt().close();
                        });
                        }
                        else {
                            alertify.error("Los datos ingresados no corresponden a un mail válido. Inténtelo nuevamente");
                        }
                    }

               , function () {

               }).set('labels', {ok:'Aceptar', cancel:'Cancelar'});

            });
        });
    });
});
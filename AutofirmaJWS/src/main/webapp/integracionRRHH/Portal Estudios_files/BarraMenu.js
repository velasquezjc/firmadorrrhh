$(document).ready(function () {
    Backend.start(function () {
        var boton_usuario = new BotonDesplegable("contenedor_imagen_usuario", "contenedor_menu_usuarios");
        var boton_aplicaciones = new BotonDesplegable("menu_cuadrados", "contenedor_menu_cuadrados");
        var boton_mensajes = new BotonDesplegable("menu_mensajes", "contenedor_menu_mensajes");

        $('#boton_home').click(function () {
            Backend.ElUsuarioLogueadoTienePermisosPara(51).onSuccess(function (tiene_permisos) {   
                if(tiene_permisos) window.location.href = '../Portal/Portal.aspx';
                else window.location.href = '../MenuPrincipal/Menu.aspx';
            });
        });

        var cargar_alertas = function () {
            $(".contenedor_de_alertas_y_mensajes").empty()
            Backend.GetConsultasDePortalNoLeidas().onSuccess(function (consultasJSON) {
                var consultas = JSON.parse(consultasJSON);
                _.forEach(consultas, function (consulta) {
                    var ui_consulta = $("#plantillas_barra_menu .ui_mensaje_alerta").clone();
                    ui_consulta.find(".titulo_mensaje_alerta").text("Título: " + consulta.tipo_consulta + " - Estado: " + consulta.estado);
                    ui_consulta.find(".contenido_mensaje_alerta").text(consulta.resumen);
                    $(".contenedor_de_alertas_y_mensajes").append(ui_consulta);

                    ui_consulta.click(function () {
                        window.location.href = "../Portal/Consultas.aspx";

//                        boton_mensajes.contraer();
//                        vex.defaultOptions.className = 'vex-theme-os';
//                        vex.open({
//                            afterOpen: function ($vexContent) {
//                                var ui = $("#contenedor_chat_mensajes");
//                                $vexContent.append(ui);
//                                $('contenedor_de_alertas_y_mensajes').hide();
//                            }, 
//                            contentCSS: {
//                                width: "70%",
//                                height: "80%"
//                            }

//                        });
                    });
                });

                //var resultado = $.grep(consultas, function (consulta) { return consulta.leida; });
                $('#notificacion_punto_rojo').text(consultas.length);
                if (consultas.length == 0) {
                    $('#notificacion_punto_rojo').hide();
                    $('#notificacion_punto_verde').show();
                    
                } else {
                    $('#notificacion_punto_rojo').show();
                    $('#notificacion_punto_verde').hide();
                }

            });

            
            Backend.ElUsuarioLogueadoTienePermisosPara(50).onSuccess(function (tiene_permisos) {
                if (tiene_permisos) {
                    Backend.GetSolicitudesDeCambioDeImagenPendientes().onSuccess(function (solicitudes) {
                        _.forEach(solicitudes, function (solicitud) {
                            var ui_consulta = $("#plantillas_barra_menu .ui_mensaje_alerta").clone();
                            ui_consulta.find(".titulo_mensaje_alerta").text("Solicitud de cambio de imagen pendiente");
                            ui_consulta.find(".contenido_mensaje_alerta").text("Solicitante:" + "(" + solicitud.usuario.Alias.replace(' ', '') + ") " + solicitud.usuario.Owner.Apellido + ", " + solicitud.usuario.Owner.Nombre + " DNI:" + solicitud.usuario.Owner.Documento);

                            ui_consulta.click(function(){
                                boton_mensajes.contraer();
                                $("#plantillas_barra_menu").append($("<div>").load("../Componentes/AdministradorSolicitudCambioImagen.htm", function(){
                                    var admin = new AdministradorSolicitudCambioImagen(solicitud);
                        
                                }));
                            });
                            $(".contenedor_de_alertas_y_mensajes").append(ui_consulta);
                        });
                    });
                }
            });
           
        };

        cargar_alertas();
        //setInterval(cargar_alertas, 20000);

        Backend.GetMenuPara('PRINCIPAL').onSuccess(function (modulos) {

            for (var i = 0; i < modulos.Items.length; i++) {

                var item = modulos.Items[i];
                var modulito = $('<a>')
                modulito.attr('href', item.Acceso.Url);
                //                modulito.text(item.NombreItem);
                var imagen = $('<img>');
                imagen.attr('src', '../MenuPrincipal/' + item.NombreItem.replace(/ /g, '_') + '.png');
                imagen.attr('class', 'redondeo-modulos');
                imagen.attr('style', 'margin: 5px;');
                modulito.append(imagen);
                $('#contenedor_menu_cuadrados').append(modulito);

                //                item.Orden = ;

            }

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
                var subidor = new SubidorDeImagenes();
                subidor.subirImagen(function (id_imagen) {
                    Backend.SolicitarCambioDeImagen(id_imagen).onSuccess(function () {
                        alertify.success("solicitud de cambio de imagen realizada con éxito");
                    });
                });
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
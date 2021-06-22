/*configuracion de la aplicacion de firma*/
var HOST = Constants.URL_BASE_APP;
var storage = "http://www.milocal.com:8080" + "/AlmacenarFirma"; //document.getElementById('storageService').value;
var retrieve = HOST + "/RecuperarFirma";//document.getElementById('retrieveService').value;
var jnlp = Constants.URL_BASE_JNLP;//document.getElementById('jnlpService').value;
MiniApplet.setServlets(storage, retrieve);
MiniApplet.setJnlpService (jnlp); // URL donde esta el generador de JNLP
if (navigator.platform.indexOf("Linux")!=-1 || navigator.platform.indexOf("Mac")!=-1){
//En Mac y Linux se fuerza la utilización de servidores intermedios
MiniApplet.setForceWSMode(true);
}
//dominio desde el que se realiza la llamada al servicio
//MiniApplet.cargarAppAfirma('miniapplet.js');
MiniApplet.cargarAppAfirma(HOST+'valide/js/miniapplet.js',MiniApplet.KEYSTORE_WINDOWS);

/********funciones de la pagina***/
var RECIBOS = (function (window, undefined) {

    function getTiposLiquidacion() {
        Backend.GetTiposLiquidacion()
        .onSuccess(function (tiposLiquidacion) {
            /*tiposLiquidacion es la respuesta*/
            //var options = document.getElementById("cmb_tipo_liquidacion");
            var select = document.getElementById("cmb_tipo_liquidacion");
            var i;
            //**parsear el objeto json y loopear para cargar las opciones*/
            //recupero la respuesta en forma de objeto json
            //este contine Id,Descripcion

            var resp = JSON.parse(tiposLiquidacion);
            var longitud; //tamaño de la lista de tipos de liquidacion
            longitud = Object.keys(resp).length;

            //genero la lista de opciones
            for (i = 0; i < longitud; i++) {
                var option = document.createElement("option"); //creamos el elemento
                option.value = resp[i].Id; //asignamos valores a sus parametros
                option.text = resp[i].Descripcion;
                select.add(option); //insertamos el elemento

            }
            /*usando jquery no me funciona, fix despues*/
            /*$.each(tiposLiquidacion, function () {
            options.append($("<option />").val(this.Id).text(this.Descripcion));
            });*/
        })
        .onError(function (e) {

        });

    }
    
    function getRecibos(tipoLiquidacion, anio, mes) {
        Backend.GetRecibosResumen(tipoLiquidacion, anio, mes)
        .onSuccess(function (recibosResumen) {
            /*recibosResumen es la respuesta*/

            //            lista_recibos_resumen_div = document.getElementById("cmb_tipo_liquidacion");
            var i;
            //recupero la respuesta en forma de objeto json
            //este contine Id_Recibo, legajo,Cuil,Nyap,Nro_Orden

            var resp = JSON.parse(recibosResumen);
            var longitud; //tamaño de la lista resumen de recibos
            longitud = Object.keys(resp).length;

            //genero la lista de recibos a firmar
            for (i = 0; i < longitud; i++) {
                //                var option = document.createElement("option"); //creamos el elemento
                //                option.value = resp[i].Id; //asignamos valores a sus parametros
                //                option.text = resp[i].Descripcion;
                //                select.add(option); //insertamos el elemento

            }
            //guardo la lista de recibos a firmar:
            lista_recibos_resumen = resp;

            //actualizo el mensaje de respuesta
            divMensajeStatus.innerHTML = '&nbsp;';
            divMensajeStatus.innerHTML = '<div class="iconInfo">Se obtubieron <B>' + longitud + '</B> recibos para firmar. </div>';

            //habilito el boton para poder firmar
            btn_firmar.disabled = false;
            btn_firmar.classList.remove('botonGrisadoFirmaM');
            btn_firmar.classList.add('botonFirmaM');

            /*usando jquery no me funciona, fix despues*/
            /*$.each(tiposLiquidacion, function () {
            options.append($("<option />").val(this.Id).text(this.Descripcion));
            });*/
        })
        .onError(function (e) {

        });
    }

    function getIdRecibosSinFirmar(tipoLiquidacion, anio, mes) {
        Backend.GetIdRecibosSinFirmar(tipoLiquidacion, anio, mes)
        .onSuccess(function (recibosResumen) {
            /*recibosResumen es la respuesta*/

            //            lista_recibos_resumen_div = document.getElementById("cmb_tipo_liquidacion");
            var i;
            //recupero la respuesta en forma de objeto json
            //este contine Id_Recibo, legajo,Cuil,Nyap,Nro_Orden

            var resp = JSON.parse(recibosResumen);
            var longitud; //tamaño de la lista resumen de recibos
            longitud = Object.keys(resp).length;

            //genero la lista de recibos a firmar
            for (i = 0; i < longitud; i++) {
                //                var option = document.createElement("option"); //creamos el elemento
                //                option.value = resp[i].Id; //asignamos valores a sus parametros
                //                option.text = resp[i].Descripcion;
                //                select.add(option); //insertamos el elemento

            }
            //guardo la lista de recibos a firmar:
            lista_recibos_resumen = resp;

            //actualizo el mensaje de respuesta
            divMensajeStatus.innerHTML = '&nbsp;';
            divMensajeStatus.innerHTML = '<div class="iconInfo">Se obtubieron <B>' + longitud + '</B> recibos para firmar. </div>';


            //habilito el boton para poder firmar si hay mas de un recibo para firmar
            if (lista_recibos_resumen.length != 0) {
               // divMensajeStatus.innerHTML = '<div class="iconInfo">Debe existir al menos un documento para firmar.</div>';
                btn_firmar.disabled = false;
                btn_firmar.classList.remove('botonGrisadoFirmaM');
                btn_firmar.classList.add('botonFirmaM');
            }
            

            /*usando jquery no me funciona, fix despues*/
            /*$.each(tiposLiquidacion, function () {
            options.append($("<option />").val(this.Id).text(this.Descripcion));
            });*/
        })
        .onError(function (e) {

        });
    }


    //descarga archivo ya codificado en base64
    function downloadRemoteDataB64POSTXXXXXXXYO(url, params, successFunction, errorFunction) {

        downloadSuccessFunction = successFunction;
        downloadErrorFunction = errorFunction;

        var httpRequest = Generales.getHttpRequest();
        httpRequest.open("POST", url, true);
        httpRequest.setRequestHeader("Content-type", "application/x-www-form-urlencoded");

        httpRequest.onreadystatechange = function (evt) {
            //if (httpRequest.readyState == 4 && httpRequest.status == 200) {
            //	if (downloadSuccessFunction) {	document.getElementById('result').value =httpRequest.responseText; 				
            //en el caso de los archivos estos ya vienen en b64 porque aun no encontre una funcion de conversion a b64 que codifique correctamente desde javascript
            //downloadSuccessFunction(httpRequest.responseText);
            //	}
            if (httpRequest.readyState === httpRequest.DONE) {
                if (httpRequest.status === 200) {
                    if (downloadSuccessFunction) {
                        //en el caso de los archivos estos ya vienen en b64 porque aun no encontre una funcion de conversion a b64 que codifique correctamente desde javascript
                        downloadSuccessFunction(httpRequest.responseText);
                    }
                }
            }
            //}
        }
        httpRequest.onerror = function (e) {
            if (downloadErrorFunction) {
                downloadErrorFunction(e);
            }
            //				else {
            //					console.log("Error en la descarga de los datos. No se invoca a ninguna funcion.");
            //				}
        }
        httpRequest.send(params);
    }
    //Modo actual
    function downloadRemoteDataB64POSTEmpleador(url, idRecibo, params, successFunction, errorFunction) {

        downloadSuccessFunction = successFunction;
        downloadErrorFunction = errorFunction;

        Backend.GetReciboPDFEmpleador(params)
                .onSuccess(function (res) {

                   //en el caso de los archivos estos ya vienen en b64 porque aun no encontre una funcion de conversion a b64 que codifique correctamente desde javascript
                   downloadSuccessFunction(res,idRecibo);
                })
            .onError(function (e) {
                   downloadErrorFunction(e);
            });

        
    }

    function downloadRemoteDataB64POSTEmpleado(url, idRecibo, params, successFunction, errorFunction) {

        downloadSuccessFunction = successFunction;
        downloadErrorFunction = errorFunction;

        Backend.GetReciboPDFEmpleado(params)
                .onSuccess(function (res) {

                    //en el caso de los archivos estos ya vienen en b64 porque aun no encontre una funcion de conversion a b64 que codifique correctamente desde javascript
                    downloadSuccessFunction(res, idRecibo);
                })
            .onError(function (e) {
                downloadErrorFunction(e);
            });


    }

    function guardarReciboPDFFirmado(idRecibo, signatureB64, anio, mes, tipoLiquidacion, successFunction, errorFunction) {

        saveSuccessFunction = successFunction;
        saveErrorFunction = errorFunction;

        Backend.GuardarReciboPDFFirmado(signatureB64,idRecibo,anio,mes,tipoLiquidacion)
                .onSuccess(function (res) {

                    //en el caso de los archivos estos ya vienen en b64 porque aun no encontre una funcion de conversion a b64 que codifique correctamente desde javascript
                    saveSuccessFunction(res);
                })
            .onError(function (e) {
                saveErrorFunction(e);
            });


    }

    function firmarRecibosxxxx(lista_recibos_resumen) {
        Backend.firmarRecibos(tipoLiquidacion, anio, mes)
        .onSuccess(function (recibosResumen) {
            /*recibosResumen es la respuesta*/

            //            lista_recibos_resumen_div = document.getElementById("cmb_tipo_liquidacion");
            var i;
            //recupero la respuesta en forma de objeto json
            //este contine Id_Recibo, legajo,Cuil,Nyap,Nro_Orden

            var resp = JSON.parse(recibosResumen);
            var longitud; //tamaño de la lista resumen de recibos
            longitud = Object.keys(resp).length;

            //genero la lista de recibos a firmar
            for (i = 0; i < longitud; i++) {
                //                var option = document.createElement("option"); //creamos el elemento
                //                option.value = resp[i].Id; //asignamos valores a sus parametros
                //                option.text = resp[i].Descripcion;
                //                select.add(option); //insertamos el elemento

            }
            //guardo la lista de recibos a firmar:
            lista_recibos_resumen = resp;

            //actualizo el mensaje de respuesta
            divMensajeSign.innerHTML = '&nbsp;';
            divMensajeSign.innerHTML = '<div class="iconInfo">Se obtubieron <B>' + longitud + '</B> recibos para firmar. </div>';

            //habilito el boton para poder firmar
            btn_firmar.disabled = false;
            btn_firmar.classList.remove('botonGrisadoFirmaM');
            btn_firmar.classList.add('botonFirmaM');

            /*usando jquery no me funciona, fix despues*/
            /*$.each(tiposLiquidacion, function () {
            options.append($("<option />").val(this.Id).text(this.Descripcion));
            });*/
        })
        .onError(function (e) {

        });
    }

    /* Metodos que publicamos del objeto RECIBOS */
    return {
        getTiposLiquidacion: getTiposLiquidacion,
        getRecibos: getRecibos,
        downloadRemoteDataB64POSTEmpleador: downloadRemoteDataB64POSTEmpleador,
        downloadRemoteDataB64POSTEmpleado: downloadRemoteDataB64POSTEmpleado,
        guardarReciboPDFFirmado: guardarReciboPDFFirmado,
        getIdRecibosSinFirmar: getIdRecibosSinFirmar
    }

})(window, undefined);

	

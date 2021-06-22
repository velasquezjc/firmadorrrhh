var Generales = ( function ( window, undefined ) {
		
	//esta funcion de llamadas ajax asincronas esta repetida en miniapplet
	//pero considero que deberia ser una funcion de ayuda
	function getHttpRequest() {
        var xmlHttp = null;
        if (typeof XMLHttpRequest != "undefined") {	// Navegadores actuales
            xmlHttp = new XMLHttpRequest();
        } else if (typeof window.ActiveXObject != "undefined") {	// Internet Explorer antiguos
            try {
                xmlHttp = new ActiveXObject("Msxml2.XMLHTTP.4.0");
            } catch (e) {
                try {
                    xmlHttp = new ActiveXObject("MSXML2.XMLHTTP");
                } catch (e) {
                    try {
                        xmlHttp = new ActiveXObject("Microsoft.XMLHTTP");
                    } catch (e) {
                        xmlHttp = null;
                    }
                }
            }
        }
        return xmlHttp;
	}
	
	//oculto todos los elementos con una dada clase y muestro solo el elemento
	//con identificador id
	function mostrarPanelDerYOcultarClase(id,clase){	
		
		var x = document.getElementsByClassName(clase);
		var i;
		for (i = 0; i < x.length; i++) {
		    x[i].style.display="none";
		}
		//muestro el elemento con id
		document.getElementById(id).style.display="inline";
	}
	
	
	
	/* Metodos que publicamos del objeto Generales */
	return {
		getHttpRequest : getHttpRequest,
		mostrarPanelDerYOcultarClase : mostrarPanelDerYOcultarClase
	}
	
})(window, undefined);

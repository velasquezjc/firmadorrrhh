
var Constants = {
		
	// IMPORTANTE: PARA PRUEBAS, USAR SIEMPRE UNA IP O NOMBRE DE DOMINIO, NUNCA 'LOCALHOST' O '127.0.0.1'
	// SI NO SE HACE ASI, AUTOFIRMA BLOQUEARA LA FIRMA POR SEGURIDAD
	
		/* PRODUCCION
		
	URL_BASE_APPLE : "https://valide.gov.ar/firmaMovil",

	URL_BASE_SERVICES : "https://valide.gov.ar/firmaMovil"
	*/

		/* PREPRODUCCION
		
	URL_BASE_APPLE : "http://prevalide.gov.ar/firmaMovil",

	URL_BASE_SERVICES : "http://prevalide.gov.ar/firmaMovil"
	*/

		/* DESARROLLO */
		/*NOTA: recordar que si se modifica este js, el navegador no toma los cambios porque cachea los js
		 * se puede o borrar la cache del navegador o harcodear la variable en la pagina que usa el js*/
		/*TRABAJO: como no se soporta jnlp hay que trabajar con https
		 * URL_BASE_JNLP:"https://www.milocal.com:8443/AutofirmaJWS/autofirma.jsp"*/

//    URL_BASE_APP: "http://localhost:43414/",  //este es el enlace para desarrollo
    URL_BASE_APP: "https://rrhh.desarrollosocial.gob.ar/",
		/*para que en el server se pueda ejecutar jnlp por https hay que configurarlo
		 * sino el navegador no ejecutara la app por considerar cors de http a https
		 * la diferencia es que si con el protocolo jnlp se lanza la aplicacion directamente
		 * en cambio con https se lanza primero el cartel de pregunta si desea abrir, guardar,etc
		 * igualmente el cartel anterior da la opcion de siempre realizar la misma accion con
		 * los archivos de ese tipo, deberia ser jnlp://www.milocal.com:8443/autofirma.jsp
		 * */
		//Para facu que soporta jnlp   
		//URL_BASE_JNLP:"jnlp://www.milocal.com:8080/AutofirmaJWS/autofirma.jsp"
//    URL_BASE_JNLP: "http://localhost:43414/FirmaDigital/autofirmaJNLP.aspx", //este es el enlace para desarrollo
    URL_BASE_JNLP: "https://rrhh.desarrollosocial.gob.ar/FirmaDigital/autofirmaJNLP.aspx",	
		URL_BASE_TRIFASICA:"http://www.milocal.com:8081/afirma-server-triphase-signer/SignatureService",
	
		URL_BASE_SERVICES : "https://www.milocal.com:8444/ValidacionWS/services/",
		SERVICE_VALIDECERT : "https://www.milocal.com:8444/ValidacionWS/services/validacionCert/valideCert",
		SERVICE_VALIDESIGN : "https://www.milocal.com:8444/ValidacionWS/services/validacionSign/valideSign"
		
};
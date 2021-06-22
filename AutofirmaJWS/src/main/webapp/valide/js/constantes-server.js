
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
		
		URL_BASE_APP : "https://secure-jws-app-clientefirma.7e14.starter-us-west-2.openshiftapps.com/AutofirmaJWS",
		/*para que en el server se pueda ejecutar jnlp por https hay que configurarlo
		 * sino el navegador no ejecutara la app por considerar cors de http a https
		 * la diferencia es que si con el protocolo jnlp se lanza la aplicacion directamente
		 * en cambio con https se lanza primero el cartel de pregunta si desea abrir, guardar,etc
		 * igualmente el cartel anterior da la opcion de siempre realizar la misma accion con
		 * los archivos de ese tipo, deberia ser jnlp://www.milocal.com:8443/autofirma.jsp
		 * */
		//URL_BASE_JNLP:"jnlp://www.milocal.com:8080/autofirma.jsp"
		URL_BASE_JNLP:"jnlp://secure-jws-app-clientefirma.7e14.starter-us-west-2.openshiftapps.com/AutofirmaJWS/autofirma-server.jsp",
		//URL_BASE_SERVICES : "http://www.milocal.com:8081"
		URL_BASE_TRIFASICA:"https://secure-jws-app-clientefirma.7e14.starter-us-west-2.openshiftapps.com/afirma-server-triphase-signer/SignatureService",
			
		URL_BASE_SERVICES : "https://secure-jws-app-clientefirma.7e14.starter-us-west-2.openshiftapps.com/ValidacionWS/services/",
		SERVICE_VALIDECERT : "https://secure-jws-app-clientefirma.7e14.starter-us-west-2.openshiftapps.com/ValidacionWS/services/validacionCert/valideCert",
		SERVICE_VALIDESIGN : "https://secure-jws-app-clientefirma.7e14.starter-us-west-2.openshiftapps.com/ValidacionWS/services/validacionSign/valideSign"

};
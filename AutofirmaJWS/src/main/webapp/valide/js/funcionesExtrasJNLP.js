var AppJNLP = ( function ( window, undefined ) {
	
	//descarga de la aplicacion jnlp sin lanzarla a ejecucion
	function downloadAppJnlp (jnlpServiceAddress) {
		//pregunta si tiene el js para crear un iframe para obligar a descargar el recurso
		//sin afectar a la pagina actual, en caso de error se intenta descargar el recurso
		//afectando la pagina actual
		if(window.protocolCheck){
		  //console.log("Probamos afirma://");
		  	  console.log("Probamos la descarga de la aplicacion jnlp://");
			  //alert("No dispone de AutoFirma instalado. Se va a probar la versiÃ³n online");
			  //setTimeout(window.focus,0);
			  window.protocolCheck(jnlpServiceAddress.replace("jnlp","https"),null,
			           function () {
				  		   console.log("Probamos http://");
				  		   bJNLP = false;
			               openUrl(jnlpServiceAddress.replace("jnlp","https"));
			           });
		}else{
			//no se descarga nada	
		}

	}
	
	/* Metodos que publicamos del objeto AppJnlp */
	return {
		downloadAppJnlp : downloadAppJnlp
	}
	
})(window, undefined);

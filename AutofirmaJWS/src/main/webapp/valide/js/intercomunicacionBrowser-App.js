        function saveSignature() {
			MiniApplet.saveDataToFile(
					document.getElementById('result').value,
					"Guardar firma electronica",
					null,
					null,
					null);
		}

		/*function showResultCallback(signatureB64, certificateB64) {
			showLog("Firma OK");
			document.getElementById('result').value = signatureB64;
		}*/

		function showCertCallback(certificateB64) {
			showLog("Certificado seleccionado");
			document.getElementById('result').value = certificateB64;
		}
		
		/*function showErrorCallback(errorType, errorMessage) {
			showLog("Type: " + errorType + "\nMessage: " + errorMessage);
		}*/

		function doSign() {
			
				//var data = "abc";//data igual a vacio lanza una seleccion de archivo, pero se firman los datos del archivo no el hash
				var data = document.getElementById("data").value;

				MiniApplet.sign(
					(data != undefined && data != null && data != "") ? data : null,
					document.getElementById("algorithm").value,
					document.getElementById("format").value,
					document.getElementById("params").value,
					successCallback,
					errorCallback);
				
			
		}
        //firmar y salvar
		function doSignAndSave() {
			try {
				//var data = "abc";//data igual a vacio lanza una seleccion de archivo, pero se firman los datos del archivo no el hash
				var data = document.getElementById("data").value;

				//el codigo de operacion puede ser SIGN, COSIGN,COUNTERSIGN
				MiniApplet.signAndSaveToFile(
					"SIGN",
					(data != undefined && data != null && data != "") ? data : null,
					document.getElementById("algorithm").value,
					document.getElementById("format").value,
					document.getElementById("params").value,
					"archivoFirmado.pdf",
					successCallback,
					errorCallback);
				
			} catch(e) {
				try {
					showLog("Type: " + MiniApplet.getErrorType() + "\nMessage: " + MiniApplet.getErrorMessage());
				} catch(ex) {
					showLog("Error: " + e);
				}
			}
		}
		//descarga una url y la firma
		function downloadAndSign() {
			try {
				var data = document.getElementById("urlParam").value;
				MiniApplet.downloadRemoteData(
						(data != undefined && data != null && data != "") ? data : document.location,
						downloadedSuccessCallback,
						downloadedErrorCallback);
			} catch(e) {
				showLog("Error en la descarga de los datos: " + e);
			}
		}
		
		function downloadedSuccessCallback(data) {
			document.getElementById("data").value = data;
			doSign();
		}
		
		function downloadedErrorCallback(e) {
			showLog("Error en la descarga de los datos: " + e);
		}
		
		function doSignBatch() {
			try {
				var batch = createBatchConfiguration();

				MiniApplet.signBatch(
					MiniApplet.getBase64FromText(batch),
					HOST + '/afirma-server-triphase-signer/BatchPresigner', //$NON-NLS-1$
					HOST + '/afirma-server-triphase-signer/BatchPostsigner', //$NON-NLS-1$
					document.getElementById("params").value,
					successCallback,
					errorCallback);

			} catch(e) {
				try {
					showLog("Type: " + MiniApplet.getErrorType() + "\nMessage: " + MiniApplet.getErrorMessage());
				} catch(ex) {
					showLog("Error: " + e);
				}
			}
		}

		function createBatchConfiguration() {

			var config1 = MiniApplet.getBase64FromText("FileName=C:/salida/batch/FIRMA1.xml");
			var config2 = MiniApplet.getBase64FromText("FileName=C:/salida/batch/FIRMA2.xml");

			return "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n" + //$NON-NLS-1$
			"<signbatch stoponerror=\"false\" algorithm=\"SHA256withRSA\">\r\n" + //$NON-NLS-1$
			" <singlesign Id=\"7725374e-728d-4a33-9db9-3a4efea4cead\">\r\n" + //$NON-NLS-1$
			"  <datasource>SG9sYSBNdW5kbw==</datasource>\r\n" + //$NON-NLS-1$
			"  <format>XAdES</format>\r\n" + //$NON-NLS-1$
			"  <suboperation>sign</suboperation>\r\n" + //$NON-NLS-1$
			"  <extraparams>Iw0KI1RodSBBdWcgMTMgMTY6Mjk6MDUgQ0VTVCAyMDE1DQpTaWduYXR1cmVJZD03NzI1Mzc0ZS03MjhkLTRhMzMtOWRiOS0zYTRlZmVhNGNlYWQNCg==</extraparams>\r\n" + //$NON-NLS-1$
			"  <signsaver>\r\n" + //$NON-NLS-1$
			"   <class>es.gob.afirma.signers.batch.SignSaverFile</class>\r\n" + //$NON-NLS-1$
			"   <config>" + config1 + "</config>\r\n" + //$NON-NLS-1$
			"  </signsaver>\r\n" + //$NON-NLS-1$
			" </singlesign>\r\n" + //$NON-NLS-1$
 			" <singlesign Id=\"93d1531c-cd32-4c8e-8cc8-1f1cfe66f64a\">\r\n" + //$NON-NLS-1$
			"  <datasource>SG9sYSBNdW5kbw==</datasource>\r\n" + //$NON-NLS-1$
			"  <format>CAdES</format>\r\n" + //$NON-NLS-1$
			"  <suboperation>sign</suboperation>\r\n" + //$NON-NLS-1$
			"  <extraparams>cG9saWN5SWRlbnRpZmllcj11cm46b2lkOjIuMTYuNzI0LjEuMy4xLjEuMi4xLjlcbnBvbGljeVF1YWxpZmllcj1odHRwczovL3NlZGUuMDYwLmdvYi5lcy9wb2xpdGljYV9kZV9maXJtYV9hbmV4b18xLnBkZlxucG9saWN5SWRlbnRpZmllckhhc2hBbGdvcml0aG09aHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnI3NoYTFcbnBvbGljeUlkZW50aWZpZXJIYXNoPUc3cm91Y2Y2MDArZjAzci9vMGJBT1E2V0FzMD0=</extraparams>\r\n" + //$NON-NLS-1$
			// cG9saWN5SWRlbnRpZmllcj11cm46b2lkOjIuMTYuNzI0LjEuMy4xLjEuMi4xLjlcbnBvbGljeVF1YWxpZmllcj1odHRwczovL3NlZGUuMDYwLmdvYi5lcy9wb2xpdGljYV9kZV9maXJtYV9hbmV4b18xLnBkZlxucG9saWN5SWRlbnRpZmllckhhc2hBbGdvcml0aG09aHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnI3NoYTFcbnBvbGljeUlkZW50aWZpZXJIYXNoPUc3cm91Y2Y2MDArZjAzci9vMGJBT1E2V0FzMD0=
			"  <signsaver>\r\n" + //$NON-NLS-1$
			"   <class>es.gob.afirma.signers.batch.SignSaverFile</class>\r\n" + //$NON-NLS-1$
			"   <config>" + config2 + "</config>\r\n" + //$NON-NLS-1$
			"  </signsaver>\r\n" + //$NON-NLS-1$
			" </singlesign>\r\n" + //$NON-NLS-1$
			"</signbatch>"; //$NON-NLS-1$
						
		}
		
		function doCoSign() {
			try {
				var signature = document.getElementById("signature").value;
				var data = document.getElementById("data").value;

				MiniApplet.coSign(
					(signature != undefined && signature != null && signature != "") ? signature : null,
					(data != undefined && data != null && data != "") ? data : null,
					document.getElementById("algorithm").value,
					document.getElementById("format").value,
					document.getElementById("params").value,
					successCallback,
					errorCallback);

			} catch(e) {
				showLog("Type: " + MiniApplet.getErrorType() + "\nMessage: " + MiniApplet.getErrorMessage());
			}
		}

		function doCounterSign() {
			try {
				var signature = document.getElementById("signature").value;

				MiniApplet.counterSign(
					(signature != undefined && signature != null && signature != "") ? signature : null,
					document.getElementById("algorithm").value,
					document.getElementById("format").value,
					document.getElementById("params").value,
					successCallback,
					errorCallback);
			} catch(e) {
				showLog("Type: " + MiniApplet.getErrorType() + "\nMessage: " + MiniApplet.getErrorMessage());
			}
		}

		function doSelectCert() {
			try { 
				MiniApplet.selectCertificate(
					document.getElementById("params").value,
					showCertCallback,
					errorCallback);
			} catch(e) {
				showLog("Type: " + MiniApplet.getErrorType() + "\nMessage: " + MiniApplet.getErrorMessage());
			}
		}

		function showAppletLog() {
			try {
				showLog(MiniApplet.getCurrentLog());
			} catch(e) {
				showLog("Type: " + MiniApplet.getErrorType() + "\nMessage: " + MiniApplet.getErrorMessage());
			}
		}

		/**
		 * Funcion para la carga de un fichero. Almacena el contenido del fichero en un campo oculto y muestra su nombre.
		 * LA CARGA INDEPENDIENTE DE FICHEROS DEBE EVITARSE EN LA MEDIDA DE LO POSIBLE. Si deseas firmar, cofirmar o contrafirmar
		 * un fichero, llama al metodo correspondiente (sign(), coSign() o counterSign()) sin indicar los datos.
		 * El uso del metodo de carga no sera compatible con el Cliente movil.
		 */
		function browse(title, dataField, textDiv) {
			try {
				var filenameDataB64 = MiniApplet.getFileNameContentBase64(title, null, null, null);
				if (filenameDataB64 == null) {
					return;
				}

				var dataB64;
				var separatorIdx = filenameDataB64.indexOf("|");
				if ((separatorIdx + 1) < filenameDataB64.length) {
					textDiv.innerHTML = filenameDataB64.substring(0, separatorIdx);
					dataField.value = filenameDataB64.substring(separatorIdx + 1);
				} else {
					// El fichero no contenia datos
					return;
				}
			} catch(e) {
				showLog("Type: " + MiniApplet.getErrorType() + "\nMessage: " + MiniApplet.getErrorMessage());
			}
		}
		
		function cleanDataField(dataField, textDiv) {
			textDiv.innerHTML = "";
			dataField.value = null;
		}
		
		function addExtraParam(extraParam) {
			var paramsList = document.getElementById("params");
			paramsList.value = paramsList.value + extraParam + "\n";
			document.getElementById('newParam').value = "";
		}
		
		function cleanExtraParams() {
			document.getElementById("params").value = "";
			document.getElementById('newParam').value = "";
		}
		
		function showLog(newLog) {//luego ver el filtrado de errores para ver que se muestra y que no en el estatus
			//en esta version no se muesta nada,pero esta fun se usa en el index asi que la dejo
			//tambien deberia cambiar esta fun para los ejemplos de integracion
			document.getElementById('console').value = document.getElementById('console').value + "\n" + newLog;
			//document.getElementById('divmensaje').value = newLog;
			
		}
		
	
		
		
		
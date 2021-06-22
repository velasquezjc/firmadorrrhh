
if (document.all && !window.setTimeout.isPolyfill) {
	var __nativeST__ = window.setTimeout;
	window.setTimeout = function (vCallback, nDelay /*, argumentToPass1, argumentToPass2, etc. */) {
		var aArgs = Array.prototype.slice.call(arguments, 2);
		return __nativeST__(vCallback instanceof Function ? function () {
			vCallback.apply(null, aArgs);
		} : vCallback, nDelay);
	};
	window.setTimeout.isPolyfill = true;
}

var MiniApplet = ( function ( window, undefined ) {

		var VERSION = "1.3";
		
		var JAR_NAME = 'miniapplet-full_1_3.jar';

		var JAVA_ARGUMENTS = '-Xms512M -Xmx512M ';
		
		var SYSTEM_PROPERTIES = null;
		
		var clienteFirma = null;

		var codeBase = null;

		var storageServletAddress = null;
		
		var retrieverServletAddress = null;
		
		var clientType = null;
		
		var severeTimeDelay = false;

		var selectedLocale = null;

		var defaultKeyStore = null;
		
		/* ------------------------------------------------ */
		/* Constantes para la operacion interna del Cliente */
		/* ------------------------------------------------ */
		
		/* Longitud maxima de una URL en Android para la invocacion de una aplicacion nativa. */
		var MAX_LONG_ANDROID_URL = 2000;
		
		/* Longitud maxima de una URL en iOS para la invocacion de una aplicacion nativa. */
		var MAX_LONG_IOS_URL = 80000;
		
		/* Longitud maxima de una URL en Windows 8 para la invocacion de una aplicacion nativa. */
		var MAX_LONG_WINDOWS8_URL = 2000;
		
		/* Longitud maxima que generalmente se permite a una URL. */
		var MAX_LONG_GENERAL_URL = 2000;

		/* Tamano del buffer con el que se pasa informacion al applet */
		var BUFFER_SIZE = 2 * 1024 * 1024;
		
		/* Cadena que determina el fin de una respuesta */
		var EOF = "%%EOF%%";
		
		/* Identifica que se utilizara el MiniApplet. */
		var TYPE_APPLET = "APPLET";
		
		/* Identifica que se utilizara una aplicacion nativa de firma. */
		var TYPE_JAVASCRIPT_WEB_SERVICE = "JAVASCRIPT_WEB_SERVICE";
		var TYPE_JAVASCRIPT_SOCKET = "JAVASCRIPT_SOCKET";

		/* --------------------------------- */
		/* Constantes publicas		         */
		/* --------------------------------- */

		/* Almacenes de certificados */

		var KEYSTORE_WINDOWS = "WINDOWS";

		var KEYSTORE_APPLE = "APPLE";
		
		var KEYSTORE_PKCS12 = "PKCS12";

		var KEYSTORE_PKCS11 = "PKCS11";

		var KEYSTORE_FIREFOX = "MOZ_UNI";
		
		var KEYSTORE_JAVA = "JKS";
		
		var KEYSTORE_JCEKS = "JCEKS";
		
		var KEYSTORE_JAVACE = "CaseExactJKS";

		/* Valores para la configuracion de la comprobacion de tiempo */

		var CHECKTIME_NO = "CT_NO";

		var CHECKTIME_RECOMMENDED = "CT_RECOMMENDED";

		var CHECKTIME_OBLIGATORY = "CT_OBLIGATORY";

		/* ------------------------------------ */
		/* Funciones de comprobacion de entorno */
		/* ------------------------------------ */
		
		/**
		 * Determina con un boolean si nuestro cliente es Android
		 */
		function isAndroid() {
			return navigator.userAgent.toUpperCase().indexOf("ANDROID") != -1 ||
				navigator.appVersion.toUpperCase().indexOf("ANDROID") != -1 ||
				// Para la deteccion de los Kindle Fire
				navigator.userAgent.toUpperCase().indexOf("SILK/") != -1 ||
				navigator.userAgent.toUpperCase().indexOf("KFJWI") != -1 ||
				navigator.userAgent.toUpperCase().indexOf("KFJWA") != -1 ||
				navigator.userAgent.toUpperCase().indexOf("KFTT") != -1 ||
				navigator.userAgent.toUpperCase().indexOf("KFOT") != -1 ||
				navigator.userAgent.toUpperCase().indexOf("KINDLE FIRE") != -1
				;
		}
		
		/**
		 * Determina con un boolean si nuestro cliente es iOS.
		 */
		function isIOS() {
			return (navigator.userAgent.toUpperCase().indexOf("IPAD") != -1) ||
			(navigator.userAgent.toUpperCase().indexOf("IPOD") != -1) ||
			(navigator.userAgent.toUpperCase().indexOf("IPHONE") != -1);
		}

		/** Determina con un boolean si nos encontramos en Windows 8/8.1 */
		function isWindows8() {
			return navigator.userAgent.indexOf("Windows NT 6.2") != -1 ||	/* Windows 8 */
				navigator.userAgent.indexOf("Windows NT 6.3") != -1;		/* Windows 8.1 */
		}

		/** Determina con un boolean si nos encontramos en Windows RT */
		function isWindowsRT() {
			return isWindows8() && navigator.userAgent.indexOf("ARM;") != -1;
		}

		/** Determina con un boolean si estamos en Internet Explorer */
		function isInternetExplorer() {
			return !!(navigator.userAgent.match(/MSIE/))	/* Internet Explorer 10 o inferior */
					|| !!(navigator.userAgent.match(/Trident/) && navigator.userAgent.match(/rv:11/)); /* Internet Explorer 11 o superior */
		}

		function isFirefoxUAM() {
		    return navigator.userAgent.indexOf("UAM") > 0;
		}

		/**
		 * Determina con un boolean si se accede a la web con Chrome
		 */
		function isChrome() {
			return navigator.userAgent.toUpperCase().indexOf("CHROME") != -1 ||
				navigator.userAgent.toUpperCase().indexOf("CHROMIUM") != -1;
		}

		function isURLTooLong(url) {
			if (isAndroid()) {
				return url.length > MAX_LONG_ANDROID_URL;
			}
			else if (isIOS()) {
				return url.length > MAX_LONG_IOS_URL;
			}
			else if (isWindows8()) {
				return url.length > MAX_LONG_WINDOWS8_URL;
			}
			return url.length > MAX_LONG_GENERAL_URL;
		}

		/** Indica si el navegador detecta Java. Este valor no es completamente fiable, ya que
		 * Internet Explorer siempre indica que si esta activado. */
		function isJavaEnabled() {
			return navigator.javaEnabled();
		}

		/** Comprueba si una cadena de texto es una URL (http/https). La alternativa implicaria ser un Base64. */ 
		function isValidUrl(data) { 
			return data != null && data.length > "https://".length &&
				("http:" == data.substr(0, 5) || "https:" == data.substr(0, 6));
		}

		function downloadRemoteData(url) {
			var req = getHttpRequest();
			req.open("GET", url, false);
			req.overrideMimeType('text\/plain; charset=x-user-defined');
			req.send();
			
			if (req.readyState != 4 || req.status != 200) {
				throw new Exception();
		    }
			return Base64.encode(req.responseText);
		}

		function getHttpRequest() {
			var activexmodes=["Msxml2.XMLHTTP", "Microsoft.XMLHTTP"]; //activeX versions to check for in IE
			if (window.ActiveXObject){ //Test for support for ActiveXObject in IE first (as XMLHttpRequest in IE7 is broken)
				for (var i=0; i<activexmodes.length; i++) {
					try {
						return new ActiveXObject(activexmodes[i]);
					}
					catch(e) {
						//suppress error
					}
				}
			}
			else if (window.XMLHttpRequest) { // if Mozilla, Safari etc
				return new XMLHttpRequest();
			}
			else {
				return false;
			}
		}

		/** Permite habilitar la comprobacion de la hora local contra la hora del servidor y
		 * establecer un tiempo maximo permitido y el comportamiento si se supera.
		 * Parametros:
		 *  - checkType:	Tipo de comprobacion. Admite los valores CT_NO, CT_RECOMMENDED y CT_OBLIGATORY.
		 *  - maxMillis:	Tiempo maximo de desfase en milisegundos.
		 * Cuando el HTML es local, no se realiza ningun tipo de comprobacion.
		 * */
		var checkTime = function (checkType, maxMillis) {

			if (checkType == undefined || checkType == null || checkType == CHECKTIME_NO
					|| maxMillis == undefined || maxMillis == null || maxMillis <= 0) {
				return;
			}
			
			// Hacemos una llamada al servidor para conocer su hora
			var xhr = getHttpRequest(); 
			xhr.open('GET', document.URL + '/' + Math.random(), false); 
			xhr.send(); 

			// Recogemos la hora local, nada mas obtener la respuesta del servidor
			var clientDate = new Date();
			
			// Tomamos la hora a partir de la respuesta del servidor. Si esta es 0, estamos en local
			var serverDate = new Date(xhr.getResponseHeader("Date"));
			if (serverDate == null || serverDate.getTime() == 0) {
				// No hacemos nada si estamos en local 
				return;
			}

			// Evaluamos la desincronizacion 
			var delay =  Math.abs(clientDate.getTime() - serverDate.getTime());
			if (delay > maxMillis) {
				 if (checkType == CHECKTIME_RECOMMENDED) {
					 alert("Se ha detectado un desfase horario entre su sistema y el servidor. Se recomienda que se corrija antes de pulsar Aceptar para continuar." +
							 "\nHora de su sistema: " + clientDate.toLocaleString() +
							 "\nHora del servidor: " + serverDate.toLocaleString());
				 }
				 else if (checkType == CHECKTIME_OBLIGATORY) {
					 severeTimeDelay = true;
					 alert("Se ha detectado un desfase horario entre su sistema y el servidor. Debe corregir la hora de su sistema antes de continuar." +
							 "\nHora de su sistema: " + clientDate.toLocaleString() +
							 "\nHora del servidor: " + serverDate.toLocaleString());
				 }
			}
		}

		/** Carga el MiniApplet. */
		var cargarMiniApplet = function (base, keystore) {

			// Antes que nada, comprobamos que no haya un desfase horario declarado como grave.
			if (severeTimeDelay) {
				return;
			}

			// Establecemos cual sera el keystore por defecto
			defaultKeystore = keystore;
			if (defaultKeystore == undefined) {
				defaultKeystore = null;
			}

			// Si estamos claramente en un sistema movil o que no permite la ejecucion de Java,
			// cargamos directamente el Cliente JavaScript
			if (isAndroid() || isIOS() || isWindowsRT()) {
				cargarAppAfirma(base, defaultKeystore);
				return;
			}

			// Si estamos en un entorno que permite Java, comprobamos si esta disponible
			// y en caso de no estarlo, tambien cargamos el Cliente JavaScript.
			if (!isJavaEnabled()) {
				cargarAppAfirma(base, defaultKeystore);
				return;
			}

			// Configuramos los argumentos para la seleccion de almacen
			configureKeyStore();
			
			// Incluso si el navegador informa que hay Java, puede no haberlo (Internet Explorer
			// siempre dice que hay), asi que cargamos el applet, pero tenemos en cuenta que en
			// caso de error debemos cargar el cliente JavaScript
			codeBase = (base != undefined && base != null) ? base : './';
			
			var attributes = {
					'id': 'miniApplet',
					'name': 'MiniApplet @firma (Gobierno de Espa\u00F1a)',
					'type': 'application/x-java-applet',
					'width': 1,
					'height': 1
			};
			
			// Los argumentos de java no llegan al propio applet en las pruebas con Java 6 y 7,
			// asi que (salvo los argumentos de carga) vamos a pasarlos como un parametro mas al
			// applet para luego establecerlos internamente.
			var parameters = {
					'keystore': defaultKeystore,
					'userAgent': window.navigator.userAgent,
					'archive': codeBase + '/' + JAR_NAME,
					'code': 'es.gob.afirma.miniapplet.MiniAfirmaApplet',
					'java-vm-args': JAVA_ARGUMENTS,
					'java_arguments': JAVA_ARGUMENTS,
					'system_properties': SYSTEM_PROPERTIES,
					'codebase_lookup': false,
					'separate_jvm': true,
					'locale': selectedLocale
			};

			loadMiniApplet(attributes, parameters);

			clienteFirma = document.getElementById("miniApplet");

			// Si no esta definido el cliente es porque se ha intentado cargar el applet
			// y no se ha podido, asi que se usara la aplicacion nativa
			if (clienteFirma == null) {
				cargarAppAfirma(codeBase, defaultKeystore);
			}
		}

		/** Establece los parametros de configuracion para la correcta seleccion del almacen
		 * de claves que se debe cargar. */
		function configureKeyStore() {
			if (isFirefoxUAM()) {
				if (SYSTEM_PROPERTIES == null) {
					SYSTEM_PROPERTIES = "";
				}
				SYSTEM_PROPERTIES += " -Des.gob.afirma.keystores.mozilla.UseEnvironmentVariables=true ";
			}
		}

		var sign = function (dataB64, algorithm, format, params, successCallback, errorCallback) {
			
			forceLoad();

			if (clientType == TYPE_APPLET) {

				// Si el parametro es una URL (HTTP/HTTPS), descargamos los datos
				if (isValidUrl(dataB64)) {
					try {
						dataB64 = downloadRemoteData(dataB64);
					} catch(e) {
						if (errorCallback == undefined || errorCallback == null) {
							throw e;
						}
						errorCallback("java.io.IOException", "Error al descargar los datos remotos");
						return;
					}
				}

				try {
					setData(dataB64);
					var certSignaturePair = buildData(clienteFirma.sign(algorithm, format, params));
					var sepPos = certSignaturePair.indexOf('|');
					if (successCallback == undefined || successCallback == null) {
						return certSignaturePair.substring(sepPos + 1);
					}
					successCallback(certSignaturePair.substring(sepPos + 1), certSignaturePair.substring(0, sepPos));
				} catch(e) {
					if (errorCallback == undefined || errorCallback == null) {
						throw e;
					}
					errorCallback(clienteFirma.getErrorType(), clienteFirma.getErrorMessage());
				}
			}
			else if (clientType == TYPE_JAVASCRIPT_SOCKET ) {
				clienteFirma.sign(dataB64, algorithm, format, params, successCallback, errorCallback);
			}
			else {
				clienteFirma.sign(dataB64, algorithm, format, params, successCallback, errorCallback);
			}
		}

		var coSign = function (signB64, dataB64, algorithm, format, params, successCallback, errorCallback) {

			forceLoad();

			if (clientType == TYPE_APPLET) {

				// Si el parametro es una URL (HTTP/HTTPS), descargamos los datos
				if (isValidUrl(signB64)) {
					try {
						signB64 = downloadRemoteData(signB64);
					} catch(e) {
						if (errorCallback == undefined || errorCallback == null) {
							throw e;
						}
						errorCallback("java.io.IOException", "Error al descargar la firma remota");
						return;
					}
				}

				// Si el parametro es una URL (HTTP/HTTPS), descargamos los datos
				if (isValidUrl(dataB64)) {
					try {
						dataB64 = downloadRemoteData(dataB64);
					} catch(e) {
						if (errorCallback == undefined || errorCallback == null) {
							throw e;
						}
						errorCallback("java.io.IOException", "Error al descargar los datos remotos");
						return;
					}
				}

				try {
					setData(signB64);
					var certSignaturePair = buildData(clienteFirma.coSign(dataB64, algorithm, format, params));
					var sepPos = certSignaturePair.indexOf('|');
					if (successCallback == undefined || successCallback == null) {
						return certSignaturePair.substring(sepPos + 1);
					}
					successCallback(certSignaturePair.substring(sepPos + 1), certSignaturePair.substring(0, sepPos));
				} catch(e) {
					if (errorCallback == undefined || errorCallback == null) {
						throw e;
					}
					errorCallback(clienteFirma.getErrorType(), clienteFirma.getErrorMessage());
				}
			}
			else if (clientType == TYPE_JAVASCRIPT_SOCKET) {
				clienteFirma.coSign(signB64, dataB64, algorithm, format, params, successCallback, errorCallback);
			}
			else {
				clienteFirma.coSign(signB64, dataB64, algorithm, format, params, successCallback, errorCallback);
			}
		}

		var counterSign = function (signB64, algorithm, format, params, successCallback, errorCallback) {

			forceLoad();

			if (clientType == TYPE_APPLET) {

				// Si el parametro es una URL (HTTP/HTTPS), descargamos los datos
				if (isValidUrl(signB64)) {
					try {
						signB64 = downloadRemoteData(signB64);
					} catch(e) {
						if (errorCallback == undefined || errorCallback == null) {
							throw e;
						}
						errorCallback("java.io.IOException", "Error al descargar la firma remota");
						return;
					}
				}

				try {
					setData(signB64);
					var certSignaturePair = buildData(clienteFirma.counterSign(algorithm, format, params));
					var sepPos = certSignaturePair.indexOf('|');
					if (successCallback == undefined || successCallback == null) {
						return certSignaturePair.substring(sepPos + 1);
					}
					successCallback(certSignaturePair.substring(sepPos + 1), certSignaturePair.substring(0, sepPos));
				} catch(e) {
					if (errorCallback == undefined || errorCallback == null) {
						throw e;
					}
					errorCallback(clienteFirma.getErrorType(), clienteFirma.getErrorMessage());
				}
			}
			else if (clientType == TYPE_JAVASCRIPT_SOCKET) {
				clienteFirma.counterSign(signB64, algorithm, format, params, successCallback, errorCallback);
			}
			else {
				clienteFirma.counterSign(signB64, algorithm, format, params, successCallback, errorCallback);
			}
		}

		var getBase64FromText = function (plainText, charset) {
			forceLoad();
			return clienteFirma.getBase64FromText(plainText, charset);
		}

		var getTextFromBase64 = function (dataB64, charset) {
			forceLoad();
			return clienteFirma.getTextFromBase64(dataB64, charset);
		}

		var saveDataToFile = function (dataB64, title, fileName, extension, description,  successCallback, errorCallback) {
			forceLoad();
			if (clientType == TYPE_APPLET) {
				setData(dataB64);
				return clienteFirma.saveDataToFile(title, fileName, extension, description);
			}
			else if (clientType == TYPE_JAVASCRIPT_SOCKET ) {
				clienteFirma.saveDataToFile(dataB64, title, fileName, extension, description, successCallback, errorCallback);
			}
			else {
				clienteFirma.saveDataToFile(dataB64, title, fileName, extension, description);
			}
		}

		var getFileNameContentBase64 = function (title, extensions, description, filePath) {
			forceLoad();
			return buildData(clienteFirma.getFileNameContentBase64(title, extensions, description, filePath));
		}

		var getMultiFileNameContentBase64 = function (title, extensions, description, filePath) {
			forceLoad();
			return buildData(clienteFirma.getMultiFileNameContentBase64(title, extensions, description, filePath));
		}

		var echo = function () {
			forceLoad();
			return clienteFirma.echo();
		}

		var setStickySignatory = function (sticky) {
			forceLoad();
			return clienteFirma.setStickySignatory(sticky);
		}

		var setLocale = function (locale) {
			selectedLocale = locale;
		}

		var getErrorMessage = function () {
			forceLoad();
			return clienteFirma.getErrorMessage();
		}

		var getErrorType = function () {
			forceLoad();
			return clienteFirma.getErrorType();
		}

		var getCurrentLog = function () {
			forceLoad();
			return	" === JAVASCRIPT INFORMATION === " +
					"\nnavigator.appCodeName: " + navigator.appCodeName +
					"\nnavigator.appName: " +  navigator.appName +
					"\nnavigator.appVersion: " + navigator.appVersion +
					"\nnavigator.platform: " + navigator.platform +
					"\nnavigator.userAgent: " + navigator.userAgent+
					"\nnavigator.javaEnabled(): " + navigator.javaEnabled() +
					"\nscreen.width: " + (window.screen ? screen.width : 0) +
					"\nscreen.height: " + (window.screen ? screen.height : 0) +
					"\n\n   === CLIENTE LOG === \n" + 
					clienteFirma.getCurrentLog();
		}

		var setServlets = function (storageServlet,  retrieverServlet) {
			
			storageServletAddress = storageServlet;
			retrieverServletAddress = retrieverServlet;

			if (clienteFirma && clienteFirma.setServlets) {
				clienteFirma.setServlets(storageServlet,  retrieverServlet);
			}
		}

		/*************************************************************
		 *  FUNCIONES PARA EL DESPLIEGUE DEL APPLET					 *
		 **************************************************************/

		function loadMiniApplet(attributes, parameters) {
			// Internet Explorer se carga mediante un
			// elemento <object>. El resto con un <embed>.
			if (isInternetExplorer()) {
				
				var appletTag = "<object classid='clsid:8AD9C840-044E-11D1-B3E9-00805F499D93' width='" + attributes["width"] + "' height='" + attributes["height"] + "' id='" + attributes["id"] + "'>";

				if (attributes != undefined && attributes != null) {
					for (var attribute in attributes) {
						appletTag += "<param name='" + attribute + "' value='" + attributes[attribute] + "' />";
					}
				}

				if (parameters != undefined && parameters != null) {
					for (var parameter in parameters) {
						appletTag += "<param name='" + parameter + "' value='" + parameters[parameter] + "' />";
					}
				}

				appletTag += "</object>";

				// Al agregar con append() estos nodos no se carga automaticamente el applet en IE10 e inferiores, asï¿½ que
				// hay que usar document.write() o innerHTML. Para asegurarnos de no pisar HTML previo, crearemos un <div>
				// en la pagina, lo recogeremos e insertaremos dentro suyo el codigo del applet.
				var divElem = document.createElement("div");
				var idAtt = document.createAttribute("id");
				idAtt.value = 'divAfirmaApplet';
				divElem.setAttributeNode(idAtt);

				document.body.appendChild(divElem);
				
				document.getElementById("divAfirmaApplet").innerHTML = appletTag;
			}
			else {
				var embed = document.createElement("embed");

				if (attributes != undefined && attributes != null) {
					for (var attribute in attributes) {
						var att = document.createAttribute(attribute);
						att.value = attributes[attribute];
						try {
							embed.setAttributeNode(att);
						}
						catch (e) {
							// Probamos este como alternativa en caso de error. Caso detectado en:
							// - IE10 sin modo de compabilidad con el Document Mode de IE7.
							// - Firefox en Mac OS X
							// Este intento no soluciona el error, pero evita que se propague 
							embed.setAttribute(attribute, attributes[attribute]);
						}
					}
				}

				if (parameters != undefined && parameters != null) {
					for (var parameter in parameters) {
						var att = document.createAttribute(parameter);
						att.value = parameters[parameter];
						embed.setAttributeNode(att);
					}
				}

				document.body.appendChild(embed);
			}
		}
		
		/**
		 * Establece los datos que debera procesar el applet MiniApplet. 
		 */
		function forceLoad() {

			// Antes que nada, comprobamos que no haya un desfase horario declarado como
			// grave.
			if (severeTimeDelay) {
				return;
			}
			if (clientType == null || clientType == TYPE_JAVASCRIPT_WEB_SERVICE || clientType == TYPE_JAVASCRIPT_SOCKET ) {
				var tempCliente = document.getElementById("miniApplet");
				var appletLoaded;
				try {
					appletLoaded = tempCliente != null && tempCliente.echo() != "Cliente JavaScript"; 
				}
				catch (e) {
					appletLoaded = false;
				}
				if (appletLoaded) {
					clienteFirma = tempCliente;
					clientType = TYPE_APPLET;
				}
				else if (clientType == null ) {
					cargarAppAfirma(codeBase, defaultKeystore);	
				}
				setServlets(storageServletAddress, retrieverServletAddress);
			}
		}

		/**
		 * Establece los datos que debera procesar el applet MiniApplet. 
		 */
		function setData(dataB64) {

			if (dataB64 == null) {
				return;
			}
			else if (dataB64.length <= BUFFER_SIZE) {
				clienteFirma.addData(dataB64);	
			}
			else {
				clienteFirma.addData(dataB64.substring(0, BUFFER_SIZE));
				setData(dataB64.substring(BUFFER_SIZE));
			}
		}

		/**
		 * Construye el resultado de una funcion a partir de los trozos en la que esta los divide. 
		 */
		function buildData(dataB64) {
			var buffer = dataB64;
			var chunk = clienteFirma.getRemainingData();
			while(chunk != EOF) {
				buffer += chunk;
				chunk = clienteFirma.getRemainingData();
			}
			return buffer;
		}

		/**************************************************************
		 **************************************************************
		 **************************************************************
		 **************************************************************
		 *  FUNCIONES DEL CLIENTE JAVASCRIPT						  *
		 **************************************************************
		 **************************************************************
		 **************************************************************
		 **************************************************************/

		/**
		 * Establece el objeto que simula ser el Applet de firma en sistemas en los que no se
		 * soportan los applets. Se usara comunicacion mediante socket si el entorno es compatible con:
		 *  - localStorage
		 *  - addEventListener
		 *  - postMessage
		 *  - Si las constantes AfirmaSocket.POPUP_URL y AfirmaSocket.IFRAME_URL estan inicializadas.
		 * En caso contrario, la comunicacion se realizara mediante un servidor intermedio.
		 */
		function cargarAppAfirma(clientAddress, keystore) {
			if (!isIOS() && !isAndroid() && window.localStorage && window.addEventListener && window.postMessage && AfirmaSocket && AfirmaSocket.POPUP_URL && AfirmaSocket.IFRAME_URL ){
				clienteFirma = new AppAfirmaJSSocket(clientAddress, window, undefined);
				clienteFirma.setKeyStore(keystore);
				clientType = TYPE_JAVASCRIPT_SOCKET;
			}
			else {
				clienteFirma = new AppAfirmaJSWebService(clientAddress, window, undefined);
				clienteFirma.setKeyStore(keystore);
				clientType = TYPE_JAVASCRIPT_WEB_SERVICE;
			}
		}

		/**
		 * Objeto JavaScript que va a reemplazar al cliente de firma en los entornos en los que
		 * no pueden ejecutarse applets.
		 * Utilizamos un socket y el paso por mensaje entre ventanas
		 */
		var AppAfirmaJSSocket = ( function (clientAddress, window, undefined) {

			var UnsupportedOperationException = "java.lang.UnsupportedOperationException";

			var DEFAULT_SIGNATURE_PAGE = AfirmaSocket.POPUP_URL;
			// tamano maximo en caracteres para un mensaje al popup2
			var MESSAGE_MAX_SIZE = 400000;
			
			/**
			 *  Atributos para la configuracion del objeto sustituto del applet Java de firma
			 */
			var errorMessage = '';
			var errorType = '';

			/** Puerto actual */
			var port = "";

			/** Clave de cifrado. */
			var cipherKey = "";

			var writePort;

			/** Ventana para el intercambio de datos (tiene que estar sobre http). */
			var modalWindow = null;
		
			/** Dominio desde el que se realiza la llamada al servicio */
			var baseUri = clientAddress;

			/** Dominio desde el que se realiza la llamada al servicio */
			var defaultKeystore = null;
			
			/** Funcion callback que se lanza al obtener un resultado. */
			var resultCallbackFunction = null;

			// Registramos un callback para la obtencion de los mensajes de devolucion. Lo almacenamos para no
			// registrarlo para cada operacion
			function registryCallbacks (successCallback, errorCallback) {
				var results = "";
				if (resultCallbackFunction == null) {

					resultCallbackFunction = function(event) {
						var localWindow = modalWindow;
						// Se llamara a este evento ante 2 entradas "signDatos" que almacenara el resultado de
						// la operacion y "signEstado" que se recibira antes y almacenara un "OK" o un "KO" segun
						// haya terminado bien o mal la operacion
						if (event.key == "signEstado" && event.newValue) {

							try {
								// usamos una variable auxiliar para que solo entre la primera vez que se recibe
								if (writePort) {
									port = localStorage.getItem("port");
									cipherKey = localStorage.getItem("key");
									writePort = false;
								}

								var signEstado = localStorage.getItem("signEstado");
								if (signEstado == "KO") {
									errorCallback("java.lang.Exception", localStorage.getItem("signDatos"));
									localWindow.close();
								}
								else if (signEstado == "OK") {
									//Lo han mandado en una envio
									if(localStorage.getItem("EOF") == "EOF") {
										if(results == "") {
											successCallback(localStorage.getItem("signDatos"));
										}
										else {
											successCallback(results + localStorage.getItem("signDatos"));
										}
										localWindow.close();
									}
									// No cabe en un solo envio, lo almacenamos para poder mandarlo todo junto
									else if (localStorage.getItem("EOF") == "-" ) {
										results += localStorage.getItem("signDatos");
									}
								}
								else if (signEstado == "SAVE_OK") {
									successCallback(signEstado);
									localWindow.close();
								}
								else {
									errorCallback("java.lang.Exception", "No se conoce si el resultado obtenido es correcto");
									localWindow.close();
								}
							} catch(e) {
								// Ocurrio un error al invocar a la funcion resultado
							}
							localStorage.removeItem("signEstado");
							localStorage.removeItem("signDatos");
							localStorage.removeItem("EOF");
							localStorage.removeItem("port");
							localStorage.removeItem("key");
						}
					};
					
					window.addEventListener('storage', resultCallbackFunction, false);
				}
			}

			/**
            * Envia datos a la ventana de firma.
			*/
			function sendData(window, data) {
				// Mandamos varias peticiones si el tamano del documento excede del maximo
				if (!data.dat.value) {
					window.postMessage(data, DEFAULT_SIGNATURE_PAGE);
				}
				else if(data.dat.value.length > MESSAGE_MAX_SIZE){
					var totalData = data.dat.value;
					// Calculamos el numero de elementos que le vamos a pasar 
					data.numParts = Math.ceil(data.dat.value.length / MESSAGE_MAX_SIZE);
					var send;
					var i = 1;
					do{
						send = totalData.substring(0, MESSAGE_MAX_SIZE);
						totalData = totalData.substring(MESSAGE_MAX_SIZE)
						data.dat.value = send;
						data.order = i;
						window.postMessage(data, DEFAULT_SIGNATURE_PAGE);
						i++;
					}
					while (totalData.length > 0);	
				}
				// Cabe en un envio, seguimos como siempre
				else{
					window.postMessage(data, DEFAULT_SIGNATURE_PAGE);
				}
			}

			/**
			 * Establece el almacen de certificados de que se debe utilizar por defecto.
			 */
			function setKeyStore (keystore) {
				defaultKeystore = keystore;
			}
			
			/**
			 * Inicia el proceso de firma electronica.
			 * Implementada en el applet Java de firma
			 */
			function sign (dataB64, algorithm, format, extraParams, successCallback, errorCallback) {
				signOperation("sign", dataB64, algorithm, format, extraParams, successCallback, errorCallback);
			}

			/**
			 * Inicia el proceso de cofirma de una firma electr&oacute;nica. 
			 * Implementada en el applet Java de firma.
			 */
			function coSign (signB64, dataB64, algorithm, format, extraParams, successCallback, errorCallback) {
				signOperation("cosign", signB64, algorithm, format, extraParams, successCallback, errorCallback);
			}

			/**
			 * Inicia el proceso de contrafirma de una firma electr&oacute;nica.
			 * Implementada en el applet Java de firma. 
			 */
			function counterSign (signB64, algorithm, format, extraParams, successCallback, errorCallback) {
				signOperation("countersign", signB64, algorithm, format, extraParams, successCallback, errorCallback);
			}

			/**
			 * Realiza una operacion de firma/multifirma abriendo un popUp.
			 * @param signId Identificador de la operacion a realizar (sign, cosign y countersign).
			 * @param dataB64 Datos o firma en base 64.
			 * @param algorithm Algoritmo de firma.
			 * @param format Formato de firma.
			 * @param extraParams Par&aacute;metros para la configuraci&oacute;n de la operaci&oacute;n.
			 * @param successCallback M&eacute;todo a ejecutar en caso de &eacute;xito.
			 * @param errorCallback M&eacute;todo a ejecutar en caso de error.
			 */
			function signOperation (signId, dataB64, algorithm, format, extraParams, successCallback, errorCallback) {
				// Cuando recibamos los datos del iframe guardaremos el puerto solo el primer envio para evitar errores.
				writePort = true;
				// Si no se ha abierto una pagina modal externamente, la abrimos nosotros (aunque muy probablemente quede bloqueada)
				modalWindow = window.open(DEFAULT_SIGNATURE_PAGE, "Pagina de firma" + Date.now(), "width=500,height=260");
				
				/** Almacenamos el dominio en local para poder evaluarlo desde el iframe de firma. */
				localStorage.setItem("signPageBaseUri", DEFAULT_SIGNATURE_PAGE);	// TODO: Podria ser el de la ventana exterior, pero hay que averiguar como obtenerlo

				registryCallbacks(successCallback, errorCallback);

				setTimeout(signByService, 1000, modalWindow, signId, dataB64, algorithm, format, extraParams, successCallback, errorCallback);
			}

			/**
			 * Funcion de firma medante invocacion a un servicio.
			 * @param signWindow Ventana modal que hara de pasarela para la firma.
			 * @param signId Identificador de la operacion a realizar (sign, cosign y countersign).
			 * @param dataB64 Datos o firma en base 64.
			 * @param algorithm Algoritmo de firma.
			 * @param format Formato de firma.
			 * @param extraParams Par&aacute;metros para la configuraci&oacute;n de la operaci&oacute;n.
			 * @param successCallback M&eacute;todo a ejecutar en caso de &eacute;xito.
			 * @param errorCallback M&eacute;todo a ejecutar en caso de error.
			 */
			function signByService (signWindow, signId, dataB64, algorithm, format, extraParams, successCallback, errorCallback) {

				if (dataB64 == undefined || dataB64 == "") {
					dataB64 = null;
				}

				if (dataB64 != null && !isValidUrl(dataB64)) {
					dataB64 = dataB64.replace(/\+/g, "-").replace(/\//g, "_");
				}

				var idSession = generateNewIdSession();

				var data = generateDataToSign(signId, idSession, algorithm, format, extraParams, dataB64);
				sendData(signWindow, data);
			}

			/**
			 * Genera el objeto con los datos de la transaccion para la firma
			 */
			function generateDataToSign(signId, idSession, algorithm, format, extraParams, dataB64) {
				var data = new Object();
				data.origin = generateDataKeyValue("origin", baseUri);
				data.op = generateDataKeyValue("op", signId);
				data.keystore = generateDataKeyValue("keystore", defaultKeystore);
				data.id = generateDataKeyValue("id",idSession);
				data.algorithm = generateDataKeyValue ("algorithm", algorithm);
				data.format = generateDataKeyValue ("format", format); 
				data.properties = generateDataKeyValue ("properties", extraParams != null ? Base64.encode(extraParams) : null);
				data.dat = generateDataKeyValue ("dat", dataB64 == "" ? null : dataB64);
				data.order = 1;
				data.numParts = 1;
				data.port = generateDataKeyValue ("port", port);
				data.key = generateDataKeyValue ("key", cipherKey);
				
				return data;
			}

			/**
			 * Inicia el proceso de guardado de una firma.
			 */
			function saveDataToFile (dataB64, title, filename, extension, description, successCallback, errorCallback) {
				// Cuando recibamos los datos del iframe guardaremos el puerto solo el primer envio para evitar errores.
				writePort = true;
				// Si no se ha abierto una pagina modal externamente, la abrimos nosotros (aunque muy probablemente quede bloqueada)
				modalWindow = window.open(DEFAULT_SIGNATURE_PAGE, "Pagina de firma"+Date.now(), "width=500,height=260");
				
				/** Almacenamos el dominio en local para poder evaluarlo desde el iframe de firma. */
				localStorage.setItem("signPageBaseUri", DEFAULT_SIGNATURE_PAGE);

				registryCallbacks(successCallback, errorCallback);

				setTimeout(saveByService, 3000, modalWindow, dataB64, title, filename, extension, description);
			}

			function saveByService ( signWindow, dataB64, title, filename, extension, description) {
				
				if (dataB64 == undefined || dataB64 == "") {
					dataB64 = null;
				}

				if (dataB64 != null && !isValidUrl(dataB64)) {
					dataB64 = dataB64.replace(/\+/g, "-").replace(/\//g, "_");
				}

				var idSession = generateNewIdSession();

				var data = generateDataToSave(dataB64, title, filename, extension, description, idSession);
				sendData(signWindow, data);
			}

			/**
			* Genera el objeto de datos para la operacion de guardar
			*/
			function generateDataToSave(dataB64, title, filename, extension, description, id) {
				var data = new Object();
				data.origin = generateDataKeyValue("origin", baseUri);
				data.op = generateDataKeyValue ("op", "save");
				data.id = generateDataKeyValue ("id", id);
				data.title = generateDataKeyValue ("title", title);
				data.filename = generateDataKeyValue ("filename", filename);
				data.extension = generateDataKeyValue ("extension", extension);
				data.description = generateDataKeyValue ("description", description);
				data.dat = generateDataKeyValue ("dat", dataB64);
				data.order = 1;
				data.numParts = 1;
				data.port = generateDataKeyValue ("port", port);
				data.key = generateDataKeyValue ("key", cipherKey);
				
				return data;
			}

			function generateDataKeyValue(key, value) {
				var data  = new Object();
				data.key = key;
				data.value = value;
				return data;
			}
			
			/**
			 * Convierte texto plano en texto base 64.
			 * Implementada en el applet Java de firma.
			 */
			function getBase64FromText (plainText, charset) {
				return plainText != null ? Base64.encode(plainText) : null;
			}

			/**
			 * Convierte texto base 64 en texto plano.
			 * Implementada en el applet Java de firma.
			 */
			function getTextFromBase64 (base64Text, charset) {
				return base64Text != null ? Base64.decode(base64Text) : null;
			}

			
			/**
			 * Carga de un fichero. Operacion no soportada. 
			 * Implementada en el applet Java de firma.
			 */
			function getFileNameContentBase64 (title, extensions, description) {
				throwException(UnsupportedOperationException, "La operacion de carga de ficheros no esta soportada");
			}

			/**
			 * Carga de multiples ficheros. Operacion no soportada.
			 * Implementada en el applet Java de firma.
			 */
			function getMultiFileNameContentBase64 (title, extensions, description) {
				throwException(UnsupportedOperationException, "La operacion de carga de multiples ficheros no esta soportada");
			}

			/** 
			 * Funcion para la comprobacion de existencia del objeto. No hace nada.
			 * Implementada en el applet Java de firma.
			 */
			function echo () {
				return "Cliente JavaScript";
			}

			/** 
			 * No hace nada.
			 * Implementada en el applet Java de firma.
			 */
			function setStickySignatory (sticky) {
				// No hace nada
			}

			/**
			 * Recupera el mensaje de error asociado al ultimo error capturado.
			 * Implementada en el applet Java de firma.
			 */
			function getErrorMessage () {
				return errorMessage;
			}

			/**
			 * Recupera el tipo del ultimo error capturado.
			 * Implementada en el applet Java de firma.
			 */
			function getErrorType () {
				return errorType;
			}

			/**
			 * Recupera el log de la aplicacion. Actualmente, el log solo esta
			 * disponible en el applet, no en las aplicacion moviles.
			 */
			function getCurrentLog () {
				return "Applet no cargado";
			}

			/**
			 * Funcion para identificar el tipo de objeto del Cliente (javascript, applet,...).
			 */
			function getType () {
				return "javascript";
			}

			/**
			 * Establece el error indicado como error interno y lanza una excepcion.
			 */
			function throwException (type, message) {
				errorType = type;
				errorMessage = message;
				throw new Exception();
			}

			/* Mayor entero. */
			var MAX_NUMBER = 2147483648;

			/* Caracteres validos para los ID de sesion */
			var VALID_CHARS_TO_ID = "1234567890abcdefghijklmnopqrstuwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

			/* Genera un identificador de sesion. */
			function generateNewIdSession () {
				var ID_LENGTH = 20;
				var random = "";
				var randomInts;
				if (typeof window.crypto != "undefined" && typeof window.crypto.getRandomValues != "undefined") {
					randomInts = new Uint32Array(ID_LENGTH);
					window.crypto.getRandomValues(randomInts);
				}
				else {
					randomInts = new Array(ID_LENGTH);
					for (var i = 0; i < ID_LENGTH; i++) {
						randomInts[i] = rnd() * MAX_NUMBER;
					}
				}

				for (var i = 0; i < ID_LENGTH; i++) {
					random += VALID_CHARS_TO_ID.charAt(Math.floor(randomInts[i] % VALID_CHARS_TO_ID.length));
				}

				return random;
			}

			/* Completa un numero con ceros a la izquierda. */
			function zeroFill(number, width) {
				width -= number.toString().length;
				if (width > 0) {
					return new Array(width + (/\./.test(number) ? 2 : 1)).join('0')
					+ number;
				}
				return number + "";
			}

			/**
			 * Genera numeros aleatorios con una distribucion homogenea
			 */
			var seed;
			function rnd () {
				if (seed == undefined) {
					seed = new Date().getMilliseconds() * 1000 * Math.random();
				}
			    seed = (seed * 9301 + 49297) % 233280;
			    return seed / 233280;
			}

			/* Metodos que publicamos del objeto AppAfirmaJS */
			return {
				echo : echo,
				checkTime : checkTime,
				setKeyStore : setKeyStore,
				sign : sign,
				coSign : coSign,
				counterSign : counterSign,
				saveDataToFile : saveDataToFile,
				getFileNameContentBase64: getFileNameContentBase64,
				getMultiFileNameContentBase64 : getMultiFileNameContentBase64,
				getBase64FromText : getBase64FromText,
				getTextFromBase64 : getTextFromBase64,
				setStickySignatory : setStickySignatory,
				setLocale : setLocale,
				getErrorMessage : getErrorMessage,
				getErrorType : getErrorType,
				getCurrentLog : getCurrentLog
			}
		});
		
		/**
		 * Objeto JavaScript que va a reemplazar al cliente de firma en los entornos en los que
		 * no pueden ejecutarse applets.
		 * Cuando no funciona el paso por mensajes en las versiones viejas de IE utilizamos
		 * un web service
		 */
		var AppAfirmaJSWebService = ( function (clientAddress, window, undefined) {

			var UnsupportedOperationException = "java.lang.UnsupportedOperationException";

			/**
			 *  Atributos para la configuracion del objeto sustituto del applet Java de firma
			 */
			var errorMessage = '';
			var errorType = '';
			var defaultKeystore = null;
			var retrieverServletAddress = null;
			var storageServletAddress = null;

			if (clientAddress != undefined && clientAddress != null) {
				if (clientAddress.indexOf("://") != -1 && clientAddress.indexOf("/", clientAddress.indexOf("://") + 3) != -1) {
					var servletsBase = clientAddress.substring(0, clientAddress.indexOf("/", clientAddress.indexOf("://") + 3));
					retrieverServletAddress = servletsBase + "/afirma-signature-retriever/RetrieveService";
					storageServletAddress = servletsBase + "/afirma-signature-storage/StorageService";
				} else {
					retrieverServletAddress = clientAddress + "/afirma-signature-retriever/RetrieveService";
					storageServletAddress = clientAddress + "/afirma-signature-storage/StorageService";
				}
			}

			/**
			 * Establece el almacen de certificados de que se debe utilizar por defecto.
			 */
			function setKeyStore (keystore) {
				defaultKeystore = keystore;
			}
			
			/**
			 * Inicia el proceso de firma electronica.
			 * Implementada en el applet Java de firma
			 */
			function sign (dataB64, algorithm, format, extraParams, successCallback, errorCallback) {
				signOperation("sign", dataB64, algorithm, format, extraParams, successCallback, errorCallback);
			}

			/**
			 * Inicia el proceso de cofirma de una firma electr&oacute;nica. 
			 * Implementada en el applet Java de firma.
			 */
			function coSign (signB64, dataB64, algorithm, format, extraParams, successCallback, errorCallback) {
				signOperation("cosign", signB64, algorithm, format, extraParams, successCallback, errorCallback);
			}

			/**
			 * Inicia el proceso de contrafirma de una firma electr&oacute;nica.
			 * Implementada en el applet Java de firma. 
			 */
			function counterSign (signB64, algorithm, format, extraParams, successCallback, errorCallback) {
				signOperation("countersign", signB64, algorithm, format, extraParams, successCallback, errorCallback);
			}

			/**
			 * Realiza una operacion de firma/multifirma sin abrir popUP.
			 * @param signId Identificador de la operacion a realizar (sign, cosign y countersign).
			 * @param dataB64 Datos o firma en base 64.
			 * @param algorithm Algoritmo de firma.
			 * @param format Formato de firma.
			 * @param extraParams Par&aacute;metros para la configuraci&oacute;n de la operaci&oacute;n.
			 * @param successCallback M&eacute;todo a ejecutar en caso de &eacute;xito.
			 * @param errorCallback M&eacute;todo a ejecutar en caso de error.
			 */
			function signOperation (signId, dataB64, algorithm, format, extraParams, successCallback, errorCallback) {

				if (dataB64 == undefined || dataB64 == "") {
					dataB64 = null;
				}

				if (dataB64 != null && !isValidUrl(dataB64)) {
					dataB64 = dataB64.replace(/\+/g, "-").replace(/\//g, "_");
				}

				var idSession = generateNewIdSession();
				var cipherKey = generateCipherKey();

				var i = 0;
				var params = new Array();
				if (signId != null && signId != undefined) {			params[i++] = {key:"op", value:encodeURIComponent(signId)}; }
				if (idSession != null && idSession != undefined) {		params[i++] = {key:"id", value:encodeURIComponent(idSession)}; }
				if (cipherKey != null && cipherKey != undefined) {		params[i++] = {key:"key", value:encodeURIComponent(cipherKey)}; }
				if (defaultKeystore != null &&
						defaultKeystore != undefined) {					params[i++] = {key:"keystore", value:encodeURIComponent(defaultKeystore)}; }
				if (storageServletAddress != null &&
						storageServletAddress != undefined) {			params[i++] = {key:"stservlet", value:encodeURIComponent(storageServletAddress)}; }
				if (format != null && format != undefined) {			params[i++] = {key:"format", value:encodeURIComponent(format)}; }
				if (algorithm != null && algorithm != undefined) {		params[i++] = {key:"algorithm", value:encodeURIComponent(algorithm)}; }
				if (extraParams != null && extraParams != undefined) { 	params[i++] = {key:"properties", value:encodeURIComponent(Base64.encode(extraParams))}; }
				if (dataB64 != null) {									params[i++] = {key:"dat", value:encodeURIComponent(dataB64)}; }

				var url = buildUrl(signId, params);

				// Si la URL es muy larga, realizamos un preproceso para que los datos se suban al
				// servidor y la aplicacion nativa los descargue, en lugar de pasarlos directamente 
				if (isURLTooLong(url)) {
					if (storageServletAddress == null || storageServletAddress == undefined) {
						throwException("java.lang.IllegalArgumentException", "No se ha indicado la direccion del servlet para el guardado de datos");
						return;
					}

					var fileId = preProccessData(cipherKey, storageServletAddress, signId, params);
					if (!fileId) {
						throwException("java.net.UnknownHostException", "No se han podido enviar los datos a la aplicacion de firma");
						return;
					}
					
					url = buildUrlWithoutData(signId, fileId, retrieverServletAddress, cipherKey);
					if (isURLTooLong(url)) {
						throwException("java.lang.IllegalArgumentException", "La URL de invocacion al servicio de firma es demasiado larga.");
						return;
					}
				}

				execAppIntent(url, idSession, cipherKey, successCallback, errorCallback);
			}

			/**
			 * Convierte texto plano en texto base 64.
			 * Implementada en el applet Java de firma.
			 */
			function getBase64FromText (plainText, charset) {
				return plainText != null ? Base64.encode(plainText) : null ;
				 Base64.encode(plainText);
			}

			/**
			 * Convierte texto base 64 en texto plano.
			 * Implementada en el applet Java de firma.
			 */
			function getTextFromBase64 (base64Text, charset) {
				return Base64.decode(base64Text);
			}

			/**
			 * Guardado de datos en disco. Se realiza mediante la invocacion de una app nativa. 
			 */
			function saveDataToFile (dataB64, title, filename, extension, description) {

				if (dataB64 != undefined && dataB64 != null && dataB64 != "") {
					dataB64 = dataB64.replace(/\+/g, "-").replace(/\//g, "_");
				}

				var idSession = generateNewIdSession();
				var cipherKey = generateCipherKey();

				var i = 0;
				var params = new Array();
				params[i++] = {key:"op", value:"save"};
				if (idSession != null && idSession != undefined) {		params[i++] = {key:"id", value:encodeURIComponent(idSession)}; }
				if (cipherKey != null && cipherKey != undefined) {		params[i++] = {key:"key", value:encodeURIComponent(cipherKey)}; }
				if (storageServletAddress != null &&
						storageServletAddress != undefined) {			params[i++] = {key:"stservlet", value:storageServletAddress}; }
				if (title != null && title != undefined) {				params[i++] = {key:"title", value:encodeURIComponent(title)}; }
				if (filename != null && filename != undefined) {		params[i++] = {key:"filename", value:encodeURIComponent(filename)}; }
				if (extension != null && extension != undefined) {		params[i++] = {key:"extension", value:encodeURIComponent(extension)}; }
				if (description != null && description != undefined) {	params[i++] = {key:"description", value:encodeURIComponent(description)}; }
				if (dataB64 != null && dataB64 != undefined && dataB64 != "") {			params[i++] = {key:"dat", value:encodeURIComponent(dataB64)}; }

				var url = buildUrl("save", params);

				// Si la URL es muy larga, realizamos un preproceso para que los datos se suban al
				// servidor y la aplicacion nativa los descargue, en lugar de pasarlos directamente 
				if (isURLTooLong(url)) {
					if (storageServletAddress == null || storageServletAddress == undefined) {
						throwException("java.lang.IllegalArgumentException", "No se ha indicado la direccion del servlet para el guardado de datos");
						return;
					}

					var fileId = preProccessData(cipherKey, storageServletAddress, "save", params);
					if (!fileId) {
						throwException("java.net.UnknownHostException", "No se han podido enviar los datos a la aplicacion de firma");
						return;
					}

					url = buildUrlWithoutData("save", fileId, retrieverServletAddress, cipherKey);
					if (isURLTooLong(url)) {
						throwException("java.lang.IllegalArgumentException", "La URL de invocacion al servicio de firma es demasiado larga. No se soportan tantas propiedades de configuracion.");
						return;
					}
				}

				execAppIntent(url, idSession, cipherKey);
			}

			/**
			 * Carga de un fichero. Operacion no soportada. 
			 * Implementada en el applet Java de firma.
			 */
			function getFileNameContentBase64 (title, extensions, description) {
				throwException(UnsupportedOperationException, "La operacion de carga de ficheros no esta soportada");
			}

			/**
			 * Carga de multiples ficheros. Operacion no soportada.
			 * Implementada en el applet Java de firma.
			 */
			function getMultiFileNameContentBase64 (title, extensions, description) {
				throwException(UnsupportedOperationException, "La operacion de carga de multiples ficheros no esta soportada");
			}

			/** 
			 * Funcion para la comprobacion de existencia del objeto. No hace nada.
			 * Implementada en el applet Java de firma.
			 */
			function echo () {
				return "Cliente JavaScript";
			}

			/** 
			 * No hace nada.
			 * Implementada en el applet Java de firma.
			 */
			function setStickySignatory (sticky) {
				// No hace nada
			}

			/**
			 * Recupera el mensaje de error asociado al ultimo error capturado.
			 * Implementada en el applet Java de firma.
			 */
			function getErrorMessage () {
				return errorMessage;
			}

			/**
			 * Recupera el tipo del ultimo error capturado.
			 * Implementada en el applet Java de firma.
			 */
			function getErrorType () {
				return errorType;
			}

			/**
			 * Recupera el log de la aplicacion. Actualmente, el log solo esta
			 * disponible en el applet, no en las aplicacion moviles.
			 */
			function getCurrentLog () {
				return "Applet no cargado";
			}

			/**
			 * Funcion para identificar el tipo de objeto del Cliente (javascript, applet,...).
			 */
			function getType () {
				return "javascript";
			}

			/**
			 * Establece las rutas de los servlets encargados de almacenar y recuperar las firmas de los dispositivos moviles.
			 */
			function setServlets (storageServlet,  retrieverServlet) {
				storageServletAddress = storageServlet;
				retrieverServletAddress = retrieverServlet;
			}

			/**
			 * Establece el error indicado como error interno y lanza una excepcion.
			 */
			function throwException (type, message) {
				errorType = type;
				errorMessage = message;
				throw new Exception();
			}

			/* Mayor entero. */
			var MAX_NUMBER = 2147483648;

			/* Caracteres validos para los ID de sesion */
			var VALID_CHARS_TO_ID = "1234567890abcdefghijklmnopqrstuwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

			/* Genera un identificador de sesion. */
			function generateNewIdSession () {
				var ID_LENGTH = 20;
				var random = "";
				var randomInts;
				if (typeof window.crypto != "undefined" && typeof window.crypto.getRandomValues != "undefined") {
					randomInts = new Uint32Array(ID_LENGTH);
					window.crypto.getRandomValues(randomInts);
				}
				else {
					randomInts = new Array(ID_LENGTH);
					for (var i = 0; i < ID_LENGTH; i++) {
						randomInts[i] = rnd() * MAX_NUMBER;
					}
				}

				for (var i = 0; i < ID_LENGTH; i++) {
					random += VALID_CHARS_TO_ID.charAt(Math.floor(randomInts[i] % VALID_CHARS_TO_ID.length));
				}

				return random;
			}

			/* Genera un numero aleatorio para utilizar como clave de cifrado. */
			function generateCipherKey() {
				var random;
				if (typeof window.crypto != "undefined" && typeof window.crypto.getRandomValues != "undefined") {
					var randomInts = new Uint32Array(1);
					window.crypto.getRandomValues(randomInts);
					random = zeroFill(randomInts[0] % 100000000, 8);
				}
				else {
					random = zeroFill(Math.floor(((rnd() * MAX_NUMBER) + 1) % 100000000), 8);
				}

				return random;
			}

			/* Completa un numero con ceros a la izquierda. */
			function zeroFill(number, width) {
				width -= number.toString().length;
				if (width > 0) {
					return new Array(width + (/\./.test(number) ? 2 : 1)).join('0')
					+ number;
				}
				return number + "";
			}

			/**
			 * Genera numeros aleatorios con una distribucion homogenea
			 */
			var seed;
			function rnd () {
				if (seed == undefined) {
					seed = new Date().getMilliseconds() * 1000 * Math.random();
				}
			    seed = (seed * 9301 + 49297) % 233280;
			    return seed / 233280;
			}

			/**
			 * Invoca un Intent con la operacion seleccionada, la configuraci\u00F3n establecida y las campos del
			 * formulario pasado como parametro. Si se define un callback para tratar el caso de exito o error de
			 * la operacion, se intentara descargar el resultado devuelto por la app del servidor intermedio de
			 * comunicacion. 
			 *
			 * intentURL: URL para la invocacion del Cliente JavaScript
			 * idSession: Identificador de la sesi\u00F3n para la recuperaci\u00F3n del resultado.
			 * cipherKey: Clave de cifrado para la respuesta del servidor.
			 * successCallback: Actuaci\u00F3n a realizar cuando se recupera el resultado de la operaci&oacute;n.
			 * errorCallback: Actuaci\u00F3n a realizar cuando ocurre un error al recuperar el resultado.
			 */
			function execAppIntent (intentURL, idSession, cipherKey, successCallback, errorCallback) {

				// Invocamos al cliente de firma movil.
				openUrl(intentURL);

				if (successCallback != null || errorCallback != null) {
					if (idSession != null && idSession != undefined && 
							((successCallback != undefined && successCallback != null) ||
									(errorCallback != undefined && errorCallback != null))) {
						getStoredFileFromServlet(idSession, retrieverServletAddress, cipherKey, successCallback, errorCallback);
					}
				}
			}

			/**
			 * Construye una URL para la invocaci&oacute;n del Cliente @firma nativo.
			 *
			 * op: Funcion a invocar en el cliente nativo.
			 * params: Par\u00E1metros para la configuraci\u00F3n de la operaci\u00F3n.
			 */
			function buildUrl (op, params) {

				// Operacion seleccionada
				var intentURL = 'afirma://' + op + '?';
				if (params != null && params != undefined) {
					for (var i = 0; i < params.length; i++) {
						intentURL += (i != 0 ? '&' : '') + params[i].key + '=' + params[i].value; 
					}
				}
				return intentURL;
			}

			/**
			 * Generan un XML con los datos de configuracion de la operacion indicada,
			 * los cifra y lo envia a un servidor para su descarga.
			 * @param cipherKey Clave de cifrado. Si no se indica, no se cifra.
			 * @param storageServletAddress URL del servlet que almacena.
			 * @param op Operacion que se configura.
			 * @param params Parametros de configuracion de la operacion 
			 * @returns El identificador con el que se ha guardado el fichero en servidor o false
			 * si se produjo algun error.  
			 */
			function preProccessData (cipherKey, storageServletAddress, op, params) {

				// Identificador del fichero (equivalente a un id de sesion) del que deben recuperarse los datos
				var fileId = generateNewIdSession(); 

				var httpRequest = getHttpRequest();
				if (!httpRequest) {
					throwException("java.lang.Exception", "Su navegador no permite preprocesar los datos que desea tratar");
				}

				var requestData =
					"op=put&v=1_0&id=" + fileId + "&dat=" + 
					cipher(buildXML(op, params), cipherKey);

				httpRequest.open("POST", storageServletAddress, false);
				httpRequest.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
				try {
					httpRequest.send(requestData);
				}
				catch(e) {
					errorMessage = "No se pudo conectar con el servidor remoto";
					errorType = "java.io.IOException";
				}
				
				if (httpRequest.readyState==4 && httpRequest.status==200) {
					return fileId;	
				}

				return false;
			}

			/**
			 * Construye un XML con lo valores del array de parametros proporcionado.
			 * @param op Operacion que configuramos
			 * @param params Array con los parametros del array. 
			 * @returns XML.
			 */
			function buildXML (op, params) {
				op = (op == null ? "op" : op);
				var xml = '<' + op +'>';
				for (var i = 0; i < params.length; i++) {
					xml += '<e k="' + params[i].key + '" v="' + params[i].value + '"/>';
				}
				return Base64.encode(xml + '</' + op + '>');
			}

			/**
			 * Crea una URL a partir de los parametros introducidos para la invocaci&oacute;n de
			 * una app nativa para que descargue la configuracion de la operaci&oacute;n a realizar.
			 * @param op Codigo de la operacion a la que tiene que invocar la URL.
			 * @param id Identificador para la descarga.
			 * @param rtServlet Servlet para la descarga de la configuraci&oacute;n.
			 * @param cipherKey Clave para el descifrado.
			 * @returns URL para la llamada a la app con los datos necesarios para que descargue
			 * la configuraci&oacute;n de la operaci&oacute;n a realizar.
			 */
			function buildUrlWithoutData (op, id, rtServlet, cipherKey) {
				var j = 0;
				var newParams = new Array();
				newParams[j++] = {key:"fileid", value:id};
				if (rtServlet != null || rtServlet != undefined) {
					newParams[j++] = {key:"rtservlet", value:rtServlet};
				}
				if (cipherKey != null || cipherKey != undefined) {
					newParams[j++] = {key:"key", value:cipherKey};
				}
				return buildUrl(op, newParams);
			};

			/**
			 * Llama a la aplicacion de firma a traves de la URL de invocacion sin que afecte
			 * a la pagina que se esta mostrando.
			 * @param url URL de invocacion.
			 */
			function openUrl (url) {
				
				// Usamos document.location porque tiene mejor soporte por los navegadores que
				// window.location que es el mecanismo estandar
				if (isChrome()) {
					document.location = url;
				}
				else {
					if (document.getElementById("iframeAfirma") != null) {
						document.getElementById("iframeAfirma").src = url;
					}
					else {
						var iframeElem = document.createElement("iframe");

						var idAttr = document.createAttribute("id");
						idAttr.value = "iframeAfirma";
						iframeElem.setAttributeNode(idAttr);

						var srcAttr = document.createAttribute("src");
						srcAttr.value = url;
						iframeElem.setAttributeNode(srcAttr);

						var heightAttr = document.createAttribute("height");
						heightAttr.value = 1;
						iframeElem.setAttributeNode(heightAttr);

						var widthAttr = document.createAttribute("width");
						widthAttr.value = 1;
						iframeElem.setAttributeNode(widthAttr);

						//var styleAttr = document.createAttribute("style");
						//styleAttr.value = "display: none;";
						//iframeElem.setAttributeNode(styleAttr);

						document.body.appendChild(iframeElem);
					}
				}
			}

			var iterations = 0;

			/**
			 * Ejecuta el metodo de error si el html recuperado es tal o el metodo de exito si no lo es,
			 * en cuyo caso previamente descifrara el resultado. 
			 * @param html Resultado obtenido.
			 * @param cipherKey Clave para el descifrado del resultado si no es un error.
			 * @param successCallback Metodo a ejecutar en caso de exito.
			 * @param errorCallback Metodo a ejecutar en caso de error.
			 * @returns Devuelve true si se ha fallado pero se puede volver a reintentar, false en caso de
			 * error determinante o exito.
			 */
			function successResponseFunction (html, cipherKey, successCallback, errorCallback) {

				// Si se obtiene el mensaje de  error de que el identificador no existe, seguimos intentandolo
				if (html.substr(0, 6).toLowerCase() == "err-06") {
					return true;
				}

				// Si se obtiene otro mensaje de error, se deja de intentar y se ejecuta la funcion callback de error
				if (html.substr(0, 4).toLowerCase() == "err-" && html.indexOf(":=") != -1) {
					errorMessage = html.substring(html.indexOf(":=") + 2);
					errorType = "java.lang.Exception";
					errorCallback(errorType, errorMessage);
					return false;
				}

				// Si no se obtuvo un error, habremos recibido la firma y posiblemente el certificado (que antecederia a la
				// firma y se separaria de ella con '|'). Si se definio una clave de cifrado, consideramos que la firma
				// y el certificado (en caso de estar) llegan cifrados. El cifrado de ambos elementos es independiente
				var certificate;
				var signature;
				var sepPos = html == null ? -1 : html.indexOf('|');
				if (sepPos == -1) {
					if (cipherKey != undefined && cipherKey != null) {
						signature = decipher(html, cipherKey);
					}
					else {
						signature = html;
					}
				}
				else {
					if (cipherKey != undefined && cipherKey != null) {
						certificate = decipher(html.substring(0, sepPos), cipherKey);
						signature = decipher(html.substring(sepPos + 1), cipherKey);
					}
					else {
						certificate = html.substring(0, sepPos);
						signature = html.substring(sepPos + 1);
					}
				}

				successCallback(signature, certificate);

				return false;
			}

			function errorResponseFunction (type, message, errorCallback) {

				errorType = (type != null && type.length > 0) ?
						type : "java.lang.Exception";
				errorMessage = (message != null && message.length > 0) ?
						message : "No se ha podido extablecer la comunicaci\u00F3n entre la aplicaci\u00F3n de firma y la p\u00E1gina web";
				if (errorCallback != undefined && errorCallback != null) {
					errorCallback(errorType, errorMessage);
				}
			}

			function getStoredFileFromServlet (idDocument, servletAddress, cipherKey, successCallback, errorCallback) {

				var httpRequest = getHttpRequest();
				if (!httpRequest) {
					throwException("java.lang.Exception", "Su navegador no permite obtener el resulado de la operaci\u00F3n");
				}

				iterations = 0;
				setTimeout(retrieveRequest, 4000, httpRequest, servletAddress, "op=get&v=1_0&id=" + idDocument + "&it=0", cipherKey, successCallback, errorCallback);
			}

			var NUM_MAX_ITERATIONS = 15;

			function retrieveRequest(httpRequest, url, params, cipherKey, successCallback, errorCallback) {

				// Contamos la nueva llamada al servidor
				if (iterations > NUM_MAX_ITERATIONS) {
					errorResponseFunction("java.util.concurrent.TimeoutException", "El tiempo para la recepcion de la firma por la pagina web ha expirado", errorCallback);
					return;
				}
				iterations++;

				httpRequest.open("POST", url, false);
				httpRequest.setRequestHeader("Content-type","application/x-www-form-urlencoded");

				try {
					httpRequest.send(params);
				}
				catch(e) {
					// Error en la llamada para al recuperacion del resultado. No lo encuentra o problema
					// de tipo cross-domain
					errorResponseFunction("java.lang.IOException", "Ocurrio un error de red en la llamada al servicio de firma", errorCallback);
					return;
				}

				if (httpRequest.readyState==4) {
					if (httpRequest.status==200) {
						var needContinue = successResponseFunction(httpRequest.responseText, cipherKey, successCallback, errorCallback);
						if (!needContinue) {
							return;
						}
					}
					else {
						errorResponseFunction(null, httpRequest.responseText, errorCallback);
						return;
					}
				}

				setTimeout(retrieveRequest, 4000, httpRequest, url, params.replace("&it=" + (iterations-1), "&it=" + iterations), cipherKey, successCallback, errorCallback);
			}

			/**
			 * Realiza un descifrado DES compatible con Java (Algoritmo DES, modo CBC, sin Padding).
			 * Recibe en base 64 la cadena de texto cifrado antecedido por el padding anadido manualmente
			 * a los datos para permitir el cifrado DES (separado por un punto ('.')), ademas de la clave
			 * para descifrar.
			 * Como resultado devuelve la cadena de texto descifrada en base 64.
			 */
			function decipher(cipheredData, key) {
								
				var dotPos = cipheredData.indexOf('.');
				var padding = cipheredData.substr(0, dotPos);
				
				var deciphered = des(key, base64ToString(cipheredData.substr(dotPos + 1).replace(/\-/g, "+").replace(/\_/g, "/")), 0, 0, null);
				
				return stringToBase64(deciphered.substr(0, deciphered.length - padding - 8));
			}
			
			/**
			 * Realiza un cifrado DES compatible con Java (Algoritmo DES, modo CBC, sin Padding).
			 * @param dataB64 Cadena de texto base 64.
			 * @param key Clave de cifrado.
			 * @return Base 64 cifrado.
			 */
			function cipher(dataB64, key) {

				var data = base64ToString(dataB64.replace(/\-/g, "+").replace(/\_/g, "/"));
				var padding = (8 - (data.length % 8)) % 8;
				
				// Los datos cifrados los pasamos a base 64 y, antes de devolverlos le anteponemos el padding que
				// le habra agregado el metodo de cifrado separados por un punto ('.').
				return padding  + "." + stringToBase64(des(key, data, 1, 0, null)).replace(/\+/g, "-").replace(/\//g, "_");
			}

			/* Metodos que publicamos del objeto AppAfirmaJS */
			return {
				echo : echo,
				checkTime : checkTime,
				setKeyStore : setKeyStore,
				sign : sign,
				coSign : coSign,
				counterSign : counterSign,
				saveDataToFile : saveDataToFile,
				getFileNameContentBase64: getFileNameContentBase64,
				getMultiFileNameContentBase64 : getMultiFileNameContentBase64,
				getBase64FromText : getBase64FromText,
				getTextFromBase64 : getTextFromBase64,
				setServlets : setServlets,
				setStickySignatory : setStickySignatory,
				setLocale : setLocale,
				getErrorMessage : getErrorMessage,
				getErrorType : getErrorType,
				getCurrentLog : getCurrentLog
			}
		});
		/* Metodos que publicamos del objeto MiniApplet */
		return {
			
			/* Publicamos las variables para la comprobacion de hora. */		
			CHECKTIME_NO : CHECKTIME_NO,
			CHECKTIME_RECOMMENDED : CHECKTIME_RECOMMENDED,
			CHECKTIME_OBLIGATORY : CHECKTIME_OBLIGATORY,
			
			/* Publicamos las variables para establecer parametros personalizados en la JVM */
			JAVA_ARGUMENTS : JAVA_ARGUMENTS,
			SYSTEM_PROPERTIES : SYSTEM_PROPERTIES,
			
			/* Publicamos las variables para configurar un almacen de certificados concreto. */		
			KEYSTORE_WINDOWS : KEYSTORE_WINDOWS,
			KEYSTORE_APPLE : KEYSTORE_APPLE,
			KEYSTORE_PKCS12 : KEYSTORE_PKCS12,
			KEYSTORE_PKCS11 : KEYSTORE_PKCS11,
			KEYSTORE_FIREFOX : KEYSTORE_FIREFOX,
			KEYSTORE_JAVA : KEYSTORE_JAVA,
			KEYSTORE_JCEKS : KEYSTORE_JCEKS,
			KEYSTORE_JAVACE : KEYSTORE_JAVACE,
			
			/* Metodos visibles. */
			cargarMiniApplet : cargarMiniApplet,
			cargarAppAfirma : cargarAppAfirma,
			echo : echo,
			checkTime : checkTime,
			sign : sign,
			coSign : coSign,
			counterSign : counterSign,
			saveDataToFile : saveDataToFile,
			getFileNameContentBase64: getFileNameContentBase64,
			getMultiFileNameContentBase64 : getMultiFileNameContentBase64,
			getBase64FromText : getBase64FromText,
			getTextFromBase64 : getTextFromBase64,
			setServlets : setServlets,
			setStickySignatory : setStickySignatory,
			setLocale : setLocale,
			getErrorMessage : getErrorMessage,
			getErrorType : getErrorType,
			getCurrentLog : getCurrentLog,
			isAndroid : isAndroid,
			isIOS : isIOS
		};
})(window, undefined);





/**
 *  Base64 encode / decode
 *  http://www.webtoolkit.info/
 */
var Base64 = {

		// private property
		_keyStr : "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=",
		_keyStr_URL_SAFE : "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_=",

		// public method for encoding
		encode : function (input, URL_SAFE) {
			
			var keyStr = (URL_SAFE == true) ? this._keyStr_URL_SAFE : this._keyStr;
			
			var output = "";
			var chr1, chr2, chr3, enc1, enc2, enc3, enc4;
			var i = 0;

			input = Base64._utf8_encode(input);

			while (i < input.length) {

				chr1 = input.charCodeAt(i++);
				chr2 = input.charCodeAt(i++);
				chr3 = input.charCodeAt(i++);

				enc1 = chr1 >> 2;
				enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
				enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
				enc4 = chr3 & 63;

				if (isNaN(chr2)) {
					enc3 = enc4 = 64;
				} else if (isNaN(chr3)) {
					enc4 = 64;
				}

				output = output +
				keyStr.charAt(enc1) + keyStr.charAt(enc2) +
				keyStr.charAt(enc3) + keyStr.charAt(enc4);

			}

			return output;
		},

		// public method for decoding
		decode : function (input, URL_SAFE) {
			
			var keyStr = (URL_SAFE == true) ? this._keyStr_URL_SAFE : this._keyStr;
			
			var output = "";
			var chr1, chr2, chr3;
			var enc1, enc2, enc3, enc4;
			var i = 0;

			input = (URL_SAFE == true) ? input.replace(/[^A-Za-z0-9\-\_\=]/g, "") : input.replace(/[^A-Za-z0-9\+\/\=]/g, "");

			while (i < input.length) {

				enc1 = keyStr.indexOf(input.charAt(i++));
				enc2 = keyStr.indexOf(input.charAt(i++));
				enc3 = keyStr.indexOf(input.charAt(i++));
				enc4 = keyStr.indexOf(input.charAt(i++));

				chr1 = (enc1 << 2) | (enc2 >> 4);
				chr2 = ((enc2 & 15) << 4) | (enc3 >> 2);
				chr3 = ((enc3 & 3) << 6) | enc4;

				output = output + String.fromCharCode(chr1);

				if (enc3 != 64) {
					output = output + String.fromCharCode(chr2);
				}
				if (enc4 != 64) {
					output = output + String.fromCharCode(chr3);
				}

			}

			output = Base64._utf8_decode(output);

			return output;

		},

		// private method for UTF-8 encoding
		_utf8_encode : function (string) {
			string = string.replace(/\r\n/g,"\n");
			var utftext = "";

			for (var n = 0; n < string.length; n++) {

				var c = string.charCodeAt(n);

				if (c < 128) {
					utftext += String.fromCharCode(c);
				}
				else if((c > 127) && (c < 2048)) {
					utftext += String.fromCharCode((c >> 6) | 192);
					utftext += String.fromCharCode((c & 63) | 128);
				}
				else {
					utftext += String.fromCharCode((c >> 12) | 224);
					utftext += String.fromCharCode(((c >> 6) & 63) | 128);
					utftext += String.fromCharCode((c & 63) | 128);
				}

			}

			return utftext;
		},

		// private method for UTF-8 decoding
		_utf8_decode : function (utftext) {
			var string = "";
			var i = 0;
			var c = c1 = c2 = 0;

			while ( i < utftext.length ) {

				c = utftext.charCodeAt(i);

				if (c < 128) {
					string += String.fromCharCode(c);
					i++;
				}
				else if((c > 191) && (c < 224)) {
					c2 = utftext.charCodeAt(i+1);
					string += String.fromCharCode(((c & 31) << 6) | (c2 & 63));
					i += 2;
				}
				else {
					c2 = utftext.charCodeAt(i+1);
					c3 = utftext.charCodeAt(i+2);
					string += String.fromCharCode(((c & 15) << 12) | ((c2 & 63) << 6) | (c3 & 63));
					i += 3;
				}
			}

			return string;
		}
};

//Paul Tero, July 2001
//http://www.tero.co.uk/des/

//Optimised for performance with large blocks by Michael Hayworth, November 2001
//http://www.netdealing.com

//THIS SOFTWARE IS PROVIDED "AS IS" AND
//ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
//IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
//ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
//FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
//DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
//OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
//HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
//LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
//OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
//SUCH DAMAGE.

//des
//this takes the key, the message, and whether to encrypt or decrypt
function des (key, message, encrypt, mode, iv, padding) {
	  //declaring this locally speeds things up a bit
	  var spfunction1 = new Array (0x1010400,0,0x10000,0x1010404,0x1010004,0x10404,0x4,0x10000,0x400,0x1010400,0x1010404,0x400,0x1000404,0x1010004,0x1000000,0x4,0x404,0x1000400,0x1000400,0x10400,0x10400,0x1010000,0x1010000,0x1000404,0x10004,0x1000004,0x1000004,0x10004,0,0x404,0x10404,0x1000000,0x10000,0x1010404,0x4,0x1010000,0x1010400,0x1000000,0x1000000,0x400,0x1010004,0x10000,0x10400,0x1000004,0x400,0x4,0x1000404,0x10404,0x1010404,0x10004,0x1010000,0x1000404,0x1000004,0x404,0x10404,0x1010400,0x404,0x1000400,0x1000400,0,0x10004,0x10400,0,0x1010004);
	  var spfunction2 = new Array (-0x7fef7fe0,-0x7fff8000,0x8000,0x108020,0x100000,0x20,-0x7fefffe0,-0x7fff7fe0,-0x7fffffe0,-0x7fef7fe0,-0x7fef8000,-0x80000000,-0x7fff8000,0x100000,0x20,-0x7fefffe0,0x108000,0x100020,-0x7fff7fe0,0,-0x80000000,0x8000,0x108020,-0x7ff00000,0x100020,-0x7fffffe0,0,0x108000,0x8020,-0x7fef8000,-0x7ff00000,0x8020,0,0x108020,-0x7fefffe0,0x100000,-0x7fff7fe0,-0x7ff00000,-0x7fef8000,0x8000,-0x7ff00000,-0x7fff8000,0x20,-0x7fef7fe0,0x108020,0x20,0x8000,-0x80000000,0x8020,-0x7fef8000,0x100000,-0x7fffffe0,0x100020,-0x7fff7fe0,-0x7fffffe0,0x100020,0x108000,0,-0x7fff8000,0x8020,-0x80000000,-0x7fefffe0,-0x7fef7fe0,0x108000);
	  var spfunction3 = new Array (0x208,0x8020200,0,0x8020008,0x8000200,0,0x20208,0x8000200,0x20008,0x8000008,0x8000008,0x20000,0x8020208,0x20008,0x8020000,0x208,0x8000000,0x8,0x8020200,0x200,0x20200,0x8020000,0x8020008,0x20208,0x8000208,0x20200,0x20000,0x8000208,0x8,0x8020208,0x200,0x8000000,0x8020200,0x8000000,0x20008,0x208,0x20000,0x8020200,0x8000200,0,0x200,0x20008,0x8020208,0x8000200,0x8000008,0x200,0,0x8020008,0x8000208,0x20000,0x8000000,0x8020208,0x8,0x20208,0x20200,0x8000008,0x8020000,0x8000208,0x208,0x8020000,0x20208,0x8,0x8020008,0x20200);
	  var spfunction4 = new Array (0x802001,0x2081,0x2081,0x80,0x802080,0x800081,0x800001,0x2001,0,0x802000,0x802000,0x802081,0x81,0,0x800080,0x800001,0x1,0x2000,0x800000,0x802001,0x80,0x800000,0x2001,0x2080,0x800081,0x1,0x2080,0x800080,0x2000,0x802080,0x802081,0x81,0x800080,0x800001,0x802000,0x802081,0x81,0,0,0x802000,0x2080,0x800080,0x800081,0x1,0x802001,0x2081,0x2081,0x80,0x802081,0x81,0x1,0x2000,0x800001,0x2001,0x802080,0x800081,0x2001,0x2080,0x800000,0x802001,0x80,0x800000,0x2000,0x802080);
	  var spfunction5 = new Array (0x100,0x2080100,0x2080000,0x42000100,0x80000,0x100,0x40000000,0x2080000,0x40080100,0x80000,0x2000100,0x40080100,0x42000100,0x42080000,0x80100,0x40000000,0x2000000,0x40080000,0x40080000,0,0x40000100,0x42080100,0x42080100,0x2000100,0x42080000,0x40000100,0,0x42000000,0x2080100,0x2000000,0x42000000,0x80100,0x80000,0x42000100,0x100,0x2000000,0x40000000,0x2080000,0x42000100,0x40080100,0x2000100,0x40000000,0x42080000,0x2080100,0x40080100,0x100,0x2000000,0x42080000,0x42080100,0x80100,0x42000000,0x42080100,0x2080000,0,0x40080000,0x42000000,0x80100,0x2000100,0x40000100,0x80000,0,0x40080000,0x2080100,0x40000100);
	  var spfunction6 = new Array (0x20000010,0x20400000,0x4000,0x20404010,0x20400000,0x10,0x20404010,0x400000,0x20004000,0x404010,0x400000,0x20000010,0x400010,0x20004000,0x20000000,0x4010,0,0x400010,0x20004010,0x4000,0x404000,0x20004010,0x10,0x20400010,0x20400010,0,0x404010,0x20404000,0x4010,0x404000,0x20404000,0x20000000,0x20004000,0x10,0x20400010,0x404000,0x20404010,0x400000,0x4010,0x20000010,0x400000,0x20004000,0x20000000,0x4010,0x20000010,0x20404010,0x404000,0x20400000,0x404010,0x20404000,0,0x20400010,0x10,0x4000,0x20400000,0x404010,0x4000,0x400010,0x20004010,0,0x20404000,0x20000000,0x400010,0x20004010);
	  var spfunction7 = new Array (0x200000,0x4200002,0x4000802,0,0x800,0x4000802,0x200802,0x4200800,0x4200802,0x200000,0,0x4000002,0x2,0x4000000,0x4200002,0x802,0x4000800,0x200802,0x200002,0x4000800,0x4000002,0x4200000,0x4200800,0x200002,0x4200000,0x800,0x802,0x4200802,0x200800,0x2,0x4000000,0x200800,0x4000000,0x200800,0x200000,0x4000802,0x4000802,0x4200002,0x4200002,0x2,0x200002,0x4000000,0x4000800,0x200000,0x4200800,0x802,0x200802,0x4200800,0x802,0x4000002,0x4200802,0x4200000,0x200800,0,0x2,0x4200802,0,0x200802,0x4200000,0x800,0x4000002,0x4000800,0x800,0x200002);
	  var spfunction8 = new Array (0x10001040,0x1000,0x40000,0x10041040,0x10000000,0x10001040,0x40,0x10000000,0x40040,0x10040000,0x10041040,0x41000,0x10041000,0x41040,0x1000,0x40,0x10040000,0x10000040,0x10001000,0x1040,0x41000,0x40040,0x10040040,0x10041000,0x1040,0,0,0x10040040,0x10000040,0x10001000,0x41040,0x40000,0x41040,0x40000,0x10041000,0x1000,0x40,0x10040040,0x1000,0x41040,0x10001000,0x40,0x10000040,0x10040000,0x10040040,0x10000000,0x40000,0x10001040,0,0x10041040,0x40040,0x10000040,0x10040000,0x10001000,0x10001040,0,0x10041040,0x41000,0x41000,0x1040,0x1040,0x40040,0x10000000,0x10041000);

	  //create the 16 or 48 subkeys we will need
	  var keys = des_createKeys (key);
	  var m=0, i, j, temp, right1, right2, left, right, looping;
	  var cbcleft, cbcleft2, cbcright, cbcright2;
	  var endloop, loopinc;
	  var len = message.length;
	  var chunk = 0;
	  //set up the loops for single and triple des
	  var iterations = keys.length == 32 ? 3 : 9; //single or triple des
	  if (iterations == 3) {looping = encrypt ? new Array (0, 32, 2) : new Array (30, -2, -2);}
	  else {looping = encrypt ? new Array (0, 32, 2, 62, 30, -2, 64, 96, 2) : new Array (94, 62, -2, 32, 64, 2, 30, -2, -2);}

	  //pad the message depending on the padding parameter
	  if (padding == 2) message += "        "; //pad the message with spaces
	  else if (padding == 1) {temp = 8-(len%8); message += String.fromCharCode (temp,temp,temp,temp,temp,temp,temp,temp); if (temp==8) len+=8;} //PKCS7 padding
	  else if (!padding) message += "\0\0\0\0\0\0\0\0"; //pad the message out with null bytes

	  //store the result here
	  var result = "";
	  var tempresult = "";

	  if (mode == 1) { //CBC mode
	    cbcleft = (iv.charCodeAt(m++) << 24) | (iv.charCodeAt(m++) << 16) | (iv.charCodeAt(m++) << 8) | iv.charCodeAt(m++);
	    cbcright = (iv.charCodeAt(m++) << 24) | (iv.charCodeAt(m++) << 16) | (iv.charCodeAt(m++) << 8) | iv.charCodeAt(m++);
	    m=0;
	  }

	  //loop through each 64 bit chunk of the message
	  while (m < len) {
	    left = (message.charCodeAt(m++) << 24) | (message.charCodeAt(m++) << 16) | (message.charCodeAt(m++) << 8) | message.charCodeAt(m++);
	    right = (message.charCodeAt(m++) << 24) | (message.charCodeAt(m++) << 16) | (message.charCodeAt(m++) << 8) | message.charCodeAt(m++);

	    //for Cipher Block Chaining mode, xor the message with the previous result
	    if (mode == 1) {if (encrypt) {left ^= cbcleft; right ^= cbcright;} else {cbcleft2 = cbcleft; cbcright2 = cbcright; cbcleft = left; cbcright = right;}}

	    //first each 64 but chunk of the message must be permuted according to IP
	    temp = ((left >>> 4) ^ right) & 0x0f0f0f0f; right ^= temp; left ^= (temp << 4);
	    temp = ((left >>> 16) ^ right) & 0x0000ffff; right ^= temp; left ^= (temp << 16);
	    temp = ((right >>> 2) ^ left) & 0x33333333; left ^= temp; right ^= (temp << 2);
	    temp = ((right >>> 8) ^ left) & 0x00ff00ff; left ^= temp; right ^= (temp << 8);
	    temp = ((left >>> 1) ^ right) & 0x55555555; right ^= temp; left ^= (temp << 1);

	    left = ((left << 1) | (left >>> 31)); 
	    right = ((right << 1) | (right >>> 31)); 

	    //do this either 1 or 3 times for each chunk of the message
	    for (j=0; j<iterations; j+=3) {
	      endloop = looping[j+1];
	      loopinc = looping[j+2];
	      //now go through and perform the encryption or decryption  
	      for (i=looping[j]; i!=endloop; i+=loopinc) { //for efficiency
	        right1 = right ^ keys[i]; 
	        right2 = ((right >>> 4) | (right << 28)) ^ keys[i+1];
	        //the result is attained by passing these bytes through the S selection functions
	        temp = left;
	        left = right;
	        right = temp ^ (spfunction2[(right1 >>> 24) & 0x3f] | spfunction4[(right1 >>> 16) & 0x3f]
	              | spfunction6[(right1 >>>  8) & 0x3f] | spfunction8[right1 & 0x3f]
	              | spfunction1[(right2 >>> 24) & 0x3f] | spfunction3[(right2 >>> 16) & 0x3f]
	              | spfunction5[(right2 >>>  8) & 0x3f] | spfunction7[right2 & 0x3f]);
	      }
	      temp = left; left = right; right = temp; //unreverse left and right
	    } //for either 1 or 3 iterations

	    //move then each one bit to the right
	    left = ((left >>> 1) | (left << 31)); 
	    right = ((right >>> 1) | (right << 31)); 

	    //now perform IP-1, which is IP in the opposite direction
	    temp = ((left >>> 1) ^ right) & 0x55555555; right ^= temp; left ^= (temp << 1);
	    temp = ((right >>> 8) ^ left) & 0x00ff00ff; left ^= temp; right ^= (temp << 8);
	    temp = ((right >>> 2) ^ left) & 0x33333333; left ^= temp; right ^= (temp << 2);
	    temp = ((left >>> 16) ^ right) & 0x0000ffff; right ^= temp; left ^= (temp << 16);
	    temp = ((left >>> 4) ^ right) & 0x0f0f0f0f; right ^= temp; left ^= (temp << 4);

	    //for Cipher Block Chaining mode, xor the message with the previous result
	    if (mode == 1) {if (encrypt) {cbcleft = left; cbcright = right;} else {left ^= cbcleft2; right ^= cbcright2;}}
	    tempresult += String.fromCharCode ((left>>>24), ((left>>>16) & 0xff), ((left>>>8) & 0xff), (left & 0xff), (right>>>24), ((right>>>16) & 0xff), ((right>>>8) & 0xff), (right & 0xff));

	    chunk += 8;
	    if (chunk == 512) {result += tempresult; tempresult = ""; chunk = 0;}
	  } //for every 8 characters, or 64 bits in the message

	  //return the result as an array
	  return result + tempresult;
	} //end of des

	//des_createKeys
	//this takes as input a 64 bit key (even though only 56 bits are used)
	//as an array of 2 integers, and returns 16 48 bit keys
	function des_createKeys (key) {
	  //declaring this locally speeds things up a bit
	  pc2bytes0  = new Array (0,0x4,0x20000000,0x20000004,0x10000,0x10004,0x20010000,0x20010004,0x200,0x204,0x20000200,0x20000204,0x10200,0x10204,0x20010200,0x20010204);
	  pc2bytes1  = new Array (0,0x1,0x100000,0x100001,0x4000000,0x4000001,0x4100000,0x4100001,0x100,0x101,0x100100,0x100101,0x4000100,0x4000101,0x4100100,0x4100101);
	  pc2bytes2  = new Array (0,0x8,0x800,0x808,0x1000000,0x1000008,0x1000800,0x1000808,0,0x8,0x800,0x808,0x1000000,0x1000008,0x1000800,0x1000808);
	  pc2bytes3  = new Array (0,0x200000,0x8000000,0x8200000,0x2000,0x202000,0x8002000,0x8202000,0x20000,0x220000,0x8020000,0x8220000,0x22000,0x222000,0x8022000,0x8222000);
	  pc2bytes4  = new Array (0,0x40000,0x10,0x40010,0,0x40000,0x10,0x40010,0x1000,0x41000,0x1010,0x41010,0x1000,0x41000,0x1010,0x41010);
	  pc2bytes5  = new Array (0,0x400,0x20,0x420,0,0x400,0x20,0x420,0x2000000,0x2000400,0x2000020,0x2000420,0x2000000,0x2000400,0x2000020,0x2000420);
	  pc2bytes6  = new Array (0,0x10000000,0x80000,0x10080000,0x2,0x10000002,0x80002,0x10080002,0,0x10000000,0x80000,0x10080000,0x2,0x10000002,0x80002,0x10080002);
	  pc2bytes7  = new Array (0,0x10000,0x800,0x10800,0x20000000,0x20010000,0x20000800,0x20010800,0x20000,0x30000,0x20800,0x30800,0x20020000,0x20030000,0x20020800,0x20030800);
	  pc2bytes8  = new Array (0,0x40000,0,0x40000,0x2,0x40002,0x2,0x40002,0x2000000,0x2040000,0x2000000,0x2040000,0x2000002,0x2040002,0x2000002,0x2040002);
	  pc2bytes9  = new Array (0,0x10000000,0x8,0x10000008,0,0x10000000,0x8,0x10000008,0x400,0x10000400,0x408,0x10000408,0x400,0x10000400,0x408,0x10000408);
	  pc2bytes10 = new Array (0,0x20,0,0x20,0x100000,0x100020,0x100000,0x100020,0x2000,0x2020,0x2000,0x2020,0x102000,0x102020,0x102000,0x102020);
	  pc2bytes11 = new Array (0,0x1000000,0x200,0x1000200,0x200000,0x1200000,0x200200,0x1200200,0x4000000,0x5000000,0x4000200,0x5000200,0x4200000,0x5200000,0x4200200,0x5200200);
	  pc2bytes12 = new Array (0,0x1000,0x8000000,0x8001000,0x80000,0x81000,0x8080000,0x8081000,0x10,0x1010,0x8000010,0x8001010,0x80010,0x81010,0x8080010,0x8081010);
	  pc2bytes13 = new Array (0,0x4,0x100,0x104,0,0x4,0x100,0x104,0x1,0x5,0x101,0x105,0x1,0x5,0x101,0x105);

	  //how many iterations (1 for des, 3 for triple des)
	  var iterations = key.length > 8 ? 3 : 1; //changed by Paul 16/6/2007 to use Triple DES for 9+ byte keys
	  //stores the return keys
	  var keys = new Array (32 * iterations);
	  //now define the left shifts which need to be done
	  var shifts = new Array (0, 0, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 0);
	  //other variables
	  var lefttemp, righttemp, m=0, n=0, temp;

	  for (var j=0; j<iterations; j++) { //either 1 or 3 iterations
	    left = (key.charCodeAt(m++) << 24) | (key.charCodeAt(m++) << 16) | (key.charCodeAt(m++) << 8) | key.charCodeAt(m++);
	    right = (key.charCodeAt(m++) << 24) | (key.charCodeAt(m++) << 16) | (key.charCodeAt(m++) << 8) | key.charCodeAt(m++);

	    temp = ((left >>> 4) ^ right) & 0x0f0f0f0f; right ^= temp; left ^= (temp << 4);
	    temp = ((right >>> -16) ^ left) & 0x0000ffff; left ^= temp; right ^= (temp << -16);
	    temp = ((left >>> 2) ^ right) & 0x33333333; right ^= temp; left ^= (temp << 2);
	    temp = ((right >>> -16) ^ left) & 0x0000ffff; left ^= temp; right ^= (temp << -16);
	    temp = ((left >>> 1) ^ right) & 0x55555555; right ^= temp; left ^= (temp << 1);
	    temp = ((right >>> 8) ^ left) & 0x00ff00ff; left ^= temp; right ^= (temp << 8);
	    temp = ((left >>> 1) ^ right) & 0x55555555; right ^= temp; left ^= (temp << 1);

	    //the right side needs to be shifted and to get the last four bits of the left side
	    temp = (left << 8) | ((right >>> 20) & 0x000000f0);
	    //left needs to be put upside down
	    left = (right << 24) | ((right << 8) & 0xff0000) | ((right >>> 8) & 0xff00) | ((right >>> 24) & 0xf0);
	    right = temp;

	    //now go through and perform these shifts on the left and right keys
	    for (var i=0; i < shifts.length; i++) {
	      //shift the keys either one or two bits to the left
	      if (shifts[i]) {left = (left << 2) | (left >>> 26); right = (right << 2) | (right >>> 26);}
	      else {left = (left << 1) | (left >>> 27); right = (right << 1) | (right >>> 27);}
	      left &= -0xf; right &= -0xf;

	      //now apply PC-2, in such a way that E is easier when encrypting or decrypting
	      //this conversion will look like PC-2 except only the last 6 bits of each byte are used
	      //rather than 48 consecutive bits and the order of lines will be according to 
	      //how the S selection functions will be applied: S2, S4, S6, S8, S1, S3, S5, S7
	      lefttemp = pc2bytes0[left >>> 28] | pc2bytes1[(left >>> 24) & 0xf]
	              | pc2bytes2[(left >>> 20) & 0xf] | pc2bytes3[(left >>> 16) & 0xf]
	              | pc2bytes4[(left >>> 12) & 0xf] | pc2bytes5[(left >>> 8) & 0xf]
	              | pc2bytes6[(left >>> 4) & 0xf];
	      righttemp = pc2bytes7[right >>> 28] | pc2bytes8[(right >>> 24) & 0xf]
	                | pc2bytes9[(right >>> 20) & 0xf] | pc2bytes10[(right >>> 16) & 0xf]
	                | pc2bytes11[(right >>> 12) & 0xf] | pc2bytes12[(right >>> 8) & 0xf]
	                | pc2bytes13[(right >>> 4) & 0xf];
	      temp = ((righttemp >>> 16) ^ lefttemp) & 0x0000ffff; 
	      keys[n++] = lefttemp ^ temp; keys[n++] = righttemp ^ (temp << 16);
	    }
	  } //for each iterations
	  //return the keys we've created
	  return keys;
	} //end of des_createKeys

//Convierte una cadena a Base 64. Debido a un error en el algoritmo original, pasaremos
// de cadena a hexadecimal y de hexadecimal a Base64
function stringToBase64 (s) {
	return hexToBase64(stringToHex(s));
}

//Convert a base64 string into a normal string
function base64ToString (s) {
  //the base 64 characters
  var BASE64 = new Array ('A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','0','1','2','3','4','5','6','7','8','9','+','/');
	
  var decode = new Object();
  for (var i=0; i<BASE64.length; i++) {decode[BASE64[i]] = i;} //inverse of the array
  decode['='] = 0; //add the equals sign as well
  var r = "", c1, c2, c3, c4, len=s.length; //define variables
  s += "===="; //just to make sure it is padded correctly
  for (var i=0; i<len; i+=4) { //4 input characters at a time
    c1 = s.charAt(i); //the 1st base64 input characther
    c2 = s.charAt(i+1);
    c3 = s.charAt(i+2);
    c4 = s.charAt(i+3);
    r += String.fromCharCode (((decode[c1] << 2) & 0xff) | (decode[c2] >> 4)); //reform the string
    if (c3 != '=') r += String.fromCharCode (((decode[c2] << 4) & 0xff) | (decode[c3] >> 2));
    if (c4 != '=') r += String.fromCharCode (((decode[c3] << 6) & 0xff) | decode[c4]);
  }
  return r;
}

function stringToHex (s) {
	  var r = "";
	  var hexes = new Array ("0","1","2","3","4","5","6","7","8","9","a","b","c","d","e","f");
	  for (var i=0; i<s.length; i++) {r += hexes [s.charCodeAt(i) >> 4] + hexes [s.charCodeAt(i) & 0xf];}
	  return r;
}

// --- Funciones para pasar de Hexadecimal a base64

var tableStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
var tableStr_URL_SAFE = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";

function btoa (bin, urlSafe) {
	var table = (urlSafe == true ? tableStr_URL_SAFE : tableStr).split("");
	
	for (var i = 0, j = 0, len = bin.length / 3, base64 = []; i < len; ++i) {
		var a = bin.charCodeAt(j++), b = bin.charCodeAt(j++), c = bin.charCodeAt(j++);
		if ((a | b | c) > 255) throw new Error("String contains an invalid character");
		base64[base64.length] = table[a >> 2] + table[((a << 4) & 63) | (b >> 4)] +
		(isNaN(b) ? "=" : table[((b << 2) & 63) | (c >> 6)]) +
		(isNaN(b + c) ? "=" : table[c & 63]);
	}
	return base64.join("");
}

function hexToBase64(str, urlSafe) {
	var byteString;
	var byteArray = str.replace(/\r|\n/g, "").replace(/([\da-fA-F]{2}) ?/g, "0x$1 ").replace(/ +$/, "").split(" ");
	try {
		byteString = String.fromCharCode.apply(null, byteArray);
	} catch (e) {
		var strTemp = "";
		for (var i = 0, len = byteArray.length; i < len; i++) {
			strTemp += String.fromCharCode(byteArray[i]);
		}
		byteString = strTemp;
	}
	
	return btoa(byteString, urlSafe);
}
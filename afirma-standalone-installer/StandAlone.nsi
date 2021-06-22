;Include Modern UI
  !include "MUI.nsh"

;Seleccionamos el algoritmo de compresi�n utilizado para comprimir nuestra aplicaci�n
SetCompressor lzma


;--------------------------------
;Con esta opcion alertamos al usuario y le pedimos confirmaci�n para abortar
;la instalaci�n
;Esta macro debe colocarse en esta posici�n del script sino no funcionara
	

  !define MUI_ABORTWARNING
  !define MUI_HEADERIMAGE
  !define MUI_HEADERIMAGE_BITMAP "ic_head.bmp"
  !define MUI_HEADERIMAGE_UNBITMAP "ic_head.bmp"
  !define MUI_WELCOMEFINISHPAGE_BITMAP "ic_install.bmp"
  !define MUI_UNWELCOMEFINISHPAGE_BITMAP "ic_install.bmp"
   
;Definimos el valor de la variable VERSION, en caso de no definirse en el script
;podria ser definida en el compilador
!define VERSION "2.0"


;--------------------------------
;Pages
  
  ;Mostramos la p�gina de bienvenida
  !insertmacro MUI_PAGE_WELCOME
  ;P�gina donde mostramos el contrato de licencia 
  !insertmacro MUI_PAGE_LICENSE "licencia.txt"
  ;p�gina donde se muestran las distintas secciones definidas
  !insertmacro MUI_PAGE_COMPONENTS
  ;p�gina donde se selecciona el directorio donde instalar nuestra aplicacion
  !insertmacro MUI_PAGE_DIRECTORY
  ;p�gina de instalaci�n de ficheros
  !insertmacro MUI_PAGE_INSTFILES
  ;p�gina final
  !insertmacro MUI_PAGE_FINISH
  
;p�ginas referentes al desinstalador
  !insertmacro MUI_UNPAGE_WELCOME
  !insertmacro MUI_UNPAGE_CONFIRM
  !insertmacro MUI_UNPAGE_INSTFILES
  !insertmacro MUI_UNPAGE_FINISH
  
 
;--------------------------------
;Languages
 
  !insertmacro MUI_LANGUAGE "Spanish"

; Para generar instaladores en diferentes idiomas podemos escribir lo siguiente:
;  !insertmacro MUI_LANGUAGE ${LANGUAGE}
; De esta forma pasando la variable LANGUAGE al compilador podremos generar
;paquetes en distintos idiomas sin cambiar el script

;;;;;;;;;;;;;;;;;;;;;;;;;
; Configuration General ;
;;;;;;;;;;;;;;;;;;;;;;;;;
;Nuestro instalador se llamara si la version fuera la 1.0: Ejemplo-1.0.exe
OutFile ClienteStandAlone_${VERSION}.exe

;Aqui comprobamos que en la versi�n Inglesa se muestra correctamente el mensaje:
;Welcome to the $Name Setup Wizard
;Al tener reservado un espacio fijo para este mensaje, y al ser
;la frase en espa�ol mas larga:
; Bienvenido al Asistente de Instalaci�n de Aplicaci�n $Name
; no se ve el contenido de la variable $Name si el tama�o es muy grande
Name "Cliente @firma"
Caption "Instalador de Firma @firma StandAlone"
Icon ic_launcher.ico

;Comprobacion de integridad del fichero activada
CRCCheck on
;Estilos visuales del XP activados
XPStyle on

/*
	Declaracion de variables a usar
	
*/
# tambi�n comprobamos los distintos
; tipos de comentarios que nos permite este lenguaje de script

Var PATH
Var PATH_ACCESO_DIRECTO
;Indicamos cual sera el directorio por defecto donde instalaremos nuestra
;aplicaci�n, el usuario puede cambiar este valor en tiempo de ejecuci�n.
InstallDir "$PROGRAMFILES\StandAlone"

; check if the program has already been installed, if so, take this dir
; as install dir
InstallDirRegKey HKLM SOFTWARE\StandAlone "Install_Dir"
;Mensaje que mostraremos para indicarle al usuario que seleccione un directorio
DirText "Elija un directorio donde instalar la aplicaci�n:"

;Indicamos que cuando la instalaci�n se complete no se cierre el instalador autom�ticamente
AutoCloseWindow false
;Mostramos todos los detalles del la instalaci�n al usuario.
ShowInstDetails show
;En caso de encontrarse los ficheros se sobreescriben
SetOverwrite on
;Optimizamos nuestro paquete en tiempo de compilaci�n, es �ltamente recomendable habilitar siempre esta opci�n
SetDatablockOptimize on
;Habilitamos la compresi�n de nuestro instalador
SetCompress auto
;Personalizamos el mensaje de desinstalaci�n
UninstallText "Desinstalador de Firma con @firma StandAlone."


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Install settings                                                    ;
; En esta secci�n a�adimos los ficheros que forman nuestra aplicaci�n ;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

Section "Programa"
	StrCpy $PATH "StandAlone"
	StrCpy $PATH_ACCESO_DIRECTO "StandAlone"

	SetOutPath $INSTDIR\$PATH

;Incluimos todos los ficheros que componen nuestra aplicaci�n
	File  StandAlone.exe
	File  licencia.txt

;Hacemos que la instalaci�n se realice para todos los usuarios del sistema
        SetShellVarContext all

;Creamos tambi�n el aceso directo al instalador

	;creamos un acceso directo en el escitorio
	CreateShortCut "$DESKTOP\Cliente @firma.lnk" "$INSTDIR\StandAlone\StandAlone.exe"

	;Menu items
	CreateDirectory "$SMPROGRAMS\StandAlone"
	CreateShortCut "$SMPROGRAMS\StandAlone\Cliente @firma.lnk" "$INSTDIR\StandAlone\StandAlone.exe"
	CreateShortCut "$SMPROGRAMS\StandAlone\unistall.lnk" "$INSTDIR\unistall.exe"

	
	;A�ade una entrada en la lista de "Program and Features"
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$PATH \" "DisplayName" "StandAlone ${VERSION}"
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$PATH \" "UninstallString" "$INSTDIR\unistall.exe"
	
	WriteUninstaller "unistall.exe"

	WriteRegStr HKLM SOFTWARE\$PATH "InstallDir" $INSTDIR
	WriteRegStr HKLM SOFTWARE\$PATH "Version" "${VERSION}"

	;Exec "explorer $SMPROGRAMS\$PATH_ACCESO_DIRECTO\"
	
	;Registry
	;CascadeAfirma.reg
	;WriteRegStr HKEY_CLASSES_ROOT "*\shell\standalone.sign" "" "Firmar con 'Cliente @firma'"
	;WriteRegStr HKEY_CLASSES_ROOT "*\shell\standalone.sign" "Icon" "$INSTDIR\StandAlone\StandAlone.exe"
	;WriteRegStr HKEY_CLASSES_ROOT "*\shell\standalone.sign\command" "" "$INSTDIR\StandAlone\StandAlone.exe %1"
		
	

SectionEnd


;;;;;;;;;;;;;;;;;;;;;;
; Uninstall settings ;
;;;;;;;;;;;;;;;;;;;;;;

Section "Uninstall"
	StrCpy $PATH "StandAlone"
	StrCpy $PATH_ACCESO_DIRECTO "StandAlone"
    
    SetShellVarContext all

	RMDir /r $INSTDIR\$PATH
	;remove instalation directory
	RMDir /r $INSTDIR 
	
	;delete start menu shortcuts
	Delete "$DESKTOP\Cliente @firma.lnk"
	RMDir /r $SMPROGRAMS\$PATH_ACCESO_DIRECTO


	DeleteRegKey HKLM "SOFTWARE\$PATH"
    DeleteRegKey HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$PATH \"
	
	;DeleteRegKey HKEY_CLASSES_ROOT "*\shell\standalone.sign"
	
SectionEnd

/**
 * 
 */
package ar.gov.validation.ws.rest.dto;

/**
 * @author gov.ar
 *
 */
public class ResumenResp {

	    //Datos del Subject, osea del firmante
		public String asunto_CN;
		public String asunto_SerialNumber;
		//Datos generales del certificado
		public String emisor;
		public String firmante;
		public String correo;
		public String valido_desde;
		public String valido_hasta;
		public String num_serie;
		public String politica_cert;
		public String uso_certificado;
		//Datos del servidor
		public String fecha_consulta;
	

		/**
		 * @return the asunto_CN
		 */
		public String getAsunto_CN() {
			return asunto_CN;
		}
		/**
		 * @param asunto_CN the asunto_CN to set
		 */
		public void setAsunto_CN(String asunto_CN) {
			this.asunto_CN = asunto_CN;
		}
		/**
		 * @return the asunto_SerialNumber
		 */
		public String getAsunto_SerialNumber() {
			return asunto_SerialNumber;
		}
		/**
		 * @param asunto_SerialNumber the asunto_SerialNumber to set
		 */
		public void setAsunto_SerialNumber(String asunto_SerialNumber) {
			this.asunto_SerialNumber = asunto_SerialNumber;
		}
		/**
		 * @return the emisor
		 */
		public String getEmisor() {
			return emisor;
		}
		/**
		 * @param emisor the emisor to set
		 */
		public void setEmisor(String emisor) {
			this.emisor = emisor;
		}
		/**
		 * @return the firmante
		 */
		public String getFirmante() {
			return firmante;
		}
		/**
		 * @param firmante the firmante to set
		 */
		public void setFirmante(String firmante) {
			this.firmante = firmante;
		}
		/**
		 * @return the correo
		 */
		public String getCorreo() {
			return correo;
		}
		/**
		 * @param correo the correo to set
		 */
		public void setCorreo(String correo) {
			this.correo = correo;
		}
		/**
		 * @return the valido_desde
		 */
		public String getValido_desde() {
			return valido_desde;
		}
		/**
		 * @param valido_desde the valido_desde to set
		 */
		public void setValido_desde(String valido_desde) {
			this.valido_desde = valido_desde;
		}
		/**
		 * @return the valido_hasta
		 */
		public String getValido_hasta() {
			return valido_hasta;
		}
		/**
		 * @param valido_hasta the valido_hasta to set
		 */
		public void setValido_hasta(String valido_hasta) {
			this.valido_hasta = valido_hasta;
		}
		/**
		 * @return the num_serie
		 */
		public String getNum_serie() {
			return num_serie;
		}
		/**
		 * @param num_serie the num_serie to set
		 */
		public void setNum_serie(String num_serie) {
			this.num_serie = num_serie;
		}
		/**
		 * @return the uso_certificado
		 */
		public String getUso_certificado() {
			return uso_certificado;
		}
		/**
		 * @param uso_certificado the uso_certificado to set
		 */
		public void setUso_certificado(String uso_certificado) {
			this.uso_certificado = uso_certificado;
		}
		/**
		 * @return the fecha_consulta
		 */
		public String getFecha_consulta() {
			return fecha_consulta;
		}
		/**
		 * @param fecha_consulta the fecha_consulta to set
		 */
		public void setFecha_consulta(String fecha_consulta) {
			this.fecha_consulta = fecha_consulta;
		}

		/**
		 * @return the politica_cert
		 */
		public String getPolitica_cert() {
			return politica_cert;
		}

		/**
		 * @param politica_cert the politica_cert to set
		 */
		public void setPolitica_cert(String politica_cert) {
			this.politica_cert = politica_cert;
		}
		
}

package es.gob.afirma.core.misc.protocol;

/**
 * Identificadores de las versiones de protocolo en las que se introducen cambios
 * que rompen compatibilidad. Estos identificadores no coindicen con la versi&oacute;n
 * de la aplicaci&oacute;n, sino con la versi&oacute;n del protocolo.
 */
public enum ProtocolVersion {

	/** Versi&oacute;n inicial. */
	VERSION_0 (0),
	/** Versi&oacute;n 1: introduce cambios en el cifrado de los datos para el servidor intermedio. */
	VERSION_1 (1);

	private int version;

	private ProtocolVersion(final int version) {
		this.version = version;
	}

	/**
	 * Devuelve el identificador de version asociado.
	 * @return Identificador de versi&oacute;n.
	 */
	public int getVersion() {
		return this.version;
	}

	/**
	 * Comprueba si la version de protocolo indicada esta soportada.
	 * @param protocolVersion Versi&oacute;n de protocolo que se desea comprobar.
	 * @return {@code true} si la versi&oacute;n de protocolo declarada es mayor o igual a la indicada.
	 */
	public boolean support(final Object protocolVersion) {
		if (protocolVersion == null) {
			return false;
		}
		else if (protocolVersion instanceof Integer) {
			return this.version >= ((Integer) protocolVersion).intValue();
		}
		else if (protocolVersion instanceof ProtocolVersion) {
			return this.version >= ((ProtocolVersion) protocolVersion).getVersion();
		}
		try {
			return this.version >= Integer.parseInt(protocolVersion.toString());
		}
		catch (final Exception e) {
			return false;
		}
	}

	/**
	 * Comprueba si la version de protocolo indicada esta soportada.
	 * @param protocolVersion Versi&oacute;n de protocolo que se desea comprobar.
	 * @return {@code true} si la versi&oacute;n de protocolo declarada es mayor o igual a la indicada.
	 */
	public boolean support(final int protocolVersion) {
		return this.version >= protocolVersion;
	}

	@Override
	public String toString() {
		return Integer.toString(this.version);
	}
}

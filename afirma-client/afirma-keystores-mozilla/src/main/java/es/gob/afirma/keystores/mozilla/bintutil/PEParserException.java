/* Copyright (C) 2011 [Gobierno de Espana]
 * This file is part of "Cliente @Firma".
 * "Cliente @Firma" is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 11/01/11
 * You may contact the copyright holder at: soporte.afirma5@mpt.es
 */

package es.gob.afirma.keystores.mozilla.bintutil;

/** Error en el an&aacute;lisis de un fichero PE de Microsoft.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s */
public final class PEParserException extends Exception {

	private static final long serialVersionUID = -1893004047399996424L;

	PEParserException(final String msg) {
		super(msg);
	}

}

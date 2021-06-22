/* Copyright (C) 2011 [Gobierno de Espana]
 * This file is part of "Cliente @Firma".
 * "Cliente @Firma" is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 11/01/11
 * You may contact the copyright holder at: soporte.afirma5@mpt.es
 */

package es.gob.afirma.signers.multi.cades.asic;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Properties;

import es.gob.afirma.core.AOException;
import es.gob.afirma.core.signers.AOCounterSigner;
import es.gob.afirma.core.signers.CounterSignTarget;
import es.gob.afirma.core.signers.asic.ASiCUtil;
import es.gob.afirma.signers.multi.cades.AOCAdESCounterSigner;

/** Operaciones de contrafirma CAdES ASiC-S.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public final class AOCAdESASiCSCounterSigner implements AOCounterSigner {

	/** {@inheritDoc} */
	@Override
	public byte[] countersign(final byte[] sign,
			                  final String algorithm,
			                  final CounterSignTarget targetType,
			                  final Object[] targets,
			                  final PrivateKey key,
			                  final Certificate[] certChain,
			                  final Properties extraParams) throws AOException,
			                                                       IOException {
		// Extraemos firma y datos del ASiC
		final byte[] packagedData = ASiCUtil.getASiCSData(sign);
		final byte[] packagedSign = ASiCUtil.getASiCSBinarySignature(sign);

		// Creamos la contrafirma
		final byte[] newCounterSign = new AOCAdESCounterSigner().countersign(
			packagedSign,
			algorithm,
			targetType,
			targets,
			key,
			certChain,
			extraParams
		);

		// Devolvemos un nuevo ASiC
		return ASiCUtil.createSContainer(
			newCounterSign,
			packagedData,
			ASiCUtil.getASiCSDataFilename(sign),
			ASiCUtil.ENTRY_NAME_BINARY_SIGNATURE
		);
	}
}

/* Copyright (C) 2011 [Gobierno de Espana]
 * This file is part of "Cliente @Firma".
 * "Cliente @Firma" is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 11/01/11
 * You may contact the copyright holder at: soporte.afirma5@mpt.es
 */

package es.gob.afirma.ui.core.jse.certificateselection;

import java.security.cert.X509Certificate;

import javax.swing.ImageIcon;

final class CertificateIconManager {

	private static final ImageIcon ICON_CNP_NORMAL = new ImageIcon(
		CertificateSelectionPanel.class.getClassLoader().getResource("resources/cnpicon.png") //$NON-NLS-1$
	);
	private static final ImageIcon ICON_CNP_WARNING = new ImageIcon(
		CertificateSelectionPanel.class.getClassLoader().getResource("resources/cnpicon_w.png") //$NON-NLS-1$
	);
	private static final ImageIcon ICON_CNP_ERROR = new ImageIcon(
		CertificateSelectionPanel.class.getClassLoader().getResource("resources/cnpicon_e.png") //$NON-NLS-1$
	);
	private static final ImageIcon ICON_DNIE_NORMAL = new ImageIcon(
		CertificateSelectionPanel.class.getClassLoader().getResource("resources/dnieicon.png") //$NON-NLS-1$
	);
	private static final ImageIcon ICON_DNIE_WARNING = new ImageIcon(
		CertificateSelectionPanel.class.getClassLoader().getResource("resources/dnieicon_w.png") //$NON-NLS-1$
	);
	private static final ImageIcon ICON_DNIE_ERROR = new ImageIcon(
		CertificateSelectionPanel.class.getClassLoader().getResource("resources/dnieicon_e.png") //$NON-NLS-1$
	);
	private static final ImageIcon ICON_OTHER_NORMAL = new ImageIcon(
		CertificateSelectionPanel.class.getClassLoader().getResource("resources/certicon.png") //$NON-NLS-1$
	);
	private static final ImageIcon ICON_OTHER_WARNING = new ImageIcon(
		CertificateSelectionPanel.class.getClassLoader().getResource("resources/certicon_w.png") //$NON-NLS-1$
	);
	private static final ImageIcon ICON_OTHER_ERROR = new ImageIcon(
		CertificateSelectionPanel.class.getClassLoader().getResource("resources/certicon_e.png") //$NON-NLS-1$
	);
	private static final ImageIcon ICON_FNMT_NORMAL = new ImageIcon(
		CertificateSelectionPanel.class.getClassLoader().getResource("resources/fnmticon.png") //$NON-NLS-1$
	);
	private static final ImageIcon ICON_FNMT_WARNING = new ImageIcon(
		CertificateSelectionPanel.class.getClassLoader().getResource("resources/fnmticon_w.png") //$NON-NLS-1$
	);
	private static final ImageIcon ICON_FNMT_ERROR = new ImageIcon(
		CertificateSelectionPanel.class.getClassLoader().getResource("resources/fnmticon_e.png") //$NON-NLS-1$
	);
	static {
		ICON_DNIE_NORMAL.setDescription(CertificateSelectionDialogMessages.getString("CertificateSelectionPanel.4")); //$NON-NLS-1$
		ICON_DNIE_NORMAL.getAccessibleContext().setAccessibleDescription(CertificateSelectionDialogMessages.getString("CertificateSelectionPanel.4")); //$NON-NLS-1$
		ICON_DNIE_WARNING.setDescription(CertificateSelectionDialogMessages.getString("CertificateSelectionPanel.7")); //$NON-NLS-1$
		ICON_DNIE_WARNING.getAccessibleContext().setAccessibleDescription(CertificateSelectionDialogMessages.getString("CertificateSelectionPanel.7")); //$NON-NLS-1$
		ICON_DNIE_ERROR.setDescription(CertificateSelectionDialogMessages.getString("CertificateSelectionPanel.9")); //$NON-NLS-1$
		ICON_DNIE_ERROR.getAccessibleContext().setAccessibleDescription(CertificateSelectionDialogMessages.getString("CertificateSelectionPanel.9")); //$NON-NLS-1$

		ICON_CNP_NORMAL.setDescription(CertificateSelectionDialogMessages.getString("CertificateSelectionPanel.16")); //$NON-NLS-1$
		ICON_CNP_NORMAL.getAccessibleContext().setAccessibleDescription(CertificateSelectionDialogMessages.getString("CertificateSelectionPanel.16")); //$NON-NLS-1$
		ICON_CNP_WARNING.setDescription(CertificateSelectionDialogMessages.getString("CertificateSelectionPanel.17")); //$NON-NLS-1$
		ICON_CNP_WARNING.getAccessibleContext().setAccessibleDescription(CertificateSelectionDialogMessages.getString("CertificateSelectionPanel.17")); //$NON-NLS-1$
		ICON_CNP_ERROR.setDescription(CertificateSelectionDialogMessages.getString("CertificateSelectionPanel.18")); //$NON-NLS-1$
		ICON_CNP_ERROR.getAccessibleContext().setAccessibleDescription(CertificateSelectionDialogMessages.getString("CertificateSelectionPanel.18")); //$NON-NLS-1$

		ICON_OTHER_NORMAL.setDescription(CertificateSelectionDialogMessages.getString("CertificateSelectionPanel.11")); //$NON-NLS-1$
		ICON_OTHER_NORMAL.getAccessibleContext().setAccessibleDescription(CertificateSelectionDialogMessages.getString("CertificateSelectionPanel.11")); //$NON-NLS-1$
		ICON_OTHER_WARNING.setDescription(CertificateSelectionDialogMessages.getString("CertificateSelectionPanel.13")); //$NON-NLS-1$
		ICON_OTHER_WARNING.getAccessibleContext().setAccessibleDescription(CertificateSelectionDialogMessages.getString("CertificateSelectionPanel.13")); //$NON-NLS-1$
		ICON_OTHER_ERROR.setDescription(CertificateSelectionDialogMessages.getString("CertificateSelectionPanel.15")); //$NON-NLS-1$
		ICON_OTHER_ERROR.getAccessibleContext().setAccessibleDescription(CertificateSelectionDialogMessages.getString("CertificateSelectionPanel.15")); //$NON-NLS-1$

		ICON_FNMT_NORMAL.setDescription(CertificateSelectionDialogMessages.getString("CertificateSelectionPanel.19")); //$NON-NLS-1$
		ICON_FNMT_NORMAL.getAccessibleContext().setAccessibleDescription(CertificateSelectionDialogMessages.getString("CertificateSelectionPanel.19")); //$NON-NLS-1$
		ICON_FNMT_WARNING.setDescription(CertificateSelectionDialogMessages.getString("CertificateSelectionPanel.20")); //$NON-NLS-1$
		ICON_FNMT_WARNING.getAccessibleContext().setAccessibleDescription(CertificateSelectionDialogMessages.getString("CertificateSelectionPanel.20")); //$NON-NLS-1$
		ICON_FNMT_ERROR.setDescription(CertificateSelectionDialogMessages.getString("CertificateSelectionPanel.21")); //$NON-NLS-1$
		ICON_FNMT_ERROR.getAccessibleContext().setAccessibleDescription(CertificateSelectionDialogMessages.getString("CertificateSelectionPanel.21")); //$NON-NLS-1$
	}

	/** Indica si un certificado pertenece a un DNIe.
	 * @param certificate Certificado.
	 * @return Devuelve {@code true} si el certificado pertenece a un DNIe, {@code false}
	 * en caso contrario. */
	private static boolean isDNIeCert(final X509Certificate certificate) {
		if (certificate == null) {
			return false;
		}
		final String issuer = certificate.getIssuerX500Principal().toString().toUpperCase();
		return issuer.contains("O=DIRECCION GENERAL DE LA POLICIA") && issuer.contains("OU=DNIE"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/** Indica si un certificado est&aacute; emitido por el Cuerpo Nacional de Polic&iacute;a.
	 * @param certificate Certificado.
	 * @return Devuelve {@code true} si el certificado est&aacute; emitido por el CNP, {@code false}
	 * en caso contrario. */
	private static boolean isCnpCert(final X509Certificate certificate) {
		if (certificate == null) {
			return false;
		}
		final String issuer = certificate.getIssuerX500Principal().toString().toUpperCase();
		return issuer.contains("O=DIRECCION GENERAL DE LA POLICIA") && issuer.contains("OU=CNP"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/** Indica si un certificado est&aacute; emitido por FNMT-RCM.
	 * @param certificate Certificado.
	 * @return Devuelve {@code true} si el certificado est&aacute; emitido por FNMT-RCM, {@code false}
	 * en caso contrario. */
	private static boolean isFnmtCert(final X509Certificate certificate) {
		if (certificate == null) {
			return false;
		}
		final String issuer = certificate.getIssuerX500Principal().toString().toUpperCase();
		return issuer.contains("O=FNMT"); //$NON-NLS-1$
	}

	static ImageIcon getNormalIcon(final X509Certificate certificate) {
		if (isDNIeCert(certificate)) {
			return ICON_DNIE_NORMAL;
		}
		if (isCnpCert(certificate)) {
			return ICON_CNP_NORMAL;
		}
		if (isFnmtCert(certificate)) {
			return ICON_FNMT_NORMAL;
		}
		return ICON_OTHER_NORMAL;
	}

	static ImageIcon getWarningIcon(final X509Certificate certificate) {
		if (isDNIeCert(certificate)) {
			return ICON_DNIE_WARNING;
		}
		if (isCnpCert(certificate)) {
			return ICON_CNP_WARNING;
		}
		if (isFnmtCert(certificate)) {
			return ICON_FNMT_WARNING;
		}
		return ICON_OTHER_WARNING;
	}

	static ImageIcon getErrorIcon(final X509Certificate certificate) {
		if (isDNIeCert(certificate)) {
			return ICON_DNIE_ERROR;
		}
		if (isCnpCert(certificate)) {
			return ICON_CNP_ERROR;
		}
		if (isFnmtCert(certificate)) {
			return ICON_FNMT_ERROR;
		}
		return ICON_OTHER_ERROR;
	}

}

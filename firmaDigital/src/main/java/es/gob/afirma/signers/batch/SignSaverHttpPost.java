package es.gob.afirma.signers.batch;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

import es.gob.afirma.core.misc.Base64;
import es.gob.afirma.core.misc.http.UrlHttpManagerFactory;

/** Guarda firmas envi&aacute;ndolas a un servicio HTTP POST.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public final class SignSaverHttpPost implements SignSaver {

	private static final String PROP_POST_URL = "PostUrl"; //$NON-NLS-1$
	private static final String PROP_POST_PARAM_NAME = "PostParamName"; //$NON-NLS-1$

	private String url;
	private String param;

	/** Constyruye un objeto de guardado de firmas mediante un servicio HTTP POST.
	 * El servicio recibir&aacute; los datos en Base64 dentro del par&aacute;metro indicado.
	 * @param postUrl URL del servicio HTTP POST.
	 * @param postParam Nombre del par&aacute;metro POST en el que se enviar&aacute;n los datos. */
	public SignSaverHttpPost(final String postUrl, final String postParam) {
		if (postUrl == null) {
			throw new IllegalArgumentException(
				"La URL para hacer el POST no puede ser nula" //$NON-NLS-1$
			);
		}
		if (postParam == null) {
			throw new IllegalArgumentException(
				"El nombre del parametro para hacer el POST no puede ser nulo" //$NON-NLS-1$
			);
		}
		this.url = postUrl;
		this.param = postParam;
	}

	/** Constructor vac&iacute;o. */
	public SignSaverHttpPost() {
		// Vacio
	}

	@Override
	public void saveSign(final SingleSign sign, final byte[] dataToSave) throws IOException {
		UrlHttpManagerFactory.getInstalledManager().readUrlByPost(
			this.url + "?" + this.param + "=" + Base64.encode(dataToSave) //$NON-NLS-1$ //$NON-NLS-2$
		);
	}

	@Override
	public void init(final Properties config) {
		if (config == null) {
			throw new IllegalArgumentException(
				"La configuracion no puede ser nula" //$NON-NLS-1$
			);
		}
		final String postUrl = config.getProperty(PROP_POST_URL);
		if (postUrl == null) {
			throw new IllegalArgumentException(
				"Es obligarorio que la configuracion incluya un valor para la propiedad " + PROP_POST_URL //$NON-NLS-1$
			);
		}
		this.url = postUrl;

		final String postParam = config.getProperty(PROP_POST_PARAM_NAME);
		if (postParam == null) {
			throw new IllegalArgumentException(
				"Es obligarorio que la configuracion incluya un valor para la propiedad " + PROP_POST_PARAM_NAME //$NON-NLS-1$
			);
		}
		this.param = postParam;
	}

	@Override
	public Properties getConfig() {
		final Properties p = new Properties();
		p.put(PROP_POST_URL, this.url);
		p.put(PROP_POST_PARAM_NAME, this.param);
		return p;
	}

	@Override
	public void rollback(final SingleSign sign) {
		Logger.getLogger("es.gob.afirma").severe( //$NON-NLS-1$
			"Se ha pedido deshacer la firma " + sign.getId() + ", pero no se puede hacer porque ya se ha finalizado el POST" //$NON-NLS-1$ //$NON-NLS-2$
		);

	}

}

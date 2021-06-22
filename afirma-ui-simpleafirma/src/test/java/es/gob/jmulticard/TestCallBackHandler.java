package es.gob.jmulticard;

import java.security.KeyStore;
import java.security.KeyStore.LoadStoreParameter;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStore.ProtectionParameter;
import java.security.Provider;
import java.security.Security;
import java.security.Signature;
import java.util.Enumeration;

import org.junit.Test;

import es.gob.jmulticard.jse.provider.DnieProvider;
import es.gob.jmulticard.ui.passwordcallback.gui.CommonPasswordCallback;
import es.gob.jmulticard.ui.passwordcallback.gui.DnieCallbackHandler;

/** Pruebas de tarjetas inteligentes con <i>CallbackHandler</i>. */
public class TestCallBackHandler {

	/** Prueba de <i>CallbackHandlerProtection</i>.
	 * @throws Exception En cualquier error. */
	@SuppressWarnings("static-method")
	@Test
	public void testProviderWithCallbackHandlerProtection() throws Exception {
		final Provider p = new DnieProvider();
		Security.addProvider(p);
		final KeyStore ks = KeyStore.getInstance("DNI", p); //$NON-NLS-1$
		ks.load(
			new LoadStoreParameter() {
				@Override
				public ProtectionParameter getProtectionParameter() {
					return new KeyStore.CallbackHandlerProtection(
							new DnieCallbackHandler()
					);
				}
			}
		);

		final Enumeration<String> aliases = ks.aliases();
		while (aliases.hasMoreElements()) {
			System.out.println(aliases.nextElement());
		}
		final Signature s = Signature.getInstance("SHA1withRSA"); //$NON-NLS-1$
		s.initSign(((PrivateKeyEntry)ks.getEntry("CertFirmaDigital", null)).getPrivateKey()); //$NON-NLS-1$
		s.update("Hola".getBytes()); //$NON-NLS-1$
		s.sign();
	}

	/** Prueba de <i>PasswordProtection</i>.
	 * @throws Exception En cualquier error. */
	@Test
	@SuppressWarnings("static-method")
	public void testProviderWithPasswordProtection() throws Exception {
		final Provider p = new DnieProvider();
		Security.addProvider(p);
		final KeyStore ks = KeyStore.getInstance("DNI", p); //$NON-NLS-1$
		ks.load(
			new LoadStoreParameter() {
				@Override
				public ProtectionParameter getProtectionParameter() {
					return new KeyStore.PasswordProtection(
						new CommonPasswordCallback("Introduzca el PIN de su DNIe", "PIN", true).getPassword() //$NON-NLS-1$ //$NON-NLS-2$
					);
				}
			}
		);
		final Enumeration<String> aliases = ks.aliases();
		while (aliases.hasMoreElements()) {
			System.out.println(aliases.nextElement());
		}
		final Signature s = Signature.getInstance("SHA1withRSA"); //$NON-NLS-1$
		s.initSign(((PrivateKeyEntry)ks.getEntry("CertFirmaDigital", null)).getPrivateKey()); //$NON-NLS-1$
		s.update("Hola".getBytes()); //$NON-NLS-1$
		s.sign();
	}

	/** Prueba de <i>KeyStoreBuilder</i> con <i>CallbackHandlerProtection</i>.
	 * @throws Exception En cualquier error. */
	@Test
	@SuppressWarnings("static-method")
	public void testProviderWithKeyStoreBuilderWithCallbackHandlerProtection() throws Exception {
		final KeyStore.Builder kb = KeyStore.Builder.newInstance(
				"DNI", //$NON-NLS-1$
				new DnieProvider(),
				new KeyStore.CallbackHandlerProtection(
						new DnieCallbackHandler()
				)
			);

		final Enumeration<String> aliases = kb.getKeyStore().aliases();
		while (aliases.hasMoreElements()) {
			System.out.println(aliases.nextElement());
		}
		final Signature s = Signature.getInstance("SHA1withRSA"); //$NON-NLS-1$
		s.initSign(((PrivateKeyEntry) kb.getKeyStore().getEntry("CertFirmaDigital", null)).getPrivateKey()); //$NON-NLS-1$
		s.update("Hola".getBytes()); //$NON-NLS-1$
		s.sign();
	}

	/** Prueba de <i>KeyStoreBuilder</i> con <i>PasswordProtection</i>.
	 * @throws Exception En cualquier error. */
	@Test
	@SuppressWarnings("static-method")
	public void testProviderWithKeyStoreBuilderWithPasswordProtection() throws Exception {
		final KeyStore.Builder kb = KeyStore.Builder.newInstance(
				"DNI", //$NON-NLS-1$
				new DnieProvider(),
				new KeyStore.PasswordProtection(
						new CommonPasswordCallback("Introduzca el PIN de su DNIe", "PIN", true).getPassword() //$NON-NLS-1$ //$NON-NLS-2$
					)
			);
		final Enumeration<String> aliases = kb.getKeyStore().aliases();
		while (aliases.hasMoreElements()) {
			System.out.println(aliases.nextElement());
		}
		final Signature s = Signature.getInstance("SHA1withRSA"); //$NON-NLS-1$
		s.initSign(((PrivateKeyEntry) kb.getKeyStore().getEntry("CertFirmaDigital", null)).getPrivateKey()); //$NON-NLS-1$
		s.update("Hola".getBytes()); //$NON-NLS-1$
		s.sign();
	}
}

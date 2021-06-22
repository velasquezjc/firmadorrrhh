package test.es.gob.jmulticard.ui.passwordcallback;

import javax.swing.JFrame;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import es.gob.jmulticard.ui.passwordcallback.PasswordCallbackManager;

/** Prueba del establecimiento del componente padre. */
public class TestStaticDialogOwnerSeting {

    /** Prueba del establecimiento del componente padre de un di&aacute;logo. */
    @SuppressWarnings("static-method")
    @Test
    @Ignore // Necesita GUI
    public void testSetGetDialogOwner() {
        final JFrame frame = new JFrame();
        PasswordCallbackManager.setDialogOwner(frame);
        Assert.assertEquals(frame, PasswordCallbackManager.getDialogOwner());
    }
}

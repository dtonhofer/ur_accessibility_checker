package name.heavycarbon.url_access_checker.sidejobs;

import name.heavycarbon.url_access_checker.printing.MyPrinting;
import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.Iterator;

// https://docs.oracle.com/en/java/javase/21/docs/api//java.base/java/security/KeyStore.html

public class LoadJavaTrustStore {

    private final static Path cacertsPath = Path.of(System.getProperty("java.home"), "lib", "security", "cacerts");
    private final static String password = "changeit";

    public static @NotNull LoadTrustStoreResult loadJavaTrustStore() {
        StringBuilder buf = new StringBuilder();
        buf.append("The JDK/JRE's 'cacerts' PKCS#12 file containing 'trusted certificates' should be here: ");
        buf.append(cacertsPath);
        try (FileInputStream is = new FileInputStream(cacertsPath.toFile())) {
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            // It turns out that the PKCS#12 file is not password protected by default.
            // So any password is ok.
            keystore.load(is, password.toCharArray());
            buf.append("\n");
            buf.append("File opened. Its size is ");
            buf.append(keystore.size());
            buf.append(" entries.");
            return new LoadTrustStoreResult(true, keystore, buf.toString());
        } catch (Exception exe) {
            // a lot can go wrong
            appendException(buf, "Problem handling the 'cacerts' file!", exe);
            return new LoadTrustStoreResult(false, null, buf.toString());
        }
    }

    private static void appendException(StringBuilder buf, String msg, Exception exe) {
        buf.append("\n");
        buf.append(msg);
        buf.append("\n");
        buf.append(exe.getClass().getName());
        if (exe.getMessage() != null) {
            buf.append(": '").append(exe.getMessage()).append("'");
        }
    }

    private static void appendKeyStoreAliases(StringBuilder buf, KeyStore ks) {
        try {
            for (Iterator<String> it = ks.aliases().asIterator(); it.hasNext(); ) {
                buf.append("\n");
                buf.append(it.next());
            }
        } catch (KeyStoreException exe) {
            appendException(buf, "Problem reading data from the loaded KeyStore!", exe);
        }
    }

    public static void main(String[] args) {
        final LoadTrustStoreResult mp = loadJavaTrustStore();
        final KeyStore trustStore = mp.trustStore(); // null if a problem occurred
        final StringBuilder buf = new StringBuilder(mp.msg()); // carries messages
        final int exitCode;
        if (mp.ok()) {
            assert trustStore != null;
            appendKeyStoreAliases(buf, trustStore);
            exitCode = 0;
        } else {
            exitCode = 1;
        }
        MyPrinting.log(buf);
        System.exit(exitCode);
    }

}

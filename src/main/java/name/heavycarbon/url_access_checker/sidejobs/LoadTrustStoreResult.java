package name.heavycarbon.url_access_checker.sidejobs;

import org.jetbrains.annotations.Nullable;

import java.security.KeyStore;

public record LoadTrustStoreResult(boolean ok, @Nullable KeyStore trustStore, String msg) {
}

package name.heavycarbon.website_testing.sidejobs;

import org.jetbrains.annotations.Nullable;

import java.security.KeyStore;

public record LoadTrustStoreResult(boolean ok, @Nullable KeyStore trustStore, String msg) {
}

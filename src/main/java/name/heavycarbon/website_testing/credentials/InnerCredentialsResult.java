package name.heavycarbon.website_testing.credentials;

import org.jetbrains.annotations.Nullable;

// ---
// This class just needs package visibility as the record is only
// used in "CredentialsFromFile"
// ---

record InnerCredentialsResult(@Nullable String username, @Nullable String password, @Nullable String errMsg) {
}

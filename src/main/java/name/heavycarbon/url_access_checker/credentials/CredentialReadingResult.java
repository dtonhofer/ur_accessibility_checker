package name.heavycarbon.url_access_checker.credentials;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// ---
// The data returned from program argument analysis, in a record.
//
// 1) A boolean indicating whether the credential reading succeeded or not.
// 2) Collected Credentials, or null if none could be found.
// 3) A string that is either "OK" or an explanation of what went wrong.
// ---

public record CredentialReadingResult(boolean ok, @Nullable Credentials credentials, @NotNull String msg) {
}

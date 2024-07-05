package name.heavycarbon.url_access_checker.credentials;

import org.jetbrains.annotations.NotNull;

// ---
// Credentials for logging into a website.
// ---

public record Credentials(@NotNull String user, @NotNull String pass) {

    @NotNull
    public String toString() {
        return "[" + user + " : " + "..." + "]";
    }
}

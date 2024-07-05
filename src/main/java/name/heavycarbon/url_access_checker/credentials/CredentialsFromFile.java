package name.heavycarbon.url_access_checker.credentials;

import name.heavycarbon.url_access_checker.printing.MyPrinting;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class CredentialsFromFile {

    private static @NotNull InnerCredentialsResult obtainCredentialsFromFileInner(@NotNull File credentialsFile) {
        final Pattern passwordPattern = Pattern.compile("\\s*pass(word)?\\s*=\\s*(.+?)\\s*");
        final Pattern usernamePattern = Pattern.compile("\\s*user(name)?\\s*=\\s*(.+?)\\s*");
        String username = null;
        String password = null;
        try {
            try (FileInputStream fis = new FileInputStream(credentialsFile)) {
                final LineNumberReader reader = new LineNumberReader(new InputStreamReader(fis, StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        int matches = 0;
                        if (password == null) {
                            final Matcher m = passwordPattern.matcher(line);
                            if (m.matches()) {
                                password = m.group(2);
                                matches++;
                            }
                        }
                        if (username == null) {
                            final Matcher m = usernamePattern.matcher(line);
                            if (m.matches()) {
                                username = m.group(2);
                                matches++;
                            }
                        }
                        if (matches == 0) {
                            final var extract = line.substring(0, Math.min(line.length(), 4));
                            MyPrinting.logWarning("Disregarded a line in '" + credentialsFile + "' starting with '" + extract + "...'");
                        } else {
                            assert matches == 1;
                        }
                    }
                }
            }
            return new InnerCredentialsResult(username, password, null);
        } catch (IOException exe) {
            final StringBuilder buf2 = new StringBuilder();
            buf2.append("Reading credentials from file '");
            buf2.append(credentialsFile);
            buf2.append("' resulted in ");
            buf2.append(exe.getClass().getName());
            if (exe.getMessage() != null) {
                buf2.append(": '").append(exe.getMessage()).append("'");
            }
            return new InnerCredentialsResult(null, null, buf2.toString());
        }
    }

    public static @NotNull CredentialReadingResult obtainCredentialsFromFile(@NotNull File credentialsFile) {
        final InnerCredentialsResult mid = obtainCredentialsFromFileInner(credentialsFile);
        final StringBuilder buf = new StringBuilder();
        boolean ok = false;
        {
            boolean addSep = false;
            if (mid.username() == null) {
                buf.append("No 'username = ...' line found!");
                addSep = true;
            }
            if (mid.password() == null) {
                if (addSep) {
                    buf.append(" ");
                }
                buf.append("No 'password = ...' line found!");
            }
            ok = (mid.username() != null && mid.password() != null);
        }
        if (ok) {
            return new CredentialReadingResult(true, new Credentials(mid.username(), mid.password()), "OK");
        } else {
            // use the error message from "mid" otherwise the newly constructed obe
            String msg = mid.errMsg() != null ? mid.errMsg() : buf.toString();
            return new CredentialReadingResult(false, null, msg);
        }
    }
}

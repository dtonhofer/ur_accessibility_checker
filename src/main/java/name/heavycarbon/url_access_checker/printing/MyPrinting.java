package name.heavycarbon.url_access_checker.printing;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// ---
// Some simple functionality for writing to STDOUT to tell the user whats going on.
// This is different from actual logging.
// There are no loglevels or loggers or anything.
//
// We have the general principle that anything printed out (possibly a message
// over multiple lines) is NOT followed by a final newline.
//
// This means that the next printout must begin by issuing a newline to start
// the next line, otherwise the text to print out will just be appended to
// whatever was printed earlier.
// ---

public class MyPrinting {

    private final static int defaultIndentCount = 3;

    // ---
    // Just getting strings of blanks.
    // In a serious application, one would cache the strings.
    // ---

    public static String getStringOfBlanks(int count) {
        return " ".repeat(Math.max(count, 0));
    }

    // ---
    // Print a newline to System.out if "add"
    // ---

    public static void newline(boolean add) {
        if (add) {
            System.out.println();
        }
    }

    public static void newline() {
        newline(true);
    }

    // ---
    // Print the "msg" to System.out
    // ---

    public static void log(@NotNull StringBuilder msg) {
        System.out.print(msg);
    }

    // ---
    // Print the "msg" to System.out
    // ---

    public static void log(@NotNull String msg) {
        System.out.print(msg);
    }

    // ---
    // Print the "msg" to System.out, prefixing it with "ERROR: "
    // ---

    public static void logError(@NotNull String msg) {
        System.out.print("ERROR: " + msg);
    }

    // ---
    // Print the "msg" to System.out, prefixing it with "WARNING: "
    // ---

    public static void logWarning(@NotNull String msg) {
        System.out.print("WARNING: " + msg);
    }

    // ---
    // Print the "msg" to System.out, prefixing it with "WARNING: "
    // then add the class and message (if any) of the Exception.
    // ---

    public static void logException(@NotNull String msg, @NotNull Exception e) {
        System.out.println("WARNING: " + msg);
        if (e.getMessage() != null) {
            System.out.println(e.getMessage());
        }
        System.out.print(e.getClass().getName());
    }

    // ---
    // Indent a possibly multiline string by "indentCount"
    // ---

    public static String indent(@NotNull String str, int indentCount) {
        String indentStr = getStringOfBlanks(indentCount);
        return indentStr + str.replace("\n", "\n" + indentStr);
    }

    // ---
    // Indent a possibly multiline string by the default indent
    // ---

    public static String indent(@NotNull String str) {
        return indent(str, defaultIndentCount);
    }

    // ---
    // This method is used to join "str" to the string in "buf" using the
    // given separator (generally, the newline). If "str" is empty, nothing happens.
    // If "buf" contains no string or already ends in the given separator, no
    // new separator is added.
    // ---

    public static void joinIfNotEmpty(@NotNull StringBuilder buf, @NotNull String str) {
        joinIfNotEmpty(buf, str, "\n");
    }

    public static void joinIfNotEmpty(@NotNull StringBuilder buf, @NotNull String str, @NotNull String separator) {
        if (!str.isEmpty()) {
            if (!buf.isEmpty()) {
                if (!separator.isEmpty()) {
                    final int bufLength = buf.length();
                    if (!buf.substring(bufLength - separator.length(), bufLength).equals(separator)) {
                        buf.append(separator);
                    } else {
                        System.err.println("Not adding separator to '" + buf + "' as it already ends in it");
                    }
                }
            }
            buf.append(str);
        }
    }

    // ---
    // This is just "StringBuilder.toString()" but we additionally check that the
    // buffer content does NOT end in a newline, and if it does, we warn to STDERR
    // ---

    public static @NotNull String makeString(@NotNull StringBuilder buf) {
        return makeString(buf, "\n");
    }

    public static @NotNull String makeString(@NotNull StringBuilder buf, @NotNull String separator) {
        final var res = buf.toString();
        final int bufLength = buf.length();
        if (!(res.isEmpty() || !buf.substring(bufLength - separator.length(), bufLength).equals(separator))) {
            System.err.println("Warning: StringBuilder has a 'separator' (newline) as final character");
        }
        return res;
    }

    // ---
    // Cleaning a string means: (null) and a string made of blanks are reduced to (null),
    // Any other string is trimmed.
    // ---

    public static @Nullable String cleanString(@Nullable String x) {
        if (x == null) {
            return null;
        } else {
            final var t = x.trim();
            return t.isEmpty() ? null : t;
        }
    }
}

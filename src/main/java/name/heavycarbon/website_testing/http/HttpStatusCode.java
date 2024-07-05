package name.heavycarbon.website_testing.http;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

// ---
// From https://en.wikipedia.org/wiki/List_of_HTTP_status_codes
// See also https://stackoverflow.com/questions/730283/does-java-have-a-complete-enum-for-http-response-codes
//
// You can write code like this:
//
/*
final var x = HttpStatusCode.fromCode(203);
        System.out.println(x); // "203"
        System.out.println(x.toStringWithText()); // "203 Non Authoritative Information"

final var y = HttpStatusCode.fromText("Non Authoritative Information").orElseThrow();
        System.out.println(y); // "203"
        System.out.println(y.toStringWithText()); // "203 Non Authoritative Information"

final var z = HttpStatusCode.fromText("Internal Server Error").orElseThrow();
        System.out.println(z); // "500"
        System.out.println(z.toStringWithText()); // "500 Internal Server Error"

        assert x.equals(HttpStatusCode.fromText("Non Authoritative Information").orElseThrow());
        assert x == HttpStatusCode.fromText("Non Authoritative Information").orElseThrow();
        assert x.equals(y);
        assert x == y;
        assert !x.equals(z);
        assert x != z;
 */
// ---

public final class HttpStatusCode {

    private static Map<Integer, String> codeToTextMap;
    private static Map<Integer, String> codeToTextMapUnofficials;
    private static Map<String, Integer> textToCodeMap;

    // This map grows as we request new instances via valueOf().
    // Access to it is synchronized in valueOf().

    private final static Map<Integer, HttpStatusCode> canonicals = new HashMap<>();

    static {
        Map<Integer, String> map = new HashMap<>();
        map.put(100, "Continue");
        map.put(101, "Switching Protocols");
        map.put(102, "Processing");
        map.put(103, "Early Hints");
        map.put(200, "OK");
        map.put(201, "Created");
        map.put(202, "Accepted");
        map.put(203, "Non Authoritative Information");
        map.put(204, "No Content");
        map.put(205, "Reset Content");
        map.put(206, "Partial Content");
        map.put(207, "Multi-Status");
        map.put(208, "Already Reported");
        map.put(226, "IM Used");
        map.put(300, "Multiple Choices");
        map.put(301, "Moved Permanently");
        map.put(302, "Found");
        map.put(303, "See Other");
        map.put(304, "Not Modified");
        map.put(305, "Use Proxy");
        map.put(307, "Temporary Redirect");
        map.put(308, "Permanent Redirect");
        map.put(400, "Bad Request");
        map.put(401, "Unauthorized");
        map.put(402, "Payment Required");
        map.put(403, "Forbidden");
        map.put(404, "Not Found");
        map.put(405, "Method Not Allowed");
        map.put(406, "Not Acceptable");
        map.put(407, "Proxy Authentication Required");
        map.put(408, "Request Timeout");
        map.put(409, "Conflict");
        map.put(410, "Gone");
        map.put(411, "Length Required");
        map.put(412, "Precondition Failed");
        map.put(413, "Payload Too Large");
        map.put(414, "URI Too Long");
        map.put(415, "Unsupported Media Type");
        map.put(416, "Range Not Satisfiable");
        map.put(417, "Expectation Failed");
        map.put(418, "I'm a teapot");
        map.put(421, "Misdirected Request");
        map.put(422, "Unprocessable Content");
        map.put(423, "Locked");
        map.put(424, "Failed Dependency");
        map.put(426, "Upgrade Required");
        map.put(428, "Precondition Required");
        map.put(429, "Too Many Requests");
        map.put(431, "Request Header Fields Too Large");
        map.put(451, "Unavailable For Legal Reasons");
        map.put(500, "Internal Server Error");
        map.put(501, "Not Implemented");
        map.put(502, "Bad Gateway");
        map.put(503, "Service Unavailable");
        map.put(504, "Gateway Timeout");
        map.put(505, "Http Version Not Supported");
        map.put(506, "Variant Also Negotiates");
        map.put(507, "Insufficient Storage");
        map.put(508, "Loop Detected");
        map.put(510, "Not Extended");
        map.put(511, "Network Authentication Required");
        HttpStatusCode.codeToTextMap = Collections.unmodifiableMap(map);
    }

    static {
        Map<Integer, String> map = new HashMap<>();
        map.put(218, "This is fine (Apache HTTP Server)");
        map.put(419, "Page Expired (Laravel Framework)");
        map.put(420, "Method Failure (Spring Framework), Enhance Your Calm (Twitter)");
        map.put(430, "Shopify Security Rejection (Shopify)");
        map.put(450, "Blocked by Windows Parental Controls (Microsoft)");
        map.put(498, "Invalid Token (Esri)");
        map.put(499, "Token Required (Esri)");
        map.put(509, "Bandwidth Limit Exceeded (Apache Web Server/cPanel)");
        map.put(529, "Site is overloaded (Qualys)");
        map.put(530, "Site is frozen (Pantheon), Origin DNS Error (Shopify)");
        map.put(540, "Temporarily Disabled (Shopify)");
        map.put(598, "Network read timeout error (Informal convention)");
        map.put(599, "Network Connect Timeout Error (Inofficial)");
        map.put(783, "Unexpected Token (Shopify)");
        HttpStatusCode.codeToTextMapUnofficials = Collections.unmodifiableMap(map);
    }

    static {
        // Note that we don't put the "unofficials" into the reverse map
        Map<String, Integer> reverseMap = new HashMap<>();
        HttpStatusCode.codeToTextMap.forEach((key, value) -> {
            var old = reverseMap.put(value.toLowerCase().replace(" ", ""), key);
            assert old == null;
        });
        HttpStatusCode.textToCodeMap = Collections.unmodifiableMap(reverseMap);
    }

    // ---
    // Some often used codes that you will use directly
    // ---

    public final static HttpStatusCode ok = fromCode(200);
    public final static HttpStatusCode unauthorized = fromCode(401);
    public final static HttpStatusCode forbidden = fromCode(403);
    public final static HttpStatusCode missing = fromCode(404);
    public final static HttpStatusCode moved = fromCode(301);
    public final static HttpStatusCode internal_server_error = fromCode(500);

    private final int code;
    private final int hashCode;
    private final String asString;
    private final String asStringWithText;

    // ---
    // Constructor is private because customers are expected to only call fromCode() and fromText().
    // ---

    private HttpStatusCode(int code) {
        if (code < 100 || code > 999) {
            throw new IllegalArgumentException("Code " + code + " is out of range [100,999]");
        }
        this.code = code;
        this.hashCode = Objects.hashCode(code);
        this.asString = String.valueOf(code);
        if (codeToTextMap.containsKey(code)) {
            this.asStringWithText = code + " " + codeToTextMap.get(code);
        } else if (codeToTextMapUnofficials.containsKey(code)) {
            this.asStringWithText = code + " " + codeToTextMapUnofficials.get(code);
        } else {
            this.asStringWithText = asString;
        }
    }

    // ---
    // Is there a text description for this instance?
    // ---

    public boolean existsText() {
        return codeToTextMap.containsKey(code) || codeToTextMapUnofficials.containsKey(code);
    }

    // ---
    // No surprises hash code based on the HTTP status code
    // ---

    @Override
    public int hashCode() {
        return hashCode;
    }

    // ---
    // toString() just returns the string-ified numeric code
    // ---

    @Override
    public @NotNull String toString() {
        return asString;
    }

    // ---
    // Equality is basically on code
    // ---

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            assert this.code == ((HttpStatusCode) obj).code;
            return true;
        } else {
            assert !(obj instanceof HttpStatusCode) || this.code != ((HttpStatusCode) obj).code;
            return false;
        }
    }

    // ---
    // Returns the string-ified numeric code and any existing text description that is associated to it
    // ---

    public @NotNull String toStringWithText() {
        return asStringWithText;
    }

    // ---
    // Try to find an official code (the unofficial code are disregarded)
    // that corresponds to the passed "desc" (the description in english)
    // Casing is disregarded, as are blanks.
    // ---

    public static Optional<HttpStatusCode> fromText(@Nullable String desc) {
        if (desc == null) {
            return Optional.empty();
        } else {
            final String lookFor = desc.toLowerCase().replace(" ", "");
            final Integer res = textToCodeMap.get(lookFor);
            if (res == null) {
                return Optional.empty();
            } else {
                return Optional.of(fromCode(res)); // a new instance may be created
            }
        }
    }

    // ---
    // Obtain a canonical instance of HttpStatusCode.
    // If none exists for that code, it is created.
    // ---

    public static @NotNull HttpStatusCode fromCode(int code) {
        synchronized (canonicals) {
            final HttpStatusCode res = canonicals.get(code);
            if (res != null) {
                return res;
            } else {
                final HttpStatusCode res2 = new HttpStatusCode(code);
                canonicals.put(code, res2);
                return res2;
            }
        }
    }
}

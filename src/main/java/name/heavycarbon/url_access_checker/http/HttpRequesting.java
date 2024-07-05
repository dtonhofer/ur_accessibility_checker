package name.heavycarbon.url_access_checker.http;

import name.heavycarbon.url_access_checker.building.TestConfig;
import name.heavycarbon.url_access_checker.credentials.Credentials;
import name.heavycarbon.url_access_checker.printing.MyPrinting;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Set;

// ---
// Functionality around HttpClient
// ---

public class HttpRequesting {

    // by default the string is "Java-http-client/21.0.3"
    // See https://stackoverflow.com/questions/2591083/getting-java-version-at-runtime
    private final static String userAgentString = HttpRequesting.class.getName() + "+" + HttpClient.class.getName() + "+" + System.getProperty("java.version");

    private static int getCommonPrefixLength(@NotNull Set<String> keys) {
        int length = 0;
        for (String key : keys) {
            length = Math.max(length, key.length());
        }
        return length;
    }

    private static String fillToPrefixLength(@NotNull String key, int prefixLength) {
        String blanks = " ".repeat(prefixLength - key.length());
        return key + blanks;
    }

    private static @NotNull String buildTextOfEntry(@NotNull String key, @NotNull List<String> values, int prefixLength) {
        assert !values.isEmpty(); // there should at least be an empty string, right??
        final var buf = new StringBuilder();
        final var prefixIndent = MyPrinting.getStringOfBlanks(prefixLength); // how to indent value
        MyPrinting.joinIfNotEmpty(buf, fillToPrefixLength(key, prefixLength) + ": " + values.getFirst());
        boolean first = true;
        for (var value : values) {
            if (!first) {
                MyPrinting.joinIfNotEmpty(buf, prefixIndent + "  " + value);
            }
            first = false;
        }
        return MyPrinting.makeString(buf);
    }

    private static @NotNull String boxify(String text) {
        final var buf = new StringBuilder();
        try (LineNumberReader lnr = new LineNumberReader(new StringReader(text))) {
            String line;
            while ((line = lnr.readLine()) != null) {
                MyPrinting.joinIfNotEmpty(buf, "| " + line);
            }
        } catch (IOException e) {
            String suffix = "";
            if (e.getMessage() != null) {
                suffix = ": " + e.getMessage();
            }
            MyPrinting.logError("IOException" + suffix);
        }
        return MyPrinting.makeString(buf);
    }

    private static @NotNull String toStringHttpVersion(HttpClient.Version version) {
        return switch (version) {
            case HTTP_2 -> "HTTP/2";
            case HTTP_1_1 -> "HTTP/1.1";
            default -> version.toString(); // future proof, maybe (not really, right?)
        };
    }

    public static @NotNull String buildTextOfResponse(@NotNull HttpResponse<String> httpResponse, boolean addBody) {
        final var buf = new StringBuilder();
        MyPrinting.joinIfNotEmpty(buf, "HTTP version: " + toStringHttpVersion(httpResponse.version())); // this is ugly
        MyPrinting.joinIfNotEmpty(buf, "Status Code : " + httpResponse.statusCode());
        final var prefixLength = getCommonPrefixLength(httpResponse.headers().map().keySet());
        for (var entry : httpResponse.headers().map().entrySet()) {
            final var str1 = buildTextOfEntry(entry.getKey(), entry.getValue(), prefixLength);
            final var str2 = MyPrinting.indent(str1);
            MyPrinting.joinIfNotEmpty(buf, str2);
        }
        if (addBody) {
            MyPrinting.joinIfNotEmpty(buf, boxify(httpResponse.body()));
        }
        return MyPrinting.makeString(buf);
    }

    // https://docs.oracle.com/en/java/javase/21/docs/api/java.net.http/java/net/http/HttpRequest.html

    private static @NotNull HttpResultInner performHttpRequestInner(@NotNull HttpClient httpClient, @NotNull URI uri) {
        try {
            final var httpRequest = HttpRequest
                    .newBuilder(uri)
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .setHeader("User-Agent", userAgentString)
                    .build();
            // -----> Going out to the Network ---->
            final var httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            // <-----
            return new HttpResultInnerSuccess(
                    HttpStatusCode.fromCode(httpResponse.statusCode()),
                    httpResponse);
        } catch (IOException e) {
            // If we couldn't authenticate, HTTPClient *also* throws IOException, and not even a subclass thereof.
            // Sounds like bad design, how do we weasel out of it?
            // We try to fake it, but we have no proper HttpResponse to report.
            if (e.getMessage() != null && e.getMessage().contains("too many authentication attempts")) {
                return new HttpResultInnerSuccess(
                        HttpStatusCode.forbidden, // what the server presumably sent, but java.net.HttpClient hid!
                        null); // HTTP response is missing
            } else {
                return new HttpResultInnerFailure(
                        HttpResultInnerFailure.FailureType.io_exception,
                        e.getClass().getName(),
                        e.getMessage() == null ? "(no details)" : e.getMessage());
            }
        } catch (InterruptedException e) {
            // Having been interrupted, set interrupt flag again, and then get out
            Thread.currentThread().interrupt();
            return new HttpResultInnerFailure(
                    HttpResultInnerFailure.FailureType.interrupt,
                    e.getClass().getName(),
                    "The thread got interrupted");
        }
    }

    // https://www.baeldung.com/java-httpclient-basic-auth
    // https://stackoverflow.com/questions/75150081/ioexception-too-many-authentication-attempts-limit-3-when-using-jdk-httpcli

    private static Authenticator getAuthenticator(@NotNull Credentials creds) {
        return new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                // System.out.println("Authentication was requested " + creds.user() + " " + creds.pass());
                return new PasswordAuthentication(creds.user(), creds.pass().toCharArray());
            }
        };
    }

    // https://docs.oracle.com/en/java/javase/21/docs/api/java.net.http/java/net/http/HttpClient.html
    // https://docs.oracle.com/en/java/javase/21/docs/api/java.net.http/java/net/http/HttpClient.Builder.html
    // https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/net/Authenticator.html

    private static @NotNull HttpResultInner buildHttpClientAndPerformRequest(@NotNull URI uri, @NotNull TestConfig testConfig) {
        HttpClient.Builder builder = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NEVER)
                .connectTimeout(Duration.ofSeconds(3));
        if (testConfig.getCredData().creds() != null) {
            builder = builder.authenticator(getAuthenticator(testConfig.getCredData().creds()));
        }
        // HttpClient implements AutoCloseable.
        // Use try-with-resources to have HttpClient.close() called automatically.
        try (HttpClient httpClient = builder.build()) {
            MyPrinting.newline(true);
            MyPrinting.log("Accessing URI '" + uri + "' with ");
            if (testConfig.getCredData().what() == TestConfig.WhatCreds.none) {
                MyPrinting.log("no credentials");
            } else {
                MyPrinting.log(testConfig.getCredData().what() + " credentials: " + testConfig.getCredData().creds());
            }
            // ------>
            return performHttpRequestInner(httpClient, uri);
            // <------
        }
    }

    public static @NotNull HttpResultOuter performHttpRequestAndCheckResponse(@NotNull URI uri, @NotNull TestConfig testConfig) {
        final HttpResultInner inner = buildHttpClientAndPerformRequest(uri, testConfig);
        return inner.checkHttpResponse(uri, testConfig);
    }

}

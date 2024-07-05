package name.heavycarbon.url_access_checker.http;

import name.heavycarbon.url_access_checker.building.TestConfig;
import name.heavycarbon.url_access_checker.printing.MyPrinting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpResponse;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class HttpResultInnerSuccess extends HttpResultInner {

    @NotNull
    private final HttpStatusCode httpStatusCode;
    @Nullable
    private final HttpResponse<String> httpResponse; // null if java.net.HttpClient didn't provide anything

    public HttpResultInnerSuccess(@NotNull HttpStatusCode httpStatusCode, @Nullable HttpResponse<String> httpResponse) {
        this.httpStatusCode = httpStatusCode;
        this.httpResponse = httpResponse;
    }

    public @NotNull HttpStatusCode getHttpStatusCode() {
        return httpStatusCode;
    }

    public @Nullable HttpResponse<String> getHttpResponse() {
        return httpResponse;
    }

    public @NotNull String stringify(@NotNull AlsoAdd alsoAdd) {
        final StringBuilder buf = new StringBuilder();
        MyPrinting.joinIfNotEmpty(buf, this.getClass().getName()); // TODO this is ugly printout
        MyPrinting.joinIfNotEmpty(buf, "HTTP status code   : '" + httpStatusCode.toStringWithText() + "'");
        if (httpResponse != null && alsoAdd != AlsoAdd.nothing) {
            MyPrinting.joinIfNotEmpty(buf, "HTTP Response");
            final var addBody = (alsoAdd == AlsoAdd.httpResponseAndBody);
            final var str1 = HttpRequesting.buildTextOfResponse(httpResponse, addBody);
            final var str2 = MyPrinting.indent(str1);
            MyPrinting.joinIfNotEmpty(buf, str2);
        }
        return MyPrinting.makeString(buf);
    }

    private record CheckBodyResponse(boolean ok, @NotNull List<String> failed) {
    }

    private static CheckBodyResponse checkBody(@NotNull String body, @NotNull List<String> bodyStrings) {
        List<String> failed = new LinkedList<>();
        for (var bodyString : bodyStrings) {
            if (!body.contains(bodyString)) {
                failed.add(bodyString);
            }
        }
        return new CheckBodyResponse(failed.isEmpty(), failed);
    }

    // If the exchange went well (any successful outcome including "moved" etc.):
    // HttpResultInnerSuccess(ok, non-null HttpStatusCode, non-null java.net.HttpResponse, "OK");

    public @NotNull HttpResultOuter checkHttpResponse(@NotNull URI uri, @NotNull TestConfig testConfig) {
        if (testConfig.isExpected(httpStatusCode)) {
            final boolean bodyShouldBeChecked = testConfig.getOptions().isBodyStringsSet();
            if (bodyShouldBeChecked) {
                if (httpStatusCode == HttpStatusCode.forbidden) {
                    // There is no body, there isn't even a http response, if the status code is "forbidden", so we can't check anything.
                    assert httpResponse == null;
                    MyPrinting.logWarning("HTTP status code for '" + uri + "' is " + httpStatusCode + ", but 'options' carries body strings to check -- disregarding those");
                    return new HttpResultOuter(HttpResultOuter.TestOutcome.match, "HTTP status code matches", this);
                } else {
                    assert httpResponse != null;
                    final String body = httpResponse.body();
                    assert body != null;
                    final List<String> bodyStrings = testConfig.getOptions().getBodyStrings();
                    assert bodyStrings != null;
                    final CheckBodyResponse bodyRes = checkBody(body, bodyStrings);
                    if (bodyRes.ok()) {
                        final int count = testConfig.getOptions().getBodyStrings().size();
                        return new HttpResultOuter(HttpResultOuter.TestOutcome.match, "HTTP status code matches. Also body matches (looked for " + count + " substrings)", this);
                    } else {
                        final var offendStr = bodyRes.failed().stream().map(str -> "'" + str + "'").collect(Collectors.joining(","));
                        return new HttpResultOuter(HttpResultOuter.TestOutcome.body_mismatch, "HTTP status code matches, but body does not match. Offending strings: " + offendStr, this);
                    }
                }
            } else {
                return new HttpResultOuter(HttpResultOuter.TestOutcome.match, "HTTP status code matches", this);
            }
        } else {
            final String msg = "Expected HTTP status " + testConfig.stringifyExpectedHttpStatusCodes() + ", but got '" + httpStatusCode.toStringWithText() + "'";
            return new HttpResultOuter(HttpResultOuter.TestOutcome.http_status_mismatch, msg, this);
        }
    }

}

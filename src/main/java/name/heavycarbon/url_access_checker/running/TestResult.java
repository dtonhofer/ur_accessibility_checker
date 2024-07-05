package name.heavycarbon.url_access_checker.running;

import name.heavycarbon.url_access_checker.building.MethodName;
import name.heavycarbon.url_access_checker.building.TestConfig;
import name.heavycarbon.url_access_checker.http.*;
import name.heavycarbon.url_access_checker.printing.MyPrinting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;

/*

 This is a complex tree structure:

 > TestResult
 > |
 > +-- @NotNull  MethodName methodName // wrapped String
 > +-- @NotNull  String     msg        // the same as the String from "HttpResultOuter" (unless the URI construction failed)
 > +-- @Nullable Details    details    // locally visible record
 >               |
 >               +-- @NotNull URI uri
 >               +-- @NotNull TestConfig testConfig  // publicly visible record
 >               |            |
 >               |            +-- @Nullable Credentials credentials  // null if no credentials needed, never print this
 >               |            +-- @NotNull SelectedCredsCode selectedCredsCode // exist especially for printing to indicate credentials used
 >               |            +-- @NotNull Set<HttpStatusCode> expectedHttpStatusCodes // list on a single line
 >               |            +-- @NotNull MethodName methodName // same as the outer "methodName"
 >               |            +-- @NotNull String path // path of the URL, no need to print as we have the URI
 >               |            +-- @NotNull Options options // a Map key->value, do not print for now, contains Credentials, too
 >               |
 >               +-- @NotNull HttpResultOuter httpResultOuter // publicly visible record
 >                            |
 >                            +-- @NotNull TestOutcome testOutcome // overall outcome of the test, an enum
 >                            +-- @NotNull String msg // a synthetic message or the message from "inner failure"
 >                            +-- @NotNull HttpResultInner httpResultInner
 >                                |        |
 >                                |        +-- @NotNull HttpClientOutcome httpClientOutcome // an enum signaling what jaba.netHttpClient delivered
 >                                |
 >                                +->> @NotNull HttpResultInnerFailure
 >                                |             |
 >                                |             +-- @Nullable String exceptionClass; // null if there has not been an exception
 >                                |             +-- @NotNull String msg; // message from the exception or a synthetic message
 >                                |
 >                                +->> @NotNull HttpResultInnerSuccess
 >                                              |
 >                                              +-- @NotNull HttpStatusCode httpStatusCode // actual http status code returned, if needed synthesized
 >                                              +-- @Nullable HttpResponse<String> httpResponse  // null if java.net.HttpClient didn't provide anything
 >                                                            |
 >                                                            +------ (headers, body etc.)
 */

public class TestResult {

    // substructure used if a HTTP request could be performed

    private record Details(@NotNull URI uri, @NotNull TestConfig testConfig, @NotNull HttpResultOuter httpResultOuter) {
    }

    final @NotNull MethodName methodName; // also available as details.testConfig.methodName
    final @NotNull String msg;
    final @Nullable Details details;

    // ---
    // The standard constructor.
    // Note that the "methodName" is not passed separately, but taken form testConfig.
    // The "msg" is the msg from the HttpResultOuter.
    // ---

    public TestResult(@NotNull URI uri, @NotNull TestConfig testConfig, @NotNull HttpResultOuter httpResultOuter, @NotNull String msg) {
        this.methodName = testConfig.getMethodName(); // the method
        this.msg = msg;
        this.details = new Details(uri, testConfig, httpResultOuter);
    }

    // ---
    // This constructor is called when the URL couldn't even be properly built
    // The "msg" is the msg from the failed UriBuildResult.
    // ---

    public TestResult(@NotNull MethodName methodName, @NotNull String msg) {
        this.methodName = methodName;
        this.msg = msg;
        this.details = null;
    }

    // ---
    // Printing it up
    // ---

    private @NotNull String stringifyUri() {
        if (details != null) {
            return "URI         : " + details.uri;
        } else {
            return "";
        }
    }

    private @NotNull String stringifyDetails() {
        assert details != null;
        final var buf = new StringBuilder();
        MyPrinting.joinIfNotEmpty(buf, stringifyUri());
        {
            MyPrinting.joinIfNotEmpty(buf, "Test Configuration");
            final boolean withPath = false; // we already have the URI at the top level
            final boolean withMethodName = false; // we already have this at the top level
            final var str1 = details.testConfig.stringify(withPath, withMethodName);
            final var str2 = MyPrinting.indent(str1);
            MyPrinting.joinIfNotEmpty(buf, str2);
        }
        {

            MyPrinting.joinIfNotEmpty(buf, "HTTP result, outer");

            final AlsoAdd alsoAdd1 =
                    switch (details.httpResultOuter().getTestOutcome()) {
                        case match, request_failure -> AlsoAdd.nothing;
                        case body_mismatch -> AlsoAdd.httpResponseAndBody;
                        case http_status_mismatch -> AlsoAdd.httpResponse;
                    };
            final AlsoAdd alsoAdd2 = AlsoAdd.max(alsoAdd1, details.testConfig().getOptions().getAlsoAdd());
            final var str1 = details.httpResultOuter.stringify(alsoAdd2);
            final var str2 = MyPrinting.indent(str1);
            MyPrinting.joinIfNotEmpty(buf, str2);
        }
        return buf.toString();
    }

    // ---
    // This method returns a String that has no final newline, which is important
    // when chaining output.
    // ---

    public @NotNull String stringify() {
        final var buf = new StringBuilder();
        MyPrinting.joinIfNotEmpty(buf, "Method      : " + methodName.name());
        MyPrinting.joinIfNotEmpty(buf, "Message     : " + msg);
        if (details != null) {
            MyPrinting.joinIfNotEmpty(buf, stringifyDetails());
        }
        return MyPrinting.makeString(buf);
    }
}

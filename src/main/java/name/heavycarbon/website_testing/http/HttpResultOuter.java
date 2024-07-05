package name.heavycarbon.website_testing.http;

import name.heavycarbon.website_testing.printing.MyPrinting;
import org.jetbrains.annotations.NotNull;

// "msg" is synthetic of the "msg" from "inner failure"

public class HttpResultOuter {

    public enum TestOutcome {match, http_status_mismatch, body_mismatch, request_failure}

    private final @NotNull TestOutcome testOutcome;
    private final @NotNull String msg;
    private final @NotNull HttpResultInner httpResultInner;

    public HttpResultOuter(@NotNull TestOutcome testOutcome, @NotNull String msg, @NotNull HttpResultInner httpResultInner) {
        this.testOutcome = testOutcome;
        this.msg = msg;
        this.httpResultInner = httpResultInner;
    }

    public @NotNull TestOutcome getTestOutcome() {
        return testOutcome;
    }

    public @NotNull String getMsg() {
        return msg;
    }

    public @NotNull HttpResultInner getHttpResultInner() {
        return httpResultInner;
    }

    public @NotNull String stringify(@NotNull AlsoAdd alsoAdd) {
        final var buf = new StringBuilder();
        MyPrinting.joinIfNotEmpty(buf, "Test outcome : " + testOutcome);
        MyPrinting.joinIfNotEmpty(buf, "Message      : " + msg); // this SHOULD already be printed at the topmost level
        MyPrinting.joinIfNotEmpty(buf, "HTTP result, inner");
        {
            // here we actually cannot be fully object-oriented
            final String str1;
            if (httpResultInner instanceof HttpResultInnerSuccess success) {
                str1 = success.stringify(alsoAdd);
            } else if (httpResultInner instanceof HttpResultInnerFailure failure) {
                final boolean withFailureMsg = true; // this SHOULD already be printed at the topmost level
                str1 = failure.stringify(withFailureMsg);
            } else {
                throw new IllegalStateException("Program error");
            }
            final var str2 = MyPrinting.indent(str1);
            MyPrinting.joinIfNotEmpty(buf, str2);
        }
        return MyPrinting.makeString(buf);
    }

}



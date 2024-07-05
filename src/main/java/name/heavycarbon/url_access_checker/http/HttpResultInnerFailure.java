package name.heavycarbon.url_access_checker.http;

import name.heavycarbon.url_access_checker.building.TestConfig;
import name.heavycarbon.url_access_checker.printing.MyPrinting;
import org.jetbrains.annotations.NotNull;

import java.net.URI;

public class HttpResultInnerFailure extends HttpResultInner {

    public enum FailureType {io_exception, interrupt}

    private final @NotNull FailureType failureType;
    private final @NotNull String exceptionClass; // null if there has not been an exception
    private final @NotNull String msg; // message from the exception or a synthetic message

    public HttpResultInnerFailure(@NotNull FailureType failureType, @NotNull String exceptionClass, @NotNull String msg) {
        this.failureType = failureType;
        this.exceptionClass = exceptionClass;
        this.msg = msg;
    }

    public @NotNull String getExceptionClass() {
        return exceptionClass;
    }

    public @NotNull String getMsg() {
        return msg;
    }

    public @NotNull FailureType getFailureType() {
        return failureType;
    }

    public @NotNull String stringify(boolean withFailureMsg) {
        final StringBuilder buf = new StringBuilder();
        MyPrinting.joinIfNotEmpty(buf, this.getClass().getName()); // TODO this is ugly printout
        MyPrinting.joinIfNotEmpty(buf, "Failure type       : " + failureType);
        MyPrinting.joinIfNotEmpty(buf, "Exception class    : " + exceptionClass);
        if (withFailureMsg) {
            MyPrinting.joinIfNotEmpty(buf, "Message            : " + msg);
        }
        return MyPrinting.makeString(buf);
    }

    @Override
    public @NotNull HttpResultOuter checkHttpResponse(@NotNull URI uri, @NotNull TestConfig testConfig) {
        return new HttpResultOuter(HttpResultOuter.TestOutcome.request_failure, getMsg(), this);
    }
}

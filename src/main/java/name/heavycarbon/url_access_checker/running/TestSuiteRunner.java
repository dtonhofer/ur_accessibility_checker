package name.heavycarbon.url_access_checker.running;

import name.heavycarbon.url_access_checker.building.MachineName;
import name.heavycarbon.url_access_checker.building.Scheme;
import name.heavycarbon.url_access_checker.building.TestConfig;
import name.heavycarbon.url_access_checker.http.HttpRequesting;
import name.heavycarbon.url_access_checker.http.HttpResultInnerFailure;
import name.heavycarbon.url_access_checker.http.HttpResultInnerSuccess;
import name.heavycarbon.url_access_checker.http.HttpResultOuter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public abstract class TestSuiteRunner {

    private record UriBuildResult(boolean ok, @Nullable URI uri, @NotNull String msg) {
    }

    // https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/net/URI.html

    private static @NotNull TestSuiteRunner.UriBuildResult buildURI(@NotNull TestConfig.EndpointData endpointData) {
        @NotNull MachineName machineName = endpointData.machineName();
        @NotNull Scheme scheme = endpointData.scheme();
        @NotNull String path = endpointData.path();
        final String tPath = path.trim();
        if (tPath.isEmpty()) {
            return new UriBuildResult(false, null, "Path is empty");
        }
        if (!tPath.startsWith("/")) {
            return new UriBuildResult(false, null, "Path does not start with '/'");
        }
        try {
            final var uri = new URI(scheme.toString(), machineName.toString(), tPath, null);
            return new UriBuildResult(true, uri, "OK");
        } catch (URISyntaxException e) {
            String suffix = "";
            if (e.getMessage() != null) {
                suffix = "': '" + e.getMessage() + "'";
            }
            return new UriBuildResult(false, null, "Could not build URI from path '" + path + suffix);
        }
    }

    private static void runSingleTest(@NotNull TestConfig testConfig, @NotNull TestSuiteResults results) {
        final var uriBuildResult = buildURI(testConfig.getEndpoint());
        if (!uriBuildResult.ok()) {
            results.addToMismatches(new TestResult(testConfig.getMethodName(), uriBuildResult.msg()));
        } else {
            assert uriBuildResult.uri() != null;
            final var uri = uriBuildResult.uri();
            // ---->
            final var httpResultOuter = HttpRequesting.performHttpRequestAndCheckResponse(uri, testConfig);
            // <---
            if (httpResultOuter.getTestOutcome() == HttpResultOuter.TestOutcome.request_failure) {
                assert httpResultOuter.getHttpResultInner() instanceof HttpResultInnerFailure;
            } else {
                assert httpResultOuter.getHttpResultInner() instanceof HttpResultInnerSuccess;
            }
            final var tr = new TestResult(uri,
                    testConfig, // already contains method name
                    httpResultOuter,
                    httpResultOuter.getMsg());
            switch (httpResultOuter.getTestOutcome()) {
                case body_mismatch, http_status_mismatch, request_failure -> results.addToMismatches(tr);
                case match -> results.addToMatches(tr);
                default -> throw new IllegalStateException("Unhandled code: " + httpResultOuter.getTestOutcome());
            }
        }
    }

    public static TestSuiteResults runTestSuite(@NotNull List<TestConfig> testSuite) {
        final TestSuiteResults results = new TestSuiteResults();
        for (TestConfig testConfig : testSuite) {
            assert testConfig != null;
            runSingleTest(testConfig, results);
        }
        return results;
    }

}

package name.heavycarbon.website_testing.building;

import name.heavycarbon.website_testing.http.AlsoAdd;
import name.heavycarbon.website_testing.http.HttpStatusCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static name.heavycarbon.website_testing.building.TestConfig.WhatCreds;

public abstract class TestSuiteBuilderMethods {

    private final static TestConfig.CredData noCreds = new TestConfig.CredData(null, WhatCreds.none);

    // ---
    // Helpers
    // ---

    private static @NotNull TestConfig.CredData buildCredData(@NotNull Options options, @NotNull WhatCreds whatCreds) {
        final var creds = switch (whatCreds) {
            case good -> options.getGoodCreds();
            case bad -> options.getBadCreds();
            case none -> null;
        };
        if (creds == null && whatCreds != WhatCreds.none) {
            throw new IllegalArgumentException("Requested '" + whatCreds + "' but there are no such credentials");
        }
        return new TestConfig.CredData(creds, whatCreds);
    }

    private static @Nullable TestConfig.CredData buildCredDataLeniently(@NotNull Options options, @NotNull WhatCreds whatCreds) {
        if (whatCreds == WhatCreds.none) {
            return new TestConfig.CredData(null, WhatCreds.none);
        } else if (whatCreds == WhatCreds.good) {
            if (options.getGoodCreds() != null) {
                return new TestConfig.CredData(options.getGoodCreds(), WhatCreds.good);
            } else {
                return null;
            }
        } else {
            assert (whatCreds == WhatCreds.bad);
            if (options.getBadCreds() != null) {
                return new TestConfig.CredData(options.getBadCreds(), WhatCreds.bad);
            } else {
                return null;
            }
        }
    }

    // inject the possible response "Internal Server Error" into the result Set if needed
    private static @NotNull Set<HttpStatusCode> buildExpectedSet(boolean isIsrPossible, HttpStatusCode... sc) {
        if (sc.length == 0) {
            throw new IllegalArgumentException("You need to pass at least on HttpStatusCode");
        }
        final var expectedSet = new HashSet<>(Arrays.stream(sc).toList());
        if (isIsrPossible) {
            expectedSet.add(HttpStatusCode.internal_server_error);
        }
        return expectedSet;
    }

    private static @NotNull MethodName buildMethodName(@NotNull MethodName methodName, @NotNull String postfix, @NotNull WhatCreds whatCreds) {
        return new MethodName(methodName.name() + "/" + postfix + "/" + whatCreds);
    }

    // ---
    // Forbidden
    // ---

    // Asserting that URL is "forbidden", no credentials are required to test that.
    // But we throw in testing with any available credentials for good measure.

    public static @NotNull List<TestConfig> urlIsForbidden_WithAnyCredentials(@NotNull MethodName methodName, @NotNull TestConfig.EndpointData epData, @NotNull Options options) {
        final var res = new LinkedList<TestConfig>();
        for (WhatCreds whatCreds : WhatCreds.values()) {
            final var credData = buildCredDataLeniently(options, whatCreds);
            if (credData != null) {
                final var newMethodName = buildMethodName(methodName, "urlIsForbidden_WithAnyCredentials", whatCreds);
                final var httpExpected = HttpStatusCode.forbidden;
                res.add(new TestConfig(epData, credData, newMethodName, httpExpected, options));
            }
        }
        return res;
    }

    // An interesting mixture

    public static @NotNull List<TestConfig> urlIsUnauthorizedOrForbidden_WithAnyCredentials(@NotNull MethodName methodName, @NotNull TestConfig.EndpointData epData, @NotNull Options options) {
        final var res = new LinkedList<TestConfig>();
        for (WhatCreds whatCreds : WhatCreds.values()) {
            final var credData = buildCredDataLeniently(options, whatCreds);
            if (credData != null) {
                final var newMethodName = buildMethodName(methodName, "urlIsUnauthorizedOrForbidden_WithAnyCredentials", whatCreds);
                final var httpExpected = (whatCreds == WhatCreds.none) ? HttpStatusCode.unauthorized : HttpStatusCode.forbidden;
                res.add(new TestConfig(epData, credData, newMethodName, httpExpected, options));
            }
        }
        return res;
    }

    // ---
    // Accessible
    // ---

    // Asserting that URL is "accessible", if you have the correct credentials

    public static @NotNull List<TestConfig> urlIsAccessible_OnlyWithGoodCredentials(@NotNull MethodName methodName, @NotNull TestConfig.EndpointData epData, @NotNull Options options) {
        final var res = new LinkedList<TestConfig>();
        for (WhatCreds whatCreds : WhatCreds.values()) {
            final var newMethodName = buildMethodName(methodName, "urlIsAccessible_OnlyWithGoodCredentials", whatCreds);
            final var credData = buildCredData(options, whatCreds);
            final var httpExpected = switch (whatCreds) {
                case none -> HttpStatusCode.unauthorized;
                case bad -> HttpStatusCode.forbidden;
                case good -> HttpStatusCode.ok;
            };
            final var expectedSet = buildExpectedSet(options.isIsrPossible(), httpExpected);
            res.add(new TestConfig(epData, credData, newMethodName, expectedSet, options));
        }
        return res;
    }

    // Asserting that URL is "accessible" even with no credentials at all (no need to check "with credentials")

    public static @NotNull List<TestConfig> urlIsAccessible_WithoutCredentials(@NotNull MethodName methodName, @NotNull TestConfig.EndpointData epData, @NotNull Options options) {
        final var newMethodName = buildMethodName(methodName, "urlIsAccessible_WithoutCredentials", WhatCreds.none);
        final var expectedSet = buildExpectedSet(options.isIsrPossible(), HttpStatusCode.ok);
        return List.of(new TestConfig(epData, noCreds, newMethodName, expectedSet, options));
    }

    // ---
    // Missing
    // ---

    // If the URL is "not found" without credentials, it is also "not found" "with good or bad credentials"
    // We can use a general method that prepares TestConfig for all credentials available

    public static @NotNull List<TestConfig> urlIsMissing_WithAnyCredentials(@NotNull MethodName methodName, @NotNull TestConfig.EndpointData epData, @NotNull Options options) {
        final var newOptions = options.withAlsoAddToPrintout(AlsoAdd.httpResponseAndBody);
        final var res = new LinkedList<TestConfig>();
        for (WhatCreds whatCreds : WhatCreds.values()) {
            final var credData = buildCredDataLeniently(options, whatCreds);
            if (credData != null) {
                final var newMethodName = buildMethodName(methodName, "urlIsMissing_WithAnyCredentials", whatCreds);
                res.add(new TestConfig(epData, credData, newMethodName, HttpStatusCode.missing, newOptions));
            }
        }
        return res;
    }

    // The URL is forbidden with bad or no, but missing with goo credentials

    public static @NotNull List<TestConfig> urlIsMissing_WithGoodCredentialsOnly(@NotNull MethodName methodName, @NotNull TestConfig.EndpointData epData, @NotNull Options options, @NotNull List<String> bodyStrings) {
        final var newOptions = options.withAlsoAddToPrintout(AlsoAdd.httpResponseAndBody);
        final var res = new LinkedList<TestConfig>();
        {
            final var whatCreds = WhatCreds.none;
            final var credData = buildCredData(options, whatCreds);
            final var httpExpected = HttpStatusCode.unauthorized; // the page of the HTTP server is shown
            final var newMethodName = buildMethodName(methodName, "urlIsMissing_WithGoodCredentialsOnly", whatCreds);
            res.add(new TestConfig(epData, credData, newMethodName, httpExpected, newOptions));
        }
        {
            final var whatCreds = WhatCreds.bad;
            final var credData = buildCredData(options, whatCreds);
            final var httpExpected = HttpStatusCode.forbidden; // the page of the HTTP server is shown
            final var newMethodName = buildMethodName(methodName, "urlIsMissing_WithGoodCredentialsOnly", whatCreds);
            res.add(new TestConfig(epData, credData, newMethodName, httpExpected, newOptions));
        }
        {
            final var whatCreds = WhatCreds.good;
            final var credData = buildCredData(options, whatCreds);
            final var httpExpected = HttpStatusCode.missing; // the page of the Wiki is shown
            final var newMethodName = buildMethodName(methodName, "urlIsMissing_WithGoodCredentialsOnly", whatCreds);
            // add the body strings here
            res.add(new TestConfig(epData, credData, newMethodName, httpExpected, newOptions.withBodyStrings(bodyStrings)));
        }
        return res;
    }

    // ---
    // Moved
    // ---

    // If the URL is "moved" without credentials, it is also "moved" "with good or bad credentials"
    // We can use a general method that prepares TestConfig for all credentials available

    public static @NotNull List<TestConfig> urlIsMoved_WithAnyCredentials(@NotNull MethodName methodName, @NotNull TestConfig.EndpointData epData, @NotNull Options options) {
        final var res = new LinkedList<TestConfig>();
        for (WhatCreds whatCreds : WhatCreds.values()) {
            final var credData = buildCredDataLeniently(options, whatCreds);
            if (credData != null) {
                final var newMethodName = buildMethodName(methodName, "urlIsMoved_WithAnyCredentials", whatCreds);
                res.add(new TestConfig(epData, credData, newMethodName, HttpStatusCode.moved, options));
            }
        }
        return res;
    }

    // If the URL is not accessible with bad or no credentials, and moved with good credentials

    public static @NotNull List<TestConfig> urlIsMoved_OnlyWithGoodCredentials(@NotNull MethodName methodName, @NotNull TestConfig.EndpointData epData, @NotNull Options options) {
        final var res = new LinkedList<TestConfig>();
        for (WhatCreds whatCreds : WhatCreds.values()) {
            final var newMethodName = buildMethodName(methodName, "urlIsMoved_OnlyWithGoodCredentials", whatCreds);
            final var credData = buildCredData(options, whatCreds);
            final var httpExpected = switch (whatCreds) {
                case none -> HttpStatusCode.unauthorized;
                case bad -> HttpStatusCode.forbidden;
                case good -> HttpStatusCode.moved;
            };
            final var expectedSet = buildExpectedSet(options.isIsrPossible(), httpExpected);
            res.add(new TestConfig(epData, credData, newMethodName, expectedSet, options));
        }
        return res;
    }
}

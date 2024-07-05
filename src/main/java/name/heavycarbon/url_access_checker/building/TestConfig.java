package name.heavycarbon.url_access_checker.building;

import name.heavycarbon.url_access_checker.credentials.Credentials;
import name.heavycarbon.url_access_checker.http.HttpStatusCode;
import name.heavycarbon.url_access_checker.printing.MyPrinting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class TestConfig {

    // What credentials one want to apply in the test

    public enum WhatCreds {none, good, bad}

    // The URI is constructed from this

    public record EndpointData(@NotNull MachineName machineName, @NotNull Scheme scheme, @NotNull String path) {
    }

    // Credentials may be null in CredData if "selected creds" is equal to "none"

    public record CredData(@Nullable Credentials creds, @NotNull TestConfig.WhatCreds what) {
    }

    private final @NotNull TestConfig.EndpointData endpointData;
    private final @NotNull CredData credData;
    private final @NotNull MethodName methodName; // this is just the name of the Java method, used for logging
    private final @NotNull Options options;
    private final @NotNull Set<HttpStatusCode> expectedHttpStatusCodes = new HashSet<>();

    // ---
    // Standard constructor, taking a set of expected HttpStatusCodes
    // Later the URI will be constructed from the Endpoint and that may fail, but we don't check that here yet.
    // ---

    public TestConfig(@NotNull TestConfig.EndpointData endpointData, @NotNull CredData credData, @NotNull MethodName methodName, @NotNull Set<HttpStatusCode> expectedSet, @NotNull Options options) {
        this.endpointData = endpointData;
        this.credData = credData;
        if (credData.what != WhatCreds.none && credData.creds == null) {
            // it's okay to pass Credentials if 'useCredentials' is 'none'
            throw new IllegalArgumentException("No 'credentials' passed, but 'useCredentials' is not 'none' but '" + credData.what + "'");
        }
        this.methodName = methodName;
        this.options = options;
        this.expectedHttpStatusCodes.addAll(expectedSet);
        if (expectedHttpStatusCodes.isEmpty()) {
            throw new IllegalArgumentException("No 'expected http status code' value passed, need at least 1");
        }
    }

    // ---
    // Standard constructor, taking:
    // 1) Credentials (but you can still specify 'none' for "use credentials" to disregard that)
    // 2) a SINGLE expected HttpStatusCodes
    // ---

    public TestConfig(@NotNull TestConfig.EndpointData endpointData, @NotNull CredData credData, @NotNull MethodName methodName, @NotNull HttpStatusCode expected, @NotNull Options options) {
        this(endpointData, credData, methodName, Set.of(expected), options);
    }

    // ---
    // Is a given code expected?
    // ---

    public boolean isExpected(@NotNull HttpStatusCode code) {
        return expectedHttpStatusCodes.contains(code);
    }

    // ---
    // For printing: join all the "expected" HttpStatusCodes together into a single string
    // ---

    public @NotNull String stringifyExpectedHttpStatusCodes() {
        return expectedHttpStatusCodes.stream().map(sc -> "'" + sc.toStringWithText() + "'").collect(Collectors.joining(" or "));
    }

    // ---
    // Getters
    // ---

    public @NotNull CredData getCredData() {
        return credData;
    }

    // this is just the name of the caller, for logging

    public @NotNull MethodName getMethodName() {
        return methodName;
    }

    public @NotNull TestConfig.EndpointData getEndpoint() {
        return endpointData;
    }

    public @NotNull Options getOptions() {
        return options;
    }

    public @NotNull Set<HttpStatusCode> getExpectedSet() {
        return expectedHttpStatusCodes;
    }

    // ---
    // Stringification
    // ---

    public @NotNull String stringify(boolean withEndpoint, boolean withMethodName) {
        final var buf = new StringBuilder();
        // Do not print Credentials (just the "selected credentials" value)
        MyPrinting.joinIfNotEmpty(buf, "Selected credentials : " + getCredData().what());
        if (withEndpoint) {
            MyPrinting.joinIfNotEmpty(buf, "Scheme               : " + endpointData.scheme);
            MyPrinting.joinIfNotEmpty(buf, "Machine              : " + endpointData.machineName);
            MyPrinting.joinIfNotEmpty(buf, "Path                 : " + endpointData.path);
        }
        if (withMethodName) {
            MyPrinting.joinIfNotEmpty(buf, "Method               : " + methodName.name());
        }
        MyPrinting.joinIfNotEmpty(buf, "Expected codes       : " + stringifyExpectedHttpStatusCodes());
        return MyPrinting.makeString(buf);
    }

}

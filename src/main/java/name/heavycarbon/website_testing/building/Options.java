package name.heavycarbon.website_testing.building;

import name.heavycarbon.website_testing.credentials.Credentials;
import name.heavycarbon.website_testing.http.AlsoAdd;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// ---
// A class gives additional options for when calling HttpClient
// ---

public class Options {

    private final @Nullable List<String> bodyStrings; // strings to look for in body, generally null
    private final @Nullable Credentials goodCreds; // credentials that will be used for "good access"; may be null
    private final @Nullable Credentials badCreds; // credentials that will be used for "bad access"; may be null
    private final boolean isrIsPossible; // "internal server error is possible" if the access works but the application is down
    private final @NotNull AlsoAdd alsoAdd; // force printing of HTTP response and body

    public Options() {
        alsoAdd = AlsoAdd.nothing;
        bodyStrings = null;
        goodCreds = null;
        badCreds = null;
        isrIsPossible = false;
    }

    // Private because used from the withX() "builder methods" only, we won't let
    // consumers use this.

    private Options(@Nullable List<String> bodyStrings,
                    @Nullable Credentials goodCreds,
                    @Nullable Credentials badCreds,
                    @NotNull AlsoAdd alsoAdd,
                    boolean isrIsPossible) {
        this.alsoAdd = alsoAdd;
        this.bodyStrings = bodyStrings;
        this.goodCreds = goodCreds;
        this.badCreds = badCreds;
        this.isrIsPossible = isrIsPossible;
    }

    public boolean isBodyStringsSet() {
        return bodyStrings != null && !bodyStrings.isEmpty();
    }

    // ---
    // Builders
    // ---

    public @NotNull Options withBodyStrings(@Nullable List<String> bodyStrings) {
        return new Options(bodyStrings, this.goodCreds, this.badCreds, this.alsoAdd, this.isrIsPossible);
    }

    // convenience
    public @NotNull Options withBodyStrings(@Nullable String ... bodyStrings) {
        return new Options(List.of(bodyStrings), this.goodCreds, this.badCreds, this.alsoAdd, this.isrIsPossible);
    }

    public @NotNull Options withGoodCreds(@Nullable Credentials goodCreds) {
        return new Options(this.bodyStrings, goodCreds, this.badCreds, this.alsoAdd, this.isrIsPossible);
    }

    public @NotNull Options withBadCreds(@Nullable Credentials badCreds) {
        return new Options(this.bodyStrings, this.goodCreds, badCreds, this.alsoAdd, this.isrIsPossible);
    }

    public @NotNull Options withAlsoAddToPrintout(@NotNull AlsoAdd alsoAdd) {
        return new Options(this.bodyStrings, this.goodCreds, this.badCreds, alsoAdd, this.isrIsPossible);
    }

    public @NotNull Options withIsIsrPossible(boolean isrIsPossible) {
        return new Options(this.bodyStrings, this.goodCreds, this.badCreds, this.alsoAdd, isrIsPossible);
    }

    public @Nullable Credentials getCredentials(@NotNull TestConfig.WhatCreds selected) {
        return switch (selected) {
            case bad -> badCreds; // could be null
            case good -> goodCreds; // could be null
            case none -> null;
        };
    }

    public @Nullable List<String> getBodyStrings() {
        return bodyStrings;
    }

    public @Nullable Credentials getGoodCreds() {
        return goodCreds;
    }

    public @Nullable Credentials getBadCreds() {
        return badCreds;
    }

    public boolean isIsrPossible() {
        return isrIsPossible;
    }

    public @NotNull AlsoAdd getAlsoAdd() {
        return alsoAdd;
    }
}

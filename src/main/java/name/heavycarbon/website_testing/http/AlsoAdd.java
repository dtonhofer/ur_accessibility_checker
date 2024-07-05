package name.heavycarbon.website_testing.http;

import org.jetbrains.annotations.NotNull;

public enum AlsoAdd {
    nothing, httpResponse, httpResponseAndBody;

    public static AlsoAdd max(@NotNull AlsoAdd a1, @NotNull AlsoAdd a2) {
        int x = Math.max(a1.ordinal(), a2.ordinal());
        return AlsoAdd.values()[x];
    }
}

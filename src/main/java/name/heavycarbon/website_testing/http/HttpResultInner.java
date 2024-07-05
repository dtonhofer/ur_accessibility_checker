package name.heavycarbon.website_testing.http;

import name.heavycarbon.website_testing.building.TestConfig;
import org.jetbrains.annotations.NotNull;

import java.net.URI;

public abstract class HttpResultInner {

    public abstract @NotNull HttpResultOuter checkHttpResponse(@NotNull URI uri, @NotNull TestConfig testConfig);
}

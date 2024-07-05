package name.heavycarbon.url_access_checker.http;

import name.heavycarbon.url_access_checker.building.TestConfig;
import org.jetbrains.annotations.NotNull;

import java.net.URI;

public abstract class HttpResultInner {

    public abstract @NotNull HttpResultOuter checkHttpResponse(@NotNull URI uri, @NotNull TestConfig testConfig);
}

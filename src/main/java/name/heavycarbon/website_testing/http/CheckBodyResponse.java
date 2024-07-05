package name.heavycarbon.website_testing.http;

import org.jetbrains.annotations.NotNull;

import java.util.List;

record CheckBodyResponse(boolean ok, @NotNull List<String> failed) {
}

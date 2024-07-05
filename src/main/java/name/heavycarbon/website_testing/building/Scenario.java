package name.heavycarbon.website_testing.building;

import java.util.Arrays;
import java.util.stream.Collectors;

// ---
// Allowed scenarios:
// local - the originator of the HTTP/HTTPS requests is the local machine, querying the local webserver
// insider - the originator of the HTTP/HTTPS requests is in the set of "allowed" IP addresses
// outsider - the originator of the HTTP/HTTPS requests is the set of "disallowed" IP addresses (the world outside the "allowed" IP addresses)
// ---

public enum Scenario {

    local, insider, outsider;

    public final static String allScenariosAsString = Arrays.stream(Scenario.values()).map(Enum::toString).collect(Collectors.joining(", "));

}

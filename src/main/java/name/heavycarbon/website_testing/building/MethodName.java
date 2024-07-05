package name.heavycarbon.website_testing.building;

import org.jetbrains.annotations.NotNull;

// ---
// Just a wrapped string with a specific type for easier coding, as one can tell the compiler
// or linter more than just "this is a string"
// ---

public record MethodName(@NotNull String name) {
    public MethodName {
        if (!name.trim().equals(name)) {
            throw new IllegalArgumentException("The 'name' is not trimmed");
        }
        if (name.isEmpty()) {
            throw new IllegalArgumentException("The 'name' is empty");
        }
    }
}

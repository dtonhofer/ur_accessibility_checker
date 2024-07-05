package name.heavycarbon.website_testing.building;

import org.jetbrains.annotations.NotNull;

// ---
// Just a wrapped string with a specific type for easier coding, as one can tell the compiler
// or linter more than just "this is a string"
// ---

public class MachineName {

    @NotNull
    private final String name;

    public MachineName(String x) {
        if (x == null) {
            throw new IllegalArgumentException("The passed 'machine name' is (null))");
        }
        this.name = x.trim().toLowerCase();
        if (this.name.isEmpty()) {
            throw new IllegalArgumentException("The passed 'machine name' is empty after trimming");
        }
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else {
            return obj instanceof MachineName && this.name.equals(((MachineName) obj).name);
        }
    }

    @Override
    public String toString() {
        return name;
    }
}

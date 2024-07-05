package name.heavycarbon.url_access_checker.running;

import name.heavycarbon.url_access_checker.printing.MyPrinting;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class TestSuiteResults {

    // TestResults go into one of these two lists,

    private final @NotNull List<TestResult> matches = new LinkedList<>();
    private final @NotNull List<TestResult> mismatches = new LinkedList<>();

    // Default constructor

    public TestSuiteResults() {
    }

    // Constructor to use when you already have lists of TestResult
    // See also: merge()

    public TestSuiteResults(List<TestResult> matches, List<TestResult> mismatches) {
        this.matches.addAll(matches);
        this.mismatches.addAll(mismatches);
    }

    // direct access

    public @NotNull List<TestResult> getMatches() {
        return matches;
    }

    // direct access

    public @NotNull List<TestResult> getMismatches() {
        return mismatches;
    }

    public boolean isMatchesEmpty() {
        return matches.isEmpty();
    }

    public boolean isMismatchesEmpty() {
        return mismatches.isEmpty();
    }

    public int getMatchesCount() {
        return matches.size();
    }

    public int getMismatchesCount() {
        return mismatches.size();
    }

    public void addToMatches(@NotNull TestResult testResult) {
        matches.add(testResult);
    }

    public void addToMismatches(@NotNull TestResult testResult) {
        mismatches.add(testResult);
    }

    // ---
    // This method returns a String that has no final newline, which is important
    // when chaining output.
    // ---

    private String stringifyMatchesCommon(@NotNull String header, @NotNull List<TestResult> list) {
        final var buf = new StringBuilder();
        for (TestResult tr : list) {
            MyPrinting.joinIfNotEmpty(buf, header);
            final var str1 = tr.stringify();
            final var str2 = MyPrinting.indent(str1);
            MyPrinting.joinIfNotEmpty(buf, str2);
        }
        return MyPrinting.makeString(buf);
    }

    // ---
    // This method returns a String that has no final newline, which is important
    // when chaining output.
    // ---

    public String stringify(boolean printMatches) {
        final var buf = new StringBuilder();
        if (printMatches && !matches.isEmpty()) {
            MyPrinting.joinIfNotEmpty(buf, stringifyMatchesCommon("Match:", matches));
        }
        if (!mismatches.isEmpty()) {
            MyPrinting.joinIfNotEmpty(buf, stringifyMatchesCommon("*** Mismatch ***", mismatches));
        }
        return MyPrinting.makeString(buf);
    }

    // ---
    // Merging two TestSuiteResults into a single new one
    // ---

    public static TestSuiteResults merge(@NotNull TestSuiteResults... tsrs) {
        final List<TestResult> matches = new LinkedList<>();
        final List<TestResult> mismatches = new LinkedList<>();
        for (TestSuiteResults tsr : tsrs) {
            matches.addAll(tsr.matches);
            mismatches.addAll(tsr.mismatches);
        }
        return new TestSuiteResults(matches, mismatches);
    }

}

package name.heavycarbon.website_testing.main;

import name.heavycarbon.website_testing.building.*;
import name.heavycarbon.website_testing.credentials.Credentials;
import name.heavycarbon.website_testing.credentials.CredentialsFromFile;
import name.heavycarbon.website_testing.printing.MyPrinting;
import name.heavycarbon.website_testing.running.TestSuiteResults;
import name.heavycarbon.website_testing.running.TestSuiteRunner;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

// ---
// The command supports -h/--help (help) and -V/--version (version print)
// through the "mixinStandardHelpOptions" annotation
//
// Info about Picocli at: https://picocli.info/
// ---

@Command(name = "website_testing",
        version = "website_testing 0.1",  // TODO: How do you update the version number automatically from the POM?
        mixinStandardHelpOptions = true,
        usageHelpWidth = 100,
        sortOptions = false, // in help msg, sort options by their "order" attribute
        description = "\"Check access permissions for a set of known URLs\"")
public class UrlAccessChecker implements Callable<Integer> {

    // ------------------------------------
    // Converter class for Picocli option
    // ------------------------------------

    public static class MachineNameConverter implements CommandLine.ITypeConverter<MachineName> {

        @Override
        public MachineName convert(String s) throws Exception {
            return new MachineName(s);
        }
    }

    // ------------------------------------
    // Converter class for Picocli option
    // ------------------------------------

    public static class CredentialsConverter implements CommandLine.ITypeConverter<Credentials> {

        @Override
        public Credentials convert(final String rawFile) {
            if (rawFile.isBlank()) {
                throw new CommandLine.TypeConversionException("The file is blank: '" + rawFile + "'");
            }
            final Path path = new File(rawFile).toPath();
            final Path path2;
            if (path.startsWith(Path.of("~"))) {
                final Path homePath = Path.of(System.getProperty("user.home"));
                final Path tailPath = path.subpath(1, path.getNameCount());
                path2 = homePath.resolve(tailPath);
            } else {
                path2 = path;
            }
            final var file = path2.toFile();
            if (!file.exists()) {
                // if file is "blank" then the file does not exist, although
                // file.getAbsolutePath() still yields something correct
                throw new CommandLine.TypeConversionException("Given file '" + file.getAbsolutePath() + "' does not exist");
            }
            if (!file.isFile()) {
                throw new CommandLine.TypeConversionException("Given file '" + file.getAbsolutePath() + "' is not a file");
            }
            if (!file.canRead()) {
                throw new CommandLine.TypeConversionException("Given file '" + file.getAbsolutePath() + "' is not readable");
            }
            final var credentialReadingResult = CredentialsFromFile.obtainCredentialsFromFile(file);
            if (credentialReadingResult.ok()) {
                return credentialReadingResult.credentials();
            } else {
                throw new CommandLine.TypeConversionException("Reading file '" + file.getAbsolutePath() + "' failed: " + credentialReadingResult.msg());
            }
        }

    }
    // ------------------------------------
    // Options filled by Picocli
    // ------------------------------------

    // enum is transparently translated (case-sensitive)
    @CommandLine.Option(names = {"-s", "--scenario"}, order = 1, required = true, description = "This machine plays the role of: local, insider, outsider")
    private Scenario scenario;

    // MachineName is translated via converter
    @CommandLine.Option(names = {"-m", "--machine"}, converter = MachineNameConverter.class, order = 2, required = true, description = "Machine to check (IPv4 address or hostname)")
    private MachineName machine;

    // Credentials are pulled from the given file via converter
    @CommandLine.Option(names = {"--wiki-creds"}, converter = CredentialsConverter.class, order = 3, required = true, description = "File with credentials to the wiki URLs (accepts  '~/...' notation)")
    private Credentials wikiCreds;

    // Credentials are pulled from the given file via converter
    @CommandLine.Option(names = {"--tools-creds"}, converter = CredentialsConverter.class, order = 4, required = true, description = "File with credentials to the tools URLs (accepts '~/...' notation)")
    private Credentials toolsCreds;

    @CommandLine.Option(names = {"--print-matches"}, defaultValue = "false", order = 5, description = "Also print info about successful matches, not only failed matches")
    private boolean printMatches;

    // ------------------------------------
    // Stuff called from call()
    // ------------------------------------

    private static @NotNull List<TestConfig> buildTestSuite(@NotNull MachineName machine, @NotNull Scenario scenario, @NotNull Credentials toolsCreds, @NotNull Credentials wikiCreds) {
        if (scenario == Scenario.local) {
            return new TestSuiteBuilder_Local(machine, toolsCreds, wikiCreds).build();
        } else if (scenario == Scenario.outsider) {
            return new TestSuiteBuilder_Outsider(machine, toolsCreds, wikiCreds).build();
        } else {
            assert scenario == Scenario.insider;
            return new TestSuiteBuilder_Insider(machine, toolsCreds, wikiCreds).build();
        }
    }

    private static int expectedMatchCount(@NotNull Scenario scenario) {
        return switch (scenario) {
            case local -> 67;
            case outsider -> 88;
            case insider -> 90;
        };
    }

    private static int printFinalMessageAndReturnExitValue(@NotNull TestSuiteResults tsr, @NotNull Scenario scenario) {
        var exitVal = -1;
        final var buf = new StringBuilder();
        buf.append("********************************\n");
        if (tsr.isMatchesEmpty() && tsr.isMismatchesEmpty()) {
            buf.append("*** NO TESTS HAVE BEEN  RUN\n");
            exitVal = 2;
        } else if (tsr.isMismatchesEmpty()) {
            buf.append("*** THE TEST SUCCEEDED\n");
            buf.append("*** Matches : " + tsr.getMatchesCount() + "\n");
            buf.append("*** No mismatches\n");
            final int exp = expectedMatchCount(scenario);
            if (tsr.getMatchesCount() != exp) {
                buf.append("*** But expected " + exp + " matches\n");
                exitVal = 1;
            } else {
                exitVal = 0;
            }
        } else {
            buf.append("*** THE TEST FAILED\n");
            buf.append("*** Matches    : " + tsr.getMatchesCount() + "\n");
            buf.append("*** Mismatches : " + tsr.getMismatchesCount() + "\n");
            exitVal = 1;
        }
        buf.append("********************************");
        MyPrinting.log(buf);
        return exitVal;
    }

    // ---
    // Java's HTTPClient is hardcoded to retry access if its
    // access is refused. Set the number of retries to 0. Then failure
    // will be signaled with an IOException (rather than a proper
    // status code), we will handle that messily.
    // https://stackoverflow.com/questions/75150081/ioexception-too-many-authentication-attempts-limit-3-when-using-jdk-httpcli
    // For logging from HTTPClient, see
    // System.setProperty("jdk.httpclient.HttpClient.log", "all");
    // and https://medium.com/@kir.maxim/lesson-i-have-learned-from-using-jdk11-http-client-2cf990daba03
    // ---

    private static void prologue() {
        System.setProperty("jdk.httpclient.auth.retrylimit", "0");
    }

    private void entryPrint() {
        final var buf = new StringBuilder();
        buf.append("Accessing machine : " + machine + "\n");
        buf.append("Scenario          : " + scenario + "\n");
        buf.append("Wiki creds file   : " + wikiCreds + "\n");
        buf.append("Tools creds file  : " + toolsCreds);
        MyPrinting.log(buf);
    }

    // ------------------------------------
    // The entry point called by Picocli, returns the process exit value
    // ------------------------------------

    @Override
    public Integer call() {
        entryPrint();
        boolean addSep = true;
        prologue();
        // --->
        final List<TestConfig> testSuite = buildTestSuite(machine, scenario, toolsCreds, wikiCreds);
        final TestSuiteResults testSuiteResults = TestSuiteRunner.runTestSuite(testSuite);
        // <---
        {
            String str = testSuiteResults.stringify(printMatches);
            if (!str.isEmpty()) {
                MyPrinting.newline(addSep);
                MyPrinting.log(str);
                addSep = true;
            }
        }
        MyPrinting.newline(addSep);
        final int exitValue = printFinalMessageAndReturnExitValue(testSuiteResults, scenario);
        MyPrinting.newline(addSep);
        return exitValue;
    }

    // ---
    // MAIN just invokes the Picocli command line processor
    // ---

    public static void main(String... args) {
        final var mainClass = new UrlAccessChecker();
        final var exitCode = new CommandLine(mainClass).execute(args);
        System.exit(exitCode);
    }
}
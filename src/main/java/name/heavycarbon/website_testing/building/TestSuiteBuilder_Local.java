package name.heavycarbon.website_testing.building;

import name.heavycarbon.website_testing.credentials.Credentials;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static name.heavycarbon.website_testing.building.TestSuiteBuilderMethods.*;

// ---
// For tests run from a "local" machine that can access URLs with no credentials
// ---

public class TestSuiteBuilder_Local {

    private final static String CLASS = TestSuiteBuilder_Local.class.getSimpleName();

    private final @NotNull MachineName machine;
    private final @NotNull Credentials wikiCreds;
    private final Options defaultOptions = new Options()
            .withBadCreds(new Credentials("foo", "bar"));

    public TestSuiteBuilder_Local(@NotNull MachineName machine, @NotNull Credentials toolCreds, @NotNull Credentials wikiCreds) {
        this.machine = machine;
        this.wikiCreds = wikiCreds;
    }

    private @NotNull TestConfig.EndpointData ep(@NotNull String path) {
        return new TestConfig.EndpointData(machine, Scheme.https, path);
    }

    private @NotNull List<TestConfig> buildTestSuite_Tools() {
        final var methodName = new MethodName(CLASS + ":buildTestSuite_Tools");
        final var options = defaultOptions;
        return Stream.of(
                urlIsMoved_WithAnyCredentials(methodName, ep("/tools"), options),
                urlIsAccessible_WithoutCredentials(methodName, ep("/tools/"), options),
                urlIsAccessible_WithoutCredentials(methodName, ep("/tools/server-status"), options),
                urlIsAccessible_WithoutCredentials(methodName, ep("/tools/server-info"), options),
                urlIsAccessible_WithoutCredentials(methodName, ep("/tools/server-status/"), options),
                urlIsAccessible_WithoutCredentials(methodName, ep("/tools/server-info/"), options)
        ).flatMap(Collection::stream).toList(); // need to flatten the stream of sets
    }

    private @NotNull List<TestConfig> buildTestSuite_Root() {
        final var methodName = new MethodName(CLASS + ":buildTestSuite_Root");
        return urlIsAccessible_WithoutCredentials(methodName, ep("/"), defaultOptions);
    }

    private @NotNull List<TestConfig> buildTestSuite_RandomMissingFileAtRoot() {
        final var methodName = new MethodName(CLASS + ":buildTestSuite_RandomMissingFileAtRoot");
        final var ep = ep("/this_is_missing.html");
        final var newOptions = defaultOptions.withBodyStrings(
                "Object not found!",
                "Error 404",
                "<a href=\"/\">" + ep.machineName() + "</a>",
                "<span>Apache/2</span>");
        return urlIsMissing_WithAnyCredentials(methodName, ep, newOptions);
    }

    // the widely accessible and fancily indexed "stuff" directory

    private @NotNull List<TestConfig> buildTestSuite_StuffDirectory() {
        final var methodName = new MethodName(CLASS + ":buildTestSuite_StuffDirectory");
        final var newOptions = defaultOptions.withBodyStrings(
                "Index of /stuff",
                "Last modified",
                "Parent Directory");
        return Stream.of(
                        urlIsMoved_WithAnyCredentials(methodName, ep("/stuff"), defaultOptions),
                        urlIsAccessible_WithoutCredentials(methodName, ep("/stuff/"), newOptions))
                .flatMap(Collection::stream).toList(); // need to flatten the stream of sets
    }

    private @NotNull List<TestConfig> buildTestSuite_PersonalWikiAccessiblePages(@NotNull Credentials wikiCreds) {
        final var methodName = new MethodName(CLASS + ":buildTestSuite_PersonalWikiAccessiblePages");
        final var newOptions = defaultOptions
                .withGoodCreds(wikiCreds) // bad creds are already in there
                .withIsIsrPossible(true); // if the wiki isn't running
        return Stream.of(
                urlIsMoved_WithAnyCredentials(methodName, ep("/wikis/personal"), newOptions),
                urlIsAccessible_WithoutCredentials(methodName, ep("/wikis/personal/"), newOptions),
                urlIsAccessible_WithoutCredentials(methodName, ep("/wikis/personal/viewtopic.png"), newOptions),
                urlIsAccessible_WithoutCredentials(methodName, ep("/wikis/personal/index.html"), newOptions)
        ).flatMap(Collection::stream).toList(); // need to flatten the stream of sets
    }

    private @NotNull List<TestConfig> buildTestSuite_PersonalWikiBinDirectory(@NotNull Credentials wikiCreds) {
        final var methodName = new MethodName(CLASS + ":buildTestSuite_PersonalWikiBinDirectory");
        final var newOptions = defaultOptions
                .withGoodCreds(wikiCreds) // bad creds are already in there
                .withIsIsrPossible(true); // if the wiki isn't running
        return Stream.of(
                // the "configure" script - this will change after finished installation
                urlIsAccessible_OnlyWithGoodCredentials(methodName, ep("/wikis/personal/bin/configure"), newOptions),
                // the "bin" directory
                urlIsMoved_WithAnyCredentials(methodName, ep("/wikis/personal/bin"), newOptions),
                urlIsForbidden_WithAnyCredentials(methodName, ep("/wikis/personal/bin/"), newOptions),
                urlIsAccessible_WithoutCredentials(methodName, ep("/wikis/personal/bin/attach"), newOptions),
                urlIsAccessible_WithoutCredentials(methodName, ep("/wikis/personal/bin/view"), newOptions)
        ).flatMap(Collection::stream).toList(); // need to flatten the stream of sets
    }

    private @NotNull List<TestConfig> buildTestSuite_PersonalWikiPubDirectory(@NotNull Credentials wikiCreds) {
        final var methodName = new MethodName(CLASS + ":buildTestSuite_PersonalWikiPubDirectory");
        final var newOptions = defaultOptions
                .withGoodCreds(wikiCreds) // bad creds are already in there
                .withIsIsrPossible(true); // if the wiki isn't running
        final var localOptionsWithBody = newOptions.withBodyStrings("You are trying to access an attachment that does not exist");
        return Stream.of(
                // pub is actually browsable
                urlIsMoved_WithAnyCredentials(methodName, ep("/wikis/personal/pub"), newOptions),
                urlIsAccessible_WithoutCredentials(methodName, ep("/wikis/personal/pub/"), newOptions),
                // error 404 is special
                urlIsMissing_WithAnyCredentials(methodName, ep("/wikis/personal/pub/this_is_missing.html"), localOptionsWithBody),
                // pub/trash is inaccessible
                urlIsForbidden_WithAnyCredentials(methodName, ep("/wikis/personal/pub/Trash/"), newOptions)
        ).flatMap(Collection::stream).toList(); // need to flatten the stream of sets
    }

    private @NotNull List<TestConfig> buildTestSuite_PersonalWikiInaccessiblePages(@NotNull Credentials wikiCreds) {
        final var methodName = new MethodName(CLASS + ":buildTestSuite_PersonalWikiInaccessiblePages");
        final var newOptions = defaultOptions
                .withGoodCreds(wikiCreds) // bad creds are already in there
                .withIsIsrPossible(true); // if the wiki isn't running
        return Stream.of(
                urlIsForbidden_WithAnyCredentials(methodName, ep("/wikis/personal/data/mime.types"), newOptions),
                urlIsForbidden_WithAnyCredentials(methodName, ep("/wikis/personal/lib/Assert.pm"), newOptions),
                urlIsForbidden_WithAnyCredentials(methodName, ep("/wikis/personal/locale/da.po"), newOptions),
                urlIsForbidden_WithAnyCredentials(methodName, ep("/wikis/personal/STUFF/LICENSE"), newOptions),
                urlIsForbidden_WithAnyCredentials(methodName, ep("/wikis/personal/templates/oops.tmpl"), newOptions),
                urlIsForbidden_WithAnyCredentials(methodName, ep("/wikis/personal/tools/extender.pl"), newOptions),
                urlIsForbidden_WithAnyCredentials(methodName, ep("/wikis/personal/working/README"), newOptions)
        ).flatMap(Collection::stream).toList(); // need to flatten the stream of sets
    }

    // Special config files that must stay inaccessible at all times

    private @NotNull List<TestConfig> buildTestSuite_PersonalWikiConfigFiles(@NotNull Credentials wikiCreds) {
        final var methodName = new MethodName(CLASS + ":buildTestSuite_PersonalWikiConfigFiles");
        final var newOptions = defaultOptions
                .withGoodCreds(wikiCreds) // bad creds are already in there
                .withIsIsrPossible(true); // if the wiki isn't running
        return Stream.of(
                urlIsForbidden_WithAnyCredentials(methodName, ep("/wikis/personal/bin/LocalLib.cfg"), newOptions),
                urlIsForbidden_WithAnyCredentials(methodName, ep("/wikis/personal/bin/setlib.cfg"), newOptions)
        ).flatMap(Collection::stream).toList(); // need to flatten the stream of sets
    }

    // ---
    // CALLED FROM MAIN
    // ---

    public @NotNull List<TestConfig> build() {
        return Stream.of(
                buildTestSuite_Tools(),
                buildTestSuite_Root(),
                buildTestSuite_RandomMissingFileAtRoot(),
                buildTestSuite_StuffDirectory(),
                buildTestSuite_PersonalWikiAccessiblePages(wikiCreds),
                buildTestSuite_PersonalWikiBinDirectory(wikiCreds),
                buildTestSuite_PersonalWikiPubDirectory(wikiCreds),
                buildTestSuite_PersonalWikiInaccessiblePages(wikiCreds),
                buildTestSuite_PersonalWikiConfigFiles(wikiCreds)
        ).flatMap(Collection::stream).toList(); // need to flatten the stream of sets
    }
}

# url_access_checker

A simple program that checks a list of URLs to see whether they are properly locked down or, on the contrary, properly accessible.

- Based on Java 21.
- Uses the rather simple [`java.net.HttpClient`](https://docs.oracle.com/en/java/javase/21/docs/api/java.net.http/java/net/http/HttpClient.html)
  to perform requests (rather than [Apache HTTPClient](https://hc.apache.org/httpcomponents-client-4.5.x/index.html) for example.)
- Uses [Picocli](https://picocli.info/) to handle command line arguments.
- No complex exchange with the remote website is made, the program just queries an URL and then checks the
  [HTTP status code](https://en.wikipedia.org/wiki/List_of_HTTP_status_codes) (`ok`, `unauthorized`, `forbidden`, `missing`, `moved` etc.)
- The URLs to check are hardcoded in dedicated classes. That should probably be loosened, with data pulled in from a YAML file instead.
- Credentials (username-password pairs) are pulled in from external files.
- The program is supposed to be run with different _scenarios_:
  - `local` scenario: the program runs on machine that is considered "local". Most of the requests checked will result in "ok" and credentials are generally not needed.
    The requests to perform are defined in [`TestSuiteBuilder_Local.java`](src/main/java/name/heavycarbon/website_testing/building/TestSuiteBuilder_Local.java). 
  - `insider` scenario: the program runs on machine that is considered part of an "insider" group. Most of the request checked will result in "ok" if proper credentials are presented.
    The requests to perform are defined in [`TestSuiteBuilder_Insider.java`](src/main/java/name/heavycarbon/website_testing/building/TestSuiteBuilder_Insider.java).
  - `ousider` scenario: the program runs on machine that is considered part of an "outsider" group. Most of the request checked will result in "forbidden", irrespective of the credentials used.
    The requests to perform are defined in [`TestSuiteBuilder_Outsider.java`](src/main/java/name/heavycarbon/website_testing/building/TestSuiteBuilder_Outsider.java). 
 - The main class is [`UrlAccessChecker`](src/main/java/name/heavycarbon/website_testing/main/UrlAccessChecker.java).
 - A bash script to start the program is provided with [`runner.sh`](runner.sh)

# Notes

- `java.net.HttpClient` gives some trouble as a `forbidden` response (403) is communicated by `java.net.HttpClient` as a base `IOException`, which is just bad design.
  An absolutely valid response from the remote webserver should not yield an exception at this level of abstraction, especially not a very general one. So we
  have to code around this phenomenon. See [`HttpRequesting.java`](src/main/java/name/heavycarbon/website_testing/http/HttpRequesting.java).
- HTTP Status codes are represented by a dedicated "quasi-enum" class: [`HttpStatusCode.java`](src/main/java/name/heavycarbon/website_testing/http/HttpStatusCode.java).

#!/bin/bash

# ---
# This script assumes that you have created an executable uberjar using the "mvn:package" phase.
# (see https://maven.apache.org/plugins/maven-shade-plugin/ )
#
# In this setup, there are "tools" URLs and "wiki" URLs and they need different "good" credentials.
# Credentials can be found in files:
#
# wiki_credentials.txt
# tools_credentials.txt
#
# Each of the above contains a single username-password pair simply coded as:
#
# username=...
# password=...
#
# There is no need to add anything to the CLASSPATH as everything is in the "uberjar".
# ---

# arguments to give to this script:

SCENARIO=$1 # one of "local", "insider", "outsider"
MACHINE=$2 # the host to test

# where to find the elements

CREDDIR="$HOME/creds"
UBERJAR="$HOME/url_access_checker-1.0.jar"

# here we go; note that we switch on Java assertions with "-ea"

java -ea \
        -jar "$UBERJAR" \
        --machine=$MACHINE \
        --scenario=$SCENARIO \
        --wiki-creds="$CREDDIR/wiki_credentials.txt" \
        --tools-creds="$CREDDIR/tools_credentials.txt"
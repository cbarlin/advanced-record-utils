#!/bin/bash

# Since I keep forgetting to check the javadoc builds...
rm -rf ~/.m2/io/github/cbarlin && \
    mvn clean install && \
    mvn clean verify artifact:compare && \
    rm -rf ~/.m2/io/github/cbarlin
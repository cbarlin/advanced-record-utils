#!/bin/bash
set -euo pipefail

# Note that despite the name this is actually run on local machines as a way
#  to run the the build locally similarly to how the github action does it
#  since I keep forgetting to check the javadoc builds...
rm -rf ~/.m2/io/github/cbarlin && \
    mvn clean install && \
    mvn clean verify artifact:compare && \
    rm -rf ~/.m2/io/github/cbarlin

java -jar ~/Downloads/jacococli.jar report ./z_report_module/target/jacoco.exec \
    --classfiles advanced-record-utils-processor/target/classes \
    --classfiles aru-processor-core/target/classes \
    --sourcefiles advanced-record-utils-processor/src/main/java/ \
    --sourcefiles aru-processor-core/src/main/java/ \
    --sourcefiles aru-processor-core/target/generated-sources/annotations/ \
    --html target/test
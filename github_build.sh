#!/bin/bash
set -euo pipefail

export JAVA_HOME=/usr/lib64/jvm/temurin-21-jdk
# Note that despite the name this is actually run on local machines as a way
#  to run the the build locally similarly to how the github action does it
#  since I keep forgetting to check the javadoc builds...
rm -rf ~/.m2/io/github/cbarlin && \
    mvn clean install -T4 && \
    mvn clean verify artifact:compare -T4 && \
    rm -rf ~/.m2/io/github/cbarlin

# Generate JaCoCo coverage report if jacococli.jar is available
JACOCO_CLI="${JACOCO_CLI:-$HOME/Downloads/jacococli.jar}"
if [ -f "$JACOCO_CLI" ] && [ -f "./z_report_module/target/jacoco.exec" ]; then
    echo "Generating JaCoCo coverage report..."
    java -jar "$JACOCO_CLI" report ./z_report_module/target/jacoco.exec \
        --classfiles advanced-record-utils-processor/target/classes \
        --classfiles aru-processor-core/target/classes \
        --sourcefiles advanced-record-utils-processor/src/main/java/ \
        --sourcefiles aru-processor-core/src/main/java/ \
        --sourcefiles aru-processor-core/target/generated-sources/annotations/ \
        --html target/site
else
    echo "Skipping JaCoCo report generation (jacococli.jar not found or no execution data)"
fi
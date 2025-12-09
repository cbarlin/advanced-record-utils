#!/bin/bash

set -e

# --- Configuration ---
RELEASE_VERSION="$1"
NEXT_DEV_VERSION="$2"
RELEASE_TAG="$RELEASE_VERSION"
MAIN_BRANCH="main"
REMOTE_NAME="origin"
ARTIFACT_DIR="target/central-staging"

# Force Java 21 for release builds
# export JAVA_HOME=/usr/lib/jvm/java-21-openjdk

# --- Input Validation ---
if [ -z "$RELEASE_VERSION" ] || [ -z "$NEXT_DEV_VERSION" ]; then
  echo "Usage: $0 <release-version> <next-development-version>"
  echo "Example: $0 1.0.0 1.0.1-SNAPSHOT"
  exit 1
fi

# --- Dependency Check ---
if ! command -v gh >/dev/null 2>&1; then
    echo >&2 "ERROR: GitHub CLI 'gh' not found, but required for creating GitHub releases."
    echo >&2 "Please install it (e.g., 'brew install gh', 'sudo apt install gh') and authenticate ('gh auth login')."
    exit 1
fi

echo "Starting release process..."
echo "  Release Version: $RELEASE_VERSION"
echo "  Next Dev Version: $NEXT_DEV_VERSION"
echo "  Release Tag: $RELEASE_TAG"
echo "  Main Branch: $MAIN_BRANCH"
echo "  Remote: $REMOTE_NAME"
echo "  Artifact Directory: $ARTIFACT_DIR"


# --- Pre-checks ---
echo "Checking for uncommitted changes..."
if ! git diff-index --quiet HEAD --; then
    echo "ERROR: Uncommitted changes detected. Please commit or stash them before releasing."
    exit 1
fi

echo "Checking current branch is $MAIN_BRANCH..."
CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
if [ "$CURRENT_BRANCH" != "$MAIN_BRANCH" ]; then
    echo "ERROR: Not on the '$MAIN_BRANCH' branch. Current branch is '$CURRENT_BRANCH'."
    exit 1
fi

# --- Prepare Release Version & Validate ---
echo "Setting version to $RELEASE_VERSION..."
mvn clean
mvn versions:set -DnewVersion="$RELEASE_VERSION" -DprocessAllModules=true -DgenerateBackupPoms=true

# --- Parse Version ---
MAJOR_VERSION=$(echo "$RELEASE_VERSION" | cut -d. -f1)
MINOR_VERSION=$(echo "$RELEASE_VERSION" | cut -d. -f2)
PATCH_VERSION=$(echo "$RELEASE_VERSION" | cut -d. -f3)

# --- Update Version Constants ---
echo "Updating version constants in AdvancedRecordUtilsGenerated.java..."
sed -i "s/MAJOR_VERSION = [0-9]\{1,\};/MAJOR_VERSION = $MAJOR_VERSION;/" advanced-record-utils-annotations/src/main/java/io/github/cbarlin/aru/annotations/AdvancedRecordUtilsGenerated.java
sed -i "s/MINOR_VERSION = [0-9]\{1,\};/MINOR_VERSION = $MINOR_VERSION;/" advanced-record-utils-annotations/src/main/java/io/github/cbarlin/aru/annotations/AdvancedRecordUtilsGenerated.java
sed -i "s/PATCH_VERSION = [0-9]\{1,\};/PATCH_VERSION = $PATCH_VERSION;/" advanced-record-utils-annotations/src/main/java/io/github/cbarlin/aru/annotations/AdvancedRecordUtilsGenerated.java


echo "Building and validating reproducibility for $RELEASE_VERSION..."
BUILD_VALIDATE_SUCCESS=0
(mvn clean && mvn install && mvn clean verify artifact:compare) || BUILD_VALIDATE_SUCCESS=$?

if [ $BUILD_VALIDATE_SUCCESS -eq 0 ]; then
    echo "Build and reproducibility check successful."

    # --- Commit, Tag, Push Release ---
    echo "Committing release version $RELEASE_VERSION..."
    git commit -am "[ci skip] Prepare release $RELEASE_VERSION"

    echo "Tagging release as $RELEASE_TAG..."
    git tag "$RELEASE_TAG" -m "Release $RELEASE_TAG"

    # --- Prepare Next Development Version ---
    echo "Setting version to next development version $NEXT_DEV_VERSION..."
    mvn versions:set -DnewVersion="$NEXT_DEV_VERSION" -DprocessAllModules=true -DgenerateBackupPoms=false

    echo "Committing next development version $NEXT_DEV_VERSION..."
    git commit -am "[ci skip] Prepare for next development iteration ($NEXT_DEV_VERSION)"

    # --- Push Changes ---
    echo "Pushing commits and tag to $REMOTE_NAME..."
    git push $REMOTE_NAME "$MAIN_BRANCH"
    git push $REMOTE_NAME "$RELEASE_TAG"

    # --- Deploy Release ---
    echo "Checking out tag $RELEASE_TAG for deployment..."
    # Store current HEAD commit before checkout to return accurately
    HEAD_ON_MAIN_BRANCH=$(git rev-parse HEAD)
    git checkout "$RELEASE_TAG"

    echo "Deploying version $RELEASE_VERSION with '-Prelease' profile..."
    DEPLOY_SUCCESS=0
    mvn deploy -Prelease -DskipTests || DEPLOY_SUCCESS=$?

    if [ $DEPLOY_SUCCESS -ne 0 ]; then
        echo "------------------------------------------------------------------------"
        echo "ERROR: Deployment failed!"
        echo "The release was tagged ($RELEASE_TAG) and pushed, but deployment failed."
        echo "To re-deploy manually:"
        echo "  git checkout $RELEASE_TAG"
        echo "  mvn deploy -Prelease -DskipTests"
        echo "To roll back (use with caution):"
        echo "  git push $REMOTE_NAME :refs/tags/$RELEASE_TAG  # Delete remote tag"
        echo "  git tag -d $RELEASE_TAG                       # Delete local tag"
        echo "  git reset --hard $HEAD_ON_MAIN_BRANCH~2       # Revert last two commits (use hash before checkout)"
        echo "  git push --force $REMOTE_NAME $MAIN_BRANCH    # Force push the reverted history (dangerous!)"
        echo "------------------------------------------------------------------------"
        echo "Attempting to checkout back to $MAIN_BRANCH..."
        git checkout "$MAIN_BRANCH" || echo "WARN: Failed to checkout $MAIN_BRANCH automatically."
        exit 1
    fi

    echo "Creating draft GitHub release for $RELEASE_TAG..."
    GH_RELEASE_NOTES="Draft release for version $RELEASE_VERSION."
    GH_RELEASE_TITLE="Release $RELEASE_VERSION"
    GH_RELEASE_ARTIFACT_GLOB="$ARTIFACT_DIR/*"

    # Check if artifact directory exists and is not empty
    if [ -d "$ARTIFACT_DIR" ] && [ -n "$(ls -A $ARTIFACT_DIR)" ]; then
        echo "Uploading artifacts from $ARTIFACT_DIR..."
        gh release create "$RELEASE_TAG" \
            --draft \
            --title "$GH_RELEASE_TITLE" \
            --notes "$GH_RELEASE_NOTES" \
            target/central-staging/io/github/cbarlin/aru-parent/${RELEASE_VERSION}/*.pom \
            target/central-staging/io/github/cbarlin/aru-prism-prison/${RELEASE_VERSION}/*.pom \
            target/central-staging/io/github/cbarlin/aru-prism-prison/${RELEASE_VERSION}/*.jar \
            target/central-staging/io/github/cbarlin/aru-processor-core/${RELEASE_VERSION}/*.pom \
            target/central-staging/io/github/cbarlin/aru-processor-core/${RELEASE_VERSION}/*.jar \
            target/central-staging/io/github/cbarlin/advanced-record-utils-annotations/${RELEASE_VERSION}/*.pom \
            target/central-staging/io/github/cbarlin/advanced-record-utils-annotations/${RELEASE_VERSION}/*.jar \
            target/central-staging/io/github/cbarlin/advanced-record-utils-processor/${RELEASE_VERSION}/*.pom \
            target/central-staging/io/github/cbarlin/advanced-record-utils-processor/${RELEASE_VERSION}/*.jar
        echo "Draft GitHub release created successfully."
    else
        echo "WARN: Artifact directory '$ARTIFACT_DIR' not found or empty. Creating draft release without artifacts."
        gh release create "$RELEASE_TAG" \
            --draft \
            --title "$GH_RELEASE_TITLE" \
            --notes "$GH_RELEASE_NOTES"
        echo "Draft GitHub release created successfully (without artifacts)."
    fi

    echo "Checking back out to $MAIN_BRANCH..."
    git checkout "$MAIN_BRANCH"
    mvn versions:commit

    echo "------------------------------------------------------------------------"
    echo "Release $RELEASE_VERSION successful!"
    echo "Deployed artifacts, pushed commits and tag $RELEASE_TAG to $REMOTE_NAME."
    echo "Created draft GitHub release: $GH_RELEASE_TITLE"
    echo "------------------------------------------------------------------------"

else
    echo "------------------------------------------------------------------------"
    echo "ERROR: Build or reproducibility check failed for version $RELEASE_VERSION."
    echo "Reverting version changes in POM files..."
    mvn versions:revert -DprocessAllModules=true
    echo "Release process aborted. No commits or tags were created."
    echo "------------------------------------------------------------------------"
    exit 1
fi

exit 0
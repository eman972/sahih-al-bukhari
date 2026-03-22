This directory holds the bundled data file that gets packaged into the JAR.

  bukhari.json.gz   ← place it here before building

How to populate it
------------------

Option A — from the monorepo root (recommended):

  node java/scripts/copy-data-java.mjs

Option B — manually:

  mkdir -p java/src/main/resources
  cp data/bukhari.json.gz java/src/main/resources/

The file is intentionally excluded from Git (.gitignore) because it is
generated/copied as part of the build step.

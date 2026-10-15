# MRPSP Asset App — Auto-update source

Prepared for the existing repository: https://github.com/kolkatametroproject-web/MRPSP-Asset-App

## What this package does
- Loads the current GitHub Pages website: https://kolkatametroproject-web.github.io/firebase-excel/
- Removes the stale `injectAssetOnlyMode()` call that caused GitHub Actions Build #10 to fail.
- Adds a manual “Check for app update” action and GitHub Releases-based update-checking scaffolding.
- Keeps the existing cloud build workflow.

## Important signing limitation
Android only installs an update over an existing installation when the package ID and signing certificate match. The old workflow builds a debug APK using a temporary runner-generated debug key, which is not stable across fresh GitHub runners. Do not distribute a sequence of those APKs as in-place updates.

For reliable native APK updates, create a permanent Android upload/signing keystore, store its Base64 value and passwords as GitHub Actions secrets, and use the signed-release workflow in `CLOUD_BUILD_GUIDE.md`. Never commit the keystore/passwords. Android will still require the user to approve installation for a sideloaded app; a regular app cannot silently install its own APK.

## Apply to the existing repository
Upload/extract these files over the same paths in the existing repository, commit to `main`, and check Actions. This ZIP does not overwrite your repository by itself.

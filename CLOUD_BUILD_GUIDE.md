# Use this ZIP with the existing GitHub repository

Existing repository: https://github.com/kolkatametroproject-web/MRPSP-Asset-App

1. Download and extract this ZIP.
2. In the existing repository, use **Add file → Upload files** and upload the extracted files, preserving folder paths. If browser upload is awkward, use GitHub Codespaces or the GitHub web editor to replace each file at its exact path.
3. Commit to `main`.
4. Open Actions: https://github.com/kolkatametroproject-web/MRPSP-Asset-App/actions
5. Wait for the workflow to finish. The stale `injectAssetOnlyMode()` compiler error should be resolved.

## Native APK update requirements
Website HTML/JS updates are served by GitHub Pages and appear when the WebView reloads; a native APK rebuild is not needed for website-only changes.

For native updates outside Google Play:
- Use the same `applicationId` (`com.mrpsp.assetapp`) and same signing key for every APK.
- Generate a permanent keystore once and back it up securely.
- Store secrets in repository Settings → Secrets and variables → Actions. Do not commit the keystore or passwords.
- Publish a GitHub Release with the signed APK attached. Update checker code should compare the installed `versionCode` with the latest release and only offer the APK when newer.
- The user must enable “Install unknown apps” for the app/browser used and approve the Android package installer. Normal Android apps cannot silently install APKs.

This source package does not create a production signing key or make an existing debug installation magically updateable. First establish a stable signing key and distribute the first APK signed with it; later releases can update that installation.

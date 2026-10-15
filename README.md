# MRPSP Asset App — Simple Cloud Build

This is a minimal Android WebView wrapper for the existing hosted website:
https://kolkatametroproject-web.github.io/firebase-excel/

## Upload to your existing GitHub repository
1. Extract this ZIP on your computer.
2. Open https://github.com/kolkatametroproject-web/MRPSP-Asset-App
3. Upload the extracted files and folders to the repository root, replacing matching Android project files. Do not upload the ZIP itself.
4. Confirm the workflow path is `.github/workflows/build-apk.yml`.
5. Commit to `main`, then open the Actions tab and wait for the build to succeed.
6. Download the `MRPSP-Asset-App-debug-APK` artifact, extract it, and install `app-debug.apk` on Android.

## Important
- This ZIP is Android source, not a prebuilt APK. GitHub Actions must compile it.
- Website content changes are shown when the app reloads; native Android source changes require a new APK.
- This is a debug APK for testing. Debug builds are not suitable for a robust automatic-update channel.
- The app requests camera permission for website face verification/scanning. If permission is denied, non-camera website features can still load.

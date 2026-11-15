# Manifest or resources stay old after a change

**Seen:** 12 November 2026 (trial) and 14 November 2026 (kick-off session). Kotlin Toolchain 0.13.0.

## What you see

You change only `android/src/AndroidManifest.xml` or a file in `android/res/` and build again. The build succeeds, but the APK still contains the old manifest or resources. Nothing warns you.

## Cause

Unknown. The incremental build does not seem to notice a change in only these files.

## Workaround

1. Stop a running server: `clean` has no `-m` option and wipes the whole project, including the server.
2. Run `./kotlin clean` (Windows: `kotlin.bat clean`) and build again.
3. Check that the change is in the APK, for example the manifest:

```bash
~/Android/Sdk/build-tools/37.0.0/aapt2 dump xmltree --file AndroidManifest.xml build/tasks/_android_buildAndroidDebug/gradle-project-debug.apk
```

## Status

Not reported to JetBrains yet (FTD R-A2).

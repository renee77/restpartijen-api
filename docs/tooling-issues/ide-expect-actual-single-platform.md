# IntelliJ marks `expect`/`actual` as an error

**Seen:** 15 November 2026. Kotlin Toolchain 0.13.0, IntelliJ IDEA 2026.3.

## What you see

In `shared` (a `kmp/lib` with only `platforms: [ android ]`), an `expect` declaration in `src/` with its `actual` in `src@android/` is marked red in IntelliJ, as if `expect`/`actual` only works in a multiplatform project. The exact message is still to be added.

`./kotlin build` succeeds, and the compiler does check the pair: without the `actual`, the build fails with *"The 'expect' declaration … has no 'actual' declaration"* (measured).

## Cause

The toolchain documentation says to put `expect` in `src/` and `actual` in `src@<platform>/`. It says nothing about a module with one platform. IntelliJ apparently does not treat such a module as multiplatform (assumption).

## Workaround

We do not use `expect`/`actual` for the Koin platform modules. They are plain values in `src@android/`, combined in `platformModule`, which `RestpartijenApplication` loads next to `appModule` (FTD A-29). Tests in `shared/test/` can see `src@android/` (measured).

## Status

Not reported to JetBrains yet. Add the exact IntelliJ message before reporting.

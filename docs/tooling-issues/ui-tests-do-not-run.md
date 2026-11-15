# UI tests do not run

**Seen:** 12 November 2026 (trial). Kotlin Toolchain 0.13.0.

## What you see

- **Robolectric** 4.17 with `createComposeRule` fails in a unit test. First the activity from `ui-test-manifest` is not merged; after that `NoClassDefFoundError: androidx/customview/poolingcontainer/R$id`.
- **Instrumented tests** on the emulator: the toolchain documentation only knows `test/` and `test@platform/`.

## Cause

The toolchain does not generate `R` classes and library resources for unit tests, and has no instrumented test support (measured in the trial; documentation of the toolchain).

## Workaround

None yet. At least three UI tests are required (FTD §1.5), so this is a risk (FTD R-A9). The question is with the teacher (FTD O-12). Fallback: convert to Gradle with ktc-to-gradle (FTD A-14).

## Status

Not reported to JetBrains yet.

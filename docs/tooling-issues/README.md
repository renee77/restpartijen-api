# Tooling issues and workarounds

Problems caused by the tooling (Kotlin Toolchain, IntelliJ IDEA, Android SDK), not by our code.
Each file describes one issue: what you see, the cause as far as we know, the workaround, and the status.

Add an issue when you lose time on the tooling, so the next person does not. Mark what you measured and what you assume.

| Issue | Affects | Workaround | Reported |
|---|---|---|---|
| [Manifest or resources stay old after a change](manifest-and-resources-stay-old.md) | `android` | `./kotlin clean`, then build | No |
| [UI tests do not run](ui-tests-do-not-run.md) | `shared` tests | None yet; question with the teacher (FTD O-12) | No |
| [IntelliJ marks `expect`/`actual` as an error](ide-expect-actual-single-platform.md) | `shared` | Platform code without `expect`/`actual` (FTD A-29) | No |
| [`./kotlin build` does not run Android Lint](no-android-lint.md) | `android`, `shared` | None yet (FTD O-15) | No |
| [JDK download fails on a fresh checkout](jdk-download-fails.md) | all modules | Copy the build state or download the JDK by hand | No |

Versions at the time of writing: Kotlin Toolchain 0.13.0, IntelliJ IDEA 2026.3, Kotlin 2.4.20.

# `./kotlin build` does not run Android Lint

**Seen:** 12 November 2026 (trial). Kotlin Toolchain 0.13.0.

## What you see

`./kotlin build` compiles, tests and packages the app, but no Android Lint report appears. Problems such as hard-coded texts outside `strings.xml` are therefore not found automatically (FTD NFR-A1).

## Workaround

None yet. Check by review in every pull request. How Android Lint can run with the toolchain is open (FTD O-15).

## Status

Not reported to JetBrains yet.

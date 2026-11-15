# JDK download fails on a fresh checkout

**Seen:** 24 October 2026, with Amper 0.12.2 (now Kotlin Toolchain). Not checked again with 0.13.0.

## What you see

`./kotlin run` in a fresh checkout stops with `TooLongLineException: Line exceeds limit of 8192 characters` while it downloads the Temurin JDK.

## Cause

GitHub sends a `content-security-policy` header of about 9200 characters; the HTTP client of the toolchain stops at 8192.

## Workaround

- Copy `build/incremental.state/jdk-list-download-*` and `jdk-provisioning___majorVersion__25_-*` from a checkout where it works, or
- download the JDK archive by hand into `~/.cache/JetBrains/Kotlin/download.cache/` and check the checksum.

## Status

Not reported to JetBrains yet. Check whether 0.13.0 still has this before reporting.

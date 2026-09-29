# Restpartijen API

Ktor Web API for the restpartijenplatform (proftaak, AI-powered software development, period 1).
The design is described in `FTD-restpartijen-webapi-v2_1.md`.

## Requirements

- IntelliJ IDEA 2026.2.1 or newer with the Kotlin Toolchain plugin
- No JDK needed: the Kotlin Toolchain downloads it on the first run

## Run

    ./kotlin run

On Windows: `kotlin.bat run`

## Test

    ./kotlin test

Tests live in `server/test`, not in `server/src`.

## Working agreements

- `main` always works. Nobody commits directly to `main`.
- Every change goes through a pull request with at least one review.
- Changes in `shared` need a review from both other team members.
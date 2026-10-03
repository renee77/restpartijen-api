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

## Configuration

- The application reads two environment variables: JWT_SECRET which is required to run, and DB_MODE which is optional. 
- | Variable | Required | Values | Default |
  |---|---|---|---|
  | `JWT_SECRET` | /* */ | /* at least 32 bytes */ | /* none: the app stops */ |
  | `DB_MODE` | /* */ | /* */ | /* */ |

## Powershell

- $bytes = New-Object byte[] 32
  [System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
  $env:JWT_SECRET = [Convert]::ToBase64String($bytes)
- ./kotlin.bat run
- Remove-Item Env:JWT_SECRET

## Bash

- export JWT_SECRET=$(openssl rand -base64 32)
- ./kotlin run
- unset JWT_SECRET
# Restpartijen API

Ktor Web API for the restpartijenplatform (proftaak, AI-powered software development, period 1).
The design is described in `docs/FTD-restpartijen-webapi-v2_4.md`; the architecture decisions are in `docs/adr/`.

## Requirements

- IntelliJ IDEA 2026.2.1 or newer with the Kotlin Toolchain plugin
- No JDK needed: the Kotlin Toolchain downloads it on the first run

## Run

    ./kotlin run

On Windows: `kotlin.bat run`

## Test

    ./kotlin test

Tests live in `server/test`, not in `server/src`.

## Demo accounts

On an empty database the app seeds six accounts, three suppliers and 21 products (`server/src/seed/SeedData.kt`).
All accounts share one password, for the demo only: `Demo-Restpartij-2026`.

| Email | Role |
|---|---|
| `admin@mail.com` | ADMIN |
| `supermarkt@mail.com` | SUPPLIER |
| `bakker@mail.com` | SUPPLIER |
| `groothandel@mail.com` | SUPPLIER |
| `afhaler-a@mail.com` | COLLECTOR |
| `afhaler-b@mail.com` | COLLECTOR |

The dates are relative to the moment of starting. Overdue products and reservations are processed by the
first maintenance run, five minutes after the start.

## Working agreements

- `main` always works. Nobody commits directly to `main`.
- Every change goes through a pull request with at least one review.
- Changes in `shared` need a review from both other team members.

## Configuration

- The application reads two environment variables: `JWT_SECRET`, which is required, and `DB_MODE`, which is optional.
- Without `JWT_SECRET` the application stops at startup with a message (FTD §16.4, decision B-52, issue #5). Setting the variable is not a "manual step" in the sense of GI-2.
- Generate a secret with `openssl rand -base64 32` (Linux/macOS) or the PowerShell lines below, and put it in `.env` (ignored by git) or in the run configuration of IntelliJ. `.env.example` shows the variables.

  | Variable | Required | Values | Default |
  |---|---|---|---|
  | `JWT_SECRET` | yes | any string of at least 32 bytes | none: the application stops |
  | `DB_MODE` | no | `memory` or `file` | `memory` |

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
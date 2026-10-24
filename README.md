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

The application reads three environment variables. If a required one is missing, the application stops at startup and the error message names it (FTD §16.4, decision B-52, issue #5). Setting `JWT_SECRET` is not a "manual step" in the sense of GI-2.

| Variable | Required | Values | Default |
|---|---|---|---|
| `JWT_SECRET` | yes | any string of at least 32 bytes | none: the application stops |
| `OFF_CONTACT_EMAIL` | yes | one e-mail address, sent to Open Food Facts in the User-Agent (US-02) | none: the application stops |
| `DB_MODE` | no | `memory` or `file` | `memory` |

There are two ways to set them:

- **In the terminal** (below). The variables last only as long as that terminal window, and only apps started from that window see them.
- **In the IntelliJ run configuration**, under *Environment variables*. They are kept between runs.

`.env.example` lists the variables. You can copy it to `.env` (ignored by git) to keep your values in one place, but **the application does not read `.env` by itself**: load it into the terminal first, as shown below. If a value in `.env` is empty, the variable counts as not set and the application stops.

Write each value in `.env` without quotes: `JWT_SECRET=abc...`. Bash reads `.env` as a script, so save it with Unix line endings (LF).

To fill in `OFF_CONTACT_EMAIL` in `.env`, enter a real e-mail address after the `=`: `OFF_CONTACT_EMAIL=you@example.com`. Open Food Facts uses it to contact you if your app causes problems.

To fill in `JWT_SECRET`, generate a random secret and copy the line it prints. In `.env`, paste it after `JWT_SECRET=`; in the terminal, use it as shown below.

```powershell
# PowerShell
$bytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
[Convert]::ToBase64String($bytes)
```

```bash
# Bash
openssl rand -base64 32
```

The secret is the key the application uses to sign its tokens (JWTs). It is not a token itself. Keep it out of git, chats and screenshots.

A new `JWT_SECRET` makes all tokens issued before it invalid, so after a restart with a new secret everyone has to log in again.

### PowerShell

Set the variables, with the secret you generated above:

```powershell
$env:JWT_SECRET = "<your secret>"
$env:OFF_CONTACT_EMAIL = "you@example.com"
```
(Use a real email address.)

Or load them from `.env`:

```powershell
Get-Content .env | Where-Object { $_ -match '^\s*[^#\s][^=]*=' } | ForEach-Object {
    $name, $value = $_ -split '=', 2
    Set-Item "Env:$($name.Trim())" $value.Trim()
}
```

Then start the application:

```powershell
./kotlin.bat run
```

To remove the variables before closing the window:

```powershell
Remove-Item Env:JWT_SECRET, Env:OFF_CONTACT_EMAIL
```

### Bash

Set the variables, with the secret you generated above:

```bash
export JWT_SECRET="<your secret>"
export OFF_CONTACT_EMAIL="you@example.com"
```
(Use a real email address.)

Or load them from `.env`:

```bash
set -a; . ./.env; set +a
```

Then start the application:

```bash
./kotlin run
```

To remove the variables before closing the window:

```bash
unset JWT_SECRET OFF_CONTACT_EMAIL
```
---
tags: [presentation, S1, config]
branch: shared/stefan-config
---

# S1 — Configuration is read in: presentation

Sources: [[FTD-restpartijen-webapi-v2_2]] (v2.2), [[FTD-restpartijen-webapi-v2_2-tasks-stefan#Phase 2 — S1 de configuratie wordt uitgelezen (basis T12)|my task list, phase 2]]

> [!warning] Fix before the presentation
> - [ ] `.env.example`: `JWT_SECRET= 32 byte minimum` sets the value to that text → the app stops with "too short" if anyone loads the file. Make it `JWT_SECRET=` (empty) and move the explanation into a comment. Remove the `/* ... */` placeholder lines.
> - [ ] README: fill in the `/* */` cells of the table, add the `DB_MODE` commands and the IntelliJ section, change `v2_1` → `v2_2`.
> - [ ] Dutch work comments (B-27): `// TODO GI-3: kan overschreden worden indien nodig` in `Application.kt` → `// TODO (GI-3): map to JwtSettings and call configureSecurity once GI-3 is on main`
> - [ ] `JwtProperties.kt`: replace `//Keuze A voor nu: ask Paul for input` with `// TODO (§21): option A for now, pending group decision`; remove the commented-out `secretGenerated` lines, the commented-out option C and the unused `SecureRandom` / `Base64` imports.

---

## 1. Context: what S1 is and what was agreed

- S1 = the configuration is read in ([[FTD-restpartijen-webapi-v2_2#GI-2 Applicatie-opzet — uitvoering Stefan|GI-2, decision 2.3]]).
- Secrets never go in the repository ([[FTD-restpartijen-webapi-v2_2#16.4 Secrets|§16.4]]).
- Before coding, I agreed the setup with **Eva** (H2 settings, in the issue) and **Lonneke** (JWT).

**Why first:** everything that follows comes from those agreements.

## 2. `application.yaml`: two new sections

```yaml
database:
  mode: "$DB_MODE:memory"   # memory (default) or file
  file: ./data/restpartijen # only used when mode is file

jwt:
  secret: "$?JWT_SECRET"
  issuer: restpartijen-api
  audience: restpartijen-app
  validityHours: 24
```

- `"$DB_MODE:memory"`: take the environment variable, otherwise `memory`.
- `"$?JWT_SECRET"`: the `?` makes it optional at YAML level; if the variable is missing, the key doesn't exist.

**Why:** in-memory is the default (B-17), and switching to file needs only an environment variable, not a changed file in Git. With `$?`, *our* code decides what happens when the secret is missing, with a clear message.

## 3. The principle of the `config` package

- Config is read in **one place** and turned into **typed Kotlin objects**.
- Other packages receive an object and never read YAML themselves.
- Every value has a **default in code**, because `testApplication` does not load `application.yaml`.

**Why:** key names live in one place, validation happens once at startup, and tests can build the objects directly.

## 4. `DatabaseSettings` + `DatabaseSettingsTest`

- `enum class DbMode { MEMORY, FILE }`, read case-insensitively.
- Empty or missing → `MEMORY`.
- Unknown value → the app stops with a message naming `DB_MODE`.
- 5 tests with `MapApplicationConfig`.

**Why:** a typo should never silently fall back to something else, and tests always run in memory ([[FTD-restpartijen-webapi-v2_2#GI-1 Persistentielaag — uitvoering Eva|GI-1, decision 1.2]]).

## 5. `JwtProperties` + `JwtPropertiesTest`

- Empty counts as missing; missing → stop ("JWT_SECRET is not set"). **Option A, pending the group decision on §21.**
- Minimum **32 bytes** (= 256 bits, what HS256 needs).
- Error messages name the variable, **never the value**.
- `toString()` shows `secret=***`.
- Validity becomes a `Duration`; it must be a whole number of hours above 0.
- 7 tests. Point out the security ones: *error message never contains the secret*, *toString hides the secret*.

**Why the name `JwtProperties`:** Lonneke's `JwtSettings` already exists in `security`. *Properties* = what's in the file; *Settings* = what the mechanism uses. `config` and `security` don't import each other; only `module()` knows both.

## 6. `Application.module()`

```kotlin
val jwtProperties = environment.config.jwtProperties()
log.info("JWT config: $jwtProperties")
// TODO (GI-3): map to JwtSettings and call configureSecurity once GI-3 is on main
DatabaseFactory.init(environment.config.databaseSettings())
```

- JWT is read **before** the database: fail fast.
- Logging is safe because `toString()` hides the secret.

**Why:** a wrong config stops the app at startup, not at the first login request.

## 7. `TestConfig` + change in `ProductRoutesTest`

```kotlin
environment { config = testConfig() }
application { module() }
```

- `testConfig()` in `testsupport` returns a `MapApplicationConfig` with a test secret.
- A `fun`, not a `val`: `MapApplicationConfig` is mutable, so each test gets a fresh copy.

**Why:** with option A, `module()` stops without a secret, and `testApplication` doesn't read the YAML.

> [!important] Take-away for Eva and Lonneke
> Every route test that calls `module()` needs `environment { config = testConfig() }`.

## 8. `.gitignore`, `.env.example`, README

- `.gitignore`: `/data/` (root only, so a package named `data` isn't ignored) and `*.trace.db`.
- `.env.example`: the variables, without values.
- README: how to set them in PowerShell, Git Bash and IntelliJ.
- Note: **nothing loads `.env` automatically.**

## 9. Live demo (2 minutes, PowerShell)

1. No secret → stops with "JWT_SECRET is not set".
2. `$env:JWT_SECRET = "abc"` → stops with "too short"; `abc` appears nowhere.
3. Generated secret → starts; the log shows `secret=***`.
4. `$env:DB_MODE = "file"` → a `.mv.db` file appears; `git status` doesn't show it.

```powershell
$bytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
$env:DB_MODE = "file"
.\kotlin.bat run
```

---

## Decisions and asks for the group

### §21: option A or C?

See [[FTD-restpartijen-webapi-v2_2#21. Open punten|§21]].

| | Option C (random secret per run) | Option A (no fallback, stop) |
|---|---|---|
| `kotlin.bat run` without preparation | starts, with a warning | stops with a message |
| GI-2 (no manual steps) | met | stretched |
| §16.4 (secret not in repo) | met | met |
| Existing integration tests | unchanged | all need `testConfig()` |
| Tokens after a restart | invalid | stay valid |
| Code change to switch | `fallbackSecret()` returns a `String` | `fallbackSecret()` returns `Nothing` |

Switching costs about one function and two tests.

### Merge order with Eva

- Both branches changed `DatabaseFactory.kt`, `Application.kt`, `.gitignore` and `project.yaml`.
- Proposal: S1 first, then Eva merges `main` and keeps her own `DatabaseFactory`.
- Eva can drop her `data/` line in `.gitignore`; `/data/` is in S1.

### With Lonneke

- Who adds the `JwtProperties` → `JwtSettings` mapping in `module()`? Proposal: whoever merges second.
- Could her `JwtSettings` also get a `toString()` that hides the secret?

### Where does the `.mv.db` file land?

- Mention what you saw (project root or `server/`), so the assessor demo instructions are correct ([[FTD-restpartijen-webapi-v2_2#Bijlage B — H2 wat het is en hoe wij het gebruiken|bijlage B]]).

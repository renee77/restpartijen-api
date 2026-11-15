---
tags: [presentation, S2, di, routing]
branch: shared/stefan-di-routing
---

# S2 — DI and routing per feature: code review

Sources: [[FTD-restpartijen-webapi-v2_2]] (v2.2), [[FTD-restpartijen-webapi-v2_2-tasks-stefan#Phase 3 — S2 DI en routing per feature (basis T11)|my task list, phase 3]], previous step: [[S1-presentation-stefan]]

> [!warning] Fix before the review
> - [ ] F2 placeholder: `server/src/reservation/routes/ReservationRoutes.kt` with an empty `Application.configureReservationRouting()`, called in `module()`.
> - [ ] `module()`: `// TODO (GI-3): map jwtProperties to JwtSettings and call configureSecurity once GI-3 is on main` under SECURITY, and `// TODO (F3, Lonneke): configurePricingRouting() once it exists` under ROUTES.
> - [ ] `module()`: remove the two commented-out hand-wiring blocks and the unused imports (Ctrl+Alt+O).
> - [ ] `Dependencies.kt`: remove the `// TODO: imports ...` line and the blank lines in the block.
> - [ ] `.\kotlin.bat test`: all green (the last run was the red-before-green check).
> - [ ] Start the app with a secret: `GET /api/v1/products/1` → `200`.

---

## 1. Context: what S2 is

- One `Application.module()` that calls a routing function per feature ([[FTD-restpartijen-webapi-v2_2#GI-2 Applicatie-opzet — uitvoering Stefan|GI-2, decision 2.2]]).
- Dependencies via Ktor's own DI plugin ([[FTD-restpartijen-webapi-v2_2#ADR-04 — Ktor's eigen dependency-injectionplugin|ADR-04]], decision 2.1).
- Acceptance criteria (GI-2):
  - a new feature is hooked up with **one line** in `module()`;
  - `testApplication` can replace any service with a fake **without changing production code**.

**Branch:** `shared/stefan-di-routing`, branched from S1 (`shared/stefan-config`), because S1 isn't on `main` yet. The S2 PR goes against `main` once S1 is merged.

## 2. Two kinds of "dependency"

| | `module.yaml` → `dependencies:` | Dependency injection |
|---|---|---|
| What | **libraries** (jars) | **objects** a class needs |
| When | build time | run time |
| Example | `io.ktor:ktor-server-di` | `ProductService` needs a `ProductRepository` |

We already injected by hand: `ProductService(ExposedProductRepository())`. The plugin adds what hand-wiring can't: an integration test can swap an object *before* `module()` builds everything.

## 3. `module.yaml`: one new library

```yaml
  - io.ktor:ktor-server-di
```

No version: the Ktor BOM from `settings.ktor` sets it to 3.6.0.

## 4. `plugins/Dependencies.kt`: one place for the wiring

```kotlin
fun Application.configureDependencies() {
    dependencies {
        // F1
        provide<ProductRepository>(::ExposedProductRepository)
        provide(::ProductService)

        // F2 (Stefan), F3 (Lonneke): register their contracts here once they exist
    }
}
```

**Decisions**
- **Key = the contract (interface), recipe = the implementation.** Whoever asks for a `ProductRepository` gets `ExposedProductRepository`, unless a test registered a fake first.
- **Constructor references (`::X`).** The plugin reads the constructor's parameters and resolves them itself: `ProductService` gets the `ProductRepository` from line 1.
- **No key for `ProductService`.** Nobody asks for an interface there; the class itself is the key.
- **Order doesn't matter.** Resolution happens on demand, not top to bottom.
- **One place ([[FTD-restpartijen-webapi-v2_2#12.1 De koppelvlakken|§12.1]]).** Eva and Lonneke add one `provide` line per contract here, e.g. `ProductReader`, `PriceProvider`.

## 5. `ProductRoutes.kt`: the routing function asks DI itself

```kotlin
fun Application.configureProductRouting() {
    val service: ProductService by dependencies
    routing { /* unchanged */ }
}
```

**Decision: option B.**

| | A: `module()` resolves and passes on | B: routing function resolves itself |
|---|---|---|
| Lines per feature in `module()` | 2 + import | **1** |
| Eva's file | unchanged | signature changed |
| Pattern for F2/F3 | — | "ask DI for what you need" |

B meets "one line per feature" literally and gives everyone the same pattern.

> [!important] For Eva
> `configureProductRouting()` no longer takes a parameter; the service comes from DI.

## 6. `Application.module()`: four sections in a fixed order

```kotlin
fun Application.module() {
    // 1. CONFIGURATION
    val jwtProperties = environment.config.jwtProperties()
    log.info("JWT config: $jwtProperties")
    DatabaseFactory.init(environment.config.databaseSettings())

    // 2. PLUGINS
    configureSerialization()
    configureDependencies()

    // 3. SECURITY
    // TODO (GI-3): map jwtProperties to JwtSettings and call configureSecurity once GI-3 is on main

    // 4. ROUTES
    configureProductRouting()        // F1, Eva
    configureReservationRouting()    // F2, Stefan
    // TODO (F3, Lonneke): configurePricingRouting() once it exists
}
```

**Why this order**
1. **Configuration first:** fail fast; no point installing plugins if the secret is missing.
2. **Plugins before security:** `Authentication` is a plugin too.
3. **Security before routing:** `authenticate(PROVIDER_NAME) { ... }` needs the provider to exist ([[FTD-restpartijen-webapi-v2_2#GI-3 Authenticatie en rollen — uitvoering Lonneke|GI-3, decision 3.9]]).
4. **Routing last:** one line per feature.

This function is the table of contents of the app (deel B, point 1).

## 7. `DependencyInjectionTest`: the proof

```kotlin
val fakeProduct = Product(id = 42, name = "Fake product", category = "FRESH")
environment { config = testConfig() }
application {
    dependencies.provide<ProductRepository> { FakeProductRepository(listOf(fakeProduct)) }
    module()
}
val response = client.get("/api/v1/products/42")
assertEquals(HttpStatusCode.OK, response.status)
assertEquals("Fake product", body.name)
```

**Decisions**
- **Id 42 and a name that isn't in the seed data.** The real database only has 1 and 2, so a `200` with "Fake product" can only come from the fake. A test that also passes with the real repository proves nothing.
- **Fake the repository, not the service.** `ProductService` is a final class (no interface), and faking the repository tests the full chain route → service → repository, with only the data source swapped.
- **`provide` before `module()`.** In tests, Ktor ignores the conflicting registration in `configureDependencies()`: first registration wins.
- **A lambda, not `::FakeProductRepository`.** The fake needs a `List<Product>`, which DI can't resolve.
- **Reuses Eva's `FakeProductRepository`**, unchanged.

**Red before green:** without the `provide` line → `expected: <200 OK> but was: <404 Not Found>`. Proves the test depends on the swap.

> [!important] For everyone
> To replace a dependency in a route test: `dependencies.provide<Contract> { fake }` inside `application { }`, **before** `module()`.

## 8. Live demo (2 minutes)

1. `.\kotlin.bat test` → all green; point at `DependencyInjectionTest`.
2. Comment out the `provide` line → that test fails with `404`. Put it back.
3. Start the app with a secret → `GET /api/v1/products/1` returns `200` (DI also works outside the tests).

---

## How to hook up your own feature (for Eva and Lonneke)

1. Register your contracts in `configureDependencies()`: `provide<YourContract>(::YourImplementation)`.
2. In your routing function, ask DI for what you need: `val service: YourService by dependencies`.
3. Add **one** line to `// 4. ROUTES` in `module()`.
4. In your route tests: `environment { config = testConfig() }` and, if you want a fake, `dependencies.provide<...> { ... }` before `module()`.

## Decisions and asks for the group

- **Option B for routing:** OK with everyone that routing functions resolve their own dependencies? (Eva: your signature changed.)
- **Merge order:** S1 first, then S2. Eva's persistence branch also touches `Application.kt`; agree who merges when.
- **Contracts in `shared`:** who registers `ProductReader`, `ProductStatusUpdater`, `PriceProvider` and `ReservationMaintenance` in `configureDependencies()`? Proposal: the implementer, in their own PR.
- **Security slot:** Lonneke's `configureSecurity(JwtConfig(...))` goes under `// 3. SECURITY`, together with the `JwtProperties` → `JwtSettings` mapping (whoever merges second).
- **Open from S1:** §21, option A or C for a missing secret.

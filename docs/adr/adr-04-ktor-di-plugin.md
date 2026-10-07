---
type: ADR
title: "ADR-04 — Ktor's eigen dependency-injectionplugin"
description: "De ingebouwde dependency-injectionplugin van Ktor in plaats van Koin of handmatige constructor injection."
tags: [adr, restpartijen, ktor, dependency-injection]
timestamp: 2026-10-04T00:00:00
---

# ADR-04 — Ktor's eigen dependency-injectionplugin

> Tot en met v2.3 stond dit ADR in §8.6 van het FTD. Het staat nu alleen hier. Verwijzingen naar paragrafen (§) gaan over het FTD ([[FTD-restpartijen-webapi-v2_3]]).

- **Context.** De services moeten in tests door fakes vervangen kunnen worden zonder dat de productiecode verandert.
- **Besluit.** De ingebouwde DI-plugin van Ktor (`io.ktor.server.plugins.di`) wordt gebruikt in plaats van Koin of handmatige constructor injection.
- **Status.** Aangenomen.
- **Gevolgen.** Positief: geen extra library, en `testApplication` kan een dependency rechtstreeks overschrijven met een fake (JetBrains s.r.o., z.j.). Negatief: de plugin is relatief nieuw, waardoor er minder voorbeelden buiten de officiële documentatie zijn.
- **Alternatieven.** Koin werd overwogen en is herbruikbaar in de Android-app van periode 2, maar kost een extra library om te leren en te verdedigen. Handmatige constructor injection blijft overzichtelijk bij dit aantal dependencies, maar maakt testvervanging omslachtiger.

## Bronnen

JetBrains s.r.o. (z.j.). *Testing with dependency injection*. Ktor. Geraadpleegd op 12 september 2026, van https://ktor.io/docs/server-di-testing.html

---
type: ADR
title: "ADR-05 — Ktor-plugins en verantwoordelijkheden"
description: "Welke Ktor-plugins de applicatie installeert, met per plugin de verantwoordelijkheid en de eigenaar."
tags: [adr, restpartijen, ktor, plugins]
timestamp: 2026-10-04T00:00:00
---

# ADR-05 — Ktor-plugins en verantwoordelijkheden

> Tot en met v2.3 stond dit ADR in §8.6 van het FTD. Het staat nu alleen hier. Verwijzingen naar paragrafen (§) gaan over het FTD ([[FTD-restpartijen-webapi-v2_3]]).

- **Context.** De rubric vraagt voor niveau "goed" inzicht in de opbouw van Ktor-modules, routing, plugins en verantwoordelijkheden (r.373).
- **Besluit.** De volgende plugins worden geïnstalleerd, elk met één duidelijke verantwoordelijkheid:

| Plugin | Verantwoordelijkheid | Eigenaar |
|--------|----------------------|----------|
| `Routing` | Endpoints koppelen aan handlers; per feature een eigen routingbestand | Per feature |
| `ContentNegotiation` (kotlinx.serialization) | JSON in- en uitpakken | GI-2, Stefan |
| `Authentication` (JWT) | Token valideren, principal beschikbaar maken | GI-3, Lonneke |
| `StatusPages` | Domeinexcepties omzetten naar HTTP-statuscodes | GI-4, Stefan |
| `RequestValidation` | Structurele validatie van request bodies | GI-4, Stefan |
| `CORS` | Toegang vanaf de Android-app in periode 2 | GI-2, Stefan |
| `CallLogging` | Requests loggen voor demonstratie en foutzoeken | GI-2, Stefan |
| DI (`io.ktor.server.plugins.di`) | Services en repositories registreren en vervangbaar maken | GI-2, Stefan |

- **Status.** Aangenomen.
- **Gevolgen.** De `Application`-module blijft dun: hij installeert plugins en roept per feature een routingfunctie aan. De domeinlogica zit in de servicelaag, niet in de route handlers. Dat is ook de reden dat de servicelaag los te unittesten is.
- **Alternatieven.** Validatie in de route handlers zelf werd afgewezen omdat de logica dan niet los van HTTP te testen is.

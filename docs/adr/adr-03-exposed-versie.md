---
type: ADR
title: "ADR-03 — Exposed 0.x in plaats van 1.0"
description: "Vervallen sinds v1.1. In v1.0 koos de profgroep voor Exposed 0.x; de module schrijft inmiddels Exposed 1.5.0 voor."
tags: [adr, restpartijen, exposed, vervallen]
timestamp: 2026-10-04T00:00:00
---

# ADR-03 — Exposed 0.x in plaats van 1.0

> Tot en met v2.3 stond dit ADR in §8.6 van het FTD. Het staat nu alleen hier. Verwijzingen naar paragrafen (§) gaan over het FTD ([[FTD-restpartijen-webapi-v2_3]]).
>
> Dit besluit geldt niet meer. Het blijft staan, zodat te zien is wat er in v1.0 is besloten en waarom het is vervallen. Context, besluit, gevolgen en alternatieven zijn de tekst uit FTD v1.0.

- **Context.** Exposed 1.0 is in 2026 uitgebracht met een stabiele API, maar verplaatst alle imports naar `org.jetbrains.exposed.v1.*` en vereist Kotlin 2.2 (JetBrains s.r.o., 2026b).
- **Besluit.** De profgroep gebruikt een 0.x-versie met de oude `org.jetbrains.exposed.sql.*`-imports.
- **Status.** Vervallen in v1.1 (W-9). De module schrijft de versies voor: Kotlin 2.4.20 en Exposed 1.5.0 (§3.2). Er valt dus niets meer te kiezen. Er is geen nieuw ADR voor in de plaats gekomen, want een verplichting is geen besluit.
- **Gevolgen.** Positief: het overgrote deel van de beschikbare voorbeelden, tutorials en antwoorden sluit aan op deze versie, wat de leercurve verkort. Negatief: de profgroep werkt bewust niet op de nieuwste versie en moet dat bij het assessment kunnen verantwoorden.
- **Alternatieven.** Exposed 1.0 werd afgewezen omdat de tijd die het kost om afwijkende imports te herleiden niet bijdraagt aan de beoordeelde leeruitkomst.

## Wat er nu geldt

Exposed 1.0 is in januari 2026 uitgebracht met een stabiele API (JetBrains s.r.o., 2026a). Het project gebruikt Exposed 1.5.0. De overstap van 0.x naar 1.x raakt de code op drie plekken: de imports, de indeling in modules en de transacties vanuit coroutines (JetBrains s.r.o., 2026b). Die drie staan als besluiten 1.5 tot en met 1.7 in GI-1 (§13).

Het voordeel uit de gevolgen hierboven is nu een nadeel. Voorbeelden op internet en suggesties van AI-tooling gaan vrijwel altijd uit van Exposed 0.x. Dat is risico R-07 (§17).

## Bronnen

JetBrains s.r.o. (2026a, januari). *Exposed 1.0 is now available*. The JetBrains Blog. https://blog.jetbrains.com/kotlin/2026/01/exposed-1-0-is-now-available/

JetBrains s.r.o. (2026b, 26 augustus). *Migrating from 0.61.0 to 1.0.0*. Exposed Documentation. https://www.jetbrains.com/help/exposed/migration-guide-1-0-0.html

---
type: Index
title: "Architectuurbeslissingen (ADR's) — Restpartijenplatform Web API"
description: "Overzicht van alle architectuurbeslissingen van de Web API, met per ADR de status, de eigenaar en een korte samenvatting."
tags: [adr, restpartijen, index]
timestamp: 2026-10-04T00:00:00
---

# Architectuurbeslissingen

Elk ADR legt één besluit vast: de context, het besluit, de status, de gevolgen en de alternatieven. Tot en met v2.3 stonden de ADR's in §8.6 van het [FTD](../FTD-restpartijen-webapi-v2_3.md). Wat er sinds het begin aan de ADR's is veranderd, staat in het [log](log.md).

| ADR | Titel | Status | Eigenaar | Samenvatting |
|-----|-------|--------|----------|--------------|
| 01 | [Ktor met featuregerichte packages in plaats van laaggerichte](adr-01-featuregerichte-packages.md) | Aangenomen | Allen | De packages volgen de features. Zo werkt elke student in een eigen package. |
| 02 | [Afprijzing als aparte policy-hiërarchie](adr-02-afprijzing-als-policy-hierarchie.md) | Aangenomen | Lonneke | De afprijzing staat in een eigen `DiscountPolicy`-hiërarchie, los van de producthiërarchie. |
| 03 | [Exposed 0.x in plaats van 1.0](adr-03-exposed-versie.md) | Vervallen | Eva | In v1.0 gekozen. Vervallen omdat de module Exposed 1.5.0 voorschrijft. |
| 04 | [Ktor's eigen dependency-injectionplugin](adr-04-ktor-di-plugin.md) | Aangenomen | Stefan | De DI-plugin van Ktor, zodat tests een dependency door een fake kunnen vervangen. |
| 05 | [Ktor-plugins en verantwoordelijkheden](adr-05-ktor-plugins.md) | Aangenomen | Stefan, Lonneke | Welke plugins de applicatie installeert, met per plugin de verantwoordelijkheid en de eigenaar. |
| 06 | [Staffels in plaats van doorlopende afprijscurves](adr-06-staffels-afprijzing.md) | Aangenomen | Stefan, Lonneke | Kortingen in staffels per productsoort. De prijs van het moment van reserveren ligt vast. |
| 07 | [Bedragen als hele centen in een `Long`](adr-07-bedragen-in-centen.md) | Aangenomen | Lonneke, Eva | Bedragen in hele eurocenten en kortingen als heel percentage, zodat `Money` op elk platform werkt. |

De eigenaren komen uit de takenlijst van het team (`samenwerken/FTD-restpartijen-webapi-v2_2-tasks.md`).

## Een nieuw ADR toevoegen

- Neem het volgende vrije nummer. Een nummer wordt niet opnieuw gebruikt.
- Noem het bestand `adr-NN-korte-titel.md` en gebruik de vijf vaste onderdelen.
- Zet het ADR in de tabel hierboven en noteer de wijziging in het [log](log.md).
- Geldt een besluit niet meer? Verwijder het dan niet, maar zet de status op "Vervallen" en schrijf erbij waarom.

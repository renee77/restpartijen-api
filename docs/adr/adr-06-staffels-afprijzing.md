---
type: ADR
title: "ADR-06 — Staffels in plaats van doorlopende afprijscurves"
description: "Kortingen in staffels met vaste grenzen in plaats van doorlopende curves; de prijs van het moment van reserveren ligt vast."
tags: [adr, restpartijen, afprijzing, prijs]
timestamp: 2026-10-04T00:00:00
---

# ADR-06 — Staffels in plaats van doorlopende afprijscurves

> Tot en met v2.3 stond dit ADR in §8.6 van het FTD. Het staat nu alleen hier. Verwijzingen naar paragrafen (§) gaan over het FTD ([[FTD-restpartijen-webapi-v2_3]]).

- **Context.** In v1.0 zakte de prijs doorlopend: elke minuut een fractie lager, volgens een formule per productsoort. Bij het nalopen bleken daar twee problemen aan te zitten. Een prijs is op geen enkel moment uit te leggen zonder de formule erbij te pakken. En het is onduidelijk welk moment telt: wie dagen vooruit reserveert voor een afhaalmoment vlak voor de datum, zou de hoogste korting kunnen opeisen.
- **Besluit.** De korting verloopt in staffels met vaste grenzen per productsoort (§9.4). De prijs die geldt op het moment van reserveren wordt vastgelegd in de reservering.
- **Status.** Aangenomen. De profgroep heeft op 19 september 2026 bevestigd dat de prijs van het moment van reserveren geldt.
- **Gevolgen.** Positief: een prijs is in één zin uit te leggen ("vanaf 24 uur voor de datum 40% korting"). Elke grens is een scherpe testwaarde. Vooruit reserveren levert geen extra korting op, en wie op de volgende staffel wacht, loopt het risico dat een ander eerder reserveert. Negatief: de drie policies hebben nu dezelfde vorm en verschillen alleen in grenzen en percentages. Het polymorfisme blijft aantoonbaar, want `PricingService` roept `discountPercentage()` aan zonder te weten welke policy hij in handen heeft. Het verschil per klasse is wel kleiner dan bij drie formules. Dat raakt R-05.
- **Alternatieven.** De doorlopende curves uit v1.0 werden afgewezen om de twee problemen hierboven. Eén generieke staffelklasse met drie configuraties is minder code, maar haalt de interface met drie implementaties weg die de casus juist moet tonen (r.78-79, r.165); ook afgewezen.

---
type: ADR
title: "ADR-07 — Bedragen als hele centen in een `Long`"
description: "Bedragen als hele eurocenten in een Long en kortingen als heel percentage, met het oog op multiplatform."
tags: [adr, restpartijen, bedragen, multiplatform]
timestamp: 2026-10-04T00:00:00
---

# ADR-07 — Bedragen als hele centen in een `Long`

> Tot en met v2.3 stond dit ADR in §8.6 van het FTD. Het staat nu alleen hier. Verwijzingen naar paragrafen (§) gaan over het FTD ([[FTD-restpartijen-webapi-v2_3]]).

- **Context.** Tot v2.1 rekende het prijsmodel met `BigDecimal`. Dat type zat in `Money` en `PriceBreakdown`, en die staan in `shared`. Juist die types komen in aanmerking om in periode 3 te delen met een multiplatform-app. De docent adviseert daarom kotlinx-libraries boven Java-libraries (§3.2). Voor decimale getallen vond de profgroep geen kotlinx-variant. `BigDecimal` bestaat alleen op de JVM, en de multiplatform-libraries die er wel zijn, staan nog op versie 0.x. Die bevinding is aan de docent voorgelegd, met drie richtingen: `BigDecimal` op de server houden, hele centen in een `Long`, of een externe library.
- **Besluit.** Een bedrag is een aantal hele eurocenten in een `Long`, verpakt in het waardetype `Money`. Een korting is een heel percentage (`Int`). De docent heeft dit besloten (P. de Mast, persoonlijke communicatie, 20 september 2026). Twee regels volgen eruit. De korting wordt afgerond door de gehele deling, dus naar beneden (§9.4). En de API stuurt en ontvangt euro's; de omrekening naar centen rondt af naar de dichtstbijzijnde cent (§10.1).
- **Status.** Aangenomen.
- **Gevolgen.** Positief: `Long` en `Int` bestaan op elk platform, dus `Money` en `PriceBreakdown` zijn zonder aanpassing te delen. Rekenen met hele getallen is exact; er ontstaan geen afrondingsfouten zoals bij `Double`. De precisie is ruim voldoende: een `Long` reikt tot ruim 92 biljard euro. De staffels in §9.4 zijn al hele percentages, dus er gaat niets verloren. Negatief: afronden gebeurt niet meer vanzelf goed, maar moet een expliciete regel zijn. En op de grens van de API moet een bedrag worden omgerekend van euro's naar centen en terug.
- **Alternatieven.** `BigDecimal` op de server houden, en alleen het bedrag platformneutraal over de lijn sturen. Afgewezen, omdat `Money` dan niet te delen is. Een externe multiplatform-library voor decimalen. Afgewezen, omdat een library op versie 0.x een extra risico is om te leren en te verdedigen, voor een precisie die we niet nodig hebben.

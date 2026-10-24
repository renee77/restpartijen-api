---
type: ADR
title: "ADR-01 — Ktor met featuregerichte packages in plaats van laaggerichte"
description: "De packagestructuur volgt de features in plaats van de lagen, zodat elke student in een eigen package werkt."
tags: [adr, restpartijen, ktor, architectuur]
timestamp: 2026-10-24T00:00:00
---

# ADR-01 — Ktor met featuregerichte packages in plaats van laaggerichte

> Tot en met v2.3 stond dit ADR in §8.6 van het FTD. Het staat nu alleen hier. Verwijzingen naar paragrafen (§) gaan over het FTD ([[FTD-restpartijen-webapi-v3_0]]).

- **Context.** Drie studenten werken in één repository aan één applicatie. Elke student moet bij het assessment eigen code kunnen aanwijzen (r.367), en merge-conflicten moeten beperkt blijven.
- **Besluit.** De packagestructuur volgt de features (`product`, `reservation`, `pricing`, `security`), niet de lagen (`controllers`, `services`, `repositories`). Binnen elke featurepackage zit wel de laagindeling. Wat de features delen, staat in eigen packages: `shared` (contracten tussen de features), `plugins`, `config`, `persistence` en `seed`.
- **Status.** Aangenomen.
- **Gevolgen.** Positief: elke student werkt vrijwel uitsluitend in de eigen package; eigenaarschap is zichtbaar in de mapstructuur. Negatief: de laagindeling is vier keer herhaald, wat bij een grotere applicatie tot duplicatie zou leiden.
- **Alternatieven.** Laaggerichte packages werden afgewezen omdat alle drie de studenten dan in dezelfde mappen zouden schrijven, met merge-conflicten en onduidelijk eigenaarschap als gevolg.

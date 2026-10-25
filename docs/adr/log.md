---
type: Log
title: "Log architectuurbeslissingen — Restpartijenplatform Web API"
description: "Wijzigingen aan de ADR's, de nieuwste bovenaan. De versies voor oktober 2026 zijn afgeleid uit de revisiehistorie van het FTD."
tags: [adr, restpartijen, log]
timestamp: 2026-10-24T00:00:00
---

# Log architectuurbeslissingen

De nieuwste wijziging staat bovenaan. Het overzicht van alle ADR's staat in de [index](index.md).

## 2026-10-24 — ADR's naast de code en FTD v3.0 gelegd

- Alle ADR's en de index verwijzen nu naar FTD v3.0 in plaats van v2.4. De §-nummers zijn gelijk gebleven.
- ADR-01: `security` staat bij de featurepackages, en de gedeelde packages zijn genoemd. De laagindeling geldt voor zover een feature de laag nodig heeft: `pricing` heeft geen `repository`.
- ADR-02: `shelfLifeRemaining(clock)` met de `Clock` als parameter, zoals in de code.
- ADR-05: de reden voor `CORS` is de demo-frontend in de browser. De Android-app heeft CORS niet nodig, want alleen browsers handhaven het. Er is een bronnenlijst bij gekomen. De tekst bij Gevolgen is opgeschoond. Het besluit zelf is niet veranderd.
- ADR-06: het polymorfisme zit in `tiers()`. `discountPercentage(remaining)` staat één keer in de interface.
- ADR-07: `shared` is een package in de module `server`, geen aparte module.

## 2026-10-08 — ADR-05 bijgewerkt

- De tabel met plugins in ADR-05 bevat nu ook `RoleCheck`: de eigen, route-scoped plugin uit GI-3 (Lonneke) die na de authenticatie de rol controleert. De plugin staat sinds 1 oktober 2026 in de code (`security/service/Rolecheck.kt`).

## 2026-10-04 — Hernummering doorgevoerd

- Alle verwijzingen naar ADR-08 zijn ADR-07 geworden: in de revisiehistorie van het FTD, in de takenlijst in `samenwerken/` en in de ADR-map. Het volgende nieuwe ADR krijgt nummer 08.

## 2026-10-04 — ADR's naar een eigen map

- De ADR's staan niet meer in §8.6 van het FTD v2.3, maar in `docs/adr/`, met één bestand per ADR. In het FTD staat onder hoofdstuk 8 een verwijzing naar deze map. §8.7 Kwaliteitsscenario's heet daardoor nu §8.6.
- ADR-08 (bedragen in centen) heet nu ADR-07. Het nummer 7 was vrij: het eerdere ADR-07 was een concept dat nooit in een uitgebrachte versie heeft gestaan (zie 2026-09-18).
- ADR-03 heeft weer de oorspronkelijke tekst uit FTD v1.0, met de status "Vervallen" en een kopje "Wat er nu geldt".
- ADR-03 en ADR-04 hebben een eigen bronnenlijst. De bron "Exposed 1.0 is now available" staat alleen nog bij ADR-03.
- Index en log toegevoegd.

## 2026-09-29 — FTD v2.2

- Nieuw: ADR-07 (toen genummerd als ADR-08), bedragen als hele centen in een `Long`. Besloten door de docent op 20 september 2026.

## 2026-09-18 — FTD v1.1

- Nieuw: ADR-06, staffels in plaats van doorlopende afprijscurves (W-1).
- ADR-03 vervallen: de module schrijft de versies voor (W-9).
- Een concept-ADR-07 is geschrapt voordat v1.1 af was (W-13). De inhoud is niet bewaard. De reden was: "de toolchain is een verplichting en geen besluit".

## 2026-09-12 — FTD v1.0

- Eerste versie met ADR-01 tot en met ADR-05.

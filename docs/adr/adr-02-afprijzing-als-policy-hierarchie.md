---
type: ADR
title: "ADR-02 — Afprijzing als aparte policy-hiërarchie"
description: "De afprijzing staat in een aparte DiscountPolicy-hiërarchie, los van de producthiërarchie."
tags: [adr, restpartijen, afprijzing, oo]
timestamp: 2026-10-04T00:00:00
---

# ADR-02 — Afprijzing als aparte policy-hiërarchie

> Tot en met v2.3 stond dit ADR in §8.6 van het FTD. Het staat nu alleen hier. Verwijzingen naar paragrafen (§) gaan over het FTD ([[FTD-restpartijen-webapi-v2_3]]).

- **Context.** De afprijzing verschilt per productsoort. In de eerste opzet van de casus was dat een method in de productklassen zelf. Dat zou betekenen dat de eigenaar van de producthiërarchie en de eigenaar van de afprijzing in dezelfde bestanden schrijven.
- **Besluit.** `SurplusProduct` houdt `shelfLifeRemaining()` — dat is een eigenschap van het product. De afprijzing verhuist naar een aparte interface `DiscountPolicy` met drie implementaties, met een eigen eigenaar.
- **Status.** Aangenomen.
- **Gevolgen.** Positief: twee polymorfe hiërarchieën in plaats van één, elk met een eigenaar die hem zelfstandig kan verdedigen; geen gedeelde bestanden; de afprijsregels zijn los te testen zonder een product op te bouwen. Negatief: er is een koppelvlak nodig dat een productsoort aan een policy koppelt (`DiscountPolicyResolver`), en het model is iets indirecter dan één klasse met een method.
- **Alternatieven.** Alles in de productklassen werd afgewezen op eigenaarschap en merge-risico. De hele producthiërarchie bij één student werd afgewezen omdat de andere student dan geen eigen klassenhiërarchie zou hebben om OO-abstractie mee aan te tonen.

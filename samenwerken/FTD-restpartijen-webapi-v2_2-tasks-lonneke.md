---
type: takenlijst
title: "Takenlijst restpartijen Web API — Lonneke (F3)"
description: "Lonnekes eigen to-do's bij FTD v2.2: Money en de omrekening, GI-3 authenticatie en rollen, US-07, US-08, US-09 met het beheeroverzicht, en de testdekking."
tags: [takenlijst, proftaak, ktor, kotlin, lu1, f3]
timestamp: 2026-09-29T00:00:00
---

# Takenlijst restpartijen Web API — Lonneke (F3)

Status, DoD en gedeelde to-do's staan in de [[FTD-restpartijen-webapi-v2_2-tasks|algemene lijst]]. Hier staan alleen jouw eigen to-do's, in de volgorde waarin je ze doet. Elk item linkt naar het FTD; een item `Start na:` is een overdracht van een ander.

## Fase 0 — Voorbereiding en verificatie

Status en DoD: [[FTD-restpartijen-webapi-v2_2-tasks#Fase 0 — Voorbereiding en verificatie|algemene lijst]]

### Voordat je begint
- [ ] Controleer dat IntelliJ IDEA 2026.2.1 of nieuwer is, met de plugin Kotlin Toolchain (Help → About; Settings → Plugins) ([[FTD-restpartijen-webapi-v2_2#3.2 Randvoorwaarden|§3.2]])
- [ ] Draai de testen op de branch van de startsessie ([[FTD-restpartijen-webapi-v2_2#GI-2 Applicatie-opzet — uitvoering Stefan|GI-2]])
      ```bash
      # Get the walking-skeleton branch and run all tests with the Kotlin Toolchain
      git fetch origin && git switch feature/walking-skeleton && ./kotlin test
      ```
      Verwacht: alle testen groen.
      Als het misgaat: de eerste run downloadt de toolchain en de JDK. Faalt die download, controleer dan je netwerk en probeer het opnieuw.
- [ ] Lees bijlage A van het FTD: werken met contracten ([[FTD-restpartijen-webapi-v2_2#Bijlage A — Werken met contracten|bijlage A]])
- [ ] Lees bijlage B van het FTD: H2 ([[FTD-restpartijen-webapi-v2_2#Bijlage B — H2 wat het is en hoe wij het gebruiken|bijlage B]])
- [ ] Lees de opmerking v2.2 onder §11.3: vier voorstellen voor F3, de keuze is aan jou ([[FTD-restpartijen-webapi-v2_2#11.3 Lonneke van Oers — F3 Prijs, houdbaarheid en statusbewaking|§11.3]])

## Fase 1 — GI-5 en GI-6 Gedeelde domeinkern en testopzet op main

Status en DoD: [[FTD-restpartijen-webapi-v2_2-tasks#Fase 1 — GI-5 en GI-6 Gedeelde domeinkern en testopzet op main|algemene lijst]]

### Voordat je begint
- [ ] Start na: [[FTD-restpartijen-webapi-v2_2-tasks-eva#Fase 1 — GI-5 en GI-6 Gedeelde domeinkern en testopzet op main|Eva — de walking skeleton staat op `main`]]
- [ ] Maak een branch vanaf de nieuwste `main` ([[FTD-restpartijen-webapi-v2_2#12.3 Git-werkwijze|B-28]])
      ```bash
      # Branch for the changes to Money and PriceBreakdown in shared
      git fetch origin && git switch -c shared/lonneke-money origin/main
      ```

### To-do
- [ ] Haal het Nederlandse commentaar in `src/shared/PriceBreakdown.kt` weg of vervang het door Engelse KDoc ([[FTD-restpartijen-webapi-v2_2#12.3 Git-werkwijze|B-27]])
- [ ] Besluit of `Money` een privé constructor krijgt, met een factory voor centen en een voor euro's ([[FTD-restpartijen-webapi-v2_2#11.3 Lonneke van Oers — F3 Prijs, houdbaarheid en statusbewaking|§11.3]])
- [ ] Schrijf de omrekening van euro's naar centen op één plek, bij voorkeur in `src/shared/Money.kt`: vermenigvuldig met 100 en rond af naar de dichtstbijzijnde cent met `roundToLong()`, nooit afkappen ([[FTD-restpartijen-webapi-v2_2#10.1 Endpoints|B-25]])
- [ ] Schrijf op dezelfde plek de omrekening van centen naar euro's voor de antwoorden ([[FTD-restpartijen-webapi-v2_2#10.1 Endpoints|B-25]])
- [ ] Schrijf eerst de test `0.29 → 29` en draai hem met afkappen (`toLong()`); zie hem falen met 28 ([[FTD-restpartijen-webapi-v2_2#10.1 Endpoints|B-25]])
      ```bash
      # Run all tests; expect the new conversion test to fail while truncating
      ./kotlin test
      ```
- [ ] Vervang afkappen door afronden en draai opnieuw; voeg de testen `3.49 → 349` en `349 → 3.49` toe ([[FTD-restpartijen-webapi-v2_2#10.1 Endpoints|B-25]])
- [ ] Besluit of `PriceBreakdown` zichzelf bewaakt: percentage tussen 0 en 100, en actuele prijs niet hoger dan de oorspronkelijke ([[FTD-restpartijen-webapi-v2_2#11.3 Lonneke van Oers — F3 Prijs, houdbaarheid en statusbewaking|§11.3]])
- [ ] Voeg die controles toe met `require` in een `init`-blok, zoals `PickupWindow` doet, en schrijf per regel een test ([[FTD-restpartijen-webapi-v2_2#11.3 Lonneke van Oers — F3 Prijs, houdbaarheid en statusbewaking|§11.3]])
- [ ] Open de PR naar `main` en vraag Eva en Stefan als reviewer; een wijziging in `shared` vraagt twee reviews ([[FTD-restpartijen-webapi-v2_2#GI-5 Gedeelde domeinkern — geen eigenaar, PR met review door alle drie|GI-5]])

## Fase 5 — GI-3 Authenticatie en rollen

Status en DoD: [[FTD-restpartijen-webapi-v2_2-tasks#Fase 5 — GI-3 Authenticatie en rollen|algemene lijst]]

### Voordat je begint
- [ ] Start na: [[FTD-restpartijen-webapi-v2_2-tasks-eva#Fase 3 — GI-1 Persistentielaag|Eva — `Repository<T]]` en het aanmelden van tabellen staan op `main`>
- [ ] Start na: [[FTD-restpartijen-webapi-v2_2-tasks-stefan#Fase 4 — GI-2 Applicatie-opzet|Stefan — DI en het inlezen van het secret staan op `main`]]
- [ ] Maak een branch vanaf de nieuwste `main` ([[FTD-restpartijen-webapi-v2_2#12.3 Git-werkwijze|B-28]])
      ```bash
      # Branch for GI-3, as in the example of B-28
      git fetch origin && git switch -c shared/lonneke-security origin/main
      ```

### To-do
- [ ] Besluit welke BCrypt-library je gebruikt en noteer de reden in de PR (aanvulling, [[FTD-restpartijen-webapi-v2_2-tasks#A6 Library voor BCrypt|A6]])
- [ ] Voeg `io.ktor:ktor-server-auth`, `io.ktor:ktor-server-auth-jwt` en de BCrypt-library toe aan `module.yaml` ([[FTD-restpartijen-webapi-v2_2#GI-3 Authenticatie en rollen — uitvoering Lonneke|GI-3 besluit 3.1]])
- [ ] Maak `src/security/UsersTable.kt` met de kolommen van `USERS` uit het ERD, en meld hem aan in `src/Application.kt` ([[FTD-restpartijen-webapi-v2_2#9.1 ERD|§9.1]])
- [ ] Maak `src/security/User.kt` en `UserRepository`, die `Repository<User>` implementeert. Dit is je voorbeeld van generics ([[FTD-restpartijen-webapi-v2_2#11.3 Lonneke van Oers — F3 Prijs, houdbaarheid en statusbewaking|§11.3]])
- [ ] Maak een hasher die wachtwoorden met BCrypt hasht en controleert ([[FTD-restpartijen-webapi-v2_2#GI-3 Authenticatie en rollen — uitvoering Lonneke|GI-3 besluit 3.4]])
- [ ] Maak `src/security/JwtConfig.kt`: bouwt en controleert tokens, met het secret uit de configuratie van Stefan, één claim `role` met de enumnaam en 24 uur geldigheid. Neem de tijd van een meegegeven `Clock` ([[FTD-restpartijen-webapi-v2_2#GI-3 Authenticatie en rollen — uitvoering Lonneke|GI-3 besluit 3.5]])
- [ ] Maak in `src/security/` een function `configureSecurity()` die `install(Authentication)` met JWT doet; gebruik de nieuwe typed authentication niet ([[FTD-restpartijen-webapi-v2_2#GI-3 Authenticatie en rollen — uitvoering Lonneke|GI-3 besluit 3.1]])
- [ ] Roep `configureSecurity()` één keer aan in `Application.module()`, na de plugins en vóór de routing ([[FTD-restpartijen-webapi-v2_2#GI-3 Authenticatie en rollen — uitvoering Lonneke|GI-3 besluit 3.9]])
- [ ] Maak de kern van de rolcheck: een hulpfunction waarmee een feature in de eigen routing zegt welke rol een endpoint vraagt. Zet er geen regels per endpoint in ([[FTD-restpartijen-webapi-v2_2#GI-3 Authenticatie en rollen — uitvoering Lonneke|GI-3 besluit 3.6]])
- [ ] Maak een hulpfunction die uit de principal de gebruiker en de rol haalt, voor de services van de features ([[FTD-restpartijen-webapi-v2_2#16.2 Autorisatiemodel|§16.2]])
- [ ] Besluit de validatieregels voor registreren: e-mailadres, wachtwoord en weergavenaam (aanvulling, [[FTD-restpartijen-webapi-v2_2-tasks#A11 Validatieregels bij registreren|A11]])
- [ ] Voeg `POST /api/v1/auth/register` toe: alleen e-mailadres, wachtwoord en weergavenaam; de rol is altijd `COLLECTOR`, ook als het verzoek een rol meestuurt ([[FTD-restpartijen-webapi-v2_2#GI-3 Authenticatie en rollen — uitvoering Lonneke|GI-3 besluit 3.7]])
- [ ] Gooi bij een bestaand e-mailadres de exceptie uit het besluit over A7 (aanvulling, [[FTD-restpartijen-webapi-v2_2-tasks#A7 Exceptie voor 409 bij een bestaand e-mailadres|A7]])
- [ ] Voeg `POST /api/v1/auth/login` toe: controleer de hash; een verkeerd wachtwoord geeft `UnauthorizedException`; het antwoord bevat het token en de rol ([[FTD-restpartijen-webapi-v2_2#GI-3 Authenticatie en rollen — uitvoering Lonneke|GI-3 besluit 3.8]])
- [ ] Laat het antwoord van register en login nooit het wachtwoord of de hash bevatten ([[FTD-restpartijen-webapi-v2_2#GI-3 Authenticatie en rollen — uitvoering Lonneke|GI-3]])
- [ ] Schrijf in `test/security/` een testroute die de rol `ADMIN` vraagt, en de testen: zonder token `401`, verkeerde rol `403`, juiste rol `200` ([[FTD-restpartijen-webapi-v2_2#GI-3 Authenticatie en rollen — uitvoering Lonneke|GI-3]])
- [ ] Schrijf de testen voor registreren: `201` met rol `COLLECTOR` ook met `"role":"ADMIN"` in de body, `409` bij een bestaand e-mailadres, de regels uit A11 ([[FTD-restpartijen-webapi-v2_2#11.3 Lonneke van Oers — F3 Prijs, houdbaarheid en statusbewaking|§11.3]])
- [ ] Schrijf de testen voor inloggen: token en rol in het antwoord, `401` bij een verkeerd wachtwoord, geen `password` of hash in de body ([[FTD-restpartijen-webapi-v2_2#GI-3 Authenticatie en rollen — uitvoering Lonneke|GI-3]])
- [ ] Schrijf de test die de claim `role` uitleest, en de test die met een vaste klok controleert dat `exp` min `iat` 24 uur is ([[FTD-restpartijen-webapi-v2_2#GI-3 Authenticatie en rollen — uitvoering Lonneke|GI-3]])
- [ ] Schrijf de test die de rij in `users` leest: `password_hash` begint met `$2` ([[FTD-restpartijen-webapi-v2_2#16.1 Authenticatie en sessiebeheer|§16.1]])
- [ ] Controleer dat de log van een inlogverzoek het wachtwoord niet bevat ([[FTD-restpartijen-webapi-v2_2#16.7 Logging|§16.7]])
- [ ] Controleer een verse clone in een lege map: `./kotlin run` start volgens het besluit uit fase 4, en `git grep -n "secret" -- resources/` toont alleen de placeholder ([[FTD-restpartijen-webapi-v2_2#GI-2 Applicatie-opzet — uitvoering Stefan|GI-2]])
- [ ] Stel de indeling van de `/auth`-endpoints binnen `security` voor in de PR, en zet het punt in §21 van het FTD op afgehandeld na de review ([[FTD-restpartijen-webapi-v2_2#GI-3 Authenticatie en rollen — uitvoering Lonneke|GI-3 besluit 3.9]])
- [ ] Open de PR naar `main` en vraag Eva en Stefan als reviewer ([[FTD-restpartijen-webapi-v2_2#GI-5 Gedeelde domeinkern — geen eigenaar, PR met review door alle drie|GI-5]])
- [ ] Laat Eva weten dat `UsersTable` en de hasher op `main` staan ([[FTD-restpartijen-webapi-v2_2#Bijlage A — Werken met contracten|bijlage A]])

## Fase 7 — K-1 en K-6 Contracten van F1 geleverd

Status en DoD: [[FTD-restpartijen-webapi-v2_2-tasks#Fase 7 — K-1 en K-6 Contracten van F1 geleverd|algemene lijst]]

### To-do
- [ ] Spreek met Eva af hoe `/auth/login` het `supplierId` krijgt zonder dat `security` F1 importeert, bijvoorbeeld via een klein contract in `shared` (aanvulling, [[FTD-restpartijen-webapi-v2_2-tasks#A15 Het supplierId bij inloggen|A15]])
- [ ] Voeg het `supplierId` toe aan het antwoord van `/auth/login` voor een aanbieder ([[FTD-restpartijen-webapi-v2_2#GI-3 Authenticatie en rollen — uitvoering Lonneke|GI-3 besluit 3.8]])
- [ ] Schrijf de test: inloggen als aanbieder geeft het `supplierId`, inloggen als afhaler niet ([[FTD-restpartijen-webapi-v2_2#GI-3 Authenticatie en rollen — uitvoering Lonneke|GI-3]])

## Fase 16 — US-07 Actuele prijs zien

Status en DoD: [[FTD-restpartijen-webapi-v2_2-tasks#Fase 16 — US-07 Actuele prijs zien|algemene lijst]]

### Voordat je begint
- [ ] Maak je featurebranch vanaf de nieuwste `main` ([[FTD-restpartijen-webapi-v2_2#12.3 Git-werkwijze|§12.3]])
      ```bash
      # One long-lived branch for F3; merge main into it daily
      git fetch origin && git switch -c feature/f3-prijzen origin/main
      ```

### To-do
- [ ] Maak `src/pricing/model/DiscountTier.kt`: een data class met `appliesFrom: Duration` en `percentage: Int` ([[FTD-restpartijen-webapi-v2_2#8.4 Klassendiagram|§8.4]])
- [ ] Maak `src/pricing/model/DiscountPolicy.kt`: een interface met `appliesTo()` en `tiers()`, en met `discountPercentage(remaining)` en `maxDiscount()` als default implementatie ([[FTD-restpartijen-webapi-v2_2#8.4 Klassendiagram|§8.4]])
- [ ] Schrijf de extension function `List<DiscountTier>.tierFor(Duration)`, die de staffel voor een resterende houdbaarheid vindt; op de grens geldt de hogere korting ([[FTD-restpartijen-webapi-v2_2#11.3 Lonneke van Oers — F3 Prijs, houdbaarheid en statusbewaking|§11.3]])
- [ ] Maak `FreshDiscountPolicy`, `FrozenDiscountPolicy` en `AmbientDiscountPolicy` met de staffels uit §9.4 ([[FTD-restpartijen-webapi-v2_2#9.4 Afprijsstaffels per productsoort|§9.4]])
- [ ] Maak `DiscountPolicyResolver` als `object`, met de map van categorie naar policy als `by lazy` ([[FTD-restpartijen-webapi-v2_2#11.3 Lonneke van Oers — F3 Prijs, houdbaarheid en statusbewaking|§11.3]])
- [ ] Maak `src/pricing/service/PricingService.kt`: implementeert `PriceProvider`, krijgt een `Clock` in de constructor en ziet een partij alleen als `PricedProduct` ([[FTD-restpartijen-webapi-v2_2#SD-3 — Productdetail met actuele prijs, polymorfe afprijzing (US-07)|SD-3]])
- [ ] Reken de prijs uit als `originalPrice × (100 − korting) / 100` in hele centen: eerst vermenigvuldigen, dan delen ([[FTD-restpartijen-webapi-v2_2#9.4 Afprijsstaffels per productsoort|B-24]])
- [ ] Geef `null` terug als de resterende houdbaarheid nul of negatief is ([[FTD-restpartijen-webapi-v2_2#Besluiten van de startsessie en daarna (v2.2)|B-18]])
- [ ] Schrijf eerst de test "349 cent met 60% korting geeft 139 cent" en draai hem met de verkeerde volgorde `(100 − korting) / 100`; zie hem falen. Zet daarna de volgorde goed ([[FTD-restpartijen-webapi-v2_2#9.4 Afprijsstaffels per productsoort|B-24]])
- [ ] Schrijf de unittests voor alle zeven staffelgrenzen, telkens precies op de grens en één seconde erboven (TC-07) ([[FTD-restpartijen-webapi-v2_2#11.3 Lonneke van Oers — F3 Prijs, houdbaarheid en statusbewaking|§11.3]])
- [ ] Schrijf de unittests: percentage nooit hoger dan de hoogste staffel, prijs nooit negatief, nooit hoger dan de oorspronkelijke, verlopen geeft `null` ([[FTD-restpartijen-webapi-v2_2#11.3 Lonneke van Oers — F3 Prijs, houdbaarheid en statusbewaking|§11.3]])
- [ ] Schrijf de unittest: twee aanroepen met dezelfde vaste klok geven dezelfde prijs ([[FTD-restpartijen-webapi-v2_2#14. Niet-functionele eisen (NFR)|NFR-06]])
- [ ] Registreer `PricingService` in `src/Application.kt` als `PriceProvider` ([[FTD-restpartijen-webapi-v2_2#12.1 De koppelvlakken|§12.1]])
- [ ] Laat Eva en Stefan weten dat `PricingService` op `main` staat ([[FTD-restpartijen-webapi-v2_2#Bijlage A — Werken met contracten|bijlage A]])
- [ ] Start na: [[FTD-restpartijen-webapi-v2_2-tasks-eva#Fase 16 — US-07 Actuele prijs zien|Eva — het productdetail met prijs staat op `main`]]
- [ ] Schrijf de integratietest van TC-07 tegen `GET /api/v1/products/{id}`: de drie prijsvelden in euro's, en een verlopen partij geeft `200` met `EXPIRED` en zonder prijs ([[FTD-restpartijen-webapi-v2_2#6. Traceability matrix|§6]])
- [ ] Noteer het gebruik van AI-tooling voor US-07 in het AI-logboek ([[FTD-restpartijen-webapi-v2_2#7. Definition of Done|§7]])
- [ ] Open de PR en vink het sjabloon af ([[FTD-restpartijen-webapi-v2_2#7. Definition of Done|§7]])

## Fase 17 — US-08 Automatische statusbewaking

Status en DoD: [[FTD-restpartijen-webapi-v2_2-tasks#Fase 17 — US-08 Automatische statusbewaking|algemene lijst]]

### To-do
- [ ] Maak `src/pricing/service/ExpiryScheduler.kt` met `ReservationMaintenance`, `ProductReader`, `ProductStatusUpdater` en `Clock` in de constructor ([[FTD-restpartijen-webapi-v2_2#SD-4 — Automatische statusbewaking (US-08)|SD-4]])
- [ ] Schrijf `runMaintenance()`: stap 1 `lapseOverdue(clock.now())`, stap 2 `findExpiredListings(clock.now())` en `markExpired`. Geef een rapport terug met de aantallen per overgang ([[FTD-restpartijen-webapi-v2_2#SD-4 — Automatische statusbewaking (US-08)|SD-4]])
- [ ] Log het rapport met `also` ([[FTD-restpartijen-webapi-v2_2#11.3 Lonneke van Oers — F3 Prijs, houdbaarheid en statusbewaking|§11.3]])
- [ ] Voeg `POST /api/v1/admin/maintenance/expire` toe in `src/pricing/routes/` met de rolregel `ADMIN` ([[FTD-restpartijen-webapi-v2_2#10.1 Endpoints|§10.1]])
- [ ] Besluit het interval van de periodieke statusbewaking (aanvulling, [[FTD-restpartijen-webapi-v2_2-tasks#A8 Interval van de periodieke statusbewaking|A8]])
- [ ] Start de periodieke statusbewaking met `launch` bij het opstarten van de applicatie, met dat interval (aanvulling, [[FTD-restpartijen-webapi-v2_2-tasks#A8 Interval van de periodieke statusbewaking|A8]])
- [ ] Schrijf de unittests met fakes: een verlopen partij wordt `EXPIRED` (TC-08), een opgehaalde partij blijft ongewijzigd, een tweede run meldt nul ([[FTD-restpartijen-webapi-v2_2#11.3 Lonneke van Oers — F3 Prijs, houdbaarheid en statusbewaking|§11.3]])
- [ ] Start na: [[FTD-restpartijen-webapi-v2_2-tasks-stefan#Fase 17 — US-08 Automatische statusbewaking|Stefan — `lapseOverdue` staat op `main`]]
- [ ] Schrijf de integratietest op H2: een gereserveerde partij die ook over de datum is, eindigt op `EXPIRED` en niet op `LISTED` ([[FTD-restpartijen-webapi-v2_2#11.3 Lonneke van Oers — F3 Prijs, houdbaarheid en statusbewaking|§11.3]])
- [ ] Schrijf de integratietest: een niet-opgehaalde reservering vervalt en de partij is weer `LISTED` ([[FTD-restpartijen-webapi-v2_2#5.8 US-08 — Automatische statusbewaking|§5.8]])
- [ ] Schrijf de rolchecks: `401` zonder token, `403` met een andere rol dan `ADMIN` ([[FTD-restpartijen-webapi-v2_2#GI-6 Testopzet — uitvoering gezamenlijk in de startsessie|GI-6]])
- [ ] Voeg een voorbeeld toe aan `requests.http` ([[FTD-restpartijen-webapi-v2_2#1.7 Succescriteria|§1.7]])
- [ ] Noteer het gebruik van AI-tooling voor US-08 in het AI-logboek ([[FTD-restpartijen-webapi-v2_2#7. Definition of Done|§7]])
- [ ] Open de PR en vink het sjabloon af ([[FTD-restpartijen-webapi-v2_2#7. Definition of Done|§7]])

## Fase 18 — US-09 Aanbod verwijderen als beheerder

Status en DoD: [[FTD-restpartijen-webapi-v2_2-tasks#Fase 18 — US-09 Aanbod verwijderen als beheerder|algemene lijst]]

### To-do
- [ ] Maak `src/pricing/service/AdminProductService.kt` met `ProductReader`, `ProductStatusUpdater` en `ReservationMaintenance` in de constructor ([[FTD-restpartijen-webapi-v2_2#12.1 De koppelvlakken|§12.1]])
- [ ] Start na: [[FTD-restpartijen-webapi-v2_2-tasks-stefan#Fase 18 — US-09 Aanbod verwijderen als beheerder|Stefan — `lapseActiveFor` staat op `main`]]
- [ ] Schrijf `delete(productId)`: niet gevonden geeft `NotFoundException`; `COLLECTED`, `EXPIRED` of `REMOVED` geeft `IllegalStateTransitionException`; bij `RESERVED` eerst `lapseActiveFor`, dan `markRemoved` ([[FTD-restpartijen-webapi-v2_2#5.9 US-09 — Aanbod verwijderen als beheerder|§5.9]])
- [ ] Zet beide aanroepen in één transactie met de helper van GI-1 ([[FTD-restpartijen-webapi-v2_2#GI-1 Persistentielaag — uitvoering Eva|GI-1 besluit 1.4]])
      Als het misgaat: werkt de rollback niet over de twee contracten heen, vraag Eva hoe transacties in elkaar grijpen in Exposed.
- [ ] Voeg `DELETE /api/v1/admin/products/{id}` toe met de rolregel `ADMIN`; het antwoord is `204` ([[FTD-restpartijen-webapi-v2_2#10.1 Endpoints|§10.1]])
- [ ] Schrijf `overview(statuses)`: zonder filter alle statussen behalve `REMOVED`, anders precies de gevraagde, via `findByStatus` ([[FTD-restpartijen-webapi-v2_2#5.9 US-09 — Aanbod verwijderen als beheerder|§5.9]])
- [ ] Lees de parameter `status` in, ook meerdere waarden; een onbekende waarde geeft `ValidationException` met `ProductStatus.entries` in de melding ([[FTD-restpartijen-webapi-v2_2#5.9 US-09 — Aanbod verwijderen als beheerder|§5.9]])
- [ ] Voeg `GET /api/v1/admin/products` toe met de rolregel `ADMIN` ([[FTD-restpartijen-webapi-v2_2#10.1 Endpoints|§10.1]])
- [ ] Schrijf de testen voor verwijderen: `204` en `REMOVED` (TC-09), gereserveerde partij, `409` bij `COLLECTED`, `EXPIRED` en `REMOVED` ([[FTD-restpartijen-webapi-v2_2#5.9 US-09 — Aanbod verwijderen als beheerder|§5.9]])
- [ ] Schrijf de rollback-test: laat `markRemoved` falen na `lapseActiveFor`; de reservering is daarna nog `ACTIVE` ([[FTD-restpartijen-webapi-v2_2#5.9 US-09 — Aanbod verwijderen als beheerder|§5.9]])
- [ ] Schrijf de testen voor het overzicht: zonder filter geen `REMOVED`, één status, twee statussen, `status=REMOVED`, onbekende waarde `400` ([[FTD-restpartijen-webapi-v2_2#5.9 US-09 — Aanbod verwijderen als beheerder|§5.9]])
- [ ] Schrijf de rolchecks voor beide routes: `401` zonder token, `403` met een andere rol dan `ADMIN` ([[FTD-restpartijen-webapi-v2_2#GI-6 Testopzet — uitvoering gezamenlijk in de startsessie|GI-6]])
- [ ] Voeg voorbeelden toe aan `requests.http` ([[FTD-restpartijen-webapi-v2_2#1.7 Succescriteria|§1.7]])
- [ ] Noteer het gebruik van AI-tooling voor US-09 in het AI-logboek ([[FTD-restpartijen-webapi-v2_2#7. Definition of Done|§7]])
- [ ] Open de PR en vink het sjabloon af ([[FTD-restpartijen-webapi-v2_2#7. Definition of Done|§7]])

## Fase 20 — Integratie, kwaliteit en assessmentvoorbereiding

Status en DoD: [[FTD-restpartijen-webapi-v2_2-tasks#Fase 20 — Integratie, kwaliteit en assessmentvoorbereiding|algemene lijst]]

### To-do
- [ ] Draai in IntelliJ de testen met coverage (Run → Run with Coverage) en exporteer het rapport naar de testrapportage ([[FTD-restpartijen-webapi-v2_2#14. Niet-functionele eisen (NFR)|NFR-03]])
      Als het misgaat: ligt de regeldekking van de servicelaag onder 80%, noteer dan per feature waar de dekking ontbreekt en meld het in de weekcheck.
- [ ] Schrijf het demoscript: afprijsstaffels, vastgelegde prijs bij reserveren, afgeleid afhaalvenster en de twee automatische overgangen ([[FTD-restpartijen-webapi-v2_2#17. Risico's|R-05]])
- [ ] Noteer voor elk ✔-concept in jouw kolom van §11.4 een bestand en regelnummer, onder andere `Money` voor encapsulatie en `UserRepository` voor generics ([[FTD-restpartijen-webapi-v2_2#11.4 Dekking van de beoordeelde concepten|§11.4]])
- [ ] Tel je eigen unittesten; het zijn er minstens drie, met happy flow en edge cases ([[FTD-restpartijen-webapi-v2_2#1.7 Succescriteria|§1.7]])
- [ ] Bereid je uitleg voor: architectuur, jouw code, je testaanpak en je keuzes, zoals de staffels en de volgorde van de statusbewaking ([[FTD-restpartijen-webapi-v2_2#1.7 Succescriteria|§1.7]])

## Fase 21 — Inleveren

Status en DoD: [[FTD-restpartijen-webapi-v2_2-tasks#Fase 21 — Inleveren|algemene lijst]]

### To-do
- [ ] Controleer dat `feature/f3-prijzen`, `shared/lonneke-money` en `shared/lonneke-security` volledig op `main` staan: `git log origin/main..origin/feature/f3-prijzen` geeft niets ([[FTD-restpartijen-webapi-v2_2#12.3 Git-werkwijze|§12.3]])

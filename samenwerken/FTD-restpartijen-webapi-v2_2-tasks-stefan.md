---
type: takenlijst
title: "Takenlijst restpartijen Web API — Stefan (F2)"
description: "Stefans eigen to-do's bij FTD v2.2: GI-2 applicatie-opzet, GI-4 foutafhandeling, US-04, US-05, US-06, US-11 en de kant van F2 bij de statusbewaking en het verwijderen door de beheerder."
tags: [takenlijst, proftaak, ktor, kotlin, lu1, f2]
timestamp: 2026-09-29T00:00:00
---

# Takenlijst restpartijen Web API — Stefan (F2)

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

### To-do
- [ ] Maak op een testbranch het bestand `test/RunTestCheck.kt` met één test die `import kotlinx.coroutines.test.runTest` gebruikt en `runTest { }` aanroept ([[FTD-restpartijen-webapi-v2_2#21. Open punten|§21]])
- [ ] Draai de testen ([[FTD-restpartijen-webapi-v2_2#21. Open punten|§21]])
      ```bash
      # Compile and run all tests, including the check file
      ./kotlin test
      ```
      Verwacht: groen als `runTest` beschikbaar is; anders een fout "Unresolved reference".
      Als het misgaat: `runTest` is niet beschikbaar. Noteer dat en stel voor `org.jetbrains.kotlinx:kotlinx-coroutines-test` toe te voegen onder `test-dependencies`; dat besluit neemt de groep.
- [ ] Noteer de uitkomst in het AI-logboek en verwijder `test/RunTestCheck.kt` en de testbranch ([[FTD-restpartijen-webapi-v2_2#21. Open punten|§21]])

## Fase 4 — GI-2 Applicatie-opzet

Status en DoD: [[FTD-restpartijen-webapi-v2_2-tasks#Fase 4 — GI-2 Applicatie-opzet|algemene lijst]]

### Voordat je begint
- [ ] Maak een branch vanaf de nieuwste `main` ([[FTD-restpartijen-webapi-v2_2#12.3 Git-werkwijze|B-28]])
      ```bash
      # Start the GI-2 branch from the latest main (branch pattern shared/<name>-<part>)
      git fetch origin && git switch -c shared/stefan-setup origin/main
      ```

### To-do
- [ ] Maak `libs.versions.toml` in de projectroot en zet daarin Exposed 1.5.0, H2 2.4.240 en MockK 1.14.11 ([[FTD-restpartijen-webapi-v2_2#GI-2 Applicatie-opzet — uitvoering Stefan|GI-2 besluit 2.6]])
- [ ] Vervang in `module.yaml` de coördinaten van die drie door `$libs.<key>`; de Ktor-artefacten houden geen versie, die komt van `settings.ktor` ([[FTD-restpartijen-webapi-v2_2#16.6 Afhankelijkheden|§16.6]])
- [ ] Voeg in `module.yaml` onder `settings.kotlin` de regel `allWarningsAsErrors: true` toe ([[FTD-restpartijen-webapi-v2_2#14. Niet-functionele eisen (NFR)|NFR-05]])
- [ ] Controleer dat de instelling werkt: zet tijdelijk `val unused = 1` in een function in `src/Application.kt` en draai `./kotlin test` ([[FTD-restpartijen-webapi-v2_2#14. Niet-functionele eisen (NFR)|NFR-05]])
      Verwacht: de build faalt op een warning. Haal de regel weg en draai opnieuw: groen.
- [ ] Voeg de dependencies `io.ktor:ktor-server-di`, `io.ktor:ktor-server-cors` en `io.ktor:ktor-server-call-logging` toe aan `module.yaml` ([[FTD-restpartijen-webapi-v2_2#ADR-05 — Ktor-plugins en verantwoordelijkheden|ADR-05]])
- [ ] Registreer in `Application.module()` de repository en de service van de walking skeleton via de DI-plugin, in plaats van ze met de hand aan te maken ([[FTD-restpartijen-webapi-v2_2#GI-2 Applicatie-opzet — uitvoering Stefan|GI-2 besluit 2.1]])
- [ ] Laat elke feature aanhaken met één regel `configure…Routing()` in `Application.module()` ([[FTD-restpartijen-webapi-v2_2#GI-2 Applicatie-opzet — uitvoering Stefan|GI-2 besluit 2.2]])
- [ ] Schrijf een integratietest die in `testApplication` de repository van F1 vervangt door een fake, zonder productiecode te wijzigen ([[FTD-restpartijen-webapi-v2_2#GI-2 Applicatie-opzet — uitvoering Stefan|GI-2]])
- [ ] Maak `src/plugins/Cors.kt` en installeer `CORS` ([[FTD-restpartijen-webapi-v2_2#ADR-05 — Ktor-plugins en verantwoordelijkheden|ADR-05]])
- [ ] Maak `src/plugins/Logging.kt` en installeer `CallLogging` ([[FTD-restpartijen-webapi-v2_2#ADR-05 — Ktor-plugins en verantwoordelijkheden|ADR-05]])
- [ ] Spreek met Lonneke de naam van de omgevingsvariabele voor het JWT-secret af ([[FTD-restpartijen-webapi-v2_2#16.4 Secrets|§16.4]])
- [ ] Zet in `resources/application.yaml` een placeholder voor het secret die naar die omgevingsvariabele verwijst ([[FTD-restpartijen-webapi-v2_2#GI-2 Applicatie-opzet — uitvoering Stefan|GI-2 besluit 2.3]])
- [ ] Maak in `src/config/` een klasse die het secret uit de configuratie leest; lees alleen in, bouw geen tokens ([[FTD-restpartijen-webapi-v2_2#GI-3 Authenticatie en rollen — uitvoering Lonneke|GI-3 besluit 3.9]])
- [ ] Maak `.env.example` in de projectroot met de naam van de variabele en zonder waarde ([[FTD-restpartijen-webapi-v2_2#16.4 Secrets|§16.4]])
- [ ] Besluit met Lonneke hoe starten zonder handmatige stappen samengaat met het secret, en noteer het in §21 van het FTD ([[FTD-restpartijen-webapi-v2_2#21. Open punten|§21]])
- [ ] Werk `README.md` bij: verwijs naar FTD v2_2 en noem de omgevingsvariabele (aanvulling, [[FTD-restpartijen-webapi-v2_2-tasks#A9 Repository wijkt af van het FTD|A9]])
- [ ] Controleer dat de header `Authorization` niet in de log komt ([[FTD-restpartijen-webapi-v2_2#16.7 Logging|§16.7]])
      ```bash
      # With the API running (./kotlin run): send a request with a fake token
      curl -s -H "Authorization: Bearer test123" http://localhost:8080/api/v1/products/1
      ```
      Verwacht: de log van `./kotlin run` bevat `test123` niet.
- [ ] Controleer een verse clone ([[FTD-restpartijen-webapi-v2_2#GI-2 Applicatie-opzet — uitvoering Stefan|GI-2]])
      ```bash
      # Clone into an empty folder and start from scratch
      cd /tmp && git clone https://github.com/renee77/restpartijen-api.git check-clone && cd check-clone && git switch shared/stefan-setup && ./kotlin run
      ```
      Verwacht: de API start; `GET /api/v1/products/1` geeft `200`.
- [ ] Open de PR naar `main` en vink het sjabloon af ([[FTD-restpartijen-webapi-v2_2#12.3 Git-werkwijze|§12.3]])

## Fase 6 — GI-4 Foutafhandeling

Status en DoD: [[FTD-restpartijen-webapi-v2_2-tasks#Fase 6 — GI-4 Foutafhandeling|algemene lijst]]

### Voordat je begint
- [ ] Maak een branch vanaf de nieuwste `main` ([[FTD-restpartijen-webapi-v2_2#12.3 Git-werkwijze|B-28]])
      ```bash
      # Start the GI-4 branch from the latest main
      git fetch origin && git switch -c shared/stefan-errors origin/main
      ```

### To-do
- [ ] Voeg `io.ktor:ktor-server-status-pages` en `io.ktor:ktor-server-request-validation` toe aan `module.yaml` ([[FTD-restpartijen-webapi-v2_2#ADR-05 — Ktor-plugins en verantwoordelijkheden|ADR-05]])
- [ ] Maak `src/shared/ErrorResponse.kt`: een `@Serializable` data class met `code`, `message` en `field` (nullable). Dit is een wijziging in `shared`, dus twee reviews ([[FTD-restpartijen-webapi-v2_2#12.2 Wat bewust gedeeld blijft|§12.2]])
- [ ] Maak `src/plugins/StatusPagesConfig.kt` met per exceptie uit §10.2 een eigen regel `exception<…> { … }` die de statuscode en een `ErrorResponse` stuurt ([[FTD-restpartijen-webapi-v2_2#GI-4 Foutafhandeling — uitvoering Stefan|GI-4 besluit 4.1]])
- [ ] Voeg een regel toe voor Ktor's `BadRequestException`, zodat een body die niet te deserialiseren is `400` geeft in dezelfde vorm ([[FTD-restpartijen-webapi-v2_2#10.2 Validatie en foutafhandeling|§10.2]])
- [ ] Voeg als laatste een regel toe voor `Throwable`: `500` met een algemene melding, en de stacktrace alleen in de log ([[FTD-restpartijen-webapi-v2_2#GI-4 Foutafhandeling — uitvoering Stefan|GI-4 besluit 4.4]])
- [ ] Voeg de exceptie uit het besluit over A7 toe, als de groep daarvoor kiest (aanvulling, [[FTD-restpartijen-webapi-v2_2-tasks#A7 Exceptie voor 409 bij een bestaand e-mailadres|A7]])
- [ ] Maak `src/plugins/RequestValidation.kt` en installeer `RequestValidation` ([[FTD-restpartijen-webapi-v2_2#ADR-05 — Ktor-plugins en verantwoordelijkheden|ADR-05]])
- [ ] Roep beide aan in `Application.module()`, vóór de routing ([[FTD-restpartijen-webapi-v2_2#GI-2 Applicatie-opzet — uitvoering Stefan|GI-2 besluit 2.2]])
- [ ] Schrijf in `test/plugins/StatusPagesTest.kt` per statuscode een integratietest met een testroute die de exceptie gooit; controleer ook de velden van het foutantwoord ([[FTD-restpartijen-webapi-v2_2#GI-4 Foutafhandeling — uitvoering Stefan|GI-4]])
- [ ] Schrijf de test voor de onverwachte exceptie: `500`, en de body bevat geen `at com.` of `Exception` ([[FTD-restpartijen-webapi-v2_2#GI-4 Foutafhandeling — uitvoering Stefan|GI-4]])
- [ ] Open de PR naar `main` en vraag Eva en Lonneke als reviewer ([[FTD-restpartijen-webapi-v2_2#GI-5 Gedeelde domeinkern — geen eigenaar, PR met review door alle drie|GI-5]])
- [ ] Laat Eva weten dat `StatusPages` op `main` staat ([[FTD-restpartijen-webapi-v2_2#10.2 Validatie en foutafhandeling|§10.2]])

## Fase 12 — US-04 Aanbod zoeken en filteren

Status en DoD: [[FTD-restpartijen-webapi-v2_2-tasks#Fase 12 — US-04 Aanbod zoeken en filteren|algemene lijst]]

### Voordat je begint
- [ ] Maak je featurebranch vanaf de nieuwste `main` ([[FTD-restpartijen-webapi-v2_2#12.3 Git-werkwijze|§12.3]])
      ```bash
      # One long-lived branch for F2; merge main into it daily
      git fetch origin && git switch -c feature/f2-reserveren origin/main
      ```

### To-do
- [ ] Maak in `test/reservation/` een `FakeProductReader` en een `FakePriceProvider`, elk met een `MutableList` of vaste antwoorden ([[FTD-restpartijen-webapi-v2_2#GI-6 Testopzet — uitvoering gezamenlijk in de startsessie|GI-6 besluit 6.4]])
- [ ] Maak `src/reservation/model/SearchCriteria.kt`: een data class met `category`, `maxPrice` en `minShelfLifeHours`, alle drie optioneel met een default ([[FTD-restpartijen-webapi-v2_2#11.2 Stefan Pellikaan — F2 Zoeken en reserveren|§11.2]])
- [ ] Lees in de route `category` in; een waarde buiten `ProductCategory` geeft `ValidationException` met `ProductCategory.entries` in de melding ([[FTD-restpartijen-webapi-v2_2#5.4 US-04 — Aanbod zoeken en filteren|§5.4]])
- [ ] Reken `maxPrice` van euro's om naar `Money` met de gedeelde omrekening ([[FTD-restpartijen-webapi-v2_2#10.1 Endpoints|B-25]])
- [ ] Maak `src/reservation/service/ProductSearchService.kt` met `ProductReader`, `PriceProvider` en `Clock` in de constructor ([[FTD-restpartijen-webapi-v2_2#SD-2 — Zoeken en reserveren met autorisatie (US-04, US-05)|SD-2]])
- [ ] Schrijf `search(criteria)`: haal `findAvailable(clock.now())` op, vraag per partij `priceBreakdown()` op en laat partijen met `null` weg ([[FTD-restpartijen-webapi-v2_2#SD-2 — Zoeken en reserveren met autorisatie (US-04, US-05)|SD-2]])
- [ ] Filter met predicaten van het type `(ProductView) -> Boolean`, vergelijk `maxPrice` met de actuele prijs, en sorteer oplopend op `shelfLifeRemaining(clock)` ([[FTD-restpartijen-webapi-v2_2#5.4 US-04 — Aanbod zoeken en filteren|§5.4]])
- [ ] Besluit hoe `Page<T>` in het zoekresultaat past en bouw hem (aanvulling, [[FTD-restpartijen-webapi-v2_2-tasks#A14 Paginering met Page|A14]])
- [ ] Maak in `src/reservation/dto/` het antwoord per partij: naam en coördinaten van de aanbieder, en `originalPrice`, `discountPercentage` en `currentPrice` in euro's ([[FTD-restpartijen-webapi-v2_2#5.4 US-04 — Aanbod zoeken en filteren|§5.4]])
- [ ] Voeg de openbare route `GET /api/v1/products` toe in `src/reservation/routes/` ([[FTD-restpartijen-webapi-v2_2#10.1 Endpoints|§10.1]])
- [ ] Schrijf de unittests met de fakes: filter op categorie (TC-04), maximumprijs op de afgeprijsde prijs, combinatie werkt als EN, `minShelfLifeHours`, sortering ([[FTD-restpartijen-webapi-v2_2#11.2 Stefan Pellikaan — F2 Zoeken en reserveren|§11.2]])
- [ ] Schrijf de unittest: een partij die nog `LISTED` is maar over de datum, komt niet in het resultaat ([[FTD-restpartijen-webapi-v2_2#11.2 Stefan Pellikaan — F2 Zoeken en reserveren|§11.2]])
- [ ] Schrijf de unittest: een fake `PriceProvider` die `null` geeft, laat die partij wegvallen ([[FTD-restpartijen-webapi-v2_2#SD-2 — Zoeken en reserveren met autorisatie (US-04, US-05)|SD-2]])
- [ ] Schrijf de integratietesten: `200` zonder token, `400` bij een onbekende categorie met de toegestane waarden ([[FTD-restpartijen-webapi-v2_2#5.4 US-04 — Aanbod zoeken en filteren|§5.4]])
- [ ] Voeg voorbeelden met en zonder filters toe aan `requests.http` ([[FTD-restpartijen-webapi-v2_2#1.7 Succescriteria|§1.7]])
- [ ] Noteer het gebruik van AI-tooling voor US-04 in het AI-logboek ([[FTD-restpartijen-webapi-v2_2#7. Definition of Done|§7]])
- [ ] Open de PR en vink het sjabloon af ([[FTD-restpartijen-webapi-v2_2#7. Definition of Done|§7]])

## Fase 13 — US-05 Partij reserveren

Status en DoD: [[FTD-restpartijen-webapi-v2_2-tasks#Fase 13 — US-05 Partij reserveren|algemene lijst]]

### Voordat je begint
- [ ] Start na: [[FTD-restpartijen-webapi-v2_2-tasks-eva#Fase 7 — K-1 en K-6 Contracten van F1 geleverd|Eva — de contracten van F1 staan op `main`]]

### To-do
- [ ] Maak `src/reservation/model/ReservationStatus.kt` (`ACTIVE`, `COLLECTED`, `CANCELLED`, `LAPSED`) en `ReservationEvent.kt` (`RESERVE`, `COLLECT`, `CANCEL`, `LAPSE`) ([[FTD-restpartijen-webapi-v2_2#9.5 Statusmachine|§9.5]])
- [ ] Maak `src/reservation/service/ReservationStateMachine.kt` met `transition(ProductStatus, ReservationEvent)` en `allowedEvents(ProductStatus)`, precies volgens de tabel in §9.5; elke andere combinatie gooit `IllegalStateTransitionException` ([[FTD-restpartijen-webapi-v2_2#9.5 Statusmachine|§9.5]])
- [ ] Maak `src/reservation/repository/ReservationsTable.kt` met de kolommen van `RESERVATIONS` uit het ERD, en meld hem aan in `src/Application.kt` ([[FTD-restpartijen-webapi-v2_2#9.1 ERD|§9.1]])
- [ ] Maak `src/reservation/model/Reservation.kt` en `ReservationRepository`, die `Repository<Reservation>` implementeert ([[FTD-restpartijen-webapi-v2_2#11.2 Stefan Pellikaan — F2 Zoeken en reserveren|§11.2]])
- [ ] Schrijf `ReservationService.reserve(productId, collectorId)` in deze volgorde: niet gevonden of `REMOVED` → `NotFoundException`; niet `LISTED` of over de datum → `IllegalStateTransitionException`; venster voorbij → `DomainRuleException` ([[FTD-restpartijen-webapi-v2_2#SD-2 — Zoeken en reserveren met autorisatie (US-04, US-05)|SD-2]])
- [ ] Laat de controle op `REMOVED` vóór de controle op `LISTED` staan, zodat een verwijderde partij `404` geeft ([[FTD-restpartijen-webapi-v2_2#9.5 Statusmachine|B-32]])
- [ ] Vraag daarna de overgang aan de statusmachine, de prijs via `currentPrice()`, en roep `markReserved` aan; `false` wordt `IllegalStateTransitionException` ([[FTD-restpartijen-webapi-v2_2#SD-2 — Zoeken en reserveren met autorisatie (US-04, US-05)|SD-2]])
- [ ] Sla de reservering op met status `ACTIVE` en de prijs in `reserved_price_cents`, in dezelfde transactie als `markReserved` ([[FTD-restpartijen-webapi-v2_2#9.6 Opslagkeuzes|§9.6]])
- [ ] Bereken het moment waarop de reservering vervalt: het vroegste van reserveren plus 24 uur en het einde van het afhaalvenster ([[FTD-restpartijen-webapi-v2_2#5.5 US-05 — Partij reserveren|§5.5]])
- [ ] Voeg de route `POST /api/v1/reservations` toe met de rolregel `COLLECTOR`; de afhaler komt uit het token ([[FTD-restpartijen-webapi-v2_2#GI-3 Authenticatie en rollen — uitvoering Lonneke|GI-3 besluit 3.6]])
- [ ] Laat het antwoord geen e-mailadres van de afhaler bevatten ([[FTD-restpartijen-webapi-v2_2#15. Privacy by design|§15]])
- [ ] Besluit de criteria voor `GET /reservations` en schrijf ze in de PR (aanvulling, [[FTD-restpartijen-webapi-v2_2-tasks#A3 Criteria voor GET reservations|A3]])
- [ ] Voeg de route `GET /api/v1/reservations` toe: alleen de eigen reserveringen, met de rolregel `COLLECTOR` (aanvulling, [[FTD-restpartijen-webapi-v2_2-tasks#A3 Criteria voor GET reservations|A3]])
- [ ] Schrijf de unittests voor de statusmachine: elke toegestane overgang, en een ongeldige overgang gooit de exceptie ([[FTD-restpartijen-webapi-v2_2#11.2 Stefan Pellikaan — F2 Zoeken en reserveren|§11.2]])
- [ ] Schrijf de unittests voor `reserve`: happy flow (TC-05), dubbele reservering, verlopen partij ook als die nog `LISTED` is, na het venster, `REMOVED` geeft `NotFoundException` ([[FTD-restpartijen-webapi-v2_2#11.2 Stefan Pellikaan — F2 Zoeken en reserveren|§11.2]])
- [ ] Schrijf de unittest: de gereserveerde prijs blijft gelijk nadat de partij in een volgende staffel valt ([[FTD-restpartijen-webapi-v2_2#11.2 Stefan Pellikaan — F2 Zoeken en reserveren|§11.2]])
- [ ] Schrijf een integratietest met twee gelijktijdige reserveringen op dezelfde partij: precies één `201` en één `409` ([[FTD-restpartijen-webapi-v2_2#5.5 US-05 — Partij reserveren|§5.5]])
- [ ] Schrijf een integratietest waarin de insert faalt; de partij is daarna weer `LISTED` ([[FTD-restpartijen-webapi-v2_2#9.6 Opslagkeuzes|§9.6]])
- [ ] Schrijf de rolchecks voor beide routes: `403` als `SUPPLIER`, `401` zonder token ([[FTD-restpartijen-webapi-v2_2#GI-6 Testopzet — uitvoering gezamenlijk in de startsessie|GI-6]])
- [ ] Voeg voorbeelden toe aan `requests.http` ([[FTD-restpartijen-webapi-v2_2#1.7 Succescriteria|§1.7]])
- [ ] Noteer het gebruik van AI-tooling voor US-05 in het AI-logboek ([[FTD-restpartijen-webapi-v2_2#7. Definition of Done|§7]])
- [ ] Open de PR en vink het sjabloon af ([[FTD-restpartijen-webapi-v2_2#7. Definition of Done|§7]])

## Fase 14 — US-06 Reservering intrekken

Status en DoD: [[FTD-restpartijen-webapi-v2_2-tasks#Fase 14 — US-06 Reservering intrekken|algemene lijst]]

### To-do
- [ ] Schrijf `ReservationService.cancel(reservationId, collectorId)`: een reservering van een ander gooit `ForbiddenException`; de statusmachine beslist met `CANCEL` ([[FTD-restpartijen-webapi-v2_2#5.6 US-06 — Reservering intrekken|§5.6]])
- [ ] Zet de reservering op `CANCELLED` en roep `markListed` aan, in één transactie ([[FTD-restpartijen-webapi-v2_2#9.5 Statusmachine|§9.5]])
- [ ] Voeg de route `DELETE /api/v1/reservations/{id}` toe met de rolregel `COLLECTOR`; het antwoord is `204` ([[FTD-restpartijen-webapi-v2_2#5.6 US-06 — Reservering intrekken|§5.6]])
- [ ] Schrijf de integratietesten: eigen reservering `204` en partij `LISTED`, van een ander `403` (TC-06), `COLLECTED` geeft `409`, `403` als `SUPPLIER`, `401` zonder token ([[FTD-restpartijen-webapi-v2_2#5.6 US-06 — Reservering intrekken|§5.6]])
- [ ] Voeg een voorbeeld toe aan `requests.http` ([[FTD-restpartijen-webapi-v2_2#1.7 Succescriteria|§1.7]])
- [ ] Noteer het gebruik van AI-tooling voor US-06 in het AI-logboek ([[FTD-restpartijen-webapi-v2_2#7. Definition of Done|§7]])
- [ ] Open de PR en vink het sjabloon af ([[FTD-restpartijen-webapi-v2_2#7. Definition of Done|§7]])

## Fase 15 — US-11 Ophalen bevestigen

Status en DoD: [[FTD-restpartijen-webapi-v2_2-tasks#Fase 15 — US-11 Ophalen bevestigen|algemene lijst]]

### To-do
- [ ] Schrijf `ReservationService.collect(reservationId, collectorId)`: een reservering van een ander gooit `ForbiddenException`, buiten het afhaalvenster `DomainRuleException`, en de statusmachine beslist met `COLLECT` ([[FTD-restpartijen-webapi-v2_2#5.11 US-11 — Ophalen bevestigen|§5.11]])
- [ ] Zet de reservering op `COLLECTED` met `collectedAt` uit de klok, en roep `markCollected` aan, in één transactie ([[FTD-restpartijen-webapi-v2_2#5.11 US-11 — Ophalen bevestigen|§5.11]])
- [ ] Voeg de route `POST /api/v1/reservations/{id}/collect` toe met de rolregel `COLLECTOR`; het antwoord is `200` ([[FTD-restpartijen-webapi-v2_2#10.1 Endpoints|§10.1]])
- [ ] Schrijf de integratietesten: happy flow (TC-11), van een ander `403`, al opgehaald, ingetrokken of vervallen `409`, buiten het venster `422`, `403` als `SUPPLIER`, `401` zonder token ([[FTD-restpartijen-webapi-v2_2#11.2 Stefan Pellikaan — F2 Zoeken en reserveren|§11.2]])
- [ ] Voeg een voorbeeld toe aan `requests.http` ([[FTD-restpartijen-webapi-v2_2#1.7 Succescriteria|§1.7]])
- [ ] Noteer het gebruik van AI-tooling voor US-11 in het AI-logboek ([[FTD-restpartijen-webapi-v2_2#7. Definition of Done|§7]])
- [ ] Open de PR en vink het sjabloon af ([[FTD-restpartijen-webapi-v2_2#7. Definition of Done|§7]])

## Fase 17 — US-08 Automatische statusbewaking

Status en DoD: [[FTD-restpartijen-webapi-v2_2-tasks#Fase 17 — US-08 Automatische statusbewaking|algemene lijst]]

### To-do
- [ ] Maak `src/reservation/service/ExposedReservationMaintenance.kt`, die `ReservationMaintenance` implementeert ([[FTD-restpartijen-webapi-v2_2#12.1 De koppelvlakken|§12.1]])
- [ ] Schrijf `lapseOverdue(now)`: zoek de actieve reserveringen waarvan het venster voorbij is of die 24 uur oud zijn, zet ze via `LAPSE` op `LAPSED`, roep per partij `markListed` aan en geef het aantal terug ([[FTD-restpartijen-webapi-v2_2#SD-4 — Automatische statusbewaking (US-08)|SD-4]])
- [ ] Registreer de implementatie in DI, in `src/Application.kt` ([[FTD-restpartijen-webapi-v2_2#12.1 De koppelvlakken|§12.1]])
- [ ] Schrijf de unittests: een reservering ouder dan 24 uur vervalt ook binnen het venster; een reservering na het venster vervalt; een reservering binnen beide grenzen blijft `ACTIVE` ([[FTD-restpartijen-webapi-v2_2#11.2 Stefan Pellikaan — F2 Zoeken en reserveren|§11.2]])
- [ ] Open de PR en laat Lonneke weten dat `lapseOverdue` op `main` staat ([[FTD-restpartijen-webapi-v2_2#Bijlage A — Werken met contracten|bijlage A]])

## Fase 18 — US-09 Aanbod verwijderen als beheerder

Status en DoD: [[FTD-restpartijen-webapi-v2_2-tasks#Fase 18 — US-09 Aanbod verwijderen als beheerder|algemene lijst]]

### To-do
- [ ] Schrijf `lapseActiveFor(productId)`: laat de actieve reservering van die partij vervallen via `LAPSE`, roep `markListed` aan en geef `true`; zonder actieve reservering `false` ([[FTD-restpartijen-webapi-v2_2#12.1 De koppelvlakken|§12.1]])
- [ ] Schrijf de unittests voor `true` en `false` ([[FTD-restpartijen-webapi-v2_2#12.1 De koppelvlakken|§12.1]])
- [ ] Open de PR en laat Lonneke weten dat `lapseActiveFor` op `main` staat ([[FTD-restpartijen-webapi-v2_2#Bijlage A — Werken met contracten|bijlage A]])

## Fase 20 — Integratie, kwaliteit en assessmentvoorbereiding

Status en DoD: [[FTD-restpartijen-webapi-v2_2-tasks#Fase 20 — Integratie, kwaliteit en assessmentvoorbereiding|algemene lijst]]

### To-do
- [ ] Meet met de tool uit fase 0 twintig gelijktijdige aanroepen op `GET /api/v1/products` over de volledige seeddata, en noteer de p95 in de testrapportage ([[FTD-restpartijen-webapi-v2_2#8.7 Kwaliteitsscenario's|§8.7]])
- [ ] Noteer voor elk ✔-concept in jouw kolom van §11.4 een bestand en regelnummer, onder andere `by lazy` voor delegation ([[FTD-restpartijen-webapi-v2_2#11.4 Dekking van de beoordeelde concepten|§11.4]])
- [ ] Tel je eigen unittesten; het zijn er minstens drie, met happy flow en edge cases ([[FTD-restpartijen-webapi-v2_2#1.7 Succescriteria|§1.7]])
- [ ] Bereid je uitleg voor: architectuur, jouw code, je testaanpak en je keuzes, zoals de statusmachine en de applicatie-opzet ([[FTD-restpartijen-webapi-v2_2#1.7 Succescriteria|§1.7]])

## Fase 21 — Inleveren

Status en DoD: [[FTD-restpartijen-webapi-v2_2-tasks#Fase 21 — Inleveren|algemene lijst]]

### To-do
- [ ] Controleer dat `feature/f2-reserveren`, `shared/stefan-setup` en `shared/stefan-errors` volledig op `main` staan: `git log origin/main..origin/feature/f2-reserveren` geeft niets ([[FTD-restpartijen-webapi-v2_2#12.3 Git-werkwijze|§12.3]])

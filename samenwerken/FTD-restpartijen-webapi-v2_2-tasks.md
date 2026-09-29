---
type: takenlijst
title: "Takenlijst restpartijen Web API — algemeen"
description: "Gezamenlijke takenlijst bij FTD v2.2: alle fasen met hun DoD, de gedeelde to-do's, aanvullingen, dekking en open vragen. De eigen to-do's staan in de lijsten van Eva, Stefan en Lonneke."
tags: [takenlijst, proftaak, ktor, kotlin, lu1]
timestamp: 2026-09-29T00:00:00
---

# Takenlijst restpartijen Web API — algemeen

Bron: [[FTD-restpartijen-webapi-v2_2]], versie 2.2 van 29 september 2026. Persoonlijke lijsten: [[FTD-restpartijen-webapi-v2_2-tasks-eva|Eva (F1)]], [[FTD-restpartijen-webapi-v2_2-tasks-stefan|Stefan (F2)]] en [[FTD-restpartijen-webapi-v2_2-tasks-lonneke|Lonneke (F3)]].

## Leeswijzer

- Elk item eindigt op één marker: een link naar de paragraaf in het FTD die het item uitvoert, of `(aanvulling, An)` voor iets wat het FTD niet zegt maar de lijst wel nodig heeft (zie Aanvullingen).
- Deze lijst is de enige plek waar een fase sluit. Hier staan alle DoD-items en de gedeelde to-do's; de eigen to-do's staan in de persoonlijke lijsten, waar `Toegewezen:` naar linkt.
- Paden zijn relatief aan de root van de repository `restpartijen-api/`. Commando's zijn voor Linux en macOS; op Windows is `./kotlin` gelijk aan `kotlin.bat`. Bewijs bij een DoD-item noteer je in de beschrijving van de pull request (PR).

## Aanvullingen

### A1 PR-sjabloon met story-DoD en bewijs
- Het FTD zegt niet hoe de acht punten van de story-DoD (§7) per story worden afgevinkt, en ook niet waar het bewijs komt te staan.
- De lijst heeft één plek nodig waar elke DoD per PR te controleren is. Een sjabloon `.github/pull_request_template.md` zet de acht punten van §7, de controle op Nederlands werkcommentaar (B-27) en een kopje "Bewijs" in elke nieuwe PR.
- Bevestigt: de profgroep, in fase 0.

### A2 Vaste plek voor AI-logboek, testrapportage en weekcheck
- Het FTD eist een AI-logboek (§7), een testrapportage (NFR-03, §8.7) en een wekelijkse check (R-01), maar niet waar die staan. §21 noemt dit zelf als open punt voor de testrapportage.
- Zonder vaste plek is het bewijs bij het assessment niet terug te vinden.
- Bevestigt: de profgroep, in fase 0. Het afgesproken pad komt in `README.md`.

### A3 Criteria voor GET reservations
- `GET /reservations` staat in §10.1 en §18.4, maar heeft geen user story en geen acceptatiecriteria.
- Zonder criteria is er geen DoD. De lijst neemt de minimale criteria op die uit §1.3 en §18.4 volgen: `200 OK` met alleen de eigen reserveringen, de afhaler komt uit het token, en een `SUPPLIER` krijgt `403`.
- Bevestigt: Stefan.

### A4 Criteria voor GET suppliers products
- `GET /suppliers/{id}/products` staat in §10.1 en §18.4, maar heeft geen acceptatiecriteria. Alleen vast staat dat de publieke API een `REMOVED`-partij niet toont (§9.5).
- De app gebruikt deze endpoint voor "Mijn aanbod". Welke statussen erin horen (bijvoorbeeld `RESERVED` wel, `EXPIRED` niet), bepaalt wat de aanbieder ziet.
- Bevestigt: Eva.

### A5 HTTP-client en User-Agent voor Open Food Facts
- Het FTD zegt dat `OpenFoodFactsClient` `suspend` is en een `User-Agent` met contactadres meestuurt (§5.2, §10.3), maar niet met welke HTTP-client en welk contactadres.
- Een dependency komt pas in `module.yaml` na een besluit. Het advies van de docent (kotlinx boven Java, B-15) wijst naar de client van Ktor zelf.
- Bevestigt: Eva, met review van de PR door Stefan en Lonneke.

### A6 Library voor BCrypt
- GI-3 besluit 3.4 kiest BCrypt, maar niet welke library. Een kotlinx-variant is er niet, dus B-15 geeft geen antwoord.
- Een dependency komt pas in `module.yaml` na een besluit.
- Bevestigt: Lonneke, met review van de PR door Eva en Stefan.

### A7 Exceptie voor 409 bij een bestaand e-mailadres
- GI-3 eist `409 Conflict` bij registreren met een bestaand e-mailadres. In §10.2 hoort `409` alleen bij `IllegalStateTransitionException`, en dit is geen statusovergang.
- Hergebruik maakt de naam onwaar; een zevende exceptie is een wijziging in `shared` (GI-5 besluit 5.3).
- Bevestigt: alle drie. Dit is een afwijking van §10.2 en staat ook onder Open vragen.

### A8 Interval van de periodieke statusbewaking
- §11.3 noemt `launch` voor de periodieke statusbewaking; §19 noemt haar een periodieke taak. Het interval staat nergens.
- Zonder interval is de taak niet te bouwen en niet te testen.
- Bevestigt: Lonneke.

### A9 Repository wijkt af van het FTD
- Bij het opstellen van deze lijst bleek: de wrapper `kotlin` haalt Kotlin CLI 0.12.2 op, het FTD noemt 0.12.0 (§3.2). `libs.versions.toml` bestaat niet; de versies staan in `module.yaml` (GI-2 besluit 2.6). `README.md` verwijst naar FTD v2_1.
- Het FTD en de code moeten hetzelfde zeggen, anders klopt de verantwoording bij het assessment niet.
- Bevestigt: de profgroep in fase 0; Stefan voert de versiepunten uit in fase 4.

### A10 Oude taaknummers in FTD en code
- §9.7 en §21 van het FTD en commentaar in de code (`ProductsTable.kt`, `ProductRoutes.kt`, `src/shared/z-package-info.md`) verwijzen naar taaknummers van een eerdere lijst: T2, T9, T15, T21, "F3 T1" en "blok 4".
- Met deze lijst vervallen die nummers. Zo'n verwijzing is anders een losse draad.
- Bevestigt: Eva. Ze vervangt de verwijzingen in fase 0 door fasenummers van deze lijst.

### A11 Validatieregels bij registreren
- GI-3 besluit 3.7 zegt dat `/auth/register` een POST met validatie is, maar niet welke regels gelden voor e-mailadres, wachtwoord en weergavenaam.
- Zonder regels is er geen test op te schrijven.
- Bevestigt: Lonneke.

### A12 Testvolgorde laten variëren
- GI-6 eist dat de testsuite volgordeonafhankelijk is: "twee keer draaien in willekeurige volgorde". De documentatie van de Kotlin Toolchain beschrijft geen manier om de volgorde te laten variëren.
- De lijst controleert daarom twee keer achter elkaar draaien, met een schone database per test. Willekeurige volgorde blijft een open vraag.
- Bevestigt: de profgroep.

### A13 Wijzigen van een partij
- §5.3 zegt dat een aanbieder een eigen partij kan wijzigen, maar niet welke velden en in welke status. Mag een `RESERVED`-partij bijvoorbeeld een andere prijs krijgen?
- Zonder die regel is de PUT niet af te bakenen en niet te testen.
- Bevestigt: Eva.

### A14 Paginering met Page
- §11.2 noemt `Page<T>` als voorbeeld van generics bij Stefan, maar §5.4 en §10.1 kennen geen parameters voor paginering.
- De klasse moet een plek hebben in de API, anders is zij bij het assessment niet te verdedigen.
- Bevestigt: Stefan.

### A15 Het supplierId bij inloggen
- `/auth/login` moet bij een aanbieder het `supplierId` teruggeven (GI-3 besluit 3.8). Dat staat in `SUPPLIERS`, een tabel van F1. `security` mag geen feature importeren (§8.3), en §12.1 heeft geen contract voor deze vraag.
- Zonder afspraak kan Lonneke het criterium niet halen, of ontstaat er een afhankelijkheid de verkeerde kant op. Omdat `SUPPLIERS` pas in fase 7 bestaat, controleert de lijst dit criterium in fase 7 en niet in fase 5.
- Bevestigt: Eva en Lonneke. Een nieuw contract in `shared` vraagt twee reviews (GI-5).

## Begrippen

De begrippen uit het domein staan in [[FTD-restpartijen-webapi-v2_2#19. Begrippenlijst|§19]]. Hieronder alleen wat deze lijst toevoegt.

| Begrip | Betekenis |
|--------|-----------|
| DoD | Definition of Done: de controles waarmee een fase sluit. Elk DoD-item is met ja of nee te beantwoorden en noemt zijn bewijs |
| Walking skeleton | Eén endpoint, één tabel en één test door de hele keten, gebouwd in de startsessie (R-07) |
| Fake | Een eenvoudige nepversie van een contract of repository voor een test, bijvoorbeeld met een `MutableList` (GI-6 besluit 6.4) |
| Eerst rood | Een test die je eerst ziet falen voordat hij slaagt. Zo weet je dat hij echt iets controleert |
| Overdracht | Een item `Start na:` in een persoonlijke lijst: je begint pas als een ander iets heeft opgeleverd |
| `./kotlin` | Het wrapperscript van de Kotlin Toolchain in de repository. `./kotlin run` start de API, `./kotlin test` draait alle testen |

---

## Fase 0 — Voorbereiding en verificatie

Basis: [[FTD-restpartijen-webapi-v2_2#3.2 Randvoorwaarden|§3.2]], [[FTD-restpartijen-webapi-v2_2#13. Gedeelde infrastructuur (GI-1 t/m GI-6)|§13]], [[FTD-restpartijen-webapi-v2_2#21. Open punten|§21]], [[FTD-restpartijen-webapi-v2_2#Besluiten van de startsessie en daarna (v2.2)|Besluiten v2.2]]
Raakt: `module.yaml`, `kotlin`, `README.md`, `.github/pull_request_template.md`, `FTD-restpartijen-webapi-v2_2.md`
Start na: —
Toegewezen: [[FTD-restpartijen-webapi-v2_2-tasks-eva#Fase 0 — Voorbereiding en verificatie|Eva]], [[FTD-restpartijen-webapi-v2_2-tasks-stefan#Fase 0 — Voorbereiding en verificatie|Stefan]], [[FTD-restpartijen-webapi-v2_2-tasks-lonneke#Fase 0 — Voorbereiding en verificatie|Lonneke]]

### Gedeelde to-do
- [ ] Loop samen de besluittabel v2.2 door en bevestig de afrondingsregel B-24: gehele deling, 349 cent met 60% korting wordt 139 cent ([[FTD-restpartijen-webapi-v2_2#9.4 Afprijsstaffels per productsoort|B-24]])
- [ ] Bevestig B-31: de seeddata bevat een partij met status `REMOVED` ([[FTD-restpartijen-webapi-v2_2#9.7 Seeddata|B-31]])
- [ ] Bevestig B-32: reserveren van een verwijderde partij levert `404` ([[FTD-restpartijen-webapi-v2_2#9.5 Statusmachine|B-32]])
- [ ] Spreek af waar het AI-logboek, de testrapportage en de weekcheck staan (aanvulling, [[#A2 Vaste plek voor AI-logboek, testrapportage en weekcheck|A2]])
- [ ] Spreek af met welke tool jullie de performance uit §8.7 meten (twintig gelijktijdige aanroepen, p95) ([[FTD-restpartijen-webapi-v2_2#21. Open punten|§21]])
- [ ] Besluit over de afwijkingen tussen repository en FTD: toolchain 0.12.2 of 0.12.0, en de versiecatalogus (aanvulling, [[#A9 Repository wijkt af van het FTD|A9]])
- [ ] Besluit over A7: een nieuwe exceptie voor `409` bij een bestaand e-mailadres, of hergebruik van een bestaande (aanvulling, [[#A7 Exceptie voor 409 bij een bestaand e-mailadres|A7]])
- [ ] Controleer dat ieder `opdrachten/proftaak-casus-en-portfolio.md` kan openen ([[FTD-restpartijen-webapi-v2_2#Revisiehistorie|r.-verwijzingen]])

### DoD
- [ ] Alle drie draaien `./kotlin test` groen op `feature/walking-skeleton` op de eigen machine; bewijs is de uitvoer, per persoon in de PR van fase 1 ([[FTD-restpartijen-webapi-v2_2#GI-2 Applicatie-opzet — uitvoering Stefan|GI-2]])
- [x] `module.yaml` zet Kotlin 2.4.20, Ktor 3.6.0 en Exposed 1.5.0: `grep -n "2.4.20\|3.6.0\|1.5.0" module.yaml` geeft drie of meer regels, zonder andere versies van deze drie ([[FTD-restpartijen-webapi-v2_2#3.2 Randvoorwaarden|§3.2]])
- [ ] De kolom *Status* in de besluittabel v2.2 van het FTD zegt "Vast" bij B-24, B-31 en B-32 ([[FTD-restpartijen-webapi-v2_2#Besluiten van de startsessie en daarna (v2.2)|Besluiten v2.2]])
- [ ] `README.md` noemt het pad van het AI-logboek, de testrapportage en de weekcheck (aanvulling, [[#A2 Vaste plek voor AI-logboek, testrapportage en weekcheck|A2]])
- [ ] `.github/pull_request_template.md` staat op `feature/walking-skeleton` en bevat de acht punten van §7, de controle op Nederlands commentaar en een kopje "Bewijs" (aanvulling, [[#A1 PR-sjabloon met story-DoD en bewijs|A1]])
- [ ] De uitkomst van de controle op `runTest` staat in het AI-logboek: beschikbaar, of een voorstel voor de dependency ([[FTD-restpartijen-webapi-v2_2#21. Open punten|§21]])
- [ ] Het besluit over A7 en A9 staat in §21 van het FTD (aanvulling, [[#A9 Repository wijkt af van het FTD|A9]])
- [ ] Iedereen kan het bestand `opdrachten/proftaak-casus-en-portfolio.md` openen, waar alle regelverwijzingen (r.) naar wijzen ([[FTD-restpartijen-webapi-v2_2#Revisiehistorie|r.-verwijzingen]])

---

## Fase 1 — GI-5 en GI-6 Gedeelde domeinkern en testopzet op main

Basis: [[FTD-restpartijen-webapi-v2_2#13. Gedeelde infrastructuur (GI-1 t/m GI-6)|§13]], [[FTD-restpartijen-webapi-v2_2#GI-5 Gedeelde domeinkern — geen eigenaar, PR met review door alle drie|GI-5]], [[FTD-restpartijen-webapi-v2_2#GI-6 Testopzet — uitvoering gezamenlijk in de startsessie|GI-6]], [[FTD-restpartijen-webapi-v2_2#12.1 De koppelvlakken|§12.1]], [[FTD-restpartijen-webapi-v2_2#12.2 Wat bewust gedeeld blijft|§12.2]], [[FTD-restpartijen-webapi-v2_2#10.1 Endpoints|B-25]], [[FTD-restpartijen-webapi-v2_2#17. Risico's|R-07]]
Raakt: `src/shared/*.kt`, `src/shared/z-package-info.md`, `test/testsupport/*`, `test/shared/*`, `module.yaml`
Start na: fase 0
Toegewezen: [[FTD-restpartijen-webapi-v2_2-tasks-eva#Fase 1 — GI-5 en GI-6 Gedeelde domeinkern en testopzet op main|Eva]], [[FTD-restpartijen-webapi-v2_2-tasks-lonneke#Fase 1 — GI-5 en GI-6 Gedeelde domeinkern en testopzet op main|Lonneke]]; Stefan reviewt (gedeelde to-do)

### Gedeelde to-do
- [ ] Vergelijk samen elke function in `src/shared/` met de contracttabel in §12.1: naam, parameters, returntype en `suspend` ([[FTD-restpartijen-webapi-v2_2#12.1 De koppelvlakken|§12.1]])
- [ ] Review de PR van `feature/walking-skeleton` naar `main` met z'n drieën; wijzigingen in `shared` vragen twee reviews naast de auteur ([[FTD-restpartijen-webapi-v2_2#GI-5 Gedeelde domeinkern — geen eigenaar, PR met review door alle drie|GI-5]])
- [ ] Bevestig in die PR besluit B-26: `findByStatus(statuses)` vervangt `findAll()` ([[FTD-restpartijen-webapi-v2_2#12.1 De koppelvlakken|B-26]])

### DoD
- [ ] `main` bevat de walking skeleton: `git log origin/main --oneline | head -5` toont de merge van `feature/walking-skeleton` ([[FTD-restpartijen-webapi-v2_2#13. Gedeelde infrastructuur (GI-1 t/m GI-6)|§13]])
- [ ] Op `main` is `./kotlin test` groen, inclusief `ProductRoutesTest` (één endpoint, één tabel, één test door de keten) ([[FTD-restpartijen-webapi-v2_2#17. Risico's|R-07]])
- [ ] De contracten in `src/shared/` volgen de tabel in §12.1; de reviewers bevestigen dat in de PR ([[FTD-restpartijen-webapi-v2_2#12.1 De koppelvlakken|§12.1]])
- [x] `shared` importeert geen featurepackage: `grep -rn "import com.restpartijen.api.\(product\|reservation\|pricing\|security\|persistence\)" src/shared/` geeft niets ([[FTD-restpartijen-webapi-v2_2#GI-5 Gedeelde domeinkern — geen eigenaar, PR met review door alle drie|GI-5]])
- [ ] `src/shared/` bevat alleen waardetypes, enums, excepties en interfaces; de reviewers bevestigen dat in de PR ([[FTD-restpartijen-webapi-v2_2#GI-5 Gedeelde domeinkern — geen eigenaar, PR met review door alle drie|GI-5 besluit 5.4]])
- [ ] De omrekening van euro's naar centen rondt af naar de dichtstbijzijnde cent: de tests `3.49 → 349`, `0.29 → 29` en `349 → 3.49` zijn groen, en de test `0.29 → 29` is eerst rood gezien met afkappen ([[FTD-restpartijen-webapi-v2_2#10.1 Endpoints|B-25]])
- [ ] Er is één omrekening: `grep -rn "\* 100\|100.0" src/` toont alleen de regel in de gedeelde omrekening ([[FTD-restpartijen-webapi-v2_2#10.1 Endpoints|B-25]])
- [ ] Geen Nederlands commentaar in `src/shared/*.kt`; de reviewer bevestigt dat in de PR ([[FTD-restpartijen-webapi-v2_2#12.3 Git-werkwijze|B-27]])
- [ ] `test/testsupport/FixedClock.kt` staat op `main` en `FixedClockTest` is groen ([[FTD-restpartijen-webapi-v2_2#GI-6 Testopzet — uitvoering gezamenlijk in de startsessie|GI-6 besluit 6.5]])
- [x] `module.yaml` bevat onder `test-dependencies` `ktor-server-test-host` en `mockk` ([[FTD-restpartijen-webapi-v2_2#GI-6 Testopzet — uitvoering gezamenlijk in de startsessie|GI-6 besluit 6.7]])

Terugdraaien: de merge naar `main` terugdraaien met `git revert -m 1 <merge-commit>` via een nieuwe PR.

---

## Fase 2 — Doorlopend samenwerken en bijsturen

Basis: [[FTD-restpartijen-webapi-v2_2#12.3 Git-werkwijze|§12.3]], [[FTD-restpartijen-webapi-v2_2#17. Risico's|§17]], [[FTD-restpartijen-webapi-v2_2#3.3 Ambitieniveau per rubriccriterium|§3.3]], [[FTD-restpartijen-webapi-v2_2#7. Definition of Done|§7]]
Start na: fase 1. Deze fase sluit pas in fase 21.
Toegewezen: iedereen, alleen gedeelde to-do's

### Gedeelde to-do
- [ ] Houd elke week een korte check: ligt elke use case op schema? Noteer de uitkomst op de afgesproken plek (A2), en bij achterstand het besluit om voor realisatie of testen terug te vallen op "voldoende" ([[FTD-restpartijen-webapi-v2_2#17. Risico's|R-01]])
- [ ] Besluit in de weekcheck of US-10 (Should) doorgaat; noteer het besluit ([[FTD-restpartijen-webapi-v2_2#4.1 F1 — Aanbod en productdata (Eva Bouwman)|§4.1]])
- [ ] Trek elke werkdag `main` binnen in je eigen branch ([[FTD-restpartijen-webapi-v2_2#12.3 Git-werkwijze|§12.3]])
      ```bash
      # Fetch the latest main and merge it into the branch you are on
      git fetch origin && git merge origin/main
      ```
      Verwacht: "Already up to date" of een merge zonder conflicten.
      Als het misgaat: een conflict in een bestand van een ander los je niet alleen op. Stop en overleg met de eigenaar.
- [ ] Review een PR die `shared` wijzigt vóór je andere reviews ([[FTD-restpartijen-webapi-v2_2#17. Risico's|R-02]])
- [ ] Noteer bij elke story het gebruik van AI-tooling in het AI-logboek, inclusief waar een AI-suggestie afweek van de officiële documentatie ([[FTD-restpartijen-webapi-v2_2#17. Risico's|R-07]])
- [ ] Meld een blokkade door de toolchain dezelfde dag bij de docent ([[FTD-restpartijen-webapi-v2_2#17. Risico's|R-07]])
- [ ] Verhoog geen versie in `module.yaml`, tenzij een fout daartoe dwingt of de docent het voorschrijft ([[FTD-restpartijen-webapi-v2_2#3.2 Randvoorwaarden|§3.2]])

### DoD
- [ ] Er is een weekcheck voor elke week tussen de start van fase 1 en het inleveren ([[FTD-restpartijen-webapi-v2_2#17. Risico's|R-01]])
- [ ] Elke gemergde PR naar `main` heeft minstens één review; controle via de lijst met gesloten PR's op GitHub ([[FTD-restpartijen-webapi-v2_2#12.3 Git-werkwijze|§12.3]])
- [ ] De branchbeveiliging op `main` staat aan: direct committen kan niet (GitHub, Settings → Branches) ([[FTD-restpartijen-webapi-v2_2#12.3 Git-werkwijze|§12.3]])
- [ ] Het AI-logboek heeft een regel per story die gerealiseerd is ([[FTD-restpartijen-webapi-v2_2#7. Definition of Done|§7]])
- [ ] `git log -p -- module.yaml` toont na fase 0 geen verhoogde versie zonder vermelde reden ([[FTD-restpartijen-webapi-v2_2#3.2 Randvoorwaarden|§3.2]])

---

## Fase 3 — GI-1 Persistentielaag

Basis: [[FTD-restpartijen-webapi-v2_2#GI-1 Persistentielaag — uitvoering Eva|GI-1]], [[FTD-restpartijen-webapi-v2_2#9.6 Opslagkeuzes|§9.6]], [[FTD-restpartijen-webapi-v2_2#ADR-03 — Exposed-versie (vervallen in v1.1)|ADR-03]], [[FTD-restpartijen-webapi-v2_2#Bijlage B — H2 wat het is en hoe wij het gebruiken|bijlage B]], [[FTD-restpartijen-webapi-v2_2#21. Open punten|§21]]
Raakt: `src/persistence/*`, `resources/application.yaml`, `src/Application.kt` (één regel), `test/persistence/*`, `test/testsupport/*`
Start na: fase 1
Toegewezen: [[FTD-restpartijen-webapi-v2_2-tasks-eva#Fase 3 — GI-1 Persistentielaag|Eva]]

### Gedeelde to-do
- [ ] Stefan en Lonneke lezen de KDoc van `Repository<T>` en bevestigen in de PR dat ze hem voor hun eigen entiteit kunnen implementeren ([[FTD-restpartijen-webapi-v2_2#GI-1 Persistentielaag — uitvoering Eva|GI-1 besluit 1.8]])

### DoD
- [ ] `src/persistence/Repository.kt` bevat de generieke interface `Repository<T>` met `findById`, `findAll`, `save` en `delete` ([[FTD-restpartijen-webapi-v2_2#8.4 Klassendiagram|§8.4]])
- [ ] De keuze of die functions `suspend` zijn, staat met reden in de KDoc van `Repository<T>` ([[FTD-restpartijen-webapi-v2_2#12.1 De koppelvlakken|B-21]])
- [ ] De persistentielaag kent geen feature: `grep -rn "import com.restpartijen.api.\(product\|reservation\|pricing\|security\)" src/persistence/` geeft niets ([[FTD-restpartijen-webapi-v2_2#21. Open punten|§21]])
- [ ] `ProductsTable` wordt aangemeld via het nieuwe mechanisme met één regel in `src/Application.kt`; `DatabaseFactory` noemt geen tabel ([[FTD-restpartijen-webapi-v2_2#GI-1 Persistentielaag — uitvoering Eva|GI-1]])
- [ ] De connection URL bevat `DB_CLOSE_DELAY=-1`: `grep -rn "DB_CLOSE_DELAY=-1" src/ resources/` geeft een regel ([[FTD-restpartijen-webapi-v2_2#Bijlage B — H2 wat het is en hoe wij het gebruiken|bijlage B]])
- [ ] Standaard draait H2 in-memory: na `./kotlin run` ontstaat geen bestand `*.mv.db` in de projectroot ([[FTD-restpartijen-webapi-v2_2#GI-1 Persistentielaag — uitvoering Eva|GI-1 besluit 1.2]])
- [ ] Met de schakelaar op bestand ontstaat een `.mv.db`-bestand, en `GET /api/v1/products/1` geeft na een herstart nog steeds `200` ([[FTD-restpartijen-webapi-v2_2#GI-1 Persistentielaag — uitvoering Eva|GI-1 besluit 1.2]])
- [ ] Databasewerk loopt via `suspendTransaction` binnen `withContext(Dispatchers.IO)`; `grep -rn "newSuspendedTransaction" src/` geeft niets ([[FTD-restpartijen-webapi-v2_2#GI-1 Persistentielaag — uitvoering Eva|GI-1 besluit 1.6]])
- [ ] Een testhelper maakt de database per test leeg zonder de productieconfiguratie te wijzigen; `test/persistence/CleanDatabaseTest.kt` bewijst dat twee testen elkaars rijen niet zien ([[FTD-restpartijen-webapi-v2_2#GI-1 Persistentielaag — uitvoering Eva|GI-1]])
- [ ] Het schema ontstaat bij het opstarten met `SchemaUtils.create` ([[FTD-restpartijen-webapi-v2_2#9.6 Opslagkeuzes|§9.6]])
- [ ] De plek van `SeedData` is gekozen zonder dat `persistence` een feature importeert; de keuze staat in de PR ([[FTD-restpartijen-webapi-v2_2#21. Open punten|§21]])

Terugdraaien: de commits op `shared/eva-persistence` terugdraaien; `main` wijzigt pas bij de merge van de PR.

---

## Fase 4 — GI-2 Applicatie-opzet

Basis: [[FTD-restpartijen-webapi-v2_2#GI-2 Applicatie-opzet — uitvoering Stefan|GI-2]], [[FTD-restpartijen-webapi-v2_2#ADR-04 — Ktor's eigen dependency-injectionplugin|ADR-04]], [[FTD-restpartijen-webapi-v2_2#ADR-05 — Ktor-plugins en verantwoordelijkheden|ADR-05]], [[FTD-restpartijen-webapi-v2_2#16.4 Secrets|§16.4]], [[FTD-restpartijen-webapi-v2_2#16.6 Afhankelijkheden|§16.6]], [[FTD-restpartijen-webapi-v2_2#14. Niet-functionele eisen (NFR)|§14]]
Raakt: `src/Application.kt`, `src/plugins/*`, `src/config/*`, `resources/application.yaml`, `module.yaml`, `libs.versions.toml`, `.env.example`, `README.md`
Start na: fase 1
Toegewezen: [[FTD-restpartijen-webapi-v2_2-tasks-stefan#Fase 4 — GI-2 Applicatie-opzet|Stefan]]

### Gedeelde to-do
- [ ] Stefan en Lonneke besluiten hoe "starten zonder handmatige stappen" samengaat met het JWT-secret uit een omgevingsvariabele; ze noteren het besluit in §21 van het FTD ([[FTD-restpartijen-webapi-v2_2#21. Open punten|§21]])
- [ ] Stefan en Lonneke spreken de naam van de omgevingsvariabele voor het JWT-secret af ([[FTD-restpartijen-webapi-v2_2#16.4 Secrets|§16.4]])

### DoD
- [ ] De DI-plugin van Ktor is geïnstalleerd en services komen uit DI. Een integratietest vervangt de repository van F1 door een fake zonder productiecode te wijzigen; de test is groen ([[FTD-restpartijen-webapi-v2_2#GI-2 Applicatie-opzet — uitvoering Stefan|GI-2]])
- [ ] `src/Application.kt` roept per feature precies één `configure…Routing()` aan ([[FTD-restpartijen-webapi-v2_2#GI-2 Applicatie-opzet — uitvoering Stefan|GI-2 besluit 2.2]])
- [ ] `CORS` en `CallLogging` zijn geïnstalleerd: `grep -rn "install(CORS)\|install(CallLogging)" src/` geeft twee regels ([[FTD-restpartijen-webapi-v2_2#ADR-05 — Ktor-plugins en verantwoordelijkheden|ADR-05]])
- [ ] De versies staan in `libs.versions.toml` in de projectroot; `module.yaml` verwijst ernaar met `$libs.`; Ktor-artefacten hebben geen eigen versie ([[FTD-restpartijen-webapi-v2_2#GI-2 Applicatie-opzet — uitvoering Stefan|GI-2 besluit 2.6]])
- [ ] `settings.kotlin.allWarningsAsErrors` staat op `true`. Eerst rood: een ongebruikte variabele laat `./kotlin test` falen; na verwijderen is het weer groen ([[FTD-restpartijen-webapi-v2_2#14. Niet-functionele eisen (NFR)|NFR-05]])
- [ ] `resources/application.yaml` bevat voor het secret een placeholder en geen waarde; `.env.example` noemt de variabele zonder waarde ([[FTD-restpartijen-webapi-v2_2#16.4 Secrets|§16.4]])
- [ ] Een request met de header `Authorization: Bearer test123` verschijnt in de log zonder `test123` ([[FTD-restpartijen-webapi-v2_2#16.7 Logging|§16.7]])
- [ ] Een verse clone in een lege map start met `./kotlin run` en `GET /api/v1/products/1` geeft `200` ([[FTD-restpartijen-webapi-v2_2#GI-2 Applicatie-opzet — uitvoering Stefan|GI-2]])
- [ ] `README.md` verwijst naar FTD v2_2 (aanvulling, [[#A9 Repository wijkt af van het FTD|A9]])

---

## Fase 5 — GI-3 Authenticatie en rollen

Basis: [[FTD-restpartijen-webapi-v2_2#GI-3 Authenticatie en rollen — uitvoering Lonneke|GI-3]], [[FTD-restpartijen-webapi-v2_2#16.1 Authenticatie en sessiebeheer|§16.1]], [[FTD-restpartijen-webapi-v2_2#16.2 Autorisatiemodel|§16.2]], [[FTD-restpartijen-webapi-v2_2#15. Privacy by design|§15]], [[FTD-restpartijen-webapi-v2_2#10.1 Endpoints|§10.1]], [[FTD-restpartijen-webapi-v2_2#Besluiten van 19 september 2026 (v2.0)|B-2 en B-5]]
Raakt: `src/security/*`, `src/Application.kt` (één regel), `resources/application.yaml`, `module.yaml`, `test/security/*`
Start na: fase 3 (`Repository<T>` en aanmelden van tabellen) en fase 4 (DI en configuratie)
Toegewezen: [[FTD-restpartijen-webapi-v2_2-tasks-lonneke#Fase 5 — GI-3 Authenticatie en rollen|Lonneke]]

### Gedeelde to-do
- [ ] Eva en Stefan reviewen de PR die een BCrypt-library toevoegt (aanvulling, [[#A6 Library voor BCrypt|A6]])

### DoD
- [ ] Het mechanisme gebruikt `install(Authentication)` met JWT; de experimentele typed authentication komt niet voor ([[FTD-restpartijen-webapi-v2_2#GI-3 Authenticatie en rollen — uitvoering Lonneke|GI-3 besluit 3.1]])
- [ ] Een beschermde testroute zonder token geeft `401` ([[FTD-restpartijen-webapi-v2_2#GI-3 Authenticatie en rollen — uitvoering Lonneke|GI-3]])
- [ ] Dezelfde testroute met een geldig token maar de verkeerde rol geeft `403` ([[FTD-restpartijen-webapi-v2_2#GI-3 Authenticatie en rollen — uitvoering Lonneke|GI-3]])
- [ ] Het token bevat één claim `role` met exact de enumnaam in hoofdletters; een test leest de claim uit ([[FTD-restpartijen-webapi-v2_2#GI-3 Authenticatie en rollen — uitvoering Lonneke|GI-3 besluit 3.3]])
- [ ] Het token is 24 uur geldig: een test met een vaste klok controleert `exp` min `iat` ([[FTD-restpartijen-webapi-v2_2#GI-3 Authenticatie en rollen — uitvoering Lonneke|GI-3 besluit 3.5]])
- [ ] `POST /auth/register` geeft `201 Created` en een account met rol `COLLECTOR`, ook als het verzoek `"role":"ADMIN"` meestuurt ([[FTD-restpartijen-webapi-v2_2#GI-3 Authenticatie en rollen — uitvoering Lonneke|GI-3]])
- [ ] Registreren met een bestaand e-mailadres geeft `409 Conflict` ([[FTD-restpartijen-webapi-v2_2#GI-3 Authenticatie en rollen — uitvoering Lonneke|GI-3]])
- [ ] Registreren vraagt alleen e-mailadres, wachtwoord en weergavenaam; de validatieregels uit A11 zijn getest ([[FTD-restpartijen-webapi-v2_2#15. Privacy by design|§15]])
- [ ] `POST /auth/login` geeft het token en de rol; het `supplierId` volgt in fase 7 ([[FTD-restpartijen-webapi-v2_2#GI-3 Authenticatie en rollen — uitvoering Lonneke|GI-3 besluit 3.8]])
- [ ] Inloggen met een verkeerd wachtwoord geeft `401` ([[FTD-restpartijen-webapi-v2_2#18.4 Request-flow tussen de app en de API|§18.4]])
- [ ] Het antwoord op registreren en inloggen bevat nooit `password` of de hash; een test controleert de body ([[FTD-restpartijen-webapi-v2_2#GI-3 Authenticatie en rollen — uitvoering Lonneke|GI-3]])
- [ ] De kolom `password_hash` bevat een BCrypt-hash (begint met `$2`); een test leest de rij ([[FTD-restpartijen-webapi-v2_2#16.1 Authenticatie en sessiebeheer|§16.1]])
- [ ] Het gedeelde mechanisme bevat geen regels per endpoint; de reviewers bevestigen dat in de PR ([[FTD-restpartijen-webapi-v2_2#16.2 Autorisatiemodel|§16.2]])
- [ ] `UsersTable` is toegevoegd zonder een bestand in `src/persistence/` te wijzigen: `git diff --stat origin/main` toont geen pad onder `src/persistence/` ([[FTD-restpartijen-webapi-v2_2#GI-1 Persistentielaag — uitvoering Eva|GI-1]])
- [ ] De repository voor `USERS` implementeert `Repository<User>` ([[FTD-restpartijen-webapi-v2_2#11.3 Lonneke van Oers — F3 Prijs, houdbaarheid en statusbewaking|§11.3]])
- [ ] Een verse clone start zonder handmatige stappen volgens het besluit uit fase 4, en er staat geen secret in de repository: `git grep -n "secret" -- resources/` toont alleen de placeholder ([[FTD-restpartijen-webapi-v2_2#GI-2 Applicatie-opzet — uitvoering Stefan|GI-2]])
- [ ] De log van een inlogverzoek bevat het wachtwoord niet ([[FTD-restpartijen-webapi-v2_2#16.7 Logging|§16.7]])
- [ ] De indeling van de `/auth`-endpoints binnen `security` is afgesproken en staat in §21 van het FTD als afgehandeld ([[FTD-restpartijen-webapi-v2_2#GI-3 Authenticatie en rollen — uitvoering Lonneke|GI-3 besluit 3.9]])

---

## Fase 6 — GI-4 Foutafhandeling

Basis: [[FTD-restpartijen-webapi-v2_2#GI-4 Foutafhandeling — uitvoering Stefan|GI-4]], [[FTD-restpartijen-webapi-v2_2#10.2 Validatie en foutafhandeling|§10.2]], [[FTD-restpartijen-webapi-v2_2#ADR-05 — Ktor-plugins en verantwoordelijkheden|ADR-05]]
Raakt: `src/plugins/StatusPagesConfig.kt`, `src/plugins/RequestValidation.kt`, `src/Application.kt`, `test/plugins/*`
Start na: fase 4
Toegewezen: [[FTD-restpartijen-webapi-v2_2-tasks-stefan#Fase 6 — GI-4 Foutafhandeling|Stefan]], [[FTD-restpartijen-webapi-v2_2-tasks-eva#Fase 6 — GI-4 Foutafhandeling|Eva]]

### DoD
- [ ] Elke exceptie uit de tabel in §10.2 geeft de bijbehorende statuscode; er is een integratietest per statuscode ([[FTD-restpartijen-webapi-v2_2#GI-4 Foutafhandeling — uitvoering Stefan|GI-4]])
- [ ] Een onverwachte exceptie geeft `500` met een algemene melding en zonder stacktrace in de body ([[FTD-restpartijen-webapi-v2_2#GI-4 Foutafhandeling — uitvoering Stefan|GI-4]])
- [ ] De stacktrace van die exceptie staat wel in de log ([[FTD-restpartijen-webapi-v2_2#GI-4 Foutafhandeling — uitvoering Stefan|GI-4 besluit 4.4]])
- [ ] Elk foutantwoord heeft de vorm `{ code, message, field? }`; een test controleert de velden ([[FTD-restpartijen-webapi-v2_2#GI-4 Foutafhandeling — uitvoering Stefan|GI-4 besluit 4.3]])
- [ ] Een request body met een onbekende enumwaarde geeft `400` in dezelfde foutvorm ([[FTD-restpartijen-webapi-v2_2#10.2 Validatie en foutafhandeling|§10.2]])
- [ ] `RequestValidation` is geïnstalleerd: `grep -rn "install(RequestValidation)" src/` geeft een regel ([[FTD-restpartijen-webapi-v2_2#ADR-05 — Ktor-plugins en verantwoordelijkheden|ADR-05]])
- [ ] De walking-skeleton-route bouwt geen eigen `404` meer: `grep -n "HttpStatusCode.NotFound" src/product/routes/ProductRoutes.kt` geeft niets, en `GET /api/v1/products/999` geeft nog steeds `404` ([[FTD-restpartijen-webapi-v2_2#10.2 Validatie en foutafhandeling|§10.2]])
- [ ] Als A7 een nieuwe exceptie oplevert: die exceptie geeft `409`, met een test (aanvulling, [[#A7 Exceptie voor 409 bij een bestaand e-mailadres|A7]])

---

## Fase 7 — K-1 en K-6 Contracten van F1 geleverd

Basis: [[FTD-restpartijen-webapi-v2_2#12.1 De koppelvlakken|K-1 en K-6]], [[FTD-restpartijen-webapi-v2_2#8.4 Klassendiagram|§8.4]], [[FTD-restpartijen-webapi-v2_2#9.1 ERD|§9.1]], [[FTD-restpartijen-webapi-v2_2#9.2 Entiteiten|§9.2]], [[FTD-restpartijen-webapi-v2_2#9.3 Vertaling van het objectmodel naar het relationele model|§9.3]], [[FTD-restpartijen-webapi-v2_2#11.1 Eva Bouwman — F1 Aanbod en productdata|§11.1]], [[FTD-restpartijen-webapi-v2_2#8.6 Architectuurbeslissingen|ADR-08]], [[FTD-restpartijen-webapi-v2_2#Bijlage A — Werken met contracten|bijlage A]]
Raakt: `src/product/model/*`, `src/product/repository/*`, `test/product/*`
Start na: fase 3 en fase 5 (`UsersTable`, voor de foreign key van `suppliers`)
Toegewezen: [[FTD-restpartijen-webapi-v2_2-tasks-eva#Fase 7 — K-1 en K-6 Contracten van F1 geleverd|Eva]], [[FTD-restpartijen-webapi-v2_2-tasks-lonneke#Fase 7 — K-1 en K-6 Contracten van F1 geleverd|Lonneke]]

### Gedeelde to-do
- [ ] Stefan en Lonneke passen hun fakes van `ProductReader` aan als de compiler daarom vraagt ([[FTD-restpartijen-webapi-v2_2#12.1 De koppelvlakken|B-26]])
- [ ] Eva en Lonneke spreken af hoe `/auth/login` het `supplierId` krijgt zonder dat `security` F1 importeert (aanvulling, [[#A15 Het supplierId bij inloggen|A15]])

### DoD
- [ ] `SurplusProduct` is een `sealed` abstracte class met `FreshProduct`, `FrozenProduct` en `AmbientProduct`; elke subklasse overschrijft `category` en `maxShelfLife()` ([[FTD-restpartijen-webapi-v2_2#8.4 Klassendiagram|§8.4]])
- [ ] `shelfLifeRemaining(clock)` en `isExpired(clock)` staan alleen in de basisklasse; unittests met een vaste klok zijn groen ([[FTD-restpartijen-webapi-v2_2#8.4 Klassendiagram|§8.4]])
- [ ] `SurplusProduct` implementeert `ProductView` en daarmee `PricedProduct` ([[FTD-restpartijen-webapi-v2_2#12.1 De koppelvlakken|§12.1]])
- [ ] De status heeft een privé setter; buiten F1 verandert hij alleen via `ProductStatusUpdater` ([[FTD-restpartijen-webapi-v2_2#11.1 Eva Bouwman — F1 Aanbod en productdata|§11.1]])
- [ ] De tabellen `surplus_products`, `suppliers` en `product_allergens` hebben alle kolommen uit het ERD ([[FTD-restpartijen-webapi-v2_2#9.1 ERD|§9.1]])
- [ ] Bedragen staan als `long` in `original_price_cents`; tijden via `timestamp()` uit `exposed-kotlin-datetime` ([[FTD-restpartijen-webapi-v2_2#GI-1 Persistentielaag — uitvoering Eva|GI-1 besluit 1.7]])
- [ ] De factory kiest per `category` de juiste subklasse; een test per categorie is groen ([[FTD-restpartijen-webapi-v2_2#9.3 Vertaling van het objectmodel naar het relationele model|§9.3]])
- [ ] De `when` in de factory heeft geen `else`: `grep -n "else ->" src/product/model/ProductFactory.kt` geeft niets ([[FTD-restpartijen-webapi-v2_2#8.4 Klassendiagram|§8.4]])
- [ ] Elke belofte van `ProductReader` in §12.1 heeft een integratietest op H2 (`findById`, `findAvailable`, `findExpiredListings`, `findByStatus`) ([[FTD-restpartijen-webapi-v2_2#12.1 De koppelvlakken|§12.1]])
- [ ] Elke belofte van `ProductStatusUpdater` in §12.1 heeft een integratietest op H2, inclusief `false` bij `markReserved` en `markRemoved` op een partij die niet `LISTED` is ([[FTD-restpartijen-webapi-v2_2#12.1 De koppelvlakken|§12.1]])
- [ ] Twee gelijktijdige aanroepen van `markReserved` op dezelfde partij geven precies één keer `true` ([[FTD-restpartijen-webapi-v2_2#11.1 Eva Bouwman — F1 Aanbod en productdata|§11.1]])
- [ ] Een `ProductView` bevat de aanbieder als `SupplierSummary` met id, naam en coördinaten ([[FTD-restpartijen-webapi-v2_2#Besluiten van de startsessie en daarna (v2.2)|B-20]])
- [ ] `src/product/` importeert geen andere feature: `grep -rn "import com.restpartijen.api.\(reservation\|pricing\)" src/product/` geeft niets ([[FTD-restpartijen-webapi-v2_2#GI-5 Gedeelde domeinkern — geen eigenaar, PR met review door alle drie|GI-5]])
- [ ] `POST /auth/login` met een aanbiedersaccount geeft het `supplierId`; met een afhaler ontbreekt het. `grep -rn "import com.restpartijen.api.product" src/security/` geeft niets ([[FTD-restpartijen-webapi-v2_2#GI-3 Authenticatie en rollen — uitvoering Lonneke|GI-3]])

---

## Fase 8 — US-01 Restpartij plaatsen

Basis: [[FTD-restpartijen-webapi-v2_2#5.1 US-01 — Restpartij plaatsen|§5.1]], [[FTD-restpartijen-webapi-v2_2#SD-1 — Productgegevens opzoeken en restpartij plaatsen (US-01, US-02, US-10)|SD-1]], [[FTD-restpartijen-webapi-v2_2#9.10 Plausibiliteit van de houdbaarheidsdatum per productsoort|§9.10]], [[FTD-restpartijen-webapi-v2_2#8.7 Kwaliteitsscenario's|§8.7]], [[FTD-restpartijen-webapi-v2_2#11.1 Eva Bouwman — F1 Aanbod en productdata|§11.1]], [[FTD-restpartijen-webapi-v2_2#10.2 Validatie en foutafhandeling|§10.2]]
Raakt: `src/product/*`, `test/product/*`, `requests.http`
Start na: fase 1 (omrekening euro's), fase 5, fase 6, fase 7
Toegewezen: [[FTD-restpartijen-webapi-v2_2-tasks-eva#Fase 8 — US-01 Restpartij plaatsen|Eva]]

### DoD
- [ ] Een aanbieder met geldig JWT en rol `SUPPLIER` plaatst een partij: `201 Created`, status `LISTED` (TC-01) ([[FTD-restpartijen-webapi-v2_2#5.1 US-01 — Restpartij plaatsen|§5.1]])
- [ ] Zonder `bestBeforeAt`: `400` en de foutmelding noemt het veld ([[FTD-restpartijen-webapi-v2_2#5.1 US-01 — Restpartij plaatsen|§5.1]])
- [ ] `bestBeforeAt` in het verleden: `422` ([[FTD-restpartijen-webapi-v2_2#5.1 US-01 — Restpartij plaatsen|§5.1]])
- [ ] Een afhaalvenster dat eindigt na `bestBeforeAt`: `422` ([[FTD-restpartijen-webapi-v2_2#5.1 US-01 — Restpartij plaatsen|§5.1]])
- [ ] Geen afhaalvenster en geen openingstijden: `422` ([[FTD-restpartijen-webapi-v2_2#5.1 US-01 — Restpartij plaatsen|§5.1]])
- [ ] `quantity` kleiner dan 1: `422` ([[FTD-restpartijen-webapi-v2_2#5.1 US-01 — Restpartij plaatsen|§5.1]])
- [ ] Per productsoort: een datum precies op de grens van `maxShelfLife` (14 dagen, 1095 dagen, 1825 dagen) is toegestaan; één dag erover geeft `422` met de grens in de foutmelding ([[FTD-restpartijen-webapi-v2_2#9.10 Plausibiliteit van de houdbaarheidsdatum per productsoort|§9.10]])
- [ ] Een allergeen buiten de EU-lijst: `400` ([[FTD-restpartijen-webapi-v2_2#5.1 US-01 — Restpartij plaatsen|§5.1]])
- [ ] Een lege allergenenlijst is toegestaan ([[FTD-restpartijen-webapi-v2_2#11.1 Eva Bouwman — F1 Aanbod en productdata|§11.1]])
- [ ] Rol `COLLECTOR`: `403` ([[FTD-restpartijen-webapi-v2_2#5.1 US-01 — Restpartij plaatsen|§5.1]])
- [ ] Zonder token: `401` ([[FTD-restpartijen-webapi-v2_2#8.7 Kwaliteitsscenario's|§8.7]])
- [ ] De prijs in de request body is in euro's: `3.49` staat na plaatsen als 349 cent in de database ([[FTD-restpartijen-webapi-v2_2#10.1 Endpoints|B-25]])
- [ ] `requests.http` bevat een werkend voorbeeld voor `POST /api/v1/products` ([[FTD-restpartijen-webapi-v2_2#1.7 Succescriteria|§1.7]])
- [ ] De acht punten van §7 zijn afgevinkt in de PR van deze story ([[FTD-restpartijen-webapi-v2_2#7. Definition of Done|§7]])

---

## Fase 9 — US-02 Productgegevens via barcode

Basis: [[FTD-restpartijen-webapi-v2_2#5.2 US-02 — Productgegevens via barcode|§5.2]], [[FTD-restpartijen-webapi-v2_2#SD-1 — Productgegevens opzoeken en restpartij plaatsen (US-01, US-02, US-10)|SD-1]], [[FTD-restpartijen-webapi-v2_2#9.8 Allergenen|§9.8]], [[FTD-restpartijen-webapi-v2_2#10.3 Integratie met Open Food Facts|§10.3]], [[FTD-restpartijen-webapi-v2_2#8.7 Kwaliteitsscenario's|§8.7]]
Raakt: `src/product/service/*`, `src/product/client/*`, `src/product/routes/*`, `module.yaml`, `test/product/*`, `requests.http`
Start na: fase 8
Toegewezen: [[FTD-restpartijen-webapi-v2_2-tasks-eva#Fase 9 — US-02 Productgegevens via barcode|Eva]]

### DoD
- [ ] Opzoeken is een eigen aanroep, `GET /api/v1/products/lookup/{barcode}`; een test met MockK bewijst dat `POST /products` de client niet aanroept ([[FTD-restpartijen-webapi-v2_2#5.2 US-02 — Productgegevens via barcode|§5.2]])
- [ ] Een bestaande barcode geeft `200` met naam en allergenen (TC-02, client vervangen) ([[FTD-restpartijen-webapi-v2_2#5.2 US-02 — Productgegevens via barcode|§5.2]])
- [ ] Een onbekende barcode geeft `404` ([[FTD-restpartijen-webapi-v2_2#5.2 US-02 — Productgegevens via barcode|§5.2]])
- [ ] Geen antwoord binnen drie seconden geeft `200` met een leeg resultaat en een veld dat handmatige invoer nodig is ([[FTD-restpartijen-webapi-v2_2#5.2 US-02 — Productgegevens via barcode|§5.2]])
- [ ] Terwijl Open Food Facts niet antwoordt, lukt `POST /products` gewoon ([[FTD-restpartijen-webapi-v2_2#8.7 Kwaliteitsscenario's|§8.7]])
- [ ] Elke uitgaande aanroep heeft een `User-Agent` met applicatienaam, versie en contactadres; een test controleert de header ([[FTD-restpartijen-webapi-v2_2#5.2 US-02 — Productgegevens via barcode|§5.2]])
- [ ] `AllergenMapper` vertaalt een bekende tag naar de juiste enumwaarde en negeert een onbekende tag met een logregel; beide tests zonder netwerk ([[FTD-restpartijen-webapi-v2_2#9.8 Allergenen|§9.8]])
- [ ] Een tweede opzoekactie met dezelfde barcode roept Open Food Facts niet opnieuw aan (cache); MockK controleert één aanroep ([[FTD-restpartijen-webapi-v2_2#10.3 Integratie met Open Food Facts|§10.3]])
- [ ] Bij overschrijding van de rate limit valt de API terug op handmatige invoer, niet op een fout ([[FTD-restpartijen-webapi-v2_2#10.3 Integratie met Open Food Facts|§10.3]])
- [ ] De veertien tags uit §9.8 zijn met één echte aanroep gecontroleerd; de uitkomst staat in de PR ([[FTD-restpartijen-webapi-v2_2#9.8 Allergenen|§9.8]])
- [ ] Rol `COLLECTOR`: `403`; zonder token: `401` ([[FTD-restpartijen-webapi-v2_2#10.1 Endpoints|§10.1]])
- [ ] `requests.http` bevat een voorbeeld voor de lookup ([[FTD-restpartijen-webapi-v2_2#1.7 Succescriteria|§1.7]])
- [ ] De acht punten van §7 zijn afgevinkt in de PR van deze story ([[FTD-restpartijen-webapi-v2_2#7. Definition of Done|§7]])

---

## Fase 10 — US-03 Eigen aanbod beheren

Basis: [[FTD-restpartijen-webapi-v2_2#5.3 US-03 — Eigen aanbod beheren|§5.3]], [[FTD-restpartijen-webapi-v2_2#16.2 Autorisatiemodel|§16.2]], [[FTD-restpartijen-webapi-v2_2#9.5 Statusmachine|§9.5]], [[FTD-restpartijen-webapi-v2_2#11.1 Eva Bouwman — F1 Aanbod en productdata|§11.1]], [[FTD-restpartijen-webapi-v2_2#18.4 Request-flow tussen de app en de API|§18.4]]
Raakt: `src/product/*`, `test/product/*`, `requests.http`
Start na: fase 8
Toegewezen: [[FTD-restpartijen-webapi-v2_2-tasks-eva#Fase 10 — US-03 Eigen aanbod beheren|Eva]]

### DoD
- [ ] Een aanbieder wijzigt een eigen partij: `200` met de bijgewerkte partij (TC-03), binnen de regels uit A13 ([[FTD-restpartijen-webapi-v2_2#5.3 US-03 — Eigen aanbod beheren|§5.3]])
- [ ] Een partij van een andere aanbieder wijzigen: `403` via `OwnershipGuard` ([[FTD-restpartijen-webapi-v2_2#5.3 US-03 — Eigen aanbod beheren|§5.3]])
- [ ] Een eigen partij verwijderen: `204`; de rij staat nog in de database met status `REMOVED` ([[FTD-restpartijen-webapi-v2_2#5.3 US-03 — Eigen aanbod beheren|§5.3]])
- [ ] Verwijderen van een partij met status `RESERVED`, `COLLECTED`, `EXPIRED` of `REMOVED`: `409` ([[FTD-restpartijen-webapi-v2_2#5.3 US-03 — Eigen aanbod beheren|§5.3]])
- [ ] Een niet-bestaande partij: `404` bij `PUT` en `DELETE` ([[FTD-restpartijen-webapi-v2_2#5.3 US-03 — Eigen aanbod beheren|§5.3]])
- [ ] Een partij met status `REMOVED` geeft `404` bij `GET /api/v1/products/{id}` ([[FTD-restpartijen-webapi-v2_2#5.3 US-03 — Eigen aanbod beheren|§5.3]])
- [ ] Rol `COLLECTOR` bij `PUT` en `DELETE`: `403`; zonder token: `401` ([[FTD-restpartijen-webapi-v2_2#10.1 Endpoints|§10.1]])
- [ ] `GET /api/v1/suppliers/{id}/products` toont het aanbod van één aanbieder zonder `REMOVED`, volgens de criteria uit A4 (aanvulling, [[#A4 Criteria voor GET suppliers products|A4]])
- [ ] `requests.http` bevat voorbeelden voor `PUT`, `DELETE` en `GET /suppliers/{id}/products` ([[FTD-restpartijen-webapi-v2_2#1.7 Succescriteria|§1.7]])
- [ ] De acht punten van §7 zijn afgevinkt in de PR van deze story ([[FTD-restpartijen-webapi-v2_2#7. Definition of Done|§7]])

---

## Fase 11 — US-10 Openingstijden vastleggen

Basis: [[FTD-restpartijen-webapi-v2_2#5.10 US-10 — Openingstijden vastleggen|§5.10]], [[FTD-restpartijen-webapi-v2_2#9.9 Openingstijden en afhaalvenster|§9.9]], [[FTD-restpartijen-webapi-v2_2#4.1 F1 — Aanbod en productdata (Eva Bouwman)|§4.1]], [[FTD-restpartijen-webapi-v2_2#SD-1 — Productgegevens opzoeken en restpartij plaatsen (US-01, US-02, US-10)|SD-1]]
Raakt: `src/product/model/OpeningHours.kt`, `src/product/service/PickupWindowCalculator.kt`, `src/product/repository/*`, `src/product/routes/*`, `test/product/*`, `requests.http`
Start na: fase 8, en het besluit uit de weekcheck (fase 2) dat US-10 doorgaat
Toegewezen: [[FTD-restpartijen-webapi-v2_2-tasks-eva#Fase 11 — US-10 Openingstijden vastleggen|Eva]]

Vervalt US-10 volgens de weekcheck, dan sluit deze fase met alleen het eerste DoD-item.

### DoD
- [ ] Het besluit over US-10 staat in de weekcheck; bij "vervalt" ook in §21 van het FTD ([[FTD-restpartijen-webapi-v2_2#4.1 F1 — Aanbod en productdata (Eva Bouwman)|§4.1]])
- [ ] Een aanbieder legt per weekdag één openings- en sluitingstijd vast: `200` met de opgeslagen tijden (TC-10) ([[FTD-restpartijen-webapi-v2_2#5.10 US-10 — Openingstijden vastleggen|§5.10]])
- [ ] Een sluitingstijd die niet na de openingstijd ligt: `422` ([[FTD-restpartijen-webapi-v2_2#5.10 US-10 — Openingstijden vastleggen|§5.10]])
- [ ] Openingstijden van een andere aanbieder wijzigen: `403` ([[FTD-restpartijen-webapi-v2_2#5.10 US-10 — Openingstijden vastleggen|§5.10]])
- [ ] `GET /suppliers/{id}/opening-hours` werkt zonder token ([[FTD-restpartijen-webapi-v2_2#5.10 US-10 — Openingstijden vastleggen|§5.10]])
- [ ] Zonder afhaalvenster leidt de API het venster af uit de openingstijden ([[FTD-restpartijen-webapi-v2_2#5.1 US-01 — Restpartij plaatsen|§5.1]])
- [ ] Het afgeleide venster begint bij het plaatsen als de aanbieder open is, anders op het eerstvolgende openingsmoment ([[FTD-restpartijen-webapi-v2_2#5.10 US-10 — Openingstijden vastleggen|§5.10]])
- [ ] Het afgeleide venster eindigt op `bestBeforeAt`, of op het laatste sluitingsmoment daarvoor; het voorbeeld uit §9.9 (verloopt 15.00 uur, open tot 18.00 uur) geeft 15.00 uur ([[FTD-restpartijen-webapi-v2_2#9.9 Openingstijden en afhaalvenster|§9.9]])
- [ ] Een dag zonder openingstijden wordt overgeslagen ([[FTD-restpartijen-webapi-v2_2#5.10 US-10 — Openingstijden vastleggen|§5.10]])
- [ ] Geen enkel openingsmoment vóór `bestBeforeAt`: plaatsen geeft `422` ([[FTD-restpartijen-webapi-v2_2#5.10 US-10 — Openingstijden vastleggen|§5.10]])
- [ ] Een opgegeven afhaalvenster gaat voor op het afgeleide ([[FTD-restpartijen-webapi-v2_2#9.9 Openingstijden en afhaalvenster|§9.9]])
- [ ] Een latere wijziging van openingstijden verandert `pickup_from` en `pickup_until` van bestaande partijen niet ([[FTD-restpartijen-webapi-v2_2#9.9 Openingstijden en afhaalvenster|§9.9]])
- [ ] De calculator rekent in `Europe/Amsterdam`; een test over de overgang naar wintertijd (25 oktober 2026) is groen ([[FTD-restpartijen-webapi-v2_2#9.9 Openingstijden en afhaalvenster|§9.9]])
- [ ] `PickupWindowCalculator` is een pure functie: geen repository en geen Ktor-import in het bestand ([[FTD-restpartijen-webapi-v2_2#8.4 Klassendiagram|§8.4]])
- [ ] Rol `COLLECTOR` bij `PUT`: `403`; zonder token: `401` ([[FTD-restpartijen-webapi-v2_2#10.1 Endpoints|§10.1]])
- [ ] De acht punten van §7 zijn afgevinkt in de PR van deze story ([[FTD-restpartijen-webapi-v2_2#7. Definition of Done|§7]])

---

## Fase 12 — US-04 Aanbod zoeken en filteren

Basis: [[FTD-restpartijen-webapi-v2_2#5.4 US-04 — Aanbod zoeken en filteren|§5.4]], [[FTD-restpartijen-webapi-v2_2#SD-2 — Zoeken en reserveren met autorisatie (US-04, US-05)|SD-2]], [[FTD-restpartijen-webapi-v2_2#5.7 US-07 — Actuele prijs zien|§5.7]], [[FTD-restpartijen-webapi-v2_2#11.2 Stefan Pellikaan — F2 Zoeken en reserveren|§11.2]], [[FTD-restpartijen-webapi-v2_2#18.3 Wat de API nu al biedt voor periode 2|§18.3]]
Raakt: `src/reservation/service/ProductSearchService.kt`, `src/reservation/model/SearchCriteria.kt`, `src/reservation/routes/*`, `src/reservation/dto/*`, `test/reservation/*`, `requests.http`
Start na: fase 6 en fase 7 (voor de integratietest); unittests kunnen eerder met fakes
Toegewezen: [[FTD-restpartijen-webapi-v2_2-tasks-stefan#Fase 12 — US-04 Aanbod zoeken en filteren|Stefan]]

### DoD
- [ ] Zonder filters: alleen partijen met status `LISTED` die nog houdbaar zijn, oplopend gesorteerd op resterende houdbaarheid ([[FTD-restpartijen-webapi-v2_2#5.4 US-04 — Aanbod zoeken en filteren|§5.4]])
- [ ] Een partij die nog `LISTED` heet maar over de datum is, komt niet in het resultaat; de service kijkt zelf naar de klok ([[FTD-restpartijen-webapi-v2_2#5.4 US-04 — Aanbod zoeken en filteren|§5.4]])
- [ ] `category=FRESH` geeft alleen die soort (TC-04) ([[FTD-restpartijen-webapi-v2_2#5.4 US-04 — Aanbod zoeken en filteren|§5.4]])
- [ ] `maxPrice` vergelijkt met de afgeprijsde prijs en is een bedrag in euro's ([[FTD-restpartijen-webapi-v2_2#5.4 US-04 — Aanbod zoeken en filteren|§5.4]])
- [ ] `minShelfLifeHours` geeft alleen partijen met minstens die resterende houdbaarheid ([[FTD-restpartijen-webapi-v2_2#5.4 US-04 — Aanbod zoeken en filteren|§5.4]])
- [ ] Meerdere filters werken als EN ([[FTD-restpartijen-webapi-v2_2#5.4 US-04 — Aanbod zoeken en filteren|§5.4]])
- [ ] Een onbekende `category`: `400` met de toegestane waarden in de foutmelding ([[FTD-restpartijen-webapi-v2_2#5.4 US-04 — Aanbod zoeken en filteren|§5.4]])
- [ ] De endpoint werkt zonder token ([[FTD-restpartijen-webapi-v2_2#5.4 US-04 — Aanbod zoeken en filteren|§5.4]])
- [ ] Elke partij in het antwoord heeft de naam en de coördinaten van de aanbieder ([[FTD-restpartijen-webapi-v2_2#5.4 US-04 — Aanbod zoeken en filteren|§5.4]])
- [ ] Elke partij in het antwoord heeft `originalPrice`, `discountPercentage` en `currentPrice`, in euro's ([[FTD-restpartijen-webapi-v2_2#5.7 US-07 — Actuele prijs zien|§5.7]])
- [ ] Zoeken vraagt de prijsopbouw op met `priceBreakdown()`; een partij waarvoor die `null` geeft, ontbreekt in het resultaat (test met een fake die `null` geeft) ([[FTD-restpartijen-webapi-v2_2#SD-2 — Zoeken en reserveren met autorisatie (US-04, US-05)|SD-2]])
- [ ] `Page<T>` is gebruikt volgens het besluit uit A14 (aanvulling, [[#A14 Paginering met Page|A14]])
- [ ] `requests.http` bevat voorbeelden met en zonder filters ([[FTD-restpartijen-webapi-v2_2#1.7 Succescriteria|§1.7]])
- [ ] De acht punten van §7 zijn afgevinkt in de PR van deze story ([[FTD-restpartijen-webapi-v2_2#7. Definition of Done|§7]])

---

## Fase 13 — US-05 Partij reserveren

Basis: [[FTD-restpartijen-webapi-v2_2#5.5 US-05 — Partij reserveren|§5.5]], [[FTD-restpartijen-webapi-v2_2#SD-2 — Zoeken en reserveren met autorisatie (US-04, US-05)|SD-2]], [[FTD-restpartijen-webapi-v2_2#9.5 Statusmachine|§9.5]], [[FTD-restpartijen-webapi-v2_2#9.6 Opslagkeuzes|§9.6]], [[FTD-restpartijen-webapi-v2_2#11.2 Stefan Pellikaan — F2 Zoeken en reserveren|§11.2]], [[FTD-restpartijen-webapi-v2_2#ADR-06 — Staffels in plaats van doorlopende afprijscurves|ADR-06]]
Raakt: `src/reservation/*`, `src/Application.kt` (één regel), `test/reservation/*`, `requests.http`
Start na: fase 5, fase 6 en fase 7
Toegewezen: [[FTD-restpartijen-webapi-v2_2-tasks-stefan#Fase 13 — US-05 Partij reserveren|Stefan]]

### DoD
- [ ] Een afhaler met rol `COLLECTOR` reserveert een `LISTED`-partij: `201` en de partij wordt `RESERVED` (TC-05) ([[FTD-restpartijen-webapi-v2_2#5.5 US-05 — Partij reserveren|§5.5]])
- [ ] Een partij die al `RESERVED` is: `409` ([[FTD-restpartijen-webapi-v2_2#5.5 US-05 — Partij reserveren|§5.5]])
- [ ] Een partij met status `EXPIRED`, of `LISTED` maar over de datum: `409` ([[FTD-restpartijen-webapi-v2_2#5.5 US-05 — Partij reserveren|§5.5]])
- [ ] Een partij die niet bestaat of `REMOVED` is: `404`; er is een aparte test voor `REMOVED` ([[FTD-restpartijen-webapi-v2_2#9.5 Statusmachine|B-32]])
- [ ] Na het einde van het afhaalvenster: `422` ([[FTD-restpartijen-webapi-v2_2#5.5 US-05 — Partij reserveren|§5.5]])
- [ ] Het antwoord bevat het moment waarop de reservering vervalt: 24 uur na reserveren, of het einde van het venster als dat eerder is ([[FTD-restpartijen-webapi-v2_2#5.5 US-05 — Partij reserveren|§5.5]])
- [ ] Twee gelijktijdige reserveringen op dezelfde partij: precies één `201`, de ander `409` ([[FTD-restpartijen-webapi-v2_2#5.5 US-05 — Partij reserveren|§5.5]])
- [ ] Een reservering geldt voor de hele partij; de request body kent geen `quantity` ([[FTD-restpartijen-webapi-v2_2#5.5 US-05 — Partij reserveren|§5.5]])
- [ ] Rol `SUPPLIER`: `403`; zonder token: `401` ([[FTD-restpartijen-webapi-v2_2#5.5 US-05 — Partij reserveren|§5.5]])
- [ ] De prijs van het moment van reserveren staat in `reserved_price_cents` en verandert niet als de partij daarna in een volgende staffel valt ([[FTD-restpartijen-webapi-v2_2#5.5 US-05 — Partij reserveren|§5.5]])
- [ ] `markReserved` en de insert van de reservering vallen in één transactie: faalt de insert, dan is de partij weer `LISTED` ([[FTD-restpartijen-webapi-v2_2#9.6 Opslagkeuzes|§9.6]])
- [ ] `ReservationStateMachine` weigert elke combinatie buiten de tabel in §9.5 met `IllegalStateTransitionException` ([[FTD-restpartijen-webapi-v2_2#9.5 Statusmachine|§9.5]])
- [ ] `ReservationsTable` is toegevoegd zonder een bestand in `src/persistence/` te wijzigen ([[FTD-restpartijen-webapi-v2_2#GI-1 Persistentielaag — uitvoering Eva|GI-1]])
- [ ] `ReservationRepository` implementeert `Repository<Reservation>` ([[FTD-restpartijen-webapi-v2_2#11.2 Stefan Pellikaan — F2 Zoeken en reserveren|§11.2]])
- [ ] `GET /api/v1/reservations` geeft alleen de eigen reserveringen, volgens de criteria uit A3 (aanvulling, [[#A3 Criteria voor GET reservations|A3]])
- [ ] Een antwoord met een reservering bevat geen e-mailadres van de afhaler ([[FTD-restpartijen-webapi-v2_2#15. Privacy by design|§15]])
- [ ] De acht punten van §7 zijn afgevinkt in de PR van deze story ([[FTD-restpartijen-webapi-v2_2#7. Definition of Done|§7]])

---

## Fase 14 — US-06 Reservering intrekken

Basis: [[FTD-restpartijen-webapi-v2_2#5.6 US-06 — Reservering intrekken|§5.6]], [[FTD-restpartijen-webapi-v2_2#9.5 Statusmachine|§9.5]], [[FTD-restpartijen-webapi-v2_2#18.4 Request-flow tussen de app en de API|§18.4]]
Raakt: `src/reservation/*`, `test/reservation/*`, `requests.http`
Start na: fase 13
Toegewezen: [[FTD-restpartijen-webapi-v2_2-tasks-stefan#Fase 14 — US-06 Reservering intrekken|Stefan]]

### DoD
- [ ] Een afhaler trekt een eigen reservering in: `204`, de partij is weer `LISTED` en de reservering `CANCELLED` ([[FTD-restpartijen-webapi-v2_2#5.6 US-06 — Reservering intrekken|§5.6]])
- [ ] De reservering van een ander intrekken: `403` (TC-06) ([[FTD-restpartijen-webapi-v2_2#5.6 US-06 — Reservering intrekken|§5.6]])
- [ ] Een reservering met status `COLLECTED` intrekken: `409` ([[FTD-restpartijen-webapi-v2_2#5.6 US-06 — Reservering intrekken|§5.6]])
- [ ] Rol `SUPPLIER`: `403`; zonder token: `401` ([[FTD-restpartijen-webapi-v2_2#10.1 Endpoints|§10.1]])
- [ ] `requests.http` bevat een voorbeeld ([[FTD-restpartijen-webapi-v2_2#1.7 Succescriteria|§1.7]])
- [ ] De acht punten van §7 zijn afgevinkt in de PR van deze story ([[FTD-restpartijen-webapi-v2_2#7. Definition of Done|§7]])

---

## Fase 15 — US-11 Ophalen bevestigen

Basis: [[FTD-restpartijen-webapi-v2_2#5.11 US-11 — Ophalen bevestigen|§5.11]], [[FTD-restpartijen-webapi-v2_2#9.5 Statusmachine|§9.5]], [[FTD-restpartijen-webapi-v2_2#18.4 Request-flow tussen de app en de API|§18.4]]
Raakt: `src/reservation/*`, `test/reservation/*`, `requests.http`
Start na: fase 13
Toegewezen: [[FTD-restpartijen-webapi-v2_2-tasks-stefan#Fase 15 — US-11 Ophalen bevestigen|Stefan]]

### DoD
- [ ] Een afhaler bevestigt het ophalen van een eigen reservering: `200`, reservering en partij zijn `COLLECTED` en `collectedAt` is gevuld (TC-11) ([[FTD-restpartijen-webapi-v2_2#5.11 US-11 — Ophalen bevestigen|§5.11]])
- [ ] De reservering van een ander bevestigen: `403` ([[FTD-restpartijen-webapi-v2_2#5.11 US-11 — Ophalen bevestigen|§5.11]])
- [ ] Een reservering die al opgehaald, ingetrokken of vervallen is: `409` ([[FTD-restpartijen-webapi-v2_2#5.11 US-11 — Ophalen bevestigen|§5.11]])
- [ ] Bevestigen buiten het afhaalvenster: `422` ([[FTD-restpartijen-webapi-v2_2#5.11 US-11 — Ophalen bevestigen|§5.11]])
- [ ] Rol `SUPPLIER`: `403`; zonder token: `401` ([[FTD-restpartijen-webapi-v2_2#10.1 Endpoints|§10.1]])
- [ ] `requests.http` bevat een voorbeeld ([[FTD-restpartijen-webapi-v2_2#1.7 Succescriteria|§1.7]])
- [ ] De acht punten van §7 zijn afgevinkt in de PR van deze story ([[FTD-restpartijen-webapi-v2_2#7. Definition of Done|§7]])

---

## Fase 16 — US-07 Actuele prijs zien

Basis: [[FTD-restpartijen-webapi-v2_2#5.7 US-07 — Actuele prijs zien|§5.7]], [[FTD-restpartijen-webapi-v2_2#9.4 Afprijsstaffels per productsoort|§9.4]], [[FTD-restpartijen-webapi-v2_2#SD-3 — Productdetail met actuele prijs, polymorfe afprijzing (US-07)|SD-3]], [[FTD-restpartijen-webapi-v2_2#ADR-02 — Afprijzing als aparte policy-hiërarchie|ADR-02]], [[FTD-restpartijen-webapi-v2_2#ADR-06 — Staffels in plaats van doorlopende afprijscurves|ADR-06]], [[FTD-restpartijen-webapi-v2_2#8.6 Architectuurbeslissingen|ADR-08]], [[FTD-restpartijen-webapi-v2_2#11.3 Lonneke van Oers — F3 Prijs, houdbaarheid en statusbewaking|§11.3]], [[FTD-restpartijen-webapi-v2_2#14. Niet-functionele eisen (NFR)|§14]]
Raakt: `src/pricing/*`, `src/shared/PriceBreakdown.kt`, `src/product/service/ProductService.kt`, `src/product/dto/*`, `src/Application.kt` (één regel), `test/pricing/*`, `test/product/*`
Start na: fase 1; het detail met prijs (Eva) start na de `PricingService` van Lonneke
Toegewezen: [[FTD-restpartijen-webapi-v2_2-tasks-lonneke#Fase 16 — US-07 Actuele prijs zien|Lonneke]], [[FTD-restpartijen-webapi-v2_2-tasks-eva#Fase 16 — US-07 Actuele prijs zien|Eva]]

### DoD
- [ ] Het kortingspercentage klopt op elke staffelgrens: vers 48 uur, 24 uur en 12 uur; diepvries 7 dagen en 24 uur; houdbaar 14 en 7 dagen. Telkens precies op de grens (hogere korting) en één seconde erboven (TC-07) ([[FTD-restpartijen-webapi-v2_2#5.7 US-07 — Actuele prijs zien|§5.7]])
- [ ] Het percentage is nooit hoger dan de hoogste staffel van de soort ([[FTD-restpartijen-webapi-v2_2#5.7 US-07 — Actuele prijs zien|§5.7]])
- [ ] De actuele prijs is nooit negatief en nooit hoger dan de oorspronkelijke ([[FTD-restpartijen-webapi-v2_2#5.7 US-07 — Actuele prijs zien|§5.7]])
- [ ] 349 cent met 60% korting geeft 139 cent; eerst rood gezien met de volgorde "eerst delen" ([[FTD-restpartijen-webapi-v2_2#9.4 Afprijsstaffels per productsoort|B-24]])
- [ ] Een verlopen partij geeft `null` bij `currentPrice` en `priceBreakdown` ([[FTD-restpartijen-webapi-v2_2#Besluiten van de startsessie en daarna (v2.2)|B-18]])
- [ ] Twee aanroepen met dezelfde vaste klok geven dezelfde prijs ([[FTD-restpartijen-webapi-v2_2#14. Niet-functionele eisen (NFR)|NFR-06]])
- [ ] `PricingService` krijgt de `Clock` van buiten; `grep -rn "Clock.System" src/pricing/` geeft niets ([[FTD-restpartijen-webapi-v2_2#5.7 US-07 — Actuele prijs zien|§5.7]])
- [ ] `DiscountPolicy` heeft `discountPercentage()` en `maxDiscount()` als default implementatie; elke policy levert alleen `tiers()` ([[FTD-restpartijen-webapi-v2_2#8.4 Klassendiagram|§8.4]])
- [ ] `DiscountPolicyResolver` is een `object` en kiest de policy per categorie ([[FTD-restpartijen-webapi-v2_2#8.4 Klassendiagram|§8.4]])
- [ ] `src/pricing/` importeert geen andere feature en `src/product/` bevat geen afprijsregels: `grep -rn "discount" -i src/product/model/` geeft niets ([[FTD-restpartijen-webapi-v2_2#ADR-02 — Afprijzing als aparte policy-hiërarchie|ADR-02]])
- [ ] De oude namen zijn weg: `grep -rn "discountFactor\|\.factor" src/` geeft niets ([[FTD-restpartijen-webapi-v2_2#11.3 Lonneke van Oers — F3 Prijs, houdbaarheid en statusbewaking|§11.3]])
- [ ] `GET /api/v1/products/{id}` toont `originalPrice`, `discountPercentage` en `currentPrice` in euro's; de integratietest van TC-07 draait tegen deze endpoint ([[FTD-restpartijen-webapi-v2_2#6. Traceability matrix|§6]])
- [ ] Een partij over de datum geeft bij `GET /api/v1/products/{id}` `200` met status `EXPIRED` en zonder prijs ([[FTD-restpartijen-webapi-v2_2#5.7 US-07 — Actuele prijs zien|§5.7]])
- [ ] `ProductService` kent de afprijsregels niet en vraagt de prijs via `PriceProvider` ([[FTD-restpartijen-webapi-v2_2#SD-3 — Productdetail met actuele prijs, polymorfe afprijzing (US-07)|SD-3]])
- [ ] `PricingService` is in `src/Application.kt` geregistreerd als `PriceProvider` ([[FTD-restpartijen-webapi-v2_2#12.1 De koppelvlakken|§12.1]])
- [ ] De acht punten van §7 zijn afgevinkt in de PR van deze story ([[FTD-restpartijen-webapi-v2_2#7. Definition of Done|§7]])

---

## Fase 17 — US-08 Automatische statusbewaking

Basis: [[FTD-restpartijen-webapi-v2_2#5.8 US-08 — Automatische statusbewaking|§5.8]], [[FTD-restpartijen-webapi-v2_2#SD-4 — Automatische statusbewaking (US-08)|SD-4]], [[FTD-restpartijen-webapi-v2_2#9.5 Statusmachine|§9.5]], [[FTD-restpartijen-webapi-v2_2#12.1 De koppelvlakken|K-5 en K-6]], [[FTD-restpartijen-webapi-v2_2#11.3 Lonneke van Oers — F3 Prijs, houdbaarheid en statusbewaking|§11.3]]
Raakt: `src/pricing/service/ExpiryScheduler.kt`, `src/pricing/routes/*`, `src/reservation/service/*` (Stefan), `src/Application.kt` (één regel), `test/pricing/*`, `test/reservation/*`, `requests.http`
Start na: fase 7 (`markExpired`) en fase 13 (reserveringen)
Toegewezen: [[FTD-restpartijen-webapi-v2_2-tasks-lonneke#Fase 17 — US-08 Automatische statusbewaking|Lonneke]], [[FTD-restpartijen-webapi-v2_2-tasks-stefan#Fase 17 — US-08 Automatische statusbewaking|Stefan]]

### DoD
- [ ] Een partij waarvan `bestBeforeAt` verstreken is, wordt `EXPIRED` (TC-08) ([[FTD-restpartijen-webapi-v2_2#5.8 US-08 — Automatische statusbewaking|§5.8]])
- [ ] Een niet-opgehaalde reservering vervalt aan het einde van het afhaalvenster; de partij is weer `LISTED` ([[FTD-restpartijen-webapi-v2_2#5.8 US-08 — Automatische statusbewaking|§5.8]])
- [ ] Een reservering van 24 uur oud vervalt, ook binnen het afhaalvenster ([[FTD-restpartijen-webapi-v2_2#11.2 Stefan Pellikaan — F2 Zoeken en reserveren|§11.2]])
- [ ] Een gereserveerde partij die ook over de datum is, eindigt in dezelfde run op `EXPIRED`: eerst vervallen, dan verlopen ([[FTD-restpartijen-webapi-v2_2#SD-4 — Automatische statusbewaking (US-08)|SD-4]])
- [ ] Een partij met status `COLLECTED` blijft onveranderd ([[FTD-restpartijen-webapi-v2_2#5.8 US-08 — Automatische statusbewaking|§5.8]])
- [ ] Twee runs na elkaar: de tweede meldt nul overgangen ([[FTD-restpartijen-webapi-v2_2#5.8 US-08 — Automatische statusbewaking|§5.8]])
- [ ] `POST /api/v1/admin/maintenance/expire` geeft `200` met de aantallen per overgang ([[FTD-restpartijen-webapi-v2_2#SD-4 — Automatische statusbewaking (US-08)|SD-4]])
- [ ] Zonder token: `401`; een andere rol dan `ADMIN`: `403` ([[FTD-restpartijen-webapi-v2_2#10.1 Endpoints|§10.1]])
- [ ] `lapseOverdue` van F2 laat vervallen via de statusmachine (`LAPSE`) en geeft het aantal ([[FTD-restpartijen-webapi-v2_2#12.1 De koppelvlakken|§12.1]])
- [ ] De statusbewaking draait ook periodiek via `launch`, met het interval uit A8 (aanvulling, [[#A8 Interval van de periodieke statusbewaking|A8]])
- [ ] `ReservationMaintenance` is in `src/Application.kt` geregistreerd ([[FTD-restpartijen-webapi-v2_2#12.1 De koppelvlakken|§12.1]])
- [ ] `requests.http` bevat een voorbeeld ([[FTD-restpartijen-webapi-v2_2#1.7 Succescriteria|§1.7]])
- [ ] De acht punten van §7 zijn afgevinkt in de PR van deze story ([[FTD-restpartijen-webapi-v2_2#7. Definition of Done|§7]])

---

## Fase 18 — US-09 Aanbod verwijderen als beheerder

Basis: [[FTD-restpartijen-webapi-v2_2#5.9 US-09 — Aanbod verwijderen als beheerder|§5.9]], [[FTD-restpartijen-webapi-v2_2#9.5 Statusmachine|§9.5]], [[FTD-restpartijen-webapi-v2_2#12.1 De koppelvlakken|K-5 en K-6]], [[FTD-restpartijen-webapi-v2_2#Besluiten van de startsessie en daarna (v2.2)|B-26]]
Raakt: `src/pricing/service/AdminProductService.kt`, `src/pricing/routes/*`, `src/reservation/service/*` (Stefan), `test/pricing/*`, `test/reservation/*`, `requests.http`
Start na: fase 17
Toegewezen: [[FTD-restpartijen-webapi-v2_2-tasks-lonneke#Fase 18 — US-09 Aanbod verwijderen als beheerder|Lonneke]], [[FTD-restpartijen-webapi-v2_2-tasks-stefan#Fase 18 — US-09 Aanbod verwijderen als beheerder|Stefan]]

### DoD
- [ ] Een `ADMIN` verwijdert elke partij, ongeacht de aanbieder: `204` en status `REMOVED` (TC-09) ([[FTD-restpartijen-webapi-v2_2#5.9 US-09 — Aanbod verwijderen als beheerder|§5.9]])
- [ ] Bij een gereserveerde partij vervalt eerst de reservering en daarna wordt de partij `REMOVED`, in één transactie ([[FTD-restpartijen-webapi-v2_2#5.9 US-09 — Aanbod verwijderen als beheerder|§5.9]])
- [ ] Faalt `markRemoved` na `lapseActiveFor`, dan is de reservering nog `ACTIVE`; een test bewijst de rollback ([[FTD-restpartijen-webapi-v2_2#GI-1 Persistentielaag — uitvoering Eva|GI-1 besluit 1.4]])
- [ ] Een partij met status `COLLECTED`, `EXPIRED` of `REMOVED`: `409` ([[FTD-restpartijen-webapi-v2_2#5.9 US-09 — Aanbod verwijderen als beheerder|§5.9]])
- [ ] Zonder rol `ADMIN`: `403`; zonder token: `401` ([[FTD-restpartijen-webapi-v2_2#5.9 US-09 — Aanbod verwijderen als beheerder|§5.9]])
- [ ] `GET /api/v1/admin/products` geeft een `ADMIN` alle partijen van alle aanbieders: `200` ([[FTD-restpartijen-webapi-v2_2#5.9 US-09 — Aanbod verwijderen als beheerder|§5.9]])
- [ ] Zonder filter bevat het overzicht elke status behalve `REMOVED` ([[FTD-restpartijen-webapi-v2_2#5.9 US-09 — Aanbod verwijderen als beheerder|§5.9]])
- [ ] Met `status` alleen de gevraagde statussen, ook meerdere tegelijk; `status=REMOVED` toont de verwijderde ([[FTD-restpartijen-webapi-v2_2#5.9 US-09 — Aanbod verwijderen als beheerder|§5.9]])
- [ ] Een onbekende waarde voor `status`: `400` met de toegestane waarden ([[FTD-restpartijen-webapi-v2_2#5.9 US-09 — Aanbod verwijderen als beheerder|§5.9]])
- [ ] Het overzicht gebruikt `findByStatus`; `grep -rn "findAll" src/pricing/` geeft niets ([[FTD-restpartijen-webapi-v2_2#Besluiten van de startsessie en daarna (v2.2)|B-26]])
- [ ] `requests.http` bevat voorbeelden voor verwijderen en het overzicht ([[FTD-restpartijen-webapi-v2_2#1.7 Succescriteria|§1.7]])
- [ ] De acht punten van §7 zijn afgevinkt in de PR van deze story ([[FTD-restpartijen-webapi-v2_2#7. Definition of Done|§7]])

---

## Fase 19 — Seeddata compleet

Basis: [[FTD-restpartijen-webapi-v2_2#9.7 Seeddata|§9.7]], [[FTD-restpartijen-webapi-v2_2#12.1 De koppelvlakken|§12.1]], [[FTD-restpartijen-webapi-v2_2#1.7 Succescriteria|§1.7]], [[FTD-restpartijen-webapi-v2_2#Bijlage B — H2 wat het is en hoe wij het gebruiken|bijlage B]]
Raakt: het bestand van `SeedData` (plek gekozen in fase 3), `test/persistence/*`, `requests.http`, `README.md`
Start na: fase 5 (gebruikers en hashing), fase 7, fase 11 (of het besluit dat US-10 vervalt) en fase 13
Toegewezen: [[FTD-restpartijen-webapi-v2_2-tasks-eva#Fase 19 — Seeddata compleet|Eva]]

### DoD
- [ ] Drie aanbieders: een supermarkt, een bakker en een groothandel, elk met eigen locatie en openingstijden ([[FTD-restpartijen-webapi-v2_2#9.7 Seeddata|§9.7]])
- [ ] Minstens vijf partijen per aanbieder; supermarkt en groothandel bieden alle drie de soorten, de bakker vers en houdbaar ([[FTD-restpartijen-webapi-v2_2#9.7 Seeddata|§9.7]])
- [ ] Bij het opstarten valt in elke staffel van elke soort minstens één partij: een test telt tien verschillende combinaties van soort en percentage ([[FTD-restpartijen-webapi-v2_2#9.7 Seeddata|§9.7]])
- [ ] Houdbaarheidsdatums staan relatief ten opzichte van de klok: `grep -n "Instant.parse" <pad van SeedData>` geeft niets ([[FTD-restpartijen-webapi-v2_2#9.7 Seeddata|§9.7]])
- [ ] Minstens één partij per status: `LISTED`, `RESERVED`, `COLLECTED`, `EXPIRED` en `REMOVED` ([[FTD-restpartijen-webapi-v2_2#9.7 Seeddata|§9.7]])
- [ ] Zes accounts: drie aanbieders, twee afhalers en één beheerder, met BCrypt-hash ([[FTD-restpartijen-webapi-v2_2#9.7 Seeddata|§9.7]])
- [ ] Seeden gebeurt alleen bij een lege database; een tweede start met de schakelaar op bestand voegt niets toe ([[FTD-restpartijen-webapi-v2_2#12.1 De koppelvlakken|§12.1]])
- [ ] `requests.http` en `README.md` noemen de demo-accounts ([[FTD-restpartijen-webapi-v2_2#1.7 Succescriteria|§1.7]])

---

## Fase 20 — Integratie, kwaliteit en assessmentvoorbereiding

Basis: [[FTD-restpartijen-webapi-v2_2#1.7 Succescriteria|§1.7]], [[FTD-restpartijen-webapi-v2_2#14. Niet-functionele eisen (NFR)|§14]], [[FTD-restpartijen-webapi-v2_2#8.7 Kwaliteitsscenario's|§8.7]], [[FTD-restpartijen-webapi-v2_2#11.4 Dekking van de beoordeelde concepten|§11.4]], [[FTD-restpartijen-webapi-v2_2#17. Risico's|§17]], [[FTD-restpartijen-webapi-v2_2#Bijlage B — H2 wat het is en hoe wij het gebruiken|bijlage B]], [[FTD-restpartijen-webapi-v2_2#21. Open punten|§21]]
Raakt: `requests.http`, `README.md`, de testrapportage (A2)
Start na: fase 8 tot en met 19
Toegewezen: [[FTD-restpartijen-webapi-v2_2-tasks-eva#Fase 20 — Integratie, kwaliteit en assessmentvoorbereiding|Eva]], [[FTD-restpartijen-webapi-v2_2-tasks-stefan#Fase 20 — Integratie, kwaliteit en assessmentvoorbereiding|Stefan]], [[FTD-restpartijen-webapi-v2_2-tasks-lonneke#Fase 20 — Integratie, kwaliteit en assessmentvoorbereiding|Lonneke]]

### Gedeelde to-do
- [ ] Houd een oefensessie waarin ieder de architectuur, de eigen code, de testaanpak en de eigen keuzes uitlegt; de anderen stellen vragen zoals een assessor ([[FTD-restpartijen-webapi-v2_2#1.7 Succescriteria|§1.7]])
- [ ] Laat in die sessie ieder uitleggen waarom de staffelgrenzen in §9.4 liggen waar ze liggen ([[FTD-restpartijen-webapi-v2_2#21. Open punten|§21]])
- [ ] Loop `requests.http` van boven naar beneden door tegen een verse `./kotlin run` ([[FTD-restpartijen-webapi-v2_2#1.7 Succescriteria|§1.7]])
- [ ] Maak in de testrapportage een tabel met per beschermde endpoint uit §10.1 de drie rolchecks en de naam van elke test ([[FTD-restpartijen-webapi-v2_2#GI-6 Testopzet — uitvoering gezamenlijk in de startsessie|GI-6]])
- [ ] Draai `./kotlin test` met de netwerkverbinding uit, en noteer de uitkomst in de testrapportage ([[FTD-restpartijen-webapi-v2_2#GI-6 Testopzet — uitvoering gezamenlijk in de startsessie|GI-6]])
- [ ] Draai `./kotlin test` twee keer achter elkaar, en noteer beide uitkomsten in de testrapportage (aanvulling, [[#A12 Testvolgorde laten variëren|A12]])
- [ ] Tel per student de unittesten in de eigen feature, en noteer het aantal in de testrapportage ([[FTD-restpartijen-webapi-v2_2#1.7 Succescriteria|§1.7]])

### DoD
- [ ] De servicelaag heeft minstens 80% regeldekking in de coverage-run van IntelliJ; de export staat in de testrapportage ([[FTD-restpartijen-webapi-v2_2#14. Niet-functionele eisen (NFR)|NFR-03]])
- [ ] Twintig gelijktijdige aanroepen op `GET /api/v1/products` over de volledige seeddata geven een p95 onder 300 ms; de meting staat in de testrapportage ([[FTD-restpartijen-webapi-v2_2#8.7 Kwaliteitsscenario's|§8.7]])
- [ ] Alle gerealiseerde user stories zijn te tonen met `requests.http`; bij elke story is het verwachte antwoord gezien ([[FTD-restpartijen-webapi-v2_2#1.7 Succescriteria|§1.7]])
- [ ] Ieder heeft voor elk ✔-concept van de eigen kolom in §11.4 een bestand en regelnummer genoteerd ([[FTD-restpartijen-webapi-v2_2#11.4 Dekking van de beoordeelde concepten|§11.4]])
- [ ] Het demoscript toont de afprijsstaffels, de vastgelegde prijs bij reserveren, het afgeleide afhaalvenster en de twee automatische overgangen ([[FTD-restpartijen-webapi-v2_2#17. Risico's|R-05]])
- [ ] De demo met de schakelaar op bestand laat zien dat een reservering een herstart overleeft ([[FTD-restpartijen-webapi-v2_2#Bijlage B — H2 wat het is en hoe wij het gebruiken|bijlage B]])
- [ ] De opzoekacties voor de demo zijn vooraf gedaan, zodat de cache gevuld is en Open Food Facts niet nodig is ([[FTD-restpartijen-webapi-v2_2#17. Risico's|R-04]])
- [ ] Ieder heeft in de oefensessie de eigen code en keuzes uitgelegd zonder hulp; de anderen bevestigen dat ([[FTD-restpartijen-webapi-v2_2#1.7 Succescriteria|§1.7]])

---

## Fase 21 — Inleveren

Basis: [[FTD-restpartijen-webapi-v2_2#3.2 Randvoorwaarden|§3.2]], [[FTD-restpartijen-webapi-v2_2#12.3 Git-werkwijze|§12.3]], [[FTD-restpartijen-webapi-v2_2#Bijlage B — H2 wat het is en hoe wij het gebruiken|bijlage B]], [[FTD-restpartijen-webapi-v2_2#21. Open punten|§21]]
Raakt: `main`, `README.md`, `FTD-restpartijen-webapi-v2_2.md`
Start na: fase 20
Toegewezen: [[FTD-restpartijen-webapi-v2_2-tasks-eva#Fase 21 — Inleveren|Eva]], [[FTD-restpartijen-webapi-v2_2-tasks-stefan#Fase 21 — Inleveren|Stefan]], [[FTD-restpartijen-webapi-v2_2-tasks-lonneke#Fase 21 — Inleveren|Lonneke]]

### Gedeelde to-do
- [ ] Besluit of bijlage B uit het FTD gaat vóór het inleveren ([[FTD-restpartijen-webapi-v2_2#Bijlage B — H2 wat het is en hoe wij het gebruiken|bijlage B]])
- [ ] Zet de PR-historie klaar als bewijs voor het filmpje over samenwerken met Git ([[FTD-restpartijen-webapi-v2_2#12.3 Git-werkwijze|§12.3]])
- [ ] Werk §21 van het FTD bij: elk open punt is afgehandeld of bewust open gelaten ([[FTD-restpartijen-webapi-v2_2#21. Open punten|§21]])
- [ ] Controleer een verse clone van `main` in een lege map met `./kotlin test` en `./kotlin run` ([[FTD-restpartijen-webapi-v2_2#GI-2 Applicatie-opzet — uitvoering Stefan|GI-2]])
- [ ] Lever in vóór 25 oktober 2026 ([[FTD-restpartijen-webapi-v2_2#3.2 Randvoorwaarden|§3.2]])

### DoD
- [ ] Alle branches met werk voor deel 1 zijn via een PR op `main` gekomen; open PR's zijn gesloten of bewust blijven staan ([[FTD-restpartijen-webapi-v2_2#12.3 Git-werkwijze|§12.3]])
- [ ] Een verse clone van `main` in een lege map: `./kotlin test` is groen en `./kotlin run` start ([[FTD-restpartijen-webapi-v2_2#GI-2 Applicatie-opzet — uitvoering Stefan|GI-2]])
- [ ] `git grep -n -i "nog nakijken" -- src/` geeft niets, en de reviewers vonden geen Nederlands commentaar op `main` ([[FTD-restpartijen-webapi-v2_2#12.3 Git-werkwijze|B-27]])
- [ ] De bevestiging van het inleveren is bewaard ([[FTD-restpartijen-webapi-v2_2#3.2 Randvoorwaarden|§3.2]])

---

## Lijst-DoD

- [ ] Alle vier de CRUD-soorten hebben een integratietest: `POST`, `GET`, `PUT` en `DELETE` op `/products` ([[FTD-restpartijen-webapi-v2_2#1.7 Succescriteria|§1.7]])
- [ ] Ieder heeft minstens drie zinvolle unittesten in de eigen feature, verdeeld over happy flow en edge cases ([[FTD-restpartijen-webapi-v2_2#1.7 Succescriteria|§1.7]])
- [ ] Elke beschermde endpoint uit §10.1 heeft een test zonder token, met de verkeerde rol en met de juiste rol; een tabel in de testrapportage telt ze ([[FTD-restpartijen-webapi-v2_2#GI-6 Testopzet — uitvoering gezamenlijk in de startsessie|GI-6]])
- [ ] De volledige testsuite is groen zonder netwerkverbinding ([[FTD-restpartijen-webapi-v2_2#GI-6 Testopzet — uitvoering gezamenlijk in de startsessie|GI-6]])
- [ ] `./kotlin test` twee keer achter elkaar geeft hetzelfde resultaat ([[FTD-restpartijen-webapi-v2_2#GI-6 Testopzet — uitvoering gezamenlijk in de startsessie|GI-6]])
- [ ] Geen feature importeert een andere feature: `grep -rn "import com.restpartijen.api.\(product\|reservation\|pricing\)" src/` toont alleen imports binnen de eigen package ([[FTD-restpartijen-webapi-v2_2#ADR-01 — Ktor met featuregerichte packages in plaats van laaggerichte|ADR-01]])
- [ ] `Clock.System` staat alleen op de plek waar de dependencies worden geregistreerd: `grep -rn "Clock.System" src/` ([[FTD-restpartijen-webapi-v2_2#GI-6 Testopzet — uitvoering gezamenlijk in de startsessie|GI-6 besluit 6.5]])
- [ ] `grep -rn "receiveNullable" src/` geeft niets ([[FTD-restpartijen-webapi-v2_2#3.2 Randvoorwaarden|§3.2]])
- [ ] Geen zelf samengestelde SQL: `grep -rn "exec(" src/` geeft niets ([[FTD-restpartijen-webapi-v2_2#16.5 Invoervalidatie|§16.5]])
- [ ] Alleen de DSL van Exposed: `grep -rn "LongEntity\|IntEntity" src/` geeft niets ([[FTD-restpartijen-webapi-v2_2#GI-1 Persistentielaag — uitvoering Eva|GI-1 besluit 1.1]])
- [ ] Eén repository per entiteit; de reviewers bevestigen dat bij de laatste PR ([[FTD-restpartijen-webapi-v2_2#GI-1 Persistentielaag — uitvoering Eva|GI-1 besluit 1.3]])
- [ ] Elke wijziging in `shared` ging via een PR met twee reviews; controle via `git log -- src/shared/` en de PR's ([[FTD-restpartijen-webapi-v2_2#GI-5 Gedeelde domeinkern — geen eigenaar, PR met review door alle drie|GI-5]])
- [ ] `settings.kotlin.allWarningsAsErrors` staat nog op `true` in `module.yaml` ([[FTD-restpartijen-webapi-v2_2#14. Niet-functionele eisen (NFR)|NFR-05]])
- [ ] Alle fasen 0 tot en met 21 zijn gesloten ([[FTD-restpartijen-webapi-v2_2#7. Definition of Done|§7]])
- [ ] Elke open vraag hieronder is beantwoord, of bewust open gelaten met een reden ([[FTD-restpartijen-webapi-v2_2#21. Open punten|§21]])

## Dekking

| Element uit het FTD | Fase | Wie |
|---------------------|------|-----|
| [[FTD-restpartijen-webapi-v2_2#Revisiehistorie\|Documentbeheer en revisiehistorie]] | 21 (bijwerken naar de ingeleverde versie) | Eva |
| [[FTD-restpartijen-webapi-v2_2#Besluiten van 19 september 2026 (v2.0)\|Besluiten v2.0]] B-1 tot en met B-12 | via de paragrafen waarin ze verwerkt zijn | — |
| [[FTD-restpartijen-webapi-v2_2#Besluiten van de startsessie en daarna (v2.2)\|Besluiten v2.2]] B-13 tot en met B-23, B-25, B-27 tot en met B-30 | via de paragrafen waarin ze verwerkt zijn | — |
| [[FTD-restpartijen-webapi-v2_2#Besluiten van de startsessie en daarna (v2.2)\|Besluiten v2.2]] B-24, B-31, B-32 bevestigen | 0 | allen |
| [[FTD-restpartijen-webapi-v2_2#Besluiten van de startsessie en daarna (v2.2)\|Besluiten v2.2]] B-26 bevestigen | 1 | allen |
| [[FTD-restpartijen-webapi-v2_2#Wijzigingen in v1.1\|Wijzigingen v1.1]] W-1 tot en met W-39 | via de genoemde paragrafen; geen eigen taak | — |
| [[FTD-restpartijen-webapi-v2_2#1.1 Probleemstelling\|§1.1]], [[FTD-restpartijen-webapi-v2_2#1.2 Doel van de applicatie\|§1.2]], [[FTD-restpartijen-webapi-v2_2#1.4 Belangrijkste functionaliteiten\|§1.4]] | beschrijvend; uitgevoerd via de user stories | — |
| [[FTD-restpartijen-webapi-v2_2#1.3 Doelgroep\|§1.3]] rechten per rol | 5, 8 tot en met 18 (rolchecks per endpoint) | allen |
| [[FTD-restpartijen-webapi-v2_2#1.5 In scope\|§1.5]] in scope | 3, 5, 9, lijst-DoD | allen |
| [[FTD-restpartijen-webapi-v2_2#1.6 Buiten scope\|§1.6]] buiten scope | niet in deze lijst: bewust buiten scope | — |
| [[FTD-restpartijen-webapi-v2_2#1.7 Succescriteria\|§1.7]] succescriteria | 19 (seeddata), 20 (demo, uitleg), lijst-DoD (CRUD, unittesten) | allen |
| [[FTD-restpartijen-webapi-v2_2#2. Betrokkenen en verdeling\|§2]] rollen | verdeling over de persoonlijke lijsten | — |
| [[FTD-restpartijen-webapi-v2_2#3.1 Context\|§3.1]] context | beschrijvend | — |
| [[FTD-restpartijen-webapi-v2_2#3.2 Randvoorwaarden\|§3.2]] versies, Engelse code, Nederlands werkcommentaar, `receive<T?>()`, inleverdatum | 0, 2, 21, lijst-DoD | allen |
| [[FTD-restpartijen-webapi-v2_2#3.3 Ambitieniveau per rubriccriterium\|§3.3]] ambitieniveau | niet als taak: beoordelingsdoel; bewaakt via de weekcheck (fase 2) | allen |
| [[FTD-restpartijen-webapi-v2_2#4.1 F1 — Aanbod en productdata (Eva Bouwman)\|§4.1]], [[FTD-restpartijen-webapi-v2_2#4.2 F2 — Zoeken en reserveren (Stefan Pellikaan)\|§4.2]], [[FTD-restpartijen-webapi-v2_2#4.3 F3 — Prijs, houdbaarheid en statusbewaking (Lonneke van Oers)\|§4.3]] user stories | 8 tot en met 18 | per eigenaar |
| [[FTD-restpartijen-webapi-v2_2#4.4 Use case diagram\|§4.4]] use case diagram | beschrijvend | — |
| [[FTD-restpartijen-webapi-v2_2#5.1 US-01 — Restpartij plaatsen\|§5.1]] US-01 | 8 (criterium over het afgeleide venster: 11) | Eva |
| [[FTD-restpartijen-webapi-v2_2#5.2 US-02 — Productgegevens via barcode\|§5.2]] US-02 | 9 | Eva |
| [[FTD-restpartijen-webapi-v2_2#5.3 US-03 — Eigen aanbod beheren\|§5.3]] US-03 | 10 | Eva |
| [[FTD-restpartijen-webapi-v2_2#5.4 US-04 — Aanbod zoeken en filteren\|§5.4]] US-04 | 12 | Stefan |
| [[FTD-restpartijen-webapi-v2_2#5.5 US-05 — Partij reserveren\|§5.5]] US-05 | 13 | Stefan |
| [[FTD-restpartijen-webapi-v2_2#5.6 US-06 — Reservering intrekken\|§5.6]] US-06 | 14 | Stefan |
| [[FTD-restpartijen-webapi-v2_2#5.7 US-07 — Actuele prijs zien\|§5.7]] US-07 | 16; prijsvelden in `GET /products`: 12 | Lonneke, Eva, Stefan |
| [[FTD-restpartijen-webapi-v2_2#5.8 US-08 — Automatische statusbewaking\|§5.8]] US-08 | 17 | Lonneke, Stefan |
| [[FTD-restpartijen-webapi-v2_2#5.9 US-09 — Aanbod verwijderen als beheerder\|§5.9]] US-09 en beheeroverzicht | 18 | Lonneke, Stefan |
| [[FTD-restpartijen-webapi-v2_2#5.10 US-10 — Openingstijden vastleggen\|§5.10]] US-10 | 11 | Eva |
| [[FTD-restpartijen-webapi-v2_2#5.11 US-11 — Ophalen bevestigen\|§5.11]] US-11; de afspraak aan de balie is geen API-regel | 15 | Stefan |
| [[FTD-restpartijen-webapi-v2_2#6. Traceability matrix\|§6]] traceability (TC-01 tot en met TC-11) | 8 tot en met 18 | per eigenaar |
| [[FTD-restpartijen-webapi-v2_2#7. Definition of Done\|§7]] story-DoD | elke storyfase (PR-sjabloon A1), 2 (AI-logboek) | allen |
| [[FTD-restpartijen-webapi-v2_2#8.1 Context (C4 niveau 1)\|§8.1]], [[FTD-restpartijen-webapi-v2_2#8.2 Containers (C4 niveau 2)\|§8.2]] context en containers | beschrijvend; H2 ingebed in 3 | Eva |
| [[FTD-restpartijen-webapi-v2_2#8.3 Packagestructuur\|§8.3]] packagestructuur en mappen | 1, 3, 7, lijst-DoD | allen |
| [[FTD-restpartijen-webapi-v2_2#8.4 Klassendiagram\|§8.4]] klassendiagram | 3, 7, 13, 16 | per eigenaar |
| [[FTD-restpartijen-webapi-v2_2#SD-1 — Productgegevens opzoeken en restpartij plaatsen (US-01, US-02, US-10)\|SD-1]] | 8, 9, 11 | Eva |
| [[FTD-restpartijen-webapi-v2_2#SD-2 — Zoeken en reserveren met autorisatie (US-04, US-05)\|SD-2]] | 12, 13 | Stefan |
| [[FTD-restpartijen-webapi-v2_2#SD-3 — Productdetail met actuele prijs, polymorfe afprijzing (US-07)\|SD-3]] | 16 | Lonneke, Eva |
| [[FTD-restpartijen-webapi-v2_2#SD-4 — Automatische statusbewaking (US-08)\|SD-4]] | 17 | Lonneke, Stefan |
| [[FTD-restpartijen-webapi-v2_2#8.6 Architectuurbeslissingen\|§8.6]] architectuurbeslissingen | per ADR hieronder | — |
| [[FTD-restpartijen-webapi-v2_2#ADR-01 — Ktor met featuregerichte packages in plaats van laaggerichte\|ADR-01]] | lijst-DoD | allen |
| [[FTD-restpartijen-webapi-v2_2#ADR-02 — Afprijzing als aparte policy-hiërarchie\|ADR-02]] | 16 | Lonneke |
| [[FTD-restpartijen-webapi-v2_2#ADR-03 — Exposed-versie (vervallen in v1.1)\|ADR-03]] (vervallen) | via GI-1 besluiten 1.5 tot en met 1.7: 3, 7 | Eva |
| [[FTD-restpartijen-webapi-v2_2#ADR-04 — Ktor's eigen dependency-injectionplugin\|ADR-04]] | 4 | Stefan |
| [[FTD-restpartijen-webapi-v2_2#ADR-05 — Ktor-plugins en verantwoordelijkheden\|ADR-05]] | 4, 5, 6 | Stefan, Lonneke |
| [[FTD-restpartijen-webapi-v2_2#ADR-06 — Staffels in plaats van doorlopende afprijscurves\|ADR-06]] | 13 (vastgelegde prijs), 16 (staffels) | Stefan, Lonneke |
| [[FTD-restpartijen-webapi-v2_2#8.6 Architectuurbeslissingen\|ADR-08]] | 1 (omrekening), 7 (kolommen), 16 (afronding) | Lonneke, Eva |
| [[FTD-restpartijen-webapi-v2_2#8.7 Kwaliteitsscenario's\|§8.7]] performance | 20 | Stefan |
| [[FTD-restpartijen-webapi-v2_2#8.7 Kwaliteitsscenario's\|§8.7]] beschikbaarheid | 9 | Eva |
| [[FTD-restpartijen-webapi-v2_2#8.7 Kwaliteitsscenario's\|§8.7]] security | 8 | Eva |
| [[FTD-restpartijen-webapi-v2_2#9.1 ERD\|§9.1]] ERD | 5 (`USERS`), 7, 11, 13 | per eigenaar |
| [[FTD-restpartijen-webapi-v2_2#9.2 Entiteiten\|§9.2]] eigenaar per entiteit | 5, 7, 11, 13 | per eigenaar |
| [[FTD-restpartijen-webapi-v2_2#9.3 Vertaling van het objectmodel naar het relationele model\|§9.3]] single table inheritance, `BIGINT` | 7 | Eva |
| [[FTD-restpartijen-webapi-v2_2#9.4 Afprijsstaffels per productsoort\|§9.4]] staffels en afronding | 0, 16 | allen, Lonneke |
| [[FTD-restpartijen-webapi-v2_2#9.5 Statusmachine\|§9.5]] statusmachine en `REMOVED` | 7, 10, 13, 14, 15, 17, 18 | Eva, Stefan, Lonneke |
| [[FTD-restpartijen-webapi-v2_2#9.6 Opslagkeuzes\|§9.6]] opslagkeuzes | 3, 13 | Eva, Stefan |
| [[FTD-restpartijen-webapi-v2_2#9.7 Seeddata\|§9.7]] seeddata | 0 (B-31), 19 | allen, Eva |
| [[FTD-restpartijen-webapi-v2_2#9.8 Allergenen\|§9.8]] allergenen | 1 (enum), 8, 9 | Eva |
| [[FTD-restpartijen-webapi-v2_2#9.9 Openingstijden en afhaalvenster\|§9.9]] openingstijden | 11 | Eva |
| [[FTD-restpartijen-webapi-v2_2#9.10 Plausibiliteit van de houdbaarheidsdatum per productsoort\|§9.10]] plausibiliteit | 8 | Eva |
| [[FTD-restpartijen-webapi-v2_2#10.1 Endpoints\|§10.1]] endpoints en bedragen in euro's | 1, 5, 8 tot en met 18 | allen |
| [[FTD-restpartijen-webapi-v2_2#10.2 Validatie en foutafhandeling\|§10.2]] validatie en foutafhandeling | 6 | Stefan |
| [[FTD-restpartijen-webapi-v2_2#10.3 Integratie met Open Food Facts\|§10.3]] Open Food Facts | 9 | Eva |
| [[FTD-restpartijen-webapi-v2_2#11.1 Eva Bouwman — F1 Aanbod en productdata\|§11.1]] Eva | 3, 7 tot en met 11, 16, 19, 20 | Eva |
| [[FTD-restpartijen-webapi-v2_2#11.2 Stefan Pellikaan — F2 Zoeken en reserveren\|§11.2]] Stefan | 4, 6, 12 tot en met 15, 17, 18, 20 | Stefan |
| [[FTD-restpartijen-webapi-v2_2#11.3 Lonneke van Oers — F3 Prijs, houdbaarheid en statusbewaking\|§11.3]] Lonneke, inclusief de opmerking v2.2 | 1, 5, 16, 17, 18, 20 | Lonneke |
| [[FTD-restpartijen-webapi-v2_2#11.4 Dekking van de beoordeelde concepten\|§11.4]] concepten | 20 | allen |
| [[FTD-restpartijen-webapi-v2_2#12.1 De koppelvlakken\|§12.1]] K-1 tot en met K-6, `suspend`, uitzondering `SeedData` | 1, 7, 12, 13, 16, 17, 18, 19 | allen |
| [[FTD-restpartijen-webapi-v2_2#12.1 De koppelvlakken\|§12.1]] opmerking v2.2 over `findByStatus` | 1, 7, 18 | allen |
| [[FTD-restpartijen-webapi-v2_2#12.2 Wat bewust gedeeld blijft\|§12.2]] gedeeld eigendom | 1, lijst-DoD | allen |
| [[FTD-restpartijen-webapi-v2_2#12.3 Git-werkwijze\|§12.3]] Git-werkwijze | 2, 21 | allen |
| [[FTD-restpartijen-webapi-v2_2#13. Gedeelde infrastructuur (GI-1 t/m GI-6)\|§13]] startsessie | 1 | allen |
| [[FTD-restpartijen-webapi-v2_2#GI-1 Persistentielaag — uitvoering Eva\|GI-1]] besluiten 1.1 tot en met 1.8 en criteria | 3, 5, 7, 13, lijst-DoD | Eva |
| [[FTD-restpartijen-webapi-v2_2#GI-2 Applicatie-opzet — uitvoering Stefan\|GI-2]] besluiten 2.1 tot en met 2.7 en criteria | 0, 4, 5, 21 | Stefan |
| [[FTD-restpartijen-webapi-v2_2#GI-3 Authenticatie en rollen — uitvoering Lonneke\|GI-3]] besluiten 3.1 tot en met 3.9 en criteria | 5, 7 (`supplierId`), lijst-DoD | Lonneke |
| [[FTD-restpartijen-webapi-v2_2#GI-4 Foutafhandeling — uitvoering Stefan\|GI-4]] besluiten 4.1 tot en met 4.4 en criteria | 6 | Stefan |
| [[FTD-restpartijen-webapi-v2_2#GI-5 Gedeelde domeinkern — geen eigenaar, PR met review door alle drie\|GI-5]] besluiten 5.1 tot en met 5.4 en criteria | 1, 7, lijst-DoD | allen |
| [[FTD-restpartijen-webapi-v2_2#GI-6 Testopzet — uitvoering gezamenlijk in de startsessie\|GI-6]] besluiten 6.1 tot en met 6.7 en criteria | 1, 3, lijst-DoD | allen |
| [[FTD-restpartijen-webapi-v2_2#14. Niet-functionele eisen (NFR)\|§14]] NFR-03 | 20 | Lonneke |
| [[FTD-restpartijen-webapi-v2_2#14. Niet-functionele eisen (NFR)\|§14]] NFR-05 | 4, lijst-DoD | Stefan |
| [[FTD-restpartijen-webapi-v2_2#14. Niet-functionele eisen (NFR)\|§14]] NFR-06 | 16 | Lonneke |
| [[FTD-restpartijen-webapi-v2_2#15. Privacy by design\|§15]] privacy | 5 (dataminimalisatie), 13 (geen e-mailadres), 12 (geen locatie van de afhaler) | Lonneke, Stefan |
| [[FTD-restpartijen-webapi-v2_2#16.1 Authenticatie en sessiebeheer\|§16.1]] | 5 | Lonneke |
| [[FTD-restpartijen-webapi-v2_2#16.2 Autorisatiemodel\|§16.2]] | 5, 10 (`OwnershipGuard`), rolchecks per fase | allen |
| [[FTD-restpartijen-webapi-v2_2#16.3 Versleuteling\|§16.3]] versleuteling | niet in deze lijst: bewust niet geïmplementeerd | — |
| [[FTD-restpartijen-webapi-v2_2#16.4 Secrets\|§16.4]] secrets | 4, 5 | Stefan, Lonneke |
| [[FTD-restpartijen-webapi-v2_2#16.5 Invoervalidatie\|§16.5]] invoervalidatie | 6, lijst-DoD | Stefan |
| [[FTD-restpartijen-webapi-v2_2#16.6 Afhankelijkheden\|§16.6]] afhankelijkheden | 4; geen scan: bewust geaccepteerd gat | Stefan |
| [[FTD-restpartijen-webapi-v2_2#16.7 Logging\|§16.7]] logging | 4, 5 | Stefan, Lonneke |
| [[FTD-restpartijen-webapi-v2_2#17. Risico's\|§17]] R-01, R-02, R-07 | 2 | allen |
| [[FTD-restpartijen-webapi-v2_2#17. Risico's\|§17]] R-03 (vervallen) | niet in deze lijst | — |
| [[FTD-restpartijen-webapi-v2_2#17. Risico's\|§17]] R-04 | 9, 20 | Eva |
| [[FTD-restpartijen-webapi-v2_2#17. Risico's\|§17]] R-05 | 20 | Lonneke |
| [[FTD-restpartijen-webapi-v2_2#17. Risico's\|§17]] R-06 | 2, lijst-DoD | allen |
| [[FTD-restpartijen-webapi-v2_2#17. Risico's\|§17]] R-07 walking skeleton | 1 | allen |
| [[FTD-restpartijen-webapi-v2_2#18. Vooruitblik periode 2\|§18]] vooruitblik periode 2 | alleen wat de API nu al moet bieden; zie de regels hieronder | — |
| [[FTD-restpartijen-webapi-v2_2#18.1 Sensoren\|§18.1]], [[FTD-restpartijen-webapi-v2_2#18.2 Navigatiestructuur\|§18.2]] sensoren, kaart, navigatie | niet in deze lijst: periode 2 | — |
| [[FTD-restpartijen-webapi-v2_2#18.3 Wat de API nu al biedt voor periode 2\|§18.3]] wat de API nu biedt | 4 (CORS), 6 (één foutmodel), 12 (coördinaten, openbaar, prijs in partij), 16 | Stefan, Lonneke, Eva |
| [[FTD-restpartijen-webapi-v2_2#18.3 Wat de API nu al biedt voor periode 2\|§18.3]] chart library | niet in deze lijst: besluit bij de start van periode 2 | — |
| [[FTD-restpartijen-webapi-v2_2#18.4 Request-flow tussen de app en de API\|§18.4]] request-flow | 5 (login, `401`), 13 (`GET /reservations`), 14 (`204`) | Lonneke, Stefan |
| [[FTD-restpartijen-webapi-v2_2#19. Begrippenlijst\|§19]] begrippen | Begrippen | — |
| [[FTD-restpartijen-webapi-v2_2#20. Terugkoppeling van de vakdocent\|§20]] terugkoppeling docent | 1 (gezamenlijke opzet), 5 (optie B), kaart en chart: periode 2 | allen |
| [[FTD-restpartijen-webapi-v2_2#21. Open punten\|§21]] open punten | 0, 3, 4, 5, 21, Open vragen | allen |
| [[FTD-restpartijen-webapi-v2_2#Bronnen\|Bronnen]] | niet in deze lijst: geen taak | — |
| [[FTD-restpartijen-webapi-v2_2#Bijlage A — Werken met contracten\|bijlage A]] werken met contracten | 0 (lezen), 7, 12 | allen |
| [[FTD-restpartijen-webapi-v2_2#Bijlage B — H2 wat het is en hoe wij het gebruiken\|bijlage B]] H2 | 0 (lezen), 3, 20, 21 | Eva, allen |

## Open vragen

1. **Welke endpoint verzorgt Lonneke in de app?** Account aanmaken of het prijsverloop. Niet in deze lijst: het besluit hoort bij periode 2 ([[FTD-restpartijen-webapi-v2_2#21. Open punten|§21]]).
2. **Blijft `/auth/register` bij Lonneke als zij het prijsverloop kiest?** Tot het besluit houdt deze lijst de endpoint bij haar ([[FTD-restpartijen-webapi-v2_2#21. Open punten|§21]]).
3. **Indeling van de `/auth`-endpoints binnen `security`.** Lonneke stelt haar voor in fase 5 ([[FTD-restpartijen-webapi-v2_2#21. Open punten|§21]]).
4. **Starten zonder handmatige stappen tegenover het secret uit een omgevingsvariabele.** Besluit van Stefan en Lonneke in fase 4 ([[FTD-restpartijen-webapi-v2_2#21. Open punten|§21]]).
5. **Testrapportage en loadtool.** Besluit in fase 0 ([[FTD-restpartijen-webapi-v2_2#21. Open punten|§21]], [[#A2 Vaste plek voor AI-logboek, testrapportage en weekcheck|A2]]).
6. **Is `runTest` beschikbaar via de huidige test-dependencies?** Stefan controleert het in fase 0 ([[FTD-restpartijen-webapi-v2_2#21. Open punten|§21]]).
7. **Bevestigen van B-24, B-31 en B-32** in fase 0; **B-26** in de PR van fase 1 ([[FTD-restpartijen-webapi-v2_2#21. Open punten|§21]]).
8. **409 bij een bestaand e-mailadres** wijkt af van de exceptietabel in §10.2 ([[#A7 Exceptie voor 409 bij een bestaand e-mailadres|A7]]).
9. **Toolchain 0.12.2 of 0.12.0, en de versiecatalogus.** De repository wijkt af van het FTD ([[#A9 Repository wijkt af van het FTD|A9]]).
10. **Welke statussen toont `GET /suppliers/{id}/products`?** ([[#A4 Criteria voor GET suppliers products|A4]])
11. **Criteria voor `GET /reservations`.** ([[#A3 Criteria voor GET reservations|A3]])
12. **HTTP-client en contactadres voor Open Food Facts.** ([[#A5 HTTP-client en User-Agent voor Open Food Facts|A5]])
13. **BCrypt-library.** ([[#A6 Library voor BCrypt|A6]])
14. **Interval van de periodieke statusbewaking.** ([[#A8 Interval van de periodieke statusbewaking|A8]])
15. **Validatieregels bij registreren.** ([[#A11 Validatieregels bij registreren|A11]])
16. **Willekeurige testvolgorde.** De toolchain documenteert geen manier ([[#A12 Testvolgorde laten variëren|A12]]).
17. **Welke velden van een partij zijn te wijzigen, en in welke status?** ([[#A13 Wijzigen van een partij|A13]])
18. **Paginering met `Page<T>`.** ([[#A14 Paginering met Page|A14]])
19. **Is `Repository<T>` `suspend`?** B-21 geldt voor contracten; §8.4 toont de functions zonder. Eva besluit in fase 3 ([[FTD-restpartijen-webapi-v2_2#12.1 De koppelvlakken|B-21]]).
20. **Waar staat `SeedData`?** `SeedData` schrijft in alle tabellen, maar de persistentielaag mag geen feature importeren. Eva besluit in fase 3 ([[FTD-restpartijen-webapi-v2_2#21. Open punten|§21]]).
21. **Oude taaknummers.** Verwijzingen naar T2, T9, T15, T21 en "blok 4" vervallen ([[#A10 Oude taaknummers in FTD en code|A10]]).
22. **Hoe en wat leveren we in op 25 oktober?** Het FTD noemt de datum, niet de vorm ([[FTD-restpartijen-webapi-v2_2#3.2 Randvoorwaarden|§3.2]]).
23. **PR-sjabloon.** Gaat de groep akkoord met het sjabloon voor de story-DoD en het bewijs? ([[#A1 PR-sjabloon met story-DoD en bewijs|A1]])
24. **Hoe krijgt `/auth/login` het `supplierId`?** `security` mag F1 niet importeren en §12.1 heeft er geen contract voor ([[#A15 Het supplierId bij inloggen|A15]]).

---
type: takenlijst
title: "Takenlijst restpartijen Web API — Eva (F1)"
description: "Eva's eigen to-do's bij FTD v2.2: GI-1 persistentielaag, de contracten van F1, US-01, US-02, US-03, US-10, het productdetail en de seeddata."
tags: [takenlijst, proftaak, ktor, kotlin, lu1, f1]
timestamp: 2026-09-29T00:00:00
---

# Takenlijst restpartijen Web API — Eva (F1)

Status, DoD en gedeelde to-do's staan in de [[FTD-restpartijen-webapi-v2_2-tasks|algemene lijst]]. Hier staan alleen jouw eigen to-do's, in de volgorde waarin je ze doet. Elk item linkt naar het FTD; een item `Start na:` is een overdracht van een ander.

Jouw werkvolgorde: GI-1 met de walking skeleton, dan de contracten van F1, dan US-01 en US-03, dan US-02, en US-10 alleen als er tijd over is.

## Fase 0 — Voorbereiding en verificatie

Status en DoD: [[FTD-restpartijen-webapi-v2_2-tasks#Fase 0 — Voorbereiding en verificatie|algemene lijst]]

### Voordat je begint
- [ ] Controleer dat IntelliJ IDEA 2026.2.1 of nieuwer is, met de plugin Kotlin Toolchain (Help → About; Settings → Plugins) ([[FTD-restpartijen-webapi-v2_2#3.2 Randvoorwaarden|§3.2]])
- [ ] Draai de testen op de branch van de startsessie ([[FTD-restpartijen-webapi-v2_2#GI-2 Applicatie-opzet — uitvoering Stefan|GI-2]])
      ```bash
      # Get the walking-skeleton branch and run all tests with the Kotlin Toolchain
      git fetch origin && git switch feature/walking-skeleton && ./kotlin test
      ```
      Verwacht: alle testen groen, onder andere `ProductRoutesTest` en `FixedClockTest`.
      Als het misgaat: de eerste run downloadt de toolchain en de JDK. Faalt die download, controleer dan je netwerk en probeer het opnieuw.

### To-do
- [ ] Maak `.github/pull_request_template.md` op `feature/walking-skeleton`, met de acht punten van §7 als checkboxes, een checkbox "Geen Nederlands werkcommentaar meer in de code" en een kopje "Bewijs" (aanvulling, [[FTD-restpartijen-webapi-v2_2-tasks#A1 PR-sjabloon met story-DoD en bewijs|A1]])
- [ ] Zoek de oude taaknummers in de code op (aanvulling, [[FTD-restpartijen-webapi-v2_2-tasks#A10 Oude taaknummers in FTD en code|A10]])
      ```bash
      # Find references to task numbers of the earlier list (T2, T9, T15, T21, "blok 4")
      grep -rnw -e 'T[0-9]\{1,2\}' -e 'blok 4' src/ test/
      ```
      Verwacht: onder andere `../server/src`, `../server/src`, `../server/src` en `../server/src`.
- [ ] Vervang elke gevonden verwijzing door het fasenummer uit deze lijst, bijvoorbeeld "T15" door "fase 6" (aanvulling, [[FTD-restpartijen-webapi-v2_2-tasks#A10 Oude taaknummers in FTD en code|A10]])
- [ ] Vervang in §9.7 en §21 van het FTD "T21" en "T9" door de fasen 19 en 3 (aanvulling, [[FTD-restpartijen-webapi-v2_2-tasks#A10 Oude taaknummers in FTD en code|A10]])
- [ ] Zet na de bevestiging in de groep de kolom *Status* bij B-24, B-31 en B-32 op "Vast" ([[FTD-restpartijen-webapi-v2_2#Besluiten van de startsessie en daarna (v2.2)|Besluiten v2.2]])
- [ ] Zet de afgesproken paden van het AI-logboek, de testrapportage en de weekcheck in `README.md` (aanvulling, [[FTD-restpartijen-webapi-v2_2-tasks#A2 Vaste plek voor AI-logboek, testrapportage en weekcheck|A2]])
- [ ] Noteer de besluiten over A7 en A9 in §21 van het FTD (aanvulling, [[FTD-restpartijen-webapi-v2_2-tasks#A9 Repository wijkt af van het FTD|A9]])

## Fase 1 — GI-5 en GI-6 Gedeelde domeinkern en testopzet op main

Status en DoD: [[FTD-restpartijen-webapi-v2_2-tasks#Fase 1 — GI-5 en GI-6 Gedeelde domeinkern en testopzet op main|algemene lijst]]

### To-do
- [ ] Haal in `../server/src` de vier Nederlandse commentaarregels boven `findByStatus` weg; de voorbeelden staan al in de KDoc of komen er in het Engels bij ([[FTD-restpartijen-webapi-v2_2#12.3 Git-werkwijze|B-27]])
- [ ] Open een PR van `feature/walking-skeleton` naar `main`, vul het sjabloon in en vraag Stefan en Lonneke als reviewer ([[FTD-restpartijen-webapi-v2_2#13. Gedeelde infrastructuur (GI-1 t/m GI-6)|§13]])
- [ ] Merge de PR pas na twee goedkeuringen, omdat hij `shared` bevat ([[FTD-restpartijen-webapi-v2_2#GI-5 Gedeelde domeinkern — geen eigenaar, PR met review door alle drie|GI-5]])

## Fase 3 — GI-1 Persistentielaag

Status en DoD: [[FTD-restpartijen-webapi-v2_2-tasks#Fase 3 — GI-1 Persistentielaag|algemene lijst]]

### Voordat je begint
- [ ] Maak een branch vanaf de nieuwste `main` ([[FTD-restpartijen-webapi-v2_2#12.3 Git-werkwijze|B-28]])
      ```bash
      # Start the GI-1 branch from the latest main (branch pattern shared/<name>-<part>)
      git fetch origin && git switch -c shared/eva-persistence origin/main
      ```

### To-do
- [ ] Maak `../server/src` met de generieke interface `Repository<T>`: `findById(id: Long): T?`, `findAll(): List<T>`, `save(entity: T): T` en `delete(id: Long): Boolean`, met KDoc ([[FTD-restpartijen-webapi-v2_2#GI-1 Persistentielaag — uitvoering Eva|GI-1 besluit 1.8]])
- [ ] Besluit of deze functions `suspend` zijn en schrijf de reden in de KDoc. B-21 geldt voor contracten die de database raken; §8.4 toont `Repository<T>` zonder `suspend` ([[FTD-restpartijen-webapi-v2_2#12.1 De koppelvlakken|B-21]])
- [ ] Voeg in `../server/src` een `suspend`-helper toe die `suspendTransaction` binnen `withContext(Dispatchers.IO)` uitvoert, bijvoorbeeld `dbQuery { }` ([[FTD-restpartijen-webapi-v2_2#GI-1 Persistentielaag — uitvoering Eva|GI-1 besluit 1.6]])
- [ ] Laat `ExposedProductRepository.findById` in `../server/src` de helper gebruiken in plaats van `transaction { }` ([[FTD-restpartijen-webapi-v2_2#GI-1 Persistentielaag — uitvoering Eva|GI-1 besluit 1.6]])
      Als het misgaat: de testen in `../server/test` compileren niet meer, omdat ze nu een `suspend`-function aanroepen. Zet de body van de test in `runTest { }` of `runBlocking { }`, afhankelijk van de uitkomst van de `runTest`-controle in fase 0.
- [ ] Verander `DatabaseFactory.init()` zo dat hij de tabellen als parameter krijgt, en haal de import van `ProductsTable` weg ([[FTD-restpartijen-webapi-v2_2#21. Open punten|§21]])
- [ ] Geef in `../server/src` `ProductsTable` mee aan `DatabaseFactory.init()`, in één regel ([[FTD-restpartijen-webapi-v2_2#GI-1 Persistentielaag — uitvoering Eva|GI-1]])
- [ ] Kies de plek van `SeedData`: het bestand schrijft in alle tabellen, maar `persistence` mag geen feature importeren. Een voor de hand liggende plek is naast `../server/src`. Schrijf de keuze in de PR ([[FTD-restpartijen-webapi-v2_2#21. Open punten|§21]])
- [ ] Verplaats de twee walking-skeleton-partijen uit `DatabaseFactory` naar `SeedData` op de gekozen plek ([[FTD-restpartijen-webapi-v2_2#12.1 De koppelvlakken|§12.1]])
- [ ] Voeg in `../server/resources` een instelling toe voor H2: in-memory of op bestand, standaard in-memory ([[FTD-restpartijen-webapi-v2_2#GI-1 Persistentielaag — uitvoering Eva|GI-1 besluit 1.2]])
- [ ] Laat `Application.module()` die instelling lezen en aan `DatabaseFactory.init()` doorgeven; kies op basis daarvan `jdbc:h2:mem:restpartijen;DB_CLOSE_DELAY=-1` of `jdbc:h2:./restpartijen` ([[FTD-restpartijen-webapi-v2_2#Bijlage B — H2 wat het is en hoe wij het gebruiken|bijlage B]])
- [ ] Maak in `../server/test` een helper die alle tabellen leegmaakt vóór een test ([[FTD-restpartijen-webapi-v2_2#GI-6 Testopzet — uitvoering gezamenlijk in de startsessie|GI-6 besluit 6.6]])
- [ ] Schrijf `../server/test`: test één schrijft een rij, test twee ziet die rij niet. Schrijf deze eerste test zelf, volgens Arrange-Act-Assert ([[FTD-restpartijen-webapi-v2_2#GI-1 Persistentielaag — uitvoering Eva|GI-1]])
- [ ] Draai de testen ([[FTD-restpartijen-webapi-v2_2#GI-6 Testopzet — uitvoering gezamenlijk in de startsessie|GI-6]])
      ```bash
      # Run all tests
      ./kotlin test
      ```
      Verwacht: alles groen.
- [ ] Start de API in-memory en controleer dat er geen databasebestand ontstaat ([[FTD-restpartijen-webapi-v2_2#GI-1 Persistentielaag — uitvoering Eva|GI-1 besluit 1.2]])
      ```bash
      # Start the API; stop it with Ctrl+C once it logs that it is listening
      ./kotlin run
      # In a second terminal: expect no output
      ls *.mv.db 2>/dev/null
      ```
- [ ] Zet de instelling op bestand, start, stop en start opnieuw; `GET /api/v1/products/1` in `requests.http` geeft beide keren `200` ([[FTD-restpartijen-webapi-v2_2#Bijlage B — H2 wat het is en hoe wij het gebruiken|bijlage B]])
- [ ] Zet de instelling terug op in-memory en verwijder `restpartijen.mv.db` ([[FTD-restpartijen-webapi-v2_2#GI-1 Persistentielaag — uitvoering Eva|GI-1 besluit 1.2]])
- [ ] Open de PR naar `main` en vraag Stefan en Lonneke de KDoc van `Repository<T>` te bevestigen ([[FTD-restpartijen-webapi-v2_2#GI-1 Persistentielaag — uitvoering Eva|GI-1 besluit 1.8]])

## Fase 6 — GI-4 Foutafhandeling

Status en DoD: [[FTD-restpartijen-webapi-v2_2-tasks#Fase 6 — GI-4 Foutafhandeling|algemene lijst]]

### To-do
- [ ] Start na: [[FTD-restpartijen-webapi-v2_2-tasks-stefan#Fase 6 — GI-4 Foutafhandeling|Stefan — `StatusPages` staat op `main`]]
- [ ] Laat `ProductService.getProduct` een `NotFoundException` gooien als de partij niet bestaat, in plaats van `null` terug te geven ([[FTD-restpartijen-webapi-v2_2#10.2 Validatie en foutafhandeling|§10.2]])
- [ ] Haal in `../server/src` het eigen antwoord `HttpStatusCode.NotFound` weg; `StatusPages` maakt nu de `404` ([[FTD-restpartijen-webapi-v2_2#GI-4 Foutafhandeling — uitvoering Stefan|GI-4 besluit 4.1]])
- [ ] Gooi in dezelfde route een `ValidationException` als het id geen getal is, in plaats van zelf `BadRequest` te sturen ([[FTD-restpartijen-webapi-v2_2#10.2 Validatie en foutafhandeling|§10.2]])
- [ ] Pas `ProductServiceTest` aan: de test voor een onbekend id verwacht nu de exceptie met `assertFailsWith<NotFoundException>` ([[FTD-restpartijen-webapi-v2_2#10.2 Validatie en foutafhandeling|§10.2]])
- [ ] Draai `./kotlin test`; `ProductRoutesTest` blijft groen, dus `GET /api/v1/products/999` geeft nog steeds `404` ([[FTD-restpartijen-webapi-v2_2#10.2 Validatie en foutafhandeling|§10.2]])

## Fase 7 — K-1 en K-6 Contracten van F1 geleverd

Status en DoD: [[FTD-restpartijen-webapi-v2_2-tasks#Fase 7 — K-1 en K-6 Contracten van F1 geleverd|algemene lijst]]

### Voordat je begint
- [ ] Start na: [[FTD-restpartijen-webapi-v2_2-tasks-lonneke#Fase 5 — GI-3 Authenticatie en rollen|Lonneke — `UsersTable` staat op `main`]]
- [ ] Maak je featurebranch vanaf de nieuwste `main` ([[FTD-restpartijen-webapi-v2_2#12.3 Git-werkwijze|§12.3]])
      ```bash
      # One long-lived branch for F1; merge main into it daily
      git fetch origin && git switch -c feature/f1-aanbod origin/main
      ```

### To-do
- [ ] Maak `../server/src`: een `sealed` abstracte class met de velden uit het klassendiagram, die `ProductView` implementeert ([[FTD-restpartijen-webapi-v2_2#8.4 Klassendiagram|§8.4]])
- [ ] Geef `status` een privé setter: `var status: ProductStatus` met `private set` ([[FTD-restpartijen-webapi-v2_2#11.1 Eva Bouwman — F1 Aanbod en productdata|§11.1]])
- [ ] Schrijf in de basisklasse `shelfLifeRemaining(clock)` en `isExpired(clock)`; de tijd komt altijd uit de meegegeven `Clock` ([[FTD-restpartijen-webapi-v2_2#GI-6 Testopzet — uitvoering gezamenlijk in de startsessie|GI-6 besluit 6.5]])
- [ ] Maak `FreshProduct`, `FrozenProduct` en `AmbientProduct` in `../server/src`; elke subklasse overschrijft `category` en `maxShelfLife()`: 14, 1095 en 1825 dagen ([[FTD-restpartijen-webapi-v2_2#9.10 Plausibiliteit van de houdbaarheidsdatum per productsoort|§9.10]])
- [ ] Schrijf zelf, volgens Arrange-Act-Assert, de unittest voor `shelfLifeRemaining` met `FixedClock` uit `../server/test` ([[FTD-restpartijen-webapi-v2_2#11.1 Eva Bouwman — F1 Aanbod en productdata|§11.1]])
- [ ] Breid `ProductsTable` in `../server/src` uit met alle kolommen van `SURPLUS_PRODUCTS` uit het ERD; `original_price_cents` is `long`, tijden zijn `timestamp()` ([[FTD-restpartijen-webapi-v2_2#9.1 ERD|§9.1]])
- [ ] Maak `../server/src` voor `suppliers`, met een foreign key `user_id` naar de `UsersTable` van Lonneke ([[FTD-restpartijen-webapi-v2_2#9.1 ERD|§9.1]])
- [ ] Maak `../server/src` voor `product_allergens` ([[FTD-restpartijen-webapi-v2_2#9.1 ERD|§9.1]])
- [ ] Meld de twee nieuwe tabellen aan in `../server/src`, op dezelfde manier als `ProductsTable` ([[FTD-restpartijen-webapi-v2_2#GI-1 Persistentielaag — uitvoering Eva|GI-1]])
- [ ] Maak `ProductFactory` als companion object: `from(row)` kiest met een `when` over `ProductCategory` de subklasse, zonder `else` ([[FTD-restpartijen-webapi-v2_2#9.3 Vertaling van het objectmodel naar het relationele model|§9.3]])
- [ ] Werk de extension function `ResultRow.toProduct()` bij zodat hij `ProductFactory` gebruikt; de omzetting van `Long` naar `Money` is één regel ([[FTD-restpartijen-webapi-v2_2#9.3 Vertaling van het objectmodel naar het relationele model|§9.3]])
- [ ] Verwijder `../server/src`, en vervang het gebruik ervan in `../server/src` en `../server/test` door `SurplusProduct`. De testen blijven, met de nieuwe klasse ([[FTD-restpartijen-webapi-v2_2#8.4 Klassendiagram|§8.4]])
- [ ] Laat de repository van F1 `Repository<SurplusProduct>` implementeren ([[FTD-restpartijen-webapi-v2_2#GI-1 Persistentielaag — uitvoering Eva|GI-1 besluit 1.8]])
- [ ] Maak `../server/src`, die `ProductReader` implementeert volgens de beloftes in §12.1 ([[FTD-restpartijen-webapi-v2_2#12.1 De koppelvlakken|§12.1]])
- [ ] Maak `../server/src`, die `ProductStatusUpdater` implementeert ([[FTD-restpartijen-webapi-v2_2#12.1 De koppelvlakken|§12.1]])
- [ ] Schrijf `markReserved` als één voorwaardelijke update: alleen waar de status nog `LISTED` is. Het aantal gewijzigde rijen bepaalt `true` of `false` ([[FTD-restpartijen-webapi-v2_2#11.1 Eva Bouwman — F1 Aanbod en productdata|§11.1]])
- [ ] Registreer beide implementaties in DI, in `../server/src` ([[FTD-restpartijen-webapi-v2_2#12.1 De koppelvlakken|§12.1]])
- [ ] Schrijf de unittest: de factory kiest per categorie de juiste subklasse ([[FTD-restpartijen-webapi-v2_2#11.1 Eva Bouwman — F1 Aanbod en productdata|§11.1]])
- [ ] Schrijf per belofte van `ProductReader` en `ProductStatusUpdater` een integratietest op H2 in `../server/test` ([[FTD-restpartijen-webapi-v2_2#12.1 De koppelvlakken|§12.1]])
- [ ] Schrijf een test waarin twee coroutines tegelijk `markReserved` aanroepen op dezelfde partij; precies één krijgt `true` ([[FTD-restpartijen-webapi-v2_2#5.5 US-05 — Partij reserveren|§5.5]])
- [ ] Spreek met Lonneke af hoe `/auth/login` het `supplierId` krijgt, en bouw de kant van F1 (aanvulling, [[FTD-restpartijen-webapi-v2_2-tasks#A15 Het supplierId bij inloggen|A15]])
- [ ] Draai `./kotlin test`, open de PR en meld Stefan en Lonneke dat de contracten van F1 op `main` staan zodra hij gemerged is ([[FTD-restpartijen-webapi-v2_2#Bijlage A — Werken met contracten|bijlage A]])

## Fase 8 — US-01 Restpartij plaatsen

Status en DoD: [[FTD-restpartijen-webapi-v2_2-tasks#Fase 8 — US-01 Restpartij plaatsen|algemene lijst]]

### Voordat je begint
- [ ] Start na: [[FTD-restpartijen-webapi-v2_2-tasks-lonneke#Fase 1 — GI-5 en GI-6 Gedeelde domeinkern en testopzet op main|Lonneke — omrekening euro's naar centen staat op `main`]]
- [ ] Start na: [[FTD-restpartijen-webapi-v2_2-tasks-lonneke#Fase 5 — GI-3 Authenticatie en rollen|Lonneke — authenticatie staat op `main`]]
- [ ] Start na: [[FTD-restpartijen-webapi-v2_2-tasks-stefan#Fase 6 — GI-4 Foutafhandeling|Stefan — `StatusPages` staat op `main`]]

### To-do
- [ ] Schrijf `validateForListing(clock)` in `SurplusProduct`: datum in de toekomst, afhaalvenster niet na de datum, aantal minstens 1, en resterende houdbaarheid niet langer dan `maxShelfLife()`. Een overtreding gooit `DomainRuleException`; bij de laatste regel noemt de melding de grens ([[FTD-restpartijen-webapi-v2_2#9.10 Plausibiliteit van de houdbaarheidsdatum per productsoort|§9.10]])
- [ ] Maak een command-object voor het plaatsen, met named en default arguments: `barcode` en het afhaalvenster zijn optioneel ([[FTD-restpartijen-webapi-v2_2#11.1 Eva Bouwman — F1 Aanbod en productdata|§11.1]])
- [ ] Maak in `../server/src` een `@Serializable` request-DTO voor `POST /products`, met de prijs in euro's en de allergenen als `Set<Allergen>` ([[FTD-restpartijen-webapi-v2_2#10.1 Endpoints|B-25]])
- [ ] Reken de prijs om naar `Money` met de gedeelde omrekening van Lonneke; schrijf geen eigen omrekening ([[FTD-restpartijen-webapi-v2_2#10.1 Endpoints|B-25]])
- [ ] Schrijf `ProductService.createProduct(command)`: kies de subklasse, roep `validateForListing` aan en sla op. Zonder afhaalvenster en zonder openingstijden gooit de service `DomainRuleException` ([[FTD-restpartijen-webapi-v2_2#SD-1 — Productgegevens opzoeken en restpartij plaatsen (US-01, US-02, US-10)|SD-1]])
- [ ] Bepaal de aanbieder uit de gebruiker in het token, nooit uit de request body ([[FTD-restpartijen-webapi-v2_2#16.2 Autorisatiemodel|§16.2]])
- [ ] Voeg de route `POST /api/v1/products` toe in `../server/src`, binnen `authenticate`, met de rolregel `SUPPLIER` die je zelf schrijft met het mechanisme van Lonneke; lees de body met `call.receive<T>()` ([[FTD-restpartijen-webapi-v2_2#GI-3 Authenticatie en rollen — uitvoering Lonneke|GI-3 besluit 3.6]])
- [ ] Schrijf de unittests voor `validateForListing`: datum in het verleden, venster na de datum, aantal 0 ([[FTD-restpartijen-webapi-v2_2#11.1 Eva Bouwman — F1 Aanbod en productdata|§11.1]])
- [ ] Schrijf per productsoort twee unittests: precies op de grens van `maxShelfLife` toegestaan, één dag erover `DomainRuleException` met de grens in de melding ([[FTD-restpartijen-webapi-v2_2#9.10 Plausibiliteit van de houdbaarheidsdatum per productsoort|§9.10]])
- [ ] Schrijf de integratietest: de prijs `3.49` in de request body staat na plaatsen als 349 cent in de database ([[FTD-restpartijen-webapi-v2_2#10.1 Endpoints|B-25]])
- [ ] Schrijf de integratietesten met `testApplication`: `201` (TC-01), `400` zonder `bestBeforeAt`, `400` bij een onbekend allergeen, lege allergenenlijst toegestaan, `422` zonder venster en openingstijden, `403` als `COLLECTOR`, `401` zonder token ([[FTD-restpartijen-webapi-v2_2#5.1 US-01 — Restpartij plaatsen|§5.1]])
- [ ] Voeg aan `requests.http` een inlogverzoek als aanbieder toe en een `POST /api/v1/products` die het token gebruikt ([[FTD-restpartijen-webapi-v2_2#1.7 Succescriteria|§1.7]])
- [ ] Noteer het gebruik van AI-tooling voor US-01 in het AI-logboek ([[FTD-restpartijen-webapi-v2_2#7. Definition of Done|§7]])
- [ ] Open de PR van `feature/f1-aanbod` naar `main` en vink het sjabloon af ([[FTD-restpartijen-webapi-v2_2#7. Definition of Done|§7]])

## Fase 9 — US-02 Productgegevens via barcode

Status en DoD: [[FTD-restpartijen-webapi-v2_2-tasks#Fase 9 — US-02 Productgegevens via barcode|algemene lijst]]

### To-do
- [ ] Besluit welke HTTP-client je gebruikt en welk contactadres in de `User-Agent` komt; de client van Ktor sluit aan bij B-15 (aanvulling, [[FTD-restpartijen-webapi-v2_2-tasks#A5 HTTP-client en User-Agent voor Open Food Facts|A5]])
- [ ] Voeg de client toe aan `../server/module.yaml`. Neem voor de header-test ook de test-dependency voor een nep-engine mee, zoals `ktor-client-mock` (aanvulling, [[FTD-restpartijen-webapi-v2_2-tasks#A5 HTTP-client en User-Agent voor Open Food Facts|A5]])
      ```bash
      # After editing module.yaml: build and test to check the new dependencies resolve
      ./kotlin test
      ```
- [ ] Maak `../server/src` met `suspend fun fetchProduct(barcode)`, die `GET https://world.openfoodfacts.org/api/v2/product/{barcode}.json` aanroept ([[FTD-restpartijen-webapi-v2_2#10.3 Integratie met Open Food Facts|§10.3]])
- [ ] Zet een time-out van drie seconden; bij een time-out geeft de client een leeg resultaat ([[FTD-restpartijen-webapi-v2_2#10.3 Integratie met Open Food Facts|§10.3]])
- [ ] Stuur bij elke aanroep de header `User-Agent` mee met applicatienaam, versie en contactadres ([[FTD-restpartijen-webapi-v2_2#5.2 US-02 — Productgegevens via barcode|§5.2]])
- [ ] Bewaar opgehaalde gegevens per barcode in het geheugen, voor de duur van de applicatiesessie ([[FTD-restpartijen-webapi-v2_2#10.3 Integratie met Open Food Facts|§10.3]])
- [ ] Behandel een weigering door de rate limit als "handmatige invoer nodig", niet als fout ([[FTD-restpartijen-webapi-v2_2#10.3 Integratie met Open Food Facts|§10.3]])
- [ ] Maak `../server/src` met `fromTags(tags)`: een pure function die de tags uit de tabel in §9.8 vertaalt en een onbekende tag negeert met een logregel ([[FTD-restpartijen-webapi-v2_2#9.8 Allergenen|§9.8]])
- [ ] Schrijf `ProductService.lookupProduct(barcode)`: naam en allergenen als voorzet, `NotFoundException` bij een onbekende barcode, en een veld voor handmatige invoer bij een leeg resultaat ([[FTD-restpartijen-webapi-v2_2#5.2 US-02 — Productgegevens via barcode|§5.2]])
- [ ] Voeg de route `GET /api/v1/products/lookup/{barcode}` toe met de rolregel `SUPPLIER` ([[FTD-restpartijen-webapi-v2_2#10.1 Endpoints|§10.1]])
- [ ] Controleer de tags met één echte aanroep vanaf je eigen machine, met een product dat allergenen heeft ([[FTD-restpartijen-webapi-v2_2#9.8 Allergenen|§9.8]])
      ```bash
      # Show the allergen tags of one real product (replace <barcode> and the contact address)
      curl -s -H "User-Agent: restpartijen-api/0.1 (<contact>)" \
        "https://world.openfoodfacts.org/api/v2/product/<barcode>.json" | grep -o '"allergens_tags":\[[^]]*\]'
      ```
      Verwacht: tags als `en:milk` en `en:gluten`.
      Als het misgaat: heet een tag anders dan in §9.8, pas dan de mapper en de tabel in het FTD aan.
- [ ] Schrijf de unittests voor `AllergenMapper`: een bekende tag geeft de juiste enumwaarde; een onbekende tag wordt genegeerd ([[FTD-restpartijen-webapi-v2_2#11.1 Eva Bouwman — F1 Aanbod en productdata|§11.1]])
- [ ] Schrijf met MockK de testen voor de service: time-out valt terug op handmatige invoer, onbekende barcode geeft `NotFoundException`, een tweede opzoekactie roept de client niet opnieuw aan ([[FTD-restpartijen-webapi-v2_2#11.1 Eva Bouwman — F1 Aanbod en productdata|§11.1]])
- [ ] Schrijf de test die bewijst dat `createProduct` de client nooit aanroept: `verify(exactly = 0)` ([[FTD-restpartijen-webapi-v2_2#5.2 US-02 — Productgegevens via barcode|§5.2]])
- [ ] Schrijf de test: een weigering door de rate limit geeft een leeg resultaat met "handmatige invoer nodig" ([[FTD-restpartijen-webapi-v2_2#10.3 Integratie met Open Food Facts|§10.3]])
- [ ] Schrijf de test voor de `User-Agent`-header met de nep-engine ([[FTD-restpartijen-webapi-v2_2#5.2 US-02 — Productgegevens via barcode|§5.2]])
- [ ] Schrijf de integratietesten: `200` met voorzet (TC-02, client vervangen), `404`, `403` als `COLLECTOR`, `401` zonder token ([[FTD-restpartijen-webapi-v2_2#5.2 US-02 — Productgegevens via barcode|§5.2]])
- [ ] Voeg een lookup-voorbeeld toe aan `requests.http` ([[FTD-restpartijen-webapi-v2_2#1.7 Succescriteria|§1.7]])
- [ ] Noteer het gebruik van AI-tooling voor US-02 in het AI-logboek ([[FTD-restpartijen-webapi-v2_2#7. Definition of Done|§7]])
- [ ] Open de PR en vink het sjabloon af ([[FTD-restpartijen-webapi-v2_2#7. Definition of Done|§7]])

## Fase 10 — US-03 Eigen aanbod beheren

Status en DoD: [[FTD-restpartijen-webapi-v2_2-tasks#Fase 10 — US-03 Eigen aanbod beheren|algemene lijst]]

### To-do
- [ ] Besluit welke velden een aanbieder mag wijzigen en in welke status; schrijf het besluit in de PR en in §21 van het FTD (aanvulling, [[FTD-restpartijen-webapi-v2_2-tasks#A13 Wijzigen van een partij|A13]])
- [ ] Maak `../server/src`: vergelijk de aanbieder van de partij met de aanbieder van de gebruiker uit het token, en gooi anders `ForbiddenException` ([[FTD-restpartijen-webapi-v2_2#16.2 Autorisatiemodel|§16.2]])
- [ ] Schrijf `ProductService.updateProduct`, met de eigenaarscontrole en de regels uit A13; de prijs komt in euro's binnen ([[FTD-restpartijen-webapi-v2_2#5.3 US-03 — Eigen aanbod beheren|§5.3]])
- [ ] Voeg de route `PUT /api/v1/products/{id}` toe met de rolregel `SUPPLIER` ([[FTD-restpartijen-webapi-v2_2#10.1 Endpoints|§10.1]])
- [ ] Schrijf `ProductService.deleteProduct`: alleen `LISTED` wordt `REMOVED`; een andere status gooit `IllegalStateTransitionException` ([[FTD-restpartijen-webapi-v2_2#5.3 US-03 — Eigen aanbod beheren|§5.3]])
- [ ] Laat `getProduct`, `updateProduct` en `deleteProduct` een `NotFoundException` gooien bij een partij die niet bestaat of `REMOVED` is ([[FTD-restpartijen-webapi-v2_2#9.5 Statusmachine|§9.5]])
- [ ] Voeg de route `DELETE /api/v1/products/{id}` toe met de rolregel `SUPPLIER` ([[FTD-restpartijen-webapi-v2_2#10.1 Endpoints|§10.1]])
- [ ] Besluit welke statussen `GET /suppliers/{id}/products` toont; `REMOVED` in elk geval niet (aanvulling, [[FTD-restpartijen-webapi-v2_2-tasks#A4 Criteria voor GET suppliers products|A4]])
- [ ] Voeg de openbare route `GET /api/v1/suppliers/{id}/products` toe (aanvulling, [[FTD-restpartijen-webapi-v2_2-tasks#A4 Criteria voor GET suppliers products|A4]])
- [ ] Schrijf de integratietesten: wijzigen `200` (TC-03), partij van een ander `403`, verwijderen `204` met status `REMOVED` in de database, gereserveerde partij verwijderen `409`, niet-bestaande partij `404`, `REMOVED` bij `GET /products/{id}` `404` ([[FTD-restpartijen-webapi-v2_2#11.1 Eva Bouwman — F1 Aanbod en productdata|§11.1]])
- [ ] Schrijf voor `PUT` en `DELETE` de rolchecks: `403` als `COLLECTOR`, `401` zonder token ([[FTD-restpartijen-webapi-v2_2#GI-6 Testopzet — uitvoering gezamenlijk in de startsessie|GI-6]])
- [ ] Voeg voorbeelden voor `PUT`, `DELETE` en `GET /suppliers/{id}/products` toe aan `requests.http` ([[FTD-restpartijen-webapi-v2_2#1.7 Succescriteria|§1.7]])
- [ ] Noteer het gebruik van AI-tooling voor US-03 in het AI-logboek ([[FTD-restpartijen-webapi-v2_2#7. Definition of Done|§7]])
- [ ] Open de PR en vink het sjabloon af ([[FTD-restpartijen-webapi-v2_2#7. Definition of Done|§7]])

## Fase 11 — US-10 Openingstijden vastleggen

Status en DoD: [[FTD-restpartijen-webapi-v2_2-tasks#Fase 11 — US-10 Openingstijden vastleggen|algemene lijst]]

### Voordat je begint
- [ ] Controleer in de weekcheck dat US-10 doorgaat; vervalt de story, noteer dat dan in §21 van het FTD en sla deze fase over ([[FTD-restpartijen-webapi-v2_2#4.1 F1 — Aanbod en productdata (Eva Bouwman)|§4.1]])

### To-do
- [ ] Maak `../server/src`: een data class met `DayOfWeek`, `opensAt` en `closesAt` als `LocalTime` ([[FTD-restpartijen-webapi-v2_2#8.4 Klassendiagram|§8.4]])
- [ ] Maak `../server/src` voor `supplier_opening_hours`, met `supplier_id` en `day_of_week` samen als primary key, en meld hem aan in `../server/src` ([[FTD-restpartijen-webapi-v2_2#9.1 ERD|§9.1]])
- [ ] Schrijf in de service: een sluitingstijd die niet na de openingstijd ligt, gooit `DomainRuleException` ([[FTD-restpartijen-webapi-v2_2#5.10 US-10 — Openingstijden vastleggen|§5.10]])
- [ ] Voeg `PUT /api/v1/suppliers/{id}/opening-hours` toe met de rolregel `SUPPLIER` en `OwnershipGuard` ([[FTD-restpartijen-webapi-v2_2#5.10 US-10 — Openingstijden vastleggen|§5.10]])
- [ ] Voeg de openbare route `GET /api/v1/suppliers/{id}/opening-hours` toe ([[FTD-restpartijen-webapi-v2_2#5.10 US-10 — Openingstijden vastleggen|§5.10]])
- [ ] Maak `../server/src` met `defaultWindow(hours, now, bestBeforeAt): PickupWindow?`, zonder repository en zonder Ktor, met de tijdzone `Europe/Amsterdam` via kotlinx-datetime ([[FTD-restpartijen-webapi-v2_2#9.9 Openingstijden en afhaalvenster|§9.9]])
- [ ] Roep de calculator aan in `createProduct` als er geen afhaalvenster is opgegeven; `null` wordt `DomainRuleException` ([[FTD-restpartijen-webapi-v2_2#SD-1 — Productgegevens opzoeken en restpartij plaatsen (US-01, US-02, US-10)|SD-1]])
- [ ] Schrijf de unittests voor de calculator: start nu als de aanbieder open is, anders het eerstvolgende openingsmoment; einde op `bestBeforeAt` als de aanbieder dan open is; een gesloten dag wordt overgeslagen; geen openingsmoment vóór de datum geeft `null` ([[FTD-restpartijen-webapi-v2_2#11.1 Eva Bouwman — F1 Aanbod en productdata|§11.1]])
- [ ] Schrijf de unittest met het voorbeeld uit §9.9: verloopt om 15.00 uur, open tot 18.00 uur, venster eindigt om 15.00 uur ([[FTD-restpartijen-webapi-v2_2#9.9 Openingstijden en afhaalvenster|§9.9]])
- [ ] Schrijf een unittest over de nacht van 25 oktober 2026, de overgang naar wintertijd ([[FTD-restpartijen-webapi-v2_2#9.9 Openingstijden en afhaalvenster|§9.9]])
- [ ] Schrijf de integratietesten: `200` met opgeslagen tijden (TC-10), `422` bij sluiten vóór openen, `403` bij een andere aanbieder, `GET` zonder token, opgegeven venster gaat voor, latere wijziging raakt bestaande partijen niet, `401` zonder token, `403` als `COLLECTOR` ([[FTD-restpartijen-webapi-v2_2#5.10 US-10 — Openingstijden vastleggen|§5.10]])
- [ ] Voeg voorbeelden toe aan `requests.http` ([[FTD-restpartijen-webapi-v2_2#1.7 Succescriteria|§1.7]])
- [ ] Noteer het gebruik van AI-tooling voor US-10 in het AI-logboek ([[FTD-restpartijen-webapi-v2_2#7. Definition of Done|§7]])
- [ ] Open de PR en vink het sjabloon af ([[FTD-restpartijen-webapi-v2_2#7. Definition of Done|§7]])

## Fase 16 — US-07 Actuele prijs zien

Status en DoD: [[FTD-restpartijen-webapi-v2_2-tasks#Fase 16 — US-07 Actuele prijs zien|algemene lijst]]

### To-do
- [ ] Laat `ProductService` een `PriceProvider` krijgen via de constructor; test met een fake zolang de echte er niet is ([[FTD-restpartijen-webapi-v2_2#Bijlage A — Werken met contracten|bijlage A]])
- [ ] Laat `getProduct` de prijsopbouw opvragen met `priceBreakdown()`; bij `null` geeft het detail status `EXPIRED` en geen prijs ([[FTD-restpartijen-webapi-v2_2#SD-3 — Productdetail met actuele prijs, polymorfe afprijzing (US-07)|SD-3]])
- [ ] Breid `ProductResponse` in `../server/src` uit met de aanbieder, `originalPrice`, `discountPercentage` en `currentPrice`, de bedragen in euro's via de gedeelde omrekening ([[FTD-restpartijen-webapi-v2_2#5.7 US-07 — Actuele prijs zien|§5.7]])
- [ ] Schrijf de unittests met een fake `PriceProvider`: prijsvelden gevuld; bij `null` status `EXPIRED` zonder prijs ([[FTD-restpartijen-webapi-v2_2#5.7 US-07 — Actuele prijs zien|§5.7]])
- [ ] Start na: [[FTD-restpartijen-webapi-v2_2-tasks-lonneke#Fase 16 — US-07 Actuele prijs zien|Lonneke — `PricingService` staat op `main`]]
- [ ] Review de integratietest van TC-07 van Lonneke tegen `GET /api/v1/products/{id}` ([[FTD-restpartijen-webapi-v2_2#6. Traceability matrix|§6]])
- [ ] Open de PR en vink het sjabloon af ([[FTD-restpartijen-webapi-v2_2#7. Definition of Done|§7]])

## Fase 19 — Seeddata compleet

Status en DoD: [[FTD-restpartijen-webapi-v2_2-tasks#Fase 19 — Seeddata compleet|algemene lijst]]

### Voordat je begint
- [ ] Start na: [[FTD-restpartijen-webapi-v2_2-tasks-stefan#Fase 13 — US-05 Partij reserveren|Stefan — `ReservationsTable` staat op `main`]]
- [ ] Start na: [[FTD-restpartijen-webapi-v2_2-tasks-lonneke#Fase 5 — GI-3 Authenticatie en rollen|Lonneke — het hashen van wachtwoorden staat op `main`]]

### To-do
- [ ] Schrijf in `SeedData` zes accounts: drie aanbieders, twee afhalers en één beheerder, met een BCrypt-hash via de hasher van Lonneke ([[FTD-restpartijen-webapi-v2_2#9.7 Seeddata|§9.7]])
- [ ] Schrijf drie aanbieders: een supermarkt, een bakker en een groothandel, elk met eigen coördinaten en openingstijden ([[FTD-restpartijen-webapi-v2_2#9.7 Seeddata|§9.7]])
- [ ] Schrijf per aanbieder minstens vijf partijen; supermarkt en groothandel alle drie de soorten, de bakker vers en houdbaar ([[FTD-restpartijen-webapi-v2_2#9.7 Seeddata|§9.7]])
- [ ] Reken elke datum uit vanaf de klok, bijvoorbeeld `now + 6.hours`; nooit een vaste datum ([[FTD-restpartijen-webapi-v2_2#9.7 Seeddata|§9.7]])
- [ ] Kies de datums zo dat elke staffel van §9.4 minstens één partij heeft: vier bij vers, drie bij diepvries, drie bij houdbaar ([[FTD-restpartijen-webapi-v2_2#9.4 Afprijsstaffels per productsoort|§9.4]])
- [ ] Zorg voor minstens één partij per status; `RESERVED` en `COLLECTED` krijgen een rij in `reservations` ([[FTD-restpartijen-webapi-v2_2#9.7 Seeddata|§9.7]])
- [ ] Laat `SeedData` alleen draaien als de database leeg is ([[FTD-restpartijen-webapi-v2_2#12.1 De koppelvlakken|§12.1]])
- [ ] Controleer dat met de schakelaar op bestand een tweede start niets toevoegt: tel de partijen met `GET /api/v1/admin/products` vóór en na de herstart, en zet de schakelaar daarna terug ([[FTD-restpartijen-webapi-v2_2#GI-1 Persistentielaag — uitvoering Eva|GI-1 besluit 1.2]])
- [ ] Schrijf de test "seeddata bevat alle statussen" ([[FTD-restpartijen-webapi-v2_2#11.1 Eva Bouwman — F1 Aanbod en productdata|§11.1]])
- [ ] Schrijf de test die per soort de kortingspercentages van de seeddata telt; verwacht tien combinaties ([[FTD-restpartijen-webapi-v2_2#9.7 Seeddata|§9.7]])
- [ ] Zet de demo-accounts in `requests.http` en `README.md` ([[FTD-restpartijen-webapi-v2_2#1.7 Succescriteria|§1.7]])
- [ ] Open de PR en vink het sjabloon af ([[FTD-restpartijen-webapi-v2_2#7. Definition of Done|§7]])

## Fase 20 — Integratie, kwaliteit en assessmentvoorbereiding

Status en DoD: [[FTD-restpartijen-webapi-v2_2-tasks#Fase 20 — Integratie, kwaliteit en assessmentvoorbereiding|algemene lijst]]

### To-do
- [ ] Noteer voor elk ✔-concept in jouw kolom van §11.4 een bestand en regelnummer, bijvoorbeeld `SurplusProduct.kt:12` voor de abstracte class ([[FTD-restpartijen-webapi-v2_2#11.4 Dekking van de beoordeelde concepten|§11.4]])
- [ ] Tel je eigen unittesten; het zijn er minstens drie, met happy flow en edge cases ([[FTD-restpartijen-webapi-v2_2#1.7 Succescriteria|§1.7]])
- [ ] Schrijf de stappen op om de cache van Open Food Facts vóór de demo te vullen: welke barcodes, in welke volgorde ([[FTD-restpartijen-webapi-v2_2#17. Risico's|R-04]])
- [ ] Schrijf de stappen voor de demo met de schakelaar op bestand: reservering maken, herstarten, reservering tonen ([[FTD-restpartijen-webapi-v2_2#Bijlage B — H2 wat het is en hoe wij het gebruiken|bijlage B]])
- [ ] Bereid je uitleg voor: architectuur, jouw code, je testaanpak en je keuzes, zoals de hiërarchie van `SurplusProduct` en de contracten ([[FTD-restpartijen-webapi-v2_2#1.7 Succescriteria|§1.7]])

## Fase 21 — Inleveren

Status en DoD: [[FTD-restpartijen-webapi-v2_2-tasks#Fase 21 — Inleveren|algemene lijst]]

### To-do
- [ ] Controleer dat `feature/f1-aanbod` en `shared/eva-persistence` volledig op `main` staan: `git log origin/main..origin/feature/f1-aanbod` geeft niets ([[FTD-restpartijen-webapi-v2_2#12.3 Git-werkwijze|§12.3]])
- [ ] Werk *Documentbeheer* en *Revisiehistorie* van het FTD bij naar de versie die je inlevert ([[FTD-restpartijen-webapi-v2_2#Revisiehistorie|Revisiehistorie]])

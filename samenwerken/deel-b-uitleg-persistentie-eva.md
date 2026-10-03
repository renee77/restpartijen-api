---
onderwerp: antwoorden van Eva op de zes vragen over persistentie uit deel B van sessie 2
tags: [werk-in-uitvoering, proftaak, sessie-2, persistentie, exposed, h2, transacties]
status: actueel
oorsprong: samenwerking-eva-en-claude
aangemaakt: 2026-10-03
gewijzigd: 2026-10-03
gerelateerd:
  - "[[takenlijst-sessie-2-eva]]"
  - "[[voorstel-sessie-2-v2]]"
  - "[[FTD-restpartijen-webapi-v2_2]]"
---

# Deel B — uitleg persistentie (Eva)

## Waar ik aan werk

Phase 6 uit [[takenlijst-sessie-2-eva#Phase 6 — Deel B jouw uitleg voorbereiden|mijn takenlijst]]: per vraag uit [[voorstel-sessie-2-v2#Deel B — Wat ieder moet kunnen uitleggen|deel B]] een kort antwoord in eigen woorden, met de plek in de code waar ik het kan aanwijzen. In blok 3 van de sessie leg ik dit uit aan Stefan en Lonneke. Een assessor kan dezelfde vragen stellen.

De antwoorden staan hieronder. De code staat in `restpartijen-api` op de branch `shared/eva-persistence`.

## Nog te doen

- [x] Per vraag een kort antwoord, met een plek in de code
- [ ] De zes vragen hardop beantwoorden, zonder mee te lezen
- [ ] De vragen van Stefan en Lonneke in deel B doorlezen
- [ ] Testen dat een `dbQuery` binnen een andere `dbQuery` aansluit bij de buitenste (zie vraag 3)
- [ ] Met Stefan bespreken hoe zijn reservering één transactie wordt (zie vraag 3)

---

## 1. Hoe voeg je een tabel toe, en waarom staat de tabel in je eigen featurepackage en niet in `persistence`?

Je maakt een `object` dat erft van `Table`, want je hebt er maar één van nodig. Daarin zet je de kolommen en de primary key. Voorbeeld: `ProductsTable.kt`.

Daarna meld je de tabel aan bij `DatabaseFactory.init` in `Application.module()`. Pas dan maakt de app hem aan bij het opstarten. In een test geef je hem mee aan `createEmptyTestDatabase`. Je repository leest en schrijft in die tabel. `ExposedProductRepository` implementeert `ProductRepository`, en die erft van `Repository<T>`.

De tabel staat in mijn featurepackage, omdat de gegevens van mijn feature zijn. Als hij in `persistence` staat, kan elke feature hem gebruiken. Dat is niet de afspraak: features kennen elkaar niet, alleen de afspraken uit `shared`. Bovendien moet `persistence` dan alle features kennen, terwijl het andersom hoort. En iedereen kan een eigen tabel toevoegen zonder een bestand van een ander aan te passen ([[FTD-restpartijen-webapi-v2_2#GI-1 Persistentielaag — uitvoering Eva|GI-1, acceptatiecriteria]]).

**Aanwijzen:** `ProductsTable.kt`, `Application.kt:29`, en commit `219f7ae` (de import van `ProductsTable` uit `persistence` weggehaald).

## 2. Waarom één repository per entiteit en niet per feature (besluit 1.3)?

Tussen `Repository<T>` en de implementatie staat de interface `ProductRepository`. Daar komen later de queries die alleen voor producten gelden, zoals `findAvailable`. Die horen niet in de generieke `Repository<T>`, want dan moet iedereen ze implementeren. En de service kent alleen `ProductRepository`, dus in een test geef ik hem de fake in plaats van de echte repository.

Eén repository per entiteit betekent: elke tabel heeft precies één eigenaar en één plek waar de SQL staat. Zo is altijd duidelijk wie in welke tabel mag schrijven. Anderen gaan via de contracten in `shared`. Per feature zou dat kunnen, maar de grens wordt dan vaag: een feature heeft vaak gegevens van een ander nodig, en dan schrijf je al snel zelf in diens tabel. Samen met de regel dat features elkaar niet importeren, voorkomt dit dat iemand de regels van een andere feature omzeilt.

**Aanwijzen:** `ProductRepository.kt`, `ExposedProductRepository.kt`, `FakeProductRepository.kt`.

## 3. Waar begint en eindigt een transactie, en waarom daar (besluit 1.4)?

Een transactie is een groep databasehandelingen die samen slagen of samen falen. Hij begint aan het begin van een `dbQuery { … }`-blok. Exposed opent dan een verbinding. Hij eindigt aan het eind van dat blok. Gaat alles goed, dan wordt alles opgeslagen (commit). Gaat er iets mis, dan wordt alles teruggedraaid (rollback). Daarna gaat de verbinding dicht.

Volgens besluit 1.4 hoort een transactie om één service-aanroep heen. De reden is dat een service soms meer dan één tabel wijzigt. Een reservering zet het product op `RESERVED` én maakt een reservering aan. Mislukt de tweede stap, dan moet de eerste ook teruggedraaid worden. Anders staat een product op gereserveerd zonder dat er een reservering is.

Nu zit `dbQuery` nog in elke function van de repository. Dat werkt, omdat mijn service maar één aanroep doet. Zodra een service meer stappen heeft, moet de transactie om de hele service-aanroep heen. De `dbQuery`'s in de repository sluiten dan aan bij die ene transactie. De service krijgt de transactie dan van buiten, zodat de unittests met de fake zonder database blijven werken.

**Aanwijzen:** `DbQuery.kt` (waar de transactie begint), `ExposedProductRepository.kt` (waar hij nu gebruikt wordt), `DbQueryTest.kt` (bewijs dat een fout halverwege alles terugdraait).

### De kern, in drie zinnen

1. **Wat:** een transactie is "alles of niets".
2. **Waar:** van het begin tot het eind van `dbQuery`. Volgens het besluit is dat één service-aanroep.
3. **Waarom daar:** omdat één handeling van een gebruiker, zoals reserveren, soms twee tabellen wijzigt. Die mogen niet half gebeuren.

**Vergelijking:** een overboeking bij de bank. Geld eraf bij jou en geld erbij bij de ander is één transactie. Gaat het tweede mis, dan komt het geld terug op jouw rekening. Bij een reservering is "product op gereserveerd" het geld eraf, en "reservering aanmaken" het geld erbij.

### Meerdere tabellen in één transactie

Het mechanisme: een `dbQuery` binnen een andere `dbQuery` maakt geen nieuwe transactie, maar sluit aan bij de buitenste. De repository-code hoeft daarvoor niet te veranderen.

Schets van Stefans service. De namen zijn bedacht, het gaat om de vorm:

```kotlin
class ReservationService(
    private val productStatusUpdater: ProductStatusUpdater,   // contract from shared, implemented by Eva
    private val reservationRepository: ReservationRepository  // Stefan's own
) {
    suspend fun reserve(productId: Long, collectorId: Long): Reservation = dbQuery {   // outer transaction
        val reserved = productStatusUpdater.markReserved(productId)                    // dbQuery inside: joins
        if (!reserved) throw IllegalStateTransitionException("Product is no longer available")
        reservationRepository.create(Reservation(id = 0, productId, collectorId))     // dbQuery inside: joins
    }
}
```

Wat er gebeurt:

```
dbQuery {                          ← transaction starts
    markReserved(…)                  inner dbQuery: already in a transaction → joins, no commit yet
    reservationRepository.create(…)  inner dbQuery: joins as well
}                                  ← only here: commit, or rollback if anything above threw
```

Faalt `create`, dan gooit hij een exception. De buitenste `dbQuery` draait dan alles terug, ook `markReserved`. Het product staat weer op `LISTED`.

**Wat ik Stefan vertel:** "Zet de hele service-function in één `dbQuery { … }`. Mijn `markReserved` en jouw `create` doen intern ook `dbQuery`, maar die sluiten aan bij jouw buitenste. Gaat er iets mis, dan wordt alles teruggedraaid."

**Hoe zeker is dit?** De Exposed-documentatie zegt dat geneste transacties standaard aansluiten bij de buitenste, en dat `suspendTransaction` zich daarin gedraagt als `transaction` (JetBrains s.r.o., z.j.-a). Of het met mijn `dbQuery`, die er ook nog `withContext` omheen zet, precies zo werkt, is nog niet met een test bevestigd.

## 4. Waarom `suspendTransaction` binnen `withContext(Dispatchers.IO)` (besluit 1.6)?

JDBC blokkeert: de thread die een query uitvoert, staat stil tot de database antwoordt. Ktor handelt verzoeken af met maar een paar threads. Staat zo'n thread stil, dan kan hij geen andere verzoeken helpen. Daarom verplaatst `withContext(Dispatchers.IO)` het databasewerk naar een aparte groep threads die bedoeld is voor wachten. De coroutine van het verzoek pauzeert zolang, en laat zijn thread vrij. `suspendTransaction` zorgt daarbinnen voor de transactie: alles samen opslaan of alles samen terugdraaien. Het is de `suspend`-versie, zodat je hem vanuit een coroutine kunt aanroepen. De oude `newSuspendedTransaction` is in Exposed 1.x vervallen (JetBrains s.r.o., z.j.-b).

**Aanwijzen:** `DbQuery.kt`. In een test is het verschil niet te zien: zonder `Dispatchers.IO` slagen de testen ook. Het verschil merk je pas als veel verzoeken tegelijk binnenkomen.

### Vergelijking: de oven

Je bakt een taart. De oven is de database. De taart erin zetten is de query.

- **JDBC zonder `Dispatchers.IO`:** je zet de taart in de oven en blijft ervoor staan tot hij klaar is. Veertig minuten kun je niets anders doen. Je bent de thread, en je staat stil.
- **Met `withContext(Dispatchers.IO)`:** je vraagt een huisgenoot om bij de oven te blijven. Die is er speciaal voor het wachten: dat zijn de IO-threads. Jij gaat ondertussen de afwas doen, of de deur opendoen, dus andere verzoeken afhandelen.
- **`suspend`:** het moment dat je de oven loslaat. Je pauzeert dat ene klusje, zonder dat jij zelf stilstaat. Roept je huisgenoot "klaar!", dan ga je verder waar je was.
- **`suspendTransaction`:** het recept. De taart en het glazuur horen bij elkaar. Mislukt het glazuur, dan gaat de hele taart weg. Je serveert geen halve taart.

In één zin: "JDBC laat je voor de oven wachten. `Dispatchers.IO` zet iemand anders voor de oven, zodat jij door kunt."

## 5. Wat is het verschil tussen de twee standen van H2, en waarom in-memory voor demo en testen (bijlage B)?

H2 is in beide standen dezelfde database: dezelfde tabellen, dezelfde repositories, dezelfde transacties. Het enige verschil is waar de gegevens staan. Dat bepaalt één instelling, de URL. In-memory staat alles in het werkgeheugen. Stopt de app, dan is alles weg. Op bestand staat de database op schijf, en blijft de inhoud bewaard na een herstart. In-memory betekent dus niet dat er geen database is, maar dat de database niets onthoudt als de app stopt.

Voor de demo gebruiken we in-memory, omdat de seeddata rekent met datums vanaf het moment van vullen. Zo zit er bij elke start een partij in elke afprijsstaffel, en kunnen we de afprijzing laten zien. Een bestand dat een week eerder is gevuld, bevat op de dag zelf alleen nog verlopen partijen.

Voor testen geldt hetzelfde, maar nog strenger. Elke test krijgt een eigen lege database, zodat testen elkaar niet beïnvloeden.

De stand op bestand hebben we om te laten zien dat gegevens een herstart kunnen overleven, als de assessor daarom vraagt. Je zet hem aan met de omgevingsvariabele `DB_MODE=file`.

**Aanwijzen:**
- `DatabaseFactory.kt`: de `when` die per stand de URL kiest.
- `TestDatabase.kt`: een lege database per test.
- `DatabaseFactoryTest.kt`: het bewijs dat een rij een herstart overleeft.

**Let op:** de seeddata bestaat nog niet. Die staat niet meer in `DatabaseFactory`, en `SeedData` moet nog geschreven worden. Dit antwoord beschrijft het ontwerp.

## 6. Hoe vertaalt het ontwerp drie productsoorten naar één tabel (§9.3)?

De drie productsoorten hebben dezelfde gegevens en verschillen alleen in gedrag, zoals hun afprijsstaffels. Daarom staan ze in één tabel, `SURPLUS_PRODUCTS`. Dat heet single table inheritance. De kolom `category` zegt welke soort een rij is. Bij het lezen kiest een factory op basis van die kolom de juiste Kotlin-klasse. Het alternatief, één tabel per soort, zou drie tabellen met dezelfde kolommen opleveren, en elke zoekopdracht over alle soorten zou moeten combineren. Het nadeel van één tabel: krijgt één soort later een eigen veld, bijvoorbeeld een vriestemperatuur, dan blijft die kolom leeg bij de andere twee.

**Aanwijzen:** `ProductsTable.kt`, de kolom `category` met zijn commentaar. De subklassen en de factory komen in F1 T2.

---

## Bronnen

JetBrains s.r.o. (z.j.-a). *Transactions*. Exposed. https://www.jetbrains.com/help/exposed/transactions.html

JetBrains s.r.o. (z.j.-b). *Migration Guide 1.0.0*. Exposed. https://www.jetbrains.com/help/exposed/migration-guide-1-0-0.html

Bronnen uit de eigen werkmap: [[FTD-restpartijen-webapi-v2_2]], besluiten 1.3, 1.4 en 1.6 in GI-1, §9.3 en bijlage B.

De vergelijkingen met de bank en de oven, de schets van de reserveringsservice en de afweging bij vraag 2 komen van Claude, niet uit een bron.

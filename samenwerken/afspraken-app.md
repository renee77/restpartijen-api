# Afspraken app (periode 2)

Eén overzicht van wat we samen hebben afgesproken voor de app: de besluiten uit het FTD en onze werkafspraken. Het FTD blijft de bron; hier staat de korte versie met de datum, zodat je snel ziet wat geldt.

Stand: 21 november 2026. Bijwerken: één keer per week, na de samenkomst op zaterdag.

Bronnen: het FTD van de app (`docs/FTD-restpartijen-android-app-v2_5.md`, tabel "Besluiten") en `docs/besluiten-ftd-app.md`.

## Besluiten uit het FTD

| # | Onderwerp | Afspraak (kort) | Wie, wanneer |
|---|---|---|---|
| A-01 | Planning | Fase 1 gezamenlijke basis 8–29 nov (18–22 nov persoonlijke ontwikkeling); fase 2 eigen features 30 nov–27 dec; alles af op 10 jan 2027; fase 3 finetunen en film 11–17 jan | Profgroep, 7 nov |
| A-02 | Definition of Done | Een feature is pas af als haar deel in het FTD af is | Profgroep, 7 nov |
| A-03 | Platform | Kotlin Multiplatform met Compose Multiplatform, alleen Android | Vakdocent, 10 nov |
| A-04 | Vorm van het ontwerp | Eigen FTD voor de app, met verwijzingen naar FTD v3 | Profgroep, 7 nov |
| A-05 | Verdeling | Eva: Mijn aanbod, Partij toevoegen. Stefan: Ontdekken, Mijn reserveringen, Reserveringsdetail. Lonneke: Inloggen en registreren, Productdetail | Profgroep, 7 nov |
| A-06 | Sensoren | Camera (Eva), GPS (Stefan), vingerafdruk (Lonneke) | Profgroep, 7 nov |
| A-07 | HTTPS | De API krijgt https (GB-3) | Eva, 5 nov |
| A-08 | Technieken | De technieken uit de lessen, in hun multiplatform-variant | Eva, 5 nov; aangepast 10 nov |
| A-09 | Architectuur | Scherm, ViewModel, repository; unidirectional data flow; geen MVI | Profgroep, 7 nov |
| A-10 | Camera en barcode | CameraX met ML Kit Barcode Scanning (gebundeld) | Profgroep, 7 nov; akkoord vakdocent 8 nov |
| A-11 | Vingerafdruk | AndroidX Biometric; eerst wachtwoord, daarna vingerafdruk | Profgroep, 7 nov; akkoord vakdocent 8 nov |
| A-12 | GPS en kaart | MapLibre Compose met `FusedLocationProviderClient` | Vakdocent, september |
| A-13 | Token bewaren | Versleuteld in DataStore met `datastore-tink`, sleutel in de Android Keystore; geen terugval | Profgroep, 7 nov; aangepast Eva 12 nov |
| A-14 | Modules en bouwtool | Module `shared` plus dunne `android`-app; Kotlin Toolchain in IntelliJ IDEA; terugval ktc-to-gradle | Profgroep, 7 nov; vakdocent 8 nov |
| A-15 | Design patterns | Ieder 2 (1 eigen, 1 in een library). Lonneke: State, Builder. Stefan: Adapter, Facade. Eva: Strategy, Chain of Responsibility | Startsessie, 14 nov |
| A-16 | Wireframes | Uit de user stories, door ieder gecontroleerd; klaar vóór 30 nov | Profgroep, 7 nov |
| A-17 | Testen | Test-driven; unit tests voor ViewModels en repository's, UI-tests op stateless schermen | Eva, 5 nov |
| A-18 | FTD v3 bevroren | Wijzigingen aan de API staan in §12 van het app-FTD | Eva, 7 nov |
| A-19 | Iconen | Android-XML met `painterResource`; Valkyrie alleen als we het pad bewerken | Eva, 12 nov |
| A-20 | Packagenaam | `nl.restpartijen.app` | Startsessie, 14 nov |
| A-21 | Standaardtaal | Engels standaard, Nederlands in `values-nl/` | Startsessie, 14 nov |
| A-22 | Fouten uit een repository | `ApiResult` (`Success`/`Failure`), `ApiError.Http(status, message, field?)` of `ApiError.Network` | Startsessie, 14 nov |
| A-23 | Navigatie | Routes in `core.navigation`; koppeling in `AppNavigation.kt` | Startsessie, 14 nov |
| A-24 | App-model | In `data` van de feature; alleen gedeelde types in `core.model` | Startsessie, 14 nov |
| A-25 | Bedragen | `Long` in centen | Startsessie, 14 nov |
| A-26 | Platformcode | Android-code in `src@android/`, gedeelde code in `src/` | Startsessie, 14 nov |
| A-27 | Inloggen en terug | "Inloggen nodig" als toestand in de UiState; de wachtende actie gaat door na inloggen | Startsessie, 14 nov |
| A-28 | Prijsweergave | Altijd euro; notatie volgt de taal: EN `€2.39`, NL `€ 2,39`; `formatPrice` in `core` (Lonneke) | Startsessie, 14 nov; eigenaar 15 nov |
| A-29 | Koin-modules | Een module per feature, `platformModule` voor Android-code, strenge modus | Eva, 15 nov |
| A-30 | Token en `401` | Eigen plugin `SessionAuthorization`: token alleen naar de eigen host; sessie wissen bij `401` met het huidige token | Eva, 15 nov |

## Wie doet wat (sessie 2-afspraken die blijven gelden)

| Afspraak | Wie | Wanneer |
|---|---|---|
| Een antwoord van de API wordt een `ApiResult` in één functie in `core/network/`. Stefan bouwt die (#104); Eva levert de `HttpClient` en de fout-DTO (#94) | Stefan, Eva | 21 nov (#104) |
| Eva installeert de plugin `SessionAuthorization` van Lonneke in haar `HttpClient`, met `allowedHost` | Eva | 21 nov (PR #117) |
| Eva bouwt de vraag om toestemming voor het lokale netwerk (GB-1) | Eva | 15 nov |
| Lonneke bouwt `formatPrice` | Lonneke | 15 nov |

## Werkafspraken

| Afspraak | Sinds |
|---|---|
| Per pull request een nieuwe branch vanaf een verse `main` (`git switch main`, `git pull`, `git switch -c <naam>`) | 21 nov |
| Raakt een PR `core/`, `App.kt`, `AppModule.kt`, `AppNavigation.kt`, `strings.xml`, `libs.versions.toml`, een `module.yaml`, `project.yaml` of `.github/CODEOWNERS`? Dan reviewen beide anderen; anders één. GitHub vraagt beide reviews automatisch aan via `.github/CODEOWNERS`. GitHub dwingt er één af; de tweede is onze afspraak | 15 nov, uitgebreid 21 nov |
| Wie als tweede in hetzelfde bestand merget (bijvoorbeeld `coreModule`), haalt eerst `main` binnen en lost een conflict daar op | 21 nov |
| Verwijs in een PR naar het issue: `Closes #<nummer>`, of `Deel van #<nummer>` als het issue nog niet af is | 15 nov |
| Het FTD werken we één keer per week bij, na de samenkomst op zaterdag | 21 nov |
| Na een upgrade van de toolchain: `git pull`, `./kotlin clean` en `./kotlin build` (Windows: `.\kotlin.bat`), en de IntelliJ-plugin bijwerken | 21 nov (PR #119) |

## Nog open

| Punt | Wie |
|---|---|
| Heeft de app de `code` uit het foutmodel nodig (O-17)? | Stefan |
| Wat staat er in fase 1 achter `LoginRoute` (draaiboek sessie 2, punt 3)? | Stefan, Lonneke |
| Wie verbergt de tab Mijn aanbod voor een afhaler (punt 6)? | Stefan |
| Is https op de API af vóór sessie 2 (punt 8)? | Lonneke, samen |
| Geeft het scherm de taal mee aan `formatPrice`? | Samen, sessie 2 |

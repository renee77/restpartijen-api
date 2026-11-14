---
onderwerp: besluiten en open punten als invoer voor het FTD van de Android-app
tags: [proftaak, periode-2, android, ftd, besluiten]
status: actueel
oorsprong: samenwerking-eva-en-claude
aangemaakt: 2026-11-05
gewijzigd: 2026-11-14
gerelateerd:
  - "[[context-periode-2]]"
  - "[[voorstel-verdeling-app-periode-2]]"
  - "[[voorstel-opbouw-van-de-app]]"
  - "[[voorstel-libraries-voor-de-sensoren]]"
  - "[[voorstel-opbouw-van-de-module]]"
  - "[[leerpaden-android-en-compose]]"
titel: Besluiten voor het FTD van de Android-app (periode 2)
bijgewerkt: 2026-11-10 (antwoord Paul van 8 nov verwerkt; GB-7 toegevoegd; overstap naar Kotlin Multiplatform)
doel: Invoer voor de skill writing-ftd-documents bij het opstellen van het FTD van de app
bronnen: periode-2/toetsdocument.md, periode-2/lesbeschrijving-p2-apsd, periode-2/context-periode-2.md, einddocumenten-periode-1/FTD-restpartijen-webapi-v3_0.md
---

# Besluiten voor het FTD van de Android-app

Status: **Vast** = besloten. **Open** = nog te bespreken. Alle voorstellen zijn op zaterdag 7 november 2026 besproken door de profgroep (Eva, Stefan, Lonneke).

## Besluiten

| # | Onderwerp | Besluit | Onderbouwing | Status |
|---|---|---|---|---|
| P-1 | Planning | Fase 0 besluiten t/m 7 nov, daarna FTD; fase 1 gezamenlijke basis 8–29 nov, waarvan 18–22 nov een week voor persoonlijke ontwikkeling (geen werk aan de basis); fase 2 eigen features 30 nov–27 dec; buffer rest van de kerstvakantie; alles af op 10 jan 2027; fase 3 finetunen en film 11–17 jan | In periode 1 werd het eind een race tegen de klok; de buffer vangt ziekte en uitloop op. Aangepast 7 nov: fase 1 en 2 elk een week langer, omdat Lonneke rust nodig heeft. Gevolg: kleinere buffer en één week voor fase 3 | Vast (aangepast, 7 nov) |
| P-2 | Definition of Done | Een feature is pas af als het bijbehorende stuk in het FTD ook af is | Voorkomt schrijfwerk aan het eind | Vast (7 nov) |
| P-3 | Platform | ~~Alleen Android met Jetpack Compose; KMP wordt een nieuw project in periode 3~~ Vervangen op 10 nov: Kotlin Multiplatform met Compose Multiplatform, met Android als enige target. Navigation 3 en Koin blijven | Keuze Eva, 5 nov; vervangen op advies van Paul, 10 nov. Desktop als tweede target overwogen en afgevallen (Eva, 10 nov). Uitgewerkt in FTD v2.0 (A-03) | Vast (aangepast, 10 nov) |
| P-4 | Vorm van het FTD | Nieuw FTD voor de app, met verwijzingen naar FTD v3.0 voor de API. Zelfde werkwijze: revisiehistorie, wijzigingstabel en besluitentabel | FTD v3.0 is ruim 2.300 regels met historie die voor de app niet telt; het toetsdocument staat een apart document toe | Vast |
| P-5 | Verdeling | Eva: Mijn aanbod, Partij toevoegen (F1). Stefan: Ontdekken (lijst en kaart), Mijn reserveringen, Reserveringsdetail (F2). Lonneke: Inloggen en registreren, Productdetail (GI-3, US-07) | Zie "Voorstel verdeling app periode 2" | Vast (7 nov) |
| P-6 | Sensoren | Camera (Eva, barcode), GPS (Stefan, afstand en kaart), vingerafdruk (Lonneke, snel inloggen). Schudden vervalt als reserve | Eén sensor per persoon, elk in de gebruikersflow; emulator ondersteunt vingerafdruk via `finger touch` | Vast (7 nov) |
| P-7 | HTTPS | De Ktor API krijgt https; taak in de gezamenlijke basis; FTD v3 §16.3 corrigeren | Android staat standaard alleen https toe (lesbeschrijving); de API draait nu op http | Vast |
| P-8 | Technieken | De technieken uit de lessen zijn de standaard: Compose, Material 3, ViewModel met StateFlow, Repository, Navigation 3, Ktor Client, Koin, Preferences DataStore, Coil, MapLibre met FusedLocationProviderClient | Toets: "gebouwd volgens de tijdens de les aangeboden technieken" | Vast |
| P-9 | Architectuur | Drie lagen: scherm (Compose), ViewModel, repository. Unidirectional data flow. Geen MVI | Scheiding UI en logica is een toetscriterium; testbaar met nep-repository; ViewModel overleeft draaien; sluit aan op les 10–12. Zie "Voorstel opbouw van de app" | Vast (7 nov) |
| P-10 | Camera en barcode | CameraX met ML Kit Barcode Scanning, gebundelde variant (`com.google.mlkit:barcode-scanning`). Niet de Google Code Scanner | Eigen camerascherm laat het sensorgebruik echt zien; ML Kit ondersteunt EAN-13, EAN-8, UPC-A en UPC-E; gebundeld werkt zonder download, dus betrouwbaar in de demo. De Code Scanner vraagt geen cameratoestemming en heeft geen eigen UI: daarmee zit de camera in Google Play services en niet in onze app | Vast (7 nov); akkoord Paul 8 nov |
| P-11 | Vingerafdruk | AndroidX Biometric (`BiometricPrompt`). Eerste keer inloggen met wachtwoord, daarna met vingerafdruk | Jetpack-library; Android raadt aan de eerste aanmelding anders te doen en biometrie te gebruiken voor latere aanmeldingen | Vast (7 nov); akkoord Paul 8 nov |
| P-12 | GPS en kaart | MapLibre Compose met `FusedLocationProviderClient` (Stefan) | Bepaald door Paul: advies in september (FTD v3.0 §20) en lesstof in les 13/14 | Vast |

## Open punten (besproken 7 nov)

| # | Onderwerp | Wat besloten moet worden |
|---|---|---|
| C | Libraries buiten de lessen | Besloten (7 nov). Libraries: zie P-10 en P-11, akkoord van Paul (8 nov); andere libraries mogen als ze op klibs.io staan. Token: optie B, het token in Preferences DataStore, versleuteld met een sleutel uit de Android Keystore; de vingerafdruk kan die sleutel ontgrendelen. Terugvaloptie A als B op 20 dec niet werkt: token onversleuteld in Preferences DataStore, vingerafdruk ontgrendelt alleen het scherm (`BiometricPrompt` zonder `CryptoObject`). Risico beperken: token wissen bij uitloggen en bij `401`, korte geldigheid van het JWT (API), sandbox van de app. Bij terugval in het FTD onder privacy en security by design uitleggen waarom en welk risico overblijft. `EncryptedSharedPreferences` niet gebruiken: volgens Claude afgeschaft door Google, nog na te kijken |
| D | Opbouw van de module | D1 akkoord (7 nov): zelfde repo, map `android/` (al voorbereid in `project.yaml`), één module. D3 (vast, Eva 5 nov): packages per feature zoals periode 1; voorstel `product` (Eva), `reservation` (Stefan), `security` en `pricing` (Lonneke), `core` (gedeeld), met `ui` en `data` per feature. D2 (8 nov, Paul): Kotlin Toolchain in IntelliJ IDEA. Terugval: omzetten naar Gradle met ktc-to-gradle; andersom overstappen is veel lastiger. Achtergrond, de twee combinaties die we voorlegden: (a) Kotlin Toolchain (voorheen Amper) in IntelliJ IDEA met de Android-plugin, net als de server; Paul gebruikt zelf de Toolchain. Nadeel: Alpha, en de documentatie noemt Android Studio niet. (b) Gradle in Android Studio, net als de lesvoorbeelden. Nadeel: twee bouwtools in één repo |
| E | Design patterns | Akkoord (7 nov). Iedereen kiest op zaterdag 14 nov zelf 2 patterns (1 in eigen code, 1 herkend in een library), liefst zonder dubbele. Zie "Inspiratie design patterns" hieronder |
| F | Gezamenlijke basis | Akkoord (7 nov), met de eigenaren zoals voorgesteld. GB-4 (projectopzet) gebruikt de Kotlin Toolchain (D2). Zie "Gezamenlijke basis" hieronder |
| G | Wireframes | Akkoord (7 nov): Claude genereert na 7 november de 7 schermen uit de user stories en de request-flow (FTD v3 §18). Iedereen controleert en verbetert de eigen schermen tegen de user stories. Prompt bewaren en aanpassingen noteren, als voorbeeld voor de AI-verantwoording. Klaar vóór 30 november (aangepast 7 nov door de langere fase 1) |
| H | Testen | Vast (Eva, 5 nov): test-driven, van rood naar groen, en (bijna) alle code getest, net als in periode 1. Unit tests voor ViewModels (nep-repository) en repository's (Ktor mock-engine). UI-tests met Compose UI testing op de stateless schermen; per scherm de hoofdactie van de user story en de foutsituatie, plus de navigatie tussen schermen. Sensoren worden in tests vervangen door nepinvoer. Testdata: de seeddata van de API |

## UI-testen: wat is representatief

Een UI-test doet na wat een gebruiker doet: iets zien, ergens op drukken, en controleren wat er daarna te zien is. Representatief zijn testen die:

1. de hoofdactie van een user story nadoen;
2. de foutsituatie laten zien die de gebruiker kan tegenkomen;
3. de navigatie tussen schermen controleren.

| Wie | Hoofdactie | Foutsituatie |
|---|---|---|
| Eva | Na het scannen is het formulier voorgevuld met naam en allergenen | Lege naam geeft een melding bij het veld |
| Stefan | Filter op maximumprijs toont alleen passende partijen | Reserveren mislukt (`409`): melding en een ververste lijst |
| Lonneke | Productdetail toont oorspronkelijke prijs, korting en actuele prijs | Te kort wachtwoord bij registreren geeft een melding bij het veld (B-56) |

Aangepast 10 nov (Eva): de UI-test "reserveren mislukt (`409`)" is van Lonneke, want de knop staat op haar scherm Productdetail. Voorstel voor Stefan in de plaats: intrekken met `409`. Stefan bevestigt dat nog (FTD v1.3, O-11).

Tips (bron: eigen kennis Claude, niet nagekeken):

- Test het scherm los van de ViewModel. Dankzij state hoisting (les 8) geef je het scherm een nep-toestand mee. Dan is de test snel en heeft hij geen API nodig.
- Sensoren test je niet echt. Geef nep-invoer, zoals een vaste barcode of een vaste locatie, en test wat de app ermee doet.
- Niet representatief: testen die alleen controleren of een tekst er staat, of welke kleur iets heeft.

## Gezamenlijke basis (akkoord, 7 nov)

Moet klaar zijn voordat iemand aan de eigen feature begint (fase 1, 8 t/m 29 november, zonder de week van 18 t/m 22 november). Verdeeld zoals in periode 1: ieder bouwt het gedeelde stuk dat bij de eigen rol past.

| # | Onderdeel | Inhoud | Eigenaar | Vergelijkbaar in periode 1 |
|---|---|---|---|---|
| GB-1 | Data- en netwerklaag | Ktor client, kotlinx.serialization, basis-URL, Koin-opzet, basis voor de repository's | Eva | GI-1 persistentie |
| GB-2 | Navigatie en foutafhandeling | Navigation 3, bottom navigation met drie tabs, het foutmodel van de API (FTD v3 §10.2) op één plek afhandelen | Stefan | GI-2 en GI-4 |
| GB-3 | Sessie en beveiliging | Token bewaren (Preferences DataStore, versleuteld met de Android Keystore; zie open punt C), bij `401` naar het inlogscherm, https op de Ktor API | Lonneke | GI-3 security |
| GB-4 | Projectopzet | Map `android/`, Kotlin Toolchain, ktlint, lege packages | Samen | Startsessie |
| GB-5 | Thema en teksten | Material 3-thema, `strings.xml` in NL en EN, gedeelde UI-onderdelen (laden, foutmelding) | Samen | GI-5 gedeeld |
| GB-6 | Test-setup | Eén voorbeeld-unit test en één voorbeeld-UI-test | Samen | GI-6 testopzet |
| GB-7 | Coverage-meting | Uitzoeken hoe de regeldekking in de Android-module gemeten wordt (uitgangspunt: de JaCoCo-plugin van de server), en daarna Stefan en Lonneke uitleggen hoe zij de meting draaien. Af voordat Eva aan haar eigen schermen begint (toegevoegd 10 nov, Eva) | Eva | Coverage-meting in GI-6 |

Aandachtspunt: navigatie komt in les 10 en de Ktor client in les 11. Wie GB-1 of GB-2 bouwt, leert vooruit met unit 3 en 4 en de voorbeeldcode van Paul.

## Inspiratie design patterns

Ter inspiratie, geen keuze. Iedereen kiest zelf. Bron: eigen kennis (Claude), nog niet per library nagekeken, behalve de Builder van AndroidX Biometric. Paul behandelt strategy, singleton en observer in les 12.

| Pattern | Soort | Eigen code (voorbeeld in onze app) | Herkend in een library |
|---|---|---|---|
| Strategy | Gedrag | Sorteren of filteren op afstand, prijs of houdbaarheid (Stefan); scannen of handmatig invoeren (Eva) | – |
| State | Gedrag | Inlogtoestand: uitgelogd, ingelogd, vergrendeld (Lonneke); gedrag per reserveringsstatus (Stefan) | – |
| Observer | Gedrag | Eigen Flow met locatie-updates | `StateFlow` en `collectAsState` in Compose; CameraX die beelden aanlevert |
| Command | Gedrag | Schermacties als vaste set berichten naar de ViewModel (`ReserveClicked`) | – |
| Chain of Responsibility | Gedrag | – | Plugins van de Ktor client die een request na elkaar bewerken |
| Template Method | Gedrag | Basisklasse met een vaste volgorde: laden, tonen, fout afhandelen | – |
| Adapter | Structuur | ML Kit-resultaat omzetten naar ons barcode-model (Eva); API-antwoord (DTO) naar domeinmodel | – |
| Facade | Structuur | Repository die Ktor client, DataStore en foutafhandeling achter één simpele interface verbergt | – |
| Decorator | Structuur | Repository omhullen met caching of logging, zonder de repository zelf te wijzigen | `Modifier`-keten in Compose (vergelijkbaar idee) |
| Proxy | Structuur | Object dat het token toevoegt voordat het request naar de API gaat | Auth-plugin van de Ktor client |
| Composite | Structuur | – | De Compose UI-boom: composables die composables bevatten |
| Builder | Creatie | Builder voor een zoekopdracht met filters | `BiometricPrompt.PromptInfo.Builder` (Lonneke); configuratie van de Ktor client |
| Factory | Creatie | Factory die de echte of een nep-repository maakt (handig voor testen) | `ViewModelProvider.Factory`; `viewModel { }` in Koin |
| Singleton | Creatie | Eén gedeelde `HttpClient` als Kotlin `object` | `single { }` in Koin |

Architectuurpatronen als MVVM, Repository en Dependency Injection gebruiken we sowieso, maar die tellen we niet als een van de twee design patterns (besluit Eva, 5 nov). Iedereen kiest uit de klassieke design patterns hierboven.

## Gekozen design patterns (14 nov)

Gekozen in de startsessie (A-15, O-4). Geen dubbele. Waar een pattern precies in de code komt, vult de eigenaar aan zodra het gebouwd is.

| Wie | Eigen code | Waar | Herkend in een library | Waar |
|---|---|---|---|---|
| Lonneke | State | Nog aan te vullen door Lonneke | Builder | Nog aan te vullen door Lonneke |
| Stefan | Adapter | Nog aan te vullen door Stefan | Facade | Nog aan te vullen door Stefan |
| Eva | Strategy | Een barcode scannen of handmatig invoeren: twee manieren achter één interface (F1) | Chain of Responsibility | De plugins van de Ktor-client (GB-1) |

Eva koos Adapter niet, omdat Stefan die al had. Dat was in de inspiratielijst haar voorbeeld (ML Kit-resultaat naar ons barcode-model).

**Chain of Responsibility in de Ktor-client.** Elke geïnstalleerde plugin haakt in een vaste fase van een request, en het request gaat in volgorde langs alle plugins: `onRequest`, `Send`, `SendingRequest`, `onResponse`. In de fase `Send` kan een plugin een request zelf afhandelen of opnieuw versturen, bijvoorbeeld bij een redirect, een retry of authenticatie (Ktor, z.j.). De documentatie noemt dit zelf geen Chain of Responsibility; dat het pattern hierop past, is onze eigen redenering.

Bron: Ktor. (z.j.). *Custom client plugins*. JetBrains. Geraadpleegd op 14 november 2026, van https://ktor.io/docs/client-custom-plugins.html

## Aandachtspunten

- De ingeleverde zip van periode 1 bevatte `.env` met de echte `JWT_SECRET`, plus `.git` en `.claude/settings.local.json`. De zip van periode 2 zonder deze bestanden maken.

## Vragen voor Paul

1. Camera, barcode scannen en vingerafdruk komen niet in de lessen voor. Wij willen CameraX met ML Kit Barcode Scanning en AndroidX Biometric gebruiken. Is dat akkoord?
   - Antwoord (8 nov): akkoord. Libraries van klibs.io zijn prima.
2. Welke bouwtool en IDE wil je voor de Android-app: de Kotlin Toolchain in IntelliJ IDEA (zoals onze server), of Gradle in Android Studio (zoals de lesvoorbeelden)? De documentatie van de Toolchain noemt het Alpha en beschrijft alleen IntelliJ IDEA.
   - Antwoord (8 nov): Kotlin Toolchain in IntelliJ IDEA voor nieuwe projecten. Lukt iets echt niet, dan omzetten naar Gradle met ktc-to-gradle (https://github.com/Heapy/ktc-to-gradle).
3. Https: onze API draait op http en Android blokkeert dat. Mogen we https toevoegen met een zelfondertekend certificaat?
   - Antwoord (8 nov): akkoord, zelfs beter.

---
onderwerp: takenlijst van Lonneke voor sessie 2 van de app, sessie en beveiliging overdragen (GB-3)
tags: [werk-in-uitvoering, proftaak, periode-2, android, sessie-2, takenlijst]
status: actueel
oorsprong: samenwerking-eva-en-claude
aangemaakt: 2026-11-12
gewijzigd: 2026-11-15
gerelateerd:
  - "[[draaiboek-sessie-2-android]]"
  - "[[FTD-restpartijen-android-app-v2_5]]"
  - "[[literaire-onderbouwing-token-en-vingerafdruk]]"
  - "[[instructie-koin-modules]]"
  - "[[besluiten-ftd-app]]"
---

# Takenlijst sessie 2 app — Lonneke: sessie en beveiliging

> **Bijgewerkt op 15 november (v2).** Geschreven op 12 november; op 15 november bijgewerkt met de besluiten van de startsessie en het nalopen van het draaiboek (FTD v2.5, A-20 t/m A-30). Nieuw: een **prioriteit** (zie hieronder), `formatPrice`, de Koin-opzet per feature, jouw plugin `SessionAuthorization` als besluit (A-30), en een leerroute voor je design patterns als optie (Phase 3b).

Bron: [[draaiboek-sessie-2-android]] (bijgewerkt op 15 november). Variant: solo, alleen jouw deel. Plafond: alles wat het draaiboek van Lonneke vraagt, vóór, tijdens en direct na de sessie. De vingerafdruk (US-14) en de schermen Inloggen en Registreren horen bij fase 2 en staan niet in deze lijst.

**Leeswijzer.** Elk item zegt *wat* je doet en *waarom*. Het *hoe* staat er bewust niet in: dat zoek je zelf uit, zodat je het kunt uitleggen. Elk item eindigt met een link naar de plek in het draaiboek, of met een verwijzing naar een aanvulling (A1..A4). Verwijzingen met §, GB, K-A, O-, A- en W-A gaan naar het [[FTD-restpartijen-android-app-v2_5|FTD]] (v2.5).

## Prioriteit: wat Eva en Stefan van jou nodig hebben

Jouw feature is klein, daarom is jouw deel van de gezamenlijke basis groot. Dat mag langer duren dan bij Eva en Stefan. **Eva en Stefan hebben alleen prio 1 van je nodig** om zonder wachten door te bouwen. Prio 2 en 3 mogen langer duren, ook tot in fase 2.

| Prio | Wat | Phase | Wie wacht erop |
|---|---|---|---|
| **1** | `formatPrice(cents)` in `core` (A-28) | 0b | Eva (Mijn aanbod) en Stefan (Ontdekken) tonen prijzen in fase 2 |
| **1** | De branch `gb-3-401-clears-session` afmaken: ktlint, pull request | 0b | Iedereen: het token gaat mee, en een `401` wist de sessie |
| **1** | Beslissing 3 met Stefan: wat staat er in fase 1 achter `LoginRoute` | 0 en 0b | Stefan, voor S5 |
| 2 | `EncryptedSessionStore`, de back-upregels en de wissel in Koin (L1–L3) | 1 en 2 | Niemand: tot dan werkt `FakeSessionStore` voor iedereen |
| 3 | Https op de server (W-A1, L6) | 4 | Niemand: tot dan werkt http naar `10.0.2.2` (§11.3) |

> **Voorwaarde voor de fake (belangrijk).** Koin draait in strenge modus (A-29): staat hetzelfde type twee keer aangemeld, dan crasht de app bij het starten. Wissel `FakeSessionStore` daarom pas voor `EncryptedSessionStore` als die **af en getest** is, en doe het **in één pull request**: de echte aanmelden in `corePlatformModule`, de fake uit Koin halen, en de fake naar `test/` verhuizen. Zo werkt de app voor Eva en Stefan op elk moment, met de fake of met de echte versie. Zie [[instructie-koin-modules]].

**W-A1 en fase 1.** In het FTD staat W-A1 in fase 1. Schuift het naar fase 2, dan is dat beslissing 8: leg het vast, met een reden, zodat Eva het in het FTD zet.

**Commando's.** De commando's staan zoals op Linux. Op Windows gebruik je `.\kotlin.bat` in plaats van `./kotlin` (Claude, niet getest op Windows).

## Aanvullingen

### A1 Rood vóór groen bij elke controle
- **Wat het draaiboek niet zegt.** Het draaiboek vraagt alleen bij L7 om een test eerst te laten falen.
- **Waarom de lijst het nodig heeft.** Een test die nooit rood is geweest, bewijst niets. Het FTD vraagt deze werkwijze ook ([[FTD-restpartijen-android-app-v2_5#13. Teststrategie (A-17)|§13]]).
- **Wie bevestigt.** Lonneke.

### A2 Volgorde: eerst wat anderen nodig hebben
- **Wat het draaiboek niet zegt.** In welke volgorde je L1 tot en met L7 doet.
- **Waarom de lijst het nodig heeft.** Zie "Prioriteit" hierboven: eerst prio 1 (phase 0b), daarna in je eigen tempo L1–L3, en W-A1 als laatste. L4 (het token) en de `401` heb je al gebouwd (PR #90 en `gb-3-401-clears-session`).
- **Wie bevestigt.** Eva en Lonneke (15 nov).

### A3 Geheimen blijven uit Git
- **Wat het draaiboek niet zegt.** Het draaiboek zegt alleen dat de keystore en de wachtwoorden niet in Git staan, niet hoe je dat controleert.
- **Waarom de lijst het nodig heeft.** Een geheim dat één keer is gecommit, staat voorgoed in de geschiedenis. Controleren vóór elke commit is de enige zekerheid ([[FTD-restpartijen-android-app-v2_5#16.5 Secrets en afhankelijkheden|§16.5]]).
- **Wie bevestigt.** Lonneke.

### A4 Versies en documentatie nakijken
- **Wat het draaiboek niet zegt.** Dat je de namen controleert in de documentatie van de versies die wij gebruiken.
- **Waarom de lijst het nodig heeft.** Het FTD noemt DataStore 1.3.0 met `datastore-tink` en Tink 1.24.0 ([[FTD-restpartijen-android-app-v2_5#8.6 Technische keuzes (A-08)|§8.6]]). `datastore-tink` is nieuw; oudere voorbeelden en AI-tools gebruiken vaak `EncryptedSharedPreferences`, en die is afgeschaft.
- **Wie bevestigt.** Lonneke.

### A5 Design patterns leren herkennen (optie)
- **Wat het draaiboek niet zegt.** Waar en hoe je jouw twee design patterns gebruikt. Het FTD eist per student twee patterns met een klassen- en een sequencediagram ([[FTD-restpartijen-android-app-v2_5#Besluiten|A-15]]); jij koos State (zelf toegepast) en Builder (herkend in een library).
- **Waarom de lijst het nodig heeft.** Phase 3b is een leerroute zoals Eva en Stefan die ook hebben. **Je mag het zo doen, maar ook je eigen weg kiezen**: andere plekken of een andere volgorde, zolang je aan het eind beide patterns kunt aanwijzen, uitleggen en tekenen.
- **Wie bevestigt.** Lonneke.

## Begrippen

- **Sessie.** Wat de app na het inloggen bewaart: het token, de rol en het `supplierId`.
- **Token (JWT).** Het bewijs van de server dat je bent ingelogd. 24 uur geldig.
- **Android Keystore.** De kluis van Android. Een sleutel die daarin staat, verlaat de telefoon niet.
- **Tink.** Een library van Google die versleutelt, met een sleutel die door de Keystore wordt beschermd.
- **Nepversie (fake).** Een eenvoudige vervanger van een interface. `FakeSessionStore` bewaart de sessie alleen in het geheugen.
- **`MockEngine`.** Een onderdeel van de Ktor Client dat in een test een vast antwoord geeft, zonder netwerk.
- **Zelfondertekend certificaat.** Een certificaat dat je zelf maakt. Geen instantie staat ervoor in; een app moet het daarom zelf als vertrouwd aanmerken.
- **Keystore van de server.** Een bestand met de geheime sleutel en het certificaat van de server. Niet hetzelfde als de Android Keystore.
- **Rood vóór groen.** Je laat een test eerst falen. Pas dan weet je dat de test iets controleert.
- **Koin-module per feature en strenge modus.** Elke feature en `core` hebben een eigen Koin-module; Android-code staat in een platform-module, zoals `corePlatformModule`. Koin weigert een type dat twee keer is aangemeld (A-29, [[instructie-koin-modules]]).
- **Design pattern.** Een beproefde vorm voor een terugkerend ontwerpprobleem, met vaste rollen.
- **State.** Een object gedraagt zich anders per toestand; elke toestand is een eigen klasse, en het object geeft het werk door aan de huidige toestand.
- **Builder.** Bouwt een ingewikkeld object stap voor stap op, in plaats van met één constructor met veel parameters.

---

## Phase 0 — Voorbereiden en controleren

Basis: [[draaiboek-sessie-2-android#Eerst beslissen (vóór iemand begint)|Eerst beslissen]], [[draaiboek-sessie-2-android#Wat je nodig hebt om aan je eigen schermen te beginnen|Wat je nodig hebt]]
Raakt: niets in de code
Start na: de startsessie van 14 november

### Voordat je begint
- [ ] `main` is opgehaald en bevat het werk van de startsessie, de Koin-modules (PR #91) en jouw plugin `SessionAuthorization` (PR #90). Controle: `git log --oneline -5` (laat de laatste vijf commits zien).
- [ ] De build en de tests zijn groen. Controle: `./kotlin build` (bouwt alle modules) en `./kotlin test -m shared` (draait de tests van de app).
- [ ] De app start op de emulator. Controle: Run 'android' in IntelliJ.

### To-do

- [ ] **Lees de besluiten A-13 (optie B, door jou bevestigd), A-26 (`src@android/`), A-28 (`formatPrice`), A-29 (Koin) en A-30 (jouw plugin) in het FTD, en de [[instructie-koin-modules|instructie Koin]].**
  Waarom: jouw antwoorden van stap 4.5 en de keuzes van 15 november staan daar nu als besluit. ([[draaiboek-sessie-2-android#Nalopen na zaterdag|Nalopen]])

- [ ] **Lees in het FTD [[FTD-restpartijen-android-app-v2_5#16.1 Authenticatie en sessie|§16.1]], [[FTD-restpartijen-android-app-v2_5#16.3 Versleuteling|§16.3]] en [[FTD-restpartijen-android-app-v2_5#12.1 W-A1 https op de Ktor API|§12.1]], en het onderzoek [[literaire-onderbouwing-token-en-vingerafdruk]].**
  Waarom: het draaiboek verwijst ernaar. Je controleert of daar staat wat het draaiboek beweert, en je moet het straks zelf uitleggen.
  Verwacht: je kunt in drie zinnen zeggen wie wat versleutelt. Klopt iets niet met het draaiboek, dan gaat het naar de open vragen. ([[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)|A-Lonneke]])

- [ ] **Vraag Eva om de code van de proef voor de sessie-opslag te laten zien.**
  Waarom: de proef staat op een branch die niet naar GitHub gaat. Jij kunt er dus niet zelf bij. ([[draaiboek-sessie-2-android#Open vragen|open vraag 1]])

- [ ] **Zoek in `libs.versions.toml` op of DataStore, `datastore-tink` en Tink er al staan, en met welke versie.**
  Waarom: de volgende stap controleert de documentatie van precies die versies. Staan ze er niet, dan voeg je ze in phase 1 toe. (aanvulling, [[#A4 Versies en documentatie nakijken|A4]])

- [ ] **Lees in de documentatie van die versie van DataStore hoe `datastore-tink` een bestand versleutelt.**
  Waarom: je bouwt op wat de documentatie zegt, niet op wat een AI-tool voorstelt.
  Als het misgaat: wijkt de documentatie af van §16.3, bouw dan nog niets en vraag het na bij Eva. (aanvulling, [[#A4 Versies en documentatie nakijken|A4]])

- [ ] **Spreek met Eva het laatste stuk van beslissing 2 af: installeert zij jouw plugin `SessionAuthorization` in haar `HttpClient`, of lever jij een functie die zij aanroept.**
  Waarom: de plugin is van jou (A-30), de `HttpClient` van Eva. ([[draaiboek-sessie-2-android#Eerst beslissen (vóór iemand begint)|beslissing 2]])

- [ ] **Spreek met Stefan beslissing 3 af: wat staat er in fase 1 achter `LoginRoute`.** (prio 1)
  Waarom: Stefan heeft het nodig voor S5. Het voorstel in het draaiboek is een tijdelijk scherm met één knop die een testsessie opslaat. Beslissing 4 is al besloten (A-27, A-30). ([[draaiboek-sessie-2-android#Eerst beslissen (vóór iemand begint)|beslissing 3]])

- [ ] **Breng beslissing 8 in bij de groep: schuift https op de API (W-A1) naar fase 2?**
  Waarom: het is prio 3 en niemand wacht erop, maar het FTD zet het in fase 1. Leg het besluit vast, met een reden. ([[draaiboek-sessie-2-android#Eerst beslissen (vóór iemand begint)|beslissing 8]])

- [ ] **Maak de branch voor GB-3.**
  Waarom: ieder werkt op een eigen branch en maakt een pull request naar `main`. De naam volgt de afspraak van de startsessie. ([[draaiboek-sessie-2-android#A-Allen. Het staat op main|A-Allen]])

### DoD

- [ ] De paragrafen van het FTD zeggen wat het draaiboek beweert, of de afwijking staat bij de open vragen. ([[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)|A-Lonneke]])
- [ ] Je hebt de code van de proef gezien, of je weet waarom niet. ([[draaiboek-sessie-2-android#Open vragen|open vraag 1]])
- [ ] Beslissing 2 (rest), 3 en 8 hebben een antwoord en staan genoteerd op een plek die jullie alle drie zien (4 is besloten). ([[draaiboek-sessie-2-android#Eerst beslissen (vóór iemand begint)|Eerst beslissen]])

---

## Phase 0b — Prio 1: wat Eva en Stefan van je nodig hebben

Basis: "Prioriteit" bovenaan, [[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)|L4 en L5]], [[draaiboek-sessie-2-android#Eerst beslissen (vóór iemand begint)|beslissing 3 en 10]]
Raakt: `shared/src/nl/restpartijen/app/core/` (de plek van `formatPrice`), `shared/test/nl/restpartijen/app/core/`, de branch `gb-3-401-clears-session`
Start na: phase 0

### To-do

- [ ] **Maak de branch `gb-3-401-clears-session` af: draai `ktlint --format`, controleer met `git diff` wat er veranderde, draai de tests, en maak de pull request.**
  Waarom: de `401`-afhandeling is gebouwd en getest (19 tests groen samen met `main`), alleen de opmaak mist nog. `main` is er al in samengevoegd (15 nov). (A-30, NFR-A2)
  Verwacht: ktlint meldt alleen nog `AppNavigation.kt`; de PR vraagt twee reviews.

- [ ] **Schrijf eerst de tests voor `formatPrice(cents)`: in het Engels en in het Nederlands, ook voor 0 cent en voor een bedrag boven de duizend euro.**
  Waarom: elke prijs in de app moet er hetzelfde uitzien (A-28): altijd euro, in de notatie van de taal van het toestel. Je deed dit ook in de backend (beslissing 10).
  Verwacht: rood.

- [ ] **Bouw `formatPrice` op één plek in `core`, en maak er een eigen pull request van.**
  Waarom: Eva en Stefan gebruiken hem in fase 2. Een kleine, losse PR is snel gereviewd.
  Verwacht: de tests zijn groen.

- [ ] **Bouw wat je met Stefan afsprak achter `LoginRoute` (beslissing 3), als dat bij jou ligt.**
  Waarom: zonder scherm is K-A3 niet te testen. Het tijdelijke scherm moet weg vóór inlevering.

### DoD

- [ ] De PR van de `401`-branch staat open of is gemerged, zonder ktlint-meldingen.
- [ ] `formatPrice` staat op `main`, met groene tests in beide talen.
- [ ] Stefan weet wat er achter `LoginRoute` staat.

---

## Phase 1 — L1: de echte `SessionStore` (prio 2)

Basis: [[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)|L1]]
Raakt: `shared/src@android/nl/restpartijen/app/core/session/` (A-26), `shared/module.yaml`, `libs.versions.toml`, `shared/test/nl/restpartijen/app/core/session/`
Start na: phase 0b. Eigen tempo: niemand wacht hierop (zie "Prioriteit").

### To-do

- [ ] **Voeg DataStore, `datastore-tink` en Tink toe, als ze er nog niet staan. Met een vaste versie.**
  Waarom: versies staan altijd vast, nooit `latest`. Dan bouwt iedereen met dezelfde versie.
  Als het misgaat: een wijziging in `libs.versions.toml` of `module.yaml` vraagt een review van beide anderen. ([[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)|L1]])

- [ ] **Bouw `EncryptedSessionStore` in `src@android/…/core/session/`: de implementatie van `SessionStore` die de sessie versleuteld bewaart met DataStore en `datastore-tink`. Zonder vingerafdruk.**
  Waarom: dit is de best practice, zonder terugval (A-13, §16.3). De vingerafdruk komt in fase 2 erbovenop, met een eigen sleutel. ([[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)|L1]])

- [ ] **Zorg dat een bestand dat niet te ontsleutelen is, een lege sessie geeft.**
  Waarom: de gebruiker is dan uitgelogd, en de app crasht niet (§16.3). ([[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)|L1]])

- [ ] **Controleer op de emulator: sla een testsessie op, stop de app, start hem weer.**
  Verwacht: de sessie staat er nog. ([[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)|L1]])

- [ ] **Bekijk het sessiebestand op de emulator en zoek erin op je testtoken.**
  Waarom: zo zie je zelf dat het bestand versleuteld is.
  Verwacht: nul treffers; het bestand is onleesbaar. ([[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)|L1]])

- [ ] **Bouw jouw besluit voor `isUsingKeystore()`: zegt Tink nee, dan bewaart `EncryptedSessionStore` niets op schijf, en leeft de sessie alleen in het geheugen tot de app sluit.**
  Waarom: zo staat er nooit een onversleuteld token op het toestel (jouw antwoord op O-7 (e), 14 nov, [[FTD-restpartijen-android-app-v2_5#16.3 Versleuteling|§16.3]]). Kijk ook wat het antwoord op de emulator is. ([[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)|L1]])

### DoD

- [ ] De sessie overleeft het stoppen en starten van de app. Bewijs: de emulator. ([[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)|L1]])
- [ ] Het bestand op de emulator bevat het token niet leesbaar. Bewijs: de zoekuitvoer. ([[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)|L1]])
- [ ] Bij `isUsingKeystore()` = nee wordt niets op schijf bewaard. Bewijs: een test. ([[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)|L1]])

---

## Phase 2 — L2 en L3: de back-up, en de echte versie in de app (prio 2)

Basis: [[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)|L2]], [[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)|L3]]
Raakt: `android/res/xml/data_extraction_rules.xml`, `android/res/xml/backup_rules.xml`, `android/src/AndroidManifest.xml`, `shared/src@android/nl/restpartijen/app/core/CorePlatformModule.kt`, `../../shared/test/nl/restpartijen/app/core/session/FakeSessionStore.kt`, `shared/test/nl/restpartijen/app/core/session/`
Start na: phase 1

### To-do

- [ ] **Vul de twee bestanden met back-upregels: het sessiebestand en de sleutelset gaan niet mee. Laat het manifest naar beide verwijzen.**
  Waarom: de sleutel in de Keystore gaat niet mee in een back-up. Een teruggezet bestand is dus niet te ontsleutelen (§16.3). ([[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)|L2]])

- [ ] **Draai `./kotlin clean`, bouw opnieuw en controleer met `aapt2` of beide regels in de APK zitten.**
  Waarom: zonder `clean` komt een wijziging in het manifest of in `android/res/` niet in de app. Dat is de valkuil uit de proef.
  Verwacht: beide bestanden en beide verwijzingen staan in de uitvoer. ([[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)|L2]])

- [ ] **Wissel de fake voor de echte versie, in één pull request (zie de voorwaarde bij "Prioriteit"): meld `EncryptedSessionStore` aan in `corePlatformModule`, haal `FakeSessionStore` uit Koin, en verhuis hem naar `shared/test/nl/restpartijen/app/core/session/`.**
  Waarom: Koin draait in strenge modus; twee aanmeldingen van `SessionStore` laten de app crashen bij het starten (A-29). En is de echte versie af, dan hoort de fake alleen nog bij de tests ([[FTD-restpartijen-android-app-v2_5#8.3 Modules en packages (A-14)|§8.3]]). Meld `SessionStore` nergens anders aan dan in `corePlatformModule` ([[instructie-koin-modules]]).
  Als het misgaat: gebruikt Stefan de fake nog in een test of een tijdelijk scherm, spreek dan af wanneer zij verhuist. ([[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)|L3]])

- [ ] **Schrijf een test die controleert dat Koin voor `SessionStore` de echte versie geeft, niet `FakeSessionStore`.**
  Waarom: de strenge modus vangt niet elke dubbele aanmelding (bijvoorbeeld via `bind`). Deze test wel. Uit de security-review van 15 november.

- [ ] **Draai `./kotlin test -m shared`.**
  Verwacht: alle bestaande tests zijn nog groen. ([[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)|L3]])

### DoD

- [ ] De back-upregels zitten in de APK. Bewijs: de uitvoer van `aapt2`. ([[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)|L2]])
- [ ] De app start met de echte `SessionStore`, en de tests zijn groen, ook `AppModuleTest`. ([[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)|L3]])

---

## Phase 3 — L4 en L5: het token in de header, en weg bij `401`

Basis: [[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)|L4]], [[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)|L5]]
Raakt: `shared/src/nl/restpartijen/app/core/network/`, `shared/test/nl/restpartijen/app/core/network/`
Start na: gebouwd vóór 15 november. Wat overblijft: het afmaken in phase 0b, en de controle in Logcat.

### To-do

- [x] **Test en plugin: met een sessie staat `Authorization: Bearer <token>` in de aanroep, zonder sessie niet, alleen naar de eigen host, een bestaande header blijft staan.** Plugin `SessionAuthorization` (`onRequest`), PR #90, gemerged. ([[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)|L4]])

- [x] **Test en bouw: na een `401` met het huidige token geeft `SessionStore` geen sessie meer; een `401` bij inloggen wist niets.** Plugin `SessionAuthorization` (`on(Send)`), branch `gb-3-401-clears-session`. Besluit A-30. Afmaken: phase 0b.

- [ ] **Kijk in Logcat naar één aanroep met een sessie. Gebruik een tijdelijke aanroep op je eigen branch, die je niet commit.**
  Verwacht: de header staat als `***`. Het token staat nergens in de log. ([[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)|L7]])

### DoD

- [x] De tests zijn groen en zijn één keer rood geweest (PR #90 en de 401-branch). ([[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)|L4]])
- [ ] Het token staat niet in Logcat. Bewijs: de regel uit Logcat. ([[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)|L4]])

---

## Phase 3b — Design patterns leren herkennen (optie, je eigen weg mag ook)

Basis: aanvulling [[#A5 Design patterns leren herkennen (optie)|A5]], [[FTD-restpartijen-android-app-v2_5#Besluiten|A-15]], [[besluiten-ftd-app#Gekozen design patterns (14 nov)|gekozen patterns]]
Raakt: geen code; een naslagnotitie in de vault
Start na: wanneer het jou uitkomt. De plekken hieronder zijn **vragen om uit te zoeken** (Claude, niet nagekeken), geen vast antwoord.

### To-do: Builder (herkend in een library)

- [ ] **1. Begrijpen. Lees wat Builder is: welk probleem het oplost en welke rollen het heeft (Builder, ConcreteBuilder, Product, Director).** Bron: Refactoring.Guru (z.j.-a).
- [ ] **2. Herkennen. Mogelijke plekken: in jouw plugin krijgt `onRequest` een `HttpRequestBuilder`, waarop je de header zet voordat het request wordt gemaakt; later in US-14 `BiometricPrompt.PromptInfo.Builder`.** Wijs bij elke rol het bestand en de regel aan.
- [ ] **3. Voelen. Bedenk hoe het zonder Builder zou moeten: één constructor met alle onderdelen van een request?**
- [ ] **4. Vastleggen. Een klassen- en een sequencediagram, en drie zinnen meerwaarde, in een naslagnotitie.** (A-15, §1.5)
- [ ] **5. Navertellen. Leg het in sessie 2 uit aan Eva of Stefan.**

### To-do: State (zelf toegepast)

- [ ] **1. Begrijpen. Lees wat State is: welk probleem het oplost en welke rollen het heeft (Context, State, ConcreteState).** Bron: Refactoring.Guru (z.j.-b).
- [ ] **2. Een plek kiezen. Mogelijke plek: de toestand van inloggen in je inlogscherm in fase 2 (uitgelogd, ingelogd, vergrendeld door de vingerafdruk), waarbij elke toestand eigen gedrag heeft.**
- [ ] **3. Voelen. Bedenk hoe het eruitziet met `if`/`when` op een toestand-veld, en wat State daaraan verbetert.**
- [ ] **4. Vastleggen. Een klassen- en een sequencediagram, en drie zinnen meerwaarde.** (A-15)
- [ ] **5. Navertellen.**

### DoD

- [ ] Je kunt bij beide patterns de plek aanwijzen en de meerwaarde in eigen woorden zeggen.
- [ ] In [[besluiten-ftd-app#Gekozen design patterns (14 nov)|de besluitennotitie]] staat bij jou waar beide patterns te zien zijn.

---

## Phase 4 — L6: https op de Ktor API (W-A1) (prio 3)

Basis: [[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)|L6]]
Raakt: `server/` (de connector en de configuratie), `.env.example`, `README.md`, `android/res/xml/network_security_config.xml`, het certificaat in de app
Start na: phase 0b en beslissing 8. Als laatste: niemand wacht hierop, en het mag naar fase 2 schuiven als beslissing 8 dat zegt.

### Voordat je begint
- [ ] De server start. Controle: laad de omgeving uit `.env` en draai `./kotlin run -m server` (start de server). Daarna antwoordt `http://localhost:8080/api/v1/products` in de browser.
- [ ] De tests van de server zijn groen. Controle: `./kotlin test -m server` (draait de tests van de server).

### To-do

- [ ] **Schrijf eerst de integratietest: inloggen over https en een beschermde endpoint aanroepen.**
  Waarom: dit is de test die §12.1 vraagt.
  Verwacht: rood, want de server luistert nog niet op https. ([[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)|L6]])

- [ ] **Maak een zelfondertekend certificaat met `keytool`, geldig voor `localhost` en `10.0.2.2`.**
  Waarom: de emulator bereikt de laptop op `10.0.2.2`. Geldt het certificaat daar niet voor, dan weigert de app de verbinding. ([[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)|L6]])

- [ ] **Geef de server een https-connector naast de bestaande http-connector. De instellingen komen uit de omgeving: het pad naar de keystore, de twee wachtwoorden en de alias.**
  Waarom: zo werkt het ook bij `JWT_SECRET`. De http-connector blijft voor de HTTP-client van IntelliJ en de demo-frontend (§12.1).
  Verwacht: de integratietest is groen. ([[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)|L6]])

- [ ] **Zet de namen van de nieuwe instellingen in `.env.example` en in de README. Alleen de namen, geen waarden.**
  Waarom: Eva en Stefan moeten hun eigen `.env` kunnen aanvullen. ([[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)|L6]])

- [ ] **Controleer vóór elke commit met `git status` dat de keystore, `.env` en de wachtwoorden er niet bij staan.**
  Waarom: een geheim dat één keer is gecommit, staat voorgoed in de geschiedenis.
  Als het misgaat: staat er toch een geheim in een commit, stop dan en meld het aan de groep. Los het niet alleen op. (aanvulling, [[#A3 Geheimen blijven uit Git|A3]])

- [ ] **Zet het certificaat (niet de sleutel) in de app, en laat de debug-build het vertrouwen.**
  Waarom: Android vertrouwt een zelfondertekend certificaat niet vanzelf. Alleen de debug-build vertrouwt het; dat is een bewuste keuze ([[FTD-restpartijen-android-app-v2_5#11.3 Https|§11.3]]). ([[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)|L6]])

- [ ] **Zet samen met Eva http uit in de netwerkregels, en verander het adres van de server naar https.**
  Waarom: de netwerkregels en het adres zijn van Eva (E2, E3). Na W-A1 staat http nergens meer aan.
  Als het misgaat: bereikt de emulator de API niet over https, dan geldt de terugval van §12.1. Dat is een besluit van de groep; zet het in het FTD. ([[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)|L6]])

- [ ] **Draai de bestaande tests van de server.**
  Verwacht: alles groen. Deze tests gebruiken geen connector en veranderen dus niet. ([[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)|L6]])

- [ ] **Maak voor deze wijziging een eigen pull request, met review.**
  Waarom: de server is bevroren. Die regel is alleen voor deze ene wijziging opgeheven (§12.1). ([[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)|L6]])

### DoD

- [ ] De integratietest over https is groen en is één keer rood geweest. Bewijs: de testuitvoer. ([[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)|L6]])
- [ ] De bestaande tests van de server zijn groen. ([[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)|L6]])
- [ ] De app op de emulator bereikt de API over https. ([[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)|L6]])
- [ ] De pull request bevat geen keystore, geen `.env` en geen wachtwoord. Bewijs: de lijst met bestanden van de pull request. (aanvulling, [[#A3 Geheimen blijven uit Git|A3]])

---

## Phase 5 — L7: jouw eigen voorbeeld

Basis: [[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)|L7]]
Raakt: `shared/test/nl/restpartijen/app/core/network/`
Start na: phase 3

### To-do

- [ ] **Kies in FTD v3 §10.1 één endpoint dat een rol vraagt.**
  Waarom: je voorbeeld moet een aanroep zijn die zonder token mislukt. ([[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)|L7]])

- [ ] **Schrijf één test met `MockEngine` voor die aanroep, met een opgeslagen testsessie.**
  Waarom: dit is wat Eva en Stefan straks doen in hun eigen repository's. Zij vertrouwen erop dat het token meegaat. ([[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)|L7]])

- [ ] **Haal je plugin `SessionAuthorization` tijdelijk uit de test-client, en kijk of de test faalt. Zet hem terug.**
  Waarom: de controle van L7. ([[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)|L7]])

- [ ] **Haal je tijdelijke aanroep weg.**
  Waarom: wat tijdelijk is, komt niet op `main`. ([[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)|L7]])

### DoD

- [ ] De test is groen en is één keer rood geweest. Bewijs: de testuitvoer. ([[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)|L7]])

---

## Phase 6 — Op `main`

Basis: [[draaiboek-sessie-2-android#A-Allen. Het staat op main|A-Allen]]
Raakt: je pull requests
Start na: phase 1 tot en met 5

### To-do

- [ ] **Draai ktlint en de build.**
  Verwacht: nul meldingen en geen warnings (NFR-A2). ([[draaiboek-sessie-2-android#A-Allen. Het staat op main|A-Allen]])

- [ ] **Draai `./kotlin test -m shared` en `./kotlin test -m server`.**
  Verwacht: alles groen. ([[draaiboek-sessie-2-android#A-Allen. Het staat op main|A-Allen]])

- [ ] **Maak per onderwerp een pull request naar `main` en vraag een review van Eva en Stefan allebei als hij `core/` of een gedeeld bestand raakt.**
  Waarom: wat niet op `main` staat, bestaat voor de anderen niet. Dat is de afgesproken reviewregel. ([[draaiboek-sessie-2-android#A-Allen. Het staat op main|A-Allen]])

- [ ] **Verwerk de opmerkingen en laat een ander mergen.**
  Waarom: je merget je eigen pull request niet. ([[draaiboek-sessie-2-android#A-Allen. Het staat op main|A-Allen]])

### DoD

- [ ] GB-3 staat op `main`. Bewijs: de gemergede pull requests. ([[draaiboek-sessie-2-android#A-Allen. Het staat op main|A-Allen]])
- [ ] De app start vanaf `main`. ([[draaiboek-sessie-2-android#A-Allen. Het staat op main|A-Allen]])

---

## Phase 7 — Deel B: jouw uitleg voorbereiden

Basis: [[draaiboek-sessie-2-android#Vragen voor Lonneke|Vragen voor Lonneke]], [[draaiboek-sessie-2-android#De toets of het is overgekomen|De toets]]
Raakt: je eigen aantekeningen
Start na: phase 6

### To-do

- [ ] **Beantwoord de elf vragen voor Lonneke in eigen woorden, en noteer bij elk antwoord het bestand en de regel.**
  Waarom: in blok 4 wijs je de plek in de code aan. Voorlezen mag niet. ([[draaiboek-sessie-2-android#Vragen voor Lonneke|Vragen voor Lonneke]])

- [ ] **Lees de vragen voor Eva en Stefan.**
  Waarom: jij vertelt in blok 2 of 3 één punt van hen na. ([[draaiboek-sessie-2-android#De toets of het is overgekomen|De toets]])

### DoD

- [ ] Bij elke vraag staat een antwoord en een plek in de code. ([[draaiboek-sessie-2-android#Vragen voor Lonneke|Vragen voor Lonneke]])

---

## Phase 8 — De sessie

Basis: [[draaiboek-sessie-2-android#Deel C — De sessie|Deel C]]
Raakt: `shared/src/nl/restpartijen/app/example/`
Start na: phase 7, en de datum van sessie 2

### To-do

- [ ] **Blok 1: haal `main` op, bouw, draai de tests, start de server en de app.** ([[draaiboek-sessie-2-android#Blok 1. Dezelfde basis (10 min)|Blok 1]])
- [ ] **Blok 2: draai zelf de coverage-meting en open het rapport. Vertel één punt van Eva na, als jij aan de beurt bent.**
  Verwacht: je kunt het percentage van één klasse noemen. ([[draaiboek-sessie-2-android#Blok 2. Eva laat zien en legt uit (30 min)|Blok 2]])
- [ ] **Blok 3: vertel één punt van Stefan na, als jij aan de beurt bent.** ([[draaiboek-sessie-2-android#De toets of het is overgekomen|De toets]])
- [ ] **Blok 4: laat L1 tot en met L7 zien en beantwoord de vragen.**
  Verwacht: een ander vertelt één punt na. ([[draaiboek-sessie-2-android#Blok 4. Lonneke laat zien en legt uit (30 min)|Blok 4]])
- [ ] **Blok 5: bouw mee aan het voorbeeldscherm met de echte API.**
  Waarom: hier blijkt of de drie delen op elkaar passen. ([[draaiboek-sessie-2-android#Blok 5. Samen één stukje door alle delen (40 min)|Blok 5]])
- [ ] **Blok 6: geef jouw antwoorden op de vragen van O-7 aan Eva voor het FTD. Maak de branch voor je eerste story.** ([[draaiboek-sessie-2-android#Blok 6. Vastleggen (15 min)|Blok 6]])

### DoD

- [ ] Eva en Stefan weten wat er voor hen verandert nu de API op https luistert. ([[draaiboek-sessie-2-android#Blok 4. Lonneke laat zien en legt uit (30 min)|Blok 4]])
- [ ] In blok 5 is gezien dat het token meegaat, en dat de header in Logcat als `***` staat. ([[draaiboek-sessie-2-android#Blok 5. Samen één stukje door alle delen (40 min)|Blok 5]])

---

## DoD van de lijst

- [ ] Alle phases zijn afgesloten.
- [ ] Je kunt zonder Eva en Stefan verder aan je eigen schermen. ([[draaiboek-sessie-2-android#De sessie is geslaagd als|Geslaagd]])
- [ ] De open vragen hieronder zijn beantwoord of bewust geaccepteerd.

## Dekking

| Element in het draaiboek | Phase |
| --- | --- |
| [[draaiboek-sessie-2-android#Eerst beslissen (vóór iemand begint)\|Eerst beslissen]]: beslissing 2 (rest), 3, 8 | 0 |
| Eerst beslissen: beslissing 4, 10 | besloten op 15 nov; 10 (`formatPrice`) in phase 0b |
| Eerst beslissen: beslissing 1, 5, 6, 7 | niet in deze lijst: van Eva en Stefan |
| Eerst beslissen: beslissing 9 (de datum) | 8, als voorwaarde |
| [[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)\|L1]] | 1 |
| [[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)\|L2]] | 2 |
| [[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)\|L3]] | 2 |
| [[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)\|L4]] | 3 (gebouwd), 0b (afmaken) |
| [[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)\|L5 (`formatPrice`)]] | 0b |
| Design patterns (A-15, aanvulling A5) | 3b |
| [[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)\|L5]] | 3 |
| [[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)\|L6]] (W-A1) | 4 |
| [[draaiboek-sessie-2-android#A-Lonneke. Sessie en beveiliging (GB-3)\|L7]] | 5 |
| A-Lonneke, "Hoort niet bij GB-3" (US-12, US-13, US-14) | niet in deze lijst: fase 2 |
| [[draaiboek-sessie-2-android#A-Allen. Het staat op main\|A-Allen]] | 6 |
| [[draaiboek-sessie-2-android#Vragen voor Lonneke\|Deel B, vragen voor Lonneke]] | 7 |
| [[draaiboek-sessie-2-android#De toets of het is overgekomen\|De toets]] | 7, 8 |
| [[draaiboek-sessie-2-android#Deel C — De sessie\|Deel C]], blok 1 tot en met 6 | 8 |
| [[draaiboek-sessie-2-android#De sessie is geslaagd als\|Geslaagd]] | 8, DoD van de lijst |
| [[draaiboek-sessie-2-android#Nalopen na zaterdag\|Nalopen]] | 0 |
| [[draaiboek-sessie-2-android#Open vragen\|Open vragen]]: vraag 1 (de code van de proef) | 0 |
| A-Eva en A-Stefan | niet in deze lijst: van hen; alleen E1 als voorwaarde in phase 3 |

## Open vragen

1. ~~**Optie B.**~~ Door jou bevestigd op 14 november (A-13), met je antwoorden op O-7 (b) t/m (e).
2. **W-A1 en je afwezigheid (beslissing 8).** W-A1 is prio 3 en mag naar fase 2 schuiven; leg dat vast als besluit.
3. **De echte telefoon.** Het certificaat geldt alleen voor `localhost` en `10.0.2.2`. Een telefoon bereikt de laptop op een ander adres (O-14). Dat hoort niet bij deze lijst, maar het raakt jouw certificaat.
4. **Het commando voor `.env` op Windows.** Hoe je de omgeving laadt vóór je de server start, staat in de notitie over het klaarzetten van de laptop. Deze lijst herhaalt dat niet.
5. **`EncryptedSharedPreferences`.** Het FTD zegt dat die is afgeschaft, en vraagt jou dat te controleren (O-7).

## Bronnen

- Refactoring.Guru. (z.j.-a). *Builder*. Geraadpleegd op 15 november 2026, van https://refactoring.guru/design-patterns/builder
- Refactoring.Guru. (z.j.-b). *State*. Geraadpleegd op 15 november 2026, van https://refactoring.guru/design-patterns/state

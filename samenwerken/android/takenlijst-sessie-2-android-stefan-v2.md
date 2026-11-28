---
onderwerp: takenlijst van Stefan voor sessie 2 van de app, navigatie en foutafhandeling overdragen (GB-2)
tags: [werk-in-uitvoering, proftaak, periode-2, android, sessie-2, takenlijst]
status: actueel
oorsprong: samenwerking-eva-en-claude
aangemaakt: 2026-11-12
gewijzigd: 2026-11-15
gerelateerd:
  - "[[draaiboek-sessie-2-android]]"
  - "[[FTD-restpartijen-android-app-v2_5]]"
  - "[[stefan-voorbereiding-navigatie-en-fouten]]"
  - "[[instructie-koin-modules]]"
  - "[[overdracht-stefan-startsessie]]"
  - "[[besluiten-ftd-app]]"
---

# Takenlijst sessie 2 app — Stefan: navigatie en foutafhandeling

> **Bijgewerkt op 15 november (v2).** Geschreven op 12 november; op 15 november bijgewerkt met de besluiten van de startsessie en het nalopen van het draaiboek (FTD v2.5, A-20 t/m A-30). Nieuw: de Koin-opzet per feature, het besluit over `loginRequired` (A-27), de open vraag over `code` (O-17) en een leerroute voor je design patterns (Phase 3b). Wat je na de startsessie gemist hebt, staat ook in [[overdracht-stefan-startsessie]].

Bron: [[draaiboek-sessie-2-android]] (bijgewerkt op 15 november). Variant: solo, alleen jouw deel. Plafond: alles wat het draaiboek van Stefan vraagt, vóór, tijdens en direct na de sessie.

**Leeswijzer.** Elk item zegt *wat* je doet en *waarom*. Het *hoe* staat er bewust niet in: dat zoek je zelf uit, zodat je het kunt uitleggen. Elk item eindigt met een link naar de plek in het draaiboek, of met een verwijzing naar een aanvulling (A1..A4). Verwijzingen met §, A-, GB, K-A, O- en NFR gaan naar het [[FTD-restpartijen-android-app-v2_5|FTD]] (v2.5).

**Commando's.** De commando's staan zoals op Linux. Op Windows gebruik je `.\kotlin.bat` in plaats van `./kotlin` (Claude, niet getest op Windows).

## Aanvullingen

### A1 Rood vóór groen bij elke controle
- **Wat het draaiboek niet zegt.** Het draaiboek vraagt alleen bij S6 om een test eerst te laten falen.
- **Waarom de lijst het nodig heeft.** Een test die nooit rood is geweest, bewijst niets. Het FTD vraagt deze werkwijze ook ([[FTD-restpartijen-android-app-v2_5#13. Teststrategie (A-17)|§13]]).
- **Wie bevestigt.** Stefan.

### A2 Volgorde: navigatie eerst, fouten daarna
- **Wat het draaiboek niet zegt.** In welke volgorde je S1 tot en met S6 doet.
- **Waarom de lijst het nodig heeft.** S1 en S2 hebben niets van een ander nodig. S3 heeft de `HttpClient` en de fout-DTO van Eva nodig (E1, E5). S5 ontwerp je samen met Lonneke. Zo wacht je nergens op.
- **Wie bevestigt.** Stefan.

### A3 De documentatie van Navigation 3 nakijken
- **Wat het draaiboek niet zegt.** Dat je de namen controleert in de documentatie van de versie die wij gebruiken.
- **Waarom de lijst het nodig heeft.** Navigation 3 is nieuw. AI-tools en oudere voorbeelden geven vaak code voor de oudere Navigation Compose ([[FTD-restpartijen-android-app-v2_5#8.4 Navigatie|§8.4]]). Wij gebruiken de multiplatform-variant, versie 1.2.0.
- **Wie bevestigt.** Stefan.

### A4 Design patterns leren herkennen
- **Wat het draaiboek niet zegt.** Waar en hoe je jouw twee design patterns gebruikt. Het FTD eist per student twee patterns met een klassen- en een sequencediagram ([[FTD-restpartijen-android-app-v2_5#Besluiten|A-15]]); jij koos Adapter (zelf toegepast) en Facade (herkend in een library).
- **Waarom de lijst het nodig heeft.** In GB-2 en je eigen feature zitten mogelijke plekken voor allebei. Phase 3b is een leerroute zoals Eva die ook volgt. **Je mag het zo doen, maar ook je eigen weg kiezen**: andere plekken of een andere volgorde, zolang je aan het eind beide patterns kunt aanwijzen, uitleggen en tekenen.
- **Wie bevestigt.** Stefan.

## Begrippen

- **Route (`NavKey`).** Een klein object dat zegt welk scherm, en met welke gegevens.
- **Back stack.** Een lijst met routes. De bovenste is het scherm dat je ziet.
- **`NavDisplay`.** Het onderdeel dat de bovenste route omzet naar een scherm.
- **Decorator.** Een instelling op `NavDisplay` die iets toevoegt aan elk scherm, zoals een eigen ViewModel per scherm.
- **`MockEngine`.** Een onderdeel van de Ktor Client dat in een test een vast antwoord geeft, zonder netwerk.
- **Rood vóór groen.** Je laat een test eerst falen. Pas dan weet je dat de test iets controleert.
- **Tijdelijk scherm.** Een scherm met alleen een titel, tot de eigenaar het echte scherm bouwt.
- **Koin-module per feature.** Elke feature en `core` hebben een eigen Koin-module; jouw onderdelen meld je aan in `reservationModule`, Android-code in `reservationPlatformModule` (A-29, [[instructie-koin-modules]]).
- **`loginRequired`.** Een veld in de UiState waarmee een ViewModel meldt dat inloggen nodig is (A-27). Het scherm roept dan `onLoginRequired()` aan; jij zet `LoginRoute` bovenop.
- **Design pattern.** Een beproefde vorm voor een terugkerend ontwerpprobleem, met vaste rollen. Je schrijft het niet als code, je herkent of kiest de vorm.
- **Adapter.** Laat twee onderdelen samenwerken waarvan de interfaces niet op elkaar passen, door de ene te vertalen naar de andere.
- **Facade.** Eén eenvoudige ingang voor een ingewikkeld geheel van klassen of een library.

---

## Phase 0 — Voorbereiden en controleren

Basis: [[draaiboek-sessie-2-android#Eerst beslissen (vóór iemand begint)|Eerst beslissen]], [[draaiboek-sessie-2-android#Wat je nodig hebt om aan je eigen schermen te beginnen|Wat je nodig hebt]]
Raakt: niets in de code
Start na: de startsessie van 14 november

### Voordat je begint
- [x] `main` is opgehaald en bevat het werk van de startsessie, de Koin-modules (PR #91) en de plugin `SessionAuthorization` van Lonneke (PR #90). Controle: `git log --oneline -5` (laat de laatste vijf commits zien).
- [x] De build en de tests zijn groen. Controle: `./kotlin build` (bouwt alle modules) en `./kotlin test -m shared` (draait de tests van de app).
- [x] De app start op de emulator. Controle: Run 'android' in IntelliJ.

### To-do

- [ ] **Lees de besluiten A-22 (`ApiResult` en jouw `ApiError`), A-23 (`AppNavigation.kt`), A-27 (`loginRequired`), A-29 (Koin) en A-30 (de plugin van Lonneke) in het FTD, en de [[instructie-koin-modules|instructie Koin]].**
  Waarom: deze besluiten zijn genomen of aangevuld na je vertrek op de startsessie. S1, S3, S4 en S5 bouwen erop. ([[draaiboek-sessie-2-android#Nalopen na zaterdag|Nalopen]])

- [ ] **Lees in het FTD [[FTD-restpartijen-android-app-v2_5#8.4 Navigatie|§8.4]], [[FTD-restpartijen-android-app-v2_5#8.5 Koppelvlakken tussen de features|§8.5]] en [[FTD-restpartijen-android-app-v2_5#11.2 Foutafhandeling|§11.2]].**
  Waarom: het draaiboek verwijst ernaar. Je controleert of daar staat wat het draaiboek beweert, en je moet het straks zelf uitleggen.
  Verwacht: je kunt de tabel van §11.2 navertellen. Klopt iets niet met het draaiboek, dan gaat het naar de open vragen. ([[draaiboek-sessie-2-android#A-Stefan. Navigatie en foutafhandeling (GB-2)|A-Stefan]])

- [x] **Zoek in `libs.versions.toml` op welke versie van Navigation 3 en van `lifecycle-viewmodel-navigation3` op `main` staat.**
  Waarom: de volgende stap controleert de documentatie van precies die versie. (aanvulling, [[#A3 De documentatie van Navigation 3 nakijken|A3]])

- [x] **Zoek in de documentatie van Navigation 3 op hoe de twee decorators uit §8.4 heten en wat ze doen.**
  Waarom: zonder de decorators hangt elke ViewModel aan de Activity en niet aan het scherm.
  Als het misgaat: vind je een andere naam dan in §8.4, bouw dan nog niets en vraag het na bij Eva. (aanvulling, [[#A3 De documentatie van Navigation 3 nakijken|A3]])

- [x] **Spreek met Eva beslissing 1 af: jij bouwt de plek waar een antwoord een `ApiResult` wordt; wat levert Eva daarvoor.**
  Waarom: het FTD geeft Eva de fout-DTO en jou het omzetten. Zonder afspraak bouwen jullie het allebei, of geen van beiden. ([[draaiboek-sessie-2-android#Eerst beslissen (vóór iemand begint)|beslissing 1]])

- [x] **Spreek met Lonneke beslissing 3 af: wat staat er in fase 1 achter `LoginRoute`.**
  Waarom: K-A3 is zonder scherm niet te testen. Beslissing 4 is al besloten: de plugin van Lonneke wist de sessie bij een `401`, jij reageert op `loginRequired` (A-27, A-30). ([[draaiboek-sessie-2-android#Eerst beslissen (vóór iemand begint)|beslissing 3]])

- [ ] **Beslis of jij de tab Mijn aanbod verbergt voor een afhaler (beslissing 6), en meld het aan de groep.**
  Waarom: het staat in het FTD (§16.2), maar bij geen GB-deel. ([[draaiboek-sessie-2-android#Eerst beslissen (vóór iemand begint)|beslissing 6]])

- [ ] **Maak de branch voor GB-2.**
  Waarom: ieder werkt op een eigen branch en maakt een pull request naar `main`. De naam volgt de afspraak van de startsessie. ([[draaiboek-sessie-2-android#A-Allen. Het staat op main|A-Allen]])

### DoD

- [ ] De paragrafen van het FTD zeggen wat het draaiboek beweert, of de afwijking staat bij de open vragen. ([[draaiboek-sessie-2-android#A-Stefan. Navigatie en foutafhandeling (GB-2)|A-Stefan]])
- [ ] Je weet de namen van de decorators, voor de versie op `main`. (aanvulling, [[#A3 De documentatie van Navigation 3 nakijken|A3]])
- [ ] Beslissing 1, 3 en 6 hebben een antwoord en staan genoteerd op een plek die jullie alle drie zien (4 is besloten). ([[draaiboek-sessie-2-android#Eerst beslissen (vóór iemand begint)|Eerst beslissen]])

---

## Phase 1 — S1: `NavDisplay` en de routes

Basis: [[draaiboek-sessie-2-android#A-Stefan. Navigatie en foutafhandeling (GB-2)|S1]]
Raakt: `shared/src/nl/restpartijen/app/App.kt`, `shared/src/nl/restpartijen/app/AppNavigation.kt`, `shared/src/nl/restpartijen/app/core/navigation/`
Start na: phase 0 (wacht niet op Eva of Lonneke, zie A2)

### To-do

- [x] **Laat `App.kt` de schermen tonen met `NavDisplay` en een back stack.**
  Waarom: dit is het hart van de navigatie. Naar een scherm gaan is een route op de stapel leggen; teruggaan is haar eraf halen. ([[draaiboek-sessie-2-android#A-Stefan. Navigatie en foutafhandeling (GB-2)|S1]])

- [x] **Koppel in `AppNavigation.kt` elke route aan een scherm.**
  Waarom: één plek moet alle schermen kennen, terwijl features elkaar niet mogen gebruiken ([[FTD-restpartijen-android-app-v2_5#8.3 Modules en packages (A-14)|§8.3]]). ([[draaiboek-sessie-2-android#A-Stefan. Navigatie en foutafhandeling (GB-2)|S1]])

- [x] **Geef `NavDisplay` de twee decorators uit §8.4.**
  Waarom: dan krijgt elk scherm een eigen ViewModel, die verdwijnt als het scherm van de stapel gaat. ([[draaiboek-sessie-2-android#A-Stefan. Navigatie en foutafhandeling (GB-2)|S1]])

- [x] **Geef een scherm een `onBack` mee, niet de back stack.**
  Waarom: een scherm hoort niets van de navigatie te weten. Zo blijft het los te testen (§8.4). ([[draaiboek-sessie-2-android#A-Stefan. Navigatie en foutafhandeling (GB-2)|S1]])

- [x] **Controleer op de emulator: heen, terug, en het toestel draaien.**
  Verwacht: na draaien sta je op hetzelfde scherm. Een scherm dat je opnieuw opent, begint met een nieuwe toestand. ([[draaiboek-sessie-2-android#A-Stefan. Navigatie en foutafhandeling (GB-2)|S1]])

- [x] **Haal één decorator tijdelijk weg en herhaal de controle. Zet hem terug.**
  Waarom: je ziet dan zelf wat er zonder misgaat. Dat is vraag 2 van deel B. (aanvulling, [[#A1 Rood vóór groen bij elke controle|A1]])

### DoD

- [x] Heen, terug en draaien werken op de emulator. ([[draaiboek-sessie-2-android#A-Stefan. Navigatie en foutafhandeling (GB-2)|S1]])
- [x] Je hebt gezien wat er zonder decorator misgaat, en kunt het vertellen. (aanvulling, [[#A1 Rood vóór groen bij elke controle|A1]])

---

## Phase 2 — S2: de drie tabs

Basis: [[draaiboek-sessie-2-android#A-Stefan. Navigatie en foutafhandeling (GB-2)|S2]]
Raakt: `shared/src/nl/restpartijen/app/App.kt`, `shared/src/nl/restpartijen/app/AppNavigation.kt`, `shared/composeResources/values/strings.xml`, `shared/composeResources/values-nl/strings.xml`, `shared/composeResources/drawable/`
Start na: phase 1

### To-do

- [ ] **Bouw een bottom navigation met drie tabs: Ontdekken, Reserveringen en Mijn aanbod.**
  Waarom: dit is de navigatiestructuur van de app (§8.4). ([[draaiboek-sessie-2-android#A-Stefan. Navigatie en foutafhandeling (GB-2)|S2]])

- [ ] **Zet achter elke tab een tijdelijk scherm.**
  Waarom: Eva, Lonneke en jij zetten later het eigen scherm op die plek. ([[draaiboek-sessie-2-android#A-Stefan. Navigatie en foutafhandeling (GB-2)|S2]])

- [ ] **Zet de namen van de tabs in `strings.xml`, in NL en EN, en de iconen als XML in `composeResources/drawable/`.**
  Waarom: teksten buiten de resources mogen niet (NFR-A1). Iconen volgen A-19 ([[FTD-restpartijen-android-app-v2_5#8.6 Technische keuzes (A-08)|§8.6]]). ([[draaiboek-sessie-2-android#A-Stefan. Navigatie en foutafhandeling (GB-2)|S2]])

- [ ] **Verberg de tab Mijn aanbod voor een afhaler, als beslissing 6 dat aan jou geeft. Lees de rol uit `SessionStore`; gebruik de nepversie.**
  Waarom: de tab is alleen zinvol voor een aanbieder. Een verborgen tab is geen beveiliging; de API beslist (§16.2). ([[draaiboek-sessie-2-android#Eerst beslissen (vóór iemand begint)|beslissing 6]])

- [ ] **Zoek uit wat er met de back stack gebeurt als je van tab wisselt, en kies bewust.**
  Waarom: dit is vraag 5 van deel B. De documentatie van Navigation 3 beschrijft hoe je dit kunt doen. ([[draaiboek-sessie-2-android#Vragen voor Stefan|vraag 5]])

### DoD

- [ ] Elke tab opent het eigen scherm, in NL en EN. Bewijs: de emulator in beide talen. ([[draaiboek-sessie-2-android#A-Stefan. Navigatie en foutafhandeling (GB-2)|S2]])
- [ ] Je kunt vertellen wat er met de back stack gebeurt als je van tab wisselt. ([[draaiboek-sessie-2-android#A-Stefan. Navigatie en foutafhandeling (GB-2)|S2]])

---

## Phase 3 — S3: van antwoord naar `ApiResult`

Basis: [[draaiboek-sessie-2-android#A-Stefan. Navigatie en foutafhandeling (GB-2)|S3]]
Raakt: `shared/src/nl/restpartijen/app/core/network/`, `shared/test/nl/restpartijen/app/core/network/`
Start na: phase 0, beslissing 1, en E1 en E5 van Eva

### Voordat je begint
- [ ] De `HttpClient` en de fout-DTO van Eva staan op `main` of op haar branch. Controle: vraag het Eva, of kijk in `shared/src/nl/restpartijen/app/core/network/`.

### To-do

- [ ] **Schrijf eerst de tests met `MockEngine`: `400` met `field`, `401`, `409`, `500` en een time-out.**
  Waarom: de tests leggen vast wat je verwacht, per rij van de tabel in §11.2. Het voorbeeld van Eva (E7) laat zien hoe zo'n test eruitziet.
  Verwacht: rood, want de omzetting bestaat nog niet. ([[draaiboek-sessie-2-android#A-Stefan. Navigatie en foutafhandeling (GB-2)|S3]])

- [ ] **Beantwoord eerst de open vraag over `code` (O-17): het foutmodel van de API heeft een `code`, `ApiError.Http` niet. Zoek uit of de app `code` nodig heeft, hoe je het zou doen en waarom, en noteer je besluit.**
  Waarom: verandert `ApiError`, dan raakt dat elke repository. Beslis het dus vóór je de omzetting bouwt. ([[FTD-restpartijen-android-app-v2_5#19.2 Open punten|O-17]])

- [ ] **Bouw de ene plek in `core/network/` die van een antwoord of een netwerkfout een `ApiResult` maakt: een fout van de server wordt `ApiError.Http(status, message, field)`, geen antwoord of een time-out `ApiError.Network` (A-22).**
  Waarom: elke repository geeft fouten dan op dezelfde manier door. Een time-out heeft geen statuscode en moet er ook doorheen. Let op: een `401` gaat onveranderd door de plugin van Lonneke heen; jij maakt er `ApiError.Http(401)` van.
  Verwacht: de tests zijn groen. ([[draaiboek-sessie-2-android#A-Stefan. Navigatie en foutafhandeling (GB-2)|S3]])

- [ ] **Laat de `message` van de server naar de log gaan, en nergens anders heen.**
  Waarom: de tekst van de server is Engels en niet voor de gebruiker bedoeld (§11.2). ([[draaiboek-sessie-2-android#A-Stefan. Navigatie en foutafhandeling (GB-2)|S3]])

### DoD

- [ ] De vijf tests zijn groen en zijn één keer rood geweest. Bewijs: de testuitvoer. ([[draaiboek-sessie-2-android#A-Stefan. Navigatie en foutafhandeling (GB-2)|S3]])

---

## Phase 3b — Design patterns leren herkennen (je eigen weg mag ook)

Basis: aanvulling [[#A4 Design patterns leren herkennen|A4]], [[FTD-restpartijen-android-app-v2_5#Besluiten|A-15]], [[besluiten-ftd-app#Gekozen design patterns (14 nov)|gekozen patterns]]
Raakt: geen code; een naslagnotitie in de vault
Start na: phase 3, want dan heb je de omzetting zelf gebouwd

Per pattern dezelfde vijf stappen. De plekken hieronder zijn **vragen om uit te zoeken**, geen vast antwoord. Vind je een betere plek, gebruik die dan, en noteer waarom.

### To-do: Adapter (zelf toegepast)

- [ ] **1. Begrijpen. Lees wat Adapter is: welk probleem het oplost en welke rollen het heeft (Client, Target, Adaptee, Adapter).**
  Bron: Refactoring.Guru (z.j.-a).
  Verwacht: je kunt in twee zinnen zeggen welk probleem Adapter oplost.

- [ ] **2. Herkennen. Is jouw omzetting uit S3 een Adapter? Mogelijke lezing: het antwoord en de fouten van Ktor (`HttpResponse`, exceptions) zijn de Adaptee, `ApiResult` is de Target, jouw omzetting is de Adapter, en de repository is de Client.**
  Waarom: dit is een plek waar je het pattern zelf toepast. Klopt de lezing niet, zoek dan een andere plek, bijvoorbeeld een DTO die je omzet naar een app-model in je eigen feature.
  Verwacht: je wijst bij elke rol het bestand en de regel aan.

- [ ] **3. Voelen. Bedenk wat er zonder jouw omzetting zou gebeuren: waar zouden de repository's dan zelf met Ktor-exceptions moeten werken?**
  Waarom: dat is de meerwaarde. Noteer het in drie zinnen.

- [ ] **4. Vastleggen. Maak een klassendiagram en een sequencediagram, en schrijf drie zinnen meerwaarde, in een naslagnotitie.**
  Waarom: A-15, §1.5.

- [ ] **5. Navertellen. Leg het in sessie 2 uit aan Eva of Lonneke.** ([[draaiboek-sessie-2-android#De toets of het is overgekomen|De toets]])

### To-do: Facade (herkend in een library)

- [ ] **1. Begrijpen. Lees wat Facade is: welk probleem het oplost, en hoe het verschilt van Adapter.**
  Bron: Refactoring.Guru (z.j.-b).
  Verwacht: je kunt het verschil met Adapter in één zin zeggen.

- [ ] **2. Herkennen. Zoek een Facade in een library die jij gebruikt. Mogelijke plekken om uit te zoeken: `NavDisplay` van Navigation 3 (één composable boven back stack, decorators en ViewModel-stores), of later de `FusedLocationProviderClient` (één eenvoudige API boven GPS, wifi en zendmasten).**
  Waarom: dit zijn ideeën, nog niet nagekeken in de documentatie (Claude). Kijk in de docs wat het onderdeel verbergt.
  Verwacht: je kunt zeggen welk ingewikkeld geheel erachter zit, en welke eenvoudige ingang jij gebruikt.

- [ ] **3. Voelen. Bedenk wat jij zelf had moeten doen zonder die Facade.**

- [ ] **4. Vastleggen. Een klassen- en een sequencediagram, en drie zinnen meerwaarde, in dezelfde naslagnotitie.**

- [ ] **5. Navertellen. Leg het in sessie 2 uit aan Eva of Lonneke.**

### DoD

- [ ] Je kunt bij beide patterns de plek in de code aanwijzen en de meerwaarde in eigen woorden zeggen. (aanvulling, [[#A4 Design patterns leren herkennen|A4]])
- [ ] De naslagnotitie bestaat, met per pattern een klassen- en een sequencediagram. (A-15)
- [ ] In [[besluiten-ftd-app#Gekozen design patterns (14 nov)|de besluitennotitie]] staat bij jou waar beide patterns te zien zijn.

---

## Phase 4 — S4: van fout naar melding

Basis: [[draaiboek-sessie-2-android#A-Stefan. Navigatie en foutafhandeling (GB-2)|S4]]
Raakt: `shared/src/nl/restpartijen/app/core/error/`, `shared/test/nl/restpartijen/app/core/error/`, `shared/composeResources/values/strings.xml`, `shared/composeResources/values-nl/strings.xml`
Start na: phase 3

### To-do

- [ ] **Werk uit welke tekst bij welke statuscode en welk veld hoort. Schrijf het op als een tabel.**
  Waarom: het FTD geeft het gedrag per statuscode, maar niet de teksten. Dat is jouw deel (§11.2). ([[draaiboek-sessie-2-android#A-Stefan. Navigatie en foutafhandeling (GB-2)|S4]])

- [ ] **Schrijf eerst een test per rij van de tabel in §11.2.**
  Verwacht: rood. ([[draaiboek-sessie-2-android#A-Stefan. Navigatie en foutafhandeling (GB-2)|S4]])

- [ ] **Bouw de ene plek in `core/error/` die bij een fout de tekst kiest. Zet de teksten in `strings.xml`, in NL en EN.**
  Waarom: twee talen is een toetsvoorwaarde. Eén plek voorkomt dat elk scherm zijn eigen foutteksten verzint.
  Verwacht: de tests zijn groen. ([[draaiboek-sessie-2-android#A-Stefan. Navigatie en foutafhandeling (GB-2)|S4]])

- [ ] **Schrijf een test die laat zien dat de `message` van de server nooit de tekst op het scherm wordt.**
  Waarom: dit is het besluit van 12 november, en het geldt ook bij `409` en bij een onbekende fout. ([[draaiboek-sessie-2-android#A-Stefan. Navigatie en foutafhandeling (GB-2)|S4]])

### DoD

- [ ] Er is een test per rij van §11.2, groen. Bewijs: de testuitvoer. ([[draaiboek-sessie-2-android#A-Stefan. Navigatie en foutafhandeling (GB-2)|S4]])
- [ ] De tabel met teksten staat op een plek waar Eva hem in het FTD kan zetten. ([[draaiboek-sessie-2-android#Blok 6. Vastleggen (15 min)|Blok 6]])

---

## Phase 5 — S5: inloggen en terugkeren (K-A3)

Basis: [[draaiboek-sessie-2-android#A-Stefan. Navigatie en foutafhandeling (GB-2)|S5]]
Raakt: `shared/src/nl/restpartijen/app/AppNavigation.kt`, `shared/src/nl/restpartijen/app/core/navigation/`
Start na: phase 1, phase 3, en beslissing 3

### To-do

- [x] **Het ontwerp: hoe een ViewModel meldt dat inloggen nodig is, en hoe de actie daarna opnieuw start.** Besloten op 15 november (A-27, [[FTD-restpartijen-android-app-v2_5#8.5 Koppelvlakken tussen de features|§8.5]]): een veld `loginRequired` in de UiState; de ViewModel onthoudt de actie en voert haar uit zodra `SessionStore.session` weer een sessie geeft. Zie ook [[overdracht-stefan-startsessie]].

- [ ] **Bouw het: als een scherm `onLoginRequired()` aanroept, komt `LoginRoute` bovenop de back stack. Na een geslaagde login gaat zij er weer af; de ViewModel voert de actie alsnog uit.**
  Waarom: het scherm eronder blijft staan. Niemand hoeft te onthouden waar de gebruiker vandaan kwam (§8.5). ([[draaiboek-sessie-2-android#A-Stefan. Navigatie en foutafhandeling (GB-2)|S5]])

- [ ] **Controleer op de emulator, met de nepversie van `SessionStore` en het tijdelijke scherm van beslissing 3.**
  Verwacht: een actie zonder sessie opent de inlogroute. Na het inloggen sta je op hetzelfde scherm en is de actie uitgevoerd. ([[draaiboek-sessie-2-android#A-Stefan. Navigatie en foutafhandeling (GB-2)|S5]])

### DoD

- [ ] De vier stappen van K-A3 werken op de emulator. ([[draaiboek-sessie-2-android#A-Stefan. Navigatie en foutafhandeling (GB-2)|S5]])
- [ ] Je kunt het ontwerp van A-27 in eigen woorden uitleggen, en Lonneke herkent het in haar plugin.

---

## Phase 6 — S6: jouw eigen voorbeeld

Basis: [[draaiboek-sessie-2-android#A-Stefan. Navigatie en foutafhandeling (GB-2)|S6]]
Raakt: `shared/src/nl/restpartijen/app/AppNavigation.kt`, `shared/test/nl/restpartijen/app/core/`
Start na: phase 4

### To-do

- [ ] **Laat `ProductDetailRoute(productId)` een tijdelijk scherm openen dat het `productId` toont.**
  Waarom: dit is K-A1. Lonneke zet hier later het echte scherm neer. ([[draaiboek-sessie-2-android#A-Stefan. Navigatie en foutafhandeling (GB-2)|S6]])

- [ ] **Schrijf één test waarin een fout van `MockEngine` tot aan de gekozen tekst loopt.**
  Waarom: dit is het voorbeeld dat Eva en Lonneke nadoen. ([[draaiboek-sessie-2-android#A-Stefan. Navigatie en foutafhandeling (GB-2)|S6]])

- [ ] **Breek één regel van de omzetting, bijvoorbeeld `409` krijgt de tekst van `404`. Kijk of de test faalt. Zet het terug.**
  Waarom: de controle van S6. ([[draaiboek-sessie-2-android#A-Stefan. Navigatie en foutafhandeling (GB-2)|S6]])

### DoD

- [ ] De route met een gegeven werkt op de emulator. ([[draaiboek-sessie-2-android#A-Stefan. Navigatie en foutafhandeling (GB-2)|S6]])
- [ ] De test is groen en is één keer rood geweest. ([[draaiboek-sessie-2-android#A-Stefan. Navigatie en foutafhandeling (GB-2)|S6]])

---

## Phase 7 — Op `main`

Basis: [[draaiboek-sessie-2-android#A-Allen. Het staat op main|A-Allen]]
Raakt: je pull request
Start na: phase 1 tot en met 6

### To-do

- [ ] **Draai ktlint en de build.**
  Verwacht: nul meldingen en geen warnings (NFR-A2). ([[draaiboek-sessie-2-android#A-Allen. Het staat op main|A-Allen]])

- [ ] **Draai `./kotlin test -m shared`.**
  Verwacht: alles groen. ([[draaiboek-sessie-2-android#A-Allen. Het staat op main|A-Allen]])

- [ ] **Maak een pull request naar `main` en vraag een review van Eva en Lonneke allebei.**
  Waarom: wat niet op `main` staat, bestaat voor de anderen niet. Je raakt `core/`, `App.kt` en `AppNavigation.kt`, dus de afgesproken reviewregel vraagt twee reviews. Meld je onderdelen aan in `reservationModule`, niet in `AppModule.kt` ([[instructie-koin-modules]]). ([[draaiboek-sessie-2-android#A-Allen. Het staat op main|A-Allen]])

- [ ] **Verwerk de opmerkingen en laat een ander mergen.**
  Waarom: je merget je eigen pull request niet. ([[draaiboek-sessie-2-android#A-Allen. Het staat op main|A-Allen]])

### DoD

- [ ] GB-2 staat op `main`. Bewijs: de gemergede pull request. ([[draaiboek-sessie-2-android#A-Allen. Het staat op main|A-Allen]])
- [ ] De app start vanaf `main`. ([[draaiboek-sessie-2-android#A-Allen. Het staat op main|A-Allen]])

---

## Phase 8 — Deel B: jouw uitleg voorbereiden

Basis: [[draaiboek-sessie-2-android#Vragen voor Stefan|Vragen voor Stefan]], [[draaiboek-sessie-2-android#De toets of het is overgekomen|De toets]]
Raakt: je eigen aantekeningen
Start na: phase 7

### To-do

- [ ] **Beantwoord de tien vragen voor Stefan in eigen woorden, en noteer bij elk antwoord het bestand en de regel.**
  Waarom: in blok 3 wijs je de plek in de code aan. Voorlezen mag niet. ([[draaiboek-sessie-2-android#Vragen voor Stefan|Vragen voor Stefan]])

- [ ] **Lees de vragen voor Eva en Lonneke.**
  Waarom: jij vertelt in blok 2 of 4 één punt van hen na. ([[draaiboek-sessie-2-android#De toets of het is overgekomen|De toets]])

### DoD

- [ ] Bij elke vraag staat een antwoord en een plek in de code. ([[draaiboek-sessie-2-android#Vragen voor Stefan|Vragen voor Stefan]])

---

## Phase 9 — De sessie

Basis: [[draaiboek-sessie-2-android#Deel C — De sessie|Deel C]]
Raakt: `shared/src/nl/restpartijen/app/example/`
Start na: phase 8, en de datum van sessie 2

### To-do

- [ ] **Blok 1: haal `main` op, bouw, draai de tests, start de server en de app.** ([[draaiboek-sessie-2-android#Blok 1. Dezelfde basis (10 min)|Blok 1]])
- [ ] **Blok 2: draai zelf de coverage-meting en open het rapport. Vertel één punt van Eva na, als jij aan de beurt bent.**
  Verwacht: je kunt het percentage van één klasse noemen. ([[draaiboek-sessie-2-android#Blok 2. Eva laat zien en legt uit (30 min)|Blok 2]])
- [ ] **Blok 3: laat S1 tot en met S6 zien en beantwoord de vragen.**
  Verwacht: een ander vertelt één punt na. ([[draaiboek-sessie-2-android#Blok 3. Stefan laat zien en legt uit (30 min)|Blok 3]])
- [ ] **Blok 4: vertel één punt van Lonneke na, als jij aan de beurt bent.** ([[draaiboek-sessie-2-android#De toets of het is overgekomen|De toets]])
- [ ] **Blok 5: bouw mee aan het voorbeeldscherm met de echte API.**
  Waarom: hier blijkt of de drie delen op elkaar passen. ([[draaiboek-sessie-2-android#Blok 5. Samen één stukje door alle delen (40 min)|Blok 5]])
- [ ] **Blok 6: geef de tabel met teksten en het ontwerp van K-A3 aan Eva voor het FTD. Maak de branch voor je eerste story.** ([[draaiboek-sessie-2-android#Blok 6. Vastleggen (15 min)|Blok 6]])

### DoD

- [ ] Eva en Lonneke weten welke bestanden zij aanraken voor een nieuw scherm en een nieuwe melding. ([[draaiboek-sessie-2-android#Blok 3. Stefan laat zien en legt uit (30 min)|Blok 3]])
- [ ] Het voorbeeldscherm toont met de server uit de melding en de knop "Opnieuw". ([[draaiboek-sessie-2-android#Blok 5. Samen één stukje door alle delen (40 min)|Blok 5]])

---

## DoD van de lijst

- [ ] Alle phases zijn afgesloten.
- [ ] Je kunt zonder Eva en Lonneke verder aan je eigen schermen. ([[draaiboek-sessie-2-android#De sessie is geslaagd als|Geslaagd]])
- [ ] De open vragen hieronder zijn beantwoord of bewust geaccepteerd.

## Dekking

| Element in het draaiboek | Phase |
| --- | --- |
| [[draaiboek-sessie-2-android#Eerst beslissen (vóór iemand begint)\|Eerst beslissen]]: beslissing 1, 3, 6 | 0, 2, 5 |
| Eerst beslissen: beslissing 4 | besloten op 15 nov (A-27, A-30) |
| Eerst beslissen: beslissing 2, 5, 7, 8 | niet in deze lijst: van Eva en Lonneke |
| Eerst beslissen: beslissing 9 (de datum) | 9, als voorwaarde |
| [[draaiboek-sessie-2-android#A-Stefan. Navigatie en foutafhandeling (GB-2)\|S1]] | 1 |
| [[draaiboek-sessie-2-android#A-Stefan. Navigatie en foutafhandeling (GB-2)\|S2]] | 2 |
| [[draaiboek-sessie-2-android#A-Stefan. Navigatie en foutafhandeling (GB-2)\|S3]] | 3 |
| Design patterns (A-15, aanvulling A4) | 3b |
| [[draaiboek-sessie-2-android#A-Stefan. Navigatie en foutafhandeling (GB-2)\|S4]] | 4 |
| [[draaiboek-sessie-2-android#A-Stefan. Navigatie en foutafhandeling (GB-2)\|S5]] | 5 |
| [[draaiboek-sessie-2-android#A-Stefan. Navigatie en foutafhandeling (GB-2)\|S6]] | 6 |
| [[draaiboek-sessie-2-android#A-Allen. Het staat op main\|A-Allen]] | 7 |
| [[draaiboek-sessie-2-android#Vragen voor Stefan\|Deel B, vragen voor Stefan]] | 2 (vraag 5), 8 |
| [[draaiboek-sessie-2-android#De toets of het is overgekomen\|De toets]] | 8, 9 |
| [[draaiboek-sessie-2-android#Deel C — De sessie\|Deel C]], blok 1 tot en met 6 | 9 |
| [[draaiboek-sessie-2-android#De sessie is geslaagd als\|Geslaagd]] | 9, DoD van de lijst |
| [[draaiboek-sessie-2-android#Nalopen na zaterdag\|Nalopen]] | 0 |
| A-Eva en A-Lonneke | niet in deze lijst: van hen; alleen E1 en E5 als voorwaarde in phase 3 |

## Open vragen

1. ~~**De vorm van `ApiError`.**~~ Gekozen door jou: `Http` en `Network` (A-22). Nieuw open: of `code` erbij moet (O-17, phase 3).
2. **De UI-test met `409`.** Het voorstel voor jouw UI-test is "intrekken met `409`" (O-11). Dat moet jij nog bevestigen, vóór fase 2. UI-tests draaien nog niet met de toolchain (O-12); deze lijst vraagt daarom alleen unit tests.
3. **De tab Mijn aanbod (beslissing 6).** Het FTD wijst dit aan niemand toe. Tot jij beslist, staat het in deze lijst als "als beslissing 6 dat aan jou geeft".
4. **Navigatietest.** Het FTD vraagt bij de UI-tests ook één navigatietest (§13). Van wie die is, en of hij bij GB-2 hoort, staat nergens.

## Bronnen

- Refactoring.Guru. (z.j.-a). *Adapter*. Geraadpleegd op 15 november 2026, van https://refactoring.guru/design-patterns/adapter
- Refactoring.Guru. (z.j.-b). *Facade*. Geraadpleegd op 15 november 2026, van https://refactoring.guru/design-patterns/facade

---
onderwerp: takenlijst van Eva voor sessie 2 van de app, de data- en netwerklaag en de coverage-meting overdragen (GB-1, GB-7)
tags: [werk-in-uitvoering, proftaak, periode-2, android, sessie-2, takenlijst]
status: actueel
oorsprong: samenwerking-eva-en-claude
aangemaakt: 2026-11-12
gewijzigd: 2026-11-15
gerelateerd:
  - "[[draaiboek-sessie-2-android]]"
  - "[[FTD-restpartijen-android-app-v2_5]]"
  - "[[instructie-koin-modules]]"
  - "[[besluiten-ftd-app]]"
---

# Takenlijst sessie 2 app — Eva: data- en netwerklaag en coverage

> **Bijgewerkt op 15 november.** Geschreven op 12 november; op 15 november bijgewerkt met de besluiten van de startsessie en het nalopen van het draaiboek (FTD v2.5, A-20 t/m A-30). Nieuw: E0 (klaar) en een leerroute voor je design patterns (Phase 1b).

Bron: [[draaiboek-sessie-2-android]] (bijgewerkt op 15 november). Variant: solo, alleen jouw deel. Plafond: alles wat het draaiboek van Eva vraagt, vóór, tijdens en direct na de sessie.

**Leeswijzer.** Elk item zegt *wat* je doet en *waarom*. Het *hoe* staat er bewust niet in. Elk item eindigt met een link naar de plek in het draaiboek, of met een verwijzing naar een aanvulling (A1..A4). Verwijzingen met §, A-, GB, K-A, O- en NFR gaan naar het [[FTD-restpartijen-android-app-v2_5|FTD]] (v2.5).

## Aanvullingen

### A1 Rood vóór groen bij elke controle
- **Wat het draaiboek niet zegt.** Het draaiboek vraagt alleen bij E7 om een test eerst te laten falen.
- **Waarom de lijst het nodig heeft.** Een test die nooit rood is geweest, bewijst niets. Het FTD vraagt deze werkwijze ook ([[FTD-restpartijen-android-app-v2_5#13. Teststrategie (A-17)|§13]]).
- **Wie bevestigt.** Eva.

### A2 De proef is naslag
- **Wat het draaiboek niet zegt.** Hoe je de code van de proef gebruikt. Die staat plat in één package, op een branch die niet naar GitHub gaat.
- **Waarom de lijst het nodig heeft.** GB-1 komt in de packages van het FTD ([[FTD-restpartijen-android-app-v2_5#8.3 Modules en packages (A-14)|§8.3]]). Wat je uit de proef overneemt, moet je kunnen uitleggen in deel B.
- **Wie bevestigt.** Eva.

### A3 Versies en documentatie nakijken
- **Wat het draaiboek niet zegt.** Dat je de namen van instellingen controleert in de documentatie van de versie die op `main` staat.
- **Waarom de lijst het nodig heeft.** Het FTD noemt Ktor 3.6.0 en Koin 4.2.2 ([[FTD-restpartijen-android-app-v2_5#8.6 Technische keuzes (A-08)|§8.6]]). Namen veranderen per versie, en AI-tools geven vaak oudere namen.
- **Wie bevestigt.** Eva.

### A4 Design patterns leren herkennen
- **Wat het draaiboek niet zegt.** Waar en hoe je jouw twee design patterns gebruikt. Het FTD eist per student twee patterns met een klassen- en een sequencediagram ([[FTD-restpartijen-android-app-v2_5#Besluiten|A-15]]); jij koos Strategy (zelf toegepast) en Chain of Responsibility (herkend in een library).
- **Waarom de lijst het nodig heeft.** Design patterns zijn nieuw voor je. In E1 zitten ze allebei al, in de Ktor Client: de engine is een Strategy, de plugins vormen een Chain of Responsibility. Daar leer je ze herkennen vóór je ze zelf toepast in je eigen feature. Je doel is 2×2: elk pattern zelf toepassen én in een library aanwijzen.
- **Wie bevestigt.** Eva (15 nov).

## Begrippen

- **Nepversie (fake).** Een eenvoudige vervanger van een interface. Een test gebruikt hem in plaats van de echte implementatie.
- **`MockEngine`.** Een onderdeel van de Ktor Client dat in een test een vast antwoord geeft, zonder netwerk.
- **DTO.** Een klasse die precies de vorm van de JSON van de API heeft.
- **Rood vóór groen.** Je laat een test eerst falen. Pas dan weet je dat de test iets controleert.
- **Debuggable.** Een build die je vanuit IntelliJ start. Een release-build is dat niet.
- **Koin-module per feature.** Elke feature en `core` hebben een eigen Koin-module; `appModule` voegt ze samen, `platformModule` bevat de Android-implementaties (A-29, [[instructie-koin-modules]]).
- **Design pattern.** Een beproefde vorm voor een terugkerend ontwerpprobleem, met vaste rollen. Je schrijft het niet als code, je herkent of kiest de vorm.
- **Strategy.** Een familie uitwisselbare manieren om iets te doen, elk in een eigen klasse; de gebruiker (Context) kiest er één en merkt het verschil niet.
- **Chain of Responsibility.** Een request gaat langs een keten van handlers; elke handler doet zijn deel of geeft het door.
- **Plugin (Ktor Client).** Een onderdeel dat in de `HttpClient` wordt geïnstalleerd en op vaste momenten (hooks) meekijkt met elk request en antwoord, zoals `SessionAuthorization` van Lonneke.

---

## Phase 0 — Voorbereiden en controleren

Basis: [[draaiboek-sessie-2-android#Eerst beslissen (vóór iemand begint)|Eerst beslissen]], [[draaiboek-sessie-2-android#Wat je nodig hebt om aan je eigen schermen te beginnen|Wat je nodig hebt]]
Raakt: niets in de code
Start na: de startsessie van 14 november

### Voordat je begint
- [ ] `main` is opgehaald en bevat het werk van de startsessie, de Koin-modules (PR #91) en de plugin `SessionAuthorization` van Lonneke (PR #90). Controle: `git log --oneline -5` (laat de laatste vijf commits zien).
- [ ] De build en de tests zijn groen. Controle: `./kotlin build` (bouwt alle modules) en `./kotlin test -m shared` (draait de tests van de app).

### To-do

- [ ] **Lees de besluiten A-20 t/m A-30 in het FTD en de [[instructie-koin-modules|instructie Koin]].**
  Waarom: daar staat wat de startsessie en het nalopen hebben besloten; E1, E5, E6 en E7 bouwen erop. ([[draaiboek-sessie-2-android#Nalopen na zaterdag|Nalopen]])

- [ ] **Lees in het FTD [[FTD-restpartijen-android-app-v2_5#8.6 Technische keuzes (A-08)|§8.6]] (de rijen Ktor Client en Koin), [[FTD-restpartijen-android-app-v2_5#8.7 Kwaliteitsscenario's|§8.7]], [[FTD-restpartijen-android-app-v2_5#11.1 Gebruik van de API|§11.1]], [[FTD-restpartijen-android-app-v2_5#11.3 Https|§11.3]], [[FTD-restpartijen-android-app-v2_5#11.4 Android 17 en het lokale netwerk|§11.4]] en [[FTD-restpartijen-android-app-v2_5#16.6 Logging|§16.6]].**
  Waarom: het draaiboek verwijst ernaar als onderbouwing. Je controleert of daar staat wat het draaiboek beweert.
  Verwacht: elke paragraaf zegt wat het draaiboek ervan zegt. Zo niet, dan gaat het naar de open vragen. ([[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)|A-Eva]])

- [ ] **Zoek in `libs.versions.toml` en `shared/module.yaml` op welke versies van Ktor en Koin op `main` staan.**
  Waarom: de volgende stap controleert de documentatie van precies die versies. (aanvulling, [[#A3 Versies en documentatie nakijken|A3]])

- [ ] **Controleer in de documentatie van die versies hoe `expectSuccess`, de time-outs, het loggen en de `MockEngine` heten en werken.**
  Waarom: je bouwt op wat de documentatie zegt, niet op wat een AI-tool voorstelt.
  Als het misgaat: wijkt een naam of advies af van §8.6, bouw dan nog niets en zet het bij de open vragen. (aanvulling, [[#A3 Versies en documentatie nakijken|A3]])

- [ ] **Spreek met Stefan beslissing 1 af: wie bouwt de plek waar een antwoord een `ApiResult` wordt, en wat lever jij daarvoor.**
  Waarom: het FTD geeft jou de fout-DTO en Stefan het omzetten. Zonder afspraak bouwen jullie het allebei, of geen van beiden. ([[draaiboek-sessie-2-android#Eerst beslissen (vóór iemand begint)|beslissing 1]])

- [ ] **Spreek met Lonneke het laatste stuk van beslissing 2 af: installeer jij haar plugin `SessionAuthorization` in jouw `HttpClient`, of levert zij een functie die jij aanroept.**
  Waarom: de plugin is van haar (A-30), de `HttpClient` van jou. ([[draaiboek-sessie-2-android#Eerst beslissen (vóór iemand begint)|beslissing 2]])

- [ ] **Stel de reviewregel in GitHub in: review van beide anderen voor `core/` en de gedeelde bestanden, de rest één review.**
  Waarom: de groep heeft de regel afgesproken; zonder instelling hangt hij af van ieders geheugen. Zoek eerst op wat de beste manier is (branch protection of rulesets, CODEOWNERS). ([[draaiboek-sessie-2-android#A-Allen. Het staat op main|A-Allen]])

- [ ] **Noteer de afspraken op een plek die jullie alle drie zien.**
  Waarom: in blok 6 gaan de besluiten naar het FTD. Tot die tijd moeten ze niet alleen in je hoofd zitten. ([[draaiboek-sessie-2-android#Blok 6. Vastleggen (15 min)|Blok 6]])

- [ ] **Maak de branch voor GB-1.**
  Waarom: ieder werkt op een eigen branch en maakt een pull request naar `main`. De naam volgt de afspraak van de startsessie. ([[draaiboek-sessie-2-android#A-Allen. Het staat op main|A-Allen]])

### DoD

- [ ] De paragrafen van het FTD zeggen wat het draaiboek beweert, of de afwijking staat bij de open vragen. ([[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)|A-Eva]])
- [ ] Je weet de juiste namen van de instellingen, voor de versies op `main`. (aanvulling, [[#A3 Versies en documentatie nakijken|A3]])
- [ ] Beslissing 1 en het laatste stuk van beslissing 2 hebben een antwoord en staan genoteerd. Beslissing 5 en 7 zijn al besloten (15 nov). ([[draaiboek-sessie-2-android#Eerst beslissen (vóór iemand begint)|Eerst beslissen]])
- [ ] De reviewregel staat in GitHub. Bewijs: een PR die `core/` raakt, vraagt twee reviews.

---

## Phase 0b — E0: het Koin-geraamte per feature (klaar, 15 november)

Basis: [[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)|E0]]
Raakt: `shared/src/.../AppModule.kt`, `shared/src/.../<feature>/<Feature>Module.kt`, `shared/src@android/.../PlatformModule.kt`, `android/.../RestpartijenApplication.kt`, `shared/test/.../AppModuleTest.kt`

### To-do

- [x] **Proef: `expect`/`actual` tussen `src/` en `src@android/`.** Werkt in de build, maar IntelliJ kleurt het rood; daarom niet gebruikt (`docs/tooling-issues/`).
- [x] **Een Koin-module per feature, `appModule` met `includes()`, `platformModule` in `src@android/`, strenge modus.** (A-29)
- [x] **`AppModuleTest`: `verify()` en `noTypeIsDefinedTwice`, allebei één keer rood geweest.**
- [x] **Twee reviewrondes, PR #91 gemerged.**
- [x] **De [[instructie-koin-modules|instructie]] voor Stefan en Lonneke, en het groepsbericht.**

### DoD

- [x] De app start en de tests zijn groen op `main` (PR #91).

---

## Phase 1 — E5 en E1: de fout van de API en de `HttpClient`

Basis: [[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)|E1]], [[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)|E5]]
Raakt: `shared/src/nl/restpartijen/app/core/network/`, `shared/src/nl/restpartijen/app/core/CoreModule.kt`, `shared/module.yaml`, `shared/test/nl/restpartijen/app/core/network/`
Start na: phase 0. Stefan en Lonneke wachten op deze phase; doe haar dus eerst.

### To-do

- [ ] **Schrijf eerst de test die een fout-antwoord van de server uitleest: één met `field` en één zonder.**
  Waarom: de test legt vast welke vorm je verwacht ([[FTD-restpartijen-android-app-v2_5#11.2 Foutafhandeling|§11.2]]).
  Verwacht: rood, want de DTO bestaat nog niet. ([[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)|E5]])

- [ ] **Schrijf de DTO van het foutmodel in `core/network/`.**
  Waarom: Stefan zet deze vorm in GB-2 om naar een melding.
  Verwacht: de test is groen. ([[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)|E5]])

- [ ] **Voeg aan `shared/module.yaml` toe wat E1 nog nodig heeft: `$ktor.client.okhttp`, `$ktor.client.contentNegotiation`, `$ktor.serialization.kotlinx.json` en `$ktor.client.logging`.**
  Waarom: Lonneke zette `settings.ktor` (3.6.0), `$ktor.client.core` en `$ktor.client.mock` er al in (PR #90). De versie komt uit de Ktor-BOM, zoals in de proef (stap 4); geen versie per regel.
  Als het misgaat: een wijziging in `libs.versions.toml` of `module.yaml` vraagt een review van beide anderen. ([[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)|E1]])

- [ ] **Schrijf de test met `MockEngine`: een goed antwoord wordt uitgelezen, ook met een veld dat het model niet kent.**
  Waarom: de API stuurt meer velden dan de app gebruikt.
  Verwacht: rood. ([[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)|E1]])

- [ ] **Bouw de ene `HttpClient` in `core/network/` en meld hem aan in `coreModule` (`core/CoreModule.kt`).**
  Waarom: elke repository gebruikt dan dezelfde instellingen (§8.6).
  Verwacht: de test is groen. ([[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)|E1]])

- [ ] **Zet de time-outs: 5 seconden voor verbinden, 15 seconden voor het hele request.**
  Waarom: het kwaliteitsscenario "Geen netwerk" vraagt binnen 5 seconden een melding (§8.7). ([[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)|E1]])

- [ ] **Breek de instelling voor onbekende velden bewust, en kijk of de test faalt. Zet haar terug.**
  Waarom: pas dan weet je dat de test de instelling controleert. (aanvulling, [[#A1 Rood vóór groen bij elke controle|A1]])

- [ ] **Installeer de plugin `SessionAuthorization` van Lonneke in de `HttpClient`, met `allowedHost` op het adres van de server.**
  Waarom: dan gaat het token mee, maar alleen naar onze eigen API (A-30). Volg wat je in phase 0 met Lonneke afsprak.
  Verwacht: haar tests blijven groen, en `AppModuleTest` ook. ([[draaiboek-sessie-2-android#Eerst beslissen (vóór iemand begint)|beslissing 2]])

- [ ] **Laat Stefan en Lonneke weten dat de `HttpClient` en de fout-DTO op je branch staan, of maak hiervoor een eerste pull request.**
  Waarom: S3 en L4 bouwen hierop. ([[draaiboek-sessie-2-android#Wat je nodig hebt om aan je eigen schermen te beginnen|Volgorde]])

### DoD

- [ ] De test van de fout-DTO is groen, met en zonder `field`. Bewijs: de testuitvoer. ([[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)|E5]])
- [ ] De test met `MockEngine` is groen en is één keer rood geweest. ([[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)|E1]])
- [ ] De app start nog. Bewijs: Run 'android'. ([[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)|E1]])
- [ ] `AppModuleTest` is groen. ([[instructie-koin-modules]])

---

## Phase 1b — Design patterns leren herkennen in E1

Basis: aanvulling [[#A4 Design patterns leren herkennen|A4]], [[FTD-restpartijen-android-app-v2_5#Besluiten|A-15]], [[besluiten-ftd-app#Gekozen design patterns (14 nov)|gekozen patterns]]
Raakt: geen code; een naslagnotitie in de vault
Start na: phase 1, want dan heb je de `HttpClient` zelf gebouwd. Doe elke stap samen met Claude, één per keer.

Per pattern dezelfde vijf stappen. **Eerst Strategy** (de eenvoudigste), **dan Chain of Responsibility**.

### To-do: Strategy

- [ ] **1. Begrijpen. Lees wat Strategy is: welk probleem het oplost en welke rollen het heeft (Context, Strategy, ConcreteStrategy).**
  Waarom: dit is de taal waarin je het straks uitlegt. Bronnen: Refactoring.Guru (z.j.-b) en *Head First Design Patterns* (Freeman & Robson, 2020), hoofdstuk 1.
  Verwacht: je kunt in twee zinnen zeggen welk probleem Strategy oplost.

- [ ] **2. Herkennen. Zoek de rollen in jouw code: `HttpClient` is de Context, de engine is de Strategy, `OkHttp` en `MockEngine` zijn ConcreteStrategies.**
  Waarom: een pattern is geen code die je schrijft, maar een vorm die je ziet. Ktor geeft de engine mee bij het maken van de client (JetBrains, z.j.-a).
  Verwacht: je wijst bij elke rol het bestand en de regel aan.

- [ ] **3. Voelen. Draai je repository-test (met `MockEngine`) en start de app (met `OkHttp`). Kijk wat er tussen die twee anders is in jouw code.**
  Waarom: dit is de meerwaarde: je wisselt de manier van versturen zonder één regel van de repository te veranderen.
  Verwacht: alleen de engine verschilt. Bedenk wat je zonder Strategy had moeten doen om zonder netwerk te testen.

- [ ] **4. Vastleggen. Maak een klassendiagram en een sequencediagram van Strategy in de Ktor Client, en schrijf drie zinnen meerwaarde, in een naslagnotitie.**
  Waarom: het FTD eist beide diagrammen per pattern (A-15, §1.5). Het is je bewijs voor het assessment.

- [ ] **5. Navertellen. Leg het in sessie 2 uit aan Stefan of Lonneke.**
  Waarom: als een ander het kan navertellen, snap jij het. ([[draaiboek-sessie-2-android#De toets of het is overgekomen|De toets]])

### To-do: Chain of Responsibility

- [ ] **1. Begrijpen. Lees wat Chain of Responsibility is: welk probleem het oplost en welke rollen het heeft (Handler, ConcreteHandler, Client).**
  Waarom: dezelfde reden als bij Strategy. Bron: Refactoring.Guru (z.j.-a).
  Verwacht: je kunt in twee zinnen zeggen wat een handler doet als hij een request niet zelf afhandelt.

- [ ] **2. Herkennen. Zoek de rollen in jouw `HttpClient`: de plugins (`ContentNegotiation`, `HttpTimeout`, `Logging`, `SessionAuthorization`) zijn de handlers, `HttpClient` stuurt het request de keten in.**
  Waarom: elk request gaat in vaste volgorde langs de hooks van alle plugins (`onRequest`, `on(Send)`, `onResponse`) (JetBrains, z.j.-b). De plugin van Lonneke is een schakel in die keten.
  Verwacht: je wijst bij elke rol het bestand en de regel aan.

- [ ] **3. Voelen. Maak op een proefbranch twee mini-plugins die "A" en "B" loggen in `onRequest`. Installeer ze, wissel de volgorde, en kijk in de test wat er verandert. Gooi de proef daarna weg.**
  Waarom: zo zie je dat elke schakel los staat, en dat je een stap toevoegt zonder de andere te veranderen. Dat is de meerwaarde.
  Verwacht: de volgorde in de log volgt de volgorde van installeren (te controleren, Claude).

- [ ] **4. Vastleggen. Maak een klassendiagram en een sequencediagram van de keten, en schrijf drie zinnen meerwaarde, in dezelfde naslagnotitie.**
  Waarom: A-15, §1.5.

- [ ] **5. Navertellen. Leg het in sessie 2 uit aan Stefan of Lonneke.**

### DoD

- [ ] Je kunt bij beide patterns de rollen in de code aanwijzen en de meerwaarde in eigen woorden zeggen. (aanvulling, [[#A4 Design patterns leren herkennen|A4]])
- [ ] De naslagnotitie bestaat, met per pattern een klassen- en een sequencediagram. (A-15)
- [ ] In [[besluiten-ftd-app#Gekozen design patterns (14 nov)|de besluitennotitie]] staat bij jou waar beide patterns te zien zijn.

**Daarna, in fase 2 (niet in deze lijst):** Strategy zelf toepassen in Partij toevoegen (scannen of handmatig invoeren), en Chain of Responsibility zelf toepassen, bijvoorbeeld met een eigen plugin. Zo kom je op 2×2.

---

## Phase 2 — E2 en E3: het adres en de netwerkregels

Basis: [[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)|E2]], [[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)|E3]]
Raakt: `shared/src/nl/restpartijen/app/core/network/`, `android/src/AndroidManifest.xml`, `android/res/xml/network_security_config.xml`
Start na: phase 1

### Voordat je begint
- [ ] De server draait. Controle: `set -a; . ./.env; set +a; ./kotlin run -m server` (laadt de omgeving en start de server). Daarna antwoordt `http://localhost:8080/api/v1/products` in de browser.

### To-do

- [ ] **Zet het adres van de server op één plek.**
  Waarom: de emulator en later een telefoon werken dan zonder dat de code verandert (§11.1). ([[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)|E2]])

- [ ] **Zoek uit of de toolchain een adres per omgeving (per build type) kan, en schrijf op wat je vindt.**
  Waarom: dit is O-14, en jij bent de eigenaar ([[FTD-restpartijen-android-app-v2_5#19.2 Open punten|§19.2]]).
  Als het misgaat: vind je het niet, dan blijft O-14 open. Zet dat bij de open vragen; raad niet. ([[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)|E2]])

- [ ] **Vul `network_security_config.xml`: http alleen naar `10.0.2.2`. Zet `INTERNET` en `ACCESS_LOCAL_NETWORK` in het manifest.**
  Waarom: Android blokkeert gewoon http (§11.3), en vanaf Android 17 ook het lokale netwerk (§11.4). ([[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)|E3]])

- [ ] **Bouw de vraag om toestemming voor het lokale netwerk: bij het eerste scherm dat data laadt, met uitleg, volgens de vaste regel van [[FTD-restpartijen-android-app-v2_5#15. Privacy by design|§15]].**
  Waarom: zonder toestemming krijgt elke aanroep op de emulator een time-out. Op Android 16 en lager bestaat de permissie niet; daar vraagt de app niets. Besloten: jij bouwt dit (beslissing 5, 15 nov). ([[draaiboek-sessie-2-android#Eerst beslissen (vóór iemand begint)|beslissing 5]])

- [ ] **Draai `./kotlin clean`, bouw opnieuw en controleer met `aapt2` of de permissies en de regels in de APK zitten.**
  Waarom: zonder `clean` komt een wijziging in het manifest of in `android/res/` niet in de app. Dat is de valkuil uit de proef.
  Verwacht: beide permissies en de netwerkregels staan in de uitvoer. ([[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)|E3]])

- [ ] **Probeer het op de emulator met een tijdelijke aanroep op je eigen branch. Commit die niet.**
  Waarom: het voorbeeldscherm sluiten wij in blok 5 samen aan.
  Verwacht: met toestemming komt er een antwoord. Met de server uit komt er binnen 5 seconden een fout, zonder crash. ([[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)|E7]])

### DoD

- [ ] Zoeken op `10.0.2.2` in `shared/` geeft één treffer. Bewijs: de zoekuitvoer. ([[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)|E2]])
- [ ] De permissies en de netwerkregels zitten in de APK. Bewijs: de uitvoer van `aapt2`. ([[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)|E3]])
- [ ] Wat je over O-14 vond, staat in het FTD of bij de open vragen. ([[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)|E2]])

---

## Phase 3 — E4: loggen alleen in debug

Basis: [[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)|E4]]
Raakt: `shared/src/nl/restpartijen/app/core/network/`
Start na: phase 2

### To-do

- [ ] **Laat de `HttpClient` per request de methode, het pad en de statuscode loggen. Geen bodies. De `Authorization`-header staat als `***`.**
  Waarom: een token of wachtwoord in een log is een lek (§16.6). ([[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)|E4]])

- [ ] **Zoek uit hoe de app weet dat zij debuggable is, en log alleen dan.**
  Waarom: in een release-build logt de app niets. Hoe dat met de toolchain moet, is open (O-14).
  Als het misgaat: lukt het niet vóór de sessie, zet het dan bij de open vragen. Laat het niet stil in de code staan. ([[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)|E4]])

- [ ] **Kijk in Logcat naar één request van je tijdelijke aanroep.**
  Verwacht: één regel met methode, pad en statuscode. Geen body, geen token. ([[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)|E4]])

### DoD

- [ ] Logcat toont per request één regel, zonder token en zonder body. Bewijs: de regel uit Logcat. ([[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)|E4]])
- [ ] "Alleen in debug" werkt, of staat bij de open vragen met wat je hebt geprobeerd. ([[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)|E4]])

---

## Phase 4 — E6: de afspraak voor Koin

Basis: [[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)|E6]]
Raakt: `shared/src/nl/restpartijen/app/core/CoreModule.kt`, `shared/test/nl/restpartijen/app/`
Start na: phase 1. De vorm van Koin is besloten en gebouwd in E0 (A-29).

### To-do

- [x] **Leg vast hoe je een repository en een ViewModel aanmeldt.** Staat in de [[instructie-koin-modules|instructie]] (E0). ([[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)|E6]])

- [x] **Een test die controleert of alles in de modules te maken is.** `AppModuleTest` met `verify()` en `noTypeIsDefinedTwice` (E0).

- [ ] **Controleer na het aanmelden van je `HttpClient` dat `AppModuleTest` groen is, en haal je aanmelding één keer tijdelijk weg om hem rood te zien. Zet haar terug.**
  Waarom: rood vóór groen voor je eigen aanmelding. Let op: `verify()` ziet een definitie op een interface niet (zie de instructie). (aanvulling, [[#A1 Rood vóór groen bij elke controle|A1]])

### DoD

- [ ] De app start. Bewijs: Run 'android'. ([[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)|E6]])
- [ ] De test van de module is groen en is één keer rood geweest. ([[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)|E6]])

---

## Phase 5 — E7: jouw eigen voorbeeld

Basis: [[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)|E7]]
Raakt: `shared/test/nl/restpartijen/app/core/network/`
Start na: phase 1 en beslissing 1

### To-do

- [ ] **Schrijf één test die de echte instellingen van de `HttpClient` gebruikt met een `MockEngine`: een goed antwoord, en een fout in de vorm van E5.**
  Waarom: dit is het voorbeeld voor elke repository-test van Stefan en Lonneke. Het FTD vraagt per repository minimaal één zo'n test (§13). ([[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)|E7]])

- [ ] **Breek één instelling bewust, en kijk of de test faalt. Zet haar terug.**
  Waarom: de controle van E7. ([[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)|E7]])

- [ ] **Haal je tijdelijke aanroep weg.**
  Waarom: het voorbeeldscherm sluiten wij in blok 5 samen aan. Wat tijdelijk is, komt niet op `main`.
  Verwacht: `git status` laat geen wijziging in `example/` zien. ([[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)|E7]])

### DoD

- [ ] De test is groen en is één keer rood geweest. Bewijs: de testuitvoer. ([[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)|E7]])
- [ ] Er staat geen tijdelijke aanroep in je branch. Bewijs: de diff van je pull request. ([[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)|E7]])

---

## Phase 6 — E8: de coverage-meting (GB-7)

Basis: [[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)|E8]]
Raakt: `coverage-shared.sh`, `coverage-shared.bat`, de README of een korte uitleg
Start na: phase 0. Deze phase wacht nergens op.

### To-do

- [ ] **Draai `./coverage-shared.sh` op `main` en open `build/coverage/shared/index.html`.**
  Waarom: je controleert dat het script na de startsessie nog werkt, met de packages van het FTD.
  Verwacht: een rapport met een percentage per klasse. ([[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)|E8]])

- [ ] **Test `coverage-shared.bat` op Windows, samen met Stefan of Lonneke.**
  Waarom: de `.bat` is nog nooit op Windows gedraaid.
  Als het misgaat: noteer de foutmelding letterlijk. Los het samen op, of zet het bij de open vragen. ([[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)|E8]])

- [ ] **Schrijf een korte uitleg: hoe je de meting draait, waar het rapport staat, en hoe je het leest.**
  Waarom: de drempel van 80% geldt voor ViewModels en repository's van ieder (NFR-A3, [[FTD-restpartijen-android-app-v2_5#14. Niet-functionele eisen (NFR)|§14]]). In blok 2 draaien Stefan en Lonneke het zelf. ([[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)|E8]])

### DoD

- [ ] Het rapport bestaat op Linux en op Windows. Bewijs: het bestand `build/coverage/shared/index.html` op beide. ([[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)|E8]])
- [ ] De uitleg staat op een plek die Stefan en Lonneke kunnen lezen. ([[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)|E8]])

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

- [ ] **Maak een pull request naar `main` en vraag een review van Stefan en Lonneke allebei.**
  Waarom: wat niet op `main` staat, bestaat voor de anderen niet. Je raakt `core/` en `module.yaml`, dus de afgesproken reviewregel vraagt twee reviews. ([[draaiboek-sessie-2-android#A-Allen. Het staat op main|A-Allen]])

- [ ] **Verwerk de opmerkingen en laat een ander mergen.**
  Waarom: je merget je eigen pull request niet. ([[draaiboek-sessie-2-android#A-Allen. Het staat op main|A-Allen]])

### DoD

- [ ] GB-1 en GB-7 staan op `main`. Bewijs: de gemergede pull request. ([[draaiboek-sessie-2-android#A-Allen. Het staat op main|A-Allen]])
- [ ] De app start vanaf `main`. ([[draaiboek-sessie-2-android#A-Allen. Het staat op main|A-Allen]])

---

## Phase 8 — Deel B: jouw uitleg voorbereiden

Basis: [[draaiboek-sessie-2-android#Vragen voor Eva|Vragen voor Eva]], [[draaiboek-sessie-2-android#De toets of het is overgekomen|De toets]]
Raakt: je eigen aantekeningen
Start na: phase 7

### To-do

- [ ] **Beantwoord de negen vragen voor Eva in eigen woorden, en noteer bij elk antwoord het bestand en de regel.**
  Waarom: in blok 2 wijs je de plek in de code aan. Voorlezen mag niet. ([[draaiboek-sessie-2-android#Vragen voor Eva|Vragen voor Eva]])

- [ ] **Lees de vragen voor Stefan en Lonneke.**
  Waarom: jij vertelt in blok 3 of 4 één punt van hen na. ([[draaiboek-sessie-2-android#De toets of het is overgekomen|De toets]])

### DoD

- [ ] Bij elke vraag staat een antwoord en een plek in de code. ([[draaiboek-sessie-2-android#Vragen voor Eva|Vragen voor Eva]])

---

## Phase 9 — De sessie

Basis: [[draaiboek-sessie-2-android#Deel C — De sessie|Deel C]]
Raakt: `shared/src/nl/restpartijen/app/example/`, het FTD
Start na: phase 8, en de datum van sessie 2

### To-do

- [ ] **Blok 1: haal `main` op, bouw, draai de tests, start de server en de app.** ([[draaiboek-sessie-2-android#Blok 1. Dezelfde basis (10 min)|Blok 1]])
- [ ] **Blok 2: laat E1 tot en met E8 zien, beantwoord de vragen, en help Stefan en Lonneke de coverage-meting te draaien.**
  Verwacht: een ander vertelt één punt na. ([[draaiboek-sessie-2-android#Blok 2. Eva laat zien en legt uit (30 min)|Blok 2]])
- [ ] **Blok 3 en 4: vertel één punt van Stefan of Lonneke na.** ([[draaiboek-sessie-2-android#De toets of het is overgekomen|De toets]])
- [ ] **Blok 5: bouw mee aan het voorbeeldscherm met de echte API.**
  Waarom: hier blijkt of de drie delen op elkaar passen. ([[draaiboek-sessie-2-android#Blok 5. Samen één stukje door alle delen (40 min)|Blok 5]])
- [ ] **Blok 6: zet de besluiten van vandaag in het FTD, met een datum.**
  Waarom: jij beheert het FTD. Een feature is pas af als haar deel in het FTD af is (A-02). ([[draaiboek-sessie-2-android#Blok 6. Vastleggen (15 min)|Blok 6]])

### DoD

- [ ] Stefan en Lonneke hebben zelf de coverage-meting gedraaid. ([[draaiboek-sessie-2-android#De sessie is geslaagd als|Geslaagd]])
- [ ] Het voorbeeldscherm toont gegevens van de echte API. ([[draaiboek-sessie-2-android#De sessie is geslaagd als|Geslaagd]])
- [ ] De besluiten staan in het FTD, of hebben een eigenaar en een datum. ([[draaiboek-sessie-2-android#Blok 6. Vastleggen (15 min)|Blok 6]])

---

## DoD van de lijst

- [ ] Alle phases zijn afgesloten.
- [ ] Je kunt zonder Stefan en Lonneke verder aan je eigen schermen. ([[draaiboek-sessie-2-android#De sessie is geslaagd als|Geslaagd]])
- [ ] De open vragen hieronder zijn beantwoord of bewust geaccepteerd.

## Dekking

| Element in het draaiboek | Phase |
| --- | --- |
| [[draaiboek-sessie-2-android#Eerst beslissen (vóór iemand begint)\|Eerst beslissen]]: beslissing 1, 2 (rest) | 0 |
| Eerst beslissen: beslissing 5, 7, 10 | besloten op 15 nov; 5 in phase 2, 7 in E0 |
| Eerst beslissen: beslissing 3, 4, 6, 8 | niet in deze lijst: van Stefan en Lonneke |
| Eerst beslissen: beslissing 9 (de datum) | 9, als voorwaarde |
| [[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)\|E0]] | 0b (klaar) |
| [[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)\|E1]] | 1 |
| Design patterns (A-15, aanvulling A4) | 1b |
| [[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)\|E2]] | 2 |
| [[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)\|E3]] | 2 |
| [[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)\|E4]] | 3 |
| [[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)\|E5]] | 1 |
| [[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)\|E6]] | 4 |
| [[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)\|E7]] | 5 |
| [[draaiboek-sessie-2-android#A-Eva. Data- en netwerklaag en coverage (GB-1, GB-7)\|E8]] (GB-7) | 6 |
| [[draaiboek-sessie-2-android#A-Allen. Het staat op main\|A-Allen]] | 7 |
| [[draaiboek-sessie-2-android#Vragen voor Eva\|Deel B, vragen voor Eva]] | 8 |
| [[draaiboek-sessie-2-android#De toets of het is overgekomen\|De toets]] | 8, 9 |
| [[draaiboek-sessie-2-android#Deel C — De sessie\|Deel C]], blok 1 tot en met 6 | 9 |
| [[draaiboek-sessie-2-android#De sessie is geslaagd als\|Geslaagd]] | 9, DoD van de lijst |
| [[draaiboek-sessie-2-android#Nalopen na zaterdag\|Nalopen]] | 0 |
| A-Stefan en A-Lonneke | niet in deze lijst: van hen |

## Open vragen

1. **O-14.** Het adres per omgeving en "is de app debuggable" zijn open in het FTD. Vind je het antwoord niet vóór de sessie, dan blijven E2 en E4 deels open. Leg je dat de groep voor, of beslis je het zelf?
2. ~~**De vraag om toestemming (beslissing 5).**~~ Besloten op 15 november: jij bouwt het (phase 2).
3. ~~**De controle van de Koin-module.**~~ Bestaat: `verify()` in `koin-test`, gebouwd in E0 (`AppModuleTest`).
4. **De uitleg van GB-7.** Waar komt die te staan: in de README van de repository, of in een notitie die jij deelt?

## Bronnen

- Freeman, E., & Robson, E. (2020). *Head First Design Patterns* (2e ed.). O'Reilly Media.
- JetBrains. (z.j.-a). *Client engines*. Ktor. Geraadpleegd op 15 november 2026, van https://ktor.io/docs/client-engines.html
- JetBrains. (z.j.-b). *Custom client plugins*. Ktor. Geraadpleegd op 15 november 2026, van https://ktor.io/docs/client-custom-plugins.html
- Refactoring.Guru. (z.j.-a). *Chain of Responsibility*. Geraadpleegd op 15 november 2026, van https://refactoring.guru/design-patterns/chain-of-responsibility
- Refactoring.Guru. (z.j.-b). *Strategy*. Geraadpleegd op 15 november 2026, van https://refactoring.guru/design-patterns/strategy

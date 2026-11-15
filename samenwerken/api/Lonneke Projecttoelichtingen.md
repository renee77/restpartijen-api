# **Phase 1 - Beslissen en overdragen**
*Kies de BCrypt Library*
De keuze is gemaakt om te gaan voor at.favre.lib:bcrypt 0.10.2 (meest recente versie)

> [!NOTE] Antwoord in 1 zin
> Hij werkt op alle JVM-machines, blijft ook werken met Android en Ktor, is niet te zwaar voor onze applicatie en is een goed onderhouden en veelgebruikte library binnen Ktor en Kotlin.

___________
**Onderbouwing:** 
- Ik heb eerst onderzoek gedaan via AI, om te bepalen welke opties er zijn, zodat ik vanuit daar zelf onderzoek kan gaan doen. Deze reactie kreeg ik terug: 
**AI Antwoord**
	Here are the realistic options. Check the current versions on Maven Central before you pin anything, because I can't look them up from here.
	**1. `at.favre.lib:bcrypt` (my pick)**
	- It's pure Java with no dependencies, so it runs on the JVM and Android.
	- It has a clean API (`BCrypt.withDefaults().hashToString(12, chars)` and `BCrypt.verifyer().verify(...)`).
	- It lets you choose what happens with passwords longer than 72 bytes: reject, truncate, or pre-hash.
	- The last release I know of was a few years ago. The code is small and stable, but it isn't actively developed.
	**2. Spring Security Crypto (`org.springframework.security:spring-security-crypto`)**
	- It provides `BCryptPasswordEncoder`, and the project is actively maintained and widely trusted.
	- It's built for the Spring ecosystem and pulls in extra dependencies for one function. It's usable in Ktor, but the extra weight is hard to justify.
	**3. Bouncy Castle (`org.bouncycastle:bcprov-jdk18on`, class `OpenBSDBCrypt`)**
	- It's actively maintained and very well known.
	- It's a large library for one feature. On Android it can conflict with the stripped-down Bouncy Castle that ships with the OS.
	**4. jBCrypt (`org.mindrot:jbcrypt`)**
	- It's the classic option and appears in many tutorials.
	- It's unmaintained (0.4 dates from around 2010), and it silently ignores anything past 72 bytes. I'd avoid it for a new project.

Op basis van deze reactie heb ik zelf via Google nog onderzoek gedaan naar wat er wordt gebruikt door andere Developers, en op basis van onderstaande twee links, heb ik mijn definitieve besluit gemaakt voor een BCrypt Library.
https://mojoauth.com/security-guides/bcrypt-in-kotlin#how-to-hash-a-password-with-bcrypt-in-kotlin
https://stackoverflow.com/questions/60352285/how-to-hash-and-verify-passwords-in-ktor

# **Zoek op wat een challenge in een JWT provider doet**

> [!NOTE] Antwoord in 1 zin
> Een challenge wordt gebruikt om iets terug te geven op het moment dat de authenticatie van het JWT signaleert dat het een onjuist token is. 

> [!NOTE] Antwoord in 1 zin V2
> Een challenge wordt gebruikt om iets terug te geven op het moment dat de authenticatie van het JWT mislukt, door een onjuist token of een ontbrekend of verlopen token.

___
**Onderbouwing**
Ik heb de research eerst zelf gedaan door op de Ktor documentatie door de informatie van JWT te lezen. Nadat ik dit heb gedaan, heb ik mijn antwoord in 1 zin genoteerd, welke ik vervolgens heb geverifieerd via Claude, om zo te bepalen of dit alles omvat, of dat er nog iets mist. Op basis hiervan heb ik hem bijgeschaafd (V2), in plaats van het antwoord van de AI klakkeloos over te nemen. 

**AI Antwoord**
	Grotendeels wel, maar het is nog wat vaag en een beetje onvolledig.
	**Wat klopt:** de challenge bepaalt wat de server teruggeeft als de authenticatie mislukt.
	**Wat ontbreekt of preciezer kan:**
	- **Wanneer hij afgaat.** Niet alleen bij een onjuist token, maar bij elke mislukte authenticatie. Dat is ook als er helemaal geen token is meegestuurd, als het token verlopen is of een ongeldige handtekening heeft, of als je `validate` `null` teruggeeft.
	- **Wat er teruggegeven wordt.** Een antwoord aan de client, normaal status 401 (Unauthorized). Standaard stuurt Ktor 401 met een `WWW-Authenticate`-header. In het `challenge { ... }`-blok kun je dat aanpassen, bijvoorbeeld door zelf een 401 met een eigen foutmelding te sturen, zoals in mijn `configureSecurity()`-voorbeeld.
	- **Wat het niet is.** De challenge gaat niet af bij een geldig token met de verkeerde rol. Dat is een aparte autorisatiecontrole en geeft 403. Dat verschil tussen 401 en 403 is een van je uitlegpunten, dus het loont om het erbij te noemen.
	
	Een sterker antwoord zou zijn:
	> Een challenge is het antwoord dat de JWT-provider naar de client stuurt wanneer authenticatie mislukt (geen token, ongeldig of verlopen token, of `validate` geeft `null`). Standaard is dat een 401 Unauthorized met een `WWW-Authenticate`-header, en je kunt het aanpassen in het `challenge`-blok. Bij een geldig token met de verkeerde rol gaat de challenge niet af; dan geeft de rolcheck een 403.


# **Voorstel 401 en 403 in het foutmodel**
> [!NOTE] Voorstel
> Mijn voorstel zou zijn om ze in eenzelfde vorm in de Challenge te noteren, zodat ze vervolgens via Stefan's foutmodel verder kunnen worden afgehandeld, ter voorkoming dat deze informatie dus weer op meerdere plekken terecht gaat komen. 


**Optie 1**
De challenge of rolecheck vangt ze op, gooit ze door naar Stefans foutmodel, om ze zo op eenzelfde manier te verwerken. 
*Voordeel* = Alle informatie van de foutmeldingen blijven op eenzelfde plek opgeslagen. Er is geen seperation of concerns, met een deel van de foutmeldingen bij mij. 
*Nadeel* = De volgorde van de installs moet kloppen, zodat `StatusPages` de excepties uit `security` ook echt opvangt. Daarnaast is een `ForbiddenException` nodig.

**Optie 2**
Er wordt direct vanuit security een melding gestuurd als de foutmelding wordt opgepakt. 
*Voordeel* = Alle informatie die met de security en authentication van doen hebben, zitten op één plek bij elkaar, ook de foutmeldingen.
*Nadeel* = Nu worden de foutmeldingen dus op twee losse locaties verwerkt. Dit betekent dat, als je de 401 foutmelding wilt veranderen, je dit op twee losse locaties moet doen. 

**AI toelichting**
	Ik heb hierbij ook, net als bij de tweede optie, gekeken of mijn beredenering klopte, duidelijk genoeg is, en deze waar nodig aangescherpt.
	Het is bijna goed. Je hebt de twee opties en een voor- en nadeel per optie, en je kiest zelf een kant. Er zijn drie dingen die je moet aanpassen, omdat ze inhoudelijk niet kloppen of onduidelijk zijn.
	**1. Bij optie 1 doet de challenge te veel.** Je schrijft dat de challenge de fout opvangt, omzet naar de juiste opbouw en doorgooit. In deze optie gooit de challenge alleen een exceptie (`UnauthorizedException`). `StatusPages` van Stefan zet die om naar `{ code, message }`. De omzetting gebeurt dus bij Stefan, niet bij jou. De 403 komt bovendien niet uit de challenge maar uit je rolcheck, die een `ForbiddenException` gooit.
	**2. Het voordeel van optie 1 leest als een nadeel.** "Er is geen separation of concerns" klinkt alsof optie 1 dat probleem heeft. Optie 1 is juist de nette scheiding: het foutmodel staat op één plek. Schrijf het positief: "Het foutmodel staat alleen in `StatusPages`, dus een wijziging hoeft maar één keer." Let ook op de spelling: _separation_, niet _seperation_.
	**3. Je voorkeur staat er te vaag.** "In eenzelfde vorm in de Challenge te noteren" zegt niet duidelijk wat je kiest. Noem expliciet dat je voor optie 1 kiest.
	Inhoudelijk is dit dezelfde keuze als de jouwe, alleen preciezer beschreven.


# **Lijst van JWT-instellingen**
Ik heb de volgende JWT-instellingen nodig uit application.yaml: 
- Naam van de variabele voor het secret
- Issuer
- Audience
- Geldigheid (van 24 uur conf. het besluit)
- Naam van de provider

**En de namen die deze variabelen gaan krijgen!**


# **Phase 4 - Rolcheck keuze**
Kies hoe de rolcheck eruitziet. Het voorstel laat dat open. Twee gangbare manieren in Ktor: een route-scoped plugin (`createRouteScopedPlugin`), of een extension function op `Route` die een blok alleen uitvoert bij de juiste rol. Schrijf in twee zinnen op waarom je kiest wat je kiest 

> [!NOTE] Antwoord in 2 zinnen
> De keuze is gevallen op een route-scoped plugin, gezien dit de endpoints buiten de security-omgeving houdt, en de plugin altijd na de authenticatie draait, zodat de juiste volgorde niet kan worden vergeten. Dit is de meest clean manier om routes te bepalen, en is ook een stuk overzichtelijker op het moment dat dit correct wordt opgezet. 


# **Phase 7 - Alles over de schutting gooien**
Voorbereiding pull-request: 
**Hoe bescherm je een route?**
Routes worden beschermt door de route te omwikkelen met `authenticate`. Hiermee wordt altijd de authenticatie uitgevoerd. 

Als je een rolcheck doet, wordt de authenticatie daarbij direct meegenomen. Er hoeft bij de routes dus niet én een rolcheck, én een authenticatie te worden meegenomen. 
Je kun een rolcheck doen door de route te omwikkelen met de `requireRole` functie. 

-- Kijk voor een voorbeeld naar `TestApplicationSetup.kt` in `test/security`

**Hoe vraag je de huidige gebruiker op?**
Dit wordt kun je opvragen via de functie **call.CurrentUser()** in `CurrentUserExtensions`, een extension functie op de ApplicationCall.
Hier worden de id en de rol uit de claims in de tokens opgehaald, om zo te kunnen verifiëren of de gebruiker bestaat. Voor de eigenaarscontrole vergelijk je dat id met het eigenaar-id van de resource. 

**Hoe krijg je een token in een test?**
Gebruik hiervoor de testtoken van `Testtoken.kt`

**Waar staat je voorbeeld?**
n.t.b.


# **Phase 8 - Uitleg voorbereiden**
### **01 - De weg van een verzoek, van de `Authorization`-header tot de route**
*Toelichting*
Een verzoek naar een beschermde route gaat door deze stappen:
1. **De header.** De client stuurt het token mee als `Authorization: Bearer <token>`.
2. **De provider pakt het token op.** De Authentication-plugin (geïnstalleerd in `configureSecurity`) leest het token uit de header, voor routes binnen `authenticate(PROVIDER_NAME)`. `requireRole` doet die `authenticate` zelf.
3. **De verifier controleert het token:** handtekening, issuer, audience en vervaldatum (`jwtConfig.verifier`).
4. **`validate` controleert onze eigen claims.** `userId` moet aanwezig zijn en `role` moet een waarde uit `Role` zijn. Zo ja, dan ontstaat een `JWTPrincipal`.
 5. **Mislukt stap 3 of 4, dan gaat de `challenge` af.** Die gooit een `UnauthorizedException`, en `StatusPages` maakt daar een **401** van. De route draait dan niet.
 6. **De rolcheck.** Na de authenticatie leest de rolcheck de rol uit de `JWTPrincipal`. Staat die niet bij de toegestane rollen van de route, dan gooit hij een `ForbiddenException` en wordt het een **403**.
 7. **De route.** Pas als beide controles slagen draait de route. Die kan met `call.currentUser()` het id en de rol opvragen.

Kort samengevat: eerst authenticatie (wie ben je, 401), dan autorisatie (mag je dit, 403), en dan pas de route.

*Waar in de code kun je het vinden?*
**Naam van de provider**
`PROVIDER_NAME` in `Security.kt`

**Token uit de header halen, provider installeren**
`configureSecurity` in `Security.kt` (`install(Authentication)` met `jwt(PROVIDER_NAME)`)

**Handtekening, issuer, audience, vervaldatum**
`verifier` in `JwtConfig.kt`

**Eigen claims controleren (`userId`, `role` in `Role`)**
het `validate`-blok in `Security.kt`

**401 bij een mislukte authenticatie**
het `challenge`-blok in `Security.kt` (gooit `UnauthorizedException`)

**403 bij een verkeerde rol**
de plugin in `Rolecheck.kt` (gooit `ForbiddenException`)

**Een route beschermen (authenticatie plus rolcheck)**
`requireRole(...)` in `Rolecheck.kt`

**Exceptie omzetten in statuscode**
`StatusPages` (Stefan, GI-4). In je tests staat een tijdelijke versie in `ValidationTests.kt`

**Huidige gebruiker in de route**
`currentUser()` in `CurrentUserExtensions.kt`, met `CurrentUser` in `security/model/`

**Getest**
`ValidationTests.kt` (401 en 200) en `RoleAuthTest.kt` (rollen)

### **02 - Het verschil tussen authenticatie en autorisatie, en wat de plugin doet en wat jullie zelf doen**
*Toelichting*
**Authenticatie: Wie ben jij?** 
Ofwel, Authenticatie verifieert de informatie die wordt opgestuurd via de Header. Er wordt gekeken of je token echt en geldig is. Als dit het niet geval is, krijgt de gebruiker een statuscode 401

**Autorisatie: Mag jij dit?**
Ofwel, Autorisatie bepaalt, nadat is bevestigd wie de gebruiker is, of de gebruiker ook de rechten heeft om een bepaalde route te benaderen. 

*Waar in de code kun je het vinden?* 
De bepaling van een geldige code gebeurt in `validate` in `Security.kt`
De vervolgstappen bij een ongeldige code gebeurt in `challenge` in `Security.kt

De autorisatie gebeurt in `RoleCheck.kt`

### **03 - Wanneer statuscode 401 en wanneer statuscode 403?**
*Toelichting*
**401 Unauthorized:** het systeem weet niet wie de gebruiker is. Dit gebeurt bij een verzoek zonder token, met een ongeldig of verlopen token, met een token met een andere handtekening, of met een onbekende rol. De authenticatie mislukt dan.

**403 Forbidden:** het systeem weet wel wie de gebruiker is (het token is geldig), maar de gebruiker mag dit niet: de rol heeft geen toegang tot deze route, of de gebruiker is niet de eigenaar van de resource. 

*Waar in de code kun je het vinden?*
401: de `challenge` in `Security.kt` (gooit een `UnauthorizedException`).
403: de rolcheck in `Rolecheck.kt` (gooit een `ForbiddenException`).
Tests zijn te vinden in mijn `ValidationTests.kt` (401) en in `RoleAuthTest.kt` (401 en 403).

### **04 - Waar de rol vandaan komt en waarom iemand de claim niet zelf kan aanpassen**
*Toelichting*
De rol komt direct uit de UsersTable. Bij het inloggen wordt er een token aangemaakt, waar deze informatie direct wordt omgezet in een claim (role). Een gebruiker kan de claim niet aanpassen, want dit token is dus ondertekend met het secret en het HMAC256 algoritme.
Als deze wel wordt gewijzigd, wordt de handtekening gebroken en wordt hij door de verifier afgewezen, met een 401 melding. 

*Waar in de code kun je het vinden?* 
Dit wordt opgebouwd in `JwtConfig.kt`. De test hiervan vind je in `ValidationTests.kt`, onder `token with wrong secret gets 401`

### **05 - Wat zit er in het token en wat niet?**
*Toelichting*
In het token staat er het ID en de rol van de gebruiker. Deze informatie is nodig om te achterhalen welk account van de gebruiker is, om zo de juiste informatie te kunnen tonen. De rol is nodig om te bepalen welke toegang deze specifieke rol heeft en dit te kunnen doorspelen naar de rolechecker. 
Buiten dit om staan de standaard gegevens er in, de iss(issuer), aud(audience), iat (issued at) en exp (expired by).
Wat er niet wordt meegenomen in een token zijn alle andere gegevens die aan een account gelinkt zijn, zoals bijvoorbeeld een e-mailadres en wachtwoord. Een token is door iedereen te lezen: het is ondertekend, maar niet versleuteld. Daarom wordt er alleen meegegeven wat de server bij elk verzoek nodig heeft.

*Waar in de code kun je het vinden?* 
Je kunt dit vinden in de `Security.kt` file, waar zichtbaar is dat de userID en rol worden binnengehaald om te valideren. Je kunt ook zien dat dit wordt getest in `ValidationTests.kt`

### **06 - Waarom 24 uurs-token en geen refresh tokens?**
*Toelichting*
Er is voor gekozen om een 24-uurstoken aan te maken in plaats van een token die maar 1 uur geldig is met daaropvolgend refresh token, omdat dit een schoolproject betreft. Hierbij is de prioriteit dat de functionaliteit kan worden getoond en een voorbeeld kan worden getoond tijdens een assessment. Het toevoegen van refresh tokens biedt in deze situatie geen extra meerwaarde, tegenover de extra complexiteit die hiermee aan het project wordt toegevoegd.
Het nadeel van deze 24-uurstokens is dat het onveilig is en hackers de kans geeft om een token van een klant te onderscheppen en voor tot maximaal 24 uur te gebruiken. 

*Waar in de code kun je het vinden?*
Het is te vinden in de code in de JwtSettings.kt. Hier staat aangegeven dat de validiteit vast staat ingesteld op 24 uur. 
Dit is ook getest in TestTokensTest.kt, de test `token is valid for 24 hours`

### **07 - Hoe je een route beschermt, en wat er gebeurt als je authenticate of de rolcheck vergeet**
*Toelichting*
Routes worden beschermt door de route te omwikkelen met `authenticate`. Hiermee wordt altijd de authenticatie uitgevoerd. 

Als je een rolcheck doet, wordt de authenticatie daarbij direct meegenomen. Er hoeft bij de routes dus niet én een rolcheck, én een authenticatie te worden meegenomen. 
Je kun een rolcheck doen door de route te omwikkelen met de `requireRole` functie. 

Vergeet je `authenticate`, dan is de route publiek en komt iedereen erin, zonder 401. 
Vergeet je alleen de rolcheck, dan komt elke ingelogde gebruiker erin, ongeacht de rol, zonder 403. 

Niets forceert dit; de drie tests per beschermd endpoint laten het zien, want zonder token moet het 401 zijn en met een verkeerde rol 403.

*Waar in de code kun je het vinden?* 
In de huidige validationConfig.kt in de testomgeving. De tests die worden uitgevoerd in `ValidationTests.kt` en `RoleAuthTests.kt` laten ook deze werkingen zijn.

### **08 - Hoe je de huidige gebruiker opvraagt voor de eigenaarscontrole**
*Toelichting*
Dit wordt opgevraagd via de functie **call.CurrentUser()** in `CurrentUserExtensions`, een extension functie op de ApplicationCall.
Hier worden de id en de rol uit de claims in de tokens opgehaald, om zo te kunnen verifiëren of de gebruiker bestaat. Voor de eigenaarscontrole vergelijk je dat id met het eigenaar-id van de resource. 

Klopt het niet, dan gooi je een `ForbiddenException`. 

*Waar in de code kun je het vinden?* 
In `CurrentUserExtensions.kt`

### **09 - Hoe je het test, en waarom de test met de verkeerde rol de belangrijkste is**
*Toelichting*
In `test/security` staat een testapp met testroutes. 
De tokens komen uit `TestTokens`, met een vaste klok, zodat de uitkomst nooit van de echte tijd afhangt. 
De tests dekken 401 (geen token, verkeerd secret, verlopen, onbekende rol), 200 (geldig token) en de rolcheck met een andere rol. 

De test met de verkeerde rol is de belangrijkste omdat een systeem waarin elke ingelogde gebruiker alles mag, alle gewone testen ook laat slagen. Alleen deze test bewijst dat een geldig token tóch wordt geweigerd bij de verkeerde rol. Ook de test met de onbekende rol heb je met "rood voor groen" bewezen, door de rolcontrole weg te halen en te zien dat hij faalde.

*Waar in de code kun je het vinden?* 
In de `test/security` map.
### **10 - Waarom het secret niet in Git staat, en waarom een hash met salt niet terug te rekenen is?**
*Toelichting*
De reden is dat als het in Git staat is dat iemand met toegang tot het secret, tokens met elke rol zou kunnen gaan ondertekenen. Ook tijdelijk opslaan moet je niet doen. Git bewaart alles, ook historische wijzigingen. 

Het staat in de variable JWT_SECRET, en er is enkel een placeholder in de application.yaml geplaatst.

Het wachtwoord wordt gehasht met Argon2id, in de `PasswordHasher`. Een hash is niet terug te rekenen, alleen te controleren door hetzelfde, juiste antwoord opnieuw te hashen. Dit wordt gedaan door `VerifyPassword`.
De salt is een willekeurige waarde in de hash zelf, waardoor hetzelfde wachtwoord wel twee verschillende hashes produceert. 

*Waar in de code kun je het vinden?* 
`JwtSettings.kt` (`security/service/`): het secret is een parameter, dus `security` leest het zelf nergens in.
`TestTokens.kt`: `TestJwtSettings` heeft `"test-secret"`. Dat is bewust een nepwaarde, alleen voor tests.
`PasswordHasher.kt` (`security/service/`): `hashPassword` hasht met Argon2id (`Argon2PasswordEncoder`), en `verifyPassword` controleert door opnieuw te hashen en te vergelijken, nooit door terug te rekenen.
`PasswordHasherTest.kt`: de test dat hetzelfde wachtwoord twee keer hashen twee verschillende hashes geeft (de salt), en de test dat een verkeerd wachtwoord niet klopt.
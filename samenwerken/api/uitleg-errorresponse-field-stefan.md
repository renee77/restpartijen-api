---
tags: [GI-4, foutafhandeling, StatusPages, ErrorResponse]
---

# Uitleg: `field` in `ErrorResponse`

Bronnen: [[FTD-restpartijen-webapi-v2_2#10.2 Validatie en foutafhandeling|§10.2]], GI-4 (besluit 4.1, 4.3, 4.4)
Code: `server/src/plugins/StatusPages.kt`, `server/src/plugins/ErrorResponse.kt`, `server/src/shared/DomainException.kt`

---

## 1. Wat is `field`?

Elke fout van de API heeft dezelfde vorm: `{ code, message, field? }`. Het veld `field` beantwoordt één vraag:

> **Welk stuk van de invoer moet de client aanpassen om deze fout op te lossen?**

Daarmee kan een frontend de melding direct naast het juiste invoerveld tonen, zonder de tekst van `message` te hoeven ontleden.

## 2. Waarom alleen bij validatie?

`field` is alleen zinvol als aan **twee voorwaarden** is voldaan:

1. De oorzaak is **één invoerwaarde** die op zichzelf fout is (bijv. negatieve `quantity`, lege `email`).
2. De code **weet welke** waarde dat is.

Bij alle andere fouten is de invoer op zichzelf prima, maar past ze niet bij de situatie. De oorzaak is dan *wie* er belt (401/403), *wat* er op de server staat (404/409), of een *combinatie* van waarden (422). Er is dan geen enkele plek om naar te wijzen, dus vertellen `code` en `message` wat er aan de hand is.

Kort gezegd: **`field` wijst naar foute invoer, en alleen validatiefouten worden gegarandeerd door één foute invoer veroorzaakt.**

## 3. Overzicht per exceptie

Volgorde zoals in `StatusPages.kt`.

| Exceptie | Status / `ErrorCode` | Oorzaak | Vult `field`? | Waarom |
|---|---|---|---|---|
| `ValidationException` (eigen) | 400 `VALIDATION_ERROR` | Een waarde in de body is op zichzelf fout, bijv. negatieve `quantity` of lege `email` | **Ja**, als de gooier het meegeeft | De fout zit op één bekende plek en de client lost het op door die ene waarde aan te passen. Het enige geval waarin onze eigen code de plek zeker weet. |
| `BadRequestException` (Ktor) | 400 `VALIDATION_ERROR` | Body kon niet gelezen worden: ongeldige JSON, ontbrekend veld, verkeerd type, onbekende enumwaarde | **Soms**, alleen bij een ontbrekend veld | `missingField()` haalt de naam uit de `MissingFieldException` van kotlinx.serialization. Bij kapotte JSON of een verkeerd type zegt de library niet betrouwbaar waar het misgaat, dus blijft `field` leeg. Gokken is erger dan leeg laten. |
| `RequestValidationException` (Ktor) | 400 `VALIDATION_ERROR` | Een validator gaf `ValidationResult.Invalid` terug in plaats van te gooien | **Nee** | Ktor geeft alleen `reasons` (lijst met strings) door, zonder veldnamen. De plek is in de validator misschien bekend, maar gaat onderweg verloren. Daarom heeft het gooien van `ValidationException` de voorkeur. |
| `UnauthorizedException` | 401 `UNAUTHORIZED` | Token ontbreekt of is ongeldig | Nee | Gaat over *wie* er belt. De token zit in een header, niet in een bodyveld. |
| `ForbiddenException` | 403 `FORBIDDEN` | Geldige identiteit, maar verkeerde rol of niet de eigenaar | Nee | Ook over *wie* er belt. Geen enkele invoerwijziging lost het op. |
| `NotFoundException` | 404 `NOT_FOUND` | De resource in de URL bestaat niet | Nee | Het probleem zit in het pad (`/reservations/42`). Alle bodyvelden kunnen geldig zijn terwijl de resource toch ontbreekt. |
| `IllegalStateTransitionException` | 409 `CONFLICT` | De huidige toestand van de resource staat deze actie niet toe | Nee | De oorzaak is serverstatus. Hetzelfde verzoek zou slagen op een resource in een andere toestand. |
| `DomainRuleException` | 422 `DOMAIN_RULE_VIOLATION` | Verzoek is goed opgebouwd maar breekt een bedrijfsregel | Nee (ontwerpkeuze) | Elke waarde is op zichzelf geldig. Het probleem is een combinatie van waarden, of waarden tegenover serverdata, dus één veld aanwijzen is vaak willekeurig. Grijs gebied: "aantal groter dan voorraad" zou naar `quantity` kunnen wijzen. |
| `Throwable` (vangnet) | 500 `INTERNAL_ERROR` | Iets onverwachts: een bug, databasefout, onverwachte null | **Nee, bewust** | De client heeft niets fout gedaan, dus er is geen invoer om te verbeteren. De vaste melding (stacktrace alleen in de log, besluit 4.4) houdt interne details uit de response. Een `field` zou interne namen kunnen lekken. |

## 4. Kanttekeningen

- **De drie 400-regels laten zien dat beide voorwaarden nodig zijn.** Het zijn alle drie invoerfouten, maar alleen `ValidationException` weet de plek altijd. `BadRequestException` weet hem soms, `RequestValidationException` is hem kwijt.
- **422 is een keuze, geen wet.** Als het ooit nodig is, kan `DomainRuleException` een `field` krijgen zonder iets te breken, want `field` is al nullable in `ErrorResponse`.

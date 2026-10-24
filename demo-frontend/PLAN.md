# Plan: demo-frontend voor restpartijen-api

## Context

Voor het portfolio-filmpje (deadline zondag 25 oktober 2026) moet elke user story (US-01 t/m US-11) van de Ktor Web API van begin tot eind te tonen zijn. Er is nog geen frontend; de Android-app komt in periode 2. Daarom komt er een duidelijk als "demo" herkenbare statische pagina die de API aanroept en per stap het endpoint, de request en de response laat zien.

Eva koos: statische HTML/JS zonder build, map `demo-frontend/` in de repo op branch `feat/demo-frontend`, alle stories US-01 t/m US-11, demo-accounts en demowachtwoord vooraf ingevuld, het productcontract van `feat/opening-hours`, en de huisstijl uit `Hoe onze codebase eruitziet.pptx` (logo useITtoo en de logokleuren).

## Huisstijl (uit de pptx)

| Rol | Kleur | Bron |
|---|---|---|
| Primair (knoppen, banner-accent) | `#ED8938` oranje | logo + slides |
| Secundair (blad, badges rol/status) | `#98B870` lichtgroen | logo-blad |
| Koppen, donker accent | `#2F5233` / `#3E762A` donkergroen | slides |
| Zachte vlakken | `#D5ECD4` lichtgroen, `#F0F0F0` grijs | slides/thema |
| Tekst | `#3E3E3E`, secundair `#666666` | thema |
| Font | Arial | thema |

Logo: `ppt/media/image-1-1.png` (1792×576, "useITtoo") → `demo-frontend/assets/logo.png`, in de banner.

## Aannames (vastleggen in demo-frontend/README.md)

| # | Aanname | Reden |
|---|---|---|
| A1 | Productcontract van `feat/opening-hours`: `bestBeforeAt` is een datum (`"2027-06-30"`), geen `pickupWindow` in POST/PUT. | Dat is het contract na de merge van PR #70 (morgen). |
| A2 | US-04 volgens `feature/stefan-product-search`: `GET /products?category=&maxPrice=&minShelfLifeHours=`; antwoord `SearchResultResponse` met `supplier{id,name,latitude,longitude}`, `currentPrice`, `shelfLifeRemainingHours`, `pickupFrom/Until`. | Enige bestaande implementatie. |
| A3 | US-06: `DELETE /reservations/{id}` → 204, geen body. US-11: `POST /reservations/{id}/collect` → 200 met de reservering (status `COLLECTED`). | FTD §10.1 en §5.6/§5.11; nog nergens in code. |
| A4 | Login geeft geen `supplierId` (`LoginResponse` = `{token, role}`), ook niet op `feat/opening-hours`. De demo kent de supplier-id's uit de seed: 1 supermarkt, 2 bakker, 3 groothandel. | `SeedData.kt:90-94`. |
| A5 | Barcode voor US-02: `3017620422003` (Nutella), zoals `requests.http:135`. | Bestaand voorbeeld. |
| A6 | Pagina draait op `http://localhost:8000` via `python3 -m http.server`; CORS `anyHost()` laat dat toe zonder API-wijziging. | `server/src/plugins/Cors.kt:15-21`. |
| A7 | Verificatie vannacht tegen een `git archive`-export van `feat/opening-hours` in de scratchpad (geen branch-switch in Eva's checkout). US-04/06/11 tonen dan de 404 van de API. | `main` mist US-10 en het nieuwe contract. |

## Bestanden (nieuw, niets in `server/` wijzigt)

```
demo-frontend/
  index.html   # één pagina: banner, accountkeuze, stappen per story, request/response-paneel
  app.js       # api(method, path, body) helper, tokenopslag per rol, één functie per story
  style.css    # sober; banner "Demo-frontend voor de Web API"
  README.md    # doel, starten, aannames A1-A7, wie welke story bouwde
  PLAN.md      # kopie van dit plan (Eva's keuze, 24 okt)
  assets/logo.png
```

Geen framework, geen build, geen dependencies. Teksten in de UI in het Nederlands, code en comments in het Engels.

## Opzet van de pagina

- Banner: "Demo-frontend voor de Web API — geen product, geen Android-app. Toont de user stories van restpartijen-api."
- Linksboven: API-basis-URL (`http://localhost:8080/api/v1`, aanpasbaar) en inloggen: keuzelijst met de zes seed-accounts, wachtwoord ingevuld, knop "Inloggen" (`POST /auth/login`). Token per rol onthouden in geheugen (SUPPLIER/COLLECTOR/ADMIN), zodat de flow niet steeds opnieuw inlogt.
- Rechts, vast: paneel "Laatste aanroep" met methode + pad, request-headers (token ingekort), request-body, status + response-body (pretty JSON). Dit is wat het filmpje laat zien.
- Hoofdkolom: stappen in flow-volgorde, elk met kop `US-xx — titel`, rol, endpoint, een klein formulier (vooringevuld) en één knop.

Flow-volgorde (en wie bouwde het):
1. Inloggen als aanbieder (`supermarkt@mail.com`) — Lonneke, GI-3
2. US-10 Openingstijden tonen en vastleggen (`GET/PUT /suppliers/1/opening-hours`) — Eva
3. US-02 Barcode opzoeken (`GET /products/lookup/{barcode}`) — Eva; naam en allergenen vloeien door naar stap 4
4. US-01 Partij plaatsen (`POST /products`) — Eva; id onthouden
5. US-03 Mijn aanbod (`GET /suppliers/1/products`), wijzigen (`PUT /products/{id}`), verwijderen (`DELETE /products/{id}`) — Eva
6. US-07 Actuele prijs (`GET /products/{id}`: `originalPrice`, `discountPercentage`, `currentPrice`) — Lonneke
7. Inloggen als afhaler (`afhaler-a@mail.com`)
8. US-04 Zoeken (`GET /products` met filters) — Stefan
9. US-05 Reserveren (`POST /reservations`) en eigen reserveringen (`GET /reservations`) — Stefan
10. US-06 Intrekken (`DELETE /reservations/{id}`) — Stefan
11. US-11 Ophalen bevestigen (`POST /reservations/{id}/collect`) — Stefan
12. Inloggen als beheerder (`admin@mail.com`)
13. US-08 Statusbewaking (`POST /admin/maintenance/expire`) — Lonneke
14. US-09 Beheeroverzicht met statusfilter (`GET /admin/products?status=`) en verwijderen (`DELETE /admin/products/{id}`) — Lonneke

Ook foutpaden zijn te tonen: elk formulier is bewerkbaar, dus een lege naam (400), een verlopen datum (422) of een verkeerde rol (403) is in beeld te brengen zonder extra code.

## Verificatie

1. API starten vanuit de export van `feat/opening-hours`: `.env` laden (`set -a; . .env; set +a`, niets printen) en `./kotlin run` in de achtergrond; wachten tot poort 8080 antwoordt.
2. `python3 -m http.server 8000` in `demo-frontend/` in de achtergrond.
3. In de ingebouwde browser elke stap doorklikken; per use case een screenshot. Screenshots komen in de scratchpad en in het verslag, niet in de repo (de branch bevat alleen de demo zelf).
4. Verslag in de gedeelde notitiemap (`/home/eva/.config/Claude/side-session-notes/local_4f50e929-.../demo-frontend-verslag.md`) met per story: werkt / toont 404 (nog niet geïmplementeerd) / niet getest.
5. Geen tests voor de demo zelf: één pagina zonder logica buiten `fetch`-aanroepen; de API-tests dekken het gedrag.

## Niet doen

- Niets wijzigen in `server/`, `requests.http`, README van de repo.
- Niet committen of pushen. Commitbericht klaarzetten:
  `feat(demo-frontend): add static demo page for the user stories`

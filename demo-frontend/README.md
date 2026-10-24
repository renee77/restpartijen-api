# Demo-frontend voor de Web API

Eén statische pagina (HTML, CSS, JavaScript; geen build, geen framework) die de user stories van
`restpartijen-api` laat zien voor het portfolio-filmpje. Het is nadrukkelijk een **demo van de API**:
geen product en geen Android-app (die komt in periode 2). Elke knop doet één HTTP-aanroep; het paneel
rechts toont de request (methode, pad, headers, body) en de response (status, body).

## Starten

1. Start de API vanuit de root van de repo (poort 8080). `./kotlin run` leest `.env` niet zelf, dus laad die eerst
   in dezelfde regel (`JWT_SECRET` en `OFF_CONTACT_EMAIL` zijn verplicht, zie de README van de API):

       set -a; . ./.env; set +a; ./kotlin run

2. Serveer deze map op een lokale poort. Open de pagina niet als `file://`: dan is de origin `null` en gelden
   andere browserregels.

       cd demo-frontend
       python3 -m http.server 8000

3. Open <http://localhost:8000> en log in met een demo-account (keuzelijst, wachtwoord staat ingevuld;
   het is het demowachtwoord uit de README van de API).

De API staat CORS toe voor elke origin (`server/src/plugins/Cors.kt`), dus er is niets aan de API gewijzigd.

## Flow in de demo

| Stap | Story | Rol | Endpoint | Gebouwd door |
|---|---|---|---|---|
| 1 | Inloggen | – | `POST /auth/login` | Lonneke (GI-3) |
| 2 | US-10 Openingstijden | SUPPLIER | `GET`/`PUT /suppliers/{id}/opening-hours` | Eva |
| 3 | US-02 Barcode | SUPPLIER | `GET /products/lookup/{barcode}` | Eva |
| 4 | US-01 Plaatsen | SUPPLIER | `POST /products` | Eva |
| 5 | US-03 Beheren | SUPPLIER | `GET /suppliers/{id}/products`, `PUT`/`DELETE /products/{id}` | Eva |
| 6 | US-07 Prijs | – | `GET /products/{id}` | Lonneke |
| 7 | US-04 Zoeken | – | `GET /products?category&maxPrice&minShelfLifeHours` | Stefan |
| 8 | US-05 Reserveren | COLLECTOR | `POST /reservations`, `GET /reservations` | Stefan |
| 9 | US-06 Intrekken | COLLECTOR | `DELETE /reservations/{id}` | Stefan |
| 10 | US-11 Ophalen | COLLECTOR | `POST /reservations/{id}/collect` | Stefan |
| 11 | US-08 Statusbewaking | ADMIN | `POST /admin/maintenance/expire` | Lonneke |
| 12 | US-09 Beheer | ADMIN | `GET /admin/products?status`, `DELETE /admin/products/{id}` | Lonneke |

Waarden vloeien door: de barcode-lookup vult naam en allergenen in bij US-01, het id van een geplaatste partij
komt in US-03, US-07 en US-05, en het id van een reservering in US-06 en US-11. Elk veld blijft bewerkbaar, dus
foutpaden (400, 403, 404, 409, 422) zijn met dezelfde knoppen te tonen.

## Aannames

De demo is gebouwd in de nacht van 23 op 24 oktober 2026, voordat alle branches gemerged waren.

| # | Aanname | Reden |
|---|---|---|
| A1 | Productcontract van `feat/opening-hours` (PR #70): `bestBeforeAt` is een datum (`"2027-06-30"`), zonder `pickupWindow`; de API leidt het afhaalvenster af uit de openingstijden. | Sinds 24 oktober ook op `main`. |
| A2 | US-04 volgens `feature/stefan-product-search`: filters `category`, `maxPrice`, `minShelfLifeHours`; antwoord met `supplier{id,name,latitude,longitude}`, `currentPrice`, `shelfLifeRemainingHours`, `pickupFrom`, `pickupUntil`. | Sinds 24 oktober op `main` (PR #69). |
| A3 | US-06 `DELETE /reservations/{id}` geeft 204 zonder body; US-11 `POST /reservations/{id}/collect` geeft 200 met de reservering en `collectedAt`. | Sinds 24 oktober op `main` (PR #73 en #75). |
| A4 | Login geeft `supplierId` voor een aanbieder (`null` voor afhaler en beheerder), sinds PR #72. De demo gebruikt dat id voor `/suppliers/{id}/...`. | FTD §10.1. |
| A5 | Barcode `3017620422003` (Nutella) als voorbeeld voor US-02. | Zelfde voorbeeld als `requests.http`. |
| A6 | Pagina op `http://localhost:8000` via `python3 -m http.server`. | CORS `anyHost()`; geen wijziging aan de API nodig. |

Zie `PLAN.md` voor het plan waarop deze demo is gebouwd.

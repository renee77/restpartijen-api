Feature F3: prijs, houdbaarheid en statusbewaking, plus de beheerdersendpoints.

- `model`: de afprijsstaffels (`DiscountPolicy` met drie implementaties, `DiscountTier`, `DiscountPolicyResolver`) en `MaintenanceReport`.
- `service`: `PricingService` (implementeert `PriceProvider`), `ExpiryScheduler` (de statusbewaking, US-08) en `AdminProductService` (beheeroverzicht en verwijderen als beheerder, US-09).
- `routes`: `configurePricingRouting()` met `GET /admin/products`, `DELETE /admin/products/{id}` en `POST /admin/maintenance/expire`.
- `dto`: `AdminProductResponse`.
- `MaintenanceLoop.kt`: de periodieke run van de statusbewaking (B-41).

Verwijderen als beheerder gebruikt twee contracten achter elkaar, in één transactie: eerst de reservering laten vervallen via `ReservationMaintenance`, daarna de partij op `REMOVED` zetten via `ProductStatusUpdater` (§5.9, B-38, B-39).

Eigenaar: Lonneke
Feature: F3

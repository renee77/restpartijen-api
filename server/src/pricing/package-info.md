Feature F3: price, shelf life and status monitoring, plus the admin endpoints.

- `model`: the discount tiers (`DiscountPolicy` with three implementations, `DiscountTier`, `DiscountPolicyResolver`) and `MaintenanceReport`.
- `service`: `PricingService` (implements `PriceProvider`), `ExpiryScheduler` (the status monitoring, US-08) and `AdminProductService` (admin overview and removal by an admin, US-09).
- `routes`: `configurePricingRouting()` with `GET /admin/products`, `DELETE /admin/products/{id}` and `POST /admin/maintenance/expire`.
- `dto`: `AdminProductResponse`.
- `MaintenanceLoop.kt`: the periodic run of the status monitoring (B-41).

Removal by an admin uses two contracts in a row, in one transaction: first the reservation lapses through `ReservationMaintenance`, then the product is set to `REMOVED` through `ProductStatusUpdater` (§5.9, B-38, B-39).

Owner: Lonneke
Feature: F3

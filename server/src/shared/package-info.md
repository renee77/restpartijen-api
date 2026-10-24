The shared domain core (GI-5). The only package that every feature may import. It holds everything that two or more features need:

| Kind | Contents |
|---|---|
| Enums | `Role`, `ProductStatus` (with the final status `REMOVED`, B-19), `ProductCategory`, `Allergen` (the fourteen from §9.8) |
| Value types | `Money`, `PriceBreakdown`, `PickupWindow`, `SupplierSummary` |
| Exceptions | `DomainException` with the six exceptions from §10.2, and `ConflictException` (issue #47) |
| Contracts | `ProductReader`, `ProductStatusUpdater`, `ProductView`, `PricedProduct`, `PriceProvider`, `ReservationMaintenance`, `SupplierIdLookup` (K-7) |

**What does not go in (decision 5.4):** implementations of contracts (they live with the feature that provides them), logic, and anything that only one feature uses (`ReservationStatus` belongs to F2 and lives in `reservation`).

**Ground rules:** no owner, except `Money`, `Role` and `PriceBreakdown` (Lonneke) and `PickupWindow` (Eva); they live here because others need them. Every change goes through a pull request with two reviews. Adding is allowed; changing or removing needs agreement beforehand (decision 5.3), because a renamed enum value silently breaks the code of the other two developers. `shared` never imports a feature package.

A value type may guard its own rules (`Money` refuses a negative amount, `PickupWindow` checks that the start lies before the end). That is not domain logic. A calculation across several objects or over time, such as the discount tiers, belongs in a feature.

Owner: joint (GI-5)

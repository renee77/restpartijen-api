Feature F1: aanbod en productdata. Het model van een partij (`SurplusProduct` met drie soorten), de repository op `ProductsTable`, de implementaties van de contracten `ProductReader`, `ProductStatusUpdater` en `SupplierIdLookup`, en de endpoints onder `/products`.

De koppeling met Open Food Facts (US-02) en de vertaling van allergenen staan hier ook; de enum `Allergen` zelf staat in `shared`.

De openingstijden van een aanbieder (US-10) horen bij deze package: het model `OpeningHours`, de tabel `supplier_opening_hours`, de endpoints `GET` en `PUT /suppliers/{id}/opening-hours`, en `PickupWindowCalculator`, die het afhaalvenster van een partij uit de openingstijden afleidt.

Eigenaar: Eva
Feature: F1

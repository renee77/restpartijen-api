Feature F1: offerings and product data. The model of a product (`SurplusProduct` with three kinds), the repository on `ProductsTable`, the implementations of the contracts `ProductReader`, `ProductStatusUpdater` and `SupplierIdLookup`, and the endpoints under `/products`.

The Open Food Facts integration (US-02) and the translation of allergens live here too; the enum `Allergen` itself lives in `shared`.

The opening hours of a supplier (US-10) belong to this package: the model `OpeningHours`, the table `supplier_opening_hours`, the endpoints `GET` and `PUT /suppliers/{id}/opening-hours`, and `PickupWindowCalculator`, which derives the pickup window of a product from the opening hours.

Owner: Eva
Feature: F1

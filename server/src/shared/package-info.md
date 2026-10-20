De gedeelde domeinkern (GI-5). De enige package die elke feature mag importeren. Hier staat alles wat twee of meer features nodig hebben:

| Soort | Inhoud |
|---|---|
| Enums | `Role`, `ProductStatus` (met de eindstatus `REMOVED`, B-19), `ProductCategory`, `Allergen` (de veertien uit §9.8) |
| Waardetypes | `Money`, `PriceBreakdown`, `PickupWindow`, `SupplierSummary` |
| Excepties | `DomainException` met de zes excepties uit §10.2; `ConflictException` komt erbij via issue #47 |
| Contracten | `ProductReader`, `ProductStatusUpdater`, `ProductView`, `PricedProduct`, `PriceProvider`, `ReservationMaintenance`, `SupplierIdLookup` (K-7) |

**Wat er niet in komt (besluit 5.4):** implementaties van contracten (die staan bij de leverende feature), logica, en alles wat maar één feature gebruikt (`ReservationStatus` is van F2 en staat in `reservation`).

**Spelregels:** geen eigenaar, behalve `Money`, `Role` en `PriceBreakdown` (Lonneke) en `PickupWindow` (Eva); die staan hier omdat anderen ze nodig hebben. Elke wijziging gaat via een pull request met twee reviews. Toevoegen mag; wijzigen of verwijderen vraagt overleg vooraf (besluit 5.3), want een hernoemde enumwaarde breekt stilzwijgend de code van twee anderen. `shared` importeert nooit een featurepackage.

Een waardetype mag zijn eigen regels bewaken (`Money` weigert een negatief bedrag, `PickupWindow` controleert dat het begin vóór het einde ligt). Dat is geen domeinlogica. Een berekening over meerdere objecten of over de tijd, zoals de afprijsstaffels, hoort in een feature.

Eigenaar: gezamenlijk (GI-5)

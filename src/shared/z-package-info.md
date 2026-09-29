De map waar alle gedeelde logica in wordt opgeslagen. De enige package die wij mogen importeren in onze features. 
De gedeelde domeinkern. Hier staat alles wat 2 of meer features nodig heeft. Dit bestaat uit: 

Soort	        Inhoud
Enums	        Role, ProductStatus (met REMOVED bij beslispunt 3), ProductCategory, Allergen (de veertien uit §9.8)
Waardetypes	    Money, PriceBreakdown, PickupWindow
Excepties	    DomainException met de zes excepties uit §10.2
Contracten	    ProductReader, ProductStatusUpdater, ProductView, PricedProduct, PriceProvider, ReservationMaintenance

**Wat er níet in komt (besluit 5.4):**
**Implementaties van contracten.** Die staan bij de leverende feature.
**Logica.** Alleen waardetypes, enums en interfaces.
**Iets dat maar één feature gebruikt.** ReservationStatus is bijvoorbeeld alleen van F2 en staat dus in reservation.

**De spelregels:**
Geen eigenaar, behalve Money, Role en PriceBreakdown (Lonneke) en PickupWindow (jij). Die staan hier omdat anderen ze nodig hebben, niet omdat ze gedeeld eigendom zijn.
Elke wijziging via een pull request met twee reviews (GR2).
Toevoegen mag, wijzigen of verwijderen vraagt overleg vooraf (besluit 5.3). Een hernoemde enumwaarde breekt stilzwijgend de code van twee anderen.
shared importeert nooit een featurepackage (GR1).
Eén ding om scherp te hebben. "Geen logica" en "waardetype" schuren een beetje. Money krijgt later gedrag, zoals rekenen en afronden op centen (F3 T1). PickupWindow zal controleren dat het begin vóór het einde ligt. Dat mag: een waardetype dat zijn eigen regels bewaakt, is geen domeinlogica. Wat níet mag, is een berekening die over meerdere objecten of over de tijd gaat, zoals de afprijsstaffels. Als iemand bij het assessment vraagt waarom Money gedrag heeft terwijl shared geen logica bevat, is dat je antwoord.
Optioneel. Met zo'n vijftien bestanden kan een subindeling helpen, bijvoorbeeld contract, model en exception. Dat is een keuze voor de groep, niet vastgelegd in het FTD.
Voor src/shared/package-info.md, bijvoorbeeld:
"Shared domain core: enums, value types, DomainException hierarchy and all cross-feature contracts. No implementations, no feature imports. Changes via PR with two reviews (GI-5)."


Eigenaar: iedereen (duh)
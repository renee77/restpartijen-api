Persistence is verantwoordelijk voor het opslaan en ophalen van gegevens in een applicatie. Het omvat de mechanismen en strategieën die worden gebruikt om gegevens op te slaan in databases, bestanden of andere opslagmedia, en om deze gegevens later weer op te halen voor gebruik in de applicatie.

Een tabel waar een andere feature naar verwijst, staat hier (issue #36). Elke feature mag `persistence` importeren, dus zo kan elke feature een foreign key naar die tabel leggen. De eigenaar van de tabel blijft dezelfde (§9.2); alleen de plek verandert. Nu staan hier `UsersTable` (Lonneke), `ProductsTable` en `SuppliersTable` (Eva).

Eigenaar: Gedeeld: Eva
Onderdeel: GI-1 Persistentielaag

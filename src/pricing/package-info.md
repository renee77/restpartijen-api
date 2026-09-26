In pricing wordt de prijs, prijsberekening, houdbaarheid en houdbaarheidsonderhoud van een product bijgehouden. Ook wordt hier de prijsberekening van een product gedaan.


!! Beslispunt - Removed gebruiken voor ADMIN product service. Eerst laten vallen via maintenance, daarna via pricing omzetten naar Removed.
Verwijderen als beheerder hangt aan beslispunt 3. Neemt de groep REMOVED over, dan gebruikt AdminProductService twee contracten achter elkaar: eerst de reservering laten vervallen via ReservationMaintenance, daarna de partij op REMOVED zetten via ProductStatusUpdater. Dat moet in één transactie (draaiboek, blok 4).
Eigenaar: Lonneke
Feature: F3
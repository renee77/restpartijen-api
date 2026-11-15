## Wat

<!-- Wat verandert er? Een paar zinnen, in gewone taal. -->

## Waarom

<!-- Het probleem of het besluit erachter. Verwijs naar het FTD (A-nummer, §) of het draaiboek. -->

## Issue

<!--
Schrijf hier per issue dat deze PR afmaakt: Closes #<nummer>
Bijvoorbeeld: Closes #94

Waarom: bij de merge naar main sluit GitHub dat issue dan vanzelf, en loopt de epic mee.
Zo blijft het overzicht van wie waar staat kloppen, zonder dat iemand het met de hand bijhoudt.

Let op:
- Gebruik "Closes" alleen als deze PR het issue helemaal af maakt (alles bij "Klaar als").
- Maakt de PR het issue maar voor een deel af, schrijf dan "Deel van #<nummer>". Dan blijft het issue open.
- Eén regel per issue; "Closes #94, #95" sluit alleen #94.
-->

## Hoe gecontroleerd

- [ ] `./kotlin build` is groen (warnings zijn fouten)
- [ ] `./kotlin test -m shared` is groen (en `./kotlin test -m server` als de server geraakt is)
- [ ] Elke nieuwe test is één keer rood geweest
- [ ] `ktlint` geeft geen meldingen, of alleen bekende met de reden erbij
- [ ] De app start op de emulator (bij een wijziging in de app)

## Definition of Done (FTD §7)

- [ ] Alle teksten staan in `strings.xml`, in het Nederlands en het Engels
- [ ] Het FTD is bijgewerkt waar nodig (A-02)
- [ ] Geen geheimen in de PR: geen `.env`, keystore of wachtwoord
- [ ] Het gebruik van AI staat in het AI-logboek

## Review

<!--
Raakt deze PR shared/src/nl/restpartijen/app/core/, App.kt, AppModule.kt, AppNavigation.kt,
strings.xml, libs.versions.toml of een module.yaml? Dan reviewen beide anderen. Anders één.
-->

## Bekend en geaccepteerd

<!-- Optioneel: wat bewust niet is opgelost, en waarom. Schrap dit kopje als er niets is. -->

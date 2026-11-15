package nl.restpartijen.app.pricing

import org.koin.dsl.module

// No platform module: Product detail only calls the API and needs no Android library;
// pricing is therefore not in the table of platform code in FTD §8.8.

/** Koin definitions of the pricing feature: its repositories and ViewModels (A-29). */
val pricingModule = module { }

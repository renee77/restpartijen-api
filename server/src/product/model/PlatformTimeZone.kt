package com.restpartijen.api.product.model

import kotlinx.datetime.TimeZone

/**
 * The one time zone of the platform (§9.9). Opening hours and best-before dates are local times in it;
 * PickupWindowCalculator and the request DTOs turn them into instants with this zone.
 */
val PLATFORM_TIME_ZONE: TimeZone = TimeZone.of("Europe/Amsterdam")

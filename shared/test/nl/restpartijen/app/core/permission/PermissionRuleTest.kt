package nl.restpartijen.app.core.permission

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The fixed permission rule of FTD §15, plus "Niet nu" (issue #95). The rule is shared by the local
 * network, camera and location permissions. For the local network, Android 16 and lower pass granted = true, because the
 * permission does not exist there.
 */
class PermissionRuleTest {
    @Test
    fun showsTheAppWhenThePermissionIsGranted() {
        val step = nextPermissionStep(
            PermissionFacts(granted = true, notNow = false, showRationale = false, alreadyAsked = false),
        )

        assertEquals(PermissionStep.ShowApp, step)
    }

    @Test
    fun asksTheSystemWhenNotAskedYet() {
        val step = nextPermissionStep(
            PermissionFacts(granted = false, notNow = false, showRationale = false, alreadyAsked = false),
        )

        assertEquals(PermissionStep.AskSystem, step)
    }

    // Denied once in this run: Android still allows a rationale, so explain and offer to ask again (FTD §15, rule 1).
    @Test
    fun explainsAndOffersToAskAgainAfterTheFirstDenial() {
        val step = nextPermissionStep(
            PermissionFacts(granted = false, notNow = false, showRationale = true, alreadyAsked = true),
        )

        assertEquals(PermissionStep.ExplainAndAskAgain, step)
    }

    // Denied once in an earlier run: Android still allows a rationale, so explain first instead of asking blindly.
    @Test
    fun explainsFirstWhenDeniedOnceInAnEarlierRun() {
        val step = nextPermissionStep(
            PermissionFacts(granted = false, notNow = false, showRationale = true, alreadyAsked = false),
        )

        assertEquals(PermissionStep.ExplainAndAskAgain, step)
    }

    // Denied twice: Android no longer shows its dialog, so the app says where to turn it on (FTD §15, rule 2).
    @Test
    fun explainsWhereToTurnItOnAfterRepeatedDenial() {
        val step = nextPermissionStep(
            PermissionFacts(granted = false, notNow = false, showRationale = false, alreadyAsked = true),
        )

        assertEquals(PermissionStep.ExplainWhereToTurnOn, step)
    }

    // "Niet nu" never blocks the app: its screens then show the network error with "Opnieuw" (FTD §11.2).
    @Test
    fun showsTheAppAfterNotNow() {
        val step = nextPermissionStep(
            PermissionFacts(granted = false, notNow = true, showRationale = true, alreadyAsked = true),
        )

        assertEquals(PermissionStep.ShowApp, step)
    }

    // "Niet nu" on the where-to-turn-on screen must also open the app, or the user is stuck there.
    @Test
    fun showsTheAppAfterNotNowOnTheWhereToTurnOnScreen() {
        val step = nextPermissionStep(
            PermissionFacts(granted = false, notNow = true, showRationale = false, alreadyAsked = true),
        )

        assertEquals(PermissionStep.ShowApp, step)
    }

    // Granted after the dialog or in the settings: the app opens, whatever else Android or the gate still reports.
    @Test
    fun showsTheAppWhenGrantedEvenIfOtherFactsAreSet() {
        val step = nextPermissionStep(
            PermissionFacts(granted = true, notNow = false, showRationale = true, alreadyAsked = true),
        )

        assertEquals(PermissionStep.ShowApp, step)
    }
}

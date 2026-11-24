package nl.restpartijen.app.core.permission

// The fixed permission rule of FTD §15, shared by every part of the app that needs a runtime permission:
// the local network (core, issue #95), the camera (product) and location (reservation). It only knows
// true/false facts, so it holds no Android code and no texts: each part fetches its own facts on Android
// and shows its own screen.

/**
 * What the app knows right now about one runtime permission. The four facts hold at the same time,
 * so they are fields of one class and not values of an enum.
 *
 * @property granted the user allowed the permission. Android 16 and lower pass true for
 *   ACCESS_LOCAL_NETWORK, because the permission does not exist there.
 * @property notNow the user chose "Niet nu" on our own explanation screen during this run.
 *   Android does not know about it, so it never counts as a denial.
 * @property showRationale Android's shouldShowRequestPermissionRationale: true after one denial.
 * @property alreadyAsked the app showed Android's dialog during this run.
 */
data class PermissionFacts(
    val granted: Boolean,
    val notNow: Boolean,
    val showRationale: Boolean,
    val alreadyAsked: Boolean,
)

/** The one thing the app does next, decided by the fixed rule of FTD §15. */
enum class PermissionStep {
    // Show the app itself.
    ShowApp,

    // Our explanation screen, with "Opnieuw vragen" and "Niet nu".
    ExplainAndAskAgain,

    // Android's own permission dialog.
    AskSystem,

    // Our explanation screen that says where to turn the permission on, with "Niet nu".
    ExplainWhereToTurnOn,
}

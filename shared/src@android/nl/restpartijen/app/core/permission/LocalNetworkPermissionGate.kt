package nl.restpartijen.app.core.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect

/**
 * Shows [content] only when the app may use the local network (FTD §15).
 * Android 16 and lower always pass. On Android 17 and higher [nextPermissionStep] decides
 * what to show: the app, Android's permission dialog, or [PermissionExplanationScreen].
 * When the user comes back from the phone's settings, the gate checks again.
 */
@Composable
fun LocalNetworkPermissionGate(content: @Composable () -> Unit) {
    // The Android context: our access to the system, used to check the permission.
    val context = LocalContext.current
    // The Activity is the Android screen that hosts this UI (MainActivity). Only an
    // Activity can tell whether the user denied the permission before
    // (shouldShowRequestPermissionRationale). It is null where no Activity exists,
    // such as in a preview.
    val activity = LocalActivity.current

    // Facts Android knows. They live in state, so the gate redraws when they change,
    // for example after the user answered Android's dialog. Plain remember is enough:
    // they are read from Android again, so they do not need to survive rotation.
    var granted by remember { mutableStateOf(context.hasLocalNetworkAccess()) }
    var showRationale by remember { mutableStateOf(activity.shouldShowLocalNetworkRationale()) }

    // Facts the gate keeps itself, both false when the app starts:
    // - alreadyAsked: Android's dialog has been shown during this run.
    // - notNow: the user chose "Niet nu" on the explanation screen.
    // mutableStateOf makes Compose redraw the gate when a value changes.
    // rememberSaveable keeps the value across redraws and when the screen rotates.
    // `by` lets us write `notNow` instead of `notNow.value`.
    var alreadyAsked by rememberSaveable { mutableStateOf(false) }
    var notNow by rememberSaveable { mutableStateOf(false) }

    // True while Android's dialog is open. After a rotation the AskSystem effect below runs
    // again; without this flag it would start a second dialog on top of the first.
    var dialogOpen by rememberSaveable { mutableStateOf(false) }

    // Starts Android's permission dialog. The lambda runs when the user has answered it:
    // the dialog has now been shown, and Android tells whether the permission was granted.
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        dialogOpen = false
        alreadyAsked = true
        granted = isGranted
        showRationale = activity.shouldShowLocalNetworkRationale()
    }

    fun askAndroid() {
        dialogOpen = true
        launcher.launch(Manifest.permission.ACCESS_LOCAL_NETWORK)
    }

    // Runs every time the app comes back to the front (resume), for example from the phone's
    // settings, where the user may have turned the permission on (FTD §15, rule 2). Reading the
    // facts again changes the state, so the gate redraws and opens the app by itself.
    LifecycleResumeEffect(Unit) {
        granted = context.hasLocalNetworkAccess()
        showRationale = activity.shouldShowLocalNetworkRationale()
        onPauseOrDispose { }
    }

    val facts = PermissionFacts(
        granted = granted,
        notNow = notNow,
        showRationale = showRationale,
        alreadyAsked = alreadyAsked,
    )
    when (nextPermissionStep(facts)) {
        PermissionStep.ShowApp -> content()
        // Launching during composition crashes, so it runs in an effect.
        // Nothing is shown behind Android's dialog, so the app does not call the API yet.
        PermissionStep.AskSystem -> LaunchedEffect(Unit) {
            if (!dialogOpen) askAndroid()
        }
        PermissionStep.ExplainAndAskAgain -> PermissionExplanationScreen(
            canAskAgain = true,
            onAskAgain = { askAndroid() },
            onNotNow = { notNow = true },
        )
        PermissionStep.ExplainWhereToTurnOn -> PermissionExplanationScreen(
            canAskAgain = false,
            // The button is hidden here, Android no longer shows the dialog.
            onAskAgain = {},
            onNotNow = { notNow = true },
        )
    }
}

/**
 * True when the app may use the local network. ACCESS_LOCAL_NETWORK only exists
 * from Android 17 (API 37); on older versions INTERNET is enough (FTD §11.4).
 */
private fun Context.hasLocalNetworkAccess(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.CINNAMON_BUN ||
    checkSelfPermission(Manifest.permission.ACCESS_LOCAL_NETWORK) == PackageManager.PERMISSION_GRANTED

/**
 * True after the user denied the permission once, so the app should explain why it needs it.
 * No Activity (for example in a preview) means no explanation is needed.
 */
private fun Activity?.shouldShowLocalNetworkRationale(): Boolean =
    this?.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_LOCAL_NETWORK) ?: false

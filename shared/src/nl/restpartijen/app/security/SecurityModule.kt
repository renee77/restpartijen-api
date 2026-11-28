package nl.restpartijen.app.security

import nl.restpartijen.app.security.ui.LoginViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** Koin definitions of the security feature: its repositories and ViewModels (A-29). */
val securityModule = module {
    viewModel { LoginViewModel(get()) }
}

package org.mateuszkapitula.project

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.mateuszkapitula.project.repository.User
import org.mateuszkapitula.project.service.DatabaseManager
import org.mateuszkapitula.project.view.LoginScreen

fun main() = application {
    DatabaseManager.connect()
    var currentUser by remember { mutableStateOf<User?>(null) }

    if (currentUser == null) {
        Window(onCloseRequest = ::exitApplication, title = "Logowanie") {
            LoginScreen(
                onLoginSuccess = { user -> currentUser = user },
                onExit = ::exitApplication
            )
        }
    } else {
        Window(onCloseRequest = ::exitApplication, title = "System Biblioteczny") {
            _root_ide_package_.org.mateuszkapitula.project.view.AppView(currentUser!!)
        }
    }
}
package org.mateuszkapitula.project.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import org.mateuszkapitula.project.repository.User
import org.mateuszkapitula.project.repository.UserRepository

@Composable
fun LoginScreen(onLoginSuccess: (User) -> Unit, onExit: () -> Unit) {
    val userRepo = remember { UserRepository() }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    AppTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    elevation = 10.dp,
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.width(420.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("System Biblioteczny", style = MaterialTheme.typography.h5)
                        Spacer(modifier = Modifier.height(24.dp))

                        if (error.isNotEmpty()) {
                            Text(
                                text = error,
                                color = MaterialTheme.colors.error,
                                style = MaterialTheme.typography.caption,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Użytkownik") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Ikona użytkownika") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Hasło") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Ikona hasła") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = onExit
                            ) { Text("Wyjdź") }

                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = {
                                val user = userRepo.verifyUser(username, password)
                                if (user != null) {
                                    onLoginSuccess(user)
                                } else {
                                    error = "Nieprawidłowe dane logowania."
                                }
                            }) {
                                Text("Zaloguj")
                            }
                        }
                    }
                }
            }
        }
    }
}
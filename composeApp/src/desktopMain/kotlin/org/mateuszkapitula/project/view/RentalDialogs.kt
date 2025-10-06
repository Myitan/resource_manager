package org.mateuszkapitula.project.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import org.mateuszkapitula.project.model.Client

@Composable
fun RentBookDialog(
    clients: List<Client>,
    onConfirm: (clientId: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedClient by remember { mutableStateOf<Client?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Wybierz klienta") },
        text = {
            Box {
                OutlinedButton(onClick = { expanded = true }) {
                    Text(selectedClient?.let { "${it.firstName} ${it.lastName}" } ?: "Wybierz...")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    clients.forEach { client ->
                        DropdownMenuItem(onClick = {
                            selectedClient = client
                            expanded = false
                        }) {
                            Text("${client.firstName} ${client.lastName} (${client.email})")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedClient?.let { onConfirm(it.id) }
                },
                enabled = selectedClient != null
            ) {
                Text("Wypożycz")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}
package org.mateuszkapitula.project.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.mateuszkapitula.project.model.Book
import org.mateuszkapitula.project.model.Client
import org.mateuszkapitula.project.repository.BookRepository
import org.mateuszkapitula.project.repository.ClientRepository
import org.mateuszkapitula.project.repository.RentalHistoryEntry
import org.mateuszkapitula.project.repository.RentalRepository
import org.mateuszkapitula.project.repository.User
import org.mateuszkapitula.project.service.ExportService
import java.io.File

enum class Screen(val title: String, val icon: ImageVector) {
    Books("Książki", Icons.AutoMirrored.Filled.MenuBook),
    Clients("Klienci", Icons.Default.People),
    AddBook("Dodaj książkę", Icons.Default.AddCircle),
    Export("Eksport", Icons.Default.Download)
}
@Composable
fun AppView(user: User) {
    val scaffoldState = rememberScaffoldState()

    val bookRepo = remember { BookRepository() }
    val clientRepo = remember { ClientRepository() }
    val rentalRepo = remember { RentalRepository() }
    val exportService = remember { ExportService() }

    var bookList by remember { mutableStateOf(bookRepo.findBooks()) }
    var clientList by remember { mutableStateOf(clientRepo.findClients()) }

    var bookToRent by remember { mutableStateOf<Book?>(null) }
    var bookToReturn by remember { mutableStateOf<Book?>(null) }
    var bookToEdit by remember { mutableStateOf<Book?>(null) }

    var selectedScreen by remember { mutableStateOf(Screen.Books) }

    AppTheme {
        Scaffold(scaffoldState = scaffoldState) {
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationPanel(
                    user = user,
                    selectedScreen = selectedScreen,
                    onScreenSelected = { selectedScreen = it }
                )
                Box(
                    modifier = Modifier.weight(1f)
                        .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 16.dp)
                ) {
                    when (selectedScreen) {
                        Screen.Books -> BookManagementScreen(
                            user = user,
                            bookRepo = bookRepo,
                            bookList = bookList,
                            onBookListChange = { bookList = it },
                            onRentBook = { bookToRent = it },
                            onReturnBook = { bookToReturn = it },
                            onEditBook = { bookToEdit = it }
                        )

                        Screen.Clients -> ClientManagementScreen(
                            clientRepo = clientRepo,
                            rentalRepo = rentalRepo,
                            clientList = clientList,
                            onClientListChange = { clientList = it }
                        )

                        Screen.Export -> ExportScreen(
                            bookList = bookList,
                            exportService = exportService,
                            scaffoldState = scaffoldState
                        )
                        Screen.AddBook -> AddBookScreen(
                            bookRepo = bookRepo,
                            onBookAdded = {
                                bookList = bookRepo.findBooks()
                            }
                        )
                    }
                }
            }
            bookToRent?.let { book ->
                RentBookDialog(
                    clients = clientList,
                    onConfirm = { clientId ->
                        rentalRepo.rentBook(book.id, clientId)
                        bookList = bookRepo.findBooks()
                        bookToRent = null
                    },
                    onDismiss = { bookToRent = null }
                )
            }
            bookToReturn?.let { book ->
                AlertDialog(
                    onDismissRequest = { bookToReturn = null },
                    title = { Text("Potwierdzenie zwrotu") },
                    text = { Text("Czy na pewno chcesz zwrócić książkę \"${book.title}\"?") },
                    confirmButton = {
                        Button(onClick = {
                            rentalRepo.returnBook(book.id)
                            bookList = bookRepo.findBooks()
                            bookToReturn = null
                        }) { Text("Zwróć") }
                    },
                    dismissButton = { Button(onClick = { bookToReturn = null }) { Text("Anuluj") } }
                )
            }
            bookToEdit?.let { book ->
                EditBookDialog(
                    book = book,
                    onConfirm = { updatedBook ->
                        bookRepo.updateBook(updatedBook)
                        bookList = bookRepo.findBooks()
                        bookToEdit = null
                    },
                    onDismiss = { bookToEdit = null }
                )
            }
        }
    }
}

@Composable
fun AddBookScreen(bookRepo: BookRepository, onBookAdded: () -> Unit) {
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()

    var newBookTitle by remember { mutableStateOf("") }
    var newBookAuthor by remember { mutableStateOf("") }

    Column(modifier = Modifier.widthIn(max = 500.dp)) {
        Text("Dodaj Nową Książkę", style = MaterialTheme.typography.h4)
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = newBookTitle,
            onValueChange = { newBookTitle = it },
            label = { Text("Tytuł") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = newBookAuthor,
            onValueChange = { newBookAuthor = it },
            label = { Text("Autor") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                if (newBookTitle.isNotBlank() && newBookAuthor.isNotBlank()) {
                    bookRepo.addBook(newBookTitle, newBookAuthor)
                    newBookTitle = ""
                    newBookAuthor = ""
                    scope.launch {
                        scaffoldState.snackbarHostState.showSnackbar("Książka została pomyślnie dodana!")
                    }
                    onBookAdded()
                } else {
                    scope.launch {
                        scaffoldState.snackbarHostState.showSnackbar("Tytuł i autor nie mogą być puste.")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Dodaj do katalogu")
        }
    }
}

@Composable
fun NavigationPanel(
    user: User,
    selectedScreen: Screen,
    onScreenSelected: (Screen) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(250.dp)
            .background(MaterialTheme.colors.surface)
            .padding(16.dp)
    ) {
        Text("System Biblioteczny", style = MaterialTheme.typography.h6)
        Text("Witaj, ${user.username}", style = MaterialTheme.typography.subtitle2)
        Spacer(modifier = Modifier.height(32.dp))

        Screen.entries.forEach { screen ->
            if(screen == Screen.AddBook && user.role != "admin"){

            }else{
                NavigationButton(
                    label = screen.title,
                    icon = screen.icon,
                    isSelected = screen == selectedScreen,
                    onClick = { onScreenSelected(screen) }
                )
            }
        }
    }
}
@Composable
fun NavigationButton(label: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) MaterialTheme.colors.primary.copy(alpha = 0.1f) else Color.Transparent
    val contentColor = if (isSelected) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface.copy(alpha = 0.7f)

    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.textButtonColors(backgroundColor = backgroundColor, contentColor = contentColor)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = label)
            Spacer(modifier = Modifier.width(16.dp))
            Text(label, style = MaterialTheme.typography.button)
        }
    }
}
@Composable
fun BookManagementScreen(
    user: User,
    bookRepo: BookRepository,
    bookList: List<Book>,
    onBookListChange: (List<org.mateuszkapitula.project.model.Book>) -> Unit,
    onRentBook: (org.mateuszkapitula.project.model.Book) -> Unit,
    onReturnBook: (org.mateuszkapitula.project.model.Book) -> Unit,
    onEditBook: (org.mateuszkapitula.project.model.Book) -> Unit
) {
    var filterTitle by remember { mutableStateOf("") }
    var filterAuthor by remember { mutableStateOf("") }
    var filterAvailableOnly by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()

    Column {
        Text("Zarządzanie Książkami", style = MaterialTheme.typography.h4)
        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = filterTitle,
                onValueChange = {
                    filterTitle = it
                    onBookListChange(bookRepo.findBooks(filterTitle, filterAuthor, filterAvailableOnly))
                },
                label = { Text("Tytuł") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = filterAuthor,
                onValueChange = {
                    filterAuthor = it
                    onBookListChange(bookRepo.findBooks(filterTitle, filterAuthor, filterAvailableOnly))
                },
                label = { Text("Autor") },
                modifier = Modifier.weight(1f)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically ,modifier = Modifier.padding(top = 8.dp)) {
                Checkbox(
                    checked = filterAvailableOnly,
                    onCheckedChange = {
                        filterAvailableOnly = it
                        onBookListChange(bookRepo.findBooks(filterTitle, filterAuthor, filterAvailableOnly))
                    }
                )
                Text("Tylko dostępne")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(bookList) { book ->
                BookCard(
                    book = book,
                    user = user,
                    onRent = { onRentBook(book) },
                    onReturn = { onReturnBook(book) },
                    onEdit = { onEditBook(book) },
                    onDelete = {
                        val success = bookRepo.deleteBook(book.id)
                        if (success) {
                            onBookListChange(bookRepo.findBooks(filterTitle, filterAuthor, filterAvailableOnly))
                        } else {
                            scope.launch { scaffoldState.snackbarHostState.showSnackbar("Nie można usunąć książki, jest aktualnie wypożyczona.") }
                        }
                    }
                )
            }
        }
    }
}
@Composable
fun ClientManagementScreen(
    clientRepo: ClientRepository,
    rentalRepo: RentalRepository,
    clientList: List<Client>,
    onClientListChange: (List<Client>) -> Unit
) {
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()

    var newClientFirstName by remember { mutableStateOf("") }
    var newClientLastName by remember { mutableStateOf("") }
    var newClientEmail by remember { mutableStateOf("") }

    var selectedClientForHistory by remember { mutableStateOf<Client?>(null) }
    var clientHistory by remember { mutableStateOf<List<RentalHistoryEntry>>(emptyList()) }

    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Dodaj Klienta", style = MaterialTheme.typography.h5)
            Spacer(modifier = Modifier.height(16.dp))
            AddClientForm(
                firstName = newClientFirstName,
                lastName = newClientLastName,
                email = newClientEmail,
                onFirstNameChange = { newClientFirstName = it },
                onLastNameChange = { newClientLastName = it },
                onEmailChange = { newClientEmail = it },
                onRegisterClick = {
                    if (newClientFirstName.isBlank() || newClientLastName.isBlank() || newClientEmail.isBlank() || !newClientEmail.contains("@")) {
                        scope.launch { scaffoldState.snackbarHostState.showSnackbar("Wypełnij poprawnie wszystkie pola.")}
                    } else {
                        clientRepo.addClient(newClientFirstName, newClientLastName, newClientEmail)
                        onClientListChange(clientRepo.findClients())
                        newClientFirstName = ""
                        newClientLastName = ""
                        newClientEmail = ""
                        scope.launch { scaffoldState.snackbarHostState.showSnackbar("Klient został dodany!")}
                    }
                }
            )
        }
        Column(modifier = Modifier.weight(1.5f)) {
            Text("Historia Klienta", style = MaterialTheme.typography.h5)
            Spacer(modifier = Modifier.height(16.dp))
            ClientHistoryView(
                clients = clientList,
                selectedClient = selectedClientForHistory,
                history = clientHistory,
                onClientSelected = { client ->
                    selectedClientForHistory = client
                    clientHistory = rentalRepo.getRentalHistoryForClient(client.id)
                }
            )
        }
    }
}
@Composable
fun ExportScreen(bookList: List<org.mateuszkapitula.project.model.Book>, exportService: ExportService, scaffoldState: ScaffoldState) {
    val scope = rememberCoroutineScope()
    Column {
        Text("Eksport Danych", style = MaterialTheme.typography.h4)
        Spacer(modifier = Modifier.height(16.dp))

        Card(elevation = 4.dp, shape = MaterialTheme.shapes.medium) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Eksport listy książek", style = MaterialTheme.typography.h6)
                Text("Aktualnie na liście znajduje się ${bookList.size} książek.", style = MaterialTheme.typography.body2)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        val exportsDir = File("exports")
                        if (!exportsDir.exists()) exportsDir.mkdirs()
                        val path = "exports/ksiazki_export_${System.currentTimeMillis()}.csv"
                        exportService.exportBooksToCsv(bookList, path)
                        scope.launch { scaffoldState.snackbarHostState.showSnackbar("Wyeksportowano do $path") }
                    }) {
                        Icon(Icons.Default.TableView, contentDescription = "CSV")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Eksportuj do CSV")
                    }

                    Button(onClick = {
                        val exportsDir = File("exports")
                        if (!exportsDir.exists()) exportsDir.mkdirs()
                        val path = "exports/ksiazki_export_${System.currentTimeMillis()}.pdf"
                        exportService.exportBooksToPdf(bookList, path)
                        scope.launch { scaffoldState.snackbarHostState.showSnackbar("Wyeksportowano do $path") }
                    }) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Eksportuj do PDF")
                    }
                }
            }
        }
    }
}
@Composable
fun BookCard(
    book: org.mateuszkapitula.project.model.Book,
    user: User,
    onRent: () -> Unit,
    onReturn: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(elevation = 4.dp, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(book.title, style = MaterialTheme.typography.h6)
                Text(book.author, style = MaterialTheme.typography.body2)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val availabilityColor = if (book.isAvailable) MaterialTheme.colors.secondary else Color(0xFFFF9500)
                    val availabilityText = if (book.isAvailable) "Dostępna" else "Wypożyczona"
                    Box(modifier = Modifier.size(10.dp).background(availabilityColor, shape = MaterialTheme.shapes.small))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(availabilityText, style = MaterialTheme.typography.caption)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (book.isAvailable) {
                    Button(onClick = onRent) { Text("Wypożycz") }
                } else {
                    OutlinedButton(onClick = onReturn) { Text("Zwróć") }
                }

                if (user.role == "admin") {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edytuj")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Usuń", tint = MaterialTheme.colors.error)
                    }
                }
            }
        }
    }
}


@Composable
fun AddClientForm(
    firstName: String,
    lastName: String,
    email: String,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onRegisterClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = firstName,
            onValueChange = onFirstNameChange,
            label = { Text("Imię") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.PersonOutline, contentDescription = "Imię") }
        )
        OutlinedTextField(
            value = lastName,
            onValueChange = onLastNameChange,
            label = { Text("Nazwisko") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.PersonOutline, contentDescription = "Nazwisko") }
        )
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onRegisterClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.PersonAdd, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Zarejestruj klienta")
        }
    }
}


@Composable
fun ClientHistoryView(
    clients: List<Client>,
    selectedClient: Client?,
    history: List<RentalHistoryEntry>,
    onClientSelected: (Client) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box {
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                Text(selectedClient?.let { "${it.firstName} ${it.lastName}" } ?: "Wybierz klienta...")
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Rozwiń")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.5f)
            ) {
                clients.forEach { client ->
                    DropdownMenuItem(onClick = {
                        onClientSelected(client)
                        expanded = false
                    }) {
                        Text("${client.firstName} ${client.lastName}")
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Divider()

        if (selectedClient == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Wybierz klienta, aby zobaczyć historię.", style = MaterialTheme.typography.subtitle1)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(history) { entry ->
                    Card(modifier = Modifier.fillMaxWidth(), elevation = 2.dp, shape = MaterialTheme.shapes.medium) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(entry.bookTitle, style = MaterialTheme.typography.subtitle1)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Wypożyczono: ${entry.rentalDate}", style = MaterialTheme.typography.caption)
                            Text("Planowany zwrot: ${entry.dueDate}", style = MaterialTheme.typography.caption)
                            Text(
                                text = entry.returnDate?.let { "Zwrócono: $it" } ?: "Nie zwrócono",
                                color = if (entry.returnDate == null) MaterialTheme.colors.error else MaterialTheme.colors.secondary,
                                style = MaterialTheme.typography.caption,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditBookDialog(
    book: org.mateuszkapitula.project.model.Book,
    onConfirm: (org.mateuszkapitula.project.model.Book) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(book.title) }
    var author by remember { mutableStateOf(book.author) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edytuj książkę") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tytuł") }
                )
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Autor") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedBook = book.copy(title = title, author = author)
                    onConfirm(updatedBook)
                },
                enabled = title.isNotBlank() && author.isNotBlank()
            ) {
                Text("Zapisz")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}

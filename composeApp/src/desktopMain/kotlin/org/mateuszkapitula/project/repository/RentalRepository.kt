package org.mateuszkapitula.project.repository

import org.mateuszkapitula.project.service.DatabaseManager
import java.sql.Date
import java.time.LocalDate

data class RentalHistoryEntry(
    val bookTitle: String,
    val rentalDate: Date,
    val dueDate: Date,
    val returnDate: Date?
)

class RentalRepository {
    private val connection = DatabaseManager.connection

    fun rentBook(bookId: Int, clientId: Int): Boolean {
        val checkSql = "SELECT is_available FROM books WHERE id = ?"
        var isAvailable = false
        connection.prepareStatement(checkSql).use { stmt ->
            stmt.setInt(1, bookId)
            val rs = stmt.executeQuery()
            if (rs.next()) {
                isAvailable = rs.getBoolean("is_available")
            }
        }

        if (!isAvailable) {
            println("Błąd: Książka o ID $bookId jest już wypożyczona.")
            return false
        }

        connection.autoCommit = false
        try {
            val updateBookSql = "UPDATE books SET is_available = FALSE WHERE id = ?"
            connection.prepareStatement(updateBookSql).use { it.setInt(1, bookId); it.executeUpdate() }

            val addRentalSql = "INSERT INTO rentals (book_id, client_id, rental_date, due_date) VALUES (?, ?, ?, ?)"
            connection.prepareStatement(addRentalSql).use { stmt ->
                stmt.setInt(1, bookId)
                stmt.setInt(2, clientId)
                stmt.setDate(3, Date.valueOf(LocalDate.now()))
                stmt.setDate(4, Date.valueOf(LocalDate.now().plusWeeks(2)))
                stmt.executeUpdate()
            }

            connection.commit()
            return true
        } catch (e: Exception) {
            connection.rollback()
            e.printStackTrace()
            return false
        } finally {
            connection.autoCommit = true
        }
    }
    fun returnBook(bookId: Int): Boolean {
        val checkSql = "SELECT id FROM books WHERE id = ? AND is_available = FALSE"
        connection.prepareStatement(checkSql).use { statement ->
            statement.setInt(1, bookId)
            if (!statement.executeQuery().next()) {
                println("Błąd: Książka o ID $bookId nie jest aktualnie wypożyczona.")
                return false
            }
        }

        try {
            connection.autoCommit = false

            val updateBookSql = "UPDATE books SET is_available = TRUE WHERE id = ?"
            connection.prepareStatement(updateBookSql).use {
                it.setInt(1, bookId)
                it.executeUpdate()
            }

            val updateRentalSql = "UPDATE rentals SET return_date = ? WHERE book_id = ? AND return_date IS NULL"
            connection.prepareStatement(updateRentalSql).use { statement ->
                statement.setDate(1, Date.valueOf(LocalDate.now()))
                statement.setInt(2, bookId)
                statement.executeUpdate()
            }

            connection.commit()
            println("Książka o ID $bookId została pomyślnie zwrócona.")
            return true

        } catch (e: Exception) {
            connection.rollback()
            e.printStackTrace()
            return false
        } finally {
            connection.autoCommit = true
        }
    }
    fun getRentalHistoryForClient(clientId: Int): List<RentalHistoryEntry> {
        val history = mutableListOf<RentalHistoryEntry>()
        val sql = """
        SELECT b.title, r.rental_date, r.due_date, r.return_date
        FROM rentals r
        JOIN books b ON r.book_id = b.id
        WHERE r.client_id = ?
        ORDER BY r.rental_date DESC
    """
        connection.prepareStatement(sql).use { statement ->
            statement.setInt(1, clientId)
            val rs = statement.executeQuery()
            while (rs.next()) {
                history.add(
                    RentalHistoryEntry(
                        bookTitle = rs.getString("title"),
                        rentalDate = rs.getDate("rental_date"),
                        dueDate = rs.getDate("due_date"),
                        returnDate = rs.getDate("return_date")
                    )
                )
            }
        }
        return history
    }

}
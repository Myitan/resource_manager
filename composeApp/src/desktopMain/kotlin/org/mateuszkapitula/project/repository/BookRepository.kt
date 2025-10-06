package org.mateuszkapitula.project.repository

import org.mateuszkapitula.project.service.DatabaseManager
import org.mateuszkapitula.project.model.Book
import java.sql.ResultSet

class BookRepository {
    private val connection = DatabaseManager.connection

    fun addBook(title: String, author: String) {
        val sql = "INSERT INTO books (title, author) VALUES (?, ?)"
        connection.prepareStatement(sql).use { stmt ->
            stmt.setString(1, title)
            stmt.setString(2, author)
            stmt.executeUpdate()
        }
    }

    fun findBooks(title: String = "", author: String = "", availableOnly: Boolean = false): List<Book> {
        val books = mutableListOf<Book>()
        var sql = "SELECT * FROM books WHERE title LIKE ? AND author LIKE ?"
        if (availableOnly) {
            sql += " AND is_available = TRUE"
        }

        connection.prepareStatement(sql).use { stmt ->
            stmt.setString(1, "%$title%")
            stmt.setString(2, "%$author%")
            val rs = stmt.executeQuery()
            while (rs.next()) {
                books.add(mapRowToBook(rs))
            }
        }
        return books
    }

    private fun mapRowToBook(rs: ResultSet): Book {
        return Book(
            id = rs.getInt("id"),
            title = rs.getString("title"),
            author = rs.getString("author"),
            isAvailable = rs.getBoolean("is_available"),
        )
    }
    fun updateBook(book: Book) {
        val sql = "UPDATE books SET title = ?, author = ?, is_available = ? WHERE id = ?"
        connection.prepareStatement(sql).use { statement ->
            statement.setString(1, book.title)
            statement.setString(2, book.author)
            statement.setBoolean(3, book.isAvailable)
            statement.setInt(4, book.id)
            statement.executeUpdate()
        }
    }
    fun deleteBook(bookId: Int): Boolean {
        val checkSql = "SELECT COUNT(*) FROM rentals WHERE book_id = ? AND return_date IS NULL"
        var activeRentalCount = 0
        connection.prepareStatement(checkSql).use { statement ->
            statement.setInt(1, bookId)
            val rs = statement.executeQuery()
            if (rs.next()) {
                activeRentalCount = rs.getInt(1)
            }
        }
        if (activeRentalCount > 0) {
            println("Błąd: Nie można usunąć książki (ID: $bookId), ponieważ jest aktualnie wypożyczona.")
            return false
        }
        val deleteRentalsSql = "DELETE FROM rentals WHERE book_id = ?"
        connection.prepareStatement(deleteRentalsSql).use { statement ->
            statement.setInt(1, bookId)
            statement.executeUpdate()
        }
        val deleteBookSql = "DELETE FROM books WHERE id = ?"
        connection.prepareStatement(deleteBookSql).use { statement ->
            statement.setInt(1, bookId)
            statement.executeUpdate()
        }
        return true
    }

}
package org.mateuszkapitula.project.service

import java.sql.Connection
import java.sql.DriverManager

object DatabaseManager {
    private const val URL = "jdbc:mysql://localhost:3306/library_db"
    private const val USER = "root"
    private const val PASSWORD = ""

    lateinit var connection: Connection
        private set

    fun connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver")
            connection = DriverManager.getConnection(URL, USER, PASSWORD)
            println("Połączono z bazą danych!")
        } catch (e: Exception) {
            e.printStackTrace()
            throw IllegalStateException("Nie można połączyć z bazą danych", e)
        }
    }
}
package org.mateuszkapitula.project.repository

import org.mateuszkapitula.project.service.DatabaseManager

data class User(val username: String, val role: String)

class UserRepository {
    private val connection = DatabaseManager.connection

    fun verifyUser(username: String, passwordAttempt: String): User? {
        val sql = "SELECT password_hash, role FROM users WHERE username = ?"
        connection.prepareStatement(sql).use { statement ->
            statement.setString(1, username)
            val rs = statement.executeQuery()
            if (rs.next()) {
                val storedHash = rs.getString("password_hash")
                val role = rs.getString("role")
                val expectedHash = (if (role == "admin") "admin" else "pracownik") + "_password"

                if (storedHash == expectedHash && storedHash == (passwordAttempt)) {
                    return User(username, role)
                }
            }
        }
        return null
    }
}
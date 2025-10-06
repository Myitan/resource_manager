package org.mateuszkapitula.project.repository

import org.mateuszkapitula.project.service.DatabaseManager
import org.mateuszkapitula.project.model.Client
import java.sql.ResultSet

class ClientRepository {
    private val connection = DatabaseManager.connection

    fun addClient(firstName: String, lastName: String, email: String) {
        val sql = "INSERT INTO clients (first_name, last_name, email) VALUES (?, ?, ?)"
        connection.prepareStatement(sql).use { statement ->
            statement.setString(1, firstName)
            statement.setString(2, lastName)
            statement.setString(3, email)
            statement.executeUpdate()
        }
    }

    fun updateClient(client: Client) {
        val sql = "UPDATE clients SET first_name = ?, last_name = ?, email = ? WHERE id = ?"
        connection.prepareStatement(sql).use { statement ->
            statement.setString(1, client.firstName)
            statement.setString(2, client.lastName)
            statement.setString(3, client.email)
            statement.setInt(4, client.id)
            statement.executeUpdate()
        }
    }

    fun deleteClient(clientId: Int) {
        val sql = "DELETE FROM clients WHERE id = ?"
        connection.prepareStatement(sql).use { statement ->
            statement.setInt(1, clientId)
            statement.executeUpdate()
        }
    }

    fun findClients(query: String = ""): List<Client> {
        val clients = mutableListOf<Client>()
        val sql = "SELECT * FROM clients WHERE first_name LIKE ? OR last_name LIKE ? OR email LIKE ?"
        connection.prepareStatement(sql).use { statement ->
            statement.setString(1, "%$query%")
            statement.setString(2, "%$query%")
            statement.setString(3, "%$query%")
            val resultSet = statement.executeQuery()
            while (resultSet.next()) {
                clients.add(mapRowToClient(resultSet))
            }
        }
        return clients
    }

    private fun mapRowToClient(rs: ResultSet): Client {
        return Client(
            id = rs.getInt("id"),
            firstName = rs.getString("first_name"),
            lastName = rs.getString("last_name"),
            email = rs.getString("email")
        )
    }
}

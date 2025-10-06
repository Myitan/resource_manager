package org.mateuszkapitula.project.model

import java.time.LocalDate

data class Rental(
    val id: Int,
    val bookId: Int,
    val clientId: Int,
    val rentalDate: LocalDate,
    val dueDate: LocalDate,
    val returnDate: LocalDate? = null
)
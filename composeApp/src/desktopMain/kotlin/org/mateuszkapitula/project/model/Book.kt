package org.mateuszkapitula.project.model

data class Book(
    val id: Int,
    val title: String,
    val author: String,
    val isAvailable: Boolean
)
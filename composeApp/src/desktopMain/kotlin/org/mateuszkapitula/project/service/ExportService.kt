package org.mateuszkapitula.project.service

import com.opencsv.CSVWriter
import java.io.FileWriter
import java.io.IOException
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import org.mateuszkapitula.project.model.Book

class ExportService {

    fun exportBooksToCsv(books: List<Book>, path: String) {
        try {
            CSVWriter(FileWriter(path)).use { writer ->
                val header = arrayOf("ID", "Tytul", "Autor", "Dostepnosc")
                writer.writeNext(header)
                books.forEach { book ->
                    val data = arrayOf(
                        book.id.toString(),
                        book.title,
                        book.author,
                        if (book.isAvailable) "Dostepna" else "Wypozyczona"
                    )
                    writer.writeNext(data)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun exportBooksToPdf(books: List<Book>, path: String) {
        val writer = PdfWriter(path)
        val pdf = PdfDocument(writer)
        val document = Document(pdf)

        document.add(Paragraph("Raport Ksiazek").setBold().setFontSize(18f))

        val table = Table(floatArrayOf(1f, 4f, 4f, 2f))
        table.addHeaderCell("ID")
        table.addHeaderCell("Tytul")
        table.addHeaderCell("Autor")
        table.addHeaderCell("Dostepnosc")

        books.forEach { book ->
            table.addCell(book.id.toString())
            table.addCell(book.title)
            table.addCell(book.author)
            table.addCell(if (book.isAvailable) "Dostepna" else "Wypozyczona")
        }
        document.add(table)
        document.close()
    }
}
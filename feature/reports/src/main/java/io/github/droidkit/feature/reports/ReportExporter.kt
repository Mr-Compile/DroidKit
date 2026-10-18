package io.github.droidkit.feature.reports

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import java.io.File

/** Writes the canonical report text to .txt or .pdf under `cacheDir/reports/`. */
object ReportExporter {

    fun exportText(context: Context, sessionId: String, report: String): File {
        val file = File(reportsDir(context), "droidkit-report-${sessionId.take(8)}.txt")
        file.writeText(report)
        return file
    }

    /** Renders the same text lines into a paginated PDF (no external deps). */
    fun exportPdf(context: Context, sessionId: String, report: String): File {
        val pageWidth = 595 // A4 at 72dpi
        val pageHeight = 842
        val margin = 48
        val lineHeight = 16
        val linesPerPage = (pageHeight - margin * 2) / lineHeight

        val doc = PdfDocument()
        val paint = Paint().apply {
            textSize = 10f
            isAntiAlias = true
        }
        val lines = report.lines()
        var pageNum = 0
        var index = 0
        while (index < lines.size) {
            pageNum++
            val page = doc.startPage(
                PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create(),
            )
            drawPage(page.canvas, lines, index, linesPerPage, margin, lineHeight, paint)
            index += linesPerPage
            doc.finishPage(page)
        }
        val file = File(reportsDir(context), "droidkit-report-${sessionId.take(8)}.pdf")
        file.outputStream().use { doc.writeTo(it) }
        doc.close()
        return file
    }

    private fun drawPage(
        canvas: Canvas,
        lines: List<String>,
        start: Int,
        count: Int,
        margin: Int,
        lineHeight: Int,
        paint: Paint,
    ) {
        var y = margin.toFloat()
        lines.subList(start, minOf(start + count, lines.size)).forEach { line ->
            canvas.drawText(line.take(110), margin.toFloat(), y, paint)
            y += lineHeight
        }
    }

    private fun reportsDir(context: Context): File =
        File(context.cacheDir, "reports").apply { mkdirs() }
}

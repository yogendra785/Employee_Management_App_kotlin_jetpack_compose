package com.example.neutron.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import com.example.neutron.domain.model.Employee
import java.io.File
import java.io.OutputStream

object EmployeePdfGenerator {

    fun generateRegistrationPdf(context: Context, employee: Employee, outputStream: OutputStream) {
        val pdfDocument = PdfDocument()
        val titlePaint = Paint().apply {
            textSize = 22f
            isFakeBoldText = true
            color = Color.BLACK
        }
        val paint = Paint().apply {
            textSize = 14f
            color = Color.BLACK
        }
        val labelPaint = Paint().apply {
            textSize = 14f
            isFakeBoldText = true
            color = Color.BLACK
        }

        // Standard A4 Size
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        var yPos = 60f

        // --- Header ---
        canvas.drawText("NEUTRON MANAGEMENT SYSTEM", 140f, yPos, titlePaint)
        yPos += 30f
        canvas.drawText("Official Employee Registration Form", 170f, yPos, paint)

        // --- 🔹 Passport Photo Logic ---
        val photoX = 450f
        val photoY = 100f
        val photoWidth = 100f // Approx passport width on A4
        val photoHeight = 120f // Approx passport height on A4

        // Draw a placeholder box for the photo
        val strokePaint = Paint().apply {
            color = Color.LTGRAY
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRect(photoX, photoY, photoX + photoWidth, photoY + photoHeight, strokePaint)

        // If imagePath exists, decode and draw it
        employee.imagePath?.let { path ->
            val imgFile = File(path)
            if (imgFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(path)
                bitmap?.let {
                    val destRect = Rect(photoX.toInt(), photoY.toInt(), (photoX + photoWidth).toInt(), (photoY + photoHeight).toInt())
                    canvas.drawBitmap(it, null, destRect, null)
                }
            }
        } ?: run {
            paint.textSize = 10f
            canvas.drawText("PHOTO", photoX + 30f, photoY + 65f, paint)
            paint.textSize = 14f
        }

        // --- Content Sections ---
        yPos = 130f
        canvas.drawText("EMPLOYEE ID: ${employee.employeeId}", 50f, yPos, labelPaint)

        yPos += 50f
        canvas.drawText("PERSONAL DETAILS", 50f, yPos, labelPaint)
        yPos += 25f
        canvas.drawText("Full Name: ${employee.name}", 50f, yPos, paint)
        yPos += 25f
        canvas.drawText("Parent's Name: ${employee.parentName}", 50f, yPos, paint)
        yPos += 25f
        canvas.drawText("Address: ${employee.address}", 50f, yPos, paint)

        yPos += 50f
        canvas.drawText("IDENTITY & CONTACT", 50f, yPos, labelPaint)
        yPos += 25f
        canvas.drawText("Aadhar Number: ${employee.aadharNumber}", 50f, yPos, paint)
        yPos += 25f
        canvas.drawText("PAN Number: ${employee.panNumber}", 50f, yPos, paint)
        yPos += 25f
        canvas.drawText("Contact: ${employee.contactNumber}", 50f, yPos, paint)
        yPos += 25f
        canvas.drawText("Emergency Contact: ${employee.emergencyContact}", 50f, yPos, paint)

        yPos += 50f
        canvas.drawText("PROFESSIONAL INFORMATION", 50f, yPos, labelPaint)
        yPos += 25f
        canvas.drawText("Department: ${employee.department}", 50f, yPos, paint)
        yPos += 25f
        canvas.drawText("Role: ${employee.role}", 50f, yPos, paint)
        yPos += 25f
        canvas.drawText("Joining Date: ${formatDate(employee.createdAt)}", 50f, yPos, paint)

        pdfDocument.finishPage(page)

        try {
            pdfDocument.writeTo(outputStream)
            outputStream.close()
            Toast.makeText(context, "Registration PDF with Photo saved!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error saving PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }

    // Helper to format date inside PDF
    private fun formatDate(millis: Long): String {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(millis))
    }
}
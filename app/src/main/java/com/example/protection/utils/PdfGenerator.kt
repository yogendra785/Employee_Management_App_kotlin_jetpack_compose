package com.example.protection.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.example.protection.domain.model.SalaryRecord
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class PdfGenerator(private val context: Context) {

    fun generateSalarySlip(record: SalaryRecord, employeeName: String) {
        // 1. Create PDF Document (Standard A4 Size)
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()

        // 2. Define Colors & Styles
        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 24f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        val labelPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 14f
            isFakeBoldText = true
        }
        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 14f
        }
        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 2f
        }

        // 3. Draw Content
        canvas.drawText("NEUTRON SALARY SLIP", 297.5f, 60f, titlePaint)
        canvas.drawLine(50f, 80f, 545f, 80f, linePaint)

        var yPos = 120f
        val leftX = 50f
        val rightX = 350f

        // Employee Details
        canvas.drawText("Employee Name:", leftX, yPos, labelPaint)
        canvas.drawText(employeeName, rightX, yPos, textPaint)
        yPos += 30f
        canvas.drawText("Month:", leftX, yPos, labelPaint)
        canvas.drawText(record.month, rightX, yPos, textPaint)
        yPos += 30f

        canvas.drawLine(50f, yPos, 545f, yPos, linePaint)
        yPos += 40f

        // Earnings
        canvas.drawText("EARNINGS", leftX, yPos, labelPaint)
        yPos += 30f
        canvas.drawText("Base Salary", leftX, yPos, textPaint)
        canvas.drawText("₹${record.baseSalary}", rightX, yPos, textPaint)
        yPos += 40f

        // Deductions
        canvas.drawText("DEDUCTIONS", leftX, yPos, labelPaint)
        yPos += 30f
        paint.color = Color.RED
        val penalty = record.absentDays * record.perDayDeduction
        canvas.drawText("Absence Penalty (${record.absentDays} days)", leftX, yPos, paint)
        canvas.drawText("-₹$penalty", rightX, yPos, paint)
        yPos += 30f
        canvas.drawText("Advance Taken", leftX, yPos, paint)
        canvas.drawText("-₹${record.advancePaid}", rightX, yPos, paint)
        yPos += 40f

        // Net Pay
        canvas.drawLine(50f, yPos, 545f, yPos, linePaint)
        yPos += 40f
        val netPaint = Paint().apply {
            color = Color.rgb(0, 100, 0)
            textSize = 18f
            isFakeBoldText = true
        }
        canvas.drawText("NET PAYABLE:", leftX, yPos, netPaint)
        canvas.drawText("₹${record.netPayable}", rightX, yPos, netPaint)

        pdfDocument.finishPage(page)

        // 4. Save using MediaStore (The Fix)
        savePdfToStorage(pdfDocument, employeeName, record.month)
    }

    private fun savePdfToStorage(pdfDocument: PdfDocument, employeeName: String, month: String) {
        val fileName = "PaySlip_${employeeName.replace(" ", "_")}_${month.replace(" ", "_")}.pdf"

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // 🔹 Modern Android (API 29+): Use MediaStore
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/NeutronPayslips")
                }

                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

                uri?.let {
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        pdfDocument.writeTo(outputStream)
                    }
                    Toast.makeText(context, "Saved to Downloads/NeutronPayslips", Toast.LENGTH_LONG).show()
                } ?: run {
                    Toast.makeText(context, "Failed to create file path", Toast.LENGTH_SHORT).show()
                }

            } else {
                // 🔹 Older Android (Legacy): Use File API
                val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(directory, fileName)
                pdfDocument.writeTo(FileOutputStream(file))
                Toast.makeText(context, "Saved to: ${file.absolutePath}", Toast.LENGTH_LONG).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Error saving PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }
}
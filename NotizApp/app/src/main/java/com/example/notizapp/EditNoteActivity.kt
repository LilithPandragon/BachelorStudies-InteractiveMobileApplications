package com.example.notizapp

import android.annotation.SuppressLint
import android.content.ContentValues
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.notizapp.Database.DbManager

class EditNoteActivity : AppCompatActivity() {
    private var noteID: Int = -1
    private lateinit var noteName: String
    private lateinit var noteDesc: String
    private lateinit var editTextNoteName: EditText
    private lateinit var editTextNoteDesc: EditText
    private lateinit var btnSave: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_note)

        editTextNoteName = findViewById(R.id.editTextNoteName)
        editTextNoteDesc = findViewById(R.id.editTextNoteDesc)
        btnSave = findViewById(R.id.btnSave)


        noteID = intent.getIntExtra("noteID", -1)
        noteName = intent.getStringExtra("noteName")!!
        noteDesc = intent.getStringExtra("noteDesc")!!


        editTextNoteName.setText(noteName)
        editTextNoteDesc.setText(noteDesc)

        btnSave.setOnClickListener {
            saveNote()
        }
    }

    private fun saveNote() {
        val updatedNoteName = editTextNoteName.text.toString()
        val updatedNoteDesc = editTextNoteDesc.text.toString()

        if (updatedNoteName.isNotEmpty() && updatedNoteDesc.isNotEmpty()) {
            val dbManager = DbManager(this)
            val contentValues = ContentValues()
            contentValues.put(dbManager.colTitle, updatedNoteName)
            contentValues.put(dbManager.colDes, updatedNoteDesc)

            val selectionArgs = arrayOf(noteID.toString())
            val affectedRows = dbManager.update(contentValues, "${dbManager.colId}=?", selectionArgs)

            if (affectedRows > 0) {
                Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Error while updating ", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Fill out fields", Toast.LENGTH_SHORT).show()
        }
    }
}

package com.example.notizapp

import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.example.notizapp.Database.DbManager

class AddNoteActivity : AppCompatActivity() {

    val dbTable = "Notes"
    var id = 0

    var titleET : EditText? = null
    var descET: EditText? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_note)

        supportActionBar!!.title = "Notiz aktualisieren"

        titleET = findViewById(R.id.title_et)
        descET = findViewById(R.id.desc_et)
    }

    fun addNote(view: View) {
        var dbManager = DbManager(this)

        var values = ContentValues()
        values.put(dbManager.colTitle, titleET?.text.toString())
        values.put(dbManager.colDes, descET?.text.toString())

        val ID = dbManager.insert(values)

        if (ID > 0) {
            Toast.makeText(this, "Notiz hinzugefügt", Toast.LENGTH_LONG).show()
            finish()
        } else {
            Toast.makeText(this, "Fehler beim hinzufügen", Toast.LENGTH_LONG).show()
        }
    }
}
package com.example.flavorfusion

import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.notizapp.DataBase.DbManager

class AddRecipeActivity : AppCompatActivity() {

    var id = 0
    private var isEditMode = false
    var titleET: EditText? = null
    var descET: EditText? = null
    var button: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe)

        supportActionBar!!.title = "Rezept hinzufügen"

        titleET = findViewById(R.id.title_et)
        descET = findViewById(R.id.desc_et)
        button = findViewById(R.id.add_btn)

        //Aufgerufen wenn Änderung (wenn der Intent mit einer ID beladen)
        if (intent.hasExtra("recipeID")) {
            id = intent.getIntExtra("recipeID", 0)
            titleET?.setText(intent.getStringExtra("recipeTitle"))
            descET?.setText(intent.getStringExtra("recipeText"))
            supportActionBar!!.title = "Rezept ändern"
            button?.text = "Änderungen speichern"
            isEditMode = true
        }
    }

    // Methode zum Hinzufügen oder Aktualisieren eines Rezepts
    fun addRecipe(view: View) {
        var dbManager = DbManager(this)
        var values = ContentValues()
        values.put(dbManager.colTitle, titleET?.text.toString())
        values.put(dbManager.colDes, descET?.text.toString())


        if (isEditMode) {
            //Änderung eines Rezepts in der DB
            val updatedRows = dbManager.update(values, "ID=?", arrayOf(id.toString()))
            if (updatedRows > 0) {
                Toast.makeText(this, "Änderungen gespeichert", Toast.LENGTH_LONG).show()
                finish() // Die Aktivität schließen und zum vorherigen Bildschirm zurückkehren
            } else {
                Toast.makeText(this, "Speichern der Änderungen fehlgeschlagen", Toast.LENGTH_LONG)
                    .show()
            }
        } else {
            // Hinzufügen eines neuen Rezepts zur DB
            val ID = dbManager.insert(values)
            if (ID > 0) {
                Toast.makeText(this, "Rezept hinzugefügt", Toast.LENGTH_LONG).show()
                finish()
            } else {
                Toast.makeText(this, "Rezept hinzufügen fehlgeschagen", Toast.LENGTH_LONG).show()
            }
        }
    }
}
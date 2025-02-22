package com.example.flavorfusion

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.example.flavorfusion.model.Recipe
import com.example.notizapp.DataBase.DbManager

class RecipesActivity : AppCompatActivity() {

   private lateinit var recipeListView: ListView
    private lateinit var recipeList: ArrayList<Recipe>
    private lateinit var recipeAdapter: MyRecipeAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipes)

        supportActionBar!!.title = "FlavorFusion Rezepte"

        recipeList = ArrayList()

        recipeListView = findViewById(R.id.recipesLV)

        recipeAdapter = MyRecipeAdapter(this, recipeList)
        recipeListView.adapter = recipeAdapter

        loadQuery() // Methode zum Laden der Rezepte aus der Datenbank aufrufen
    }

    @SuppressLint("Range")
    private fun loadQuery() {
        val dbManager = DbManager(this)
        val projection = arrayOf(dbManager.colId, dbManager.colTitle, dbManager.colDes)
        val selectionArgs = arrayOf("%")
        val cursor = dbManager.Query(projection, "Title like ?", selectionArgs, dbManager.colTitle)

        // Vor dem Laden der neuen Daten die vorhandene Liste löschen
        recipeList.clear()

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndex(dbManager.colId))
                val title = cursor.getString(cursor.getColumnIndex(dbManager.colTitle))
                val description = cursor.getString(cursor.getColumnIndex(dbManager.colDes))

                recipeList.add(Recipe(id, title, description))  //Rezept hinzufügen
            } while (cursor.moveToNext())
        }

        recipeAdapter.notifyDataSetChanged()
    }

    // Adapter für die ListView
    inner class MyRecipeAdapter: BaseAdapter {
        var listRecipeAdapter = ArrayList<Recipe>()
        var context: Context? = null

        constructor(context: Context, listNotesAdapter: ArrayList<Recipe>) : super (){
            this.listRecipeAdapter = listNotesAdapter
            this.context = context
        }

        override fun getCount(): Int {
            return listRecipeAdapter.size
        }

        override fun getItem(position: Int): Any {
            return listRecipeAdapter[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {

            var myView = convertView

            val viewHolder: ViewHolder

            // Abfrage um Ressourcen zusparen --> ob vorhandene View wiederverwendet werden kann
            if (myView== null) {
                // Layout für die einzelne Zeile erstellen
                myView = LayoutInflater.from(context).inflate(R.layout.row, null)

                // ViewHolder erstellen und Views der Zeile zuordnen
                viewHolder= ViewHolder()
                viewHolder.recipeTitleTV = myView.findViewById(R.id.titleTV)
                viewHolder.recipeDescTV= myView.findViewById(R.id.descTV)
                viewHolder.deleteBtn = myView.findViewById(R.id.deleteBtn)
                viewHolder.editBtn = myView.findViewById(R.id.editBtn)
                myView.tag = viewHolder  // ViewHolder erstellen und Views der Zeile zuordnen
            }else{
                viewHolder = myView.tag as ViewHolder
            }

            //Befüllen der Textviews
            val recipes = listRecipeAdapter[position]
            viewHolder.recipeTitleTV.text = recipes.recipeName
            viewHolder.recipeDescTV.text = recipes.recipeDes

            viewHolder.deleteBtn.setOnClickListener {
                // Datenbank-Manager erstellen und Rezept aus der Datenbank löschen
                var dbManager = DbManager(this.context!!)
                val selectionArgs = arrayOf(recipes.recipeID.toString())
                dbManager.delete("ID=?",selectionArgs)
                loadQuery()  // Aktualisierung der ListView durch erneutes Laden der Daten
                Toast.makeText(context, "Rezept gelöscht", Toast.LENGTH_SHORT).show()
            }

            viewHolder.editBtn.setOnClickListener {
                // Intent erstellen und zur AddRecipeActivity mit den Daten des ausgewählten Rezepts navigieren
                val intent = Intent(context, AddRecipeActivity::class.java)
                intent.putExtra("recipeID", recipes.recipeID)
                intent.putExtra("recipeTitle", recipes.recipeName)
                intent.putExtra("recipeText", recipes.recipeDes)
                context?.startActivity(intent)
            }

            return myView
        }

    }

}


private class ViewHolder {

    lateinit var recipeTitleTV: TextView
    lateinit var recipeDescTV: TextView

    lateinit var editBtn: ImageButton
    lateinit var deleteBtn: ImageButton
}
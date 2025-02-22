package com.example.flavorfusion

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.example.flavorfusion.model.Recipe
import com.example.notizapp.DataBase.DbManager
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.sqrt
import kotlin.random.Random

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var shakeSensor: Sensor
    private lateinit var recipeTitleTextView: TextView
    private lateinit var recipeDescriptionTextView: TextView

    private var listRecipes = ArrayList<Recipe>()

    private lateinit var addButton: Button
    private lateinit var showButton: Button

    private lateinit var imageView: ImageView

    private var isShakeAllowed = true
    private var isFirstShake = true

    private var currentIndex = -1
    private var lastShakeTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Die TextView- und Button-Elemente aus dem Layout initialisieren
        recipeTitleTextView = findViewById(R.id.text_recipe_title)
        recipeDescriptionTextView = findViewById(R.id.text_recipe_description)
        imageView = findViewById(R.id.imageView)
        addButton = findViewById(R.id.btn_AddRecipe)
        showButton = findViewById(R.id.btn_ShowAllRecipes)

        // SensorManager und Shake-Sensor initialisieren
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        shakeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // Click-Listener für den "Add" Button
        addButton.setOnClickListener {
            Log.d("Click Add Button", "Add Button clicked")
            val intent = Intent(this, AddRecipeActivity::class.java)
            startActivity(intent)
        }

        // Click-Listener für den "Show" Button
        showButton.setOnClickListener {
            val intent = Intent(this, RecipesActivity::class.java)
            startActivity(intent)
        }

        // Text-Views am Anfang ausblenden
        recipeTitleTextView.visibility = View.GONE
        recipeDescriptionTextView.visibility = View.GONE

        // Zufälliges Bild laden
        loadRandomImage()
    }

    // Startrezepte hinzufügen
    private fun addStartRecipesToDatabase(dbManager: DbManager) {

        //Constants --> enthält die Startrezepte
        val recipesToAdd = Constants.startRecipes.filter { recipe ->
            !listRecipes.any { it.recipeID == recipe.recipeID }
        }
        val values = ContentValues()
        for (recipe in recipesToAdd) {
            values.apply {
                put(dbManager.colTitle, recipe.recipeName)
                put(dbManager.colDes, recipe.recipeDes)
            }
            dbManager.insert(values)
            Log.d("DBLoader","Rezepte der Datenbank hinzugefügt")
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(
            this,
            shakeSensor,
            SensorManager.SENSOR_DELAY_GAME
        )
        loadQuery()
        isShakeAllowed = true
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        //Sensor prüfen
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER && isShakeAllowed) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Berechnung der Gesamtbeschleunigung basierend auf den Achsenwerten
            val acceleration = sqrt(x * x + y * y + z * z)
            val currentTime = System.currentTimeMillis()

            val SHAKE_THRESHOLD = 15f // Schwellenwert für Shake-Bewegung
            val SHAKE_INTERVAL = 2000 // Zeitintervall für Shake-Erkennung (2 Sekunden)

            if (isFirstShake && acceleration > SHAKE_THRESHOLD && currentTime - lastShakeTime >= SHAKE_INTERVAL) {
                lastShakeTime = currentTime
                isFirstShake = false
                recipeTitleTextView.visibility = View.VISIBLE
                recipeDescriptionTextView.visibility = View.VISIBLE
                loadRandomRecipe()
                loadRandomImage() // Hier das Bild aktualisieren
            } else if (!isFirstShake && acceleration > SHAKE_THRESHOLD && currentTime - lastShakeTime >= SHAKE_INTERVAL) {
                lastShakeTime = currentTime
                loadRandomRecipe()
                loadRandomImage() // Hier das Bild aktualisieren
            }
        }
    }


    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    @SuppressLint("Range")
    private fun loadQuery() {
        // Erzeugen einer Instanz der DbManager-Klasse
        val dbManager = DbManager(this)

        val projection = arrayOf(dbManager.colId, dbManager.colTitle, dbManager.colDes)
        val selectionArgs = arrayOf("%")
        // Abfrage aller Rezepte aus der Datenbank
        val cursor = dbManager.Query(projection, "Title like ?", selectionArgs, "${dbManager.colTitle} LIKE ?")

        //Überprüfen, ob die Datenbank leer ist und Startrezepte hinzufügen
        if (cursor.count == 0) {
            addStartRecipesToDatabase(dbManager)
        }
        // Liste von Rezepten leeren
        listRecipes.clear()

        // Daten aus dem Cursor in die Liste von Rezepten einfügen
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndex(dbManager.colId))
                val title = cursor.getString(cursor.getColumnIndex(dbManager.colTitle))
                val description = cursor.getString(cursor.getColumnIndex(dbManager.colDes))

                listRecipes.add(Recipe(id, title, description))
            } while (cursor.moveToNext())
        }
    }

    private fun loadRandomRecipe() {
        // Überprüfen, ob die Liste der Rezepte nicht leer ist
        if (listRecipes.isNotEmpty()) {
            // Zufälligen Index für ein Rezept auswählen, der nicht dem aktuellen Index entspricht
            var randomIndex = Random.nextInt(0, listRecipes.size)
            while (randomIndex == currentIndex && listRecipes.size > 1) {
                randomIndex = Random.nextInt(0, listRecipes.size)
            }
            currentIndex = randomIndex
            val randomRecipe = listRecipes[currentIndex]
            updateRecipeDetails(randomRecipe)
        }
        isShakeAllowed = true
    }

    //Text des Rezepts updaten (bei neuem random Rezept)
    private fun updateRecipeDetails(recipe: Recipe) {
        recipeTitleTextView.text = recipe.recipeName
        recipeDescriptionTextView.text = recipe.recipeDes
    }

    private fun loadRandomImage() {
        ImageLoaderTask().execute()
    }

    private inner class ImageLoaderTask : AsyncTask<Void, Void, Bitmap?>() {

        // Die Methode doInBackground wird im Hintergrund ausgeführt und lädt das Bild von der angegebenen URL herunter.
        override fun doInBackground(vararg params: Void?): Bitmap? {
            val category = "food"
            val accessKey = "+brY5cw/e7fwjYFoDETqQw==mQ1aJG1veJPjUbjC"
            val url = URL("https://api.api-ninjas.com/v1/randomimage?category=$category")

            var connection: HttpURLConnection? = null
            var inputStream: InputStream? = null

            try {
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("X-Api-Key", accessKey)
                connection.setRequestProperty("Accept", "image/jpg")
                connection.connect()

                // Überprüfen, ob die Verbindung erfolgreich war und eine Antwort erhalten wurde.
                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    inputStream = connection.inputStream
                    // Das Bild wird aus dem InputStream dekodiert und als Bitmap zurückgegeben.
                    return BitmapFactory.decodeStream(inputStream)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                // Die Verbindung und der InputStream werden geschlossen, um Ressourcen freizugeben.
                connection?.disconnect()
                inputStream?.close()
            }
            // Falls ein Fehler auftritt oder das Bild nicht geladen werden kann, wird null zurückgegeben.
            return null
        }

        // Die Methode onPostExecute wird nach Abschluss von doInBackground auf dem Hauptthread ausgeführt.
        // Hier wird das Ergebnis, das die geladene Bitmap enthält, verwendet, um das ImageView zu aktualisieren.
        override fun onPostExecute(result: Bitmap?) {
            super.onPostExecute(result)
            if (result != null) {
                imageView.setImageBitmap(result)
                Log.d("ImageLoader", "Bild erfolgreich geladen")
            } else {
                Log.d("ImageLoader", "Fehler beim Laden des Bildes")
            }
        }
    }
}

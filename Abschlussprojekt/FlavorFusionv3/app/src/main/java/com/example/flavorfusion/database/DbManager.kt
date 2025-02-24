package com.example.notizapp.DataBase

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQueryBuilder
import android.widget.Toast

class DbManager {

    var dbName = "MyRecipes"
    var dbTable = "Recipes"

    //columns
    var colId = "ID"
    var colTitle = "Title"
    var colDes = "Description"

    var dbVersion = 1

    val sqlCreateTable = "CREATE TABLE IF NOT EXISTS " + dbTable +
            " (" + colId + " INTEGER PRIMARY KEY, " + colTitle + " TEXT, " +
            colDes + " TEXT)"

    var sqlDB: SQLiteDatabase? = null

    constructor(context: Context){
        var db = DatabaseHelperNotes(context)
        sqlDB = db.writableDatabase

    }

    inner class DatabaseHelperNotes : SQLiteOpenHelper{
        var context: Context? = null
        constructor(context: Context) : super(context, dbName, null, dbVersion){
            this.context = context;
        }

        override fun onCreate(db: SQLiteDatabase?) {
            db!!.execSQL(sqlCreateTable)
            Toast.makeText(this.context, "Database created successful", Toast.LENGTH_LONG).show()

        }

        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
            db!!.execSQL("Drop table if Exists" + dbTable)
        }

    }

    fun insert(values: ContentValues) : Long {
        val ID = sqlDB!!.insert(dbTable,"",values)
        return ID;
    }

    fun delete (selection: String, selectionArgs: Array<String>) : Int {
        val count = sqlDB!!.delete(dbTable, selection, selectionArgs)
        return count
    }

    fun update (values: ContentValues, selection: String, selectionArgs: Array<String>) : Int {
        val count = sqlDB!!.update(dbTable, values, selection, selectionArgs)
        return count
    }

    // Abfrage
    fun Query(projection: Array<String>, selection: String, selectionArgs: Array<String>, sortOrder: String) : Cursor {
        val db = SQLiteQueryBuilder()
        db.tables = dbTable
        val cursor = db.query(sqlDB, projection, selection,selectionArgs, null, null, sortOrder)
        return cursor

    }


}
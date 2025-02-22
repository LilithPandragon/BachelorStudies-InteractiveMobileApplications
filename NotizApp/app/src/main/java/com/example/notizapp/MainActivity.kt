package com.example.notizapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import com.example.notizapp.Database.DbManager
import com.example.notizapp.Model.Note
class MainActivity : AppCompatActivity() {

    var listNotes = ArrayList<Note>()
    var noteLV : ListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        noteLV = findViewById(R.id.noteLv)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onResume() {
        super.onResume()
        loadQuery()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item != null) {
            when(item.itemId) {
                R.id.addNote -> {
                    startActivity(Intent(this, AddNoteActivity::class.java))
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("Range")
    private fun loadQuery() {
        var dbManager = DbManager(this)
        val projection = arrayOf(dbManager.colId, dbManager.colTitle, dbManager.colDes)
        val selectionArgs = arrayOf("%")
        val cursor = dbManager.query(projection, "Title like ?", selectionArgs, dbManager.colTitle)

        listNotes.clear()

        if (cursor.moveToFirst()) {
            do {
                val ID = cursor.getInt(cursor.getColumnIndex(dbManager.colId))
                val Title = cursor.getString(cursor.getColumnIndex((dbManager.colTitle)))
                val Description = cursor.getString(cursor.getColumnIndex(dbManager.colDes))

                listNotes.add(Note(ID, Title, Description))

            } while (cursor.moveToNext())
        }

        var myNotesAdapter = MyNotesAdapter(this, listNotes)
        noteLV?.adapter = myNotesAdapter
    }

    inner class MyNotesAdapter : BaseAdapter {
        var listNotesAdapter = ArrayList<Note>()
        var context: Context? = null

        constructor(context: Context, listNotesAdapter: ArrayList<Note>) : super() {
            this.listNotesAdapter = listNotesAdapter
            this.context = context
        }

        override fun getCount(): Int {
            return listNotesAdapter.size
        }

        override fun getItem(position: Int): Any {
            return listNotesAdapter[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var myView = layoutInflater.inflate(R.layout.row, null)
            val myNotes = listNotesAdapter[position]
            val holder: ViewHolder
            holder = ViewHolder()

            holder.titleTV = myView.findViewById(R.id.titleTV)
            holder.titleTV.text = myNotes.noteName

            holder.descTV = myView.findViewById(R.id.descTV)
            holder.descTV.text = myNotes.noteDes

            holder.deleteButton = myView.findViewById(R.id.deleteBtn)


            holder.deleteButton.setOnClickListener {
                var dbManager = DbManager(this.context!!)
                var selectionArgs = arrayOf(myNotes.noteID.toString())
                dbManager.delete("ID=?", selectionArgs)
                loadQuery()
            }

            holder.editButton = myView.findViewById(R.id.editBtn)

            holder.editButton.setOnClickListener {
                val intent = Intent(context, EditNoteActivity::class.java)
                intent.putExtra("noteID", myNotes.noteID)
                intent.putExtra("noteName", myNotes.noteName)
                intent.putExtra("noteDesc",myNotes.noteDes)
                context?.startActivity(intent)
            }

            return myView
        }
    }


    private class ViewHolder {
    lateinit var titleTV: TextView
    lateinit var descTV: TextView

    lateinit var deleteButton: ImageButton
    lateinit var editButton: ImageButton
} }
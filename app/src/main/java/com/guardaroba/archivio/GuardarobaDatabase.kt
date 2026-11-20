package com.guardaroba.archivio

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Schema esportato in app/schemas: un nuovo campo si aggiunge con version+1 e una AutoMigration (o Migration).
@Database(entities = [Capo::class], version = 1)
abstract class GuardarobaDatabase : RoomDatabase() {
    abstract fun capoDao(): CapoDao

    companion object {
        const val NOME = "guardaroba.db"

        @Volatile private var istanza: GuardarobaDatabase? = null

        fun get(context: Context): GuardarobaDatabase = istanza ?: synchronized(this) {
            istanza ?: apri(context).also { istanza = it }
        }

        fun apri(context: Context): GuardarobaDatabase =
            Room.databaseBuilder(context.applicationContext, GuardarobaDatabase::class.java, NOME).build()
    }
}

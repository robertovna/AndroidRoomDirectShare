package com.example.inventory.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.inventory.encrypt.SQLCypherUtils
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteDatabaseHook
import net.sqlcipher.database.SupportFactory

@Database(entities = [Item::class], version = 2, exportSchema = false)
abstract class ItemRoomDatabase : RoomDatabase() {

    abstract fun itemDao(): ItemDao

    companion object {
        @Volatile
        private var INSTANCE: ItemRoomDatabase? = null

        private const val DB_NAME: String = "item_database"

        fun getDatabase(context: Context, password: String): ItemRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val passwordBytes = password.toByteArray()
                val state = SQLCypherUtils.getDatabaseState(context, DB_NAME)

                if (state == SQLCypherUtils.State.UNENCRYPTED) {
                    SQLCypherUtils.encrypt(context, DB_NAME, passwordBytes)
                }
                val hook: SQLiteDatabaseHook = object : SQLiteDatabaseHook {
                    override fun preKey(database: SQLiteDatabase) {
                        database.rawExecSQL("PRAGMA cipher_memory_security = OFF")
                    }
                    override fun postKey(database: SQLiteDatabase) {
                    }
                }
                val supportFactory = SupportFactory(SQLiteDatabase.getBytes(password.toCharArray()), hook)
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        ItemRoomDatabase::class.java,
                        DB_NAME
                    )
                    .openHelperFactory(supportFactory)
                    .fallbackToDestructiveMigration().build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
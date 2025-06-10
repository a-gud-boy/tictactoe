package com.a_gud_boy.tictactoe

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters // Required if you add TypeConverters

// Add TypeConverter for Date/Long if you plan to use Date objects directly
// For now, we are using Long for timestamp, so it's not immediately needed unless other Date fields are added.

@Database(
    entities = [MatchEntity::class, RoundEntity::class, MoveEntity::class],
    version = 1, // Initial version
    exportSchema = false // Recommended to set to true for production apps for schema history
)
// Add @TypeConverters if you have any, e.g. @TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun matchDao(): MatchDao
    abstract fun roundDao(): RoundDao
    abstract fun moveDao(): MoveDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tictactoe_history_database" // Database file name
                )
                    // TODO: For production, implement proper migrations instead of fallbackToDestructiveMigration.
                    // For now, if schema changes, the database will be wiped and recreated.
                    .fallbackToDestructiveMigration()
                    // .addMigrations(MIGRATION_1_2, MIGRATION_2_3) // Example of adding migrations
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

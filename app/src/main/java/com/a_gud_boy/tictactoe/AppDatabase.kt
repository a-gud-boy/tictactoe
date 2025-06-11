package com.a_gud_boy.tictactoe

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Add TypeConverter for Date/Long if you plan to use Date objects directly
// For now, we are using Long for timestamp, so it's not immediately needed unless other Date fields are added.

@Database(
    entities = [MatchEntity::class, RoundEntity::class, MoveEntity::class],
    version = 2, // Incremented version
    exportSchema = false // Recommended to set to true for production apps for schema history
)
@TypeConverters(MatchWinnerTypeConverter::class) // Add the new type converter
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
                    // Removed fallbackToDestructiveMigration to use explicit migration
                    .addMigrations(MIGRATION_1_2) // Add the migration
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Migration from version 1 to 2: Adds the 'winner' column to 'matches'
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add the 'winner' column with DRAW as the default for all existing rows.
                // This ensures the NOT NULL constraint is immediately satisfied.
                database.execSQL("ALTER TABLE matches ADD COLUMN winner TEXT NOT NULL DEFAULT '${MatchWinner.DRAW.name}'")

                // Now, update rows where Player 1 won
                database.execSQL("UPDATE matches SET winner = '${MatchWinner.PLAYER1.name}' WHERE player1Score > player2Score")

                // Then, update rows where Player 2 won
                database.execSQL("UPDATE matches SET winner = '${MatchWinner.PLAYER2.name}' WHERE player2Score > player1Score")

                // Rows where player1Score == player2Score will correctly remain as DRAW due to the default.
            }
        }
    }
}

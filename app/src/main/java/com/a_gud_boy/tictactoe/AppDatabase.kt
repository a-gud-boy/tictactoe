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

import com.a_gud_boy.tictactoe.GameTypeConverter // Import new converter
import com.a_gud_boy.tictactoe.StringListConverter // Added import

@Database(
    entities = [MatchEntity::class, RoundEntity::class, MoveEntity::class],
    version = 5, // Incremented to 5
    exportSchema = false // Recommended to set to true for production apps for schema history
)
@TypeConverters(
    MatchWinnerTypeConverter::class,
    GameTypeConverter::class,
    StringListConverter::class // Added StringListConverter
)
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5) // Added MIGRATION_4_5
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

        // Migration from version 2 to 3: Adds the 'isAgainstAi' column to 'matches'
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE matches ADD COLUMN isAgainstAi INTEGER NOT NULL DEFAULT 0")
            }
        }

        // Migration from version 3 to 4: Adds the 'gameType' column to 'matches'
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE matches ADD COLUMN gameType TEXT NOT NULL DEFAULT '${GameType.NORMAL.name}'")
            }
        }

        // New Migration from version 4 to 5
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add the 'winningCombinationJson' column to the 'rounds' table.
                // It's nullable, so no default value is strictly needed beyond what SQLite provides for NULL.
                database.execSQL("ALTER TABLE rounds ADD COLUMN winningCombinationJson TEXT NULL")
            }
        }
    }
}

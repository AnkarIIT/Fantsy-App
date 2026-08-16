package com.example.data.local

import androidx.room.*
import com.example.data.models.PlayerRole
import kotlinx.coroutines.flow.Flow

class RoomConverters {
    @TypeConverter
    fun fromPlayerRole(value: PlayerRole): String = value.name

    @TypeConverter
    fun toPlayerRole(value: String): PlayerRole = PlayerRole.valueOf(value)
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val amount: Double,
    val typeName: String, // DEPOSIT, WITHDRAW, etc.
    val timestamp: Long,
    val description: String,
    val status: String
)

@Entity(tableName = "wallet_balance")
data class WalletBalanceEntity(
    @PrimaryKey val id: Int = 1, // Only 1 row for the singular user profile
    val depositBalance: Double,
    val winningsBalance: Double,
    val bonusBalance: Double
)

@Entity(tableName = "played_games")
data class PlayedGameEntity(
    @PrimaryKey val id: String,
    val title: String,
    val result: String,
    val amount: Double,
    val timestamp: Long
)

// Fantasy Sports Entities
@Entity(tableName = "fantasy_player")
data class FantasyPlayerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val teamId: String,
    val role: PlayerRole,
    val credit: Double,
    val avatarUrl: String?
)

@Entity(
    tableName = "fantasy_team",
    foreignKeys = [
        ForeignKey(
            entity = FantasyPlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["captainId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FantasyPlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["viceCaptainId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("captainId"),
        Index("viceCaptainId")
    ]
)
data class FantasyTeamEntity(
    @PrimaryKey val id: String,
    val matchId: String,
    val contestId: String,
    val captainId: String,
    val viceCaptainId: String,
    val totalPoints: Int = 0,
    val rank: Int? = null,
    val createdAt: Long = System.currentTimeMillis()
)

// Join table for Fantasy Team and Players
@Entity(
    tableName = "fantasy_team_player_cross_ref",
    primaryKeys = ["teamId", "playerId"],
    foreignKeys = [
        ForeignKey(
            entity = FantasyTeamEntity::class,
            parentColumns = ["id"],
            childColumns = ["teamId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FantasyPlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["playerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("teamId"),
        Index("playerId")
    ]
)
data class FantasyTeamPlayerCrossRef(
    val teamId: String,
    val playerId: String
)

// Data class with team and players
data class FantasyTeamWithPlayers(
    @Embedded val team: FantasyTeamEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = FantasyTeamPlayerCrossRef::class,
            parentColumn = "teamId",
            entityColumn = "playerId"
        )
    )
    val players: List<FantasyPlayerEntity>
)

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallet_balance WHERE id = 1")
    fun getWalletBalance(): Flow<WalletBalanceEntity?>

    @Query("SELECT * FROM wallet_balance WHERE id = 1")
    suspend fun getWalletBalanceOnce(): WalletBalanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateWalletBalance(balance: WalletBalanceEntity)

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)
}

@Dao
interface GameDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayedGame(game: PlayedGameEntity)

    @Query("SELECT * FROM played_games ORDER BY timestamp DESC")
    fun getAllPlayedGames(): Flow<List<PlayedGameEntity>>
}

@Dao
interface FantasyDao {
    // Players
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: FantasyPlayerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayers(players: List<FantasyPlayerEntity>)

    @Query("SELECT * FROM fantasy_player WHERE id = :playerId")
    suspend fun getPlayer(playerId: String): FantasyPlayerEntity?

    // Teams
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeam(team: FantasyTeamEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeamPlayerCrossRef(crossRef: FantasyTeamPlayerCrossRef)

    @Transaction
    @Query("SELECT * FROM fantasy_team ORDER BY createdAt DESC")
    fun getAllFantasyTeams(): Flow<List<FantasyTeamWithPlayers>>

    @Transaction
    @Query("SELECT * FROM fantasy_team WHERE matchId = :matchId ORDER BY createdAt DESC")
    fun getFantasyTeamsForMatch(matchId: String): Flow<List<FantasyTeamWithPlayers>>

    @Transaction
    @Query("SELECT * FROM fantasy_team WHERE contestId = :contestId LIMIT 1")
    suspend fun getFantasyTeamForContest(contestId: String): FantasyTeamWithPlayers?
}

@Database(
    entities = [
        TransactionEntity::class, WalletBalanceEntity::class,
        FantasyPlayerEntity::class, FantasyTeamEntity::class,
        FantasyTeamPlayerCrossRef::class, PlayedGameEntity::class
    ],
    version = 3, // Incremented DB version
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun walletDao(): WalletDao
    abstract fun fantasyDao(): FantasyDao
    abstract fun gameDao(): GameDao
}

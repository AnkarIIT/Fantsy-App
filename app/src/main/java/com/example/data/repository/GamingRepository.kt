package com.example.data.repository

import android.content.Context
import androidx.room.Room
import androidx.room.withTransaction
import com.example.data.local.*
import com.example.data.models.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.UUID

class GamingRepository(private val context: Context) {

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "royale_gaming_db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    private val walletDao by lazy { database.walletDao() }
    private val fantasyDao by lazy { database.fantasyDao() }
    private val gameDao by lazy { database.gameDao() }
    private val dataStoreManager by lazy { DataStoreManager(context) }

    // Settings from DataStore
    val appSettingsFlow: Flow<AppSettings> = dataStoreManager.appSettingsFlow

    suspend fun updateAppSettings(newSettings: AppSettings) {
        dataStoreManager.updateAppSettings(newSettings)
    }

    // Persisted profile / KYC state from DataStore
    val kycStatusFlow: Flow<KYCStatus?> = dataStoreManager.kycStatusFlow
    val userNameFlow: Flow<String?> = dataStoreManager.userNameFlow

    suspend fun saveKycStatus(status: KYCStatus) {
        dataStoreManager.saveKycStatus(status)
    }

    suspend fun saveUserName(name: String) {
        dataStoreManager.saveUserName(name)
    }

    // Fantasy Functions
    suspend fun saveFantasyTeam(
        teamId: String,
        matchId: String,
        contestId: String,
        captainId: String,
        viceCaptainId: String,
        players: List<CricketPlayer>
    ) {
        // First save all players
        val playerEntities = players.map {
            FantasyPlayerEntity(
                id = it.id, name = it.name,
                teamId = it.teamId,
                role = it.role,
                credit = it.credit,
                avatarUrl = it.avatarUrl
            )
        }
        fantasyDao.insertPlayers(playerEntities)

        // Then save team
        val teamEntity = FantasyTeamEntity(
            id = teamId,
            matchId = matchId,
            contestId = contestId,
            captainId = captainId,
            viceCaptainId = viceCaptainId
        )
        fantasyDao.insertTeam(teamEntity)

        // Save cross-ref for players
        players.forEach { player ->
            fantasyDao.insertTeamPlayerCrossRef(
                FantasyTeamPlayerCrossRef(
                    teamId = teamId,
                    playerId = player.id
                )
            )
        }
    }

    fun getAllFantasyTeams(): Flow<List<FantasyTeamWithPlayers>> {
        return fantasyDao.getAllFantasyTeams()
    }

    fun getFantasyTeamsForMatch(matchId: String): Flow<List<FantasyTeamWithPlayers>> {
        return fantasyDao.getFantasyTeamsForMatch(matchId)
    }

    // Played Games Persistence
    suspend fun savePlayedGame(playedGame: PlayedGame) {
        gameDao.insertPlayedGame(
            PlayedGameEntity(
                id = playedGame.id,
                title = playedGame.title,
                result = playedGame.result,
                amount = playedGame.amount,
                timestamp = playedGame.timestamp
            )
        )
    }

    fun observePlayedGames(): Flow<List<PlayedGame>> {
        return gameDao.getAllPlayedGames().map { entities ->
            entities.map {
                PlayedGame(
                    id = it.id,
                    title = it.title,
                    result = it.result,
                    amount = it.amount,
                    timestamp = it.timestamp
                )
            }
        }
    }

    // Seed initial balance if empty
    suspend fun checkAndSeedInitialData() {
        val current = walletDao.getWalletBalance().firstOrNull()
        if (current == null) {
            walletDao.updateWalletBalance(
                WalletBalanceEntity(
                    id = 1,
                    depositBalance = 250.00,
                    winningsBalance = 120.00,
                    bonusBalance = 15.00
                )
            )
            // Add a welcome transition
            walletDao.insertTransaction(
                TransactionEntity(
                    id = "TX-${UUID.randomUUID().toString().take(6).uppercase()}",
                    amount = 15.00,
                    typeName = TransactionType.WINNING_PAYOUT.name,
                    timestamp = System.currentTimeMillis(),
                    description = "Welcome Sign-Up Bonus Gold Cash",
                    status = "SUCCESS"
                )
            )
            walletDao.insertTransaction(
                TransactionEntity(
                    id = "TX-${UUID.randomUUID().toString().take(6).uppercase()}",
                    amount = 250.00,
                    typeName = TransactionType.DEPOSIT.name,
                    timestamp = System.currentTimeMillis() - 3600000,
                    description = "UPI Deposit Match Instant",
                    status = "SUCCESS"
                )
            )
        }
        // Seed fantasy cricket players if empty
        val existingPlayer = fantasyDao.getPlayer("p1")
        if (existingPlayer == null) {
            fantasyDao.insertPlayers(fantasyCricketPlayers.map { FantasyPlayerEntity(
                id = it.id,
                name = it.name,
                teamId = it.teamId,
                role = it.role,
                credit = it.credit,
                avatarUrl = it.avatarUrl
            ) })
        }
    }

    // Wallet Observers
    fun observeWalletBalance(): Flow<WalletBalanceEntity?> {
        return walletDao.getWalletBalance()
    }

    fun observeTransactions(): Flow<List<WalletTransaction>> {
        return walletDao.getAllTransactions().map { entities ->
            entities.map {
                WalletTransaction(
                    id = it.id,
                    amount = it.amount,
                    type = TransactionType.valueOf(it.typeName),
                    timestamp = it.timestamp,
                    description = it.description,
                    status = it.status
                )
            }
        }
    }

    // Wallet Operations — every read-modify-write runs inside a single Room transaction
    // so concurrent re-entry can never double-charge or double-credit.
    suspend fun depositFunds(amount: Double): Result<Unit> {
        delay(1200) // Simulate network call latency
        if (amount <= 0) return Result.failure(Exception("Amount must be greater than zero."))

        return database.withTransaction {
            val current = walletDao.getWalletBalanceOnce() ?: WalletBalanceEntity(1, 0.0, 0.0, 0.0)
            val updated = current.copy(depositBalance = current.depositBalance + amount)
            walletDao.updateWalletBalance(updated)

            walletDao.insertTransaction(
                TransactionEntity(
                    id = "DEP-${UUID.randomUUID().toString().take(6).uppercase()}",
                    amount = amount,
                    typeName = TransactionType.DEPOSIT.name,
                    timestamp = System.currentTimeMillis(),
                    description = "Deposit via NetBanking / UPI",
                    status = "SUCCESS"
                )
            )
            Result.success(Unit)
        }
    }

    suspend fun withdrawFunds(amount: Double): Result<Unit> {
        delay(1500) // Simulate security & gateway latency
        if (amount <= 0) return Result.failure(Exception("Amount must be greater than zero."))

        return database.withTransaction {
            val current = walletDao.getWalletBalanceOnce()
                ?: return@withTransaction Result.failure(Exception("No wallet set up."))
            if (current.winningsBalance < amount) {
                return@withTransaction Result.failure(Exception("Insufficient winnings balance. Winnings can only be withdrawn."))
            }

            val updated = current.copy(winningsBalance = current.winningsBalance - amount)
            walletDao.updateWalletBalance(updated)

            walletDao.insertTransaction(
                TransactionEntity(
                    id = "WTH-${UUID.randomUUID().toString().take(6).uppercase()}",
                    amount = amount,
                    typeName = TransactionType.WITHDRAW.name,
                    timestamp = System.currentTimeMillis(),
                    description = "Bank Account Direct Transfer",
                    status = "SUCCESS"
                )
            )
            Result.success(Unit)
        }
    }

    suspend fun awardWinnings(amount: Double): Result<Unit> {
        delay(800)
        if (amount <= 0) return Result.failure(Exception("Winnings amount must be greater than zero."))

        return database.withTransaction {
            val current = walletDao.getWalletBalanceOnce() ?: WalletBalanceEntity(1, 0.0, 0.0, 0.0)
            val updated = current.copy(winningsBalance = current.winningsBalance + amount)
            walletDao.updateWalletBalance(updated)

            walletDao.insertTransaction(
                TransactionEntity(
                    id = "WIN-${UUID.randomUUID().toString().take(6).uppercase()}",
                    amount = amount,
                    typeName = TransactionType.WINNING_PAYOUT.name,
                    timestamp = System.currentTimeMillis(),
                    description = "Game Winnings Payout",
                    status = "SUCCESS"
                )
            )
            Result.success(Unit)
        }
    }

    /**
     * Deduct entry fee for fantasy (or other). Prefers bonus (up to 10%), then deposit.
     */
    suspend fun deductEntryFee(entryFee: Double, description: String): Result<Unit> {
        delay(900)
        if (entryFee <= 0) return Result.success(Unit) // free contests ok

        return database.withTransaction {
            val current = walletDao.getWalletBalanceOnce()
                ?: return@withTransaction Result.failure(Exception("No wallet set up."))
            val totalAvailable = current.depositBalance + current.bonusBalance
            if (totalAvailable < entryFee) {
                return@withTransaction Result.failure(Exception("Insufficient balance to join. Please add Cash."))
            }

            val bonusDeduction = minOf(current.bonusBalance, entryFee * 0.10)
            val depositDeduction = entryFee - bonusDeduction

            val updated = current.copy(
                depositBalance = current.depositBalance - depositDeduction,
                bonusBalance = current.bonusBalance - bonusDeduction
            )
            walletDao.updateWalletBalance(updated)

            walletDao.insertTransaction(
                TransactionEntity(
                    id = "FAN-${UUID.randomUUID().toString().take(6).uppercase()}",
                    amount = entryFee,
                    typeName = TransactionType.JOIN_FEE.name,
                    timestamp = System.currentTimeMillis(),
                    description = description,
                    status = "SUCCESS"
                )
            )
            Result.success(Unit)
        }
    }

    /**
     * Atomically deducts the entry fee AND persists the fantasy team in one Room
     * transaction. A failure in either step rolls back both, so a user can never be
     * charged without a saved team (or vice versa).
     */
    suspend fun joinFantasyContestAtomically(
        entryFee: Double,
        description: String,
        teamId: String,
        matchId: String,
        contestId: String,
        captainId: String,
        viceCaptainId: String,
        players: List<CricketPlayer>
    ): Result<Unit> {
        delay(900)
        if (entryFee > 0) {
            val current = walletDao.getWalletBalanceOnce()
                ?: return Result.failure(Exception("No wallet set up."))
            val totalAvailable = current.depositBalance + current.bonusBalance
            if (totalAvailable < entryFee) {
                return Result.failure(Exception("Insufficient balance to join. Please add Cash."))
            }
        }

        return database.withTransaction {
            if (entryFee > 0) {
                val current = walletDao.getWalletBalanceOnce()
                    ?: return@withTransaction Result.failure(Exception("No wallet set up."))
                val bonusDeduction = minOf(current.bonusBalance, entryFee * 0.10)
                val depositDeduction = entryFee - bonusDeduction
                walletDao.updateWalletBalance(
                    current.copy(
                        depositBalance = current.depositBalance - depositDeduction,
                        bonusBalance = current.bonusBalance - bonusDeduction
                    )
                )
                walletDao.insertTransaction(
                    TransactionEntity(
                        id = "FAN-${UUID.randomUUID().toString().take(6).uppercase()}",
                        amount = entryFee,
                        typeName = TransactionType.JOIN_FEE.name,
                        timestamp = System.currentTimeMillis(),
                        description = description,
                        status = "SUCCESS"
                    )
                )
            }

            fantasyDao.insertPlayers(players.map {
                FantasyPlayerEntity(
                    id = it.id, name = it.name,
                    teamId = it.teamId,
                    role = it.role,
                    credit = it.credit,
                    avatarUrl = it.avatarUrl
                )
            })

            fantasyDao.insertTeam(
                FantasyTeamEntity(
                    id = teamId,
                    matchId = matchId,
                    contestId = contestId,
                    captainId = captainId,
                    viceCaptainId = viceCaptainId
                )
            )

            players.forEach { player ->
                fantasyDao.insertTeamPlayerCrossRef(
                    FantasyTeamPlayerCrossRef(teamId = teamId, playerId = player.id)
                )
            }
            Result.success(Unit)
        }
    }

    suspend fun registerForTournament(match: TournamentMatch): Result<Unit> {
        delay(1000) // Simulate server sync
        return database.withTransaction {
            val current = walletDao.getWalletBalanceOnce()
                ?: return@withTransaction Result.failure(Exception("No wallet set up."))
            val totalAvailable = current.depositBalance + current.bonusBalance
            if (totalAvailable < match.entryFee) {
                return@withTransaction Result.failure(Exception("Insufficient balance to join. Please add Cash."))
            }

            // Subtract first from bonus up to 10% then deposit
            val bonusDeduction = minOf(current.bonusBalance, match.entryFee * 0.10)
            val depositDeduction = match.entryFee - bonusDeduction

            val updated = current.copy(
                depositBalance = current.depositBalance - depositDeduction,
                bonusBalance = current.bonusBalance - bonusDeduction
            )

            walletDao.updateWalletBalance(updated)
            walletDao.insertTransaction(
                TransactionEntity(
                    id = "REG-${UUID.randomUUID().toString().take(6).uppercase()}",
                    amount = match.entryFee,
                    typeName = TransactionType.JOIN_FEE.name,
                    timestamp = System.currentTimeMillis(),
                    description = "Entry Fee: ${match.title} (${match.gameTitle})",
                    status = "SUCCESS"
                )
            )
            Result.success(Unit)
        }
    }

    // Static Games Provider
    val gamesList = listOf(
        GameItem(
            id = "g_rummy",
            title = "Rummy Royale 13",
            description = "Ultimate card showdown. Instant table matching with secure random shuffling.",
            category = GameCategory.CARDS,
            imageUrl = "g_rummy",
            playersCount = 42800,
            maxMultiplier = "100x Winnings",
            isHot = true
        ),
        GameItem(
            id = "g_aviator",
            title = "Rocket Multiplier X",
            description = "Predict the multiplier and cash out before the rocket crashes! High-intensity real-time strategy.",
            category = GameCategory.MULTIPLIER,
            imageUrl = "g_aviator",
            playersCount = 108420,
            maxMultiplier = "2500x Max",
            isHot = true
        ),
        GameItem(
            id = "g_cricket",
            title = "Fantasy Premier Cricket",
            description = "Form dream lineups and battle for megamillion prize pools in national and international t20 schedules.",
            category = GameCategory.FANTASY,
            imageUrl = "g_cricket",
            playersCount = 85200,
            maxMultiplier = "Rs. 2 Crore First Prize",
            isHot = false
        ),
        GameItem(
            id = "g_teenpatti",
            title = "Teen Patti Clash",
            description = "Traditional three-card poker game built with private rooms and professional blind-matching systems.",
            category = GameCategory.CARDS,
            imageUrl = "g_teenpatti",
            playersCount = 24100,
            maxMultiplier = "15x Blind Pots",
            isHot = false
        ),
        GameItem(
            id = "g_slots",
            title = "Golden Pharaoh Spins",
            description = "Dynamic 5-reel cascading slots featuring scatter symbols, free spin locks, and high stake options.",
            category = GameCategory.CASINO,
            imageUrl = "g_slots",
            playersCount = 18900,
            maxMultiplier = "500x Bonus Round",
            isHot = true
        ),
        GameItem(
            id = "g_chess",
            title = "Bullet Chess League",
            description = "1v1 high speed matches. Outclass your opponent to take home the entry stake.",
            category = GameCategory.CASUAL,
            imageUrl = "g_chess",
            playersCount = 12400,
            maxMultiplier = "Double Stake",
            isHot = false
        ),
        // === 5 NEW GAMES ADDED ===
        GameItem(
            id = "g_ludo",
            title = "Ludo Empire",
            description = "Classic 4-player race. Roll dice, climb ladders, avoid snakes. First to home all tokens wins.",
            category = GameCategory.CASUAL,
            imageUrl = "g_ludo",
            playersCount = 67200,
            maxMultiplier = "10x Winnings",
            isHot = true
        ),
        GameItem(
            id = "g_carrom",
            title = "Carrom Pro League",
            description = "Master the striker. Pocket carrom men and the queen for huge payouts in this precision game.",
            category = GameCategory.CASUAL,
            imageUrl = "g_carrom",
            playersCount = 31400,
            maxMultiplier = "50x Winnings",
            isHot = false
        ),
        GameItem(
            id = "g_bubble",
            title = "Bubble Blast Royale",
            description = "Aim and shoot colored bubbles. Create chain reactions of 3+ matching bubbles to win big.",
            category = GameCategory.CASUAL,
            imageUrl = "g_bubble",
            playersCount = 28900,
            maxMultiplier = "25x Winnings",
            isHot = true
        ),
        GameItem(
            id = "g_snakes",
            title = "Snakes & Ladders Rush",
            description = "Pure luck & fun race to 100. Ladders boost you, snakes send you sliding back.",
            category = GameCategory.CASUAL,
            imageUrl = "g_snakes",
            playersCount = 15600,
            maxMultiplier = "8x Winnings",
            isHot = false
        ),
        GameItem(
            id = "g_andar",
            title = "Andar Bahar Elite",
            description = "Indian classic. The magic card lands on Andar or Bahar — place your bets and win instantly.",
            category = GameCategory.CARDS,
            imageUrl = "g_andar",
            playersCount = 45200,
            maxMultiplier = "30x Winnings",
            isHot = true
        )
    )

    // Dynamic Server Matches Generator
    fun getLiveTournaments(): List<TournamentMatch> {
        return listOf(
            TournamentMatch(
                id = "m_rummy_grand",
                title = "Mega Rummy Championship",
                gameTitle = "Rummy Royale 13",
                entryFee = 15.00,
                prizePool = 10000.00,
                startTime = "Starts in 4 mins",
                totalSlots = 1000,
                filledSlots = 845,
                category = GameCategory.CARDS,
                rules = listOf("13 Card Pool Rummy", "Safe play with absolute anti-fraud security", "Payout instantly after declaration")
            ),
            TournamentMatch(
                id = "m_aviator_blitz",
                title = "Aviator High-Rollers Gala",
                gameTitle = "Rocket Multiplier X",
                entryFee = 50.00,
                prizePool = 50000.00,
                startTime = "Starts in 12 mins",
                totalSlots = 500,
                filledSlots = 482,
                category = GameCategory.MULTIPLIER,
                rules = listOf("Instant Cashouts allowed", "Multiplier strictly seed-random verified", "Minimum auto cashout configurable")
            ),
            TournamentMatch(
                id = "m_cricket_ind",
                title = "India vs Aus Mega Contest",
                gameTitle = "Fantasy Premier Cricket",
                entryFee = 49.00,
                prizePool = 2500000.00,
                startTime = "Starts in 2 hours",
                totalSlots = 100000,
                filledSlots = 78920,
                category = GameCategory.FANTASY,
                rules = listOf("Create team with standard 100 credits", "Maximum 7 players from one country", "Captain earns 2x points")
            ),
            TournamentMatch(
                id = "m_teenpatti_mil",
                title = "Midnight Poker Classic",
                gameTitle = "Teen Patti Clash",
                entryFee = 25.00,
                prizePool = 8000.00,
                startTime = "Starts in 18 mins",
                totalSlots = 400,
                filledSlots = 192,
                category = GameCategory.CARDS,
                rules = listOf("Standard blind limit rules apply", "Side-show requests processed instantly", "No pre-meditated team play allowed")
            ),
            TournamentMatch(
                id = "m_chess_rapid",
                title = "Grandmaster Blitz Duel",
                gameTitle = "Bullet Chess League",
                entryFee = 10.00,
                prizePool = 200.00,
                startTime = "Starts in 30 secs",
                totalSlots = 20,
                filledSlots = 18,
                category = GameCategory.CASUAL,
                rules = listOf("3 mins + 2 seconds increment", "Strict anti-cheat background process", "Rundown counts on time forfeiture")
            ),
            // === NEW TOURNAMENTS FOR THE 5 ADDED GAMES ===
            TournamentMatch(
                id = "m_ludo_royal",
                title = "Ludo Royal Championship",
                gameTitle = "Ludo Empire",
                entryFee = 20.00,
                prizePool = 15000.00,
                startTime = "Starts in 8 mins",
                totalSlots = 500,
                filledSlots = 312,
                category = GameCategory.CASUAL,
                rules = listOf("Standard Ludo rules with 2 dice", "No killing own tokens", "Payouts on exact home entry")
            ),
            TournamentMatch(
                id = "m_carrom_night",
                title = "Carrom Night Masters",
                gameTitle = "Carrom Pro League",
                entryFee = 30.00,
                prizePool = 25000.00,
                startTime = "Starts in 15 mins",
                totalSlots = 200,
                filledSlots = 145,
                category = GameCategory.CASUAL,
                rules = listOf("Best of 3 boards", "Queen + 1 carrom man minimum to win", "Foul shots deduct points")
            ),
            TournamentMatch(
                id = "m_bubble_mania",
                title = "Bubble Mania Showdown",
                gameTitle = "Bubble Blast Royale",
                entryFee = 15.00,
                prizePool = 8000.00,
                startTime = "Starts in 5 mins",
                totalSlots = 800,
                filledSlots = 620,
                category = GameCategory.CASUAL,
                rules = listOf("3+ same color pops", "Chain reactions multiply score", "Limited 30 shots per round")
            ),
            TournamentMatch(
                id = "m_snakes_classic",
                title = "Snakes & Ladders Classic",
                gameTitle = "Snakes & Ladders Rush",
                entryFee = 10.00,
                prizePool = 5000.00,
                startTime = "Starts in 3 mins",
                totalSlots = 1000,
                filledSlots = 780,
                category = GameCategory.CASUAL,
                rules = listOf("Reach exactly 100", "Ladders help, snakes hurt", "Automatic dice rolls for speed")
            ),
            TournamentMatch(
                id = "m_andar_bahar",
                title = "Andar Bahar High Stakes",
                gameTitle = "Andar Bahar Elite",
                entryFee = 25.00,
                prizePool = 12000.00,
                startTime = "Starts in 6 mins",
                totalSlots = 300,
                filledSlots = 210,
                category = GameCategory.CARDS,
                rules = listOf("Magic card decides Andar or Bahar", "Instant resolution", "Double payout on perfect guess")
            )
        )
    }

    // ========================================================================
    // FANTASY CRICKET DATA
    // ========================================================================

    // Sample Teams
    private val teamIndia = CricketTeam(
        id = "team_ind",
        name = "India",
        shortName = "IND",
        logoUrl = ""
    )

    private val teamAustralia = CricketTeam(
        id = "team_aus",
        name = "Australia",
        shortName = "AUS",
        logoUrl = ""
    )

    private val teamMI = CricketTeam(
        id = "team_mi",
        name = "Mumbai Indians",
        shortName = "MI",
        logoUrl = ""
    )

    private val teamCSK = CricketTeam(
        id = "team_csk",
        name = "Chennai Super Kings",
        shortName = "CSK",
        logoUrl = ""
    )

    // Sample Players
    val fantasyCricketPlayers = listOf(
        // India Batters
        CricketPlayer("p1", "Rohit Sharma", teamIndia.id, PlayerRole.BATTER, 10.5, recentPoints = listOf(85, 92, 78, 105, 88)),
        CricketPlayer("p2", "Virat Kohli", teamIndia.id, PlayerRole.BATTER, 11.0, recentPoints = listOf(95, 110, 82, 98, 102)),
        CricketPlayer("p3", "Shubman Gill", teamIndia.id, PlayerRole.BATTER, 9.5, recentPoints = listOf(78, 85, 95, 72, 88)),
        // India Allrounders
        CricketPlayer("p4", "Hardik Pandya", teamIndia.id, PlayerRole.ALLROUNDER, 10.0, recentPoints = listOf(102, 88, 95, 110, 75)),
        CricketPlayer("p5", "Jadeja", teamIndia.id, PlayerRole.ALLROUNDER, 9.0, recentPoints = listOf(85, 78, 92, 80, 88)),
        // India Wicketkeeper
        CricketPlayer("p6", "Rishabh Pant", teamIndia.id, PlayerRole.WICKETKEEPER, 9.5, recentPoints = listOf(72, 88, 95, 82, 78)),
        // India Bowlers
        CricketPlayer("p7", "Jasprit Bumrah", teamIndia.id, PlayerRole.BOWLER, 10.0, recentPoints = listOf(95, 88, 102, 90, 85)),
        CricketPlayer("p8", "Mohammed Siraj", teamIndia.id, PlayerRole.BOWLER, 8.5, recentPoints = listOf(78, 82, 75, 88, 80)),
        CricketPlayer("p9", "Kuldeep Yadav", teamIndia.id, PlayerRole.BOWLER, 8.0, recentPoints = listOf(72, 85, 78, 80, 82)),
        CricketPlayer("p10", "Washington Sundar", teamIndia.id, PlayerRole.ALLROUNDER, 7.5, recentPoints = listOf(68, 72, 78, 65, 70)),

        // Australia Batters
        CricketPlayer("p11", "David Warner", teamAustralia.id, PlayerRole.BATTER, 10.0, recentPoints = listOf(88, 95, 82, 78, 90)),
        CricketPlayer("p12", "Steve Smith", teamAustralia.id, PlayerRole.BATTER, 10.5, recentPoints = listOf(95, 88, 102, 90, 95)),
        CricketPlayer("p13", "Marnus Labuschagne", teamAustralia.id, PlayerRole.BATTER, 9.0, recentPoints = listOf(78, 82, 88, 75, 80)),
        // Australia Allrounders
        CricketPlayer("p14", "Cameron Green", teamAustralia.id, PlayerRole.ALLROUNDER, 9.0, recentPoints = listOf(80, 85, 78, 82, 88)),
        CricketPlayer("p15", "Glenn Maxwell", teamAustralia.id, PlayerRole.ALLROUNDER, 9.5, recentPoints = listOf(95, 88, 100, 85, 92)),
        // Australia Wicketkeeper
        CricketPlayer("p16", "Alex Carey", teamAustralia.id, PlayerRole.WICKETKEEPER, 8.0, recentPoints = listOf(65, 70, 75, 68, 72)),
        // Australia Bowlers
        CricketPlayer("p17", "Pat Cummins", teamAustralia.id, PlayerRole.BOWLER, 10.0, recentPoints = listOf(90, 95, 88, 92, 85)),
        CricketPlayer("p18", "Josh Hazlewood", teamAustralia.id, PlayerRole.BOWLER, 9.0, recentPoints = listOf(82, 88, 78, 85, 80)),
        CricketPlayer("p19", "Adam Zampa", teamAustralia.id, PlayerRole.BOWLER, 8.5, recentPoints = listOf(75, 80, 78, 82, 75)),
        CricketPlayer("p20", "Mitchell Starc", teamAustralia.id, PlayerRole.BOWLER, 9.0, recentPoints = listOf(88, 85, 90, 78, 82))
    )

    // Sample Matches
    val fantasyCricketMatches = listOf(
        CricketMatch(
            id = "match_ind_aus_t20_1",
            team1 = teamIndia,
            team2 = teamAustralia,
            venue = "Wankhede Stadium, Mumbai",
            startTime = System.currentTimeMillis() + 7200000, // 2 hours from now
            status = MatchStatus.UPCOMING,
            contestCount = 25
        ),
        CricketMatch(
            id = "match_mi_csk_ipl_1",
            team1 = teamMI,
            team2 = teamCSK,
            venue = "MA Chidambaram Stadium, Chennai",
            startTime = System.currentTimeMillis() + 14400000, // 4 hours from now
            status = MatchStatus.UPCOMING,
            contestCount = 18
        )
    )

    // Sample Contests for a match
    fun getFantasyContestsForMatch(matchId: String): List<FantasyContest> {
        return listOf(
            FantasyContest(
                id = "contest_mega_1",
                matchId = matchId,
                name = "Mega League - ₹2 Crore Prize Pool",
                entryFee = 49.0,
                prizePool = 20000000.0,
                maxTeams = 100000,
                joinedTeams = 78500,
                prizeBreakdown = listOf(
                    PrizeTier(1..1, 5000000.0),
                    PrizeTier(2..2, 2500000.0),
                    PrizeTier(3..10, 1000000.0),
                    PrizeTier(11..100, 100000.0),
                    PrizeTier(101..1000, 10000.0),
                    PrizeTier(1001..10000, 1000.0),
                    PrizeTier(10001..50000, 200.0)
                ),
                rules = listOf(
                    "Maximum 7 players from one team",
                    "Captain gets 2x points",
                    "Vice-captain gets 1.5x points"
                )
            ),
            FantasyContest(
                id = "contest_h2h_1",
                matchId = matchId,
                name = "Head to Head - Beat 1 Opponent",
                entryFee = 20.0,
                prizePool = 36.0,
                maxTeams = 2,
                joinedTeams = 1,
                prizeBreakdown = listOf(
                    PrizeTier(1..1, 36.0)
                ),
                rules = listOf("Beat your opponent to win!")
            ),
            FantasyContest(
                id = "contest_practice_1",
                matchId = matchId,
                name = "Practice League - Free Entry",
                entryFee = 0.0,
                prizePool = 5000.0,
                maxTeams = 5000,
                joinedTeams = 3200,
                prizeBreakdown = listOf(
                    PrizeTier(1..1, 1000.0),
                    PrizeTier(2..10, 200.0),
                    PrizeTier(11..100, 20.0)
                ),
                rules = listOf("Free to join - Practice your skills!")
            ),
            FantasyContest(
                id = "contest_winner_1",
                matchId = matchId,
                name = "Winner Takes All - ₹50 Entry",
                entryFee = 50.0,
                prizePool = 45000.0,
                maxTeams = 1000,
                joinedTeams = 650,
                prizeBreakdown = listOf(
                    PrizeTier(1..1, 45000.0)
                ),
                rules = listOf("Only top rank gets the prize!")
            )
        )
    }

    // Get players for a specific match
    fun getPlayersForMatch(matchId: String): List<CricketPlayer> = fantasyCricketPlayers
}

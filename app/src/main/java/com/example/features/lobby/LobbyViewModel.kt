package com.example.features.lobby

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.FantasyTeamWithPlayers
import com.example.data.local.WalletBalanceEntity
import com.example.data.models.*
import com.example.data.repository.GamingRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

sealed interface LobbyUiState {
    object Loading : LobbyUiState
    data class Success(
        val allGames: List<GameItem>,
        val filteredGames: List<GameItem>,
        val liveTournaments: List<TournamentMatch>,
        val selectedCategory: GameCategory?,
        val searchQuery: String
    ) : LobbyUiState
    data class Error(val message: String) : LobbyUiState
}

class LobbyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GamingRepository(application)
    
    val fantasyMatches = repository.fantasyCricketMatches
    fun getFantasyContests(matchId: String) = repository.getFantasyContestsForMatch(matchId)
    fun getFantasyPlayers(matchId: String) = repository.getPlayersForMatch(matchId)

    private val _selectedCategory = MutableStateFlow<GameCategory?>(null)
    val selectedCategory: StateFlow<GameCategory?> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _lobbyUiState = MutableStateFlow<LobbyUiState>(LobbyUiState.Loading)
    val lobbyUiState: StateFlow<LobbyUiState> = _lobbyUiState.asStateFlow()

    // Real-time local balance updates from Room db
    val walletBalance: StateFlow<WalletBalanceEntity?> = repository.observeWalletBalance()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val transactions: StateFlow<List<WalletTransaction>> = repository.observeTransactions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Fantasy Teams (stored in Room)
    val fantasyTeams: StateFlow<List<FantasyTeamWithPlayers>> = repository.getAllFantasyTeams()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _teamPreviewDraft = MutableStateFlow<FantasyTeamDraft?>(null)
    val teamPreviewDraft: StateFlow<FantasyTeamDraft?> = _teamPreviewDraft.asStateFlow()

    // Fantasy participation (contestId -> joined)
    private val _joinedFantasyContests = MutableStateFlow<Set<String>>(emptySet())
    val joinedFantasyContests: StateFlow<Set<String>> = _joinedFantasyContests.asStateFlow()

    // KYC (persisted in DataStore so it survives process death)
    private val _kycStatus = MutableStateFlow(KYCStatus.NOT_SUBMITTED)
    val kycStatus: StateFlow<KYCStatus> = _kycStatus.asStateFlow()

    // User display name (persisted in DataStore)
    val userDisplayName: StateFlow<String> = repository.userNameFlow
        .map { it ?: "John Doe" }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "John Doe"
        )

    fun updateDisplayName(name: String) {
        val trimmed = name.trim()
        if (trimmed.isNotBlank()) {
            viewModelScope.launch { repository.saveUserName(trimmed) }
        }
    }

    // App settings (from DataStore)
    val appSettings: StateFlow<AppSettings> = repository.appSettingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    init {
        viewModelScope.launch {
            repository.checkAndSeedInitialData()
            loadLobbyData()
            // Restore persisted KYC status
            repository.kycStatusFlow.collect { status ->
                status?.let { _kycStatus.value = it }
            }
            // Initialize joined contests from fantasy teams
            repository.getAllFantasyTeams().collect { teams ->
                _joinedFantasyContests.value = teams.map { it.team.contestId }.toSet()
            }
        }
    }

    fun loadLobbyData() {
        viewModelScope.launch {
            _lobbyUiState.value = LobbyUiState.Loading
            try {
                // Simulate a slight network latency for a high-performance database sync click
                kotlinx.coroutines.delay(800)
                
                combine(_selectedCategory, _searchQuery) { category, query ->
                    val games = repository.gamesList
                    val filtered = games.filter { game ->
                        val matchesCategory = (category == null || game.category == category)
                        val matchesSearch = game.title.contains(query, ignoreCase = true) || 
                                           game.description.contains(query, ignoreCase = true)
                        matchesCategory && matchesSearch
                    }
                    val tournaments = repository.getLiveTournaments().filter { match ->
                        category == null || match.category == category
                    }
                    LobbyUiState.Success(
                        allGames = games,
                        filteredGames = filtered,
                        liveTournaments = tournaments,
                        selectedCategory = category,
                        searchQuery = query
                    )
                }.collectLatest { state ->
                    _lobbyUiState.value = state
                }
            } catch (e: Exception) {
                _lobbyUiState.value = LobbyUiState.Error(e.localizeMessage ?: "Failed to sync Grand Lobby servers.")
            }
        }
    }

    private val Throwable.localizeMessage: String?
        get() = this.message

    fun selectCategory(category: GameCategory?) {
        _selectedCategory.value = category
    }

    fun searchGames(query: String) {
        _searchQuery.value = query
    }

    // Interactive operations with callbacks
    fun addCash(amount: Double, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = repository.depositFunds(amount)
            onResult(result)
        }
    }

    fun withdrawCash(amount: Double, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = repository.withdrawFunds(amount)
            onResult(result)
        }
    }

    fun awardWinnings(amount: Double, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = repository.awardWinnings(amount)
            onResult(result)
        }
    }

    // Deduct a game entry stake before a round starts. Callers (game views / dispatcher)
    // must suspend and only start play when this succeeds.
    suspend fun deductStake(stake: Double, description: String): Result<Unit> =
        repository.deductEntryFee(stake, description)

    fun joinMatch(match: TournamentMatch, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = repository.registerForTournament(match)
            onResult(result)
        }
    }

    fun joinFantasyContest(
        matchId: String,
        contest: FantasyContest,
        teamId: String,
        captainId: String,
        viceCaptainId: String,
        players: List<CricketPlayer>,
        onResult: (Result<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val desc = "Fantasy Entry: ${contest.name} (${matchId})"
            val result = repository.joinFantasyContestAtomically(
                entryFee = contest.entryFee,
                description = desc,
                teamId = teamId,
                matchId = matchId,
                contestId = contest.id,
                captainId = captainId,
                viceCaptainId = viceCaptainId,
                players = players
            )
            onResult(result)
        }
    }

    fun setTeamPreviewDraft(draft: FantasyTeamDraft) {
        _teamPreviewDraft.value = draft
    }

    fun clearTeamPreviewDraft() {
        _teamPreviewDraft.value = null
    }

    fun submitKyc(onResult: (Result<KYCStatus>) -> Unit = {}) {
        viewModelScope.launch {
            _kycStatus.value = KYCStatus.PENDING
            repository.saveKycStatus(KYCStatus.PENDING)
            onResult(Result.success(KYCStatus.PENDING))

            // Simulate server verification delay
            kotlinx.coroutines.delay(5000)
            _kycStatus.value = KYCStatus.VERIFIED
            repository.saveKycStatus(KYCStatus.VERIFIED)
        }
    }

    fun updateSettings(newSettings: AppSettings) {
        viewModelScope.launch {
            repository.updateAppSettings(newSettings)
        }
    }

    // Played games history (stored in Room)
    val playedGames: StateFlow<List<PlayedGame>> = repository.observePlayedGames()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun recordGamePlayed(title: String, won: Boolean, amount: Double) {
        viewModelScope.launch {
            val resultStr = if (won) "WIN" else "LOSS"
            val entry = PlayedGame(
                id = "pg-${UUID.randomUUID()}",
                title = title,
                result = resultStr,
                amount = if (won) amount else 0.0,
                timestamp = System.currentTimeMillis()
            )
            repository.savePlayedGame(entry)
        }
    }
}

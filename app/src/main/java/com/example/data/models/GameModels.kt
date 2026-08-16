package com.example.data.models

enum class GameCategory(val displayName: String, val iconName: String) {
    FANTASY("Fantasy Sports", "sports_cricket"),
    CARDS("Card Games", "casino"),
    CASINO("Slots & Live", "playing_cards"),
    CASUAL("Casual Battles", "sports_esports"),
    MULTIPLIER("Crash Games", "trending_up")
}

data class GameItem(
    val id: String,
    val title: String,
    val description: String,
    val category: GameCategory,
    val imageUrl: String,
    val playersCount: Int,
    val maxMultiplier: String,
    val isHot: Boolean = false,
    val rating: Float = 4.8f
)

data class TournamentMatch(
    val id: String,
    val title: String,
    val gameTitle: String,
    val entryFee: Double,
    val prizePool: Double,
    val startTime: String,
    val totalSlots: Int,
    val filledSlots: Int,
    val category: GameCategory,
    val rules: List<String> = emptyList()
) {
    val slotsLeft: Int get() = totalSlots - filledSlots
    val fillPercentage: Float get() = filledSlots.toFloat() / totalSlots.toFloat()
}

enum class TransactionType {
    DEPOSIT, WITHDRAW, JOIN_FEE, WINNING_PAYOUT, REFERRAL_BONUS, CASHBACK
}

data class WalletTransaction(
    val id: String,
    val amount: Double,
    val type: TransactionType,
    val timestamp: Long = System.currentTimeMillis(),
    val description: String,
    val status: String = "SUCCESS" // SUCCESS, PENDING, FAILED
)

// User Profile Models
data class UserProfile(
    val id: String,
    val name: String,
    val email: String? = null,
    val phone: String,
    val avatarUrl: String? = null,
    val referralCode: String,
    val referredBy: String? = null,
    val kycStatus: KYCStatus = KYCStatus.NOT_SUBMITTED,
    val totalWinnings: Double = 0.0,
    val totalMatches: Int = 0,
    val winRate: Float = 0.0f,
    val level: Int = 1,
    val xp: Int = 0
)

enum class KYCStatus {
    NOT_SUBMITTED, PENDING, VERIFIED, REJECTED
}

// Referral System Models
data class Referral(
    val id: String,
    val referrerId: String,
    val referredUserId: String,
    val referredUserName: String,
    val status: ReferralStatus,
    val bonusEarned: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)

enum class ReferralStatus {
    PENDING, COMPLETED, FAILED
}

// Fantasy Sports Models
data class CricketMatch(
    val id: String,
    val team1: CricketTeam,
    val team2: CricketTeam,
    val venue: String,
    val startTime: Long,
    val status: MatchStatus,
    val contestCount: Int = 0
)

data class CricketTeam(
    val id: String,
    val name: String,
    val shortName: String,
    val logoUrl: String
)

data class CricketPlayer(
    val id: String,
    val name: String,
    val teamId: String,
    val role: PlayerRole,
    val credit: Double,
    val avatarUrl: String? = null,
    val recentPoints: List<Int> = emptyList()
)

enum class PlayerRole {
    BATTER, BOWLER, ALLROUNDER, WICKETKEEPER
}

enum class MatchStatus {
    UPCOMING, LIVE, COMPLETED, CANCELLED
}

data class FantasyTeam(
    val id: String,
    val userId: String,
    val matchId: String,
    val contestId: String,
    val players: List<CricketPlayer>,
    val captainId: String,
    val viceCaptainId: String,
    val totalPoints: Int = 0,
    val rank: Int? = null
)

data class FantasyTeamDraft(
    val teamId: String,
    val matchId: String,
    val contestId: String,
    val players: List<CricketPlayer>,
    val captainId: String,
    val viceCaptainId: String
)

data class FantasyContest(
    val id: String,
    val matchId: String,
    val name: String,
    val entryFee: Double,
    val prizePool: Double,
    val maxTeams: Int,
    val joinedTeams: Int,
    val prizeBreakdown: List<PrizeTier>,
    val rules: List<String> = emptyList()
)

data class PrizeTier(
    val rankRange: IntRange,
    val amount: Double,
    val percentage: Float? = null
)

// Leaderboard Models
data class LeaderboardEntry(
    val userId: String,
    val userName: String,
    val avatarUrl: String? = null,
    val rank: Int,
    val score: Int,
    val prize: Double? = null
)

// Help & Support Models
data class FAQItem(
    val id: String,
    val question: String,
    val answer: String,
    val category: FAQCategory
)

enum class FAQCategory {
    ACCOUNT, DEPOSIT, WITHDRAWAL, GAMEPLAY, KYC, REFERRAL, TECHNICAL
}

data class SupportTicket(
    val id: String,
    val userId: String,
    val subject: String,
    val description: String,
    val status: TicketStatus,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// For dynamic game history
data class PlayedGame(
    val id: String,
    val title: String,
    val result: String, // "WIN" or "LOSS"
    val amount: Double,
    val timestamp: Long = System.currentTimeMillis()
)

enum class TicketStatus {
    OPEN, IN_PROGRESS, RESOLVED, CLOSED
}

// Settings Models
data class AppSettings(
    val notificationsEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val darkMode: Boolean = true,
    val language: String = "en"
)

package com.example.util

data class ProfileLevelTier(
    val level: Int,
    val title: String,
    val minSessions: Int,
    val icon: String
)

data class ProfileLevelProgress(
    val allTiers: List<ProfileLevelTier>,
    val currentTier: ProfileLevelTier,
    val nextTier: ProfileLevelTier?,
    val totalSessions: Int,
    val progressFraction: Float,
    val remainingSessionsToNext: Int
)

object ProfileLevelHelper {

    fun getAllTiers(strings: AppStrings): List<ProfileLevelTier> = listOf(
        ProfileLevelTier(level = 1, title = strings.profileLevel1, minSessions = 0, icon = ""),
        ProfileLevelTier(level = 2, title = strings.profileLevel2, minSessions = 4, icon = ""),
        ProfileLevelTier(level = 3, title = strings.profileLevel3, minSessions = 10, icon = ""),
        ProfileLevelTier(level = 4, title = strings.profileLevel4, minSessions = 25, icon = ""),
        ProfileLevelTier(level = 5, title = strings.profileLevel5, minSessions = 50, icon = "")
    )

    fun computeProgress(totalSessions: Int, strings: AppStrings): ProfileLevelProgress {
        val safeSessions = totalSessions.coerceAtLeast(0)
        val tiers = getAllTiers(strings)
        val currentIndex = tiers.indexOfLast { safeSessions >= it.minSessions }.coerceAtLeast(0)
        val currentTier = tiers[currentIndex]
        val nextTier = tiers.getOrNull(currentIndex + 1)

        val (fraction, remaining) = if (nextTier == null) {
            1f to 0
        } else {
            val span = (nextTier.minSessions - currentTier.minSessions).coerceAtLeast(1)
            val gained = (safeSessions - currentTier.minSessions).coerceIn(0, span)
            (gained.toFloat() / span.toFloat()) to (nextTier.minSessions - safeSessions).coerceAtLeast(0)
        }

        return ProfileLevelProgress(
            allTiers = tiers,
            currentTier = currentTier,
            nextTier = nextTier,
            totalSessions = safeSessions,
            progressFraction = fraction,
            remainingSessionsToNext = remaining
        )
    }
}

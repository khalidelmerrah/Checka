<?php

class EloRating
{

    // K-Factor determines how volatile the rating is.
    // Standard Chess K-factors:
    // K=40 for new players (games < 30) - allows fast convergence
    // K=20 for normal players
    // K=10 for masters (rating > 2400) - stable logic

    public static function calculateNewRatings($ratingA, $ratingB, $actualScoreA, $gamesPlayedA = 20, $gamesPlayedB = 20)
    {

        $kFactorA = self::getKFactor($ratingA, $gamesPlayedA);
        $kFactorB = self::getKFactor($ratingB, $gamesPlayedB);

        $expectedA = self::getExpectedScore($ratingA, $ratingB);
        $expectedB = self::getExpectedScore($ratingB, $ratingA);

        // actualScoreA: 1 for Win, 0 for Loss, 0.5 for Draw
        // actualScoreB is inverse
        $actualScoreB = 1 - $actualScoreA;

        $newRatingA = $ratingA + $kFactorA * ($actualScoreA - $expectedA);
        $newRatingB = $ratingB + $kFactorB * ($actualScoreB - $expectedB);

        return [
            'newRatingA' => round($newRatingA),
            'newRatingB' => round($newRatingB),
            'changeA' => round($newRatingA - $ratingA),
            'changeB' => round($newRatingB - $ratingB)
        ];
    }

    private static function getExpectedScore($ratingA, $ratingB)
    {
        return 1 / (1 + pow(10, ($ratingB - $ratingA) / 400));
    }

    private static function getKFactor($rating, $gamesPlayed)
    {
        if ($gamesPlayed < 30) {
            return 40; // Placement games / New player
        }
        if ($rating > 2400) {
            return 10; // Grandmaster stability
        }
        return 20; // Standard
    }

    public static function getRankTitle($rating)
    {
        if ($rating < 1200)
            return "Novice";
        if ($rating < 1400)
            return "Apprentice";
        if ($rating < 1600)
            return "Adept";
        if ($rating < 1800)
            return "Expert";
        if ($rating < 2000)
            return "Master";
        if ($rating < 2200)
            return "Grandmaster";
        return "Legend";
    }
}
?>
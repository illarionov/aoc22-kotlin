import HandShape.*
import RoundResult.*

enum class HandShape(val shapeScore: Int) {
    ROCK(1),
    PAPER(2),
    SCISSORS(3)
}

enum class RoundResult(val score: Int) {
    LOST(0), DRAW(3), WON(6)
}

fun HandShape.roundResultAgainst(opponentShape: HandShape): RoundResult {
    return when (shapeScore - opponentShape.shapeScore) {
        0 -> DRAW
        1 -> WON
        2 -> LOST
        -1 -> LOST
        -2 -> WON
        else -> error("Wrong result")
    }
}

data class StrategyRecord(
    val elfShape: HandShape,
    val playerShape: HandShape
) {
    val roundResult: RoundResult
        get() = playerShape.roundResultAgainst(elfShape)
}

data class StrategyItemPart2(
    val elfShape: HandShape,
    val expectedResult: RoundResult
) {
    val expectedShape: HandShape get() = HandShape.values().first {
        it.roundResultAgainst(elfShape) == expectedResult
    }
}

fun String.toStrategy(): StrategyRecord {
    val res = this.split(" ")
    return StrategyRecord(res[0].toElfShape(), res[1].toPlayerShape())
}

fun String.toStrategy2(): StrategyItemPart2 {
    val res = this.split(" ")
    return StrategyItemPart2(res[0].toElfShape(), res[1].toExpectedResult())
}

fun String.toElfShape(): HandShape = when (this) {
    "A" -> ROCK
    "B" -> PAPER
    "C" -> SCISSORS
    else -> error("Unknown string $this")
}

fun String.toPlayerShape(): HandShape = when (this) {
    "X" -> ROCK
    "Y" -> PAPER
    "Z" -> SCISSORS
    else -> error("Unknown string $this")
}

fun String.toExpectedResult(): RoundResult = when (this) {
    "X" -> LOST
    "Y" -> DRAW
    "Z" -> WON
    else -> error("Unknown string $this")
}

fun main() {
    fun part1(input: List<StrategyRecord>): Int {
        return input.sumOf { s -> s.roundResult.score + s.playerShape.shapeScore }
    }

    fun part2(input: List<StrategyItemPart2>): Int {
        return input.sumOf { s ->
            s.expectedResult.score + s.expectedShape.shapeScore
        }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day02_test")
        .map(String::toStrategy)
    check(part1(testInput) == 15)

    val input = readInput("Day02")
        .map(String::toStrategy)

    val totalScore1 = part1(input)
    //check(totalScore1 == 13682)
    println(totalScore1)

    val input2 = readInput("Day02")
        .map(String::toStrategy2)
    val totalScore2 = part2(input2)
    //check(totalScore2 == 12881)
    println(totalScore2)
}

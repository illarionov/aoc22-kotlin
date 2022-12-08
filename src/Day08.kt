fun main() {
    fun part1(input: List<List<Int>>): Int {
        return input.indices.sumOf { y ->
            input[y].indices.count { x ->
                input.isVisible(x, y)
            }
        }
    }

    fun part2(input: List<List<Int>>): Int {
        return input.indices.maxOf { y ->
            input[y].indices.maxOf { x ->
                input.scenicScore(x, y)
            }
        }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day08_test")
        .parseDigits()
    val part1TestResult = part1(testInput)
    println(part1TestResult)
    check(part1TestResult == 21)

    val input = readInput("Day08")
        .parseDigits()
    val part1Result = part1(input)
    println(part1Result)
    //check(part1Result == 1672)

    val part2Result = part2(input)
    println(part2Result)
    //check(part2Result == 327180)
}

fun List<List<Int>>.isVisible(x: Int, y: Int): Boolean {
    val checkedValue = this[y][x]
    val maxX = this[0].lastIndex
    val maxY = this.lastIndex
    return (0 until x).all { l -> this[y][l] < checkedValue }
            || (0 until y).all { t -> this[t][x] < checkedValue }
            || (x + 1..maxX).all { r -> this[y][r] < checkedValue }
            || (y + 1..maxY).all { b -> this[b][x] < checkedValue }
}

fun List<List<Int>>.scenicScore(x: Int, y: Int): Int {
    val checkedValue = this[y][x]
    val maxX = this[0].lastIndex
    val maxY = this.lastIndex

    val visibleLeft = (x - 1 downTo 0)
        .countUntil { l -> this[y][l] >= checkedValue }
    val visibleRight = (x + 1..maxX)
        .countUntil { r -> this[y][r] >= checkedValue }
    val visibleTop = (y - 1 downTo 0)
        .countUntil { t -> this[t][x] >= checkedValue }
    val visibleBottom = (y + 1..maxY)
        .countUntil { b -> this[b][x] >= checkedValue }

    return visibleLeft * visibleRight * visibleTop * visibleBottom
}

fun List<String>.parseDigits(): List<List<Int>> {
    return map { s -> s.map { c -> c - '0' } }
}

inline fun <T> Iterable<T>.countUntil(predicate: (T) -> Boolean): Int {
    var count = 0
    for (item in this) {
        count += 1
        if (predicate(item))
            break
    }
    return count
}
fun main() {
    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day14_test")
        .parseScanReport()
    val testInputResult = part1(testInput)
    println(testInputResult)
    check(testInputResult == 24)

    val input = readInput("Day14")
        .parseScanReport()
    val part1Result = part1(input)
    println(part1Result)
    //check(part1Result == 832)

    val testInputResult2 = part2(testInput)
    println(testInputResult2)
    //check(testInputResult2 == 93)

    val part2Result = part2(input)
    println(part2Result)
    //check(part2Result == 27601)
}

private const val FIELD_SIZE: Int = 1000

private enum class Point {
    AIR, ROCK, SAND
}

private fun part1(input: List<List<Point>>): Int {
    val field: List<MutableList<Point>> = input.copy()

    var c = 0
    var restPoint = getNextRestPoint(field)
    while (restPoint != null) {
        c += 1
        field[restPoint.second][restPoint.first] = Point.SAND
        restPoint = getNextRestPoint(field)
    }

    return c
}

private fun part2(input: List<List<Point>>): Int {
    val field: List<MutableList<Point>> = input.copy()

    val maxY = (field.size - 1 downTo 0)
        .takeWhile { i ->
            field[i].all { it == Point.AIR }
        }
        .last() + 1
    field[maxY].indices.forEach { x -> field[maxY][x] = Point.ROCK }

    var c = 0
    var restPoint2 = getNextRestPoint(field)
    while (restPoint2 != null) {
        c += 1
        field[restPoint2.second][restPoint2.first] = Point.SAND
        restPoint2 = getNextRestPoint(field)
    }

    return c
}

private val directions = listOf(
    0 to +1,
    -1 to +1,
    +1 to +1
)

private fun getNextRestPoint(field: List<List<Point>>): Pair<Int, Int>? {
    var x = 500
    var y = 0
    if (field[y][x] != Point.AIR) return null

    while (y < field.lastIndex) {
        val d = directions.firstOrNull { d ->
            field[y + d.second][x + d.first] == Point.AIR
        } ?: break
        x += d.first
        y += d.second
    }

    return if (y < field.lastIndex) x to y else null
}


private fun String.parsePath(): List<Pair<Int, Int>> {
    return this.split(""" -> """)
        .map { p ->
            val (start, length) = p.split(",")
            start.toInt() to length.toInt()
        }
}

private fun List<List<Point>>.copy(): List<MutableList<Point>> {
    return List(FIELD_SIZE) { y -> this[y].toMutableList() }
}

private fun List<String>.parseScanReport(): List<List<Point>> {
    val field = List(FIELD_SIZE) { MutableList(FIELD_SIZE) { Point.AIR } }

    fun range(v1: Int, v2: Int): IntRange {
        return if (v1 < v2) v1..v2 else v2..v1
    }

    this
        .map(String::parsePath)
        .forEach { path ->
            path.zipWithNext()
                .forEach { (pred, next) ->
                    for (y in range(pred.second, next.second)) {
                        for (x in range(pred.first, next.first)) {
                            field[y][x] = Point.ROCK
                        }
                    }
                }
        }
    return field
}

private fun List<List<Point>>.printField() {
    for (y in 0..12) {
        for (x in 490..506) {
            val p = when (this[y][x]) {
                Point.AIR -> '.'
                Point.ROCK -> '#'
                Point.SAND -> 'o'
            }
            print(p)
        }
        println()
    }
}


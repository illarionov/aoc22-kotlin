private class Highmap(
    val area: List<List<Int>>,
    val startPosition: Position,
    val endPosition: Position
) {
    val width: Int
        get() = area[0].size
    val height: Int
        get() = area.size

    fun elevationAt(pos: Position): Int {
        return area[pos.y][pos.x]
    }

    fun hasPathBetween(from: Position, to: Position): Boolean {
        return from.isInArea()
                && to.isInArea()
                && elevationAt(from) + 1 >= elevationAt(to)
    }

    private fun Position.isInArea(): Boolean {
        return this.x in (0 until width)
                && this.y in (0 until height)
    }
}

private data class Position(val x: Int, val y: Int) {
    override fun toString(): String {
        return "[$x,$y]"
    }
}

fun main() {
    fun part1(input: Highmap): Int {
        return calculateMinStepsMatrix(input)[input.startPosition]
    }

    fun part2(input: Highmap): Int {
        val minStepsMatrix = calculateMinStepsMatrix(input)
        return input.area.flatMapIndexed { y, ints ->
            ints.indices.filter { x -> ints[x] == 0 }
                .map { x -> minStepsMatrix[y][x] }
        }.min()
    }

    val testInput = readInput("Day12_test")
        .parseHighmap()
    val testResult = part1(testInput)
    check(testResult == 31)

    val input = readInput("Day12")
        .parseHighmap()

    val part1Result = part1(input)
    println(part1Result)
    //check(part1Result == 462)

    val part2Result = part2(input)
    println(part2Result)
    //check(part2Result == 451)
}

private fun calculateMinStepsMatrix(input: Highmap): List<List<Int>> {
    val dequeue: ArrayDeque<Pair<Position, Int>> = ArrayDeque()
    val minStepsMatrix: List<MutableList<Int>> = MutableList(input.height) { MutableList(input.width) { Int.MAX_VALUE } }

    dequeue.addLast(input.endPosition to 0);
    while (!dequeue.isEmpty()) {
        val (to, depth) = dequeue.removeFirst()

        if (minStepsMatrix[to] <= depth) {
            continue
        } else {
            minStepsMatrix[to] = depth
        }

        listOf(
            to.copy(x = to.x - 1),
            to.copy(x = to.x + 1),
            to.copy(y = to.y - 1),
            to.copy(y = to.y + 1)
        ).forEach { fromPos ->
            if (input.hasPathBetween(fromPos, to)) {
                dequeue.addLast(fromPos to (depth + 1))
            }
        }
    }
    return minStepsMatrix
}
private operator fun List<List<Int>>.get(from: Position): Int {
    return this[from.y][from.x]
}

private operator fun List<MutableList<Int>>.set(from: Position, value: Int) {
    this[from.y][from.x] = value
}

private fun List<String>.parseHighmap(): Highmap {
    var startPos: Position? = null
    var endPos: Position? = null

    val highMap: List<List<Int>> = this.mapIndexed { y, s ->
        s.mapIndexed i@{ x, c ->
            when (c) {
                'S' -> {
                    startPos = Position(x, y)
                    return@i 0
                }
                'E' -> {
                    endPos = Position(x, y)
                    return@i 'z' - 'a'
                }
                else -> {
                    return@i c - 'a'
                }
            }
        }
    }
    return Highmap(highMap, startPos!!, endPos!!)
}
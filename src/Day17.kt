import CHAMBER_UNIT.*

fun main() {
    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day17_test")
        .single()
        .parseMovs()
    val part1TestResult = part1(testInput)
    println("Result on test input: $part1TestResult")
    check(part1TestResult == 3068L)

    val input = readInput("Day17")
        .single()
        .parseMovs()
    val part1Result = part1(input)
    println(part1Result)
    check(part1Result == 3161L)

    val part2TestResult = part2(testInput)
    println(part2TestResult)
    check(part2TestResult == 1514285714288)
    //check(part2TestResult == 3068L)

    val part2Result = part2(input)
    println(part2Result)
}

const val CHAMBER_WIDTH = 7

private enum class ACTION {
    FALLING, GAS_PUSH
}

private class Chamber {
    val chamber: ArrayDeque<MutableList<CHAMBER_UNIT>> = ArrayDeque()
    var x: Int = 0
    var y: Int = 0
    var removedBottom = 0L

    fun startShape(shape: Shape) {
        repeat(3 + shape.height) {
            chamber.addFirst(MutableList(CHAMBER_WIDTH) { E })
        }
        x = 2
        y = 0
    }

    fun drawShape(shape: Shape, x: Int, y: Int, u: CHAMBER_UNIT = R) {
        shape.mask.forEachIndexed { dy, chamberUnits ->
            chamberUnits.forEachIndexed { dx, chamberUnit ->
                if (chamberUnit == R) {
                    chamber[y + dy][x + dx] = u
                }
            }
        }
    }


    fun isShapeCanBeMovedToPosition(shape: Shape, x: Int, y: Int): Boolean {
        if (x < 0 || x + shape.width > CHAMBER_WIDTH) {
            return false
        }
        if (y < 0 || y + shape.height > chamber.size) {
            return false
        }

        for (dy in shape.mask.indices) {
            for (dx in shape.mask[dy].indices) {
                if (shape.mask[dy][dx] == R
                    && chamber[y + dy][x + dx] == REST_ROCK
                ) {
                    return false
                }
            }
        }
        return true
    }

    fun getOutline(): List<Int> {
        return (0 until CHAMBER_WIDTH).map { x ->
            val depth = (0 until chamber.size).takeWhile { y ->
                chamber[y][x] == E
            }.count()
            depth
        }
    }
}

private fun Chamber.draw() {
    this.chamber.forEach { u ->
        println(u.joinToString("") { c ->
            when (c) {
                E -> "."
                REST_ROCK -> "#"
                R -> "@"
            }
        })
    }
}

private fun generateChamber(input: List<Mov>, rocksCount: Long = 2022): Chamber {
    val chamber = Chamber()
    var inputPos = 0
    val shapes = Shape.values()
    var action = ACTION.GAS_PUSH
    for (rockNo in 0 until rocksCount) {
        val shape = shapes[(rockNo % shapes.size.toLong()).toInt()]
        chamber.startShape(shape)
        while (true) {
            when (action) {
                ACTION.FALLING -> {
                    action = ACTION.GAS_PUSH
                    val testY = chamber.y + 1
                    if (chamber.isShapeCanBeMovedToPosition(shape, chamber.x, testY)) {
                        chamber.y = testY
                    } else {
                        break
                    }
                }

                ACTION.GAS_PUSH -> {
                    val testX = chamber.x + input[inputPos].dx
                    inputPos = (inputPos + 1) % input.size
                    action = ACTION.FALLING
                    if (chamber.isShapeCanBeMovedToPosition(shape, testX, chamber.y)) {
                        chamber.x = testX
                    }
                }
            }
        }
        chamber.drawShape(shape, chamber.x, chamber.y, REST_ROCK)
        while (chamber.chamber[0].all { it == E }) {
            chamber.chamber.removeFirst()
            chamber.y -= 1
        }
    }

    return chamber
}

private fun part1(input: List<Mov>, rocksCount: Long = 2022): Long {
    val chamber = generateChamber(input, rocksCount)
    return chamber.chamber.size.toLong() + chamber.removedBottom
}

private fun part2(input: List<Mov>, rocksCount: Long = 1000000000000): Long {

    data class CacheKey(
        val currentShape: Shape,
        val inputPos: Int,
        val outline: List<Int>
    )

    data class CacheValue(
        val rockNo: Long,
        val height: Long
    )

    val seenOutlines: MutableMap<CacheKey, CacheValue> = mutableMapOf()

    val chamber = Chamber()
    var inputPos = 0
    val shapes = Shape.values()
    var action = ACTION.GAS_PUSH
    var rockNo = 0L
    var totalHeight = 0L
    var lastChamberSize = 0
    while (rockNo < rocksCount) {
        val shape = shapes[(rockNo % shapes.size.toLong()).toInt()]
        chamber.startShape(shape)
        val currentPos = inputPos
        while (true) {
            when (action) {
                ACTION.FALLING -> {
                    action = ACTION.GAS_PUSH
                    val testY = chamber.y + 1
                    if (chamber.isShapeCanBeMovedToPosition(shape, chamber.x, testY)) {
                        chamber.y = testY
                    } else {
                        break
                    }
                }

                ACTION.GAS_PUSH -> {
                    val testX = chamber.x + input[inputPos].dx
                    inputPos = (inputPos + 1) % input.size
                    action = ACTION.FALLING
                    if (chamber.isShapeCanBeMovedToPosition(shape, testX, chamber.y)) {
                        chamber.x = testX
                    }
                }
            }
        }
        chamber.drawShape(shape, chamber.x, chamber.y, REST_ROCK)
        while (chamber.chamber[0].all { it == E }) {
            chamber.chamber.removeFirst()
            chamber.y -= 1
        }
        rockNo += 1

        totalHeight += (chamber.chamber.size - lastChamberSize)
        lastChamberSize = chamber.chamber.size

        val outline = chamber.getOutline()
        val cacheKey = CacheKey(
            currentShape = shape,
            inputPos = currentPos,
            outline = outline
        )
        if (cacheKey in seenOutlines) {
            val startCycle = seenOutlines[cacheKey]!!
            val rocksPerCycle = rockNo - startCycle.rockNo
            val heightPerCycle = chamber.chamber.size - startCycle.height

            val rocksLeft = rocksCount - rockNo
            val fullCyclesLeft = rocksLeft / rocksPerCycle

            if (fullCyclesLeft > 0) {
                println(
                    "Found cycle. Old height: $startCycle, current height: $totalHeight\n"
                            + "rocks per cycle: $rocksPerCycle heightPerCycle: $heightPerCycle rocksLeft: $rocksLeft "
                            + "full cycles left: $fullCyclesLeft"
                )
                rockNo += fullCyclesLeft * rocksPerCycle
                totalHeight += fullCyclesLeft * heightPerCycle
            }
        } else {
            seenOutlines[cacheKey] = CacheValue(rockNo = rockNo, height = totalHeight)
        }
    }

    return totalHeight
}

private enum class Mov(val dx: Int) {
    LEFT(-1), RIGHT(1)
}

private enum class Shape(val mask: List<List<CHAMBER_UNIT>>) {

    SH1(
        listOf(
            listOf(R, R, R, R)
        )
    ),
    SH2(
        listOf(
            listOf(E, R, E),
            listOf(R, R, R),
            listOf(E, R, E),
        )
    ),
    SH3(
        listOf(
            listOf(E, E, R),
            listOf(E, E, R),
            listOf(R, R, R)
        )
    ),
    SH4(
        listOf(
            listOf(R),
            listOf(R),
            listOf(R),
            listOf(R),
        )
    ),
    SH5(
        listOf(
            listOf(R, R),
            listOf(R, R),
        )
    );

    val width
        get() = this.mask[0].size
    val height
        get() = this.mask.size
}

private enum class CHAMBER_UNIT {
    E, REST_ROCK, R
}

private fun String.parseMovs(): List<Mov> {
    return this.map {
        when (it) {
            '<' -> Mov.LEFT
            '>' -> Mov.RIGHT
            else -> error("Unknown symbol $it")
        }
    }
}
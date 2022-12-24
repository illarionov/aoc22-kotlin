import Day24.Blizzard
import Day24.Direction.*
import Day24.Valley
import Day24.XY

fun main() {
    val testInput = readInput("Day24_test")
        .parseValley()
    val input = readInput("Day24")
        .parseValley()

    part1(testInput).also {
        println("Part 1, test input: $it")
        check(it == 18)
    }

    part1(input).also {
        println("Part 1, real input: $it")
        check(it == 334)
    }

    part2(testInput).also {
        println("Part 2, test input: $it")
        check(it == 54)
    }

    part2(input).also {
        println("Part 2, real input: $it")
        check(it == 934)
    }
}

private object Day24 {
    data class XY(val x: Int, val y: Int)

    enum class Direction { UP, DOWN, LEFT, RIGHT }

    private val Direction.printChar: Char
        get() = when (this) {
            UP -> '^'
            DOWN -> 'v'
            LEFT -> '<'
            RIGHT -> '>'
        }

    data class Blizzard(
        val xy: XY,
        val direction: Direction
    )

    class Valley(
        val width: Int,
        val height: Int,
        val blizzards: List<Blizzard>,
        val initialPosition: XY,
        val endPosition: XY
    ) {
        private val blizzardsMapCache: MutableMap<Int, Set<XY>> = mutableMapOf()

        fun getBlizzardsMap(pMinute: Int): Set<XY> {
            val minute = pMinute % (width * height)
            return blizzardsMapCache.getOrPut(minute) {
                blizzards.mapTo(mutableSetOf()) { b ->
                    val xy = when (b.direction) {
                        UP -> b.xy.copy(y = (b.xy.y - minute).mod(height))
                        DOWN -> b.xy.copy(y = (b.xy.y + minute).mod(height))
                        LEFT -> b.xy.copy(x = (b.xy.x - minute).mod(width))
                        RIGHT -> b.xy.copy(x = (b.xy.x + minute).mod(width))
                    }
                    xy
                }
            }
        }

        fun print() {
            val blizzardsGroup = blizzards.groupBy { it.xy }
            val enter = " ".repeat(initialPosition.x) + "E"
            println(enter)
            (0 until height).forEach { y ->
                val s = (0 until width).map { x ->
                    blizzardsGroup[XY(x, y)]?.let {
                        if (it.size == 1) {
                            it[0].direction.printChar
                        } else {
                            it.size.digitToChar(64)
                        }
                    } ?: '.'
                }.joinToString("")
                println(s)
            }

            val exit = " ".repeat(endPosition.x) + "L"
            println(exit)
        }
    }
}

private fun part1(valley: Valley): Int {
    return getMinimalTimePath(valley, 0)
}

private fun part2(input: Valley): Int {
    val timeFirstTrip = getMinimalTimePath(input, 0)
    println("Time first trip: $timeFirstTrip")

    val valleyBack = Valley(
        width = input.width,
        height = input.height,
        blizzards = input.blizzards,
        initialPosition = input.endPosition,
        endPosition = input.initialPosition
    )
    val timeBack = getMinimalTimePath(valleyBack, timeFirstTrip)
    println("Time back: $timeBack")

    val timeSecondTrip = getMinimalTimePath(input, timeBack)
    println("Time second trip: $timeSecondTrip")

    return timeSecondTrip
}

private fun getMinimalTimePath(valley: Valley, startMinute: Int): Int {
    data class State(
        val minute: Int,
        val xy: XY,
    )

    data class CacheKey(
        val xy: XY,
        val blizzardOffset: Int
    )

    var bestTime = Int.MAX_VALUE
    val cache: MutableMap<CacheKey, Int> = mutableMapOf()
    val q: ArrayDeque<State> = ArrayDeque()
    q.addLast(
        State(
            minute = startMinute,
            xy = valley.initialPosition,
        )
    )

    fun enqueueIfApplicable(newMinute: Int, newXY: XY) {
        if (newMinute >= bestTime
        ) {
            return
        }
        if (newXY == valley.endPosition) {
            //println("Found path in ${newMinute} minutes")
            bestTime = newMinute
            return
        }
        if ((newXY.x !in 0 until valley.width
                    || newXY.y !in 0 until valley.height)
            && (newXY != valley.initialPosition)
        ) {
            return
        }
        val blizzardSet = valley.getBlizzardsMap(newMinute)
        if (newXY in blizzardSet) {
            return
        }

        val cacheKey = CacheKey(
            xy = newXY,
            blizzardOffset = newMinute % (valley.width * valley.height)
        )

        cache[cacheKey]?.let { k ->
            if (k <= newMinute) {
                return
            }
        }
        cache[cacheKey] = newMinute

        q.addLast(
            State(
                minute = newMinute,
                xy = newXY,
            )
        )
    }

    while (!q.isEmpty()) {
        val s = q.removeFirst()
        // DOWN
        enqueueIfApplicable(
            newMinute = s.minute + 1,
            newXY = s.xy.copy(y = s.xy.y + 1)
        )
        // RIGHT
        enqueueIfApplicable(
            newMinute = s.minute + 1,
            newXY = s.xy.copy(x = s.xy.x + 1)
        )
        // UP
        enqueueIfApplicable(
            newMinute = s.minute + 1,
            newXY = s.xy.copy(y = s.xy.y - 1)
        )
        // LEFT
        enqueueIfApplicable(
            newMinute = s.minute + 1,
            newXY = s.xy.copy(x = s.xy.x - 1)
        )

        // WAIT
        enqueueIfApplicable(
            newMinute = s.minute + 1,
            newXY = s.xy
        )
    }

    return bestTime
}

private fun List<String>.parseValley(): Valley {
    val initialX = this[0].indexOf('.') - 1
    val endX = this.last().indexOf('.') - 1

    val width = this[0].length - 2
    val height = this.size - 2

    val blizzards: List<Blizzard> = this.flatMapIndexed { y: Int, s: String ->
        s.mapIndexedNotNull { x, c ->
            val xy = XY(x - 1, y - 1)
            when (c) {
                '<' -> Blizzard(xy, LEFT)
                '>' -> Blizzard(xy, RIGHT)
                '^' -> Blizzard(xy, UP)
                'v' -> Blizzard(xy, DOWN)
                else -> null
            }
        }
    }
    return Valley(
        width = width,
        height = height,
        blizzards = blizzards,
        initialPosition = XY(initialX, -1),
        endPosition = XY(endX, height)
    )
}
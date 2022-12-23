import Day23.Direction

fun main() {
    val testInput = readInput("Day23_test")
        .parseGround()
    val input = readInput("Day23")
        .parseGround()

    part1(testInput).also {
        println("Part 1, test input: $it")
        check(it == 110)
    }

    part1(input).also {
        println("Part 1, real input: $it")
        check(it == 3862)
    }

    part2(testInput).also {
        println("Part 2, test input: $it")
        check(it == 20)
    }

    part2(input).also {
        println("Part 2, real input: $it")
        check(it == 913)
    }
}

object Day23 {
    enum class Direction(val dxdy: XY, val adjacent: List<XY>) {
        N(
            dxdy = XY(0, -1),
            adjacent = listOf(
                XY(-1, -1),
                XY(0, -1),
                XY(1, -1)
            )
        ),
        S(
            dxdy = XY(0, 1),
            adjacent = listOf(
                XY(-1, 1),
                XY(0, 1),
                XY(1, 1),
            )
        ),
        W(
            dxdy = XY(-1, 0),
            adjacent = listOf(
                XY(-1, -1),
                XY(-1, 0),
                XY(-1, 1),
            ),
        ),
        E(
            dxdy = XY(1, 0),
            adjacent = listOf(
                XY(1, -1),
                XY(1, 0),
                XY(1, 1),
            ),
        );

        private val all by lazy { enumValues<Direction>().toList() }
        fun directionsFrom(): List<Direction> {
            val p = all.indexOf(this)
            return all.subList(p, all.size) +
                    (if (p != 0) all.subList(0, p) else emptyList())
        }
    }

    data class XY(val x: Int, val y: Int) {
        fun to(d: Direction): XY = XY(d.dxdy.x + this.x, d.dxdy.y + y)
    }

    class Field(tiles: List<List<Boolean>>) {
        val elves: MutableSet<XY> = tiles.flatMapIndexed { y, booleans ->
            booleans.mapIndexed { x, b ->
                if (b == true) {
                    XY(x, y)
                } else {
                    null
                }
            }.filterNotNull()
        }.toMutableSet()

        fun move(direction: Direction): Boolean {
            val directions = direction.directionsFrom()
            val newPositions: MutableMap<XY, Direction> = mutableMapOf()
            var fieldChanged = false
            elves.forEach { e ->
                val hasElvesAround = directions
                    .any { d ->
                        d.adjacent.any { a ->
                            val p = XY(a.x + e.x, a.y + e.y)
                            p in elves
                        }
                    }
                if (!hasElvesAround) {
                    return@forEach
                }
                fieldChanged = true

                val newDirection = directions.firstOrNull { d ->
                    d.adjacent.all {
                        val p = XY(it.x + e.x, it.y + e.y)
                        p !in elves
                    }
                }
                if (newDirection != null) {
                    newPositions[e] = newDirection
                }
            }
            val collisions: Map<XY, Int> = newPositions.map { (pos, direction) ->
                pos.to(direction)
            }.groupingBy { it }.eachCount()

            newPositions.forEach { (pos, direction) ->
                val newPosition = pos.to(direction)
                if (collisions[newPosition] == 1) {
                    check(newPosition !in elves)
                    elves -= pos
                    elves += newPosition
                }
            }
            return fieldChanged
        }

        fun getBoundingBoxWithElves(): Pair<XY, XY> {
            val leftX = elves.minOf { it.x }
            val rightX = elves.maxOf { it.x }
            val topY = elves.minOf { it.y }
            val bottomY = elves.maxOf { it.y }
            return XY(leftX, topY) to XY(rightX, bottomY)
        }

        fun printField() {
            val bbox = getBoundingBoxWithElves()
            (bbox.first.y..bbox.second.y).forEach { y ->
                val s = (bbox.first.x..bbox.second.x).map { x ->
                    if (XY(x, y) in elves) '#' else '.'
                }.joinToString("")
                println(s)
            }
        }

        fun countEmptyPositions(): Int {
            val bbox = getBoundingBoxWithElves()
            return (bbox.second.y - bbox.first.y + 1) *
                    (bbox.second.x - bbox.first.x + 1) - elves.size
        }
    }
}

private fun part1(input: List<List<Boolean>>): Int {
    val field = Day23.Field(tiles = input)
    var direction = Direction.N
    repeat(10) {
        field.move(direction)
        direction = direction.directionsFrom()[1]
    }
    return field.countEmptyPositions()
}

private fun part2(input: List<List<Boolean>>): Int {
    val field = Day23.Field(tiles = input)
    var direction = Direction.N
    var fieldChanged: Boolean
    var round = 0
    do {
        fieldChanged = field.move(direction)
        round += 1
        direction = direction.directionsFrom()[1]
    } while (fieldChanged)

    return round
}

private fun List<String>.parseGround(): List<List<Boolean>> {
    return this.map { s ->
        s.map { it == '#' }
    }
}
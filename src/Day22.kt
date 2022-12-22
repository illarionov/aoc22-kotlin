fun main() {
    val testInput = readInput("Day22_test")
    val input = readInput("Day22")

    part1(testInput).also {
        println("Part 1, test input: $it")
        check(it == 6032)
    }

    part1(input).also {
        println("Part 1, real input: $it")
        check(it == 146092)
    }

//    part2(testInput).also {
//        println("Part 2, test input: $it")
//        check(it == 5031)
//    }

    part2(input).also {
        println("Part 2, real input: $it")
        check(it == 110342)
    }
}

private class Board(
    val tiles: List<List<Tile>>,
    val position: Pos,
    val path: List<PathItem>
)

private class Cube(
    val facets: Map<Int, List<List<Tile>>>,
    val position: PosOnCube,
    val path: List<PathItem>
) {
    val facetSize = facets[0]!!.size

    fun printFacet(id: Int) {
        println("Faced $id")
        facets[id]!!.forEach { l ->
            val s = l.map {
                when (it) {
                    Tile.EMPTY -> ' '
                    Tile.OPEN -> '.'
                    Tile.WALL -> '#'
                }
            }.joinToString("")
            println(s)
        }
    }
}

private sealed class Tile {
    object EMPTY : Tile()
    object WALL : Tile()
    object OPEN : Tile()
}

private data class Pos(
    val x: Int, val y: Int, val facing: Facing
)

private data class PosOnCube(
    val facedId: Int, val x: Int, val y: Int, val facing: Facing
)

enum class Facing(val passwordValue: Int) {
    RIGHT(0), DOWN(1), LEFT(2), UP(3);

    fun clockwiseNext(): Facing {
        return when (this) {
            RIGHT -> DOWN
            DOWN -> LEFT
            LEFT -> UP
            UP -> RIGHT
        }
    }

    fun counterClockwiseNext(): Facing {
        return when (this) {
            RIGHT -> UP
            DOWN -> RIGHT
            LEFT -> DOWN
            UP -> LEFT
        }
    }
}

sealed class PathItem {
    data class Movement(val numTiles: Int) : PathItem()
    data class Rotating(val clockwise: Boolean) : PathItem()
}


private fun part1(input: List<String>): Int {
    val board = input.parseInputBoard()
    var x = board.position.x
    var y = board.position.y
    var facing = board.position.facing
    board.path.forEach { p ->
        when (p) {
            is PathItem.Movement -> {
                repeat(p.numTiles) {
                    when (facing) {
                        Facing.RIGHT -> {
                            var testX = x + 1
                            if (testX >= board.tiles[y].size
                                || board.tiles[y][testX] == Tile.EMPTY
                            ) {
                                testX = 0
                                while (testX < board.tiles[y].size
                                    && board.tiles[y][testX] == Tile.EMPTY
                                ) {
                                    testX += 1
                                }
                            }
                            if (testX < board.tiles[y].size
                                && board.tiles[y][testX] == Tile.OPEN
                            ) {
                                x = testX
                            }
                        }

                        Facing.DOWN -> {
                            var testY = y + 1
                            if (testY >= board.tiles.size
                                || board.tiles[testY][x] == Tile.EMPTY
                            ) {
                                testY = 0
                                while (testY < board.tiles.size
                                    && board.tiles[testY][x] == Tile.EMPTY
                                ) {
                                    testY += 1
                                }
                            }
                            if (testY < board.tiles.size
                                && board.tiles[testY][x] == Tile.OPEN
                            ) {
                                y = testY
                            }
                        }

                        Facing.LEFT -> {
                            var testX = x - 1
                            if (testX < 0
                                || board.tiles[y][testX] == Tile.EMPTY
                            ) {
                                testX = board.tiles[y].lastIndex
                                while (testX >= 0
                                    && board.tiles[y][testX] == Tile.EMPTY
                                ) {
                                    testX -= 1
                                }
                            }
                            if (testX >= 0
                                && board.tiles[y][testX] == Tile.OPEN
                            ) {
                                x = testX
                            }
                        }

                        Facing.UP -> {
                            var testY = y - 1
                            if (testY < 0
                                || board.tiles[testY][x] == Tile.EMPTY
                            ) {
                                testY = board.tiles.lastIndex
                                while (testY >= 0
                                    && board.tiles[testY][x] == Tile.EMPTY
                                ) {
                                    testY -= 1
                                }
                            }
                            if (testY >= 0
                                && board.tiles[testY][x] == Tile.OPEN
                            ) {
                                y = testY
                            }
                        }
                    }
                }
            }

            is PathItem.Rotating -> {
                facing = if (p.clockwise) facing.clockwiseNext() else facing.counterClockwiseNext()
            }
        }
    }

    return 1000 * (y + 1) + 4 * (x + 1) + facing.passwordValue
}

private fun part2(input: List<String>): Int {
    val cube = input.parseInputBoard().toCube()

    var position = cube.position
    cube.path.forEach { p ->
        when (p) {
            is PathItem.Movement -> {
                for (i in 0 until p.numTiles) {
                    val newPosition = cube.getNextPosition(position)
                    if (cube.facets[newPosition.facedId]!![newPosition.y][newPosition.x] == Tile.OPEN) {
                        position = newPosition
                    } else {
                        break
                    }
                }
            }

            is PathItem.Rotating -> {
                val newFacing = if (p.clockwise) {
                    position.facing.clockwiseNext()
                } else {
                    position.facing.counterClockwiseNext()
                }
                position = position.copy(facing = newFacing)
            }
        }
    }

    val y = position.y + (position.facedId / 3) * cube.facetSize
    val x = position.x + (position.facedId % 3) * cube.facetSize

    return 1000 * (y + 1) + 4 * (x + 1) + position.facing.passwordValue
}

private fun Board.toCube(): Cube {
    val width = this.tiles[0].size
    val height = this.tiles.size

    check(width < height)

    val facetSize = width / 3
    check(
        facetSize * 3 == width
                && facetSize * 4 == height
    )

    val facets: Map<Int, List<List<Tile>>> = (0 until 12).associateWith { facetId ->
        val column = facetId % 3
        val row = facetId / 3
        this.tiles.subList(row * facetSize, (row + 1) * facetSize).map { r ->
            r.subList(column * facetSize, (column + 1) * facetSize)
        }
    }

    return Cube(
        facets = facets,
        position = PosOnCube(1, 0, 0, Facing.RIGHT),
        path = this.path
    )
}


private fun Cube.getNextPosition(position: PosOnCube): PosOnCube {
    fun PosOnCube.next(): PosOnCube {
        return when (this.facing) {
            Facing.RIGHT -> this.copy(x = this.x + 1)
            Facing.DOWN -> this.copy(y = this.y + 1)
            Facing.LEFT -> this.copy(x = this.x - 1)
            Facing.UP -> this.copy(y = this.y - 1)
        }
    }

    val next = position.next()
    if (next.x in 0 until facetSize
        && next.y in 0 until facetSize
    ) {
        return next
    }

    return getAdjacentPosition(position)
}

private fun Cube.getAdjacentPosition(position: PosOnCube): PosOnCube {
    val lastIndex = facetSize - 1
    val newPosition = when (position.facedId) {
        1 -> {
            when (position.facing) {
                // edge: x:x, y:y
                Facing.RIGHT -> position.copy(
                    facedId = 2,
                    x = 0
                )

                Facing.LEFT -> position.copy(
                    // edge: x:-x, y:-y
                    facedId = 6,
                    x = 0,
                    y = lastIndex - position.y,
                    facing = Facing.RIGHT
                )

                Facing.DOWN -> position.copy(
                    // edge: x:x, y:y
                    facedId = 4,
                    y = 0
                )

                Facing.UP -> position.copy(
                    // edge: x:y, y:-x
                    facedId = 9,
                    x = 0,
                    y = position.x,
                    facing = Facing.RIGHT
                )
            }
        }

        2 -> {
            when (position.facing) {
                Facing.RIGHT -> position.copy(
                    // edge: x:-x, y:-y
                    facedId = 7,
                    x = lastIndex,
                    y = lastIndex - position.y,
                    facing = Facing.LEFT
                )

                Facing.LEFT -> position.copy(
                    // edge: x:x, y:y
                    facedId = 1,
                    x = lastIndex,
                )

                Facing.DOWN -> position.copy(
                    // edge: x:y, y:-x
                    facedId = 4,
                    y = position.x,
                    x = lastIndex,
                    facing = Facing.LEFT
                )

                Facing.UP -> position.copy(
                    // edge: x:x, y:y
                    facedId = 9,
                    y = lastIndex,
                )
            }
        }

        4 -> {
            when (position.facing) {
                Facing.RIGHT -> position.copy(
                    // edge: x:-y, y:x
                    facedId = 2,
                    y = lastIndex,
                    x = position.y,
                    facing = Facing.UP
                )

                Facing.LEFT -> position.copy(
                    // edge: x:-y, y:x
                    facedId = 6,
                    y = 0,
                    x = position.y,
                    facing = Facing.DOWN
                )

                Facing.DOWN -> position.copy(
                    // edge: x:x, y:y
                    facedId = 7,
                    y = 0,
                )

                Facing.UP -> position.copy(
                    // edge: x:x, y:y
                    facedId = 1,
                    y = lastIndex,
                )
            }
        }

        6 ->
            when (position.facing) {
                Facing.RIGHT -> position.copy(
                    // edge: x:x, y:y
                    facedId = 7,
                    x = 0,
                )

                Facing.LEFT -> position.copy(
                    // edge: x:-x, y:-y
                    facedId = 1,
                    x = 0,
                    y = lastIndex - position.y,
                    facing = Facing.RIGHT
                )

                Facing.DOWN -> position.copy(
                    // edge: x:x, y:y
                    facedId = 9,
                    y = 0,
                )

                Facing.UP -> position.copy(
                    // edge: x:y, y:-x
                    facedId = 4,
                    x = 0,
                    y = position.x,
                    facing = Facing.RIGHT
                )
            }

        7 ->
            when (position.facing) {
                Facing.RIGHT -> position.copy(
                    // edge: x:-x, y:-y
                    facedId = 2,
                    x = lastIndex,
                    y = lastIndex - position.y,
                    facing = Facing.LEFT
                )

                Facing.LEFT -> position.copy(
                    // edge: x:x, y:y
                    facedId = 6,
                    x = lastIndex,
                )

                Facing.DOWN -> position.copy(
                    // edge: x:y, y:-x
                    facedId = 9,
                    x = lastIndex,
                    y = position.x,
                    facing = Facing.LEFT
                )

                Facing.UP -> position.copy(
                    // edge: x:x, y:y
                    facedId = 4,
                    y = lastIndex,
                )
            }

        9 ->
            when (position.facing) {
                Facing.RIGHT -> position.copy(
                    // edge: x:-y, y:x
                    facedId = 7,
                    x = position.y,
                    y = lastIndex,
                    facing = Facing.UP
                )

                Facing.LEFT -> position.copy(
                    // edge: x:-y, y:x
                    facedId = 1,
                    x = position.y,
                    y = 0,
                    facing = Facing.DOWN
                )

                Facing.DOWN -> position.copy(
                    // edge: x:x, y:y
                    facedId = 2,
                    y = 0,
                )

                Facing.UP -> position.copy(
                    // edge: x:y, y:y
                    facedId = 6,
                    y = lastIndex,
                )
            }

        else -> error("Unknown facet $this")
    }
    return newPosition
}

//private fun validateMovements(input: List<String>) {
//    val cube = input.parseInputBoard().toCube()
//
//    listOf(1, 2, 4, 6, 7, 9).forEach {
//        listOf(
//            PosOnCube(it, 5, 0, Facing.UP),
//            PosOnCube(it, 5, 49, Facing.DOWN),
//            PosOnCube(it, 0, 5, Facing.LEFT),
//            PosOnCube(it, 49, 5, Facing.RIGHT)
//        ).forEach { p ->
//            var pos1 = p
//            repeat(4*50) {
//                pos1 = cube.getNextPosition(pos1)
//            }
//            check(p == pos1)
//        }
//    }
//}


private fun List<String>.parseInputBoard(): Board {
    val board = this.dropLast(2)
    val movements: String = this.takeLast(1).single()

    val tiles: List<MutableList<Tile>> = board.map { l ->
        l.map { t ->
            when (t) {
                ' ' -> Tile.EMPTY
                '.' -> Tile.OPEN
                '#' -> Tile.WALL
                else -> error("Unknown tile $t")
            }
        }.toMutableList()
    }
    val width = tiles.maxOf { it.size }
    tiles.forEach { l ->
        if (l.size < width) {
            repeat(width - l.size) {
                l += Tile.EMPTY
            }
        }
    }

    val x = tiles[0].indexOfFirst { it == Tile.OPEN }

    val path = buildList {
        var i = 0
        while (i < movements.length) {
            when (movements[i]) {
                in '0'..'9' -> {
                    var end = i
                    while (end <= movements.lastIndex && movements[end] in '0'..'9') {
                        end += 1
                    }
                    add(PathItem.Movement(movements.substring(i, end).toInt()))
                    i = end
                }

                'R' -> {
                    add(PathItem.Rotating(true))
                    i += 1
                }

                'L' -> {
                    add(PathItem.Rotating(false))
                    i += 1
                }

                else -> {
                    error("Unknown symbol ${movements[i]}")
                }
            }
        }
    }
    return Board(
        tiles = tiles,
        position = Pos(x, 0, Facing.RIGHT),
        path = path
    )
}
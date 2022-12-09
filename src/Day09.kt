import kotlin.math.abs

private enum class Direction {
    LEFT, UP, RIGHT, DOWN,
}

private fun String.toDirection(): Direction = when (this) {
    "L" -> Direction.LEFT
    "R" -> Direction.RIGHT
    "U" -> Direction.UP
    "D" -> Direction.DOWN
    else -> error("Unknown direction $this")
}

private data class RopeMove(
    val direction: Direction,
    val steps: Int
)

private data class Coordinates(
    var y: Int, var x: Int
) {
    fun moveTo(direction: Direction): Coordinates {
        return when (direction) {
            Direction.LEFT -> Coordinates(this.y, this.x - 1)
            Direction.UP -> Coordinates(this.y - 1, this.x)
            Direction.RIGHT -> Coordinates(this.y, this.x + 1)
            Direction.DOWN -> Coordinates(this.y + 1, this.x)
        }
    }
    fun isTouchingOrOverlapping(other: Coordinates): Boolean {
        return abs(this.x - other.x) <= 1
                && abs(this.y - other.y) <= 1
    }
}

private fun Coordinates.getNewTailPosition(oldTailPosition: Coordinates): Coordinates {
    if (this.isTouchingOrOverlapping(oldTailPosition)) {
        return oldTailPosition
    }

    val newY = when (this.y - oldTailPosition.y) {
        0 -> 0
        -1, -2, -> -1
        1, 2 -> 1
        else -> error("Unsupported head - tail combination")
    }
    val newX = when (this.x - oldTailPosition.x) {
        0 -> 0
        -1, -2, -> -1
        1, 2 -> 1
        else -> error("Unsupported head - tail combination")
    }

    return Coordinates(
        y = oldTailPosition.y + newY,
        x = oldTailPosition.x + newX,
    )
}

fun main() {
    fun part1(input: List<RopeMove>): Int {
        var head = Coordinates(0, 0)
        var tail = head
        val field = mutableSetOf(head)

        input.forEach { move ->
            repeat(move.steps) {
                head = head.moveTo(move.direction)
                tail = head.getNewTailPosition(tail)
                field += tail
            }
        }

        return field.count()
    }

    fun part2(input: List<RopeMove>): Int {
        // rope[0] == Head
        val rope = MutableList(10) { Coordinates(0, 0) }
        val field = mutableSetOf(rope.last())
        input.forEach { move ->
            repeat(move.steps) {
                rope[0] = rope[0].moveTo(move.direction)
                for (r in 1..9) {
                    rope[r] = rope[r - 1].getNewTailPosition(rope[r])
                }
                field += rope.last()
            }
        }
        return field.count()
    }

    val input = readInput("Day09").map(String::parseMove)

    val part1Result = part1(input)
    println(part1Result)
    check(part1Result == 5874)

    val part2Result = part2(input)
    println(part2Result)
    check(part2Result == 2467)
}

private const val FIELD_SIZE: Int = 1000
private fun Set<Coordinates>.printRope() {
    val field: List<MutableList<Char>> = List(FIELD_SIZE) { MutableList(FIELD_SIZE) { '.' } }
    this.forEachIndexed { index, coords ->
        field[coords.y][coords.x] = if (index == 0) 'H' else index.digitToChar()
    }
    field.forEach { l ->
        println(l.joinToString(""))
    }
}

private fun String.parseMove(): RopeMove {
    val (d, c) = this.split(" ")
    return RopeMove(d.toDirection(), c.toInt())
}

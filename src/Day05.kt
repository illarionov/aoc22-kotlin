data class Move(val from: Int, val to: Int, val count: Int)
data class Input(
    val dequeues: List<ArrayDeque<Char>>,
    val moves: List<Move>
)

fun main() {
    fun part1(input: Input): List<Char> {
        val dequeues = List(input.dequeues.size) { i -> ArrayDeque(input.dequeues[i]) }
        input.moves.forEach { move ->
            repeat(move.count) {
                dequeues[move.to].push(dequeues[move.from].pop())
            }
        }
        return dequeues.map { it.peek() }
    }

    fun part2(input: Input): List<Char> {
        val dequeues = List(input.dequeues.size) { i -> ArrayDeque(input.dequeues[i]) }
        input.moves.forEach { move ->
            val deque: ArrayDeque<Char> = ArrayDeque()
            repeat(move.count) {
                deque.push(dequeues[move.from].pop())
            }
            while (!deque.isEmpty()) {
                dequeues[move.to].push(deque.pop())
            }
        }
        return dequeues.map { it.peek() }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day05_test").parseInput()
    val result = part1(testInput).joinToString("")
    check(result == "CMZ")

    val input = readInput("Day05").parseInput()
    val part1Result = part1(input).joinToString("")
    println(part1Result)
    // check(part1Result == "CWMTGHBDW")

    val parse2Result = part2(input).joinToString("")
    println(parse2Result)
    // check(parse2Result == "SSCGWJCRB")
}

fun List<String>.parseInput(): Input {
    val splitPosition = this.indexOf("")
    val deq = this.take(splitPosition)
    val moves = this.drop(splitPosition + 1)
    return Input(
        dequeues = parseDequeues(deq),
        moves = parseMoves(moves)
    )
}

val movePattern = Regex("""move (\d+) from (\d+) to (\d+)""")
fun parseMoves(input: List<String>): List<Move> {
    return input.map { s ->
        val groups = movePattern.matchEntire(s)?.groups ?: error("Can not parse move `$s`")
        check(groups.size == 4)
        Move(
            count = groups[1]!!.value.toInt(),
            from = groups[2]!!.value.toInt() - 1,
            to = groups[3]!!.value.toInt() - 1
        )
    }
}

fun parseDequeues(input: List<String>): List<ArrayDeque<Char>> {
    val dequeues = mutableListOf<ArrayDeque<Char>>()
    input.asReversed().asSequence().drop(1).forEach { s ->
        val dequeCount = (s.length + 1) / 4
        if (dequeues.size < dequeCount) {
            repeat(dequeCount - dequeues.size) {
                dequeues.add(ArrayDeque())
            }
        }
        repeat(dequeCount) { index ->
            val item = s.substring(index * 4, index * 4 + 3)
            if (item != "   ") {
               dequeues[index].push(item[1])
            }
        }
    }
    return dequeues
}

private inline fun <reified E> ArrayDeque<in E>.push(item: E) = addLast(item)
private inline fun <reified E> ArrayDeque<out E>.peek(): E = this.last()
private inline fun <reified E> ArrayDeque<out E>.pop(): E = removeLast()

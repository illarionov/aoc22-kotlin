fun main() {
    val testInput = readInput("Day21_test")
        .map(String::parseYellingMonkey)
    val input = readInput("Day21")
        .map(String::parseYellingMonkey)

    part1(testInput).also {
        println("Part 1, test input: $it")
        check(it == 152L)
    }

    part1(input).also {
        println("Part 1, real input: $it")
        check(it == 72664227897438L)
    }

    part2(testInput).also {
        println("Part 2, test input: $it")
        check(it == 301L)
    }

    part2(input).also {
        println("Part 2, real input: $it")
        check(it == 3916491093817)
    }
}

private sealed class MonkeyJob {
    data class Number(val value: Long) : MonkeyJob()

    data class MathOperation(
        val left: String,
        val right: String,
        val opSymbol: Char,
        val op: (left: Long, right: Long) -> Long
    ) : MonkeyJob()
}

private data class Monkey2(
    val id: String,
    val op: MonkeyJob
)

private fun part1(input: List<Monkey2>): Long {
    val set = input.associateBy(Monkey2::id)

    fun getResultOfMonkey(id: String): Long {
        val m = set[id] ?: error("Cannot find monkey $id")
        return when (m.op) {
            is MonkeyJob.MathOperation -> {
                m.op.op(
                    getResultOfMonkey(m.op.left),
                    getResultOfMonkey(m.op.right)
                )
            }

            is MonkeyJob.Number -> m.op.value
        }
    }


    return getResultOfMonkey("root")
}

private fun part2(input: List<Monkey2>): Long {
    val set = input.associateBy(Monkey2::id)

    fun getResultOfMonkey(id: String): Long? {
        val m = set[id] ?: error("Cannot find monkey $id")
        if (id == "humn") {
            return null
        }
        return when (m.op) {
            is MonkeyJob.MathOperation -> {
                val left = getResultOfMonkey(m.op.left)
                val right = getResultOfMonkey(m.op.right)
                if (left == null || right == null) {
                    return null
                }
                m.op.op(left, right)
            }

            is MonkeyJob.Number -> m.op.value
        }
    }

    fun getResultOfHumn(id: String, expectedResult: Long): Long? {
        val monkey = set[id]!!
        if (monkey.id == "humn") {
            return expectedResult
        }
        if (monkey.op is MonkeyJob.Number) {
            error("No yelling monkeys except for human")
        }
        val op = monkey.op as MonkeyJob.MathOperation
        val left = getResultOfMonkey(monkey.op.left)
        val right = getResultOfMonkey(monkey.op.right)
        if (left == null && right != null) {
            val expectedResultFromLeft = when (op.opSymbol) {
                '-' -> expectedResult + right
                '+' -> expectedResult - right
                '*' -> expectedResult / right
                '/' -> expectedResult * right
                else -> {
                    error("Unknown operation `$op`")
                }
            }
            return getResultOfHumn(op.left, expectedResultFromLeft)
        } else if (left != null && right == null) {
            val expectedResultFromRight = when (op.opSymbol) {
                '-' -> left - expectedResult
                '+' -> expectedResult - left
                '*' -> expectedResult / left
                '/' -> left / expectedResult
                else -> {
                    error("Unknown operation `$op`")
                }
            }
            return getResultOfHumn(op.right, expectedResultFromRight)
        } else {
            error("Unsupported combination. left: $left, right: $right")
        }
    }

    val rootMonkey = set["root"]!!
    val left: Long? = getResultOfMonkey(
        (rootMonkey.op as MonkeyJob.MathOperation).left
    )
    val right: Long? = getResultOfMonkey(
        rootMonkey.op.right
    )

    val result = if (left == null && right != null) {
        getResultOfHumn(rootMonkey.op.left, right)
    } else if (left != null && right == null) {
        getResultOfHumn(rootMonkey.op.right, left)
    } else {
        error("Unsupported combination on root: left = $left, right = $right")
    }

    return result!!
}

private val numberRegex = Regex("""(\w{4}): (\d+)""")
private val opRegex = Regex("""(\w{4}): (\w{4}) ([+\-*/]) (\w{4})""")
private fun String.parseYellingMonkey(): Monkey2 {
    numberRegex.matchEntire(this)?.groupValues?.let {
        return Monkey2(it[1], MonkeyJob.Number(it[2].toLong()))
    }
    opRegex.matchEntire(this)?.groupValues?.let {
        return Monkey2(
            it[1],
            MonkeyJob.MathOperation(
                left = it[2],
                right = it[4],
                opSymbol = it[3][0],
                op = when (it[3]) {
                    "-" -> Long::minus
                    "+" -> Long::plus
                    "*" -> Long::times
                    "/" -> Long::div
                    else -> error("Unknown operation `$this`, `${it[2]}`")
                }
            )
        )
    }
    error("Cannot parse `$this`")
}
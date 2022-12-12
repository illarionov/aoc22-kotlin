import java.io.File

data class Monkey(
    val number: Int,
    val startingItems: List<Int>,
    val opp: Op,
    val test: Int,
    val testTrueMonkey: Int,
    val testFalseMonkey: Int
) {
    fun runTest(level: Long): Int = if (level % test.toLong() == 0L) testTrueMonkey else testFalseMonkey
}
data class Op(
    val first: Operand,
    val second: Operand,
    val op: OpType
) {
    fun applyTo(old: Long): Long {
        val firstI = first.getValue(old)
        val secondI = second.getValue(old)
        return when(op) {
            OpType.ADD -> firstI + secondI
            OpType.SUB -> firstI - secondI
            OpType.MUL -> firstI * secondI
            OpType.DIV -> firstI / secondI
        }
    }
}

enum class OpType { ADD, SUB, MUL, DIV }

sealed class Operand {
    object Old: Operand() {
        override fun getValue(old: Long) = old
    }

    data class Num(val i: Long): Operand() {
        override fun getValue(old: Long): Long  = i
    }

    abstract fun getValue(old: Long): Long
}

fun main() {
    fun part1(input: List<Monkey>): Long {
        val monkeyItems: Map<Int, ArrayDeque<Long>> = input.associate {
            it.number to ArrayDeque(it.startingItems.map(Int::toLong))
        }
        val monkeys = input.associateBy(Monkey::number)
        val inspectedTimes: MutableMap<Int, Long> = mutableMapOf(
            *(input.map { it.number to 0L }.toTypedArray())
        )

        repeat(20) {
            monkeyItems.forEach { (monkeyNo, items) ->
                val monkey = monkeys[monkeyNo]!!
                items.forEach { level ->
                    val inspected = monkey.opp.applyTo(level).toInt()
                    val afterBored = inspected / 3L
                    val newMonkey = monkey.runTest(afterBored)
                    monkeyItems[newMonkey]!!.addLast(afterBored)
                }
                inspectedTimes[monkeyNo] = inspectedTimes[monkeyNo]!! + items.size
                items.clear()
            }
        }
         return inspectedTimes.values
            .sorted()
            .takeLast(2)
            .reduce(Long::times)
    }

    fun part2(input: List<Monkey>): Long {
        val monkeyItems: Map<Int, ArrayDeque<Long>> = input.associate {
            it.number to ArrayDeque(it.startingItems.map(Int::toLong))
        }
        val monkeys = input.associateBy { it.number }
        val inspectedTimes: MutableMap<Int, Long> = mutableMapOf(
            *(input.map { it.number to 0L }.toTypedArray())
        )

        val max = monkeys.values.map(Monkey::test).reduce(Int::times)

        repeat(10000) {
            monkeyItems.forEach { (monkeyNo, items) ->
                val monkey = monkeys[monkeyNo]!!
                items.forEach { level ->
                    val inspected = monkey.opp.applyTo(level) % max
                    val newMonkey = monkey.runTest(inspected)
                    monkeyItems[newMonkey]!!.addLast(inspected)
                }
                inspectedTimes[monkeyNo] = inspectedTimes[monkeyNo]!! + items.size
                items.clear()
            }
        }
        return inspectedTimes.values
            .sorted()
            .takeLast(2)
            .reduce(Long::times)
    }

    // test if implementation meets criteria from the description, like:
    val testInput: List<Monkey> = File("src", "Day11_test.txt")
        .readText()
        .parseMonkeys()

    val part1CheckResult = part1(testInput)
    println(part1CheckResult)
    check(part1CheckResult == 10605L)

    val input = File("src", "Day11.txt")
        .readText()
        .parseMonkeys()
    val part1Result = part1(input)
    println(part1Result)
    //check(part1Result == 61005L)

    check(part2(testInput) == 2713310158L)
    val part2Result = part2(input)
    //check(part2Result == 20567144694L)
    println(part2Result)
}

fun String.parseMonkeys(): List<Monkey> {
    return this.split("\n\n").map(String::parseMonkey)
}

fun String.parseMonkey(): Monkey {
    val lines = this.split("\n")
    val monkeyNo = Regex("""Monkey (\d+):""").matchEntire(lines[0])!!.groups[1]!!.value.toInt()

    val items = lines[1].substringAfter("Starting items: ").split(", ")
        .map(String::toInt)
    val operationResult = Regex("""\s+Operation: new = (\S+) (.) (\S+)""").matchEntire(lines[2])
        ?.groupValues ?: error("Cannot parse `${lines[2]}`")
    val operation = Op(
        first = operationResult[1].parseOperand(),
        second = operationResult[3].parseOperand(),
        op = operationResult[2].parseOperation()
    )
    val testDivisibleBy = lines[3].substringAfter("Test: divisible by ").toInt()
    val ifTrueMonkey = lines[4].substringAfter("    If true: throw to monkey ").toInt()
    val ifFalseMonkey = lines[5].substringAfter("If false: throw to monkey ").toInt()
    return Monkey(number = monkeyNo,
        opp = operation,
        startingItems = items,
        test = testDivisibleBy,
        testTrueMonkey = ifTrueMonkey,
        testFalseMonkey =  ifFalseMonkey
    )
}

private fun String.parseOperand(): Operand {
    return if (this == "old") Operand.Old else Operand.Num(this.toLong())
}

private fun String.parseOperation(): OpType {
    return when (this) {
        "-" -> OpType.SUB
        "+" -> OpType.ADD
        "*" -> OpType.MUL
        "/" -> OpType.DIV
        else -> error("Unknown operand `$this`")
    }
}
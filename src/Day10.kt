private sealed class Instruction {
    object noop : Instruction()
    data class addx(val amount: Int) : Instruction()
}

private fun String.toInstruction(): Instruction {
    return if (this == "noop") {
        Instruction.noop
    } else {
        this.split(" ").let { (addx, count) ->
            if (addx != "addx") {
                error("Unknown instruction $this")
            }
            Instruction.addx(count.toInt())
        }
    }
}

fun main() {
    fun part1(input: List<Instruction>): Int {
        var x = 1
        var current: Instruction = Instruction.noop
        var currentEndTimeCycle = 1
        var sum = 0

        var ic = 0
        (1..220).forEach { cycle ->
            if (cycle == currentEndTimeCycle) {
                when (current) {
                    is Instruction.addx -> {
                        x += (current as Instruction.addx).amount
                        currentEndTimeCycle = cycle + 2
                    }

                    Instruction.noop -> {
                        currentEndTimeCycle = cycle + 1
                    }
                }
                current = if (input.lastIndex >= ic) input[ic] else Instruction.noop
                currentEndTimeCycle = when (current) {
                    is Instruction.addx -> {
                        cycle + 2
                    }

                    Instruction.noop -> {
                        cycle + 1
                    }
                }
                ic += 1
            }
            if (cycle in setOf(20, 60, 100, 140, 180, 220)) {
                sum += cycle * x
            }
        }

        return sum
    }

    fun part2(input: List<Instruction>) {
        var x = 1
        var current: Instruction = Instruction.noop
        var currentEndTimeCycle = 1

        var ic = 0

        val field = List(6) { MutableList(40) { ' ' } }

        (1..40 * 6).forEach { cycle ->
            if (cycle == currentEndTimeCycle) {
                when (current) {
                    is Instruction.addx -> {
                        x += (current as Instruction.addx).amount
                        currentEndTimeCycle = cycle + 2
                    }

                    Instruction.noop -> {
                        currentEndTimeCycle = cycle + 1
                    }
                }
                current = if (input.lastIndex >= ic) input[ic] else Instruction.noop
                currentEndTimeCycle = when (current) {
                    is Instruction.addx -> cycle + 2
                    Instruction.noop -> cycle + 1
                }
                ic += 1
            }

            val symbol = if (((cycle - 1) % 40) in (x - 1..x + 1)) '#' else '.'
            field[(cycle - 1) / 40][(cycle - 1) % 40] = symbol
        }

        field.forEach {
            println(it.joinToString(""))
        }
    }

    val testInput = readInput("Day10_test")
        .map(String::toInstruction)
    val part1CheckResult = part1(testInput)
    println(part1CheckResult)
    check(part1CheckResult == 13140)

    val input = readInput("Day10")
        .map(String::toInstruction)
    println(part1(input))
    part2(input)
}

fun main() {
    val testInput = readInput("Day25_test")
    val input = readInput("Day25")

    part1(testInput).also {
        println("Part 1, test input: $it")
        check(it == "2=-1=0")
    }

    part1(input).also {
        println("Part 1, real input: $it")
        //check(it == 1)
    }

    part2(testInput).also {
        println("Part 2, test input: $it")
        check(it == 1)
    }

    part2(input).also {
        println("Part 2, real input: $it")
        //check(it == 1)
    }
}

private fun part1(input: List<String>): String {
    val i = input
        .map(String::fromSnafu)
    val sum = i.sum()
    return sum.toSnafu()
}

private fun Long.toSnafu(): String {
    var cf = 0L
    var l = this
    val res: MutableList<Char> = mutableListOf()
    while (l != 0L) {
        val d = (l % 5L) + cf
        cf = 0
        when (d) {
            in 0..2 -> {
                res += d.toInt().digitToChar()
            }

            in 3..4 -> {
                cf = 1
                val v = when (d) {
                    3L -> '='
                    4L -> '-'
                    else -> error("Unknown value")
                }
                res += v
            }

            else -> {
                error("Unexpected d $d")
            }
        }
        l /= 5L
    }
    if (cf > 0) {
        res += cf.toInt().digitToChar()
    }

    return res.reversed().joinToString("")
}

private fun String.fromSnafu(): Long {
    var r = 0L
    fun Char.fromSnafu(): Long {
        return when (this) {
            '0' -> 0L
            '1' -> 1L
            '2' -> 2L
            '-' -> -1L
            '=' -> -2L
            else -> error("Unknown number")
        }
    }

    this.forEach { c ->
        r = r * 5 + c.fromSnafu()
    }
    return r
}

private fun part2(input: List<String>): Int {
    return input.size
}
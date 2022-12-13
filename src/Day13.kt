fun main() {
    fun part1(input: List<Pair<ListOrNumber, ListOrNumber>>): Int {
        return input.withIndex()
            .filter { (_, p) -> isInRightOrder(p.first, p.second) >= 0
            }
            .sumOf { it.index + 1 }
    }

    fun part2(input: List<Pair<ListOrNumber, ListOrNumber>>): Int {
        val packet2 = ListOrNumber.Sublist(ListOrNumber.Sublist(2))
        val packet6 = ListOrNumber.Sublist(ListOrNumber.Sublist(6))

        val packets = input.flatMap { p ->
            p.toList() + listOf(packet2, packet6)
        }

        val sorted = packets.sortedWith { o1, o2 -> isInRightOrder(o2, o1) }

        return (sorted.indexOf(packet2) + 1) * (sorted.indexOf(packet6) + 1)
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day13_test")
        .parse13Input()

    val test1Result = part1(testInput)
    println(test1Result)
    check(test1Result == 13)

    val input = readInput("Day13")
        .parse13Input()
    println(part1(input))

    val check2 = part2(testInput)
    println(check2)
    check(check2 == 140)

    println(part2(input))
}


private sealed class Symbol {
    object LeftBrace : Symbol()
    object RightBrace : Symbol()

    data class Number(val i: Int) : Symbol() {
        override fun toString(): String {
            return "$i"
        }
    }
}

private fun String.tokenize(): List<Symbol> {
    val s: ArrayDeque<Symbol> = ArrayDeque()
    val chars = this.toCharArray()
    var i = 0
    while (i <= chars.lastIndex) {
        when (chars[i]) {
            '[' -> {
                s.addLast(Symbol.LeftBrace)
                i += 1
            }

            ']' -> {
                i += 1
                s.addLast(Symbol.RightBrace)
            }

            ',' -> {
                i += 1
            }

            in '0'..'9' -> {
                var n = chars[i].digitToInt()
                i += 1
                while (i <= chars.lastIndex) {
                    when (chars[i]) {
                        in '0'..'9' -> {
                            n = n * 10 + chars[i].digitToInt()
                            i += 1
                        }

                        ',' -> {
                            s.addLast(Symbol.Number(n))
                            n = 0
                            i += 1
                        }

                        '[', ']' -> {
                            s.addLast(Symbol.Number(n))
                            n = 0
                            break
                        }

                        else -> {
                            error("Unknown symbol in `$this` at position $i")
                        }
                    }
                }
                if (i > chars.lastIndex) {
                    s.addLast(Symbol.Number(n))
                }
            }

            else -> {
                error("Unknown symbol in `$this` at position $i")
            }
        }
    }
    return s
}

private fun String.parseListOrNumber(): ListOrNumber {
    val symbols = this.tokenize()
    val s: ArrayDeque<ListOrNumber.Sublist> = ArrayDeque()
    if (symbols.size == 1 && symbols[0] is Symbol.Number) {
        return ListOrNumber.Number((symbols[0] as Symbol.Number).i)
    }
    check(symbols[0] == Symbol.LeftBrace)

    val root: ListOrNumber.Sublist = ListOrNumber.Sublist()
    s.addLast(root)

    symbols.drop(1).forEach { symbol ->
        when (symbol) {
            Symbol.LeftBrace -> ListOrNumber.Sublist().let { l ->
                s.last().add(l)
                s.addLast(l)
            }

            is Symbol.Number -> s.last().add(ListOrNumber.Number(symbol.i))

            Symbol.RightBrace -> s.removeLast()
        }
    }
    return root
}

// < 0: wrong order, 0: equals, 1: right order
private fun isInRightOrder(left: ListOrNumber, right: ListOrNumber): Int {
    if (left is ListOrNumber.Number && right is ListOrNumber.Number) {
        return right.v - left.v
    } else if (left is ListOrNumber.Sublist && right is ListOrNumber.Sublist) {
        left.forEachIndexed { index, listOrNumber ->
            if (index > right.lastIndex) {
                return -1
            }
            val cmp = isInRightOrder(listOrNumber, right[index])
            if (cmp != 0) return cmp
        }
        return if (left.size == right.size) 0 else 1
    } else {
        return isInRightOrder(
            if (left is ListOrNumber.Number) ListOrNumber.Sublist(left) else left,
            if (right is ListOrNumber.Number) ListOrNumber.Sublist(right) else right,
        )
    }
}


private sealed class ListOrNumber {
    data class Number(var v: Int) : ListOrNumber() {
        override fun toString(): String = v.toString()
    }

    class Sublist : MutableList<ListOrNumber> by mutableListOf(), ListOrNumber() {
        companion object {
            operator fun invoke(i: ListOrNumber): Sublist = Sublist().apply {
                add(i)
            }
            operator fun invoke(i: Int): Sublist = Sublist(Number(i))
        }
    }
}

private fun List<String>.parse13Input(): List<Pair<ListOrNumber, ListOrNumber>> {
    return this
        .filter(String::isNotEmpty)
        .map(String::parseListOrNumber)
        .chunked(2)
        .map { l -> Pair(l[0], l[1]) }
}
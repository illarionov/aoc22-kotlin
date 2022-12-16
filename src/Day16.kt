import kotlin.system.measureTimeMillis

fun main() {
    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day16_test")
        .parseCave()
    val part1TestInputResult = part1(testInput)
    println("Part 1 (test input): $part1TestInputResult")
    check(part1TestInputResult == 1651)

    val input = readInput("Day16")
        .parseCave()
    val part1Result = part1(input)
    println("Part 1: $part1Result")
    check(part1Result == 1728)

    val part2testInputResult = part2(testInput)
    println("Part 2 (test input): $part2testInputResult")
    check(part2testInputResult == 1707)

    var part2Result: Int
    val timeInMillis = measureTimeMillis {
        part2Result = part2(input)
    }
    println("Part 2: $part2Result, time: ${timeInMillis / 1000.0}")
    check(part2Result == 2304)
}

typealias ValveId = String

private data class Valve(
    val rate: Int,
    val id: ValveId,
    val pipes: MutableList<ValveId>,
    var isOpen: Boolean = false
)

private class Cave(
    valves: List<Valve>
) {
    val valves: Map<ValveId, Valve> = valves.associateBy(Valve::id)
    fun getRateOf(openValves: Set<ValveId>): Int {
        return openValves.sumOf { valves[it]!!.rate }
    }
}

private fun part1(cave: Cave): Int {
    return cave.getMaxPressure(30)
}

private fun part2(input: Cave): Int {
    return input.getMaxPressurePart2(26)
}

private fun Cave.getMaxPressure(steps: Int): Int {
    data class DequeItem(
        val id: ValveId,
        val minute: Int,
        val openedValves: Set<ValveId>,
        val maxRate: Int
    ) {
        val isOpened: Boolean = id in openedValves
    }

    // Position, minute --> opened valves, max rate,
    val cache: MutableMap<Pair<ValveId, Int>, DequeItem> = mutableMapOf()
    val queue: ArrayDeque<DequeItem> = ArrayDeque()
    val item2 = DequeItem(
        "AA", 0, emptySet(), 0
    )
    cache["AA" to 0] = item2

    queue.addLast(item2)

    while (!queue.isEmpty()) {
        val item = queue.removeLast()
        if (item.minute > steps) continue

        val currentRate = item.maxRate

        if (!item.isOpened) {
            val newOpenValves = item.openedValves + item.id
            val newItem = DequeItem(
                item.id,
                item.minute + 1,
                newOpenValves,
                currentRate + getRateOf(newOpenValves)
            )
            if (!cache.containsKey(newItem.id to newItem.minute)
                || cache[newItem.id to newItem.minute]!!.maxRate < newItem.maxRate
            ) {
                cache[newItem.id to newItem.minute] = newItem
                queue.addLast(newItem)
            }
        }

        this.valves[item.id]!!.pipes.map { p: String ->
            DequeItem(
                p,
                item.minute + 1,
                item.openedValves,
                currentRate + getRateOf(item.openedValves)
            )
        }.forEach { newItem ->
            if (!cache.containsKey(newItem.id to newItem.minute)
                || cache[newItem.id to newItem.minute]!!.maxRate < newItem.maxRate
            ) {
                cache[newItem.id to newItem.minute] = newItem
                queue.addLast(newItem)
            }
        }
    }
    return cache.maxOf { e ->
        if (e.key.second == steps - 1) e.value.maxRate else 0
    }
}

private fun Cave.getMaxPressurePart2(steps: Int): Int {
    data class Position(val user: ValveId, val elephant: ValveId)

    data class DequeItem(
        val position: Position,
        val minute: Int,
        val openValves: Set<String>,
        val maxRate: Int
    )
    // Position, Minute --> opened valves, max rate
    val cache: MutableMap<Pair<Position, Int>, DequeItem> = mutableMapOf()

    val queue: ArrayDeque<DequeItem> = ArrayDeque()
    val startPosition = Position("AA", "AA")
    val startItem = DequeItem(
        startPosition, 0, emptySet(), 0
    )
    cache[startPosition to 0] = startItem
    queue.addLast(startItem)
    var maxRate = 0

    fun addToCacheAndQueueIfRequired(newItem: DequeItem) {
        if (newItem.minute >= steps) return

        val positionNormalized = if (newItem.position.elephant < newItem.position.user) {
            Position(newItem.position.elephant, newItem.position.user)
        } else {
            newItem.position
        }

        val newRate = newItem.maxRate
        if (!cache.containsKey(positionNormalized to newItem.minute)
            || cache[positionNormalized to newItem.minute]!!.maxRate < newRate
        ) {
            cache[positionNormalized to newItem.minute] = newItem
            queue.addLast(newItem)
            maxRate = maxOf(maxRate, newRate)
        }
    }

    while (!queue.isEmpty()) {
        val item = queue.removeLast()
        if (item.minute > steps) continue

        val currentRate = item.maxRate

        if (item.position.user !in item.openValves) {
            val openValvesUser = item.openValves + item.position.user
            // Elephant move
            if (item.position.elephant !in openValvesUser) {
                val openValvesUserElephant = openValvesUser + item.position.elephant
                val newItem = DequeItem(
                    item.position,
                    item.minute + 1,
                    openValvesUserElephant,
                    currentRate + getRateOf(openValvesUserElephant)
                )
                addToCacheAndQueueIfRequired(newItem)
            }
            this.valves[item.position.elephant]!!.pipes.map { p: String ->
                DequeItem(
                    position = item.position.copy(elephant = p),
                    minute = item.minute + 1,
                    openValves = openValvesUser,
                    maxRate = currentRate + getRateOf(openValvesUser)
                )
            }.forEach { newItem ->
                addToCacheAndQueueIfRequired(newItem)
            }
        }

        this.valves[item.position.user]!!.pipes.forEach { p: String ->
            if (item.position.elephant !in item.openValves
                && item.position.elephant != item.position.user
            ) {
                val newOpenItems = item.openValves + item.position.elephant
                DequeItem(
                    position = item.position.copy(user = p),
                    minute = item.minute + 1,
                    openValves = newOpenItems,
                    maxRate = currentRate + getRateOf(newOpenItems)
                ).let { addToCacheAndQueueIfRequired(it) }
            }
            this.valves[item.position.elephant]!!.pipes.map { ep: String ->
                DequeItem(
                    position = Position(p, ep),
                    minute = item.minute + 1,
                    openValves = item.openValves,
                    maxRate = currentRate + getRateOf(item.openValves)
                )
            }.forEach { addToCacheAndQueueIfRequired(it) }
        }
    }
    return maxRate
}


private val valveRegex = Regex("""Valve (\w+) has flow rate=(\d+); tunnels? leads? to valves? (.+)""")

private fun List<String>.parseCave(): Cave {
    return Cave(this.map {
        val (id, rate, pipes) = valveRegex.matchEntire(it)
            ?.groupValues
            ?.drop(1)
            ?: error("Can not parse valve `$it")
        Valve(
            id = id,
            rate = rate.toInt(),
            pipes = pipes.split(", ").toMutableList()
        )
    })
}

import kotlin.system.measureTimeMillis

fun main() {
    val testInput = readInput("Day19_test")
        .map(String::parseBlueprint)
    val input = readInput("Day19")
        .map(String::parseBlueprint)

    part1(testInput).also {
        println("Part 1, test input: $it")
        check(it == 33)
    }

    part1(input).also {
        println("Part 1, real input: $it")
        check(it == 1487)
    }

    part2(testInput).also {
        println("Part 2, test input: $it")
        check(it == 56 * 62)
    }

    part2(input).also {
        println("Part 2, real input: $it")
        check(it == 13440) // 16*40*21
    }
}

private data class Blueprint(
    val id: Int,
    val oreRobotCost: RobotCost,
    val clayRobotCost: RobotCost,
    val obsidianRobotCost: RobotCost,
    val geodeRobotCost: RobotCost
)

private data class RobotCost(
    val ore: Int = 0,
    val clay: Int = 0,
    val obsidian: Int = 0
) {
    fun canBuildRobot(ore: Int, clay: Int, obsidian: Int): Boolean {
        return (obsidian >= this.obsidian
                && clay >= this.clay
                && ore >= this.ore)
    }
}

private val blueprintRegex = Regex(
    """Blueprint (\d+): """
            + """Each ore robot costs (\d+) ore. """
            + """Each clay robot costs (\d+) ore. """
            + """Each obsidian robot costs (\d+) ore and (\d+) clay. """
            + """Each geode robot costs (\d+) ore and (\d+) obsidian."""
)

private fun String.parseBlueprint(): Blueprint {
    val blueprint = blueprintRegex.matchEntire(this)
        ?.groupValues
        ?.drop(1)
        ?.map(String::toInt)
        ?: error("Can not parse blueprint `$this`")
    return Blueprint(
        id = blueprint[0],
        oreRobotCost = RobotCost(blueprint[1], 0, 0),
        clayRobotCost = RobotCost(blueprint[2], 0, 0),
        obsidianRobotCost = RobotCost(blueprint[3], blueprint[4], 0),
        geodeRobotCost = RobotCost(blueprint[5], 0, blueprint[6]),
    )
}


private fun part1(input: List<Blueprint>): Int {
    return input.sumOf { b ->
        val maxGeodes = b.getMaxOpenedGeodes(24)
        val level = maxGeodes * b.id
        println("Blueprint: ${b.id}, max geodes: ${maxGeodes}, level: ${level}")
        level
    }
}

private fun part2(input: List<Blueprint>): Int {
    return input.take(3).map { b ->
        val maxGeodes = b.getMaxOpenedGeodes(32)
        println("Blueprint: ${b.id}, max geodes: ${maxGeodes}")
        maxGeodes
    }.reduce(Int::times)
}

private fun Blueprint.getMaxOpenedGeodes(minutes: Int): Int {

    data class State(
        val minute: Int = 0,
        val oreRobots: Int = 1,
        val clayRobots: Int = 0,
        val obsidianRobots: Int = 0,
        val geodeRobots: Int = 0,
        val ore: Int = 0,
        val clay: Int = 0,
        val obsidian: Int = 0,
        val geode: Int = 0
    )

    val cache: MutableMap<Int, State> = mutableMapOf()
    val queue: ArrayDeque<State> = ArrayDeque()
    queue.add(State(minute = 0, oreRobots = 1, ore = 0))

    val maxOreRobots = maxOf(
        this.oreRobotCost.ore,
        this.obsidianRobotCost.ore,
        this.clayRobotCost.ore,
        this.geodeRobotCost.ore
    )

    val maxClayRobots = maxOf(
        this.oreRobotCost.clay,
        this.obsidianRobotCost.clay,
        this.clayRobotCost.clay,
        this.geodeRobotCost.clay
    )
    val maxObsidianRobots = maxOf(
        this.oreRobotCost.obsidian,
        this.clayRobotCost.obsidian,
        this.geodeRobotCost.obsidian
    )

    fun queueIfEligible(state: State): Boolean {
        cache[state.minute - 1]?.let { old ->
            if (old.geode > state.geode) {
                return false
            }
        }

        cache[state.minute] = state
        queue.addLast(state)
        return true
    }

    var maxGeodes = 0
    while (!queue.isEmpty()) {
        val state = queue.removeLast()
        val minute = state.minute + 1
        if (minute > minutes) continue

        val newOre = state.ore + state.oreRobots
        val newClay = state.clay + state.clayRobots
        val newObsidian = state.obsidian + state.obsidianRobots
        val newGeode = state.geode + state.geodeRobots

        val estimateBestCase = run {
            val timeLeft = maxOf(minutes - state.minute, 0)
            state.geode +
                    timeLeft * state.geodeRobots +
                    (timeLeft * (timeLeft - 1) / 2)
        }

        if (estimateBestCase < maxGeodes) {
            continue
        }

        maxGeodes = maxOf(maxGeodes, newGeode)
        if (this.geodeRobotCost.canBuildRobot(state.ore, state.clay, state.obsidian)) {
            val newStateWithGeode = state.copy(
                minute = minute,
                ore = newOre - this.geodeRobotCost.ore,
                clay = newClay - this.geodeRobotCost.clay,
                obsidian = newObsidian - this.geodeRobotCost.obsidian,
                geode = newGeode,
                geodeRobots = state.geodeRobots + 1
            )
            queueIfEligible(newStateWithGeode)
            continue
        }

        val canBuildOreRobot = this.oreRobotCost.canBuildRobot(state.ore, state.clay, state.obsidian)
        val canBuildClayRobot = this.clayRobotCost.canBuildRobot(state.ore, state.clay, state.obsidian)
        val canBuildObsidianRobot = this.obsidianRobotCost.canBuildRobot(state.ore, state.clay, state.obsidian)

        if (canBuildOreRobot && state.oreRobots < maxOreRobots) {
            val newStateWithOre = state.copy(
                minute = minute,
                ore = newOre - this.oreRobotCost.ore,
                clay = newClay - this.oreRobotCost.clay,
                obsidian = newObsidian - this.oreRobotCost.obsidian,
                geode = newGeode,
                oreRobots = state.oreRobots + 1
            )
            queueIfEligible(newStateWithOre)
        }
        if (canBuildClayRobot && state.clayRobots < maxClayRobots
        ) {
            val newStateWithClay = state.copy(
                minute = minute,
                ore = newOre - this.clayRobotCost.ore,
                clay = newClay - this.clayRobotCost.clay,
                obsidian = newObsidian - this.clayRobotCost.obsidian,
                geode = newGeode,
                clayRobots = state.clayRobots + 1
            )
            queueIfEligible(newStateWithClay)
        }

        if (canBuildObsidianRobot && state.obsidianRobots < maxObsidianRobots) {
            val newStateWithObsidian = state.copy(
                minute = minute,
                ore = newOre - this.obsidianRobotCost.ore,
                clay = newClay - this.obsidianRobotCost.clay,
                obsidian = newObsidian - this.obsidianRobotCost.obsidian,
                geode = newGeode,
                obsidianRobots = state.obsidianRobots + 1
            )
            queueIfEligible(newStateWithObsidian)
        }

        val newStateCollect = state.copy(
            minute = minute,
            ore = newOre,
            clay = newClay,
            obsidian = newObsidian,
            geode = newGeode
        )
        queueIfEligible(newStateCollect)
    }

    return maxGeodes
}


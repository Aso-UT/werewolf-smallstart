package org.example

class TestLodge(private vararg val assignments: Pair<Player, Role>) : Lodge() {
    override fun assignments() = assignments.toList()
}

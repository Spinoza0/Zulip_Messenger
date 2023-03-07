package com.spinoza.messenger_tfs.domain

class Cursor {
    var x = 0
        private set
    var y = 0
        private set

    fun reset(x: Int = UNDEFINED_POSITION, y: Int = UNDEFINED_POSITION) {
        if (x != UNDEFINED_POSITION && y != UNDEFINED_POSITION) {
            this.x = x
            this.y = y
            return
        }
        if (x == UNDEFINED_POSITION && y == UNDEFINED_POSITION) {
            this.x = START_POSITION
            this.y = START_POSITION
            return
        }
        if (x == UNDEFINED_POSITION) this.y = y else this.x = x
    }

    fun left(offset: Int) {
        x -= offset
    }

    fun right(offset: Int) {
        x += offset
    }

    fun up(offset: Int) {
        y -= offset
    }

    fun down(offset: Int) {
        y += offset
    }

    private companion object {
        const val START_POSITION = 0
        const val UNDEFINED_POSITION = -1
    }
}
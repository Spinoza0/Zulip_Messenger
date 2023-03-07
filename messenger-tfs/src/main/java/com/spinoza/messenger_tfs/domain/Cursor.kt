package com.spinoza.messenger_tfs.domain

class Cursor {
    var x = 0
        private set
    var y = 0
        private set

    fun resetX(x: Int) {
        reset(x, this.y)
    }

    fun resetY(y: Int) {
        reset(this.x, y)
    }

    fun reset(x: Int, y: Int) {
        this.x = x
        this.y = y
    }

    fun reset() {
        reset(START_POSITION, START_POSITION)
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
    }
}
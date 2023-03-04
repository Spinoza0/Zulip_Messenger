package com.spinoza.messenger_tfs.domain

class CursorXY {
    private var _x = 0
    val x: Int
        get() = _x

    private var _y = 0
    val y: Int
        get() = _y

    fun resetX(x: Int) {
        reset(x, y)
    }

    fun resetY(y: Int) {
        reset(x, y)
    }

    fun reset(x: Int, y: Int) {
        _x = x
        _y = y
    }

    fun left(offset: Int) {
        _x -= offset
    }

    fun right(offset: Int) {
        _x += offset
    }

    fun up(offset: Int) {
        _y -= offset
    }

    fun down(offset: Int) {
        _y += offset
    }
}
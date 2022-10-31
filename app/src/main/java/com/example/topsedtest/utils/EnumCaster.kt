package com.example.topsedtest.utils

object EnumCaster {
    inline fun <reified E : Enum<E>> fromInt(value: Int): E? {
        return enumValues<E>().firstOrNull { it.toString().toInt() == value }
    }
}
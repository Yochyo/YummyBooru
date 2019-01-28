package de.yochyo.ybooru.utils

fun large(id: Int) = "${id}Large"
fun preview(id: Int) = "${id}Preview"
fun original(id: Int) = "${id}Original"
fun large(id: String) = "${id}Large"
fun preview(id: String) = "${id}Preview"
fun original(id: String) = "${id}Original"

fun String.toTagArray(): Array<String> = split(" ").toTypedArray()
fun Array<String>.toTagString() = joinToString(" ")
fun List<String>.toTagString() = joinToString(" ")

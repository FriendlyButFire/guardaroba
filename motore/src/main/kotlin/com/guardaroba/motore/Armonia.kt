package com.guardaroba.motore

import kotlin.math.abs

// Tinta HSL -> tinta RYB (ruota del pittore), lineare a tratti: rosso 0, arancione 60, giallo 120, verde 180, blu 240, viola 300.
private val HSL_RYB = listOf(0 to 0, 30 to 60, 60 to 120, 120 to 180, 240 to 240, 300 to 300, 360 to 360)

private fun tintaRyb(h: Int): Double {
    val (a, b) = HSL_RYB.zipWithNext().first { (p, q) -> h in p.first..q.first }
    return a.second + (h - a.first) * (b.second - a.second).toDouble() / (b.first - a.first)
}

/**
 * L'Armonia tra due Colori non neutri distinti, o null se non armonizzano (soglie in ADR-0004).
 * Conta solo la tinta RYB: saturazione e luminosità non entrano nella regola.
 */
// shortcut: soglie tarate sulla Palette fissa; con colori personalizzati servono anche ΔL e S (ADR-0004).
internal fun armonia(a: Colore, b: Colore): String? {
    val diff = abs(tintaRyb(a.h) - tintaRyb(b.h)) % 360
    val d = minOf(diff, 360 - diff)
    return when {
        d <= 12 -> "monocromatico"
        d <= 75 -> "analoghi"
        d >= 150 -> "complementari"
        else -> null
    }
}

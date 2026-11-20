package com.guardaroba.motore

/** Una voce della Palette. [id] è stabile: è quello che si salva nel database. HSL in gradi e percentuali. */
data class Colore(val id: String, val nome: String, val h: Int, val s: Int, val l: Int, val neutro: Boolean)

private fun neutro(id: String, h: Int, s: Int, l: Int) = Colore(id, id.replace('_', ' '), h, s, l, neutro = true)
private fun colorato(id: String, h: Int, s: Int, l: Int) = Colore(id, id.replace('_', ' '), h, s, l, neutro = false)

/** I 25 Colori fissi (valori da build-notes/color-harmony.md, ADR-0004): prima i 10 Neutri. */
val PALETTE: List<Colore> = listOf(
    neutro("nero", 0, 0, 5),
    neutro("bianco", 0, 0, 98),
    neutro("panna", 45, 60, 92),
    neutro("grigio_chiaro", 0, 0, 78),
    neutro("grigio_antracite", 210, 8, 25),
    neutro("beige", 38, 35, 78),
    neutro("cammello", 33, 45, 58),
    neutro("marrone", 25, 50, 28),
    neutro("blu_navy", 220, 60, 20),
    neutro("denim", 215, 40, 42),
    colorato("rosso", 355, 80, 48),
    colorato("bordeaux", 345, 65, 27),
    colorato("rosa", 345, 70, 82),
    colorato("corallo", 8, 80, 64),
    colorato("arancione", 25, 90, 52),
    colorato("giallo", 50, 95, 55),
    colorato("senape", 43, 75, 45),
    colorato("verde_oliva", 70, 35, 35),
    colorato("verde_bosco", 130, 55, 25),
    colorato("verde_salvia", 95, 20, 60),
    colorato("azzurro", 205, 75, 70),
    colorato("blu_royal", 225, 75, 45),
    colorato("turchese", 175, 65, 45),
    colorato("viola", 275, 50, 40),
    colorato("lilla", 275, 45, 78),
)

private val PER_ID = PALETTE.associateBy { it.id }

/** Il Colore con questo id; fallisce se l'id non è nella Palette (dato corrotto). */
fun colore(id: String): Colore = PER_ID[id] ?: error("Colore sconosciuto: $id")

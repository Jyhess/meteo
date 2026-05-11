package com.meteo.app.ui

internal fun weatherEmoji(label: String): String {
    val normalized = label.lowercase()
    return when {
        "orage" in normalized -> "⛈️"
        "neige" in normalized -> "❄️"
        ("pluie" in normalized) || ("averse" in normalized) || ("bruine" in normalized) -> "🌧️"
        "brouillard" in normalized -> "🌫️"
        "dégagé" in normalized || "ensoleillé" in normalized -> "☀️"
        "nuage" in normalized -> "⛅"
        else -> "🌤️"
    }
}

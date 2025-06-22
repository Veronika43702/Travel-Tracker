package ru.nikfirs.android.traveltracker.core.domain.model

data class Country(
    val code: String,
    val nameEn: String,
    val nameRu: String
) {
    fun getDisplayName(locale: String): String {
        val name = if (locale.startsWith("ru")) nameRu else nameEn
        return "$name ($code)"
    }
}

object SchengenCountries {
    val countries = listOf(
        Country("AT", "Austria", "Австрия"),
        Country("BE", "Belgium", "Бельгия"),
        Country("BG", "Bulgaria", "Болгария"),
        Country("HR", "Croatia", "Хорватия"),
        Country("CY", "Cyprus", "Кипр"),
        Country("CZ", "Czech Republic", "Чехия"),
        Country("DK", "Denmark", "Дания"),
        Country("EE", "Estonia", "Эстония"),
        Country("FI", "Finland", "Финляндия"),
        Country("FR", "France", "Франция"),
        Country("DE", "Germany", "Германия"),
        Country("GR", "Greece", "Греция"),
        Country("HU", "Hungary", "Венгрия"),
        Country("IS", "Iceland", "Исландия"),
        Country("IE", "Ireland", "Ирландия"),
        Country("IT", "Italy", "Италия"),
        Country("LV", "Latvia", "Латвия"),
        Country("LI", "Liechtenstein", "Лихтенштейн"),
        Country("LT", "Lithuania", "Литва"),
        Country("LU", "Luxembourg", "Люксембург"),
        Country("MT", "Malta", "Мальта"),
        Country("NL", "Netherlands", "Нидерланды"),
        Country("NO", "Norway", "Норвегия"),
        Country("PL", "Poland", "Польша"),
        Country("PT", "Portugal", "Португалия"),
        Country("RO", "Romania", "Румыния"),
        Country("SK", "Slovakia", "Словакия"),
        Country("SI", "Slovenia", "Словения"),
        Country("ES", "Spain", "Испания"),
        Country("SE", "Sweden", "Швеция"),
        Country("CH", "Switzerland", "Швейцария")
    )

    fun getCountryByCode(code: String): Country? {
        return countries.find { it.code == code }
    }

    fun getCountryByName(name: String): Country? {
        return countries.find { it.nameEn == name || it.nameRu == name }
    }
}
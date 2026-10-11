package com.servicedesk.app.locale

enum class AppLanguage(
    val tag: String,
    val apiLang: String,
) {
    RUSSIAN("ru", "ru"),
    CHINESE("zh-CN", "zh"),
    ;

    companion object {
        fun fromTag(tag: String?): AppLanguage =
            entries.find { it.tag == tag || it.apiLang == tag } ?: RUSSIAN
    }
}

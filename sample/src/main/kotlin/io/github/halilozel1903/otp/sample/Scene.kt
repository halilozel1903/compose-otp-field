package io.github.halilozel1903.otp.sample

/** Screenshot scenes, picked with the `scene` intent extra. */
enum class Scene(val key: String) {
    Otp("otp"),
    Error("error"),
    Pin("pin"),
    Tablet("tablet"),
    ;

    companion object {
        fun from(key: String?): Scene? = entries.firstOrNull { it.key == key }
    }
}

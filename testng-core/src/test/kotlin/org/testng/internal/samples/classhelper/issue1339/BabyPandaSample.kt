package org.testng.internal.samples.classhelper.issue1339

class BabyPandaSample : LittlePandaSample() {
    private var name = "Po"

    override fun toString() = "BabyPanda{name='$name'}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BabyPandaSample

        if (name != other.name) return false

        return true
    }

    override fun hashCode() = name.hashCode()
}

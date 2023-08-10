package com.kalzakath.zoodle.data

enum class Month (
    val monthId: Int,
    val monthStr: String,
    val lenght: Int
) {
    JANVIER(1, "01", 31),
    FEVRIER_NB(2, "02", 28),
    FEVRIER_B(2, "02", 29),
    MARS(3, "03", 31),
    AVRIL(4, "04", 30),
    MAI(5, "05", 31),
    JUIN(6, "06", 30),
    JUILLET(7, "07", 31),
    AOUT(8, "08", 31),
    SEPTEMBRE(9, "09", 30),
    OCTOBRE(10, "10", 31),
    NOVEMBRE(11, "11", 30),
    DECEMBRE(12, "12",31);

    companion object {
        fun from(m: Int) : Month {
            return when (m) {
                1 -> JANVIER
                2 -> FEVRIER_NB
                3 -> MARS
                4 -> AVRIL
                5 -> MAI
                6 -> JUIN
                7 -> JUILLET
                8 -> AOUT
                9 -> SEPTEMBRE
                10 -> OCTOBRE
                11 -> NOVEMBRE
                12 -> DECEMBRE
                else -> FEVRIER_NB
            }
        }
    }
}
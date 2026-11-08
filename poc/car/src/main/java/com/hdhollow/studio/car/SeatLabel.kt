package com.hdhollow.studio.car

import ai.pleos.playground.vehicle.api.model.area.SeatArea
import ai.pleos.playground.vehicle.constant.area.SeatValue

/** 조명 패널에 보일 좌석 이름. 좌석 여러 개를 묶은 영역은 "전체". */
fun SeatArea.label(): String {
    val v = values
    if (v.size != 1) return "전체"
    return when (v[0]) {
        SeatValue.ROW_1_LEFT -> "운전석"
        SeatValue.ROW_1_RIGHT -> "동승석"
        SeatValue.ROW_1_CENTER -> "앞 가운데"
        SeatValue.ROW_2_LEFT -> "뒷좌석 왼쪽"
        SeatValue.ROW_2_CENTER -> "뒷좌석 가운데"
        SeatValue.ROW_2_RIGHT -> "뒷좌석 오른쪽"
        SeatValue.ROW_3_LEFT -> "3열 왼쪽"
        SeatValue.ROW_3_CENTER -> "3열 가운데"
        SeatValue.ROW_3_RIGHT -> "3열 오른쪽"
        else -> v[0].toString()
    }
}

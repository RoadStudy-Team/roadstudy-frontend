package com.roadstudy.automotive.session

import java.time.LocalTime

/** 내비에 즐겨찾기로 등록된 장소 하나 (집 또는 회사) */
data class CommutePlace(
    val name: String,
    val point: GeoPoint,
    /** 내비에 경로 미리보기를 요청할 때 다시 넘기는 장소 ID */
    val poiId: String,
    val poiSubId: String? = null
)

/** 내비 즐겨찾기의 집·회사. 등록하지 않은 쪽은 null */
data class CommutePlaces(
    val home: CommutePlace? = null,
    val work: CommutePlace? = null
) {
    val isEmpty: Boolean get() = home == null && work == null
}

/** 경로 안내를 시작하기 전에 홈에 미리 보여 줄 목적지 */
enum class CommuteTarget {
    WORK, HOME;

    val other: CommuteTarget get() = if (this == WORK) HOME else WORK
}

/** 안내를 시작하지 않고 내비에 물어 본 집·회사까지의 경로 */
data class RoutePreview(
    val target: CommuteTarget,
    val place: CommutePlace,
    val remainingMeters: Int,
    val etaSec: Int,
    val congestionCount: Int
)

/**
 * 내비에서 받은 출퇴근 정보.
 * [places] 가 null 이면 아직 내비에서 즐겨찾기를 받지 못한 것이다(연결 전이거나 SDK 가 없는 환경).
 * [previews] 에는 등록된 집·회사마다 경로 미리보기가 들어온다. 홈에서 회사/집 버튼을 누르면 바로 바꿔 보여 주기 위해 둘 다 받아 둔다.
 */
data class CommuteInfo(
    val places: CommutePlaces? = null,
    val previews: Map<CommuteTarget, RoutePreview> = emptyMap()
)

/** 경로 안내 전에 집과 회사 중 어디로 가는 길을 미리 보여 줄지 고른다 */
object CommuteTargetPicker {

    /** 이 시각 전에는 출근(회사), 이후에는 퇴근(집)으로 본다. 실제 출퇴근 루틴 설정이 생기면 그 값으로 바꾼다 */
    val AFTERNOON_FROM: LocalTime = LocalTime.of(14, 0)

    /** 시간대에 맞는 쪽이 등록돼 있지 않으면 다른 쪽을 고른다. 둘 다 없으면 null */
    fun pick(places: CommutePlaces, time: LocalTime): CommuteTarget? {
        val preferred = if (time < AFTERNOON_FROM) CommuteTarget.WORK else CommuteTarget.HOME
        return listOf(preferred, preferred.other).firstOrNull { placeOf(places, it) != null }
    }

    /** 사용자가 홈에서 고른 쪽([chosen])이 등록돼 있으면 그것을, 아니면 시간대로 고른다 */
    fun resolve(places: CommutePlaces, chosen: CommuteTarget?, time: LocalTime): CommuteTarget? =
        chosen?.takeIf { placeOf(places, it) != null } ?: pick(places, time)

    /** 등록된 쪽만 미리보기 요청 순서대로 돌려준다 (시간대에 맞는 쪽이 먼저) */
    fun requestOrder(places: CommutePlaces, time: LocalTime): List<CommuteTarget> {
        val first = pick(places, time) ?: return emptyList()
        return listOf(first, first.other).filter { placeOf(places, it) != null }
    }

    fun placeOf(places: CommutePlaces, target: CommuteTarget): CommutePlace? = when (target) {
        CommuteTarget.WORK -> places.work
        CommuteTarget.HOME -> places.home
    }
}

/**
 * 경로를 같은 길이로 나눈 구간들의 혼잡 여부(true = 서행·정체)에서 정체 구간 수를 센다.
 * 붙어 있는 혼잡 구간은 한 곳으로 센다. [서행, 정체, 원활, 서행] -> 2곳
 */
fun countCongestionZones(congested: List<Boolean>): Int =
    congested.indices.count { i -> congested[i] && (i == 0 || !congested[i - 1]) }

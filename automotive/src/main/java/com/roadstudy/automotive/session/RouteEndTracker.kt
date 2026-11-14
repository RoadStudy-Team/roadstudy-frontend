package com.roadstudy.automotive.session

/**
 * 세션 중에 내비 안내가 끝났는지 기억한다 (SDK·Android 비의존).
 *
 * - 안내 중인 경로를 한 번이라도 본 뒤에 경로가 없어지면(사용자가 안내 종료) 끝난 것이다.
 * - 내비가 도착을 알려도 끝난 것이다.
 * - 처음부터 경로가 없으면(내비 없이 출발) 끝난 것이 아니다. 시작 직후 내비 값이 아직 안 온 경우도 마찬가지다.
 * - 다시 안내가 시작되면 처음 상태로 돌아간다.
 */
class RouteEndTracker {

    private var seenRoute = false

    /** 안내가 끝났는지. [update] 로 내비 값을 받을 때마다 갱신된다 */
    var ended = false
        private set

    fun update(navi: NaviSignals): Boolean {
        when {
            navi.arrived -> ended = true
            navi.hasRoute -> {
                seenRoute = true
                ended = false
            }
            seenRoute -> ended = true
        }
        return ended
    }
}

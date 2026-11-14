package com.roadstudy.automotive.playback

import android.content.Context
import android.util.Log
import com.roadstudy.automotive.navi.CommuteHub
import com.roadstudy.automotive.navi.NaviSignalHub
import com.roadstudy.automotive.navi.NaviSignalSource
import com.roadstudy.automotive.session.CommutePlace
import com.roadstudy.automotive.vehicle.VehicleSignalHub
import com.roadstudy.automotive.vehicle.VehicleSignalSource

/**
 * 차량·내비 신호 수신을 앱 전체에서 하나만 켜 두는 곳.
 *
 * 화면(MainActivity)과 학습 세션이 각각 필요할 때 [acquire] 하고 다 쓰면 [release] 한다.
 * 하나라도 쓰고 있으면 켜 두므로, 내비가 앞으로 나와 화면이 가려져도 세션 중에는 신호를 계속 받는다.
 * 받은 값은 각 허브(VehicleSignalHub, NaviSignalHub, CommuteHub)로 보내고 화면과 세션이 구독한다.
 */
object SignalSources {

    private val users = mutableSetOf<String>()
    private var vehicle: VehicleSignalSource? = null
    private var navi: NaviSignalSource? = null

    /** 메인 스레드에서 부른다 */
    fun acquire(context: Context, user: String) {
        if (!users.add(user) || users.size > 1) return
        Log.d(TAG, "신호 수신 시작 ($user)")
        val app = context.applicationContext
        vehicle = VehicleSignalSource(app, onChanged = VehicleSignalHub::update).also { it.start() }
        navi = NaviSignalSource(app, onChanged = NaviSignalHub::update, onCommute = CommuteHub::update)
            .also { it.start() }
    }

    /** 메인 스레드에서 부른다 */
    fun release(user: String) {
        if (!users.remove(user) || users.isNotEmpty()) return
        Log.d(TAG, "신호 수신 끝 ($user)")
        vehicle?.stop()
        vehicle = null
        navi?.stop()
        navi = null
    }

    /** 내비에 [place] 로 경로 안내를 요청한다. 내비 앱이 앞으로 나온다. 수신 중이 아니면 아무것도 하지 않는다 */
    fun requestRoute(place: CommutePlace) {
        navi?.requestRoute(place) ?: Log.w(TAG, "내비 연결 전이라 경로 안내를 요청하지 못함")
    }

    private const val TAG = "RoadStudySignals"
}

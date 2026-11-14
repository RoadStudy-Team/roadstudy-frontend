package com.roadstudy.automotive.vehicle

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.roadstudy.automotive.session.Gear
import com.roadstudy.automotive.session.VehicleSignals

/**
 * 앱 전체가 함께 쓰는 최신 차량 신호. [VehicleSignalSource] 가 값을 넣고, 화면들이 구독한다.
 * 신호 수신은 [com.roadstudy.automotive.playback.SignalSources] 가 한 곳에서 켜므로, 화면마다 SDK 에 연결하지 않는다.
 */
object VehicleSignalHub {

    private val _signals = MutableLiveData(VehicleSignals())

    /** 지금까지 받은 차량 신호. 신호가 없으면 주차(P단) 기본값이다 */
    val signals: LiveData<VehicleSignals> = _signals

    fun update(signals: VehicleSignals) {
        _signals.postValue(signals)
    }

    /** 주행을 막 시작했는지. 주차나 중립에서 주행(D단)으로 바뀐 순간만 true */
    fun startedDriving(previous: Gear?, current: Gear): Boolean =
        previous != Gear.DRIVE && current == Gear.DRIVE
}

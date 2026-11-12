package com.roadstudy.automotive.navi

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.roadstudy.automotive.session.NaviSignals

/**
 * 앱 전체가 함께 쓰는 최신 내비 신호. [NaviSignalSource] 가 값을 넣고, 주행 화면이 구독한다.
 * 차량 신호의 [com.roadstudy.automotive.vehicle.VehicleSignalHub] 와 같은 방식이다.
 */
object NaviSignalHub {

    private val _signals = MutableLiveData(NaviSignals())

    /** 지금까지 받은 내비 신호. 경로 안내 중이 아니면 비어 있는 기본값이다 */
    val signals: LiveData<NaviSignals> = _signals

    fun update(signals: NaviSignals) {
        _signals.postValue(signals)
    }
}

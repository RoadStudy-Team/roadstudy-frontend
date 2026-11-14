package com.roadstudy.automotive.navi

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.roadstudy.automotive.session.CommuteInfo

/**
 * 앱 전체가 함께 쓰는 내비 즐겨찾기(집·회사)와 경로 미리보기. [NaviSignalSource] 가 값을 넣고, 홈·주행 화면이 구독한다.
 * 주행 신호([NaviSignalHub])와 나눈 이유: 미리보기 값이 바뀔 때마다 주행 세션이 다시 계산되지 않게 하기 위해서다.
 */
object CommuteHub {

    private val _info = MutableLiveData(CommuteInfo())

    /** 아직 내비에서 즐겨찾기를 받지 못했으면 places 가 null 이다 */
    val info: LiveData<CommuteInfo> = _info

    fun update(info: CommuteInfo) {
        _info.postValue(info)
    }
}

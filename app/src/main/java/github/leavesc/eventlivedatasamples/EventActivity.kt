package github.leavesc.eventlivedatasamples

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import github.leavesc.eventlivedata.EventLiveData
import kotlinx.android.synthetic.main.activity_event.*

/**
 * 作者：leavesC
 * 时间：2020/7/11 15:34
 * 描述：
 * GitHub：https://github.com/leavesC
 */
class EventActivity : AppCompatActivity() {

    companion object {

        val likeLiveData = MutableLiveData<Boolean>()

        val likeEventLiveData = EventLiveData<Boolean>()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event)
        likeLiveData.observe(this, Observer {
            iv_liveData.setImageResource(getDrwId(it))
            Toast.makeText(this, "MutableLiveData observe 收到消息了", Toast.LENGTH_SHORT).show()
        })
        likeEventLiveData.observe(this, Observer {
            iv_eventLiveDataObserve.setImageResource(getDrwId(it))
            Toast.makeText(this, "EventLiveData observe 收到消息了", Toast.LENGTH_SHORT).show()
        })
        likeEventLiveData.observeEvent(this, Observer {
            iv_eventLiveDataObserveEvent.setImageResource(getDrwId(it))
            Toast.makeText(this, "EventLiveData observeEvent 收到消息了", Toast.LENGTH_SHORT).show()
        })
        btn_changeState.setOnClickListener {
            likeLiveData.value = !(likeLiveData.value ?: false)
            likeEventLiveData.postValue(!(likeEventLiveData.value ?: false))
            checkState()
        }
        checkState()
    }

    private fun checkState() {
        tv_state.text = "当前的关注状态：\nlikeLiveData：${likeLiveData.value}\n" +
                "likeEventLiveData：${likeEventLiveData.value}" +
                "改变状态值会退出页面重新进入，观察各个 Observer 的不同表现 "
    }

    private fun getDrwId(like: Boolean) =
        if (like) R.drawable.icon_likes else R.drawable.icon_unlike

}
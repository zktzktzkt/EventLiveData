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
                "likeEventLiveData：${likeEventLiveData.value}\n\n" +
                "当第一次进入此 Activity 时，三个 Observer 均不会收到数据回调。当改变了关注状态后再进入 Activity 时，observeEvent 不会收到事件通知，从而避免收到旧数据（粘性数据）"
    }

    private fun getDrwId(like: Boolean) =
        if (like) R.drawable.icon_likes else R.drawable.icon_unlike

}
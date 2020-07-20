package github.leavesc.eventlivedatasamples

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import github.leavesc.eventlivedata.EventLiveData
import kotlinx.android.synthetic.main.activity_alive.*

/**
 * 作者：leavesC
 * 时间：2020/7/11 14:43
 * 描述：
 * GitHub：https://github.com/leavesC
 */
class AliveActivity : AppCompatActivity() {

    companion object {

        val likeLiveData = MutableLiveData<Boolean>()

        val likeAliveLiveData = EventLiveData<Boolean>()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alive)
        likeLiveData.observe(this, Observer {
            iv_liveData.setImageResource(getDrwId(it))
            Toast.makeText(this, "MutableLiveData observe 收到消息了", Toast.LENGTH_SHORT).show()
        })
        likeAliveLiveData.observeAlive(this, Observer {
            iv_eventLiveDataObserveLive.setImageResource(getDrwId(it))
            Toast.makeText(this, "EventLiveData observeAlive 收到消息了", Toast.LENGTH_SHORT).show()
        })
        likeAliveLiveData.observeAliveEvent(this, Observer {
            iv_eventLiveDataObserveAliveEvent.setImageResource(getDrwId(it))
            Toast.makeText(this, "EventLiveData observeAliveEvent 收到消息了", Toast.LENGTH_SHORT).show()
        })
        btn_changeState.setOnClickListener {
            likeLiveData.value = !(likeLiveData.value ?: false)
            likeAliveLiveData.postValue(!(likeAliveLiveData.value ?: false))
            checkState()
        }
        btn_changeState2.setOnClickListener {
            startActivity(Intent(this, Alive2Activity::class.java))
        }
        checkState()
    }

    private fun checkState() {
        tv_state.text = "当前的关注状态：\nlikeLiveData：${likeLiveData.value}\n" +
                "likeEventLiveData：${likeAliveLiveData.value}\n" +
                "改变状态值会退出页面重新进入，观察各个 Observer 的不同表现 "
    }

    private fun getDrwId(like: Boolean) =
        if (like) R.drawable.icon_likes else R.drawable.icon_unlike


}
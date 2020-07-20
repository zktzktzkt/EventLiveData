package github.leavesc.eventlivedatasamples

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_alive2.*

/**
 * 作者：leavesC
 * 时间：2020/7/11 17:55
 * 描述：
 * GitHub：https://github.com/leavesC
 */
class Alive2Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alive2)
        btn_changeState.setOnClickListener {
            AliveActivity.likeLiveData.value = !(AliveActivity.likeLiveData.value ?: false)
            AliveActivity.likeAliveLiveData.postValue(
                !(AliveActivity.likeAliveLiveData.value ?: false)
            )
        }
    }

}
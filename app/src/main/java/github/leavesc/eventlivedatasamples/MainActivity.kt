package github.leavesc.eventlivedatasamples

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

/**
 * 作者：leavesC
 * 时间：2020/7/11 14:35
 * 描述：
 * GitHub：https://github.com/leavesC
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn_event.setOnClickListener {
            startActivity(Intent(this, EventActivity::class.java))
        }
        btn_alive.setOnClickListener {
            startActivity(Intent(this, AliveActivity::class.java))
        }
    }

}
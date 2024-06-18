package ir.masouddabbaghi.oberbox.ui.base

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import ir.masouddabbaghi.oberbox.utils.Tools.hideKeyboard

@AndroidEntryPoint
abstract class BaseActivity : AppCompatActivity() {
    val tagLog: String = javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutResourceBinding())
    }

    protected abstract fun getLayoutResourceBinding(): View

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        hideKeyboard()
        return super.dispatchTouchEvent(ev)
    }
}

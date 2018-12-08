package bluestone.com.bluestone.`touch-handlers`

import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import bluestone.com.bluestone.utilities.printLog

class GestureListener(view: View) : GestureDetector.OnGestureListener {
    val targetView = view
    private var mMotionDownX: Float = 0.toFloat()
    private var mMotionDownY: Float = 0.toFloat()
    override fun onShowPress(e: MotionEvent?) {
        printLog("onShowPress")
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        printLog("onSingleTapUp")
        return false
    }

    override fun onDown(e: MotionEvent?): Boolean {
        printLog("onDown")
        e?.let {motionEvent ->
            mMotionDownX = motionEvent.getRawX() - targetView.translationX
            mMotionDownY = motionEvent.getRawY() - targetView.translationY
        }
        return false
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
        printLog("onFling")
        return false
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        printLog("onScroll x=$distanceX y=$distanceY xoffset = ${targetView.translationX} yoffset = ${targetView.translationY}")
        e2?.let {
            targetView.translationX = it.rawX - mMotionDownX
            targetView.translationY = it.rawY - mMotionDownY

        }
//        if (targetView.translationX == 0.0f){
//            targetView.translationX = 1.1f
//            targetView.translationY = 1.1f
//        }
//        else {
//            targetView.translationX = targetView.translationX * 1.1f
//            targetView.translationY = targetView.translationY * 1.1f
//        }
        return true
    }

    override fun onLongPress(e: MotionEvent?) {
        printLog("onLongPress")
    }
}
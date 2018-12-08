package bluestone.com.bluestone.utilities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView


const val TAG = "RVF_VIEW"
fun printLog(msg: String) {
    if (Log.isLoggable(TAG, Log.DEBUG))
        Log.d(TAG, "=-=-=-=-=-=-= $msg")
}

fun scaleImage(image: ImageView, scale: Float, targetImageView: ImageView, reset: Boolean) {
    if (reset) {
        val sourceBMP = (image.drawable as BitmapDrawable).bitmap
        targetImageView.setImageBitmap(sourceBMP)
        return
    }

    val sourceBMP = (image.drawable as BitmapDrawable).bitmap
    targetImageView.setImageBitmap(sourceBMP)
    targetImageView.scaleX = scale
    targetImageView.scaleY = 1.5f
}

fun zoomImageFromThumb(
    thumbView: View,
    imageResId: Int,
    mShortAnimationDuration: Int,
    expandedImageView: ImageView,
    containerView: ImageView
) {
    var mCurrentAnimator: Animator? = null
    // If there's an animation in progress, cancel it
    // immediately and proceed with this one.
    mCurrentAnimator?.cancel()

    // Load the high-resolution "zoomed-in" image.
    expandedImageView.setImageResource(imageResId)

    // Calculate the starting and ending bounds for the zoomed-in image.
    // This step involves lots of math. Yay, math.
    val startBoundsInt = Rect()
    val finalBoundsInt = Rect()
    val globalOffset = Point()

    // The start bounds are the global visible rectangle of the thumbnail,
    // and the final bounds are the global visible rectangle of the container
    // view. Also set the container view's offset as the origin for the
    // bounds, since that's the origin for the positioning animation
    // properties (X, Y).
    thumbView.getGlobalVisibleRect(startBoundsInt)
    containerView
        .getGlobalVisibleRect(finalBoundsInt, globalOffset)
    startBoundsInt.offset(-globalOffset.x, -globalOffset.y)
    finalBoundsInt.offset(-globalOffset.x, -globalOffset.y)

    val startBounds = RectF(startBoundsInt)
    val finalBounds = RectF(finalBoundsInt)

    // Adjust the start bounds to be the same aspect ratio as the final
    // bounds using the "center crop" technique. This prevents undesirable
    // stretching during the animation. Also calculate the start scaling
    // factor (the end scaling factor is always 1.0).
    val startScale: Float
    if ((finalBounds.width() / finalBounds.height() > startBounds.width() / startBounds.height())) {
        // Extend start bounds horizontally
        startScale = startBounds.height() / finalBounds.height()
        val startWidth: Float = startScale * finalBounds.width()
        val deltaWidth: Float = (startWidth - startBounds.width()) / 2
        startBounds.left -= deltaWidth.toInt()
        startBounds.right += deltaWidth.toInt()
    } else {
        // Extend start bounds vertically
        startScale = startBounds.width() / finalBounds.width()
        val startHeight: Float = startScale * finalBounds.height()
        val deltaHeight: Float = (startHeight - startBounds.height()) / 2f
        startBounds.top -= deltaHeight.toInt()
        startBounds.bottom += deltaHeight.toInt()
    }

    // Hide the thumbnail and show the zoomed-in view. When the animation
    // begins, it will position the zoomed-in view in the place of the
    // thumbnail.
    thumbView.alpha = 0f
    expandedImageView.visibility = View.VISIBLE

    // Set the pivot point for SCALE_X and SCALE_Y transformations
    // to the top-left corner of the zoomed-in view (the default
    // is the center of the view).
    expandedImageView.pivotX = 0f
    expandedImageView.pivotY = 0f

    // Construct and run the parallel animation of the four translation and
    // scale properties (X, Y, SCALE_X, and SCALE_Y).
    mCurrentAnimator = AnimatorSet().apply {
        play(
            ObjectAnimator.ofFloat(
                expandedImageView,
                View.X,
                startBounds.left,
                finalBounds.left
            )
        ).apply {
            with(ObjectAnimator.ofFloat(expandedImageView, View.Y, startBounds.top, finalBounds.top))
            with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X, startScale, 1f))
            with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_Y, startScale, 1f))
        }
        duration = mShortAnimationDuration.toLong()
        interpolator = DecelerateInterpolator()
        addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationEnd(animation: Animator) {
                mCurrentAnimator = null
            }

            override fun onAnimationCancel(animation: Animator) {
                mCurrentAnimator = null
            }
        })
        start()
    }

    // Upon clicking the zoomed-in image, it should zoom back down
    // to the original bounds and show the thumbnail instead of
    // the expanded image.
    expandedImageView.setOnClickListener {
        mCurrentAnimator?.cancel()

        // Animate the four positioning/sizing properties in parallel,
        // back to their original values.
        mCurrentAnimator = AnimatorSet().apply {
            play(ObjectAnimator.ofFloat(expandedImageView, View.X, startBounds.left)).apply {
                with(ObjectAnimator.ofFloat(expandedImageView, View.Y, startBounds.top))
                with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X, startScale))
                with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_Y, startScale))
            }
            duration = mShortAnimationDuration.toLong()
            interpolator = DecelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {

                override fun onAnimationEnd(animation: Animator) {
                    thumbView.alpha = 1f
                    expandedImageView.visibility = View.GONE
                    mCurrentAnimator = null
                }

                override fun onAnimationCancel(animation: Animator) {
                    thumbView.alpha = 1f
                    expandedImageView.visibility = View.GONE
                    mCurrentAnimator = null
                }
            })
            start()
        }
    }
}
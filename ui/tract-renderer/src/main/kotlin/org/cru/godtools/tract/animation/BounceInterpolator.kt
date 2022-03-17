package org.cru.godtools.tract.animation

import android.view.animation.Interpolator
import javax.annotation.concurrent.Immutable
import kotlin.math.pow
import kotlin.math.sqrt

private const val DEFAULT_BOUNCES = 4
private const val DEFAULT_HEIGHT_DECAY = 0.75

@Immutable
class BounceInterpolator(
    private val bounces: Int = DEFAULT_BOUNCES,
    decay: Double = DEFAULT_HEIGHT_DECAY
) : Interpolator {
    private val heightDecay = 1 - decay
    private val timeDecay = sqrt(heightDecay)
    private val totalTime = (0 until bounces).map { timeDecay.pow(it) }.sum()

    fun getTotalDuration(firstBounceDuration: Long) = (firstBounceDuration * totalTime).toLong()

    override fun getInterpolation(input: Float): Float {
        if (input <= 0 || input >= 1) return 0f

        // determine which bounce this is (and the x offset)
        var inputOffset = 0.0
        var bounce = 0
        while (bounce < bounces) {
            val bounceDuration = timeDecay.pow(bounce) / totalTime
            if (input <= inputOffset + bounceDuration) {
                // current bounce, center the quadratic for this bounce and quit looping
                inputOffset += bounceDuration / 2
                break
            }
            inputOffset += bounceDuration
            bounce++
        }

        // Our base quadratic equation is "-4x^2", it is a negative equation that can be shifted to completely fill the
        // (0,0) - (1,1) area of an interpolator curve.
        // we shift & scale this quadratic equation to make each bounce curve in this interpolator.
        // x is our input value shifted to center the curve on the y axis
        val x = input - inputOffset
        val q = -4 * x * x

        // we scale the quadratic equation to match the width of the first bounce. we do this by dividing the quadratic
        // by the first bounce width squared.
        // width = 1 / totalTime
        // q / width^2 ≡ q * totalTime^2
        val output = q * totalTime * totalTime

        // we calculate the output offset by decaying the full bounce by the number of bounces
        return (output + heightDecay.pow(bounce)).toFloat()
    }
}

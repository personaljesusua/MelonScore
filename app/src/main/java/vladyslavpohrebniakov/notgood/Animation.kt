package vladyslavpohrebniakov.notgood

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.transition.AutoTransition
import android.support.transition.TransitionManager

class Animation(private val constraintLayout: ConstraintLayout,
				context: Context, constraintLayoutId: Int) {

	private val constraintSet1: ConstraintSet = ConstraintSet()
	private val constraintSet2 = ConstraintSet()
	private val transition = AutoTransition()

	init {
		constraintSet1.clone(constraintLayout)
		constraintSet2.clone(context, constraintLayoutId)
		transition.duration = 700
	}

	fun animateWithConstraints(changed: Boolean) {
		TransitionManager.beginDelayedTransition(constraintLayout, transition)
		val constraint = if (!changed) constraintSet1 else constraintSet2
		constraint.applyTo(constraintLayout)
	}
}

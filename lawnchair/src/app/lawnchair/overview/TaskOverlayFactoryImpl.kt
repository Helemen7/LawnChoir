package app.lawnchair.overview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import androidx.annotation.Keep
import com.android.quickstep.TaskOverlayFactory
import com.android.quickstep.views.TaskContainer
import com.android.systemui.shared.recents.model.Task

@Keep
class TaskOverlayFactoryImpl(@Suppress("UNUSED_PARAMETER") context: Context) : TaskOverlayFactory() {

    override fun createOverlay(thumbnailView: TaskContainer) = TaskOverlay(thumbnailView)

    class TaskOverlay(
        taskThumbnailView: TaskContainer,
    ) : TaskOverlayFactory.TaskOverlay<LawnchairOverviewActionsView>(taskThumbnailView) {

        /**
         * Installs the callbacks of the overview actions. The base class installs its own, which
         * have no share or lens action, and [LawnchairOverviewActionsView] needs those to be the
         * ones in [OverlayUICallbacks].
         */
        override fun initOverlay(task: Task, thumbnail: Bitmap?, matrix: Matrix, rotated: Boolean) {
            if (thumbnail != null) {
                actionsView.setCallbacks(LawnchairOverlayCallbacks(isRealSnapshot(), task))
            }
        }

        // The actions below are protected or package level on the base class, so the callbacks go
        // through these.
        private fun screenshot(task: Task) = endLiveTileMode { saveScreenshot(task) }

        private fun splitSelect() = endLiveTileMode { enterSplitSelect() }

        private fun appPair() = endLiveTileMode { saveAppPair() }

        private fun share() = endLiveTileMode {
            val bitmap = getThumbnail() ?: return@endLiveTileMode
            mImageApi.startShareActivity(Rect(0, 0, bitmap.width, bitmap.height))
        }

        private fun lens() = endLiveTileMode { mImageApi.startLensActivity() }

        private fun blockedByPolicy() = showBlockedByPolicyMessage()

        private inner class LawnchairOverlayCallbacks(
            private val isAllowedByPolicy: Boolean,
            private val task: Task,
        ) : OverlayUICallbacks {

            override fun onScreenshot() = screenshot(task)

            override fun onSplit() = splitSelect()

            override fun onSaveAppPair() = appPair()

            override fun onShare() {
                if (isAllowedByPolicy) share() else blockedByPolicy()
            }

            override fun onLens() {
                if (isAllowedByPolicy) lens() else blockedByPolicy()
            }

            override fun onLocked(context: Context, task: Task) {
                // Locking tasks from the overview is not supported.
            }
        }
    }

    sealed interface OverlayUICallbacks : TaskOverlayFactory.OverlayUICallbacks {
        fun onShare()
        fun onLens()
        fun onLocked(context: Context, task: Task)
    }
}

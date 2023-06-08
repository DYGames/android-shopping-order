package woowacourse.shopping.utils

import android.util.Log
import woowacourse.shopping.error.DataError
import woowacourse.shopping.error.ErrorView

object ErrorHandler {
    fun Throwable.handle(errorView: ErrorView) {
        Log.d("Error", message.orEmpty())
        Log.d("Error", stackTraceToString())
        if (this is DataError)
            errorView.showError(viewMessage)
    }
}

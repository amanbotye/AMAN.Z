package ye.aman.client.core.result

import ye.aman.client.core.error.AppError

sealed interface AppResult<out T> {
    data class Success<out T>(val data: T) : AppResult<T>
    data class Error(val error: AppError) : AppResult<Nothing>

    fun isSuccess(): Boolean = this is Success
    fun getOrNull(): T? = (this as? Success)?.data
}

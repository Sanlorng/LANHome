package com.ycz.lanhome.model

data class RestResult<T>(
    val code: Int = -1,
    var message: String = "",
    var data: T? = null
) {

    companion object {
        fun success() = RestResult<Any>(ResultCode.SUCCESS.code, ResultCode.SUCCESS.message)

        fun <T> success(data: T) =
            RestResult<T>(ResultCode.SUCCESS.code, ResultCode.SUCCESS.message).apply {
                this.data = data
            }

        fun <T> failed(message: String = ResultCode.FAILED.message) =
            RestResult<T>(ResultCode.FAILED.code, message)

        fun <T> noAccess() = RestResult<T>(ResultCode.NO_ACCESS.code, ResultCode.NO_ACCESS.message)

        fun <T> notFound() = RestResult<T>(ResultCode.NOT_FOUND.code, ResultCode.NOT_FOUND.message)
        fun <T> loading() = RestResult<T>(ResultCode.LOADING.code, ResultCode.LOADING.message)
        fun <T> loadFinish() = RestResult<T>(ResultCode.LOADED.code, ResultCode.LOADED.message)
    }
}

enum class ResultCode(val code: Int = -1, var message: String = "") {
    LOADING(100),
    LOADED(101),
    SUCCESS(200),
    FAILED(-1),
    NOT_FOUND(404),
    NO_ACCESS(403)
}
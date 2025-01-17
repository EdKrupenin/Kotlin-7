@file:Suppress("unused")

package ru.kotlin.homework.network

import ru.kotlin.homework.Circle
import java.lang.IllegalArgumentException
import java.time.LocalDateTime

/**
 * Известный вам список ошибок
 */
sealed class ApiException(message: String) : Throwable(message) {
    data object NotAuthorized : ApiException("Not authorized")
    data object NetworkException : ApiException("Not connected")
    data object UnknownException : ApiException("Unknown exception")
}

interface ReadLog<out E> {
    fun dump(): List<Pair<LocalDateTime, E>>
}

interface WriteLog<in E> {
    fun log(response: NetworkResponse<*, E>)
    fun dumpLog()
}

class ErrorLogger<E : Throwable> : WriteLog<E>, ReadLog<E> {

    private val errors = mutableListOf<Pair<LocalDateTime, E>>()

    override fun log(response: NetworkResponse<*, E>) {
        if (response is Failure) {
            errors.add(response.responseDateTime to response.error)
        }
    }

    override fun dump(): List<Pair<LocalDateTime, E>> {
        return errors
    }

    override fun dumpLog() {
        errors.forEach { (date, error) ->
            println("Error at $date: ${error.message}")
        }
    }
}

fun processThrowable(logger: WriteLog<Throwable>) {
    logger.log(Success("Success"))
    Thread.sleep(100)
    logger.log(Success(Circle))
    Thread.sleep(100)
    logger.log(Failure(IllegalArgumentException("Something unexpected")))

    logger.dumpLog()
}

fun processApiErrors(apiExceptionLogger: WriteLog<ApiException>) {
    apiExceptionLogger.log(Success("Success"))
    Thread.sleep(100)
    apiExceptionLogger.log(Success(Circle))
    Thread.sleep(100)
    apiExceptionLogger.log(Failure(ApiException.NetworkException))

    apiExceptionLogger.dumpLog()
}

fun main() {
    val logger = ErrorLogger<Throwable>()

    println("Processing Throwable:")
    processThrowable(logger)

    println("Processing Api:")
    processApiErrors(logger)
}


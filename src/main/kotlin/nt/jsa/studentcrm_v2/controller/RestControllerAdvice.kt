package nt.jsa.studentcrm_v2.controller

import nt.jsa.studentcrm_v2.exceptions.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler


@ControllerAdvice
class RestControllerAdvice : ResponseEntityExceptionHandler() {
    @ExceptionHandler(value = [StudentNotFoundException::class])
    fun handleStudentNotFoundException(ex: Exception, request: WebRequest): ResponseEntity<String> {
        return ResponseEntity(ex.message, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(value = [StudentAlreadyExistsException::class])
    fun handleStudentAlreadyExistsException(ex: Exception, request: WebRequest): ResponseEntity<String> {
        return ResponseEntity(ex.message, HttpStatus.CONFLICT)
    }

    @ExceptionHandler(value = [CourseNotFoundException::class])
    fun handleCourseNotFoundException(ex: Exception, request: WebRequest): ResponseEntity<String> {
        return ResponseEntity(ex.message, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(value = [CourseAlreadyExistsException::class])
    fun handleCourseAlreadyExistsException(ex: Exception, request: WebRequest): ResponseEntity<String> {
        return ResponseEntity(ex.message, HttpStatus.CONFLICT)
    }

    @ExceptionHandler(value = [InvalidDataException::class])
    fun handleValidationException(ex: Exception, request: WebRequest): ResponseEntity<String> {
        return ResponseEntity(ex.message, HttpStatus.BAD_REQUEST)
    }
}
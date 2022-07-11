package nt.jsa.studentcrm_v2.controller

import nt.jsa.studentcrm_v2.model.Course
import nt.jsa.studentcrm_v2.service.CourseService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@CrossOrigin(origins = ["*"], allowedHeaders = ["*"])
@RequestMapping("/v1/courses")
class CourseController @Autowired constructor(
    val courseService: CourseService
) {
    @GetMapping
    fun getAllCourses(): ResponseEntity<Any> {
        val courses = object {
            val courses = courseService.findAll()
        }

        return ResponseEntity.ok(courses)
    }

    @GetMapping("/allCourses")
    fun getAllCoursesNoObject(): ResponseEntity<List<Course>> {
        return ResponseEntity.ok(courseService.findAll())
    }

    @GetMapping("/{id}")
    fun findCourseById(@PathVariable id: String): ResponseEntity<Course> {
        val course = courseService.findById(id)
        return ResponseEntity.ok(course)
    }

    @PostMapping
    fun createCourse(@Valid @RequestBody course: Course): ResponseEntity<Course> {
        val createdCourse = courseService.createCourse(course)
        return ResponseEntity(createdCourse, HttpStatus.CREATED)
    }

    @PutMapping("/{id}")
    fun updateCourse(@Valid @RequestBody course: Course, @PathVariable id: String): ResponseEntity<Course> {
        val updatedCourse = courseService.updateCourse(course)
        return ResponseEntity.ok(updatedCourse)
    }

    @DeleteMapping("/{id}")
    fun deleteStudent(@PathVariable id: String): ResponseEntity<String> {
        courseService.deleteById(id)
        return ResponseEntity.ok(id)
    }
}

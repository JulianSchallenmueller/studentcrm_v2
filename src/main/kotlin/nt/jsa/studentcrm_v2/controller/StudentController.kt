package nt.jsa.studentcrm_v2.controller

import nt.jsa.studentcrm_v2.model.Student
import nt.jsa.studentcrm_v2.service.StudentService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@CrossOrigin(origins = ["*"], allowedHeaders = ["*"])
@RequestMapping("/v1/students")
class StudentController @Autowired constructor(
    val studentService: StudentService
) {
    @GetMapping
    fun getAllStudents(): ResponseEntity<Any> {
        val students = object {
            val students = studentService.findAll()
        }

        return ResponseEntity.ok(students)
    }

    @GetMapping("/allStudents")
    fun getAllStudentsNoObject(): ResponseEntity<List<Student>> {
        val students = studentService.findAll()
        return ResponseEntity.ok(students)
    }

    @GetMapping("/{id}")
    fun findStudentById(@PathVariable id: String): ResponseEntity<Student> {
        val student = studentService.findById(id)
        return ResponseEntity.ok(student)
    }

    @PostMapping
    fun createStudent(@Valid @RequestBody student: Student): ResponseEntity<Student> {
        studentService.createStudent(student)
        return ResponseEntity(student, HttpStatus.CREATED)
    }

    @PutMapping("/{id}")
    fun updateStudent(@Valid @RequestBody student: Student, @PathVariable id: String): ResponseEntity<Student> {
        val updatedStudent = studentService.updateStudent(student)
        return ResponseEntity.ok(updatedStudent)
    }

    @DeleteMapping("/{id}")
    fun deleteStudent(@PathVariable id: String): ResponseEntity<String> {
        studentService.deleteById(id)
        return ResponseEntity.ok(id)
    }
}

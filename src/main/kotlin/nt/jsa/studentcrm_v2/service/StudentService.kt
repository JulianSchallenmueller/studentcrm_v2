package nt.jsa.studentcrm_v2.service

import nt.jsa.studentcrm_v2.exceptions.InvalidDataException
import nt.jsa.studentcrm_v2.exceptions.StudentAlreadyExistsException
import nt.jsa.studentcrm_v2.exceptions.StudentNotFoundException
import nt.jsa.studentcrm_v2.model.Course
import nt.jsa.studentcrm_v2.model.Student
import nt.jsa.studentcrm_v2.repository.CourseRepository
import nt.jsa.studentcrm_v2.repository.StudentRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Service

@Service
class StudentService @Autowired constructor(
    val studentRepository: StudentRepository,
    val courseRepository: CourseRepository,
) {
    fun findAll(): List<Student> {
        return studentRepository.findAll()
    }

    fun findById(id: String): Student {
        return studentRepository.findById(id)
            .orElseThrow { StudentNotFoundException("Student with id $id was not found") }
    }

    fun createStudent(student: Student): Student {
        try {
            if (studentRepository.existsByEmail(student.email)) throw StudentAlreadyExistsException(
                "Student with email [${student.email}] already exists"
            )
            return studentRepository.insert(student)
        } catch (e: DuplicateKeyException) {
            throw StudentAlreadyExistsException(
                "Student with id [${student.id}] or email [${student.email}] already exists"
            )
        } catch (e: InvalidDataException) {
            throw Exception(e)
        }
    }

    fun updateStudent(student: Student): Student {
        val originalStudent = studentRepository.findById(student.id)
            .orElseThrow { StudentNotFoundException("Student with id ${student.id} was not found") }
        if (student.email != originalStudent.email && studentRepository.existsByEmail(student.email))
            throw StudentAlreadyExistsException("Student with email [${student.email}] already exists")
        return studentRepository.save(
            Student(
                originalStudent.id,
                student.firstName,
                student.lastName,
                student.email,
                student.courses.also { updateCourses(student) }
            )
        )
    }

    fun deleteById(id: String) {
        studentRepository.findById(id)
            .orElseThrow { StudentNotFoundException("Student with id $id was not found") }
        studentRepository.deleteById(id)
    }

    fun updateCourses(student: Student) {
        for (course in student.courses) {
            if (!course.students.contains(student)) {
                courseRepository.save(
                    Course(
                        course.id,
                        course.description,
                        course.students.toMutableList().apply { this.add(student) }.toList()
                    )
                )
            }
        }
    }
}

package nt.jsa.studentcrm_v2.service

import nt.jsa.studentcrm_v2.exceptions.CourseAlreadyExistsException
import nt.jsa.studentcrm_v2.exceptions.CourseNotFoundException
import nt.jsa.studentcrm_v2.exceptions.InvalidDataException
import nt.jsa.studentcrm_v2.model.Course
import nt.jsa.studentcrm_v2.model.Student
import nt.jsa.studentcrm_v2.repository.CourseRepository
import nt.jsa.studentcrm_v2.repository.StudentRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Service

@Service
class CourseService @Autowired constructor(
    val courseRepository: CourseRepository,
    val studentRepository: StudentRepository
) {
    fun findAll(): List<Course> {
        return courseRepository.findAll()
    }

    fun findById(id: String): Course {
        return courseRepository.findById(id)
            .orElseThrow { CourseNotFoundException("Student with id [$id] was not found") }
    }

    fun createCourse(course: Course): Course {
        try {
            return courseRepository.insert(course)
        } catch (e: DuplicateKeyException) {
            throw CourseAlreadyExistsException(
                "Course with id [${course.id}] or description [${course.description}] already exists"
            )
        } catch (e: javax.validation.ConstraintViolationException) {
            throw InvalidDataException("Data violates constraints: " + e.message)
        }
    }

    fun updateCourse(course: Course): Course {
        val originalCourse = courseRepository.findById(course.id)
            .orElseThrow { CourseNotFoundException("Course with id [${course.id}] does not exist") }

        try {
            return courseRepository.save(
                Course(
                    originalCourse.id,
                    course.description,
                    course.students.also { updateStudent(course) }
                )
            )
        } catch (e: javax.validation.ConstraintViolationException) {
            throw InvalidDataException("Data violates constraints: " + e.message)
        }
    }

    fun deleteById(id: String) {
        courseRepository.findById(id)
            .orElseThrow { CourseNotFoundException("Course with id [$id] does not exist") }
        courseRepository.deleteById(id)
    }

    fun updateStudent(course: Course) {
        for (student in course.students) {
            if (!student.courses.contains(course)) {
                studentRepository.save(
                    Student(
                        student.id,
                        student.firstName,
                        student.lastName,
                        student.email,
                        student.courses.toMutableList().apply { this.add(course) }.toList()
                    )
                )
            }
        }
    }
}

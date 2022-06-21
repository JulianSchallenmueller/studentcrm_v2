package nt.jsa.studentcrm_v2.serviceIntTests

import nt.jsa.studentcrm_v2.MongoConfig
import nt.jsa.studentcrm_v2.exceptions.StudentAlreadyExistsException
import nt.jsa.studentcrm_v2.exceptions.StudentNotFoundException
import nt.jsa.studentcrm_v2.model.Course
import nt.jsa.studentcrm_v2.model.Student
import nt.jsa.studentcrm_v2.repository.CourseRepository
import nt.jsa.studentcrm_v2.repository.StudentRepository
import nt.jsa.studentcrm_v2.service.CourseService
import nt.jsa.studentcrm_v2.service.StudentService
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Import(MongoConfig::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = [Testcontainers::class])
class StudentServiceIntTest @Autowired constructor(
    val studentService: StudentService,
    val studentRepository: StudentRepository,
    val courseRepository: CourseRepository,
    val courseService: CourseService
) {
    companion object {
        @Container
        private val mongoDBContainer = MongoDBContainer("mongo:5.0.8").also { it.start() }

        @JvmStatic
        @DynamicPropertySource
        fun mongoDbProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.mongodb.uri") { mongoDBContainer.replicaSetUrl }
            registry.add("spring.data.mongodb.auto-index-creation") { true }
        }

        val student1 = Student(
            ObjectId().toHexString(),
            "firstName1",
            "lastName1",
            "firstNameLastName1@email.com",
            listOf()
        )
        val student2 = Student(
            ObjectId().toHexString(),
            "firstName2",
            "lastName2",
            "firstNameLastName2@email.com",
            listOf()
        )
        val student3 = Student(
            ObjectId().toHexString(),
            "firstName3",
            "lastName3",
            "firstNameLastName3@email.com",
            listOf()
        )
        val course1 = Course(
            ObjectId().toHexString(),
            "description1",
            listOf()
        )
        val course2 = Course(
            ObjectId().toHexString(),
            "description2",
            listOf()
        )
    }

    @BeforeEach
    fun clearDb() {
        studentRepository.deleteAll()
        courseRepository.deleteAll()
    }

    @Test
    fun `should get all students`() {
        createSampleStudents()
        assert(studentService.findAll().size == 3)
    }

    @Test
    fun `should get student by id`() {
        createSampleStudents()
        assert(student1 == studentService.findById(student1.id))
    }

    @Test
    fun `should throw StudentNotFoundException`() {
        assertThrows<StudentNotFoundException> { studentService.findById(student1.id) }
    }

    @Test fun `should create student`() {
        val createdStudent = studentService.createStudent(
            Student(
                ObjectId().toHexString(),
                "firstName42",
                "lastName42",
                "firstNameLastName1@email.com",
                listOf()))

        assert(createdStudent == studentService.findById(createdStudent.id))
    }

    @Test
    fun `should throw StudentAlreadyExistsException`() {
        createSampleStudents()
        val duplicatedEmailStudent = Student(
            ObjectId().toHexString(),
            "firstName42",
            "lastName42",
            "firstNameLastName1@email.com",
            listOf()
        )
        assertThrows<StudentAlreadyExistsException> { studentService.createStudent(duplicatedEmailStudent) }
    }

    @Test
    fun `should update student with name and courses`() {
        createSampleStudents()
        createSampleCourses()

        val existingStudent = studentRepository.findByEmail("firstNameLastName1@email.com")

        assert(studentService.findById(existingStudent.id).courses.isEmpty())

        val updatedStudent = studentService.updateStudent(
            Student(
                ObjectId(existingStudent.id).toHexString(),
                "Changed",
                "Also-changed",
                existingStudent.email,
                existingStudent.courses.toMutableList().apply { this.add(course1) }.toList()
            )
        )

        assert(existingStudent.id == updatedStudent.id)
        assert(updatedStudent.firstName == "Changed")
        assert(updatedStudent.lastName == "Also-changed")
        assert(existingStudent.email == updatedStudent.email)
        assert(updatedStudent.courses.size == 1)

        assert(courseService.findById(course1.id).students.size == 1)
        assert(studentService.findById(existingStudent.id).courses.size == 1)
    }

    @Test
    fun `should reject student update due to duplicated email`() {
        createSampleStudents()
        val updatedStudent = Student(
            student1.id,
            student1.firstName,
            student1.lastName,
            student2.email,
            student1.courses
        )

        assertThrows<StudentAlreadyExistsException> { studentService.updateStudent(updatedStudent) }
    }

    @Test
    fun `should delete student`() {
        createSampleStudents()
        assert(studentRepository.findAll().size == 3)
        studentService.deleteById(student1.id)
        assert(studentRepository.findAll().size == 2)
    }

    fun createSampleStudents() {
        studentRepository.save(student1)
        studentRepository.save(student2)
        studentRepository.save(student3)
    }

    fun createSampleCourses() {
        courseRepository.save(course1)
        courseRepository.save(course2)
    }
}
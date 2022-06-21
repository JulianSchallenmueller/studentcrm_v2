package nt.jsa.studentcrm_v2.serviceIntTests

import nt.jsa.studentcrm_v2.MongoConfig
import nt.jsa.studentcrm_v2.exceptions.CourseAlreadyExistsException
import nt.jsa.studentcrm_v2.exceptions.CourseNotFoundException
import nt.jsa.studentcrm_v2.exceptions.InvalidDataException
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
class CourseServiceIntTest @Autowired constructor(
    val courseService: CourseService,
    val courseRepository: CourseRepository,
    val studentRepository: StudentRepository,
    val studentService: StudentService
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
        courseRepository.deleteAll()
        studentRepository.deleteAll()
    }

    @Test
    fun `should get all courses`() {
        createSampleCourses()
        assert(courseService.findAll().size == 2)
    }

    @Test
    fun `should get course by id`() {
        createSampleCourses()
        assert(course1 == courseService.findById(course1.id))
    }

    @Test
    fun `should throw CourseNotFoundException`() {
        assertThrows<CourseNotFoundException> { courseService.findById(course1.id) }
    }

    @Test
    fun `should create Course`() {
        val createdCourse = courseService.createCourse(
            Course(
                ObjectId().toHexString(),
                "some description",
                listOf()
            )
        )

        assert(createdCourse == courseService.findById(createdCourse.id))
    }

    @Test
    fun `should reject invalid course data`() {
        val invalidCourse = Course(
            "1",
            "A very long description that should definitely be rejected because it is too long",
            listOf()
        )

        assertThrows<InvalidDataException> { courseService.createCourse(invalidCourse) }
    }

    @Test
    fun `should reject course`() {
        createSampleCourses()
        assertThrows<CourseAlreadyExistsException> { courseService.createCourse(
            Course(
                course1.id,
                "some description",
                listOf()
        )) }
    }

    @Test
    fun `should update course`() {
        createSampleCourses()
        createSampleStudents()

        val existingCourse = courseService.findById(course1.id)
        val updatedCourse = courseService.updateCourse(
            Course(
                existingCourse.id,
                "changed description",
                existingCourse.students.toMutableList().apply { this.add(student1) }.toList()
            )
        )
        assert(updatedCourse.id == existingCourse.id)
        println(updatedCourse.description)
        assert(updatedCourse.description == "changed description")
        assert(updatedCourse.students.size == 1)

        assert(studentService.findById(student1.id).courses.size == 1)
        assert(courseService.findById(existingCourse.id).students.size == 1)
    }

    @Test
    fun `should delete course`() {
        createSampleCourses()
        assert(courseRepository.findAll().size == 2)
        courseService.deleteById(course1.id)
        assert(courseRepository.findAll().size == 1)
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
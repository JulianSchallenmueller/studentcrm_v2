package nt.jsa.studentcrm_v2.restDocTests

import io.restassured.RestAssured
import io.restassured.specification.RequestSpecification
import nt.jsa.studentcrm_v2.MongoConfig
import nt.jsa.studentcrm_v2.model.Course
import nt.jsa.studentcrm_v2.model.Student
import nt.jsa.studentcrm_v2.repository.CourseRepository
import nt.jsa.studentcrm_v2.repository.StudentRepository
import nt.jsa.studentcrm_v2.service.StudentService
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.restdocs.payload.PayloadDocumentation
import org.springframework.restdocs.payload.PayloadDocumentation.beneathPath
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.restassured3.RestAssuredRestDocumentation
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestDocs
@Import(MongoConfig::class)
class CourseControllerDocTest @Autowired constructor(
    val studentRepository: StudentRepository,
    val courseRepository: CourseRepository,
    val studentService: StudentService,
) {
    @LocalServerPort
    private val port: Int = 0

    companion object {
        @Container
        private val mongoDBContainer = MongoDBContainer("mongo:5.0.8").also { it.start() }

        val student1 = Student("1", "first", "last", "firstlast1@email.com", listOf())
        val student2 = Student("2", "first", "last", "firstlast2@email.com", listOf())
        val student3 = Student("3", "first", "last", "firstlast3@email.com", listOf())

        val course1 = Course("1", "description 1", listOf())
        val course2 = Course("2", "description 2", listOf())

        @JvmStatic
        @DynamicPropertySource
        fun mongoDbProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.mongodb.uri") { mongoDBContainer.replicaSetUrl }
            registry.add("spring.data.mongodb.auto-index-creation") { true }
        }
    }

    @BeforeEach
    fun setupDb() {
        studentRepository.deleteAll()
        courseRepository.deleteAll()

        studentRepository.insert(student1)
        studentRepository.insert(student2)
        studentRepository.insert(student3)
        courseRepository.insert(course1)
        courseRepository.insert(course2)

        studentService.updateStudent(
            Student(
                student1.id,
                "Changed",
                "Also-changed",
                student1.email,
                student1.courses.toMutableList().apply { this.add(course1) }.toList()
            )
        )
    }

    @Test
    fun listCourses(@Autowired documentationSpec: RequestSpecification?, @LocalServerPort port: Int) {
        RestAssured.given(documentationSpec)
            .filter(
                RestAssuredRestDocumentation.document(
                    "list-courses",
                    PayloadDocumentation.responseFields(
                        beneathPath("courses.[0]"),
                        fieldWithPath("id").description("Course id"),
                        fieldWithPath("description").description("Description of the courses content"),
                        fieldWithPath("students").description("List of students enrolled"),
                        fieldWithPath("students.[0].id").ignored(),
                        fieldWithPath("students.[0].firstName").ignored(),
                        fieldWithPath("students.[0].lastName").ignored(),
                        fieldWithPath("students.[0].email").ignored()
                    )
                )
            )
            .`when`()
            .port(port)["/v1/courses"]
            .then().assertThat()
            .statusCode(Matchers.`is`(200))
            .body("courses.size", Matchers.equalTo(2))
    }
}

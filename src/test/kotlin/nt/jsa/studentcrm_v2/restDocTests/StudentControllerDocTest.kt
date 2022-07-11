package nt.jsa.studentcrm_v2.restDocTests

import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import nt.jsa.studentcrm_v2.MongoConfig
import nt.jsa.studentcrm_v2.model.Course
import nt.jsa.studentcrm_v2.model.Student
import nt.jsa.studentcrm_v2.repository.CourseRepository
import nt.jsa.studentcrm_v2.repository.StudentRepository
import nt.jsa.studentcrm_v2.service.StudentService
import org.apache.http.HttpStatus
import org.hamcrest.Matchers
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.restdocs.restassured3.RestAssuredRestDocumentation
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestDocs
@Import(MongoConfig::class)
class StudentControllerDocTest @Autowired constructor(
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
    fun listStudents(@Autowired documentationSpec: RequestSpecification?, @LocalServerPort port: Int) {
        RestAssured.given(documentationSpec)
            .filter(
                RestAssuredRestDocumentation.document(
                    "list-students",
                    responseFields(
                        fieldWithPath("students").description("List of Students"), // beneathPath("list.[0]"),
                        fieldWithPath("students.[*].id").description("Student Id"),
                        fieldWithPath("students.[*].firstName").description("First name of student"),
                        fieldWithPath("students.[*].lastName").description("Last name of student"),
                        fieldWithPath("students.[*].email").description("Email of student")
                    )
                )
            )
            .`when`()
            .port(port)["/v1/students"]
            .then().assertThat()
            .statusCode(Matchers.`is`(200))
            .body("students.size", equalTo(3))
    }

    @Test
    fun createStudent(@Autowired documentationSpec: RequestSpecification?, @LocalServerPort port: Int) {
        val requestBody = "{" +
            "    \"id\": \"4\"," +
            "    \"firstName\": \"first\"," +
            "    \"lastName\": \"last\"," +
            "    \"email\":\"e4@mail\"," +
            "    \"courses\": []" +
            "}"

        RestAssured.given(documentationSpec).accept(ContentType.JSON).contentType(ContentType.JSON)
            .filter(
                RestAssuredRestDocumentation.document(
                    "create-student",
                    requestFields(
                        fieldWithPath("id").description("Student Id").type(String::class),
                        fieldWithPath("firstName").description("First name").type(String::class),
                        fieldWithPath("lastName").description("last name").type(String::class),
                        fieldWithPath("email").description("Student email").type(String::class),
                        fieldWithPath("courses").description("Courses student is enrolled in").description(List::class),
                    )
                )
            )
            .body(requestBody)
            .`when`().port(port).post("/v1/students")
            .then().assertThat()
            .statusCode(HttpStatus.SC_CREATED)
            .header(HttpHeaders.CONTENT_TYPE, "application/json")
    }
}

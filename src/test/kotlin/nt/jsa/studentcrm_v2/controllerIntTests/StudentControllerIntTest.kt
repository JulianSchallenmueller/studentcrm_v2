package nt.jsa.studentcrm_v2.controllerIntTests

import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import nt.jsa.studentcrm_v2.MongoConfig
import nt.jsa.studentcrm_v2.model.Course
import nt.jsa.studentcrm_v2.model.Student
import nt.jsa.studentcrm_v2.repository.CourseRepository
import nt.jsa.studentcrm_v2.repository.StudentRepository
import nt.jsa.studentcrm_v2.service.StudentService
import nt.jsa.studentcrm_v2.serviceIntTests.StudentServiceIntTest
import org.apache.http.HttpStatus
import org.bson.types.ObjectId
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(MongoConfig::class)
class StudentControllerIntTest @Autowired constructor(
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
    }

    @Test
    fun `on get all students return with status 200`() {
        When {
            get("http://localhost:$port/v1/students")
        } Then {
            statusCode(HttpStatus.SC_OK)
            body("list.size", equalTo(3))
        }
    }

    @Test
    fun `on get student by id return with status 200`() {
        When {
            get("http://localhost:$port/v1/students/3")
        } Then {
            statusCode(HttpStatus.SC_OK)
            body("id", equalTo("3"))
            body("email", equalTo("firstlast3@email.com"))
        }
    }

    @Test
    fun `on get student by non existent id return with status 404`() {
        When {
            get("http://localhost:$port/v1/students/10954")
        } Then {
            statusCode(HttpStatus.SC_NOT_FOUND)
        }
    }

    @Test
    fun `on post create student with status 201`() {
        val jsonString = "{" +
                "    \"id\": \"4\"," +
                "    \"firstName\": \"first\"," +
                "    \"lastName\": \"last\"," +
                "    \"email\":\"e@mail\"," +
                "    \"courses\": []" +
                "}"

        Given {
            contentType(ContentType.JSON)
            body(jsonString)
        } When {
            post("http://localhost:$port/v1/students")
        } Then {
            statusCode(HttpStatus.SC_CREATED)
            body("id", equalTo("4"))
            body("firstName", equalTo("first"))
            body("lastName", equalTo("last"))
            body("email", equalTo("e@mail"))
        }

        assert(studentRepository.findAll().size == 4)
    }

    @Test
    fun `should reject post with status 409`() {
        val jsonString = "{" +
                "    \"id\": \"1\"," +
                "    \"firstName\": \"first\"," +
                "    \"lastName\": \"last\"," +
                "    \"email\":\"e@mail\"," +
                "    \"courses\": []" +
                "}"

        Given {
            contentType(ContentType.JSON)
            body(jsonString)
        } When {
            post("http://localhost:$port/v1/students")
        } Then {
            statusCode(HttpStatus.SC_CONFLICT)
        }

        assert(studentRepository.findAll().size == 3)
    }

    @Test
    fun `should reject invalid data with status 400`() {
        val jsonString = "{" +
                "    \"id\": \"4\"," +
                "    \"firstName\": \"firstnamethatisverylonganddefinitelyinvalidbecauseofit\"," +
                "    \"lastName\": \"last\"," +
                "    \"email\":\"e@mail\"," +
                "    \"courses\": []" +
                "}"

        Given {
            contentType(ContentType.JSON)
            body(jsonString)
        } When {
            post("http://localhost:$port/v1/students")
        } Then {
            statusCode(HttpStatus.SC_BAD_REQUEST)
        }

        assert(studentRepository.findAll().size == 3)
    }

    @Test
    fun `should update student with status 200`() {
        val jsonString = "{" +
                "    \"id\": \"1\"," +
                "    \"firstName\": \"some other name\"," +
                "    \"lastName\": \"last\"," +
                "    \"email\":\"firstlast1@email.com\"," +
                "    \"courses\": []" +
                "}"

        Given {
            contentType(ContentType.JSON)
            body(jsonString)
        } When {
            put("http://localhost:$port/v1/students/1")
        } Then {
            statusCode(HttpStatus.SC_OK)
            body("id", equalTo("1"))
            body("firstName", equalTo("some other name"))
            body("lastName", equalTo("last"))
            body("email", equalTo("firstlast1@email.com"))
        }

        assert(studentService.findById("1").firstName == "some other name")
    }

    @Test
    fun `on delete student by id return with status 204`() {
        Given {
            accept(ContentType.JSON)
        } When {
            delete("http://localhost:$port/v1/students/3")
        } Then {
            statusCode(HttpStatus.SC_OK)
            body(equalTo("3"))
        }

        assert(studentRepository.findAll().size == 2)
    }
}
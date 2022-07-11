package nt.jsa.studentcrm_v2.controllerIntTests

import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import nt.jsa.studentcrm_v2.model.Course
import nt.jsa.studentcrm_v2.model.Student
import nt.jsa.studentcrm_v2.repository.CourseRepository
import nt.jsa.studentcrm_v2.repository.StudentRepository
import nt.jsa.studentcrm_v2.service.CourseService
import org.apache.http.HttpStatus
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CourseControllerIntTest @Autowired constructor(
    val courseRepository: CourseRepository,
    val studentRepository: StudentRepository,
    val courseService: CourseService,
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
    fun `on get all courses return with status 200`() {
        When {
            get("http://localhost:$port/v1/courses")
        } Then {
            statusCode(HttpStatus.SC_OK)
            body("courses.size", equalTo(2))
        }
    }

    @Test
    fun `on get get course by id return with status 200`() {
        When {
            get("http://localhost:$port/v1/courses/2")
        } Then {
            statusCode(HttpStatus.SC_OK)
            body("id", equalTo("2"))
            body("description", equalTo("description 2"))
        }
    }

    @Test
    fun `on get course by non existent id return with status 404`() {
        When {
            get("http://localhost:$port/v1/courses/10954")
        } Then {
            statusCode(HttpStatus.SC_NOT_FOUND)
        }
    }

    @Test
    fun `on post create course with status 201`() {
        val jsonString = "{" +
            "    \"id\": \"3\"," +
            "    \"description\": \"descr 3\"," +
            "    \"students\": []" +
            "}"

        Given {
            contentType(ContentType.JSON)
            body(jsonString)
        } When {
            post("http://localhost:$port/v1/courses")
        } Then {
            statusCode(HttpStatus.SC_CREATED)
            body("id", equalTo("3"))
        }
    }

    @Test
    fun `should reject post with status 409`() {
        val jsonString = "{" +
            "    \"id\": \"2\"," +
            "    \"description\": \"descr 3\"," +
            "    \"students\": []" +
            "}"

        Given {
            contentType(ContentType.JSON)
            body(jsonString)
        } When {
            post("http://localhost:$port/v1/courses")
        } Then {
            statusCode(HttpStatus.SC_CONFLICT)
        }
    }

    @Test
    fun `should reject post with status 400`() {
        val jsonString = "{" +
            "    \"id\": \"5\"," +
            "    \"description\": \"descr 3fdsfdsfdsfdsffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff\"," +
            "    \"students\": []" +
            "}"

        Given {
            contentType(ContentType.JSON)
            body(jsonString)
        } When {
            post("http://localhost:$port/v1/courses")
        } Then {
            statusCode(HttpStatus.SC_BAD_REQUEST)
        }
    }

    @Test
    fun `should update course with status 200`() {
        val jsonString = "{" +
            "    \"id\": \"1\"," +
            "    \"description\": \"something different\"," +
            "    \"students\": []" +
            "}"

        Given {
            contentType(ContentType.JSON)
            body(jsonString)
        } When {
            put("http://localhost:$port/v1/courses/1")
        } Then {
            statusCode(HttpStatus.SC_OK)
            body("id", equalTo("1"))
            body("description", equalTo("something different"))
        }

        assert(courseService.findById("1").description == "something different")
    }

    @Test
    fun `on delete course by id return with status 204`() {
        Given {
            accept(ContentType.JSON)
        } When {
            delete("http://localhost:$port/v1/courses/1")
        } Then {
            statusCode(HttpStatus.SC_OK)
            body(equalTo("1"))
        }

        assert(courseRepository.findAll().size == 1)
    }
}

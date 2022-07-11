package nt.jsa.studentcrm_v2.model

import com.fasterxml.jackson.annotation.JsonBackReference
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.MongoId
import javax.validation.constraints.Email
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Document("students")
data class Student(
    @MongoId
    @get:Size(min = 1)
    @get:NotNull
    val id: String,
    @get:Size(max = 20)
    @get:NotNull
    val firstName: String,
    @get:Size(max = 20)
    @NotNull
    val lastName: String,
    @field:Email
    // @field:Indexed(unique = true, partialFilter = "{\"courses.students.email\": {\"\$exists\": \"false\"}}")
    @get:NotNull
    val email: String,
    @DBRef(lazy = true)
    @get:NotNull
    @field:JsonBackReference
    val courses: List<Course> = listOf()
)

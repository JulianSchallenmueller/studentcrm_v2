package nt.jsa.studentcrm_v2.model

import com.fasterxml.jackson.annotation.JsonManagedReference
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.MongoId
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Document("courses")
data class Course(
    @MongoId
    @get:Size(min = 1)
    @get:NotNull
    val id: String,
    @get:Size(max = 35)
    @get:NotNull
    val description: String,
    @DBRef(lazy = true)
    @get:NotNull
    @field:JsonManagedReference
    val students: List<Student> = listOf()
)

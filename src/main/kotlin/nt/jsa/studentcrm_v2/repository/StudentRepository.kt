package nt.jsa.studentcrm_v2.repository

import nt.jsa.studentcrm_v2.model.Student
import org.springframework.data.mongodb.repository.MongoRepository

interface StudentRepository : MongoRepository<Student, String> {
    fun findByEmail(email: String): Student
    fun existsByEmail(email: String): Boolean
}

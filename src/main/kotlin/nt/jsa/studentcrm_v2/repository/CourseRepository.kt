package nt.jsa.studentcrm_v2.repository

import nt.jsa.studentcrm_v2.model.Course
import org.springframework.data.mongodb.repository.MongoRepository

interface CourseRepository : MongoRepository<Course, String> {
}
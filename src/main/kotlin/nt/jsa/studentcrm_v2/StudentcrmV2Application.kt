package nt.jsa.studentcrm_v2

import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.resource.ClassLoaderResourceAccessor
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class StudentcrmV2Application

fun main(args: Array<String>) {
    val liquibase = Liquibase(
        "db/changelog/changelog.xml", ClassLoaderResourceAccessor(),
        DatabaseFactory.getInstance().openDatabase("mongodb://localhost:27017/studentcrm_v2", null, null, null, null)
    )
    liquibase.update("")

    runApplication<StudentcrmV2Application>(*args)
}

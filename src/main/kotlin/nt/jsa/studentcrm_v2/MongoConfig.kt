package nt.jsa.studentcrm_v2

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.mapping.event.ValidatingMongoEventListener
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean

@Configuration
@ComponentScan
@EnableMongoRepositories("nt.jsa.studentcrm_v2")
class MongoConfig {

    @Bean
    fun validatingMongoEventListener(): ValidatingMongoEventListener? {
        return ValidatingMongoEventListener(validator()!!)
    }

    @Bean
    fun validator(): LocalValidatorFactoryBean? {
        return LocalValidatorFactoryBean()
    }
}
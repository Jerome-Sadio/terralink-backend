package sn.terralink

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(exclude = [
    RedisAutoConfiguration::class,
    RedisRepositoriesAutoConfiguration::class
])
class TerraLinkApplication

fun main(args: Array<String>) {
    runApplication<TerraLinkApplication>(*args)
}

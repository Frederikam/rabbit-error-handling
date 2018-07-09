package com.frederikam

import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.annotation.RabbitHandler
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.rabbit.listener.RabbitListenerErrorHandler
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service


@SpringBootApplication
@ComponentScan(basePackages = ["com.frederikam"])
class Launcher

fun main(args: Array<String>) {
    SpringApplication.run(Launcher::class.java, *args)
}

@Configuration
class Config {
    @Bean
    fun errorHandler() = RabbitListenerErrorHandler { _, _, ex ->
        System.out.println("Caught exception $ex")
        null
    }

    @Bean
    fun queue() = Queue("foos", false)
}

@RabbitListener(queues = ["foos"], errorHandler = "errorHandler")
@Service
class Listener {
    @RabbitHandler()
    fun foo(str: String) {
        throw RuntimeException("foo $str")
    }

    @RabbitHandler
    fun bar(bool: Boolean) {
        throw RuntimeException("bar $bool")
    }

    @RabbitHandler(isDefault = true)
    fun default() {
        throw RuntimeException("Default")
    }
}

@Service
class FooSender(val rabbitTemplate: RabbitTemplate) : Thread() {
    init {
        @Suppress("LeakingThis")
        start()
    }
    override fun run() {
        while (true) {
            Thread.sleep(2000)
            rabbitTemplate.convertAndSend("foos", "bar")
        }
    }
}
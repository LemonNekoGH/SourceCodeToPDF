package moe.lemonneko.sctp

import kotlin.concurrent.thread

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        Runtime.getRuntime().addShutdownHook(thread(
            start = false
        ) {
            println(ViewModel.locale.goodbye)
        })
        MainWindow()
    }
}

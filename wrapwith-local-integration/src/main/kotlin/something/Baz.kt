package something

import io.github.staakk.wrapwith.Foo

fun main() {
    Actual().bar()
    println(sequence { do { yield('-') } while (true) }.take(20).joinToString(separator = ""))
    ActualWithImpl().bar()
}

class Actual : Foo()

class ActualWithImpl : Foo() {
    override fun bar() {
        super.bar()
        println("Hello from ActualWithImpl")
    }
}
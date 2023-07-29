package io.github.staakk.wrapwith

const val SampleWrap = "sample_wrap"
const val SecondarySampleWrap = "secondary_sample_wrap"

@WrapWith(SampleWrap)
fun noArg() = Unit

@WrapWith(SampleWrap)
fun noArgIntReturn() = 1

@WrapWith(SampleWrap)
fun stringArg(arg: String) = Unit

@WrapWith(SampleWrap)
fun nullReturn() = null

@WrapWith(SampleWrap)
fun throwsException(): Int {
    throw IllegalStateException("test exception")
    return 1
}

@WrapWith(SampleWrap)
fun earlyReturn(arg: Int) : Int {
    if (arg == 0) return 1
    return 2
}

@WrapWith(SampleWrap, SecondarySampleWrap)
fun multipleWraps() = Unit

fun withInternalFunction() {

    @WrapWith(SampleWrap)
    fun internalFunction() = Unit

    internalFunction()
}


@WrapWith(SampleWrap)
fun Int.withReceiver() = Unit

class Sample {

    @WrapWith(SampleWrap)
    fun noArg() = Unit

    @WrapWith(SampleWrap)
    fun String.withReceiver() = Unit
}


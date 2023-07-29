package io.github.staakk.wrapwith

import kotlin.test.*

class WrapWithTest {
    private val wrap = MockWrap()

    @AfterTest
    fun clearWrap() {
        wrap.clear()
        Wraps.removeAllWraps()
    }

    @Test
    fun test_noArg() {
        Wraps.registerWrap(SampleWrap, wrap)

        noArg()

        assertWrapCalls(
            functionName = "noArg",
            params = emptyMap(),
            returnValue = Unit,
        )
    }

    @Test
    fun test_noArgIntReturn() {
        Wraps.registerWrap(SampleWrap, wrap)

        noArgIntReturn()

        assertWrapCalls(
            functionName = "noArgIntReturn",
            params = emptyMap(),
            returnValue = 1,
        )
    }

    @Test
    fun test_stringArg() {
        Wraps.registerWrap(SampleWrap, wrap)

        stringArg("test")

        assertWrapCalls(
            functionName = "stringArg",
            params = mapOf("arg" to "test"),
            returnValue = Unit,
        )
    }

    @Test
    fun test_nullReturn() {
        Wraps.registerWrap(SampleWrap, wrap)

        nullReturn()

        assertWrapCalls(
            functionName = "nullReturn",
            params = mapOf(),
            returnValue = null,
        )
    }

    @Test
    fun test_throwsException() {
        Wraps.registerWrap(SampleWrap, wrap)

        runCatching { throwsException() }

        assertEquals(FunctionInvocation("io.github.staakk.wrapwith.throwsException", mapOf()), wrap.functionInvocation)
        assertTrue(wrap.returnValue is IllegalStateException)
        val returnValue = wrap.returnValue as IllegalStateException
        assertEquals("test exception", returnValue.message)
    }

    @Test
    fun test_earlyReturn_earlyReturnTriggered() {
        Wraps.registerWrap(SampleWrap, wrap)

        earlyReturn(0)

        assertWrapCalls(
            functionName = "earlyReturn",
            params = mapOf("arg" to 0),
            returnValue = 1,
        )
    }

    @Test
    fun test_earlyReturn_earlyReturnNotTriggered() {
        Wraps.registerWrap(SampleWrap, wrap)

        earlyReturn(1)

        assertWrapCalls(
            functionName = "earlyReturn",
            params = mapOf("arg" to 1),
            returnValue = 2,
        )
    }

    @Test
    fun test_multipleWraps() {
        Wraps.registerWrap(SampleWrap, wrap)
        val secondaryWrap = MockWrap()
        Wraps.registerWrap(SecondarySampleWrap, secondaryWrap)

        multipleWraps()

        assertWrapCalls(
            wrap,
            functionName = "multipleWraps",
            params = mapOf(),
            returnValue = Unit,
        )
        assertWrapCalls(
            secondaryWrap,
            functionName = "multipleWraps",
            params = mapOf(),
            returnValue = Unit,
        )
    }

    @Test
    fun test_internalFunction() {
        Wraps.registerWrap(SampleWrap, wrap)

        withInternalFunction()

        assertWrapCalls(
            functionName = "withInternalFunction.internalFunction",
            params = mapOf(),
            returnValue = Unit,
        )
    }

    @Test
    fun test_withReceiver() {
        Wraps.registerWrap(SampleWrap, wrap)

        1.withReceiver()

        assertWrapCalls(
            functionName = "withReceiver",
            params = mapOf("\$receiver" to 1),
            returnValue = Unit
        )
    }

    @Test
    fun test_class_method() {
        Wraps.registerWrap(SampleWrap, wrap)
        val sampleInstance = Sample()

        sampleInstance.noArg()

        assertWrapCalls(
            functionName = "Sample.noArg",
            params = mapOf("\$this" to sampleInstance),
            returnValue = Unit
        )
    }

    @Test
    fun test_class_withReceiver() {
        Wraps.registerWrap(SampleWrap, wrap)
        val sampleInstance = Sample()

        with(sampleInstance) {
            "test".withReceiver()
        }

        assertWrapCalls(
            functionName = "Sample.withReceiver",
            params = mapOf("\$this" to sampleInstance, "\$receiver" to "test"),
            returnValue = Unit
        )
    }

    private fun assertWrapCalls(
        mockWrap: MockWrap = wrap,
        functionName: String,
        params: Map<String, Any?>,
        returnValue: Any?,
    ) {
        assertEquals(FunctionInvocation("io.github.staakk.wrapwith.$functionName", params), mockWrap.functionInvocation)
        assertEquals(returnValue, mockWrap.returnValue)
    }
}
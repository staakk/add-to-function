package io.github.staakk.wrapwith

class MockWrap : Wrap {

    var functionInvocation: FunctionInvocation? = null
        set(value) {
            if (value != null && field != null)
                throw IllegalStateException("`functionInvocation` set before clearing.")
            field = value
        }

    var returnValue: Any? = null
        set(value) {
            if (value != null && field != null)
                throw IllegalStateException("`returnValue` set before clearing.")
            field = value
        }

    override fun before(functionInvocation: FunctionInvocation) {
        this.functionInvocation = functionInvocation
    }

    override fun after(returnValue: Any?) {
        this.returnValue = returnValue
    }

    fun clear() {
        functionInvocation = null
        returnValue = null
    }
}
package io.github.staakk.wrapwith

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.ir.copyTo
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.jvm.ir.hasChild
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.declarations.addFunction
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.isFakeOverride
import org.jetbrains.kotlin.ir.util.statements
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

private val BarName = Name.identifier("bar")

class MethodExtenderTransformer(
    private val pluginContext: IrPluginContext,
) : IrElementTransformerVoidWithContext() {


    private val classAggregator = pluginContext
        .referenceClass(FqName("io.github.staakk.wrapwith.Aggregator"))!!
    private val aggregatorSthReference = pluginContext
        .referenceFunctions(FqName("io.github.staakk.wrapwith.Aggregator.sth"))
        .single()

    private val fooClassReference = pluginContext
        .referenceClass(FqName("io.github.staakk.wrapwith.Foo"))!!
    private val fooType = fooClassReference.defaultType
    private val fooBarReference = pluginContext
        .referenceFunctions(FqName("io.github.staakk.wrapwith.Foo.bar"))
        .single()

    override fun visitClassNew(declaration: IrClass): IrStatement {
        if (
        // Process classes which have function with name `bar`.
            declaration.hasChild { it is IrFunction && it.name == BarName } &&
            // Exclude classes that don't inherit from `Foo` type.
            declaration.superTypes.contains(fooType)
        ) {

            // Remove `bar` declaration if it's a fake override i.e. the class doesn't override it.
            val removed = declaration
                .declarations
                .removeIf {
                    it is IrFunction && it.name == BarName && it.isFakeOverride
                }

            // When `bar` was a fake override (hence was removed) add new `bar` override.
            if (removed) declaration.addBarFunctionOverride()
            // Transform existing function otherwise.
            else declaration.transformChildren(LogAppender(), null)

        }
        return super.visitClassNew(declaration)
    }

    /**
     * Generates the following
     *
     * ```kotlin
     * override fun bar() {
     *      super.bar()
     *      Aggregator.sth()
     * }
     * ```
     */
    private fun IrClass.addBarFunctionOverride() {
        addFunction {
            name = BarName
            returnType = pluginContext.irBuiltIns.unitType
            visibility = DescriptorVisibilities.PUBLIC
        }
            .also { function ->
                function.dispatchReceiverParameter = thisReceiver?.copyTo(function)
                function.body = DeclarationIrBuilder(
                    pluginContext,
                    function.symbol,
                    function.startOffset,
                    function.endOffset
                )
                    .irBlockBody {
                        +irBlock(resultType = function.returnType) {
                            // Call super.bar()
                            +irCall(
                                callee = fooBarReference.owner,
                                origin = null,
                                superQualifierSymbol = fooClassReference
                            ).apply {
                                dispatchReceiver = irGet(function.dispatchReceiverParameter!!)
                            }
                            // Call Aggregator.sth()
                            +irCallAggregatorSth()
                        }
                    }
            }
    }

    private fun IrBlockBodyBuilder.irCallAggregatorSth() = irCall(aggregatorSthReference)
        .apply { dispatchReceiver = irGetObject(classAggregator) }

    /**
     * Transforms the body as follows
     *
     * Original
     * ```kotlin
     * override fun bar() {
     *      // some code
     * }
     * ```
     * Transformed
     * ```kotlin
     * override fun bar() {
     *      // some code
     *      Aggregator.sth()
     * }
     */
    private inner class LogAppender : IrElementTransformerVoidWithContext() {

        override fun visitFunctionNew(declaration: IrFunction): IrStatement {
            if (declaration.name == BarName)
                return super.visitFunctionNew(declaration)

            return super.visitFunctionNew(transformFunction(declaration))
        }

        private fun transformFunction(declaration: IrFunction): IrFunction {
            val body = declaration.body
            declaration.body = DeclarationIrBuilder(pluginContext, declaration.symbol)
                .irBlockBody {
                    +irBlock(resultType = declaration.returnType) {
                        // Add existing code to the function.
                        body?.let { for (statement in body.statements) +statement }
                        // Call Aggregator.sth()
                        +irCallAggregatorSth()
                    }
                }
            return declaration
        }
    }
}
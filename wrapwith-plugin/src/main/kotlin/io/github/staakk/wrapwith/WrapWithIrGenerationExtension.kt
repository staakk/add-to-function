package io.github.staakk.wrapwith

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.util.dump

class WrapWithIrGenerationExtension(
//    private val messageCollector: MessageCollector
) : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
//        messageCollector.report(CompilerMessageSeverity.STRONG_WARNING, moduleFragment.dump())
        moduleFragment.transform(MethodExtenderTransformer(pluginContext), null)
//        messageCollector.report(CompilerMessageSeverity.STRONG_WARNING, moduleFragment.dump())
    }
}
/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.processor

import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

/**
 * @author gusd
 * @Date 2021/07/28
 */
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes(
    "cn.rongcloud.annotation.HiltBinding",
    "cn.rongcloud.annotation.AutoInit"
)
class CoreProcessor : AbstractProcessor() {

    private val processorImplList = arrayListOf<BaseProcessor>()


    override fun init(processingEnv: ProcessingEnvironment?) {
        super.init(processingEnv)
        if (processingEnv == null) return
        processorImplList.apply {
            add(HiltInjectProcessor(processingEnv))
            add(ModuleInitProcessor(processingEnv))
        }
    }

    override fun process(
        annotations: MutableSet<out TypeElement>,
        processingEnv: RoundEnvironment
    ): Boolean {
        return try {
            processImpl(annotations, processingEnv)
        } catch (e: Exception) {
            logError(e.message)
            true
        }
    }

    private fun processImpl(
        annotations: MutableSet<out TypeElement>,
        processingEnv: RoundEnvironment,
    ): Boolean {
        processorImplList.forEach {
            it.processImpl(annotations, processingEnv)
        }
        return true
    }

    public fun logError(message: String?) {
        message?.let {
            processingEnv.messager.printMessage(
                Diagnostic.Kind.ERROR,
                "${this.javaClass.name}: $message\r\n"
            )
        }
    }

}
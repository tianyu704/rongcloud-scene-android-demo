/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.compiler

import cn.rongcloud.annotation.HiltBinding
import com.google.auto.service.AutoService
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

/**
 * @author gusd
 * @Date 2021/07/26
 */
@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class HiltInjectProcessor : AbstractProcessor() {
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
        processingEnv: RoundEnvironment
    ): Boolean {
        val hiltBindingElement = processingEnv.getElementsAnnotatedWith(HiltBinding::class.java)
        logDebug("$hiltBindingElement.size")
        return true
    }

    private fun logDebug(message: String?) {
        message?.let {
            processingEnv.messager.printMessage(
                Diagnostic.Kind.NOTE,
                "HiltInjectProcessor: $message"
            )
        }
    }

    private fun logError(message: String?) {
        message?.let {
            processingEnv.messager.printMessage(
                Diagnostic.Kind.ERROR,
                "HiltInjectProcessor: $message"
            )
        }
    }

}
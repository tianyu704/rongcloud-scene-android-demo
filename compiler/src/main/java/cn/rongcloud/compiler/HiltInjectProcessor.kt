/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.compiler

import com.google.auto.common.BasicAnnotationProcessor
import com.google.auto.service.AutoService
import com.google.common.collect.ImmutableList
import javax.annotation.processing.Processor
import javax.tools.Diagnostic

/**
 * @author gusd
 * @Date 2021/07/26
 */
@AutoService(Processor::class)
public class HiltInjectProcessor : BasicAnnotationProcessor() {
//    override fun process(
//        annotations: MutableSet<out TypeElement>,
//        processingEnv: RoundEnvironment
//    ): Boolean {
//        return try {
//            processImpl(annotations, processingEnv)
//        } catch (e: Exception) {
//            logError(e.message)
//            true
//        }
//    }
//
//    private fun processImpl(
//        annotations: MutableSet<out TypeElement>,
//        processingEnv: RoundEnvironment
//    ): Boolean {
//        val hiltBindingElement = processingEnv.getElementsAnnotatedWith(HiltBinding::class.java)
//        logDebug("$hiltBindingElement.size")
//        return true
//    }

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

    override fun initSteps(): MutableIterable<ProcessingStep> {
        return ImmutableList.of()
    }

}
/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.processor

import cn.rongcloud.annotation.HiltBinding
import cn.rongcloud.bean.TypeEnum
import com.google.auto.service.AutoService
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic

/**
 * @author gusd
 * @Date 2021/07/26
 */
@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("cn.rongcloud.annotation.HiltBinding")
public class HiltInjectProcessor : AbstractProcessor() {

    // 文件操作类，我们将通过此类生成kotlin文件
    private lateinit var mFiler: Filer

    // 类型工具类，处理Element的类型
    private lateinit var mTypeTools: Types

    private lateinit var mElementUtils: Elements

    // 生成类的包名
    private val mGeneratePackage = "cn.rongcloud.generate"


    override fun init(processingEnv: ProcessingEnvironment?) {
        super.init(processingEnv)
        if (processingEnv == null) return
        mFiler = processingEnv.filer
        mElementUtils = processingEnv.elementUtils
        mTypeTools = processingEnv.typeUtils


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
        processingEnv: RoundEnvironment
    ): Boolean {
        val hiltBindingElement = processingEnv.getElementsAnnotatedWith(HiltBinding::class.java)
        logDebug("${hiltBindingElement.size}")

        if (hiltBindingElement.size == 0) {
            return false
        }


        // 获取activity的类型，转换成TypeMirror，用于判断
        val activityType: TypeMirror =
            mElementUtils.getTypeElement(ProcessorConstant.ACTIVITY_PACKAGE).asType()

        // 获取fragment的类型，转换成TypeMirror，用于判断
        val fragmentType: TypeMirror =
            mElementUtils.getTypeElement(ProcessorConstant.FRAGMENT_PACKAGE).asType()

        val viewType: TypeMirror =
            mElementUtils.getTypeElement(ProcessorConstant.VIEW_PACKAGE).asType()

        hiltBindingElement.forEach { element ->
            logDebug("element name = ${element.simpleName}")

            val annotation = element.getAnnotation(HiltBinding::class.java)
            val value = try {
                annotation.value
            } catch (e: MirroredTypeException) {
                e.typeMirrors[0]
            }

            if (value == Void::class.java.name) {
                logDebug("${element.simpleName} 类使用的 hiltBinding 注解参数错误")
                return@forEach
            }
            var type: TypeEnum? = when {
                mTypeTools.isSubtype(element.asType(), activityType) -> {
                    TypeEnum.ACTIVITY
                }
                mTypeTools.isSubtype(element.asType(), fragmentType) -> {
                    TypeEnum.FRAGMENT
                }
                mTypeTools.isSubtype(element.asType(), viewType) -> {
                    TypeEnum.VIEW
                }
                else ->{
                    null
                }
            }




        }
        return true
    }

    private fun logDebug(message: String?) {
        message?.let {
            processingEnv.messager.printMessage(
                Diagnostic.Kind.NOTE,
                "HiltInjectProcessor: $message\r\n"
            )
        }
    }

    private fun logError(message: String?) {
        message?.let {
            processingEnv.messager.printMessage(
                Diagnostic.Kind.ERROR,
                "HiltInjectProcessor: $message\r\n"
            )
        }
    }

}

/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.processor

import cn.rongcloud.annotation.HiltBinding
import cn.rongcloud.bean.HiltBindingBean
import cn.rongcloud.bean.TypeEnum
import com.google.auto.service.AutoService
import com.squareup.javapoet.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import java.util.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
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

        val elementList = arrayListOf<HiltBindingBean>()

        hiltBindingElement.forEach { element ->
            logDebug("element name = ${element.simpleName}")

            if (element.kind != ElementKind.CLASS) {
                return@forEach
            }

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
                else -> {
                    null
                }
            }

            if (type != null) {
                elementList.add(
                    HiltBindingBean(
                        element.asType().toString(),
                        value.toString(),
                        type,
                        element
                    )
                )
            }

        }

        generateHiltInjectFile(elementList)
        return true
    }

    private fun generateHiltInjectFile(elementList: ArrayList<HiltBindingBean>) {
        val activityElement = elementList.filter { it.typeEnum == TypeEnum.ACTIVITY }
        val fragmentElement = elementList.filter { it.typeEnum == TypeEnum.FRAGMENT }
        val viewElement = elementList.filter { it.typeEnum == TypeEnum.VIEW }

        if (activityElement.isNotEmpty()) {
            try {
                createComponentFile(
                    activityElement,
                    ProcessorConstant.ACTIVITY_PACKAGE,
                    "ActivityComponent",
                    "Generate_ActivityModule"
                )
            } catch (e: Exception) {
                logError(e.message)
            }
        }

        if (fragmentElement.isNotEmpty()) {
            try {
                createComponentFile(
                    fragmentElement,
                    ProcessorConstant.FRAGMENT_PACKAGE,
                    "FragmentComponent",
                    "Generate_FragmentModule"
                )
            } catch (e: Exception) {
                logError(e.message)
            }
        }

        if (viewElement.isNotEmpty()) {
            try {
                // TODO: 2021/7/28 在 hilt 中 view 的情况较为特殊，暂不建议处理
//                createComponentFile(
//                    viewElement,
//                    ProcessorConstant.VIEW_PACKAGE,
//                    "ViewComponentComponent",
//                    "Generate_ViewModule"
//                )
            } catch (e: Exception) {
                logError(e.message)
            }
        }

    }

    private fun createComponentFile(
        list: List<HiltBindingBean>,
        implClass: String,
        componentClassName: String,
        fileName: String
    ) {
        val methodList = arrayListOf<MethodSpec>()
        list.forEach {
            logDebug("$it")
            val viewClassInfo = getPackageAndClassName(it.viewClazz)
            val viewImplClassInfo = getPackageAndClassName(implClass)
            val methodSpec = MethodSpec.methodBuilder("provide${viewClassInfo[1]}")
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get(viewClassInfo[0], viewClassInfo[1]))
                .addAnnotation(
                    Provides::class.java
                )
                .addParameter(
                    ClassName.get(viewImplClassInfo[0], viewImplClassInfo[1]),
                    viewClassInfo[1].toLowerCase()
                )
                .addCode(CodeBlock.of("return (${viewClassInfo[1]})${viewClassInfo[1].toLowerCase()}; "))
                .build()
            methodList.add(methodSpec)
        }

        val componentAnnotation = CodeBlock.builder()
            .add(
                "\$T.class",
                ClassName.get("dagger.hilt.android.components", componentClassName)
            )
            .build()

        val annotationSpec =
            AnnotationSpec.builder(InstallIn::class.java)
                .addMember("value", componentAnnotation)
                .build()

        val type =
            TypeSpec.classBuilder(fileName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Module::class.java)
                .addAnnotation(annotationSpec)
                .addMethods(methodList)
                .build()
        JavaFile.builder(mGeneratePackage, type).build()
            .writeTo(mFiler)
    }

    private fun getPackageAndClassName(classPath: String): Array<String> {
        val index = classPath.lastIndexOf(".")
        val packageName = classPath.subSequence(0, index)
        val className = classPath.subSequence(index + 1, classPath.length)
        return arrayOf(packageName.toString(), className.toString())
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

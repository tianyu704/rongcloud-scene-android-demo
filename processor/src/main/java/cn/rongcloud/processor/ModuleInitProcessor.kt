/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.processor

import cn.rongcloud.annotation.AutoInit
import cn.rongcloud.bean.AutoInitBean
import com.squareup.javapoet.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.inject.Named
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import kotlin.collections.HashSet

/**
 * @author gusd
 * @Date 2021/07/28
 */
class ModuleInitProcessor(processingEnv: ProcessingEnvironment) : BaseProcessor(processingEnv) {
    private val moduleInitTempFile = File(".${File.separator}build${File.separator}temp.txt")
    override fun processImpl(
        annotations: MutableSet<out TypeElement>,
        roundEnvironment: RoundEnvironment
    ): Boolean {

        val options = processingEnv.options
        val isAppModule = options[ProcessorConstant.IS_APP_MODULE].toBoolean()
        var moduleName = options[ProcessorConstant.MODULE_NAME]

        if (isAppModule && roundEnvironment.processingOver()) {
            if (moduleInitTempFile.exists()) {
                try {
                    moduleInitTempFile.delete()
                } catch (e: Exception) {
                }
            }
        }

        val autoInitElement = roundEnvironment.getElementsAnnotatedWith(AutoInit::class.java)
        logDebug("${autoInitElement.size}")
        if (autoInitElement.isEmpty()) {
            return true
        }


        if (options.isNotEmpty()) {
            options.forEach {
                logDebug("key = ${it.key},value = ${it.value}")
            }
        }

        val elementList = arrayListOf<AutoInitBean>()
        autoInitElement.forEach { element ->
            logDebug("element name = ${element.simpleName}")
            if (element.kind != ElementKind.CLASS) {
                return@forEach
            }
            if (moduleName.isNullOrEmpty()) {
                moduleName = getDefaultModuleName(element.asType().toString())
                logWarning("当前模块未配置 moduleName, 将使用默认的 $moduleName 作为模块名")
            }
            elementList.add(AutoInitBean(element.asType().toString(), element))
        }

        // 将模块名记录到文件临时文件中
        if (!moduleInitTempFile.exists()) {
            moduleInitTempFile.createNewFile()
        }
        moduleName?.let {
            val fw = FileWriter(moduleInitTempFile.path, true)
            fw.appendLine(it)
            fw.close()
        }

        generateAutoInitFile(moduleName ?: "", elementList)
        if(isAppModule){
            // 生成 APP 层的注入策略
            generateTotalInitFile()
        }
        return true
    }

    private fun generateTotalInitFile() {
        val fileReader = FileReader(moduleInitTempFile.path)
        val readLines = fileReader.readLines()
        val moduleSet = HashSet<String>()
        readLines.forEach { line ->
            if (!line.isNullOrEmpty()) {
                moduleSet.add(line)
            }
        }

//        @Module
//        @InstallIn(SingletonComponent::class)
//        class ModuleInitComponent {
//            @Provides
//            @Named("autoInit")
//            public fun provideModuleItem(
//                @Named("voiceroomdemo") list1: ArrayList<ModuleInit>,
//                @Named("common") list2: ArrayList<ModuleInit>
//            ): ArrayList<ModuleInit> {
//                return arrayListOf<ModuleInit>().apply {
//                    addAll(list1)
//                    addAll(list2)
//                }
//            }
//        }
        val moduleAndClassName = getPackageAndClassName(ProcessorConstant.INIT_MODULE)
        val moduleInitType = ClassName.get(moduleAndClassName[0], moduleAndClassName[1])
        val arrayList = ClassName.get("java.util", "ArrayList")
        val listOfInitModule = ParameterizedTypeName.get(arrayList, moduleInitType)

        val parameterList = arrayListOf<ParameterSpec>()
        var string = "ArrayList<\$T> list = new ArrayList();"
        moduleSet.forEach { moduleName ->
            logDebug("moduleName = $moduleName")
            ParameterSpec
                .builder(listOfInitModule, "${moduleName}_List")
                .addAnnotation(
                    AnnotationSpec.builder(Named::class.java)
                        .addMember("value", "\"${moduleName}\"")
                        .build()
                ).build().apply {
                    parameterList.add(this)
                }
            string += "list.addAll(${moduleName}_List);"
        }
        val returnCodeString = string + "return list;"


        val methodSpec = MethodSpec
            .methodBuilder("provideModuleItem")
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Provides::class.java)
            .addAnnotation(
                AnnotationSpec.builder(Named::class.java)
                    .addMember("value", "\"autoInit\"")
                    .build()
            )
            .addParameters(parameterList)
            .addCode(
                CodeBlock.of(
                    returnCodeString,
                    moduleInitType
                )
            )
            .returns(listOfInitModule)
            .build()

        val componentAnnotation = CodeBlock.builder()
            .add(
                "\$T.class",
                ClassName.get("dagger.hilt.components", "SingletonComponent")
            )
            .build()

        val annotationSpec =
            AnnotationSpec.builder(InstallIn::class.java)
                .addMember("value", componentAnnotation)
                .build()

        val type =
            TypeSpec.classBuilder("Generate_ModuleInitComponent")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Module::class.java)
                .addAnnotation(annotationSpec)
                .addMethod(methodSpec)
                .build()
        JavaFile.builder(mGeneratePackage, type).build()
            .writeTo(mFiler)
    }


    private fun generateAutoInitFile(
        moduleName: String = "",
        elementList: ArrayList<AutoInitBean>
    ) {
        logDebug("moduleName = ${moduleName}")

        val parameterList = arrayListOf<ParameterSpec>()
        var string = "ArrayList<\$T> list = new ArrayList();"
        elementList.forEachIndexed { index, bean ->
            val packageAndClassName = getPackageAndClassName(bean.clazz)
            ParameterSpec.builder(
                ClassName.get(packageAndClassName[0], packageAndClassName[1]),
                packageAndClassName[1].toLowerCase()
            ).build().apply {
                parameterList.add(this)
            }
            string += "list.add(${packageAndClassName[1].toLowerCase()});"
        }
        val returnCodeString = string + "return list;"
        val moduleAndClassName = getPackageAndClassName(ProcessorConstant.INIT_MODULE)
        val moduleInitType = ClassName.get(moduleAndClassName[0], moduleAndClassName[1])
        val arrayList = ClassName.get("java.util", "ArrayList")
        val listOfInitModule = ParameterizedTypeName.get(arrayList, moduleInitType)
        val methodSpec =
            MethodSpec.methodBuilder("provideModuleItem")
                .addModifiers(Modifier.PUBLIC)
                .returns(listOfInitModule)
                .addAnnotation(Provides::class.java)
                .addAnnotation(
                    AnnotationSpec.builder(Named::class.java)
                        .addMember("value", "\"${moduleName}\"")
                        .build()
                )
                .addParameters(parameterList)
                .addCode(
                    CodeBlock.of(
                        returnCodeString,
                        moduleInitType
                    )
                )
                .build()

        val componentAnnotation = CodeBlock.builder()
            .add(
                "\$T.class",
                ClassName.get("dagger.hilt.components", "SingletonComponent")
            )
            .build()

        val annotationSpec =
            AnnotationSpec.builder(InstallIn::class.java)
                .addMember("value", componentAnnotation)
                .build()
        val type =
            TypeSpec.classBuilder("${moduleName.toUpperCase()}_Generate_ModuleInit")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Module::class.java)
                .addAnnotation(annotationSpec)
                .addMethod(methodSpec)
                .build()
        JavaFile.builder(mGeneratePackage, type).build()
            .writeTo(mFiler)
    }

}
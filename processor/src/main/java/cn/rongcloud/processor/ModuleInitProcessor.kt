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
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.inject.Named
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

/**
 * @author gusd
 * @Date 2021/07/28
 */
class ModuleInitProcessor(processingEnv: ProcessingEnvironment) : BaseProcessor(processingEnv) {
    override fun processImpl(
        annotations: MutableSet<out TypeElement>,
        processingEnv: RoundEnvironment
    ): Boolean {
        val autoInitElement = processingEnv.getElementsAnnotatedWith(AutoInit::class.java)
        logDebug("${autoInitElement.size}")
        if(autoInitElement.isEmpty()){
            return true
        }

        val moduleInitType = mElementUtils.getTypeElement(ProcessorConstant.INIT_MODULE).asType()
        val elementList = arrayListOf<AutoInitBean>()
        autoInitElement.forEach { element ->
            logDebug("element name = ${element.simpleName}")
            if (element.kind != ElementKind.CLASS) {
                return@forEach
            }
            elementList.add(AutoInitBean(element.asType().toString(), element))
        }
        generateAutoInitFile(elementList)
        return true
    }

    private fun generateAutoInitFile(elementList: ArrayList<AutoInitBean>) {
//        @Module
//        @InstallIn(SingletonComponent::class)
//        class Test2Module {
//            @Named("111")
//            @Provides
//            fun provideTest(test1: Test1,test2: Test2): ArrayList<Test> {
//                return arrayListOf(test1,test2)
//            }
//        }
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
                        .addMember("value", "\"${ProcessorConstant.INIT_PROVIDE_NAMED}\"")
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
            TypeSpec.classBuilder("Generate_ModuleInit")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Module::class.java)
                .addAnnotation(annotationSpec)
                .addMethod(methodSpec)
                .build()
        JavaFile.builder(mGeneratePackage, type).build()
            .writeTo(mFiler)
    }

}
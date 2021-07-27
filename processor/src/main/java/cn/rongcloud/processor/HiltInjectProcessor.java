///*
// * Copyright © 2021 RongCloud. All rights reserved.
// */
//
//package cn.rongcloud.compiler;
//
//import com.google.auto.service.AutoService;
//
//import java.util.Set;
//
//import javax.annotation.processing.AbstractProcessor;
//import javax.annotation.processing.Filer;
//import javax.annotation.processing.ProcessingEnvironment;
//import javax.annotation.processing.Processor;
//import javax.annotation.processing.RoundEnvironment;
//import javax.annotation.processing.SupportedAnnotationTypes;
//import javax.annotation.processing.SupportedSourceVersion;
//import javax.lang.model.SourceVersion;
//import javax.lang.model.element.Element;
//import javax.lang.model.element.TypeElement;
//import javax.lang.model.util.Elements;
//import javax.lang.model.util.Types;
//import javax.tools.Diagnostic;
//
//import cn.rongcloud.annotation.HiltBinding;
//
///**
// * @author gusd
// * @Date 2021/07/27
// */
//@AutoService(Processor.class)
//@SupportedSourceVersion(SourceVersion.RELEASE_8)
//@SupportedAnnotationTypes("cn.rongcloud.annotation.HiltBinding")
//public class HiltInjectProcessor extends AbstractProcessor {
//    private static final String TAG = "HiltInjectProcessor";
//
//    private Filer mFiler = null;
//    private Types mTypes = null;
//    private Elements mElementUtils = null;
//    private static final String mGeneratePackage = "cn.rongcloud.generate";
//
//    @Override
//    public synchronized void init(ProcessingEnvironment processingEnv) {
//        super.init(processingEnv);
//        mFiler = processingEnv.getFiler();
//        mTypes = processingEnv.getTypeUtils();
//        mElementUtils = processingEnv.getElementUtils();
//    }
//
//    @Override
//    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
//        try {
//            processImpl(set, roundEnvironment);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return false;
//    }
//
//    private void processImpl(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
//        Set<? extends Element> hiltBindingElement = roundEnvironment.getElementsAnnotatedWith(HiltBinding.class);
//        handleHiltBindElement(hiltBindingElement, set, roundEnvironment);
//    }
//
//    private void handleHiltBindElement(Set<? extends Element> hiltBindingElement, Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
//        logDebug("hiltBindingElement size = " + hiltBindingElement.size());
//        for (Element element : hiltBindingElement) {
//            logDebug("hiltBindingElement = " + element.getSimpleName());
//            HiltBinding annotation = element.getAnnotation(HiltBinding.class);
//            logDebug("annotation "+annotation.value());
//        }
//    }
//
//
//    private void logDebug(String message) {
//        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "HiltInjectProcessor: " + message + "\r\n");
//    }
//
//    private void logError(String message) {
//        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "HiltInjectProcessor: " + message + "\r\n");
//    }
//}

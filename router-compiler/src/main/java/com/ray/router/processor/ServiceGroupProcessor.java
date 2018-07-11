package com.ray.router.processor;

import com.google.auto.service.AutoService;
import com.ray.router.annotation.Query;
import com.ray.router.annotation.Route;
import com.ray.router.annotation.RouteCtx;
import com.ray.router.processor.utils.TypeKind;
import com.ray.router.processor.utils.Consts;
import com.ray.router.processor.utils.Logger;
import com.ray.router.processor.utils.MethodGenerator;
import com.ray.router.processor.utils.TypeUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static com.ray.router.processor.utils.Consts.KEY_MODULE_NAME;
import static com.ray.router.processor.utils.Consts.NAME_OF_SERVICE_IMPL;
import static com.ray.router.processor.utils.Consts.WARNING_TIPS;
import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * @author zyl
 * @date Created on 2018/2/26
 */
@AutoService(Processor.class)
@SupportedOptions(KEY_MODULE_NAME)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class ServiceGroupProcessor extends AbstractProcessor {
    // File util, write class file into disk.
    private TypeUtils typeUtils;
    private Filer filer;
    private Types types;
    private Elements elements;
    private Messager messager;
    private Logger logger;
    private Map<TypeElement, List<Element>> parentAndChild = new HashMap<>();   // Contain field need autowired and his super class.
    private static final ClassName Router = ClassName.get("android.util", "Log");

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
        types = processingEnv.getTypeUtils();
        elements = processingEnv.getElementUtils();      // Get class meta.
        typeUtils = new TypeUtils(types, elements);
        messager = processingEnv.getMessager();
        logger = new Logger(messager);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> ret = new HashSet<>();
        ret.add(Route.class.getCanonicalName());
        return ret;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (CollectionUtils.isNotEmpty(annotations)) {
            Set<? extends Element> routeElements = roundEnv.getElementsAnnotatedWith(Route.class);
            try {
                logger.info(">>> Found routes, start... <<<");
                categories(routeElements);
                this.generateFiles(routeElements);
            } catch (Exception e) {
                logger.error(e);
            }
            return true;
        }
        return false;
    }

    private void generateFiles(Set<? extends Element> routeElements) throws IOException {
        if (routeElements == null || routeElements.isEmpty()) {
            return;
        }
        if (MapUtils.isEmpty(parentAndChild)) {
            return;
        }

        for (Map.Entry<TypeElement, List<Element>> entry : parentAndChild.entrySet()) {
            TypeElement fileElement = entry.getKey();
            List<Element> methodElements = entry.getValue();

            String qualifiedName = fileElement.getQualifiedName().toString();
            String packageName = qualifiedName.substring(0, qualifiedName.lastIndexOf("."));
            String fileName = fileElement.getSimpleName() + NAME_OF_SERVICE_IMPL;

            TypeSpec.Builder fileBuilder = TypeSpec.classBuilder(fileName)
                    .addJavadoc(WARNING_TIPS)
                    .addSuperinterface(ClassName.get(fileElement))
                    .addModifiers(PUBLIC);

            // Generate method body
            for (Element element : methodElements) {
                MethodSpec methodSpec = generateRouteMethod((ExecutableElement) element);
                fileBuilder.addMethod(methodSpec);
            }
            JavaFile.builder(packageName, fileBuilder.build()).build().writeTo(filer);
        }
    }

    private MethodSpec generateRouteMethod(ExecutableElement element) {
        List<? extends VariableElement> params = element.getParameters();
        MethodSpec.Builder methodBuild = new MethodGenerator(element)
                .overMethod(element.getSimpleName().toString());
        Route route = element.getAnnotation(Route.class);
        ParameterizedTypeName returnType = (ParameterizedTypeName) ClassName.get(element.getReturnType());
        TypeName realResultType;
        if (returnType.typeArguments != null && returnType.typeArguments.size() > 0) {
            realResultType = returnType.typeArguments.get(0);
        } else {
            realResultType = TypeName.VOID;
        }
        boolean isPageType = Consts.BUNDLE.equals(realResultType.toString());

        methodBuild.addStatement("$T route = $T.I()", Consts.CLASS_ROUTER, Consts.CLASS_ROUTER);
        methodBuild.addStatement(isPageType ? "$T<$T> request = route.build($S)" : "$T<$T> request = route.build($S)",
                Consts.CLASS_REQUEST, realResultType, route.value());
        VariableElement contextElement = null;
        for (VariableElement param : params) {
            if (Consts.CLASS_CONTEXT.equals(TypeName.get(param.asType()))) {
                RouteCtx ctxAn = param.getAnnotation(RouteCtx.class);
                if(contextElement == null || ctxAn != null){
                    contextElement = param;
                }
                continue;
            }
            Query query = param.getAnnotation(Query.class);
            String pName = query != null && !"".equals(query.value()) ? query.value() : param.getSimpleName().toString();
            methodBuild.addStatement("request" + getWithFieldStatement(param), pName, param);
        }
        methodBuild.addStatement("final $T<$T> call = request.dispatch($L, $T.class)", Consts.CLASS_CALL, realResultType, contextElement, realResultType);
        boolean isReturnRxObservable = Consts.CLASS_RX_OBSERVABLE.equals(returnType.rawType);
        if(isReturnRxObservable){
//            Observable<Bundle> observable = RouterRx.createCallObservable(call, request);
            methodBuild.addStatement("$T<$T> observable = $T.createCallObservable(call, request)", Consts.CLASS_RX_OBSERVABLE,realResultType,Consts.CLASS_ROUTER_RX);
            methodBuild.addStatement("return observable");
        }else{
            methodBuild.addStatement("return call");
        }
        return methodBuild.build();
    }

    private String getWithFieldStatement(VariableElement param) {
        String statement = null;
        int type = typeUtils.typeExchange(param);

        if (type == TypeKind.BOOLEAN.ordinal()) {
            statement = ".withBoolean($S, $L)";
        } else if (type == TypeKind.BYTE.ordinal()) {
            statement = ".withByte($S, $L)";
        } else if (type == TypeKind.SHORT.ordinal()) {
            statement = ".withShort($S, $L)";
        } else if (type == TypeKind.INT.ordinal()) {
            statement = ".withInt($S, $L)";
        } else if (type == TypeKind.LONG.ordinal()) {
            statement = ".withLong($S, $L)";
        } else if (type == TypeKind.FLOAT.ordinal()) {
            statement = ".withFloat($S, $L)";
        } else if (type == TypeKind.DOUBLE.ordinal()) {
            statement = ".withDouble($S, $L)";
        } else if (type == TypeKind.STRING.ordinal()) {
            statement = ".withString($S, $L)";
        }
        return statement;
    }

    /**
     * Categories field, find his papa.
     *
     * @param elements Field need autowired
     */
    private void categories(Set<? extends Element> elements) throws IllegalAccessException {
        if (CollectionUtils.isNotEmpty(elements)) {
            for (Element element : elements) {
                TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

                if (element.getModifiers().contains(Modifier.PRIVATE)) {
                    throw new IllegalAccessException("The autowired fields CAN NOT BE 'private'!!! please check field ["
                            + element.getSimpleName() + "] in class [" + enclosingElement.getQualifiedName() + "]");
                }

                // Has categeris
                if (parentAndChild.containsKey(enclosingElement)) {
                    parentAndChild.get(enclosingElement).add(element);
                } else {
                    List<Element> childs = new ArrayList<>();
                    childs.add(element);
                    parentAndChild.put(enclosingElement, childs);
                }
            }

            logger.info("categories finished.");
        }
    }
}

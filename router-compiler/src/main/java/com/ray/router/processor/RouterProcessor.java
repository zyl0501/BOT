package com.ray.router.processor;

import com.google.auto.common.AnnotationMirrors;
import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;
import com.google.auto.service.AutoService;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.ray.router.annotation.Action;
import com.ray.router.annotation.Autowired;
import com.ray.router.annotation.Interceptor;
import com.ray.router.processor.utils.TypeKind;
import com.ray.router.processor.utils.Consts;
import com.ray.router.processor.utils.Logger;
import com.ray.router.processor.utils.MoreAnnotationMirrors;
import com.ray.router.processor.utils.TextUtils;
import com.ray.router.processor.utils.TypeUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

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
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static com.ray.router.processor.utils.Consts.CLASS_ACTION_ACTIVITY;
import static com.ray.router.processor.utils.Consts.CLASS_ACTION_FRAGMENT;
import static com.ray.router.processor.utils.Consts.CLASS_ACTION_FRAGMENT_V4;
import static com.ray.router.processor.utils.Consts.CLASS_ACTION_RESULT_CALLBACK_FRAGMENT;
import static com.ray.router.processor.utils.Consts.CLASS_ACTION_RESULT_CALLBACK_FRAGMENT_V4;
import static com.ray.router.processor.utils.Consts.CLASS_BUNDLE;
import static com.ray.router.processor.utils.Consts.CLASS_COLLECTION_CLZ_EXTENDS_INTERCEPTOR;
import static com.ray.router.processor.utils.Consts.CLASS_CONTEXT;
import static com.ray.router.processor.utils.Consts.CLASS_FRAGMENT;
import static com.ray.router.processor.utils.Consts.CLASS_FRAGMENT_V4;
import static com.ray.router.processor.utils.Consts.CLASS_INTENT;
import static com.ray.router.processor.utils.Consts.CLASS_LIST_CLZ_EXTENDS_INTERCEPTOR;
import static com.ray.router.processor.utils.Consts.CLASS_MAP_CLZ_EXTENDS_ACTION_CLZ;
import static com.ray.router.processor.utils.Consts.CLASS_MAP_STRING_CLZ_EXTENDS_ACTION;
import static com.ray.router.processor.utils.Consts.CLASS_MAP_STRING_COLLECTION_CLZ_EXTENDS_INTERCEPTOR;
import static com.ray.router.processor.utils.Consts.CLASS_PROVIDER;
import static com.ray.router.processor.utils.Consts.CLASS_REQUEST_BUNDLE;
import static com.ray.router.processor.utils.Consts.CLASS_RESPONSE;
import static com.ray.router.processor.utils.Consts.KEY_MODULE_NAME;
import static com.ray.router.processor.utils.Consts.METHOD_ACTION;
import static com.ray.router.processor.utils.Consts.METHOD_ACTION_INPUT_CLZ;
import static com.ray.router.processor.utils.Consts.METHOD_CANCEL;
import static com.ray.router.processor.utils.Consts.METHOD_CREATE_INTENT;
import static com.ray.router.processor.utils.Consts.METHOD_INTERCEPTOR;
import static com.ray.router.processor.utils.Consts.METHOD_INVOKE;
import static com.ray.router.processor.utils.Consts.METHOD_MATCH_INTERCEPTOR;
import static com.ray.router.processor.utils.Consts.METHOD_ROOT_INTERCEPTOR;

@AutoService(Processor.class)
@SupportedOptions(KEY_MODULE_NAME)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class RouterProcessor extends AbstractProcessor {
    private TypeUtils typeUtils;
    private Filer filer;
    private Types types;
    private Elements elements;
    private Messager messager;
    private String moduleName;
    private Logger logger;
    private Map<TypeElement, List<Element>> parentAndChild = new HashMap<>();   // Contain field need autowired and his super class.

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
        types = processingEnv.getTypeUtils();
        elements = processingEnv.getElementUtils();      // Get class meta.
        typeUtils = new TypeUtils(types, elements);
        messager = processingEnv.getMessager();
        logger = new Logger(messager);

        // Attempt to get user configuration [moduleName]
        Map<String, String> options = processingEnv.getOptions();
        if (MapUtils.isNotEmpty(options)) {
            moduleName = options.get(KEY_MODULE_NAME);
        }

        if (StringUtils.isNotEmpty(moduleName)) {
            moduleName = moduleName.replaceAll("[^0-9a-zA-Z_]+", "");

            logger.info("The user has configuration the module name, it was [" + moduleName + "]");
        } else {
            logger.error("These no module name, at 'build.gradle', like :\n" +
                    "apt {\n" +
                    "    arguments {\n" +
                    "        moduleName project.getName();\n" +
                    "    }\n" +
                    "}\n");
            throw new RuntimeException("Router::Compiler >>> No module name, for more information, look at gradle log.");
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> ret = new HashSet<>();
        ret.add(Interceptor.class.getCanonicalName());
        ret.add(Action.class.getCanonicalName());
        return ret;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (CollectionUtils.isNotEmpty(annotations)) {
            Set<? extends Element> interceptorElements = roundEnv.getElementsAnnotatedWith(Interceptor.class);
            Set<? extends Element> actionElements = roundEnv.getElementsAnnotatedWith(Action.class);
            Set<? extends Element> autowiredElements = roundEnv.getElementsAnnotatedWith(Autowired.class);
            try {
                logger.info(">>> Found routes, start... <<<");
                categories(autowiredElements);
                this.parse(interceptorElements, actionElements);
            } catch (Exception e) {
                logger.error(e);
            }
            return true;
        }
        return false;
    }

    private void parse(Set<? extends Element> interceptorList, Set<? extends Element> actionList) {
        MethodSpec.Builder rootInterceptorBuilder = MethodSpec.methodBuilder(METHOD_ROOT_INTERCEPTOR)
                .addModifiers(Modifier.PUBLIC)
                .returns(CLASS_COLLECTION_CLZ_EXTENDS_INTERCEPTOR);
        MethodSpec.Builder interceptorBuilder = MethodSpec.methodBuilder(METHOD_INTERCEPTOR)
                .addModifiers(Modifier.PUBLIC)
                .returns(CLASS_MAP_STRING_COLLECTION_CLZ_EXTENDS_INTERCEPTOR);
        MethodSpec.Builder matchInterceptorBuilder = MethodSpec.methodBuilder(METHOD_MATCH_INTERCEPTOR)
                .addModifiers(Modifier.PUBLIC)
                .returns(CLASS_MAP_STRING_COLLECTION_CLZ_EXTENDS_INTERCEPTOR);

        rootInterceptorBuilder.addStatement("$T interceptors = new $T()", CLASS_COLLECTION_CLZ_EXTENDS_INTERCEPTOR, CLASS_LIST_CLZ_EXTENDS_INTERCEPTOR);
        interceptorBuilder.addStatement("$T interceptors = new $T()", CLASS_MAP_STRING_COLLECTION_CLZ_EXTENDS_INTERCEPTOR, CLASS_MAP_STRING_COLLECTION_CLZ_EXTENDS_INTERCEPTOR);
        matchInterceptorBuilder.addStatement("$T interceptors = new $T()", CLASS_MAP_STRING_COLLECTION_CLZ_EXTENDS_INTERCEPTOR, CLASS_MAP_STRING_COLLECTION_CLZ_EXTENDS_INTERCEPTOR);
        if (!CollectionUtils.isEmpty(interceptorList)) {
            for (Element element : interceptorList) {
                TypeMirror interceptorTm = element.asType();
                Interceptor interceptor = element.getAnnotation(Interceptor.class);
                //root interceptor
                if (isRoot(element, interceptor)) {
                    rootInterceptorBuilder.addStatement("interceptors.add($T.class)", interceptorTm);
                } else if (isPath(interceptor)) {
                    //path interceptor
                    String path = interceptor.path();
                    String listName = "list" + element.getSimpleName();
                    interceptorBuilder
                            .addStatement("$T $L = interceptors.get($S)", CLASS_COLLECTION_CLZ_EXTENDS_INTERCEPTOR, listName, path)
                            .beginControlFlow("if($L == null)", listName)
                            .addStatement("$L = new $T()", listName, CLASS_LIST_CLZ_EXTENDS_INTERCEPTOR)
                            .addStatement("interceptors.put($S, $L)", path, listName)
                            .endControlFlow()
                            .addStatement("$L.add($T.class)", listName, interceptorTm);
                } else if (isPattern(interceptor)) {
                    //pattern interceptor
                    String pattern = interceptor.pattern();
                    String listName = "list" + element.getSimpleName();
                    matchInterceptorBuilder
                            .addStatement("$T $L = interceptors.get($S)", CLASS_COLLECTION_CLZ_EXTENDS_INTERCEPTOR, listName, pattern)
                            .beginControlFlow("if($L == null)", listName)
                            .addStatement("$L = new $T()", listName, CLASS_LIST_CLZ_EXTENDS_INTERCEPTOR)
                            .addStatement("interceptors.put($S, $L)", pattern, listName)
                            .endControlFlow()
                            .addStatement("$L.add($T.class)", listName, interceptorTm);
                } else if (isClass(element)) {
                    //定义在 action 上的 interceptor
                    Action action = element.getAnnotation(Action.class);
                    if (action == null) {
                        throw new RuntimeException("Router::Compiler >>> " + interceptorTm + "所在的 clz " +
                                "没有 Action 注解");
                    } else {
                        Optional<AnnotationMirror> annotationMirror = MoreElements.getAnnotationMirror(element, Interceptor.class);
                        if (annotationMirror.isPresent()) {
                            Iterable<TypeMirror> klasses = MoreAnnotationMirrors.getTypeValue(annotationMirror.get(), "clz");
                            List<TypeElement> typeElements = FluentIterable.from(klasses).transform(
                                    new Function<TypeMirror, TypeElement>() {
                                        @Override
                                        public TypeElement apply(TypeMirror klass) {
                                            return MoreTypes.asTypeElement(klass);
                                        }
                                    }).toList();
                            String path = action.path();
                            String listName = "list" + element.getSimpleName();
                            interceptorBuilder
                                    .addStatement("$T $L = interceptors.get($S)", CLASS_COLLECTION_CLZ_EXTENDS_INTERCEPTOR, listName, path)
                                    .beginControlFlow("if($L == null)", listName)
                                    .addStatement("$L = new $T()", listName, CLASS_LIST_CLZ_EXTENDS_INTERCEPTOR)
                                    .addStatement("interceptors.put($S, $L)", path, listName)
                                    .endControlFlow();
                            for (TypeElement typeElement : typeElements) {
                                interceptorBuilder.addStatement("$L.add($T.class)", listName, typeElement.asType());
                            }

                        }
                    }
                }
            }
        }
        rootInterceptorBuilder.addStatement("return interceptors");
        interceptorBuilder.addStatement("return interceptors");
        matchInterceptorBuilder.addStatement("return interceptors");

        generateActivityAction(actionList);
        generateFragmentAction(actionList);

        MethodSpec.Builder actionBuilder = parseActions(actionList);
        MethodSpec.Builder actionInputBuilder = parseActionsInput(actionList);

        String fileName = "Router" + moduleName + "Provider";
        TypeSpec Router_Provider = TypeSpec.classBuilder(fileName)
                .addSuperinterface(CLASS_PROVIDER)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(rootInterceptorBuilder.build())
                .addMethod(interceptorBuilder.build())
                .addMethod(matchInterceptorBuilder.build())
                .addMethod(actionBuilder.build())
                .addMethod(actionInputBuilder.build())
                .build();

        try {
            JavaFile.builder("com.ray.router.route", Router_Provider)
                    .build()
                    .writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据 activity 的注解生成对应的 action
     *
     * @param actionList
     */
    private void generateActivityAction(Set<? extends Element> actionList) {
        if (CollectionUtils.isNotEmpty(actionList)) {
            TypeMirror activityTm = elements.getTypeElement(Consts.ACTIVITY).asType();
            for (Element element : actionList) {
                if (!types.isSubtype(element.asType(), activityTm)) {
                    continue;
                }
                String clzName = element.getSimpleName().toString();
                String fileName = clzName + Consts.SEPARATOR + "Action";
                TypeSpec activityAction = TypeSpec.classBuilder(fileName)
                        .superclass(CLASS_ACTION_ACTIVITY)
                        .addModifiers(Modifier.PUBLIC)
                        .addMethod(generateActivityIntentMethod(element, parentAndChild.get(element)).build())
                        .build();
                try {
                    JavaFile.builder("com.ray.router.route", activityAction)
                            .build()
                            .writeTo(filer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 根据 fragment 的注解生成对应的 action
     *
     * @param actionList
     */
    private void generateFragmentAction(Set<? extends Element> actionList) {
        if (CollectionUtils.isNotEmpty(actionList)) {
            TypeName type_FragmentAction = CLASS_ACTION_FRAGMENT;
            TypeName type_FragmentActionV4 = CLASS_ACTION_FRAGMENT_V4;
            TypeMirror fragmentTm = elements.getTypeElement(Consts.FRAGMENT).asType();
            TypeMirror fragmentV4Tm = elements.getTypeElement(Consts.FRAGMENT_V4).asType();
            for (Element element : actionList) {
                if (!types.isSubtype(element.asType(), fragmentTm) && !types.isSubtype(element.asType(), fragmentV4Tm)) {
                    continue;
                }
                boolean isV4 = types.isSubtype(element.asType(), fragmentV4Tm);

                String clzName = element.getSimpleName().toString();
                String fileName = clzName + Consts.SEPARATOR + "Action";
                TypeName superClass = isV4 ? type_FragmentActionV4 : type_FragmentAction;
                TypeSpec fragmentAction = TypeSpec.classBuilder(fileName)
                        .addSuperinterface(superClass)
                        .addModifiers(Modifier.PUBLIC)
                        .addMethod(generateFragmentActionInvokeMethod(element, parentAndChild.get(element), isV4).build())
                        .addMethod(MethodSpec.methodBuilder(METHOD_CANCEL)
                                .addAnnotation(Override.class)
                                .addModifiers(Modifier.PUBLIC)
                                .build())
                        .build();
                try {
                    JavaFile.builder("com.ray.router.route", fragmentAction)
                            .build()
                            .writeTo(filer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 生成 activity 的 action 的 createIntent 方法
     *
     * @param element
     * @param autowireList
     * @return
     */
    private MethodSpec.Builder generateActivityIntentMethod(Element element, List<? extends Element> autowireList) {
        MethodSpec.Builder actionBuilder = MethodSpec.methodBuilder(METHOD_CREATE_INTENT)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(CLASS_CONTEXT, "context")
                .addParameter(CLASS_REQUEST_BUNDLE, "request")
                .returns(CLASS_INTENT);

        Action actionEl = element.getAnnotation(Action.class);
        actionBuilder.addStatement("this.needResult = " + (actionEl.hasResult() ? "true" : "false") + ";");
        actionBuilder.addStatement("$T bundle = request.getData()", CLASS_BUNDLE);
        actionBuilder.addStatement("$T intent = new $T(context, $T.class)", CLASS_INTENT, CLASS_INTENT, ClassName.get(element.asType()));
        actionBuilder.beginControlFlow("if(bundle == null)");
        actionBuilder.addStatement("bundle = new $T()", CLASS_BUNDLE);
        actionBuilder.endControlFlow();

        if (!CollectionUtils.isEmpty(autowireList)) {
            for (Element fieldEle : autowireList) {
                Autowired fieldConfig = fieldEle.getAnnotation(Autowired.class);
                String fieldName = fieldEle.getSimpleName().toString();
                String statement = "bundle.";
                statement = buildBundleStatement(statement, typeUtils.typeExchange(fieldEle), "request");
                if (TextUtils.isEmpty(statement)) continue;
                String keyName = StringUtils.isEmpty(fieldConfig.name()) ? fieldName : fieldConfig.name();
                actionBuilder.addStatement(statement, keyName, keyName);
            }
        }
        actionBuilder.addStatement("intent.putExtras(bundle)");
        actionBuilder.addStatement("return intent");
        return actionBuilder;
    }

    /**
     * 生成 fragment 的 action 的 invoke 方法
     *
     * @param element
     * @param autowireList
     * @return
     */
    private MethodSpec.Builder generateFragmentActionInvokeMethod(Element element, List<? extends Element> autowireList, boolean isV4) {
        TypeName fragmentName = ClassName.get(element.asType());
        TypeName callbackType = isV4 ? CLASS_ACTION_RESULT_CALLBACK_FRAGMENT_V4 : CLASS_ACTION_RESULT_CALLBACK_FRAGMENT;
        TypeName fragmentType = isV4 ? CLASS_FRAGMENT_V4 : CLASS_FRAGMENT;
        TypeName responseType = CLASS_RESPONSE;

        MethodSpec.Builder actionBuilder = MethodSpec.methodBuilder(METHOD_INVOKE)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(CLASS_CONTEXT, "context")
                .addParameter(CLASS_REQUEST_BUNDLE, "request")
                .addParameter(callbackType, "callback");

        actionBuilder.addStatement("$T bundle = request.getData()", CLASS_BUNDLE);
        actionBuilder.beginControlFlow("if(bundle == null)");
        actionBuilder.addStatement("bundle = new $T()", CLASS_BUNDLE);
        actionBuilder.endControlFlow();

        if (!CollectionUtils.isEmpty(autowireList)) {
            for (Element fieldEle : autowireList) {
                Autowired fieldConfig = fieldEle.getAnnotation(Autowired.class);
                String fieldName = fieldEle.getSimpleName().toString();
                String statement = "bundle.";
                statement = buildBundleStatement(statement, typeUtils.typeExchange(fieldEle), "request");
                if (TextUtils.isEmpty(statement)) continue;
                String keyName = StringUtils.isEmpty(fieldConfig.name()) ? fieldName : fieldConfig.name();
                actionBuilder.addStatement(statement, keyName, keyName);
            }
        }
        actionBuilder.addStatement("$T fragment = new $T()", fragmentName, fragmentName);
        actionBuilder.addStatement("fragment.setArguments(bundle)");
        actionBuilder.addStatement("callback.onResponse($T.<$T>createSuccess(fragment))", responseType, fragmentType);
        return actionBuilder;
    }

    private MethodSpec.Builder parseActions(Set<? extends Element> actionList) {
        MethodSpec.Builder actionBuilder = MethodSpec.methodBuilder(METHOD_ACTION)
                .addModifiers(Modifier.PUBLIC)
                .returns(CLASS_MAP_STRING_CLZ_EXTENDS_ACTION);
        if (CollectionUtils.isNotEmpty(actionList)) {
            TypeMirror activityTm = elements.getTypeElement(Consts.ACTIVITY).asType();
            TypeMirror fragmentTm = elements.getTypeElement(Consts.FRAGMENT).asType();
            TypeMirror fragmentV4Tm = elements.getTypeElement(Consts.FRAGMENT_V4).asType();

            actionBuilder.addStatement("$T actions = new $T()", CLASS_MAP_STRING_CLZ_EXTENDS_ACTION, CLASS_MAP_STRING_CLZ_EXTENDS_ACTION);
            for (Element element : actionList) {
                Action action = element.getAnnotation(Action.class);
                if (types.isSubtype(element.asType(), activityTm)) {
                    actionBuilder.addStatement("actions.put($S, $N.class)", action.path(), element.getSimpleName().toString() + Consts.SEPARATOR + "Action");
                } else if (types.isSubtype(element.asType(), fragmentTm) || types.isSubtype(element.asType(), fragmentV4Tm)) {
                    actionBuilder.addStatement("actions.put($S, $N.class)", action.path(), element.getSimpleName().toString() + Consts.SEPARATOR + "Action");
                } else {
                    actionBuilder.addStatement("actions.put($S, $T.class)", action.path(), element.asType());
                }
            }
            actionBuilder.addStatement("return actions");
        } else {
            actionBuilder.addStatement("return null");
        }
        return actionBuilder;
    }

    private MethodSpec.Builder parseActionsInput(Set<? extends Element> actionList){
        MethodSpec.Builder actionInputClzBuilder = MethodSpec.methodBuilder(METHOD_ACTION_INPUT_CLZ)
                .addModifiers(Modifier.PUBLIC)
                .returns(CLASS_MAP_CLZ_EXTENDS_ACTION_CLZ);
        if (CollectionUtils.isNotEmpty(actionList)) {
            TypeMirror activityTm = elements.getTypeElement(Consts.ACTIVITY).asType();
            TypeMirror fragmentTm = elements.getTypeElement(Consts.FRAGMENT).asType();
            TypeMirror fragmentV4Tm = elements.getTypeElement(Consts.FRAGMENT_V4).asType();
            TypeMirror bundleTm = elements.getTypeElement(Consts.BUNDLE).asType();

            actionInputClzBuilder.addStatement("$T map = new $T()", CLASS_MAP_CLZ_EXTENDS_ACTION_CLZ, CLASS_MAP_CLZ_EXTENDS_ACTION_CLZ);
            for (Element element : actionList) {
                Action action = element.getAnnotation(Action.class);
                String pageActionName = element.getSimpleName().toString() + Consts.SEPARATOR + "Action";
                if (types.isSubtype(element.asType(), activityTm)) {
                    actionInputClzBuilder.addStatement("map.put($N.class, $T.class)", pageActionName, bundleTm);
                } else if (types.isSubtype(element.asType(), fragmentTm) || types.isSubtype(element.asType(), fragmentV4Tm)) {
                    actionInputClzBuilder.addStatement("map.put($N.class, $T.class)", pageActionName, bundleTm);
                } else {
                    Optional<AnnotationMirror> annotationMirror = MoreElements.getAnnotationMirror(element, Action.class);
                    if (annotationMirror.isPresent()) {
                        AnnotationValue inputClzValue = AnnotationMirrors.getAnnotationValue(annotationMirror.get(), "inputClz");
                        if(!TextUtils.equals(inputClzValue.toString(), Void.class.getCanonicalName()+".class")){
                            actionInputClzBuilder.addStatement("map.put($T.class, $N)", element.asType(), inputClzValue.toString());
                        }else{
//                            String inputArg = getActionInputType(element);
//                            if(!TextUtils.isEmpty(inputArg))
//                                actionInputClzBuilder.addStatement("map.put($T.class, $N)", element.asType(), inputArg+".class");
                        }
                    }
                }
            }
            actionInputClzBuilder.addStatement("return map");
        } else {
            actionInputClzBuilder.addStatement("return null");
        }
        return actionInputClzBuilder;
    }

    private String getActionInputType(Element element){
        List<? extends TypeMirror> interfaces = getAllInterfaces(element, null);
        if(interfaces != null) {
            TypeMirror actionTm = elements.getTypeElement(Consts.IACTION).asType();
            for (TypeMirror inter : interfaces) {
                try {
                    if (types.isSubtype(inter, actionTm)) {
                        List<? extends TypeMirror> args = MoreTypes.asDeclared(inter).getTypeArguments();
                        if (args != null && args.size() >= 1) {
                            return args.get(0).toString();
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private List<? extends TypeMirror> getAllInterfaces(Element element, List<TypeMirror> list) {
        if (element == null || !(element instanceof TypeElement)) {
            return null;
        }
        if (list == null) list = new ArrayList<>();
        TypeElement typeElement = (TypeElement) element;
        List<? extends TypeMirror> curInterfaceList = typeElement.getInterfaces();
        if (curInterfaceList != null) {
            for(TypeMirror interfaceType : curInterfaceList){
                try{
                    list.add(interfaceType);
                    typeElement = MoreTypes.asTypeElement(interfaceType);
                    getAllInterfaces(typeElement, list);
                }catch (Exception e){
                    e.printStackTrace();
                    break;
                }
            }
        }
        while (typeElement.getSuperclass() != null) {
            TypeMirror subClass = typeElement.getSuperclass();
            try {
                getAllInterfaces(MoreTypes.asElement(subClass), list);
            }catch (Exception e){
                e.printStackTrace();
            }
            try{
                typeElement = MoreTypes.asTypeElement(subClass);
            }catch (Exception e){
                e.printStackTrace();
                break;
            }
        }
        return list;
    }

    private boolean isRoot(Element element, Interceptor interceptor) {
        return TextUtils.isEmpty(interceptor.path())
                && TextUtils.isEmpty(interceptor.pattern())
                && !isClass(element);
    }

    private boolean isPath(Interceptor interceptor) {
        return !TextUtils.isEmpty(interceptor.path());
    }

    private boolean isPattern(Interceptor interceptor) {
        return !TextUtils.isEmpty(interceptor.pattern());
    }

    private boolean isClass(Element element) {
        Optional<AnnotationMirror> annotationMirror = MoreElements.getAnnotationMirror(element, Interceptor.class);
        if (annotationMirror.isPresent()) {
            Iterable<TypeMirror> klasses = MoreAnnotationMirrors.getTypeValue(annotationMirror.get(), "clz");
            if (klasses != null && klasses.iterator() != null) {
                TypeMirror clzType = klasses.iterator().next();
                return !TextUtils.equals(((DeclaredType) clzType).asElement().getSimpleName(), Void.class.getSimpleName());
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean hasActionInputClass(Element element) {
        Optional<AnnotationMirror> annotationMirror = MoreElements.getAnnotationMirror(element, Action.class);
        if (annotationMirror.isPresent()) {
            Iterable<TypeMirror> klasses = MoreAnnotationMirrors.getTypeValue(annotationMirror.get(), "inputClz");
            if (klasses != null && klasses.iterator() != null) {
                TypeMirror clzType = klasses.iterator().next();
                return !TextUtils.equals(((DeclaredType) clzType).asElement().getSimpleName(), Void.class.getSimpleName());
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * 根据 autowired 标注的 field 注入到 Activity/Fragment 中
     *
     * @param autowiredElements Field need autowired
     */
    private void categories(Set<? extends Element> autowiredElements) throws IllegalAccessException {
        if (CollectionUtils.isNotEmpty(autowiredElements)) {
            for (Element element : autowiredElements) {
                TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

                if (element.getModifiers().contains(Modifier.PRIVATE)) {
                    throw new IllegalAccessException("The autowired fields CAN NOT BE 'private'!!! please check field ["
                            + element.getSimpleName() + "] in class [" + enclosingElement.getQualifiedName() + "]");
                }

                if (parentAndChild.containsKey(enclosingElement)) { // Has categries
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

    private String buildBundleStatement(String statment, int type, String request) {
        if (type == TypeKind.BOOLEAN.ordinal()) {
            statment += "putBoolean($S, " + request + ".getBoolean($S, false))";
        } else if (type == TypeKind.BYTE.ordinal()) {
            statment += "putByte($S, " + request + ".getByte($S, 0))";
        } else if (type == TypeKind.SHORT.ordinal()) {
            statment += "putShort($S, " + request + ".getShort($S, 0))";
        } else if (type == TypeKind.INT.ordinal()) {
            statment += "putInt($S, " + request + ".getInt($S, 0))";
        } else if (type == TypeKind.LONG.ordinal()) {
            statment += "putLong($S, " + request + ".getLong($S, 0))";
        } else if (type == TypeKind.FLOAT.ordinal()) {
            statment += "putFloat($S, " + request + ".getFloat($S, 0))";
        } else if (type == TypeKind.DOUBLE.ordinal()) {
            statment += "putDouble($S, " + request + ".getDouble($S, 0))";
        } else if (type == TypeKind.STRING.ordinal()) {
            statment += "putString($S, " + request + ".getString($S))";
        } else {
            //其他类型返回 null
            statment = null;
        }
//        else if (type == TypeKind.PARCELABLE.ordinal()) {
//            statment += "putParcelable($S, " + request + ".getParcelable($S))";
//        }
        return statment;
    }
}

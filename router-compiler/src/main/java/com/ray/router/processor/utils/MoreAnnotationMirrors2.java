package com.ray.router.processor.utils;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor7;

import static com.google.auto.common.AnnotationMirrors.getAnnotationValue;

/**
 * Borrowed from Dagger, Copyright Google
 * https://github.com/google/dagger/blob/master/compiler/src/main/java/dagger/internal/codegen
 * /MoreAnnotationMirrors.java
 */
public final class MoreAnnotationMirrors2 {
  private MoreAnnotationMirrors2() {
  }

  private static final AnnotationValueVisitor<TypeMirror, Void> AS_TYPE =
      new SimpleAnnotationValueVisitor7<TypeMirror, Void>() {
        @Override
        public TypeMirror visitType(TypeMirror t, Void p) {
          return t;
        }

        @Override
        protected TypeMirror defaultAction(Object o, Void p) {
          throw new TypeNotPresentException(o.toString(), null);
        }
      };

  private static final AnnotationValueVisitor<AnnotationValue, String>
      AS_ANNOTATION_VALUES =
      new SimpleAnnotationValueVisitor7<AnnotationValue, String>() {
          @Override
          public AnnotationValue visitAnnotation(AnnotationMirror annotationMirror, String s) {
              return annotationMirror.getElementValues().get(s);
          }

          @Override
          protected AnnotationValue defaultAction(Object o, String s) {
              return super.defaultAction(o, s);
          }
      };

  /**
   * Returns the list of values represented by an array annotation value.
   *
   * @throws IllegalArgumentException unless {@code annotationValue} represents an array
   */
  public static AnnotationValue asAnnotationValues(
      AnnotationValue annotationValue) {
    return annotationValue.accept(AS_ANNOTATION_VALUES, null);
  }

  /**
   * Returns the type represented by an annotation value.
   *
   * @throws IllegalArgumentException unless {@code annotationValue} represents a single type
   */
  private static TypeMirror asType(AnnotationValue annotationValue) {
    return AS_TYPE.visit(annotationValue);
  }

  /**
   * Returns the value named {@code name} from {@code annotationMirror}.
   *
   * @throws IllegalArgumentException unless that member represents a single type
   */
  public static TypeMirror getTypeValue(AnnotationMirror annotationMirror, String name) {
      return asType(asAnnotationValues(getAnnotationValue(annotationMirror, name)));
  }
}

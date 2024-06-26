/*
 * This file is part of Titanium
 * Copyright (C) 2024, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium.util;

import com.hrznstudio.titanium.Titanium;
import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.ModFileScanData;

public class AnnotationUtil {

    public static List<Class<?>> getAnnotatedClasses(Class<? extends Annotation> annotation) {
        List<Class<?>> classList = new ArrayList<>();
        Type type = Type.getType(annotation);
        for (ModFileScanData allScanDatum : ModList.get().getAllScanData()) {
            for (ModFileScanData.AnnotationData allScanDatumAnnotation : allScanDatum.getAnnotations()) {
                if (Objects.equals(allScanDatumAnnotation.annotationType(), type)) {
                    try {
                        classList.add(Class.forName(allScanDatumAnnotation.memberName()));
                    } catch (ClassNotFoundException e) {
                        Titanium.LOGGER.error(e);
                    }
                }
            }
        }
        return classList;
    }

    public static List<Class<?>> getFilteredAnnotatedClasses(Class<? extends Annotation> annotation, String filter) {
        List<Class<?>> classList = new ArrayList<>();
        Type type = Type.getType(annotation);
        for (ModFileScanData.AnnotationData allScanDatumAnnotation : ModList.get().getModFileById(filter).getFile().getScanResult().getAnnotations()) {
            if (Objects.equals(allScanDatumAnnotation.annotationType(), type)) {
                try {
                    classList.add(Class.forName(allScanDatumAnnotation.memberName()));
                } catch (ClassNotFoundException e) {
                    Titanium.LOGGER.error(e);
                }
            }
        }
        return classList;
    }

    public static List<Field> getAnnotatedFields(Class<? extends Annotation> annotation) {
        List<Field> fields = new ArrayList<>();
        Type type = Type.getType(annotation);
        for (ModFileScanData allScanDatum : ModList.get().getAllScanData()) {
            for (ModFileScanData.AnnotationData annotationData : allScanDatum.getAnnotations()) {
                if (Objects.equals(annotationData.annotationType(), type)) {
                    try {
                        for (Field field : Class.forName(annotationData.clazz().getClassName()).getDeclaredFields()) {
                            if (field.getName().equalsIgnoreCase(annotationData.memberName())) {
                                fields.add(field);
                            }
                        }
                    } catch (ClassNotFoundException e) {
                        Titanium.LOGGER.error(e);
                    }
                }
            }
        }
        return fields;
    }
}

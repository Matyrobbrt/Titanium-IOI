/*
 * This file is part of Titanium
 * Copyright (C) 2018, Horizon Studio <contact@hrznstudio.com>, All rights reserved.
 */
package com.hrznstudio.titanium.base.pulsar.pulse;

import java.lang.annotation.*;

/**
 * Metadata annotation for IPulse implementations.
 * <p>
 * Pulses should use the standard Google Event Bus @Subscribe annotation to catch FML events, which are forwarded from
 * the current mod container (including Pre/Init/Post events).
 *
 * @author Arkan <arkan@drakon.io>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface Pulse {

    /**
     * @return This Pulses UID - Much like FML mods have mod IDs.
     */
    String id();

    /**
     * @return This Pulses description, human-readable for config files. Can be blank.
     */
    String description() default "";

    /**
     * @return Dependant mod IDs, seperated by ; as in FML. Skips checks when undefined.
     */
    String modsRequired() default "";

    /**
     * @return Dependeant Pulse IDs, seperated by ; . Skips checks when undefined.
     */
    String pulsesRequired() default "";

    /**
     * @return Whether this Pulse is mandatory or not (true -> mandatory).
     */
    boolean forced() default false;

    /**
     * @return Whether a configurable Pulse should be enabled by default. Ignored where forced = true.
     */
    boolean defaultEnable() default true;

}
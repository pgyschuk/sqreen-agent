package com.sqreen.agent;

import java.lang.instrument.Instrumentation;

/**
 * Entry to point Sqreen agent
 */
public class Agent {
    public static void premain(String args, Instrumentation instrumentation) {
        instrumentation.addTransformer(new HttpResponseTransformer());
    }
}

package com.sqreen.agent;

import java.lang.instrument.Instrumentation;

public class Agent {
    public static void premain(String args, Instrumentation instrumentation) {
        instrumentation.addTransformer(new HttpResponseTransformer());
    }
}

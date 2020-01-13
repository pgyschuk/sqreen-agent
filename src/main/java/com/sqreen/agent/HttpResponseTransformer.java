package com.sqreen.agent;

import org.apache.catalina.connector.Response;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

import static org.objectweb.asm.Opcodes.ALOAD;

/**
 * Class for transforming of {@link Response} and adding custom header to each request
 */
public class HttpResponseTransformer implements ClassFileTransformer {

    public byte[] transform(ClassLoader loader,
                            String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) {
        if (className.equalsIgnoreCase("org/apache/catalina/connector/Response")) {
            final ClassReader reader = new ClassReader(classfileBuffer);
            final ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
            final ClassVisitor visitor = new SqreenClassVisitor(Opcodes.ASM5, writer);
            reader.accept(visitor, 0);
            return writer.toByteArray();
        }
        return classfileBuffer;
    }

    /**
     * Class intercepting {@link Response} and modifying byte code of {@link Response#finishResponse()} method
     * Adding 4 instructions after original code:
     *  - loading 'this' reference
     *  - putting header name value to stack
     *  - putting header value to stack
     *  - adding instruction to execute addHeader method for 'this' with parameters added to stack
     */
    class SqreenClassVisitor extends ClassVisitor {
        public SqreenClassVisitor(int api, ClassVisitor cv) {
            super(api, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
            if (name.equalsIgnoreCase("finishResponse")) {
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 0);
                mv.visitLdcInsn("X-Instrumented-By");
                mv.visitLdcInsn("Sqreen");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                        "org/apache/catalina/connector/Response", "addHeader", "(Ljava/lang/String;Ljava/lang/String;)V", false);
            }
            return mv;
        }
    }
}

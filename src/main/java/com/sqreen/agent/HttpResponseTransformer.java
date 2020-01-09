package com.sqreen.agent;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

import static org.objectweb.asm.Opcodes.ALOAD;


public class HttpResponseTransformer implements ClassFileTransformer {

    public byte[] transform(ClassLoader loader,
                            String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) {
        if (className.equalsIgnoreCase("org/apache/catalina/connector/Response")) {
            final ClassReader reader = new ClassReader(classfileBuffer);
            final ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
            final ClassVisitor visitor = new ScreenClassVisitor(Opcodes.ASM5, writer);
            reader.accept(visitor, 0);
            return writer.toByteArray();
        }
        return classfileBuffer;
    }

    class ScreenClassVisitor extends ClassVisitor {
        public ScreenClassVisitor(int api, ClassVisitor cv) {
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

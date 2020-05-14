package com.hhh.transform.asm;

import java.util.List;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ASMClassVisitor extends ClassVisitor {

  private final List<ASMInvokerTrackInfo> mInvokerTrackInfos;
  private final ClassModifiedCallback mClassModifiedCallback;
  private String mClassName;
  private boolean mClassModified;

  public ASMClassVisitor(ClassVisitor classVisitor, List<ASMInvokerTrackInfo> invokerTrackInfos) {
    super(Opcodes.ASM8, classVisitor);
    mInvokerTrackInfos = invokerTrackInfos;
    mClassModifiedCallback = modified -> mClassModified = modified;
  }

  @Override
  public void visit(int version, int access, String name, String signature, String superName,
      String[] interfaces) {
    super.visit(version, access, name, signature, superName, interfaces);
    mClassName = name.replace('/', '.');
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
      String[] exceptions) {
    MethodVisitor methodVisitor =
        super.visitMethod(access, name, descriptor, signature, exceptions);
    ASMMethodVisitor asmMethodVisitor =
        new ASMMethodVisitor(Opcodes.ASM8, methodVisitor, access, name, descriptor);
    return asmMethodVisitor.setInvokerTrackInfos(mInvokerTrackInfos)
        .setClassModifiedCallback(mClassModifiedCallback).setClassName(mClassName);
  }

  public boolean isClassModified() {
    return mClassModified;
  }
}

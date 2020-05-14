package com.hhh.transform.asm;

import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.hhh.annotation.InvokerTrackMark;

public class ClassAnnotationVisitor extends ClassVisitor {

  private static final String TAG = "ASMAnnotationVisitor:";

  private final List<ASMInvokerTrackInfo> mInvokerTrackInfos;

  private boolean mIsInvokerTrackClass;
  private String mAnnotationClassName;

  public ClassAnnotationVisitor(ClassVisitor classVisitor,
      List<ASMInvokerTrackInfo> invokerTrackInfos) {
    super(Opcodes.ASM8, classVisitor);
    mInvokerTrackInfos = invokerTrackInfos;
  }

  @Override
  public void visit(int version, int access, String name, String signature, String superName,
      String[] interfaces) {
    System.out.println(TAG + "visit:" + name);
    super.visit(version, access, name, signature, superName, interfaces);
    mAnnotationClassName = name;
  }

  @Override
  public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
    System.out.println(TAG + "visitAnnotation:" + descriptor + " : " + visible);
    AnnotationVisitor annotationVisitor = super.visitAnnotation(descriptor, visible);
    if (descriptor.endsWith(InvokerTrackMark.class.getSimpleName() + ";")) {
      mIsInvokerTrackClass = true;
    }
    return annotationVisitor;
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
      String[] exceptions) {
    MethodVisitor methodVisitor =
        super.visitMethod(access, name, descriptor, signature, exceptions);
    if (mIsInvokerTrackClass) {
      System.out.println(TAG + "visitMethod:" + name);
      AnnotationMethodVisitor annotationMethodVisitor =
          new AnnotationMethodVisitor(Opcodes.ASM8, methodVisitor, access, name, descriptor);
      ASMInvokerTrackInfo invokerTrackInfo = new ASMInvokerTrackInfo();
      invokerTrackInfo.owner = mAnnotationClassName;
      invokerTrackInfo.name = name;
      annotationMethodVisitor.setInvokerTrackInfo(invokerTrackInfo, mInvokerTrackInfos);
      return annotationMethodVisitor;
    }
    return methodVisitor;
  }
}

package com.hhh.transform.asm;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;

public class MethodAnnotationVisitor extends AnnotationVisitor {

  private ASMInvokerTrackInfo mInvokerTrackInfo;

  public MethodAnnotationVisitor(AnnotationVisitor annotationVisitor,
      ASMInvokerTrackInfo invokerTrackInfo) {
    super(Opcodes.ASM8, annotationVisitor);
    mInvokerTrackInfo = invokerTrackInfo;
  }

  @Override
  public void visit(String name, Object value) {
    super.visit(name, value);
    if (name.equals("className")) {
      mInvokerTrackInfo.className = (String) value;
    } else if (name.equals("methodName")) {
      mInvokerTrackInfo.methodName = (String) value;
    }
  }
}

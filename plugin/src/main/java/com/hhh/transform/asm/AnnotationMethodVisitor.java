package com.hhh.transform.asm;

import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.AdviceAdapter;

import com.hhh.annotation.InvokerTrack;

public class AnnotationMethodVisitor extends AdviceAdapter {

  private static final String TAG = "ASMAnnotationMethodVisitor:";

  private ASMInvokerTrackInfo mInvokerTrackInfo;
  private List<ASMInvokerTrackInfo> mInvokerTrackInfos;

  public AnnotationMethodVisitor(int api, MethodVisitor methodVisitor, int access, String name,
      String descriptor) {
    super(api, methodVisitor, access, name, descriptor);
  }

  public void setInvokerTrackInfo(ASMInvokerTrackInfo invokerTrackInfo,
      List<ASMInvokerTrackInfo> invokerTrackInfos) {
    mInvokerTrackInfo = invokerTrackInfo;
    mInvokerTrackInfos = invokerTrackInfos;
  }

  @Override
  public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
    System.out.println(TAG + "visitAnnotation:" + descriptor + " : " + visible);
    AnnotationVisitor annotationVisitor = super.visitAnnotation(descriptor, visible);
    if (descriptor.endsWith(InvokerTrack.class.getSimpleName() + ";")) {
      mInvokerTrackInfos.add(mInvokerTrackInfo);
      return new MethodAnnotationVisitor(annotationVisitor, mInvokerTrackInfo);
    }
    return annotationVisitor;
  }
}

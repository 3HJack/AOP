package com.hhh.transform.asm;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.AdviceAdapter;

public class ASMMethodVisitor extends AdviceAdapter {

  private static final String TAG = "ASMMethodVisitor:";

  private List<ASMInvokerTrackInfo> mInvokerTrackInfos;
  private ClassModifiedCallback mClassModifiedCallback;
  private String mClassName;
  private int mLineNumber;

  protected ASMMethodVisitor(int api, MethodVisitor methodVisitor, int access, String name,
      String descriptor) {
    super(api, methodVisitor, access, name, descriptor);
  }

  public ASMMethodVisitor setInvokerTrackInfos(List<ASMInvokerTrackInfo> invokerTrackInfos) {
    mInvokerTrackInfos = invokerTrackInfos;
    return this;
  }

  public ASMMethodVisitor setClassModifiedCallback(ClassModifiedCallback classModifiedCallback) {
    mClassModifiedCallback = classModifiedCallback;
    return this;
  }

  public ASMMethodVisitor setClassName(String className) {
    mClassName = className;
    return this;
  }

  @Override
  public void visitMethodInsn(int opcode, String owner, String name, String descriptor,
      boolean isInterface) {
    ASMInvokerTrackInfo invokerTrackInfo = getInvokerTrackInfo(owner, name);
    if (invokerTrackInfo != null) {
      try {
        FileUtils.writeStringToFile(invokerTrackInfo.logFile,
            mClassName + " : " + mLineNumber + "\n", true);
      } catch (IOException e) {
        e.printStackTrace();
      }
      System.out.println(TAG + owner + " : " + name + " : " + mLineNumber);
      mv.visitMethodInsn(INVOKESTATIC, invokerTrackInfo.owner, invokerTrackInfo.name, "()V", false);
      if (mClassModifiedCallback != null) {
        mClassModifiedCallback.setModifyStatus(true);
      }
    }
    super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
  }

  @Override
  public void visitLineNumber(int line, Label start) {
    super.visitLineNumber(line, start);
    mLineNumber = line;
  }

  private ASMInvokerTrackInfo getInvokerTrackInfo(String owner, String name) {
    for (ASMInvokerTrackInfo invokerTrackInfo : mInvokerTrackInfos) {
      if (owner.replace('/', '.').equals(invokerTrackInfo.className)
          && name.equals(invokerTrackInfo.methodName)) {
        return invokerTrackInfo;
      }
    }
    return null;
  }
}

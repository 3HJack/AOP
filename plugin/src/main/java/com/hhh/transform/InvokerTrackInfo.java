package com.hhh.transform;

import java.io.File;

public abstract class InvokerTrackInfo {

  public String className;
  public String methodName;
  public File logFile;

  @Override
  public String toString() {
    return super.toString() + ":" + className + ":" + methodName;
  }

  public void createFile() {
    logFile = new File("apm/" + className + "." + methodName);
    if (logFile.exists()) {
      logFile.delete();
    }
  }
}

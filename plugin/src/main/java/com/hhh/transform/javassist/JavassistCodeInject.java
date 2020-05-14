package com.hhh.transform.javassist;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.JarInput;
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension;
import com.hhh.annotation.InvokerTrack;
import com.hhh.annotation.InvokerTrackMark;
import com.hhh.transform.Constants;
import com.hhh.transform.utils.ZipUtils;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

public class JavassistCodeInject {

  private static final String TAG = "JavassistCodeInject:";

  private static final int CLASS_SUFFIX_LENGTH = Constants.CLASS_SUFFIX.length() + 1;
  private static final ClassPool CLASS_POOL = ClassPool.getDefault();

  private final Map<String, Boolean> mClassModified = new HashMap<>();
  private final Map<String, Boolean> mDirectoryModified = new HashMap<>();

  private final List<JavassistInvokerTrackInfo> mInvokerTrackInfos = new ArrayList<>();
  private CtClass mInvokerTrackMarkClass;

  public void injectCode(Collection<DirectoryInput> directoryInputs,
      Collection<JarInput> jarInputs, Project project) {
    importPackage();
    appendClassPath(directoryInputs, jarInputs, project);
    if (directoryInputs != null && !directoryInputs.isEmpty()) {
      processAnnotation(directoryInputs);
      processApplication(directoryInputs);
    }
    if (jarInputs != null && !jarInputs.isEmpty()) {
      processJar(jarInputs);
    }
  }

  private void appendClassPath(Collection<DirectoryInput> directoryInputs,
      Collection<JarInput> jarInputs, Project project) {
    // 将 Application 层类加入类池
    if (directoryInputs != null && !directoryInputs.isEmpty()) {
      directoryInputs.forEach(directoryInput -> {
        String directory = directoryInput.getFile().getAbsolutePath();
        System.out.println(TAG + "ApplicationPath:" + directory);
        try {
          CLASS_POOL.appendClassPath(directory);
        } catch (NotFoundException e) {
          e.printStackTrace();
        }
      });
    }

    // 将 Library 层类加入类池
    if (jarInputs != null && !jarInputs.isEmpty()) {
      jarInputs.forEach(jarInput -> {
        String directory = jarInput.getFile().getAbsolutePath();
        directory = directory.replace(Constants.LIBRARY_JAR_SUFFIX, Constants.LIBRARY_CLASS_SUFFIX);
        System.out.println(TAG + "LibraryPath:" + directory);
        try {
          CLASS_POOL.appendClassPath(directory);
        } catch (NotFoundException e) {
          e.printStackTrace();
        }
      });
    }

    // 将 android.jar 加入类池，否则会找不到 android 相关的所有类
    if (project != null) {
      BaseAppModuleExtension extension =
          ((BaseAppModuleExtension) project.getExtensions().getByName("android"));
      String androidPath = extension.getBootClasspath().get(0).getAbsolutePath();
      System.out.println(TAG + "androidPath:" + androidPath);
      try {
        CLASS_POOL.appendClassPath(androidPath);
      } catch (NotFoundException e) {
        e.printStackTrace();
      }
    }
  }

  /** 插入代码时可以不写包名 */
  private void importPackage() {
    CLASS_POOL.importPackage("android.util");
  }

  private void processAnnotation(Collection<DirectoryInput> directoryInputs) {
    directoryInputs.forEach(directoryInput -> {
      int beginIndex = directoryInput.getFile().getAbsolutePath().length() + 1;
      FileUtils.listFiles(directoryInput.getFile(), new String[] {Constants.CLASS_SUFFIX}, true)
          .forEach(file -> {
            if (mInvokerTrackMarkClass != null) {
              return;
            }
            String classPath = file.getAbsolutePath();
            String className =
                classPath.substring(beginIndex, classPath.length() - CLASS_SUFFIX_LENGTH)
                    .replace(File.separatorChar, '.');
            try {
              CtClass ctClass = CLASS_POOL.getCtClass(className);
              InvokerTrackMark invokerTrackMark =
                  (InvokerTrackMark) ctClass.getAnnotation(InvokerTrackMark.class);
              if (invokerTrackMark != null) {
                System.out.println(TAG + ctClass.getName());
                mInvokerTrackMarkClass = ctClass;
                getInvokerTrackInfo();
              }
            } catch (NotFoundException | ClassNotFoundException e) {
              e.printStackTrace();
            }
          });
    });
  }

  private void getInvokerTrackInfo() throws ClassNotFoundException {
    String callbackPrefix = mInvokerTrackMarkClass.getName() + ".";
    for (CtMethod ctMethod : mInvokerTrackMarkClass.getDeclaredMethods()) {
      InvokerTrack invokerTrack = (InvokerTrack) ctMethod.getAnnotation(InvokerTrack.class);
      if (invokerTrack != null) {
        JavassistInvokerTrackInfo invokerTrackInfo = new JavassistInvokerTrackInfo();
        invokerTrackInfo.callback = callbackPrefix + ctMethod.getName() + "();";
        invokerTrackInfo.className = invokerTrack.className();
        invokerTrackInfo.methodName = invokerTrack.methodName();
        invokerTrackInfo.createFile();
        mInvokerTrackInfos.add(invokerTrackInfo);
        System.out.println(TAG + invokerTrackInfo.toString());
      }
    }
  }

  private void processApplication(Collection<DirectoryInput> directoryInputs) {
    directoryInputs.forEach(directoryInput -> {
      processDirectory(directoryInput.getFile().getAbsolutePath());
    });
  }

  private void processJar(Collection<JarInput> jarInputs) {
    jarInputs.forEach(jarInput -> {
      String directory = jarInput.getFile().getAbsolutePath();
      directory = directory.replace(Constants.LIBRARY_JAR_SUFFIX, Constants.LIBRARY_CLASS_SUFFIX);
      processDirectory(directory);
      Boolean modified = mDirectoryModified.get(directory);
      if (modified != null && modified) {
        try {
          ZipUtils.zipDir(jarInput.getFile().getAbsolutePath(), directory);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
  }

  private void processDirectory(String directory) {
    int beginIndex = directory.length() + 1;
    FileUtils.listFiles(new File(directory), new String[] {Constants.CLASS_SUFFIX}, true)
        .forEach(file -> {
          String classPath = file.getAbsolutePath();
          String className =
              classPath.substring(beginIndex, classPath.length() - CLASS_SUFFIX_LENGTH)
                  .replace(File.separatorChar, '.');
          System.out.println(TAG + className);
          processClass(className, directory);
        });
  }

  private void processClass(String className, String directory) {
    try {
      CtClass ctClass = CLASS_POOL.getCtClass(className);
      if (ctClass == mInvokerTrackMarkClass) {
        return;
      }
      if (ctClass.isFrozen()) {
        System.out.println(TAG + "frozen");
        ctClass.defrost();
      }
      for (CtMethod method : ctClass.getDeclaredMethods()) {
        modifyMethod(method, className);
      }
      Boolean modified = mClassModified.get(className);
      if (modified != null && modified) {
        ctClass.writeFile(directory);
        mDirectoryModified.put(directory, true);
      }
      ctClass.detach();
    } catch (NotFoundException | CannotCompileException | IOException e) {
      e.printStackTrace();
    }
  }

  private void modifyMethod(CtMethod ctMethod, String className) throws CannotCompileException {
    ctMethod.instrument(new ExprEditor() {
      @Override
      public void edit(MethodCall m) throws CannotCompileException {
        for (JavassistInvokerTrackInfo invokerTrackInfo : mInvokerTrackInfos) {
          if (m.getClassName().equals(invokerTrackInfo.className)
              && m.getMethodName().equals(invokerTrackInfo.methodName)) {
            try {
              FileUtils.writeStringToFile(invokerTrackInfo.logFile,
                  className + " : " + m.getLineNumber() + "\n", true);
            } catch (IOException e) {
              e.printStackTrace();
            }
            System.out.println(TAG + "modifyMethod:" + className + " : " + m.getLineNumber());
            mClassModified.put(className, true);
            try {
              // replace 方式和 insertAt 对于匿名内部类无效，采用 insertAt 方式生成的代码更为简洁
              // m.replace(code + "$_ = $proceed($$);");
              ctMethod.insertAt(m.getLineNumber(), invokerTrackInfo.callback);
            } catch (CannotCompileException e) {
              e.printStackTrace();
              ctMethod.insertBefore(invokerTrackInfo.callback);
            }
          }
        }
      }
    });
  }
}

package com.hhh.transform.asm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.JarInput;
import com.hhh.transform.Constants;
import com.hhh.transform.utils.ZipUtils;

public class ASMCodeInject {

  private static final String TAG = "ASMCodeInject:";

  private final Map<String, Boolean> mDirectoryModified = new HashMap<>();
  private final List<ASMInvokerTrackInfo> mInvokerTrackInfos = new ArrayList<>();

  public void injectCode(Collection<DirectoryInput> directoryInputs,
      Collection<JarInput> jarInputs) {
    if (directoryInputs != null && !directoryInputs.isEmpty()) {
      processAnnotation(directoryInputs);
      processApplication(directoryInputs);
    }
    if (jarInputs != null && !jarInputs.isEmpty()) {
      processJar(jarInputs);
    }
  }

  private void processAnnotation(Collection<DirectoryInput> directoryInputs) {
    directoryInputs.forEach(directoryInput -> {
      FileUtils.listFiles(directoryInput.getFile(), new String[] {Constants.CLASS_SUFFIX}, true)
          .forEach(file -> {
            if (!mInvokerTrackInfos.isEmpty()) {
              return;
            }
            try {
              FileInputStream fis = new FileInputStream(file);
              ClassReader classReader = new ClassReader(fis);
              ClassWriter classWriter = new ClassWriter(classReader, 0);
              ClassAnnotationVisitor classVisitor =
                  new ClassAnnotationVisitor(classWriter, mInvokerTrackInfos);
              classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);
              for (ASMInvokerTrackInfo invokerTrackInfo : mInvokerTrackInfos) {
                invokerTrackInfo.createFile();
              }
            } catch (IOException e) {
              e.printStackTrace();
            }
          });
    });
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
    FileUtils.listFiles(new File(directory), new String[] {Constants.CLASS_SUFFIX}, true)
        .forEach(file -> {
          System.out.println(TAG + file.getAbsolutePath());
          processClass(file, directory);
        });
  }

  private void processClass(File file, String directory) {
    try {
      FileInputStream fis = new FileInputStream(file);
      ClassReader classReader = new ClassReader(fis);
      // 不能设置为 ClassWriter.COMPUTE_FRAMES，否则会崩溃，异常为
      // Caused by: java.lang.ClassNotFoundException: android.text.Spanned
      ClassWriter classWriter = new ClassWriter(classReader, 0);
      ASMClassVisitor classVisitor = new ASMClassVisitor(classWriter, mInvokerTrackInfos);
      classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);
      if (classVisitor.isClassModified()) {
        mDirectoryModified.put(directory, true);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(classWriter.toByteArray());
        fos.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}

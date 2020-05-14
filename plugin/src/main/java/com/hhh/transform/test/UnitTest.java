package com.hhh.transform.test;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Status;
import com.hhh.transform.asm.ASMCodeInject;
import com.hhh.transform.javassist.JavassistCodeInject;

public class UnitTest {

  public static void main(String[] args) {
    System.out.println(UnitTest.class.getName());
    testASM();
    // testJavassist();
  }

  public static void testASM() {
    new ASMCodeInject().injectCode(getDirectoryInputs(), null);
  }

  public static void testJavassist() {
    new JavassistCodeInject().injectCode(getDirectoryInputs(), null, null);
  }

  private static Set<DirectoryInput> getDirectoryInputs() {
    Set<DirectoryInput> directoryInputs = new HashSet<>();
    directoryInputs.add(new DirectoryInput() {
      @Override
      public Map<File, Status> getChangedFiles() {
        return null;
      }

      @Override
      public String getName() {
        return null;
      }

      @Override
      public File getFile() {
        return new File("./app/build/intermediates/javac/debug/classes");
      }

      @Override
      public Set<ContentType> getContentTypes() {
        return null;
      }

      @Override
      public Set<? super Scope> getScopes() {
        return null;
      }
    });
    return directoryInputs;
  }
}

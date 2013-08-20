package com.revolsys.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class JavaProcess {

  public static Process exec(final List<String> javaArguments,
    final Class<?> klass) {
    return exec(javaArguments, klass, Collections.<String> emptyList());
  }

  public static Process exec(final List<String> javaArguments,
    final Class<?> klass, final List<String> programArguments) {
    final String javaHome = System.getProperty("java.home");
    final String javaBin = javaHome + File.separator + "bin" + File.separator
      + "java";
    final String classpath = System.getProperty("java.class.path");
    final String className = klass.getCanonicalName();

    final List<String> params = new ArrayList<String>();
    params.add(javaBin);
    params.add("-cp");
    params.add(classpath);
    if (javaArguments != null) {
      params.addAll(javaArguments);
    }
    params.add(className);
    if (programArguments != null) {
      params.addAll(programArguments);
    }
    final ProcessBuilder builder = new ProcessBuilder(params);

    try {
      return builder.start();
    } catch (final IOException e) {
      throw new RuntimeException("Unable to start " + params, e);
    }
  }

  public static Process exec(final List<String> javaArguments,
    final Class<?> klass, final String... programArguments) {
    return exec(javaArguments, klass, Arrays.asList(programArguments));
  }

  public static int execAndWait(final List<String> javaArguments,
    final Class<?> klass, final List<String> programArguments)
    throws InterruptedException {
    final Process process = exec(javaArguments, klass, programArguments);
    process.waitFor();
    return process.exitValue();
  }

  private JavaProcess() {
  }

}

package com.revolsys.spring;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Pattern;

import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import com.revolsys.io.FileUtil;
import com.revolsys.spring.config.AttributesBeanConfigurer;

public class SpringUtil {

  public static final Pattern KEY_PATTERN = Pattern.compile("(\\w[\\w\\d]*)(?:(?:\\[([\\w\\d]+)\\])|(?:\\.([\\w\\d]+)))?");

  private static final ThreadLocal<Resource> baseResource = new ThreadLocal<Resource>();

  public static void close(
    final ConfigurableApplicationContext applicationContext) {
    if (applicationContext != null) {
      if (applicationContext.isActive()) {
        applicationContext.close();
      }
    }
  }

  public static void copy(final Resource source, final Resource target) {
    final InputStream in = getInputStream(source);
    try {
      if (target instanceof FileSystemResource) {
        final FileSystemResource fileResource = (FileSystemResource)target;
        final File file = fileResource.getFile();
        final File parent = file.getParentFile();
        if (!parent.exists()) {
          parent.mkdirs();
        }
      }
      final OutputStream out = getOutputStream(target);
      try {
        FileUtil.copy(in, out);
      } finally {
        FileUtil.closeSilent(out);
      }
    } finally {
      FileUtil.closeSilent(in);
    }
  }

  public static GenericApplicationContext getApplicationContext(
    final ClassLoader classLoader, final Resource... resources) {
    final GenericApplicationContext applicationContext = new GenericApplicationContext();
    applicationContext.setClassLoader(classLoader);

    AnnotationConfigUtils.registerAnnotationConfigProcessors(
      applicationContext, null);
    final AttributesBeanConfigurer attributesConfig = new AttributesBeanConfigurer(
      applicationContext);
    applicationContext.addBeanFactoryPostProcessor(attributesConfig);

    final XmlBeanDefinitionReader beanReader = new XmlBeanDefinitionReader(
      applicationContext);
    beanReader.setBeanClassLoader(classLoader);
    beanReader.loadBeanDefinitions(resources);
    applicationContext.refresh();
    return applicationContext;
  }

  public static Resource getBaseResource() {
    final Resource baseResource = SpringUtil.baseResource.get();
    if (baseResource == null) {
      return new FileSystemResource(new File(""));
    } else {
      return baseResource;
    }
  }

  public static Resource getBaseResource(final String childPath) {
    final Resource baseResource = getBaseResource();
    return getResource(baseResource, childPath);
  }

  public static BufferedReader getBufferedReader(final Resource resource) {
    final Reader in = getReader(resource);
    return new BufferedReader(in);
  }

  public static String getContents(final Resource resource) {
    final InputStream in = getInputStream(resource);
    final Reader reader = new InputStreamReader(in);
    try {
      final Writer writer = new StringWriter();
      try {
        FileUtil.copy(reader, writer);
        return writer.toString();
      } finally {
        FileUtil.closeSilent(writer);
      }
    } finally {
      FileUtil.closeSilent(in);
    }
  }

  public static File getFile(final Resource resource) {
    try {
      return resource.getFile();
    } catch (final IOException e) {
      throw new IllegalArgumentException("Cannot get File for resource "
        + resource, e);
    }
  }

  public static File getOrDownloadFile(final Resource resource) {
    try {
      return resource.getFile();
    } catch (final IOException e) {
      if (resource.exists()) {
        String baseName = getBaseName(resource);
        String fileNameExtension = getFileNameExtension(resource);
        File file = FileUtil.createTempFile(baseName, fileNameExtension);
        FileUtil.copy(getInputStream(resource), file);
        return file;
      } else {
        throw new IllegalArgumentException("Cannot get File for resource "
          + resource, e);
      }
    }
  }

  public static File getFileOrCreateTempFile(final Resource resource) {
    try {
      if (resource instanceof FileSystemResource) {
        return resource.getFile();
      } else {
        final String filename = resource.getFilename();
        final String baseName = FileUtil.getBaseName(filename);
        final String fileExtension = FileUtil.getFileNameExtension(filename);
        return File.createTempFile(baseName, fileExtension);
      }
    } catch (final IOException e) {
      throw new RuntimeException("Unable to get file for " + resource, e);
    }
  }

  public static OutputStream getFileOutputStream(final Resource resource)
    throws IOException, FileNotFoundException {
    final File file = resource.getFile();
    return new FileOutputStream(file);
  }

  public static InputStream getInputStream(final Resource resource) {
    try {
      return resource.getInputStream();
    } catch (final IOException e) {
      throw new RuntimeException("Unable to open stream to resource "
        + resource, e);
    }
  }

  public static OutputStream getOutputStream(final Resource resource) {
    try {
      if (resource instanceof OutputStreamResource) {
        final OutputStreamResource outputStreamResource = (OutputStreamResource)resource;
        return outputStreamResource.getOutputStream();
      } else if (resource instanceof FileSystemResource) {
        return getFileOutputStream(resource);
      } else {
        final URL url = resource.getURL();
        final String protocol = url.getProtocol();
        if (protocol.equals("file")) {
          return getFileOutputStream(resource);
        } else {
          final URLConnection connection = url.openConnection();
          connection.setDoOutput(true);
          return connection.getOutputStream();
        }
      }
    } catch (final IOException e) {
      throw new RuntimeException("Unable to open stream for " + resource, e);
    }
  }

  public static PrintWriter getPrintWriter(final Resource resource) {
    final Writer writer = getWriter(resource);
    return new PrintWriter(writer);
  }

  public static Reader getReader(final Resource resource) {
    final InputStream in = getInputStream(resource);
    return new InputStreamReader(in);
  }

  public static Resource getResource(final Resource resource,
    final CharSequence childPath) {
    try {
      if (resource instanceof FileSystemResource) {
        final FileSystemResource fileResource = (FileSystemResource)resource;
        final File file = fileResource.getFile();
        final File childFile = new File(file, childPath.toString());
        return new FileSystemResource(childFile);
      } else {
        return resource.createRelative(childPath.toString());
      }
    } catch (final IOException e) {
      throw new IllegalArgumentException("Cannot create resource " + resource
        + childPath, e);
    }
  }

  public static Resource getResource(final String url) {
    try {
      return new UrlResource(url);
    } catch (final MalformedURLException e) {
      throw new RuntimeException("URL not valid " + url, e);
    }
  }

  public static Resource getResourceWithExtension(final Resource resource,
    final String extension) {
    final String baseName = getBaseName(resource);
    final String newFileName = baseName + "." + extension;
    try {
      return resource.createRelative(newFileName);
    } catch (final IOException e) {
      throw new RuntimeException("Unable to get resource " + newFileName, e);
    }

  }

  public static String getFileNameExtension(final Resource resource) {
    return FileUtil.getFileNameExtension(resource.getFilename());
  }

  public static String getBaseName(final Resource resource) {
    return FileUtil.getBaseName(resource.getFilename());
  }

  public static String getString(final Resource resource) {
    final Reader reader = getReader(resource);
    return FileUtil.getString(reader);
  }

  public static URL getUrl(final Resource resource) {
    try {
      return resource.getURL();
    } catch (final IOException e) {
      throw new IllegalArgumentException("Cannot get URL for resource "
        + resource, e);
    }
  }

  public static Resource getUrlResource(final String url) {
    try {
      return new UrlResource(url);
    } catch (final MalformedURLException e) {
      throw new IllegalArgumentException("Not a valid URL " + url, e);
    }
  }

  public static Writer getWriter(final Resource resource) {
    final OutputStream stream = getOutputStream(resource);
    return new OutputStreamWriter(stream);
  }

  public static Resource setBaseResource(final Resource baseResource) {
    final Resource oldResource = SpringUtil.baseResource.get();
    SpringUtil.baseResource.set(baseResource);
    return oldResource;
  }

}

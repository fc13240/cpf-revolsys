/*
 * Copyright 2004-2005 Revolution Systems Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.io.xml;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.converters.IntegerConverter;
import org.apache.commons.beanutils.converters.ShortConverter;
import org.springframework.core.io.Resource;

import com.revolsys.beans.EnumConverter;
import com.revolsys.util.CaseConverter;

/**
 * <p>
 * The XmlProcessor class provides a framework for processing an XML Document to
 * create a Java Object representation of the document or to perform other
 * actions on the contents of the document. The XmlProcessor uses the STAX API
 * to read the XML document using the {@link XMLStreamReader}, by using the
 * streaming API the XML parser will not load the XML document into memory
 * (although the Java Object representation created by subclasses of
 * XmlProcessor would likely be in memory at all times).
 * </p>
 * <p>
 * Users of a XmlProcessor implementation construct a new instance of the
 * subclass and invoke the {@link #process(XMLStreamReader)} method to process
 * the document and return the result object generated by the processor.
 * </p>
 * <p>
 * To process a document using the XmlProcessor a subclass of XmlProcessor must
 * be created for each XML namespace that needs to be processed. For each XML
 * element defined for the XML namespace a process method must be defined in the
 * subclass. The name of the process method must have the "process" prefix
 * followed bfollowed by the XML element name, have only an
 * {@link XMLStreamReader} parameter and return an Object (any subclass of
 * Object can be returned).
 * </p>
 * <p>
 * For example the process method for the XML element BankAccount would have the
 * following signature.
 * </p>
 * 
 * <pre>
 * public BankAccount processBankAccount(XMLStreamReader parser);
 * </pre>
 * <p>
 * For example the process method for the XML element firstName would have the
 * following signature. <b>Note that the part of the method name for the XML
 * element must have the same case as the XML element name.
 * </p>
 * 
 * <pre>
 * public String processfirstName(XMLStreamReader parser);
 * </pre>
 * <p>
 * The process methods read the attributes from the element and can either
 * create an Object for the XML element or perform other processing on the
 * element. This object can be returned from the process method so that the
 * calling method can access the object. If an XML element has child elements it
 * can either ignore them using the
 * {@link StaxUtils#skipSubTree(XMLStreamReader)} method or process the child
 * element using the {@link #process(XMLStreamReader)} method that will invoke
 * the appropriate process method for that element.
 * </p>
 * <p>
 * The following example shows the implementation of a process method for a
 * Person element that has a firstName and lastName element. As no children are
 * expected the {@link StaxUtils#skipSubTree(XMLStreamReader)} method is used to
 * skip to the end of the element.
 * </p>
 * 
 * <pre>
 * public Person processPerson(final XMLStreamReader parser)
 *   throws XMLStreamException, IOException {
 *   String firstName = parser.getAttributeValue(null, &quot;firstName&quot;);
 *   String lastName = parser.getAttributeValue(null, &quot;lastName&quot;);
 *   Person person = new Person(firstName, lastName);
 *   StaxUtils.skipSubTree(parser);
 *   return person;
 * }
 * </pre>
 * <p>
 * The following example shows the implementation of a process method for a
 * Family element that has one or more Person or Pet child elements. If an
 * unexpected element occurs and error will be recorded.
 * </p>
 * 
 * <pre>
 * public Family processFamily(final XMLStreamReader parser)
 *   throws XMLStreamException, IOException {
 *   Family family = new Family();
 *   while (parser.nextTag() == XMLStreamReader.START_ELEMENT) {
 *     Object object = process(parser);
 *     if (object instanceof Person) {
 *       config.addPerson((Person)object);
 *     } else if (object instanceof Pet) {
 *       config.addPet((Pet)object);
 *     } else {
 *       context.addError(&quot;Unexpected Element:&quot; + object, null,
 *         parser.getLocation());
 *     }
 *   }
 *   return family;
 * }
 * </pre>
 * 
 * @author Paul Austin
 */
public abstract class XmlProcessor {
  static {
    registerEnumConverter(Enum.class);
  }

  /** The arguments a processor method must have. */
  private static final Class<?>[] PROCESS_METHOD_ARGS = new Class[] {
    XMLStreamReader.class
  };

  /** The cache of processor classes to method caches. */
  private static final Map<Class<?>, Map<String, Method>> PROCESSOR_METHOD_CACHE = new HashMap<Class<?>, Map<String, Method>>();

  /**
   * Create the cache of process methods from the specified class.
   * 
   * @param processorClass The XmlPorcessor class.
   * @return The map of method names to process methods.
   */
  private static Map<String, Method> getMethodCache(
    final Class<?> processorClass) {
    Map<String, Method> methodCache = PROCESSOR_METHOD_CACHE.get(processorClass);
    if (methodCache == null) {
      methodCache = new HashMap<String, Method>();
      PROCESSOR_METHOD_CACHE.put(processorClass, methodCache);
      final Method[] methods = processorClass.getMethods();
      for (int i = 0; i < methods.length; i++) {
        final Method method = methods[i];
        final String methodName = method.getName();
        if (methodName.startsWith("process")) {
          if (Arrays.equals(method.getParameterTypes(), PROCESS_METHOD_ARGS)) {
            final String name = methodName.substring(7);
            methodCache.put(name, method);
          }
        }
      }
    }
    return methodCache;
  }

  public static void registerEnumConverter(final Class<? extends Enum> enumClass) {
    final BeanUtilsBean beanUtilsBean = BeanUtilsBean.getInstance();
    final ConvertUtilsBean convertUtils = beanUtilsBean.getConvertUtils();
    final EnumConverter enumConverter = new EnumConverter();
    convertUtils.register(enumConverter, enumClass);
  }

  /** The context for processing of the XML Document. */
  private XmlProcessorContext context = new SimpleXmlProcessorContext();

  /** The cache of XML element names to processor methods. */
  private final Map<String, Method> methodCache;

  /** The XML namespace URI processed by this processor. */
  private final String namespaceUri;

  private Map<String, Class<?>> tagNameClassMap = new HashMap<String, Class<?>>();

  private final Map<QName, Converter> typePathConverterMap = new HashMap<QName, Converter>();

  /**
   * Construct a new XmlProcessor for the XML Namespace URI.
   * 
   * @param namespaceUri The XML Namespace URI.
   */
  protected XmlProcessor(final String namespaceUri) {
    this.namespaceUri = namespaceUri;
    typePathConverterMap.put(XmlConstants.XS_SHORT, new ShortConverter());
    typePathConverterMap.put(XmlConstants.XS_INT, new IntegerConverter());
    methodCache = getMethodCache(getClass());
  }

  public XmlProcessor(final String namespaceUri,
    final Map<String, Class<?>> tagNameClassMap) {
    this(namespaceUri);
    this.tagNameClassMap = tagNameClassMap;
  }

  /**
   * Get the context for processing the XML Document.
   * 
   * @return The context for processing the XML Document.
   */
  public final XmlProcessorContext getContext() {
    return context;
  }

  public String getNamespaceUri() {
    return namespaceUri;
  }

  /**
   * Get the method to process the XML element.
   * 
   * @param element The element to process.
   * @return The method to process the XML element.
   */
  private Method getProcessMethod(final QName element) {
    final String elementName = element.getLocalPart();
    final Method method = methodCache.get(elementName);
    return method;
  }

  @SuppressWarnings("unchecked")
  public <T> T parseObject(final XMLStreamReader parser,
    final Class<? extends T> objectClass) throws XMLStreamException,
    IOException {
    try {
      if (objectClass == null) {
        Object object = null;
        while (parser.nextTag() == XMLStreamConstants.START_ELEMENT) {
          if (object != null) {
            throw new IllegalArgumentException(
              "Expecting a single child element " + parser.getLocation());
          }
          object = process(parser);
        }
        return (T)object;
      } else {
        final T object = objectClass.newInstance();
        if (object instanceof Collection) {
          final Collection<Object> collection = (Collection<Object>)object;
          while (parser.nextTag() == XMLStreamConstants.START_ELEMENT) {
            final Object value = process(parser);
            collection.add(value);
          }
        } else {
          while (parser.nextTag() == XMLStreamConstants.START_ELEMENT) {
            final String tagName = parser.getName().getLocalPart();
            final Object value = process(parser);
            try {
              String propertyName;
              if (tagName.length() > 1
                && Character.isLowerCase(tagName.charAt(1))) {
                propertyName = CaseConverter.toLowerFirstChar(tagName);
              } else {
                propertyName = tagName;
              }
              BeanUtils.setProperty(object, propertyName, value);
            } catch (final Throwable e) {
              e.printStackTrace();
            }
          }
        }
        return object;
      }
    } catch (final InstantiationException e) {
      throw new IllegalArgumentException(e);
    } catch (final IllegalAccessException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @SuppressWarnings("unchecked")
  public <T> T process(final Resource resource) {
    try {
      final XMLStreamReader xmlReader = StaxUtils.createXmlReader(resource);
      StaxUtils.skipToStartElement(xmlReader);
      return (T)process(xmlReader);
    } catch (final RuntimeException e) {
      throw e;
    } catch (final Error e) {
      throw e;
    } catch (final Exception e) {
      throw new RuntimeException("Unable to parse: " + resource, e);
    }
  }

  @SuppressWarnings("unchecked")
  public <T> T process(final String xml) {
    try {
      final StringReader reader = new StringReader(xml);
      final XMLStreamReader xmlReader = StaxUtils.createXmlReader(reader);
      StaxUtils.skipToStartElement(xmlReader);
      return (T)process(xmlReader);
    } catch (final RuntimeException e) {
      throw e;
    } catch (final Error e) {
      throw e;
    } catch (final Exception e) {
      throw new RuntimeException("Unable to parse: " + xml, e);
    }
  }

  /**
   * <p>
   * The process method is used to return an object representation of the
   * current XML element and subtree from the {@link XMLStreamReader}. The
   * method finds the process method in the subclass that has the method name
   * with the prefix "process" followed by the XML element name, have only an
   * {@link XMLStreamReader} parameter and return an Object (any subclass of
   * Object can be returned).
   * </p>
   * <p>
   * For example the process method for the XML element BankAccount would have
   * the following signature.
   * </p>
   * 
   * <pre>
   * public BankAccount processBankAccount(XMLStreamReader parser);
   * </pre>
   * <p>
   * For example the process method for the XML element firstName would have the
   * following signature. <b>Note that the part of the method name for the XML
   * element must have the same case as the XML element name.
   * </p>
   * 
   * <pre>
   * public String processfirstName(XMLStreamReader parser);
   * </pre>
   * 
   * @param parser The STAX XML parser.
   * @return The object representation of the XML subtree.
   * @throws IOException If an I/O exception occurs.
   * @throws XMLStreamException If an exception processing the XML occurs.
   */
  @SuppressWarnings("unchecked")
  public <T> T process(final XMLStreamReader parser) throws XMLStreamException,
    IOException {
    final QName element = parser.getName();

    final String tagName = element.getLocalPart();
    final QName xsiName = StaxUtils.getQNameAttribute(parser, XsiConstants.TYPE);
    boolean hasMapping = false;
    Class<?> objectClass = null;
    if (xsiName == null) {
      objectClass = tagNameClassMap.get(tagName);
      if (tagNameClassMap.containsKey(tagName)) {
        objectClass = tagNameClassMap.get(tagName);
        hasMapping = true;
      }
    } else {
      final String xsiLocalName = xsiName.getLocalPart();
      final Converter converter = typePathConverterMap.get(xsiName);
      if (converter != null) {
        final String text = StaxUtils.getElementText(parser);
        return (T)converter.convert(null, text);
      } else if (tagNameClassMap.containsKey(xsiLocalName)) {
        objectClass = tagNameClassMap.get(xsiLocalName);
        hasMapping = true;
      } else if (tagNameClassMap.containsKey(tagName)) {
        objectClass = tagNameClassMap.get(tagName);
        hasMapping = true;
      }
    }
    if (hasMapping) {
      return (T)parseObject(parser, objectClass);
    } else {
      try {
        final Method method = getProcessMethod(element);
        if (method == null) {
          return (T)StaxUtils.getElementText(parser);
        } else {
          return (T)method.invoke(this, new Object[] {
            parser
          });
        }
      } catch (final IllegalAccessException e) {
        throw new RuntimeException(e);
      } catch (final InvocationTargetException e) {
        final Throwable t = e.getTargetException();
        if (t instanceof RuntimeException) {
          throw (RuntimeException)t;
        } else if (t instanceof Error) {
          throw (Error)t;
        } else if (t instanceof XMLStreamException) {
          throw (XMLStreamException)t;
        } else if (t instanceof IOException) {
          throw (IOException)t;
        } else {
          throw new RuntimeException(t.getMessage(), t);
        }
      }
    }
  }

  /**
   * Set the context for processing the XML Document.
   * 
   * @param context The context for processing the XML Document.
   */
  public final void setContext(final XmlProcessorContext context) {
    this.context = context;
  }
}

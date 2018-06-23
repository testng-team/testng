package org.testng.xml.dom;

import org.testng.collections.ListMultiMap;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.internal.collections.Pair;
import org.testng.log4testng.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

// TODO: This class is perhaps not being used anywhere in TestNG. Need to check and remove this if
// its obsolete.
public class XDom {
  private Document m_document;
  private ITagFactory m_tagFactory;
  private static final Logger LOGGER = Logger.getLogger(XDom.class);

  public XDom(ITagFactory tagFactory, Document document) {
    m_tagFactory = tagFactory;
    m_document = document;
  }

  public Object parse()
      throws InstantiationException, IllegalAccessException, SecurityException,
          IllegalArgumentException, InvocationTargetException {
    Object result = null;
    NodeList nodes = m_document.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      Node item = nodes.item(i);
      if (item.getAttributes() != null) {
        String nodeName = item.getNodeName();

        System.out.println("Node name:" + nodeName);
        Class<?> c = m_tagFactory.getClassForTag(nodeName);
        if (c == null) {
          throw new RuntimeException("No class found for tag " + nodeName);
        }

        result = c.newInstance();
        populateAttributes(item, result);
        if (ITagSetter.class.isAssignableFrom(result.getClass())) {
          throw new RuntimeException("TAG SETTER");
        }
        populateChildren(item, result);
      }
    }
    return result;
  }

  public void populateChildren(Node root, Object result)
      throws InstantiationException, IllegalAccessException, SecurityException,
          IllegalArgumentException, InvocationTargetException {
    p("populateChildren: " + root.getLocalName());
    NodeList childNodes = root.getChildNodes();
    ListMultiMap<String, Object> children = Maps.newListMultiMap();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node item = childNodes.item(i);
      if (item.getAttributes() != null) {
        String nodeName = item.getNodeName();
        if ("suite-files".equals(nodeName)) {
          System.out.println("BREAK");
        }

        Class<?> c = m_tagFactory.getClassForTag(nodeName);
        if (c == null) {
          System.out.println("Warning: No class found for tag " + nodeName);
          boolean foundSetter = invokeOnSetter(result, (Element) item, nodeName, null);
          System.out.println("  found setter:" + foundSetter);
        } else {
          Object object = instantiateElement(c, result);
          if (ITagSetter.class.isAssignableFrom(object.getClass())) {
            System.out.println("Tag setter:" + result);
            ((ITagSetter) object).setProperty(nodeName, result, item);
          } else {
            children.put(nodeName, object);
            populateAttributes(item, object);
            populateContent(item, object);
          }
          populateChildren(item, object);
        }
      }
    }
  }

  /**
   * Try to find a @ParentSetter. If this fails, try to find a constructor that takes the parent as
   * a parameter. If this fails, use the default constructor.
   */
  private Object instantiateElement(Class<?> c, Object parent)
      throws SecurityException, IllegalArgumentException, InstantiationException,
          IllegalAccessException, InvocationTargetException {
    Object result;
    Method m = findMethodAnnotatedWith(c, ParentSetter.class);
    if (m != null) {
      result = c.newInstance();
      m.invoke(result, parent);
    } else {
      try {
        result = c.getConstructor(parent.getClass()).newInstance(parent);
      } catch (NoSuchMethodException ex) {
        result = c.newInstance();
      }
    }

    return result;
  }

  private Method findMethodAnnotatedWith(Class<?> c, Class<? extends Annotation> annotation) {
    for (Method m : c.getMethods()) {
      if (m.getAnnotation(annotation) != null) {
        return m;
      }
    }
    return null;
  }

  private void populateContent(Node item, Object object) {
    for (int i = 0; i < item.getChildNodes().getLength(); i++) {
      Node child = item.getChildNodes().item(i);
      if (child instanceof Text) {
        setText(object, (Text) child);
      }
    }
  }

  private void setText(Object bean, Text child) {
    List<Pair<Method, Wrapper>> pairs =
        Reflect.findMethodsWithAnnotation(bean.getClass(), TagContent.class, bean);
    for (Pair<Method, Wrapper> pair : pairs) {
      try {
        pair.first().invoke(bean, child.getTextContent());
      } catch (IllegalArgumentException
          | InvocationTargetException
          | IllegalAccessException
          | DOMException e) {
        LOGGER.error(e.getMessage(), e);
      }
    }
  }

  private boolean invokeOnSetter(Object object, Element element, String nodeName, Object bean) {
    Pair<Method, Wrapper> pair = Reflect.findSetterForTag(object.getClass(), nodeName, bean);

    List<Object[]> allParameters;
    if (pair != null) {
      Method m = pair.first();
      try {
        if (pair.second() != null) {
          allParameters = pair.second().getParameters(element);
        } else {
          allParameters = Lists.newArrayList();
          allParameters.add(new Object[] {bean});
        }

        for (Object[] p : allParameters) {
          m.invoke(object, p);
        }
        return true;
      } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
        LOGGER.error(e.getMessage(), e);
      }
    }

    return false;
  }

  private void populateAttributes(Node node, Object object) {
    for (int j = 0; j < node.getAttributes().getLength(); j++) {
      Node item = node.getAttributes().item(j);
      setProperty(object, item.getLocalName(), item.getNodeValue());
    }
  }

  private void setProperty(Object object, String name, Object value) {
    Pair<Method, Wrapper> setter = Reflect.findSetterForTag(object.getClass(), name, value);

    if (setter != null) {
      Method foundMethod = setter.first();
      try {
        Class<?> type = foundMethod.getParameterTypes()[0];
        if (type == Boolean.class || type == boolean.class) {
          foundMethod.invoke(object, Boolean.parseBoolean(value.toString()));
        } else if (type == Integer.class || type == int.class) {
          foundMethod.invoke(object, Integer.parseInt(value.toString()));
        } else {
          foundMethod.invoke(object, value);
        }
      } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
        LOGGER.error(e.getMessage(), e);
      }
    } else {
      e("Couldn't find setter method for property" + name + " on " + object.getClass());
    }
  }

  private void p(String string) {
    System.out.println("[XDom] " + string);
  }

  private void e(String string) {
    System.out.println("[XDom] [Error] " + string);
  }
}

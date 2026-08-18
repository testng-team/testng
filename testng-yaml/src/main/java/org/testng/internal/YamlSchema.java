package org.testng.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.testng.TestNGException;
import org.testng.log4testng.Logger;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlPackage;
import org.testng.xml.XmlScript;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertySubstitute;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;

/**
 * The schema of a YAML suite file: every key the reader accepts, and where its value goes.
 *
 * <p>Declared rather than derived. Left to itself snakeyaml binds a document onto {@link XmlSuite}
 * by JavaBean introspection, which turns the accessor surface of the model into an undocumented
 * public format: adding a setter adds a key, renaming one removes a key, and nothing anywhere says
 * so. That is how {@code fileName}, {@code parsed} and {@code parentSuite} became suite keys, how
 * {@code index} and {@code suite} became test keys, and how {@code name} on a method selector came
 * to mean {@code className}. Anything not in the tables below is now rejected.
 *
 * <p>A key names its target with a method reference rather than with the name of a setter, so a
 * wrong one does not compile. A string would only fail at parse time, and only on a file that
 * happens to use that key.
 *
 * <p>What a suite file can carry and this schema cannot express is listed on {@link
 * Yaml#toYaml(XmlSuite)}, which writes exactly the keys declared here so that {@code parse ->
 * toYaml -> parse} is lossless.
 */
final class YamlSchema {

  private static final Logger LOGGER = Logger.getLogger(YamlSchema.class);

  private YamlSchema() {}

  /**
   * @param loadClasses whether a {@code <class>} entry should be resolved against the classpath as
   *     it is read.
   * @return a snakeyaml constructor that accepts the keys declared here and nothing else.
   */
  static Constructor constructor(boolean loadClasses) {
    LoaderOptions options = new LoaderOptions();
    // snakeyaml keeps the last occurrence by default, so a suite file declaring "tests" twice
    // silently loses one of them. A key that cannot be repeated is part of the contract too.
    options.setAllowDuplicateKeys(false);
    Constructor constructor = new Constructor(XmlSuite.class, options);
    constructor.addTypeDescription(suite());
    constructor.addTypeDescription(test());
    constructor.addTypeDescription(xmlClass(loadClasses));
    constructor.addTypeDescription(xmlPackage());
    constructor.addTypeDescription(include());
    constructor.addTypeDescription(methodSelector());
    return constructor;
  }

  static SchemaType<XmlSuite> suite() {
    return new SchemaType<>(XmlSuite.class, "suite")
        .key("name", String.class, XmlSuite::setName)
        .key("verbose", Integer.class, XmlSuite::setVerbose)
        // Declared as text rather than as the enum, because ParallelMode has an accepted spelling
        // per value plus the deprecated "true"/"false", none of which is an enum constant.
        .key(
            "parallel",
            String.class,
            (XmlSuite suite, String value) ->
                suite.setParallel(XmlSuite.ParallelMode.getValidParallel(value)))
        .key("threadCount", Integer.class, XmlSuite::setThreadCount)
        .key("dataProviderThreadCount", Integer.class, XmlSuite::setDataProviderThreadCount)
        .key("timeOut", String.class, XmlSuite::setTimeOut)
        .key(
            "configFailurePolicy",
            String.class,
            (XmlSuite suite, String value) -> {
              // An unrecognised value keeps the default, as TestNGContentHandler does for XML
              // and as the sibling parallel key does for its own enum.
              XmlSuite.FailurePolicy policy = XmlSuite.FailurePolicy.getValidPolicy(value);
              if (policy != null) {
                suite.setConfigFailurePolicy(policy);
              }
            })
        .key("skipFailedInvocationCounts", Boolean.class, XmlSuite::setSkipFailedInvocationCounts)
        .key("preserveOrder", Boolean.class, XmlSuite::setPreserveOrder)
        .key("groupByInstances", Boolean.class, XmlSuite::setGroupByInstances)
        .key("allowReturnValues", Boolean.class, XmlSuite::setAllowReturnValues)
        .key(
            "shareThreadPoolForDataProviders",
            Boolean.class,
            XmlSuite::setShareThreadPoolForDataProviders)
        .key("lazyFactory", Boolean.class, XmlSuite::setLazyFactory)
        .key("parentModule", String.class, XmlSuite::setParentModule)
        .key("guiceStage", String.class, XmlSuite::setGuiceStage)
        .mapKey("parameters", YamlSchema::asText, XmlSuite::setParameters)
        .listKey("listeners", String.class, XmlSuite::setListeners)
        .listKey("includedGroups", String.class, XmlSuite::setIncludedGroups)
        .listKey("excludedGroups", String.class, XmlSuite::setExcludedGroups)
        .listKey("packages", XmlPackage.class, XmlSuite::setXmlPackages)
        .alias("xmlPackages", "packages")
        // Spelled out rather than passed as a method reference: setMethodSelectors is overloaded,
        // and which overload a bean property binds to was left to java.beans.Introspector.
        .listKey(
            "methodSelectors",
            org.testng.xml.XmlMethodSelector.class,
            (XmlSuite suite, List<org.testng.xml.XmlMethodSelector> selectors) ->
                suite.setMethodSelectors(selectors))
        .listKey("suiteFiles", String.class, XmlSuite::setSuiteFiles)
        .listKey("tests", XmlTest.class, XmlSuite::setTests);
  }

  static SchemaType<XmlTest> test() {
    return new SchemaType<>(XmlTest.class, "test")
        .key("name", String.class, XmlTest::setName)
        .key("verbose", Integer.class, XmlTest::setVerbose)
        .key(
            "parallel",
            String.class,
            (XmlTest test, String value) ->
                test.setParallel(XmlSuite.ParallelMode.getValidParallel(value)))
        .key("threadCount", Integer.class, XmlTest::setThreadCount)
        .key("timeOut", Long.class, XmlTest::setTimeOut)
        .key("preserveOrder", Boolean.class, XmlTest::setPreserveOrder)
        .key("groupByInstances", Boolean.class, XmlTest::setGroupByInstances)
        .key("allowReturnValues", Boolean.class, XmlTest::setAllowReturnValues)
        .key("skipFailedInvocationCounts", Boolean.class, XmlTest::setSkipFailedInvocationCounts)
        .mapKey("parameters", YamlSchema::asText, XmlTest::setParameters)
        .listKey("includedGroups", String.class, XmlTest::setIncludedGroups)
        .listKey("excludedGroups", String.class, XmlTest::setExcludedGroups)
        .mapKey("metaGroups", YamlSchema::asTextLists, XmlTest::setMetaGroups)
        .mapKey("dependencyGroups", YamlSchema::asText, XmlTest::setXmlDependencyGroups)
        .alias("xmlDependencyGroups", "dependencyGroups")
        .listKey(
            "methodSelectors",
            org.testng.xml.XmlMethodSelector.class,
            (XmlTest test, List<org.testng.xml.XmlMethodSelector> selectors) ->
                test.setMethodSelectors(selectors))
        .listKey("packages", XmlPackage.class, XmlTest::setXmlPackages)
        .alias("xmlPackages", "packages")
        .listKey("classes", XmlClass.class, XmlTest::setXmlClasses)
        .alias("xmlClasses", "classes");
  }

  static SchemaType<XmlPackage> xmlPackage() {
    return new SchemaType<>(XmlPackage.class, "package")
        .key("name", String.class, XmlPackage::setName)
        .listKey("include", String.class, XmlPackage::setInclude)
        .listKey("exclude", String.class, XmlPackage::setExclude);
  }

  static SchemaType<XmlInclude> include() {
    return new SchemaType<>(XmlInclude.class, "include")
        .key("name", String.class, XmlInclude::setName)
        .key("description", String.class, XmlInclude::setDescription)
        .mapKey("parameters", YamlSchema::asText, XmlInclude::setParameters);
  }

  /**
   * A method selector is written flat although the model nests the expression and the language in
   * an {@link XmlScript}, because that is the shape every fixture and the writer already use.
   */
  static SchemaType<org.testng.xml.XmlMethodSelector> methodSelector() {
    return new SchemaType<>(org.testng.xml.XmlMethodSelector.class, "method selector")
        .key("className", String.class, org.testng.xml.XmlMethodSelector::setClassName)
        .alias("name", "className")
        .key("priority", Integer.class, org.testng.xml.XmlMethodSelector::setPriority)
        .key(
            "expression",
            String.class,
            (org.testng.xml.XmlMethodSelector selector, String value) ->
                script(selector).setExpression(value))
        .key(
            "language",
            String.class,
            (org.testng.xml.XmlMethodSelector selector, String value) ->
                script(selector).setLanguage(value));
  }

  static SchemaType<XmlClass> xmlClass(boolean loadClasses) {
    return new XmlClassType(loadClasses);
  }

  static String deprecationMessage(String element, String deprecated, String canonical) {
    return "The YAML key \""
        + deprecated
        + "\" of a <"
        + element
        + "> is deprecated, use \""
        + canonical
        + "\" instead.";
  }

  static String repeatedKeyMessage(String element, String canonical, String first, String second) {
    return "The key \""
        + canonical
        + "\" of a <"
        + element
        + "> is declared twice, as \""
        + first
        + "\" and as \""
        + second
        + "\". Keep one of the two spellings.";
  }

  static String unknownKeyMessage(
      String element, String unknown, Set<String> accepted, Map<String, String> aliases) {
    return "Unknown key \""
        + unknown
        + "\" in a <"
        + element
        + ">. Accepted keys: "
        + String.join(", ", accepted)
        + (aliases.isEmpty() ? "" : ", or the deprecated " + String.join(", ", aliases.keySet()))
        + ".";
  }

  private static XmlScript script(org.testng.xml.XmlMethodSelector selector) {
    XmlScript script = selector.getScript();
    if (script == null) {
      script = new XmlScript();
      selector.setScript(script);
    }
    return script;
  }

  /**
   * Values are forced to text, because YAML resolves an unquoted {@code 44.0} or {@code true} to a
   * {@link Double} or a {@link Boolean} and the model stores parameters as {@code Map<String,
   * String>}. Without this the erased setter accepts them and iterating the map as strings throws
   * later, far from the file that caused it. A parameter is text in {@code testng.xml} too.
   */
  private static Map<String, String> asText(Map<?, ?> values) {
    Map<String, String> result = new LinkedHashMap<>();
    for (Map.Entry<?, ?> entry : values.entrySet()) {
      Object value = entry.getValue();
      result.put(String.valueOf(entry.getKey()), value == null ? null : String.valueOf(value));
    }
    return result;
  }

  private static Map<String, List<String>> asTextLists(Map<?, ?> values) {
    Map<String, List<String>> result = new LinkedHashMap<>();
    for (Map.Entry<?, ?> entry : values.entrySet()) {
      Object value = entry.getValue();
      if (!(value instanceof Collection)) {
        // Named, because the class cast that would happen otherwise reports the types and not the
        // entry, and a suite file can hold a lot of meta groups.
        throw new TestNGException(
            "The meta group \"" + entry.getKey() + "\" must list the groups it stands for");
      }
      List<String> group = new ArrayList<>();
      for (Object element : (Collection<?>) value) {
        group.add(String.valueOf(element));
      }
      result.put(String.valueOf(entry.getKey()), group);
    }
    return result;
  }

  /**
   * A declared set of keys for one model type. Overriding {@link #getProperty(String)} is what
   * makes the set closed: snakeyaml asks the type description first and only falls back to bean
   * introspection when no description is registered.
   */
  static class SchemaType<T> extends TypeDescription {

    private final String element;
    private final Map<String, SchemaProperty> keys = new LinkedHashMap<>();
    private final Map<String, String> deprecatedAliases = new LinkedHashMap<>();
    /**
     * Which spelling each bean was written through, so that the second one can be reported. Keyed
     * by identity and never cleared, which costs nothing: a description is built per parse and dies
     * with it, and every bean in it is reachable from the suite anyway.
     */
    private final Map<Object, Map<String, String>> spellingByBean = new IdentityHashMap<>();

    SchemaType(Class<T> type, String element) {
      super(type);
      this.element = element;
    }

    /** A key holding a single value. */
    <V> SchemaType<T> key(String name, Class<V> type, BiConsumer<T, V> setter) {
      return add(new SchemaProperty(name, type, cast(setter)));
    }

    /** A key holding a sequence of {@code elementType}. */
    <E> SchemaType<T> listKey(String name, Class<E> elementType, BiConsumer<T, List<E>> setter) {
      return add(new SchemaProperty(name, List.class, cast(setter), elementType));
    }

    /**
     * A key holding a mapping, converted before it reaches the model. YAML resolves the values of a
     * mapping on its own, so what arrives is never the {@code Map<String, ?>} the setter wants.
     */
    <V> SchemaType<T> mapKey(
        String name, Function<Map<?, ?>, V> conversion, BiConsumer<T, V> setter) {
      return add(
          new SchemaProperty(
              name,
              Map.class,
              (target, value) ->
                  setter.accept(uncheckedCast(target), conversion.apply((Map<?, ?>) value))));
    }

    /** An accepted spelling of {@code canonical} that warns when it is used. */
    SchemaType<T> alias(String deprecated, String canonical) {
      if (!keys.containsKey(canonical)) {
        throw new IllegalArgumentException("No such key to alias: " + canonical);
      }
      deprecatedAliases.put(deprecated, canonical);
      return this;
    }

    /**
     * A deprecated spelling and its canonical key write through the same property, so a mapping
     * carrying both declares one key twice however different the text looks -- and the second
     * silently overwrote the first. {@code LoaderOptions.setAllowDuplicateKeys(false)} does not
     * catch it, because snakeyaml compares the text of the keys.
     *
     * <p>Checked here rather than against the mapping node, because this is the first hook that
     * runs <em>after</em> snakeyaml has flattened the merge keys: {@code <<: *base} next to the
     * other spelling of one of the merged keys is the same collision, and a check on the raw node
     * cannot see it. Reporting from here also lets the failure carry the line and column of the
     * offending key rather than of the start of the document.
     *
     * <p>The same spelling repeated is left alone on purpose: that one snakeyaml does catch, and
     * its report already names the line.
     *
     * <p>Returning false asks snakeyaml to go on and write the value the usual way.
     */
    @Override
    public boolean setProperty(Object bean, String spelling, Object value) {
      String canonical = deprecatedAliases.getOrDefault(spelling, spelling);
      String previous =
          spellingByBean.computeIfAbsent(bean, key -> new HashMap<>()).put(canonical, spelling);
      if (previous != null && !previous.equals(spelling)) {
        throw new YAMLException(repeatedKeyMessage(element, canonical, previous, spelling));
      }
      return false;
    }

    Set<String> acceptedKeys() {
      return Collections.unmodifiableSet(keys.keySet());
    }

    Map<String, String> aliases() {
      return Collections.unmodifiableMap(deprecatedAliases);
    }

    @Override
    public Property getProperty(String name) {
      SchemaProperty property = keys.get(name);
      if (property != null) {
        return property;
      }
      String canonical = deprecatedAliases.get(name);
      if (canonical != null) {
        SchemaProperty aliased =
            Objects.requireNonNull(
                keys.get(canonical), "alias " + name + " points at unknown key " + canonical);
        LOGGER.warn(deprecationMessage(element, name, canonical));
        return aliased;
      }
      throw new YAMLException(unknownKeyMessage(element, name, keys.keySet(), deprecatedAliases));
    }

    private SchemaType<T> add(SchemaProperty property) {
      keys.put(property.getName(), property);
      return this;
    }

    @SuppressWarnings("unchecked")
    private static <V> V uncheckedCast(Object value) {
      return (V) value;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static BiConsumer<Object, Object> cast(BiConsumer<?, ?> setter) {
      return (BiConsumer<Object, Object>) (BiConsumer) setter;
    }
  }

  /**
   * The {@code <class>} description, which is the one type whose instance cannot be created from
   * the node alone: {@code loadClasses} decides whether naming a class also resolves it, and {@link
   * XmlClass} resolves in its constructor.
   *
   * <p>{@link #newInstance(Node)} is reached for both shapes a class entry can take, the bare name
   * and the mapping, because snakeyaml consults the registered descriptions before falling back to
   * its own scalar handling.
   */
  private static final class XmlClassType extends SchemaType<XmlClass> {

    private final boolean loadClasses;

    XmlClassType(boolean loadClasses) {
      super(XmlClass.class, "class");
      this.loadClasses = loadClasses;
      key("name", String.class, XmlClass::setName);
      mapKey("parameters", YamlSchema::asText, XmlClass::setParameters);
      listKey("includedMethods", XmlInclude.class, XmlClass::setIncludedMethods);
      listKey("excludedMethods", String.class, XmlClass::setExcludedMethods);
    }

    @Override
    public Object newInstance(Node node) {
      return new XmlClass(className(node), loadClasses);
    }

    private static String className(Node node) {
      if (node instanceof ScalarNode) {
        return ((ScalarNode) node).getValue();
      }
      if (node instanceof MappingNode) {
        for (NodeTuple tuple : ((MappingNode) node).getValue()) {
          Node key = tuple.getKeyNode();
          Node value = tuple.getValueNode();
          if (key instanceof ScalarNode
              && "name".equals(((ScalarNode) key).getValue())
              && value instanceof ScalarNode) {
            return ((ScalarNode) value).getValue();
          }
        }
      }
      throw new TestNGException("A <class> of a YAML suite must be a name or carry a \"name\" key");
    }
  }

  /**
   * A property that writes through the method reference it was declared with.
   *
   * <p>{@link PropertySubstitute} is extended rather than used as is: its own constructor takes the
   * <em>names</em> of the accessors and looks them up reflectively, falling back to the matching
   * field. Only the type information it carries is wanted here -- the declared type and the element
   * type of a sequence, which is how snakeyaml knows what to build for the value.
   */
  private static final class SchemaProperty extends PropertySubstitute {

    private final BiConsumer<Object, Object> setter;

    SchemaProperty(
        String name, Class<?> type, BiConsumer<Object, Object> setter, Class<?>... typeArguments) {
      super(name, type, typeArguments);
      this.setter = setter;
    }

    @Override
    public boolean isWritable() {
      return true;
    }

    @Override
    public void set(Object target, Object value) {
      setter.accept(target, value);
    }

    @Override
    public Object get(Object target) {
      throw new UnsupportedOperationException("The YAML schema only describes reading");
    }
  }
}

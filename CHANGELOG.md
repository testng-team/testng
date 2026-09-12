# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

Version numbers do not follow [Semantic Versioning](https://semver.org/spec/v2.0.0.html):
a minor release may carry a breaking change. Each one is listed under *Possible backward
incompatible changes*, in the section of the release that ships it.

## [Unreleased]

Next release: 7.13.0.

### Added

- [GITHUB-2663](https://github.com/testng-team/testng/issues/2663): A configuration method can now carry a priority, the way a test method already could. @BeforeSuite, @AfterSuite, @BeforeTest, @AfterTest, @BeforeClass, @AfterClass, @BeforeMethod, @AfterMethod, @BeforeGroups and @AfterGroups all accept priority(), and the lower value runs first -- so a project holding dozens of them, which until now had to name its methods for the order it wanted or chain them with dependsOnMethods, can state that order outright. The priority only ever arranges methods TestNG was otherwise free to run in any order. A dependsOnMethods or dependsOnGroups chain outranks it, and so does the inheritance guarantee: a subclass @BeforeMethod does not overtake its superclass's by asking for a lower number, and the @After methods still walk back up the hierarchy child first, with the priority ordering only what sits at the same level. Note the priority itself is not mirrored for the @After methods the way the inheritance order is -- the lower value runs first there too. Priorities are compared inside the scope TestNG already collects the methods in, which is the test class, superclasses included, for the class-level, method-level and group-level ones, and the `<test>` tag, all of its classes, for the test-level and suite-level ones; a priority therefore never couples two configuration methods TestNG would not have ordered against each other anyway, and in particular the @BeforeSuite methods contributed by different `<test>` tags still run in `<test>` order rather than being sorted across the suite. One shape is worth knowing before reaching for this: a configuration method taking part in a dependsOn chain is emitted ahead of the independent ones whatever the priorities say, which is the order TestNG already produced for such a set. Declaring no priority leaves every existing suite ordered exactly as it was (Julien Herr)
- The same `<class>` listed twice in one `<test>`, and the same `<include>` listed twice for one class, now run once per occurrence with that occurrence's own parameters. The DTD has always allowed both -- `<classes>` is class\* and both tags take their own `<parameter>` -- but discovery collapsed them: ClassInfoMap keyed the `<class>` tags by java class, so the last one silently replaced the earlier ones, and XmlTestUtils resolved parameters by name, taking the last matching `<class>` and the first matching `<include>`. So `<class name="C"><parameter name="id" value="one"/></class>` followed by the same tag with value="two" ran C once with id=two, and two `<include name="f">` tags carrying id=one and id=two ran f once with id=one. Each method is now scheduled once per `<class>` occurrence and once per `<include>` that names it, and carries that tag with it, so it reads the parameters of the occurrence it was scheduled for. A class listed once is unaffected, down to the method identity: the occurrence index folded into ITestNGMethod equality is zero for every suite that repeats neither tag. @BeforeClass and @AfterClass still run once per class per instance, which is what they are documented to do -- before the first and after all the test methods of the current class (Julien Herr)
- org.testng.ITestContext.getStartInstant() and getEndInstant() answer when a `<test>` started and stopped as java.time.Instant, and getStartDate() and getEndDate() are deprecated in their favour. Both new methods are default and built on the old pair, so an existing implementation of ITestContext keeps working and keeps compiling; the old pair stays abstract for now, because two mutually defaulting methods would compile for an implementation overriding neither and then recurse until the stack ends. org.testng.internal.Utils.requireEndInstantOf is new, and a second AbstractXmlReporter.setDurationAttributes overload takes instants beside the deprecated one that takes dates, and TestNG reads the instants everywhere it used to read the dates. As a consequence getStartDate() answers a fresh Date on every call rather than the runner's own field, so a caller that writes to the answer no longer moves the moment the run reports (Julien Herr)
- org.testng.internal.Utils.durationOf(ITestContext) answers how long a `<test>` ran in milliseconds, folding the end-minus-start expression that four reporters each spelled out. It asserts the context has finished, which is what all four already did for themselves (Julien Herr)
- A test method no longer allocates the collections it does not use. Five empty String arrays, two HashSets, a ConcurrentHashMap, a ConcurrentLinkedQueue and an ArrayList were created for every method object whether or not anything was stored in them -- 344 bytes per method, paid once per method per instance in a @Factory suite. They are now shared when empty or created on first use. A 50,000 instance factory suite holds 174 MB where it held 274 MB (Krishnan Mahadevan)
- [GITHUB-3322](https://github.com/testng-team/testng/issues/3322): A suite file may declare the schema instead of a doctype, with xsi:noNamespaceSchemaLocation="https://testng.org/testng-1.1.xsd" on `<suite>`, and is then validated against testng-1.1.xsd. A suite declaring neither is validated against the schema too, where it was previously validated by nothing: error() discarded every violation unless a doctype had been seen, and the parser was configured for DTD validation, so a document carrying no DTD had no grammar to violate. A suite declaring a doctype keeps being validated against the DTD, so nothing changes for the files that have one. TestNG always uses the schema it ships, never the URL the file names, so a parse still touches no network (Julien Herr)
- [GITHUB-3352](https://github.com/testng-team/testng/issues/3352): Reduce memory footprint of ConstructorOrMethod retained by BaseTestMethod (Krishnan Mahadevan)
- [GITHUB-3111](https://github.com/testng-team/testng/issues/3111): A @Factory produced test class instance is now described by the public org.testng.IFactoryInstance -- its index in the factory output, the parameters of the invocation that produced it, and the org.testng.IFactory that made it -- reachable from ITestResult.getFactoryInstance() and ITestNGMethod.getFactoryInstance(). Consumers such as junit-team/testng-engine no longer have to reach into org.testng.internal.IParameterInfo, whose exposure through the deprecated ITestNGMethod.getFactoryMethodParamsInfo() was the reason IClass.getInstanceHashCodes() and IClass.getInstances(boolean) could not be retired. Reading any of it leaves a lazy instance uninstantiated (Krishnan Mahadevan, Julien Herr)
- [GITHUB-3344](https://github.com/testng-team/testng/issues/3344): Lazy (just-in-time) instantiation for @Factory powered test classes (Krishnan Mahadevan)
- [GITHUB-3318](https://github.com/testng-team/testng/issues/3318): The YAML writer now also emits the suite-level groups, preserve-order, parent-module, guice-stage, allow-return-values, share-thread-pool-for-data-providers, the method selectors at both levels, class parameters and include descriptions, all of which were silently dropped (Julien Herr)
- [GITHUB-3319](https://github.com/testng-team/testng/issues/3319): testng.xml now has an XSD, testng-1.1.xsd, shipped next to testng-1.1.dtd and mirroring it declaration for declaration, for the tools that cannot consume a DTD. The DTD stays authoritative for files carrying a doctype; a test validates the whole suite corpus under both schemas and fails when the two stop agreeing (Julien Herr)
- Added round trip characterization tests covering every suite file of the test corpus, so that XML serialization can be refactored safely (Julien Herr)
- Added OpenRewrite to the build with a hand-picked recipe list (see rewrite.yml), and applied it to the main sources (Julien Herr)
- Extract the command line front end out of testng-core into the new testng-cli and testng-jcommander modules, so that testng-core no longer depends on JCommander (Julien Herr)
  - org.testng.TestNG.main now delegates to an org.testng.ITestNGCliRunner looked up through the ServiceLoader. The reference implementation is bundled inside the org.testng:testng jar, so "java -cp testng.jar org.testng.TestNG suite.xml" is unchanged. Repackaging testng.jar without META-INF/services/org.testng.ITestNGCliRunner now disables the command line.
  - testng-core no longer brings org.jcommander:jcommander transitively. Embedders that drive TestNG through its Java API drop a dependency; those that relied on it being on the classpath have to declare it.
  - Deprecated: TestNG.main, TestNG.privateMain, org.testng.CommandLineArgs (use org.testng.cli.CliOptions), TestNG.configure(CommandLineArgs) (use org.testng.cli.CliConfigurer) and TestNG.validateCommandLineParameters. They all remain available and are scheduled for removal in 8.0; see the behaviour changes listed below.
  - TestNG.validateCommandLineParameters now reports failures as org.testng.TestNGException instead of JCommander's ParameterException. Both are unchecked, so the signature is unchanged, but a caller catching ParameterException no longer catches it.
  - TestNG gained setListenerComparatorClass, setListenerFactoryClass, setExecutorServiceFactoryClass and setInjectorFactoryClass, which instantiate through the object factory in use. They are named rather than overloaded so that existing calls passing a bare null keep compiling.
  - TestNG.setThreadCount now raises a TestNGException for a value below 1 instead of printing the usage banner and calling System.exit(1). The command line output is unchanged, since TestNG.main turns that exception into the same message and exit code; embedders get an exception they can handle.
  - ITestNGCliRunner implementations never terminate the JVM: an unusable command line comes back as a TestNGException. Only TestNG.main turns that into a message, a usage banner and exit code 1, so the observable command line behaviour is unchanged. TestNG.privateMain now throws where it used to exit, which is what an embedder wants.
  - Moved: org.testng.Converter is now org.testng.cli.jcommander.Converter. It is still bundled in testng.jar.
- [GITHUB-3290](https://github.com/testng-team/testng/issues/3290): Support data providers that return Stream`<Object[]>` or Stream`<Object>`; the stream is consumed lazily and closed once its rows have been consumed (Krishnan Mahadevan)

### Changed

- [GITHUB-1346](https://github.com/testng-team/testng/issues/1346): @BeforeGroups and @AfterGroups now state in their javadoc where they sit in the lifecycle. The group configuration runs inside the lifecycle of the class the group is entered and left from, not around it: @BeforeClass runs before @BeforeGroups, and @AfterGroups before @AfterClass. [GITHUB-1346](https://github.com/testng-team/testng/issues/1346) asked for the reverse -- @BeforeTest -> @BeforeGroups -> @BeforeClass, and @AfterClass -> @AfterGroups -- on the reading that a group is declared on a `<test>` and may span several classes, so it should be the outer scope. It cannot be one. A group is a selector over test methods, not a container of classes, and two shapes TestNG has always supported make the requested order undefined rather than merely different: a class whose methods alternate between two groups enters the second group in the middle of its own run, long after its single @BeforeClass, and leaves the first one while its own methods of the second are still to come; and a test method belonging to two groups has the setup of both due before it, with neither group enclosing the other. An order that holds for the first group a class enters and is undefined for every later one is not a contract, and making it hold always would mean scheduling every group's methods contiguously -- a different feature from a lifecycle reordering, and one that would collide with preserve-order, priority, dependsOnMethods and dependsOnGroups. Nothing changes in what TestNG does: the ordering described is the one it has produced all along, it is now written down (Julien Herr)
- org.testng.internal.IObject names its hash code accessor getObjectHashCodes(), and its static helper objectHashCodes(Object), where both used to say instance. The old names collided with the deprecated org.testng.IClass.getInstanceHashCodes(), so a single method body in ClassImpl, NoOpTestClass and TestClass served both a contract deprecated since 7.10.0 and a current one still reached through ITestNGMethod -- which made marking those bodies deprecated only half true. Pulling the two apart also showed that ClassImpl answered null before its objects were built, where IObject promises an array; the field starts empty now, which is what the only live caller already made of that null. org.testng.ITestNGMethod.getInstanceHashCodes() is a different contract and is untouched. They are listed under Possible backward incompatible changes below (Julien Herr)
- the comma separated command line values -testclass, -testnames and -spilistenerstoskip now have each element trimmed, and their empty elements dropped. "-testclass a.B, a.C" used to ask the class loader for a class called " a.C" and fail, and "-testclass ''" used to ask it for a class called "". The first now runs both classes. The second, and any value that names no class once trimmed such as "-testclass ' , ,'", is rejected by the command line validation with "You need to specify at least one testng.xml, one class or one method" rather than starting a run that selects nothing. A value with no space and no empty element is split exactly as before. The two implementations of the command line -- CliConfigurer and the deprecated TestNG.configure(CommandLineArgs) -- change together, which is what CliConfigurerParityTest requires and cannot itself detect (Julien Herr)
- three internal types whose equals compares getClass are now final: org.testng.internal.collections.Pair, org.testng.internal.IObject.IdentifiableObject and org.testng.internal.KeyAwareAutoCloseableLock.AutoReleasable. None has a subclass in TestNG, and a getClass-based equals already meant a subclass could never be equal to its base, so sealing them takes away nothing that worked. They are listed under Possible backward incompatible changes below (Julien Herr)
- org.testng.xml.XmlTest.getMetaGroups() and org.testng.TestNG.runSuitesLocally() answer a mutable collection from every path. Both answered Collections.emptyMap()/emptyList() from one branch -- a test that declares no `<groups>`, a run that found no suite -- and a fresh HashMap or ArrayList from the others, so whether the answer could be written to depended on which branch ran, and a caller that got it wrong found out at runtime. Nothing in TestNG writes to either, and widening what a caller may do cannot break one that already worked. Two internal methods, IAnnotationFinder.findInheritedAnnotations and ITestInvoker.cancelRemainingInvocations, had the same split and now answer a mutable empty list from that branch too (Julien Herr)
- An array parameter is written to testng-results.xml by its contents rather than by its identity: `<value><![CDATA[[1, 2]]]></value>` where the file used to hold [I@1b6d3586. [GITHUB-2315](https://github.com/testng-team/testng/issues/2315) made the console reports print the contents of a native array, but the XML report went on calling toString() on the array itself, which answers an identity hash that differs from run to run -- so a tool parsing those values read something it could not compare between two runs of the same suite. Every other value is written exactly as before: a String unquoted, an empty one as empty CDATA, and an absent one as `<value is-null="true"/>` (Julien Herr)
- NullAway now runs on the test compiles as well as the main ones. @NullMarked applies to a package rather than to a source set, so the production package-info.class on the test compile classpath already marked the test half of the twelve packages that share a name with a main one; disabling the check there left those files claiming a contract nobody read. The one fix this exposed outside the tests is internal: the three short JarFileUtils constructors took a non-null testNames while the constructor they delegate to declares it @Nullable and the class body tests it for null, so the two halves of that pair now agree (Julien Herr)
- org.testng.IDataProviderInterceptor.intercept now takes and answers Iterator`<Object @Nullable []>`. A null row is not a missing row: it is how TestNG marks a data provider position excluded by @DataProvider(indices = ...) or by an invocation number read back from testng-failed.xml, and keeping the placeholder is what leaves the rows that do run in the data provider's own numbering. The pipeline has always produced those nulls -- four of its five consumers already skipped them by hand -- and the type said otherwise. A Java implementation is unaffected. A Kotlin implementation whose override declares the row array non-null stops overriding and must add the question mark. Any implementation that inspects the rows must expect null among them, and one that drops or reorders rows makes the reported indices its own rather than the data provider's (Julien Herr)
- An invocationCount cancelled by a failing invocation is now announced the same way whether its data provider is parallel or not. Under a parallel data provider such an invocation was announced as already skipped, so ITestListener.onTestStart never fired for it and, under alwaysRunListeners, IInvokedMethodListener saw no before/after pair for it; it also reported no parameters. It now goes STARTED then SKIP with the row it would have re-run, which is what a sequential data provider has always done. A listener that counts onTestStart, or pairs it with onTestSkipped, will see invocations of a parallel data provider it did not see before (Julien Herr)
- org.testng.ITestContext.getInjectorFactory() answers the new IInjectorFactory.NONE token instead of null for a context that names no factory, so it no longer answers null at all. Three GuiceHelper signatures carried the absence down to an assertion whose message named a case it could not see: the only factories TestNG builds come from Configuration, which initialises its own and never clears it (Julien Herr)
- org.testng.ITestResult.getMethod(), getName() and getInstanceName() no longer answer null, and setTestName no longer accepts null. The only result that had no method was the parameter carrier described in the entry below. Thirty-eight places in TestNG asserted the method was there through org.testng.internal.Utils.requireMethodOf and not one of them tested it; that helper is removed, its assertion having become the compiler's job (Julien Herr)
- A `<test>` built without going through a constructor -- which is how the YAML parser builds one -- now carries the same "Default XmlTest name `<uuid>`" the XML and the programmatic paths have always had, so org.testng.xml.XmlTest.getName() and org.testng.ITestContext.getName() no longer answer null. Nineteen places dereferenced that name: two of them keyed a ConcurrentHashMap in FailedReporter, which throws on a null key, and XmlSuite.toXml() fed it to Properties.setProperty, which rejects a null value. Two nameless tests in one YAML suite used to be rejected as duplicates named "null"; they now carry different names and both run (Julien Herr)
- Two collections a test method hands back changed shape as part of that. XmlTest.getInvocationNumbers(String) answers an immutable empty list for a method that declares no invocation numbers, where it used to answer a fresh mutable ArrayList, so a caller that means to add to what it gets back has to copy it first. ITestNGMethod.upstreamDependencies() and downstreamDependencies() answer a snapshot rather than a live view: setting the dependencies swaps in a new set instead of refilling the one already handed out, so a set taken before a replacement keeps what it held (Krishnan Mahadevan)
- [GITHUB-3322](https://github.com/testng-team/testng/issues/3322): The hint printed for a suite file that declares no grammar now offers the schema first and the doctype second, and is no longer printed for a file that declares a schema (Julien Herr)
- [GITHUB-3322](https://github.com/testng-team/testng/issues/3322): toXml() now declares the schema on `<suite>` instead of emitting a doctype, so what TestNG writes -- testng-failed.xml above all -- is what TestNG recommends. The two cannot both be declared: the DTD declares neither xmlns:xsi nor xsi:noNamespaceSchemaLocation, so a document carrying both is not DTD-valid. A suite file that already declares a doctype is unaffected; only regenerated output changes (Julien Herr)
- [GITHUB-3322](https://github.com/testng-team/testng/issues/3322): Validating a suite against the schema requires a namespace-aware parser, which widens what counts as malformed: in a suite file that declares no doctype, an undeclared namespace prefix is now an error where it used to be read as part of the name. A suite declaring a doctype is unaffected, and testng.xml.validation=off restores the previous behaviour (Julien Herr)
- org.testng.internal.Utils.escapeHtml(String) and escapeUnicode(String) no longer accept null. Both used to answer null with null; they now throw a NullPointerException, and their return types are no longer nullable. No caller in TestNG passes null to either, and the null branch made the signature contradict itself once the package declares its nullness (Julien Herr)
- org.testng.reporters.XMLUtils.escape(String) no longer accepts null. It used to answer null with null; it now throws a NullPointerException, and its return type is no longer nullable. Nothing in TestNG ever called it with null, and nothing in TestNG calls it at all outside XMLUtils itself (Julien Herr)
- org.testng.internal.ClonedMethod.getConstructorOrMethod() returns the wrapped method instead of null. It answered null while the class held the java.lang.reflect.Method all along, so its own toString() threw a NullPointerException on every call, and none of the 59 call sites of ITestNGMethod.getConstructorOrMethod() in TestNG tested the result. Keeping it non-null is what lets ITestNGMethod.getConstructorOrMethod() stay non-null in the published interface (Julien Herr)
- org.testng is now declared @NullMarked, so every member of the published API states whether it can answer null. The members that widen to @Nullable do so because their implementations already answered null, and the rest promise not to. This is binary compatible and source compatible for Java; a Kotlin caller that dereferences one of them without testing it stops compiling. They are listed under Possible backward incompatible changes below (Julien Herr)
- org.testng.internal.MethodInstance.SORT_BY_INDEX no longer throws a NullPointerException when a method a @Factory produced belongs to no `<test>` tag. It answers that the two methods cannot be compared, which is what the neighbouring branch already answers for a missing `<class>` (Julien Herr)
- org.testng.internal.IInstanceIdentity.getInstanceId(Object) answers the new NO_INSTANCE token instead of null for a method that carries no instance, so the value can be used as a map key without every caller deciding what an absent key means. The grouping is unchanged: every method without an instance still lands in one bucket (Julien Herr)
- The plain factory methods of org.testng.collections.Lists, Maps and Sets are deprecated for removal in TestNG 8. They are one-line wrappers over a JDK constructor -- Lists.newArrayList() is new ArrayList&lt;>(), Maps.newHashMap() is new HashMap&lt;>(), Sets.newHashSet() is new HashSet&lt;>() -- and each one now carries a javadoc line naming its replacement. TestNG no longer uses any of them internally. The members of those classes that have no JDK equivalent are unaffected and stay supported: Lists.merge, Lists.intersection, the Maps multimap factories, MultiMap/ListMultiMap/SetMultiMap, Objects.toStringHelper and CollectionUtils. org.testng.util.Strings is a different package and is untouched (Julien Herr)
- [GITHUB-3321](https://github.com/testng-team/testng/issues/3321): YAML suite support moved out of testng-core into a testng-yaml module. testng.jar is unchanged -- the classes are shaded in as before and snakeyaml stays an optional dependency -- but testng-core no longer names YAML anywhere, so the parser cannot drift back into the core module (Julien Herr)
- [GITHUB-3321](https://github.com/testng-team/testng/issues/3321): A test's dependency groups are written under "dependencyGroups" rather than "xmlDependencyGroups", which was named after the field it lands in. The old spelling is still read, with a deprecation warning, as are "xmlPackages", "xmlClasses" and a method selector's "name" (Julien Herr)
- Dependency refresh: Guice 6.0.0, JCommander 2.0, snakeyaml 2.6, slf4j-api 2.0.18. Guice 7 and JCommander 3 were skipped: they require jakarta.inject and Java 17 respectively
- Building TestNG now requires JDK 25 and uses Gradle 9.6.1; published artifacts still target Java 11
- Refreshed the build plugins: Error Prone 5.1.0, SonarQube 7.3.1, Kotlin 2.4.10, AssertJ 3.27.7
- GitHub Actions test matrix now covers Java 26 and early-access 27/28, and every action is pinned to a commit SHA

#### Possible backward incompatible changes

- org.testng.reporters.jq.BaseMultiSuitePanel.getContent(ISuite, XMLStringBuffer) is final. The seven concrete panels declare it public and org.testng.reporters.jq is OSGi exported, so a subclass outside TestNG could override it; it stops compiling. It had already stopped working: generate() builds the panel through writeContent now, so such an override was silently skipped.
- org.testng.reporters.FileStringBuffer raises IllegalStateException where it used to log and carry on: a flush that cannot write, and a temporary file that cannot be read back. A caller that relied on receiving a truncated or empty document instead of an error sees the error.
- org.testng.internal.collections.Pair, org.testng.internal.IObject.IdentifiableObject and org.testng.internal.KeyAwareAutoCloseableLock.AutoReleasable are final. All three are public members of internal, OSGi exported packages, so a subclass outside TestNG compiled until now; it stops compiling. Each of the three compares getClass in equals, which is to say an instance of such a subclass was never equal to a Pair, an IdentifiableObject or an AutoReleasable, and never matched one as a map key.
- org.testng.annotations.ITestOrConfiguration declares getPriority() and setPriority(int), which org.testng.annotations.ITestAnnotation declared until now and still inherits. Every TestNG implementation of the interface already had both, and a caller reading them through either type is unaffected, source and binary. A class outside TestNG implementing ITestOrConfiguration or IConfigurationAnnotation directly, rather than extending org.testng.internal.annotations.TestOrConfiguration, has to add them. The gain on the configuration side is that org.testng.IAnnotationTransformer can now set the priority of a configuration method, as it could already do for a test method.
- org.testng.ITestContext.getInjectorFactory() is declared non-null and its default answers IInjectorFactory.NONE. Code testing the result for null must test for the token instead. A custom ITestContext that never overrode it used to fail a @Guice test class with a NullPointerException naming the missing factory, and now fails it with an UnsupportedOperationException from IInjectorFactory.NONE naming the same cause; a context that names a factory, which is every context TestNG itself builds, is unaffected. A Kotlin class implementing ITestContext whose getInjectorFactory() override answers a nullable type stops compiling.
- org.testng.ITestResult.getMethod(), getName() and getInstanceName() are declared non-null and setTestName(String) no longer accepts null. A Java caller reading any of them is unaffected; one passing null to setTestName used to leave the result nameless and now gets a NullPointerException. A Kotlin class implementing ITestResult whose getMethod(), getName() or getInstanceName() override answers a nullable type, or whose setTestName override declares String?, stops compiling. org.testng.internal.Utils.requireMethodOf(ITestResult) is removed; call ITestResult.getMethod() directly.
- org.testng.internal.TestResult can no longer be built without a test method. newEmptyTestResult() and setMethod(ITestNGMethod) are removed, and newTestResult(`Object[]`, int) takes the method as its first argument. The class is a public member of an internal, OSGi exported package; only code constructing results directly is affected, and every such caller already had the method in hand.
- org.testng.xml.XmlTest.getName() and org.testng.ITestContext.getName() are declared non-null, and XmlTest.setName(String) no longer accepts null. A Java caller reading the name is unaffected; one passing null to setName now gets a NullPointerException where it used to store null. A Kotlin caller assigning null to XmlTest.name stops compiling, as does a Kotlin class implementing ITestContext whose getName() override answers String?. A suite parsed from YAML whose test declared no name now reports "Default XmlTest name `<uuid>`" where it used to report nothing, so the report files named after that test, and testng-failed.xml, change name for that suite. Two nameless tests in one suite are no longer rejected as duplicates named "null". A YAML document whose test declares a "name:" key with no value, or a blank one, is now rejected by the reader with "A `<test>` of a YAML suite must carry a name", the way the XML reader has always rejected the same document; the setter's own check remains for a programmatic caller.
- org.testng is declared @NullMarked, and these members answer @Nullable because that is what their implementations already answered. A Java caller is unaffected. A Kotlin caller that dereferences one of them without testing it stops compiling, and must add a test or a !!.
  - ITestNGMethod: getTestClass, getInstance, getId, getDescription, getMissingGroup, getXmlTest, getRetryAnalyzer, getDataProviderMethod and the deprecated getFactoryMethodParamsInfo
  - ITestResult: getTestName, getInstance, getHost, getThrowable and getTestContext
  - IClass: getXmlTest, getXmlClass, getTestName and getInstanceHashCodes
  - IAttributes: getAttribute and removeAttribute
  - IDataProviderMethod: getInstance and getMethod
  - IMethodInstance: getInstance
  - ITestClassFinder: getIClass
  - ITestNGListenerFactory: createListener
  - ITestObjectFactory: newInstance(Constructor, Object...)
  - ITestContext: getEndDate and getHost
  - ISuite: getHost, getParameter, getParentInjector and getObjectFactory
- The same mark widens the parameters of IAnnotationTransformer.transform for ITestAnnotation and IConfigurationAnnotation -- testClass, testConstructor and testMethod, of which the javadoc has always said only one is non-null -- of the four IConfigurationListener callbacks that take an ITestNGMethod, of the three IDataProviderListener callbacks and IDataProviderInterceptor.intercept that take an ITestContext, of IModuleFactory.createModule, of IMethodSelector.includeMethod, of IClass.getInstances' error message prefix, and of Reporter.setCurrentTestResult, which TestNG calls with null to clear the current result. A Java implementation is unaffected. A Kotlin implementation whose override declares the parameter non-null stops overriding and must add the question mark.
- org.testng.internal.MethodSorting.INSTANCES, the default method order, produces a different order for two invocations of the same method on different @Factory instances. Both the old and the new order are arbitrary -- instance ids are random UUIDs -- so no run that did not already depend on an arbitrary order is affected, but a run that pinned the old one will see it change.
- org.testng.internal.IInstanceIdentity.getInstanceId(Object) answers IInstanceIdentity.NO_INSTANCE rather than null for a method that carries no instance. Code testing the result for null must test for the token instead. The package is internal and OSGi exported.
- testng-core and testng-core-api no longer declare a compileOnly dependency on com.github.spotbugs:spotbugs. Nothing in TestNG uses javax.annotation any more.
- testng-failed.xml no longer records the index of a @Factory produced instance as an invocation-number. That attribute selects rows of a method's own data provider, which is the only thing TestNG ever reads it back as, so a factory powered failure produced a file that looked filtered and re-ran everything -- and, for a method that had a data provider of its own, re-ran the wrong rows because the factory index had overwritten the row index. The instance index now goes to the new factory-instances attribute of `<include>`, which is honoured on re-run. Tooling that parses testng-failed.xml to learn which factory instance failed must read factory-instances rather than invocation-numbers; a method with its own data provider now re-runs the rows that actually failed. A file generated by 7.13 and re-run by an older TestNG ignores the unknown attribute and re-runs every instance, which is what those versions already did. ([GITHUB-3111](https://github.com/testng-team/testng/issues/3111), [GITHUB-2517](https://github.com/testng-team/testng/issues/2517), [GITHUB-2521](https://github.com/testng-team/testng/issues/2521))
- The constructors of org.testng.internal.ParameterInfo and org.testng.internal.LazyParameterInfo now take an org.testng.internal.FactoryInstance instead of a loose index and parameter array. Both are implementation classes of an internal package; only code constructing them directly is affected. ([GITHUB-3111](https://github.com/testng-team/testng/issues/3111))
- org.testng.internal.ClonedMethod.getConstructorOrMethod() answers the wrapped method rather than null. Code that tested the result for null now takes the other branch; no caller in TestNG did, and the method's own toString() could never run before. ITestNGMethod.getConstructorOrMethod() therefore stays non-null when org.testng is marked in turn.
- org.testng.internal.Utils.escapeHtml(String) and escapeUnicode(String) reject null instead of answering null with null. Both are public members of an internal, OSGi exported package: a Kotlin caller passing a String? stops compiling, and a Java caller passing null gets a NullPointerException from the first character read rather than a null result. Callers that relied on null-in/null-out must test for null themselves.
- org.testng.reporters.XMLUtils.escape(String) rejects null instead of answering null with null. The package is now @NullMarked, so the parameter is declared non-null: a Kotlin caller passing a String? stops compiling, and a Java caller passing null gets a NullPointerException from the first character read rather than a null result. Callers that relied on null-in/null-out must test for null themselves.
- org.testng.internal.IObject.getInstanceHashCodes() is renamed getObjectHashCodes(), and the static org.testng.internal.IObject.instanceHashCodes(Object) is renamed objectHashCodes(Object). The package is internal and OSGi exported, so an implementor or a caller outside TestNG stops compiling and must follow the rename. No deprecated bridge is kept under the old names: keeping getInstanceHashCodes() on IObject is exactly what made it collide with the deprecated org.testng.IClass.getInstanceHashCodes(), which every implementor of both had to satisfy with one body. org.testng.ITestNGMethod.getInstanceHashCodes() keeps its name and its meaning.

### Fixed

- org.testng.reporters.FileStringBuffer no longer throws a NullPointerException when the first thing appended to it is a string of 100 000 characters or more. A string that size bypasses the in-memory builder and is written to the temporary file directly, and the file is created as the builder is flushed -- but flushToFile answers early when the builder is empty, which it is when nothing has been appended yet, so that path went straight on to write to a file it had just decided not to create. Appending anything at all first, however short, was enough to hide it. The file is now created where it is needed rather than as a side effect of a flush, and a buffer whose file cannot be created says so with an IllegalStateException naming it instead of failing as a null one frame later, which is how FileStringBuffer.toString already reports the same impossibility. org.testng.reporters.FileStringBufferTest, which had asserted nothing since it was written in 2013 and was listed in no suite, now covers both readers -- toString and toWriter -- below the flush threshold, across it, and on either side of that direct-to-file size. Two silent failures in the same class go with it: a flush that could not write used to replace the in-memory builder anyway, so the characters it still held vanished from the middle of the document with nothing raised, and toWriter logged what toString has always thrown for, so a spilled buffer whose temporary file had gone answered an empty document instead. Both now raise IllegalStateException, which is what temporaryFile already did for the same class of fault. A failed spill is recorded rather than raised on the spot, and raised when the buffer is asked for its content: appending happens deep inside building a document -- for the JUnit report, inside a listener TestNG calls bare -- so raising there would end the run at whatever tag happened to overflow. And JUnitXMLReporter catches it, because it is an IResultListener2 rather than an IReporter: TestNG.generateReports wraps each reporter in its own try/catch, but TestRunner calls its listeners with nothing around them, so a report that could not be written would have cost the run and every other report with it. It loses its file and says so through its logger. The message on the way out no longer names the temporary file, since the same catch covers a buffer that never spilled and has none -- there the fault is the destination, which while index.html is being written is the likelier of the two. And a report that gives up part way leaves no file and says so through its logger, whatever it gave up with: Utils.writeUtf8File writes its prefix before the buffer, so index.html was left as a page header with no body and nothing to say why -- and the OutOfMemoryError this entry is about is an Error rather than a RuntimeException, so the case that mattered most was the one the removal missed. And the temporary file is written and read as UTF-8 rather than through the platform default charset: a character that charset could not encode was lost at spill time, before the explicitly UTF-8 page writer ever saw it, so a supplementary character in a report reached index.html as a question mark on a JDK whose default is not UTF-8 (Julien Herr)
- [GITHUB-1259](https://github.com/testng-team/testng/issues/1259), [GITHUB-2334](https://github.com/testng-team/testng/issues/2334): index.html is no longer built by holding one panel of it in memory at a time. Every panel of the default HTML report writes into an XMLStringBuffer of its own -- backed, like the page it feeds, by a buffer that spills to a temporary file past 100 000 characters -- and used to hand that buffer to the page as a String, which read the whole file back and copied it once more for the regular expression that drops the characters XML 1.0 does not allow. So a buffer whose reason to exist is not being held in memory was held in memory twice over, once per panel, and the largest of them grows with the result count: [GITHUB-2334](https://github.com/testng-team/testng/issues/2334) ran out of heap in NavigatorPanel.generateMethodList listing the passed methods of a run of 500 000, [GITHUB-1259](https://github.com/testng-team/testng/issues/1259) in BaseMultiSuitePanel, both of them under a summary saying every test of the run had passed. The panels now stream their buffer into the page through the new XMLStringBuffer.addBuffer, which drops the same characters a slice at a time, and TimesPanel writes its table a row at a time rather than building the whole of it as one String. On a run of 20 000 results whose report is 39 MB, generating it needed a 64 MB heap and now needs 28 MB; below that the limit is what the run itself retains, which is [GITHUB-1979](https://github.com/testng-team/testng/issues/1979) and unchanged here. The page is identical, the order of what it lists included. org.testng.reporters.XMLStringBuffer.addBuffer(XMLStringBuffer) is new, and the panels of org.testng.reporters.jq write into a buffer BaseMultiSuitePanel owns, through a new package-private writeContent, rather than each building one and answering its String. BaseMultiSuitePanel.getContent(ISuite, XMLStringBuffer) stays where it is -- concrete now rather than abstract -- for callers outside the report, and is the one place left that materialises a panel. BaseMultiSuitePanel.getContent(ISuite, XMLStringBuffer) is final: generate no longer calls it, so an override outside the package decided nothing and would have been skipped without a word. TimesPanel no longer adds each result's duration into its running total on every call, which made a second call to getContent report the suite as having run twice as long (Julien Herr)
- [GITHUB-2187](https://github.com/testng-team/testng/issues/2187): testng-results.xml now writes a parameter `<value>` as `<value><![CDATA[...]]></value>` on one line, so XML consumers that read the element text get the parameter rather than the pretty-print whitespace around the CDATA (Burak Kalaycı)
- [GITHUB-1031](https://github.com/testng-team/testng/issues/1031): A data provider that supplies the wrong number or type of arguments now reports that mismatch. Count errors state the expected and actual counts. Type errors state that the types do not match. The matcher no longer claims the test method has no parameters (Burak Kalaycı)
- [GITHUB-3121](https://github.com/testng-team/testng/issues/3121): EmailableReporter2 now appends each test invocation's non-blank ITest custom name to the Java method name in the summary table and scenario headings. Configuration methods keep the Java method name (Burak Kalaycı)
- [GITHUB-2940](https://github.com/testng-team/testng/issues/2940): testng-failed.xml now includes passed members of a group that a skipped or failed test depends on. MethodHelper.getMethodsDependedUpon now adds those group members and walks each member's own method and group dependencies. The walk uses only direct graph predecessors. A rerun suite therefore keeps the full dependency closure the skipped test needs (Burak Kalaycı)
- [GITHUB-3234](https://github.com/testng-team/testng/issues/3234): A test class whose methods cannot be read because a referenced type is missing is no longer treated as if it had no TestNG annotations. TestNGClassFinder.isTestNGClass now throws TestNGException wrapping the NoClassDefFoundError when that class was named in the suite, so the class is reported rather than silently skipped. A class found through a package scan still returns false, as [GITHUB-602](https://github.com/testng-team/testng/issues/602), so an optional class with a missing type does not fail the scan. A missing type used only in a method body is still a test failure, as before (Burak Kalaycı)
- [GITHUB-3478](https://github.com/testng-team/testng/issues/3478): Report an empty DataProvider as a skipped test with EmptyDataProviderBehavior.SKIP by default, and provide EmptyDataProviderBehavior.IGNORE for backward compatibility, selectable on the command line with -emptydataproviderbehavior (Julien Herr)
- [GITHUB-2927](https://github.com/testng-team/testng/issues/2927): IMethodSelector.setTestMethods now receives the known test methods after they are bound to their ITestClass, and before selectors are consulted for test or configuration methods. A selector that snapshots the list sees the methods; includeMethod is never called first. (Burak Kalaycı)
- [GITHUB-3437](https://github.com/testng-team/testng/issues/3437): ClassHelper.getAvailableMethods now memoizes the hierarchy scan per class in a ClassValue, so data-provider and configuration lookups no longer rescan the same class on every call. Callers still receive a fresh set (Burak Kalaycı)
- [GITHUB-3239](https://github.com/testng-team/testng/issues/3239), [GITHUB-2714](https://github.com/testng-team/testng/issues/2714): configuration-method inheritance is kept whenever the explicit dependency graph has no path between the two methods. A child @BeforeClass no longer runs between two base methods, including when it only has an unrelated groups= value, and a base @AfterMethod no longer runs before the child's. An inheritance edge is omitted when it would close a cycle with an existing dependsOnGroups or dependsOnMethods chain (Burak Kalaycı)
- [GITHUB-2333](https://github.com/testng-team/testng/issues/2333): skip module-info.class and META-INF/versions entries when discovering classes in a test jar (Burak Kalaycı)
- [GITHUB-2595](https://github.com/testng-team/testng/issues/2595): A programmatic IMethodSelector at priority 10 is no longer dropped as a duplicate of the built-in XmlMethodSelector. RunInfo stored selectors in a TreeSet ordered only by priority, so two descriptors with the same priority compared equal and one was discarded. Equal-priority selectors now keep insertion order (Burak Kalaycı)
- [GITHUB-3222](https://github.com/testng-team/testng/issues/3222): dependsOnMethods in a static nested test class now resolves sibling methods, including methods inherited onto that nested class. MethodHelper.findDependedUponMethods() used to drop every method whose class had an enclosing class, so the nested class never saw its own tests (Burak Kalaycı)
- [GITHUB-3388](https://github.com/testng-team/testng/issues/3388): XmlSuite.toXml() now reports an unnamed XmlDefine reached through a test-level `<groups>` with "`<define>` has no name", matching the unnamed XmlPackage path, instead of a bare NullPointerException from Properties.setProperty (Burak Kalaycı)
- [GITHUB-1263](https://github.com/testng-team/testng/issues/1263): An IMethodInterceptor can now tell what each test method it is handed declares -- a dependsOnMethods or a dependsOnGroups -- and so what it is free to reorder and what it is free to drop. ITestNGMethod.upstreamDependencies() and downstreamDependencies() were published from the graph the run is scheduled on, and that graph is built from what the interceptors return, so every method an interceptor was handed answered two empty sets. The declared relation is now resolved over the whole method set and published before the chain runs, and only when a user interceptor is registered. It is deliberately not the scheduling graph, which over methods about to be dropped would validate their dependsOnGroups, reject a cycle they resolve and materialise the lazy @Factory instances they carry. A dependency that cannot be resolved is therefore absent from what the interceptor is told rather than fatal; the scheduling graph remains the one that validates what actually runs. What an interceptor is handed is unchanged: still every test method of the `<test>`, the ones taking part in a dependency included. Restoring the javadoc's promise that only free methods are passed would have taken from every filtering interceptor the ability to exclude a dependency-constrained test, so the wording is corrected instead, on IMethodInterceptor and on both accessors: the order preserve-order or group-by-instances imposes is TestNG's own and is not reported to an interceptor, the returned order cannot move a method ahead of what it has to follow, dropping a method another retained method depends upon ends the run with a TestNGException, and the answer is a view of a set TestNG rewrites rather than a value to hold. org.testng.internal.WrappedTestNGMethod now delegates the two accessors instead of inheriting an interface default that throws, and org.testng.internal.LiteWeightTestNGMethod answers the empty set a snapshot holding no reference to the run can give (Julien Herr)
- [GITHUB-2804](https://github.com/testng-team/testng/issues/2804): A dependsOnGroups declared by a @BeforeGroups method now orders the tests of the group that method runs before. @BeforeGroups and @AfterGroups methods are not nodes of the scheduling graph -- they are pulled dynamically, right before the first test method of a group they target -- and MethodHelper.topologicalSort leaves the group dependencies of a group configuration method alone for exactly that reason, so the dependency reached no scheduler at all: @BeforeGroups(value = "A", dependsOnGroups = "Z") ran the whole of group A, configuration included, before group Z had started. The graph now carries that dependency on the test methods of the target group, which is where it can be scheduled, so every method of Z runs before the configuration and the configuration before the first method of A, in parallel mode as well as sequentially. A group named by such a dependency but holding no method in the current `<test>` stays a no-op rather than becoming an error, as it has always been. What a failure in Z does is unchanged: TestInvoker decides skips from the test method's own dependsOnGroups, so a failing Z orders A after it without skipping it (Julien Herr)
- [GITHUB-299](https://github.com/testng-team/testng/issues/299): The chronological panel of the HTML report now closes its last `<div class="chronological-class">`. The block was opened on each class transition and closed only on the following one, and XMLStringBuffer.toXML() hands back the buffer without closing what is still on its tag stack, so index.html carried one unclosed `<div>` for every suite it reported -- 61 opening tags against 60 closing ones for a two-class suite. Every closing tag after it then matched one element too shallow, which is what put the later suites' chronological panels inside the first suite's still-open block instead of beside it. ChronologicalPanel was the only panel doing this; the other six close everything they open (Julien Herr)
- [GITHUB-2830](https://github.com/testng-team/testng/issues/2830): A parameter whose toString() throws no longer costs the run its reports. Rendering a value runs the user's code, and org.testng.internal.Utils.toString did not guard it, so a suite with one such parameter passed every test and then lost three reports at once: no testng-results.xml at all, no jq report, and emailable-report.html left at zero bytes. Nothing failed -- TestNG catches what an IReporter throws, prints it to stderr and moves on -- the files were simply never written. Utils.toString is now failsafe the way Utils.buildStackTrace already was, which covers every caller of it, each one being a report or a console line: a value that cannot render itself is written as com.example.Thing@1b6d3586, what Object.toString() answers for a class that does not override it. This is a behaviour change for the XML reports, where a run that produced an error now produces a value. An Error is caught as well as a RuntimeException, since the catch TestNG puts around a reporter covers only Exception and a toString() that recurses on itself would otherwise end the run. TestHTMLReporter is unchanged: its own [GITHUB-2830](https://github.com/testng-team/testng/issues/2830) failover calls toString() directly rather than through Utils, and spells the same identity in decimal (Julien Herr)
- [GITHUB-447](https://github.com/testng-team/testng/issues/447): index.html and emailable-report.html now report the values an invocation ran with as they were when it started. Both are written once every invocation of the run is over, so a data provider that hands the same mutable row to every invocation, or a test that changes what it was given, left every scenario of that method carrying the value's final state: three invocations of one method were all named report(invocation-4) -- which also gave the three of them one HTML anchor -- and four overlapping data provider rows were all named report(mutated). The HTML report lists the configuration methods too, the ones that passed included, so a @BeforeMethod handed the row its test method will run with was listed as prepare([mutated]) where VerboseReporter had printed [before-configuration], for the same invocation of the same run. Both reports now read the rendering TestNG takes as each invocation starts, which asks nothing of the parameter's type; every value is written exactly as before, and is still rendered once per invocation however many built-in reports read it. The factory parameters emailable-report.html lists are unchanged, since no snapshot describes them (Julien Herr)
- emailable-report.html is no longer lost whole to a parameter whose toString() answers null. Not the [GITHUB-2830](https://github.com/testng-team/testng/issues/2830) case above, and not covered by it: the rendering does not fail, it answers nothing, so Utils.toString has nothing to guard and answers null for such an object and Utils.escapeHtml stopped accepting one, so generateReport threw a NullPointerException, TestNG printed "Reporter ... failed" and left the file empty -- one such parameter anywhere in a run was enough to lose the whole report. It is now written as the word null -- on a method parameter row and on a factory parameter row alike -- which is what index.html has always shown for such a value and what testng-results.xml has always contained (Julien Herr)
- [GITHUB-775](https://github.com/testng-team/testng/issues/775): TestClass now looks its configuration methods up once for the test class instead of once per @Factory instance. The ten testMethodFinder.getBefore/After...Methods calls sat inside the loop that binds those methods to each instance, and every one of them rescans the whole class hierarchy, reads the annotations of every method it finds, builds a ConfigurationMethod per hit and then sorts the result -- none of which depends on the instance, since the argument is the test class every time. So a factory producing N instances paid for ten full scans N times over. The lookups are hoisted and their results bound to each instance in turn; a class that produced no instance is still not scanned at all. On a reduced form of the [GITHUB-772](https://github.com/testng-team/testng/issues/772) reproducer -- an eager constructor @Factory over a class carrying one method of every configuration type -- the collection phase is about six times faster -- roughly 2,4s down to 0,4s at 5000 instances and 0,48s down to 0,08s at 1000 -- and is still linear in the number of instances, the remainder being the per-instance ConfigurationMethod construction the binding performs. The methods that come out of it are unchanged, including the long-standing quirk that the array accessors for the suite, test, class and groups categories answer only the last instance's methods while the per-instance @BeforeClass/@AfterClass copies live in getInstanceBeforeClassMethods and getInstanceAfterClassMethods (Julien Herr)
- A repeated `<class>` whose `<methods>` select different methods no longer runs each of them for both occurrences. This follows from the above: an occurrence that lists `<include name="f"/>` schedules f and not g, where scheduling every method for every occurrence would have run both twice. A class listed once still creates its non-selected methods and lets XmlMethodSelector reject them, which is what makes them reportable as excluded (Julien Herr)
- XmlClass.getAllParameters() now includes the `<suite>` parameters. It is documented as "the parameters defined in this test tag and the tags above it" but read only the `<test>` tag's own parameters, stopping one level short, so it disagreed with XmlTest.getAllParameters() directly above it and with the resolution TestNG actually applies at invocation time. XmlInclude.getAllParameters(), which chains through it, gains the same level (Julien Herr)
- [GITHUB-3418](https://github.com/testng-team/testng/issues/3418): EmailableReporter2 no longer writes an empty invisible filler row under a result that already listed factory parameters. dumpParametersInfo for method parameters overwrote the flag the factory-parameter dump had just set, so a @Factory instance with no method parameters still got `<tr><th class="invisible"/></tr>` under those factory columns (Burak Kalaycı)
- [GITHUB-447](https://github.com/testng-team/testng/issues/447): A configuration method that passed now reports in the XML reports the values it ran with, where it reported their final state. testng-results.xml lists the configurations that passed and is written once every invocation of the run is over, but the rendering taken as such a method started was dropped the moment it succeeded -- on the premise that only failed and skipped configurations are listed, which was true until the XML reports started reading those renderings. So a @BeforeMethod handed the row its test method will run with reported what it left behind: VerboseReporter printed prepare([Ljava.lang.Object;)(value(s): [before-configuration]) and the file said [mutated], for the same invocation of the same run. The store now knows whether a reporter will read it after the invocations are over, and holds everything until the last one has, which also means the value is rendered once rather than captured, dropped, and rendered a second time by the fallback -- on @BeforeMethod and @AfterMethod, the most frequent invocations of a run. A run whose only readers sit in the invocation lifecycle, which is what TextReporter and VerboseReporter do, still drops what they are finished with, so it retains nothing it has no use for. What is retained otherwise is bounded by what TestNG already holds: a configuration declaring no parameter stores nothing at all, and the results themselves are kept for the whole run either way, since that is where the report reads them (Julien Herr)
- org.testng.internal.ClassHelper.forName no longer risks a ConcurrentModificationException when a class loader is registered while a suite is running. The list behind addClassLoader was a Vector, whose synchronised add says nothing about the iteration forName does over it from the runner's threads; it is a CopyOnWriteArrayList, which iterates a snapshot. addClassLoader is public static and reachable from a user thread, so the two really can overlap (Julien Herr)
- [GITHUB-3243](https://github.com/testng-team/testng/issues/3243): The throwable that ended a worker is no longer discarded. Since [GITHUB-3238](https://github.com/testng-team/testng/issues/3238) such a worker still has its nodes marked finished, so the graph moves on and the run comes back -- but that also makes it indistinguishable from a worker that ran cleanly, and GraphOrchestrator was the last place holding the cause. A listener whose class failed to initialise, for instance, took its ExceptionInInitializerError with it and the test simply failed with no explanation; the error was reachable only under a debugger. The orchestrator now keeps those throwables and TestTaskExecutor and SuiteTaskExecutor log them once the graph is done. The scheduling is deliberately unchanged: skipping the status update for a failed worker is what makes the run hang (Laszlo Kalina)
- [GITHUB-447](https://github.com/testng-team/testng/issues/447): testng-results.xml now reports the values an invocation ran with as they were when it started. The file is written once every invocation of the run is over, so a data provider that hands the same mutable row to every invocation, or a test that changes what it was given, left every `<test-method>` of that method carrying the value's final state. Only a parameter implementing Cloneable escaped it, and only because ITestResult had kept a reflective clone of it; the XML reports now read the rendering TestNG takes as each invocation starts, which asks nothing of the parameter's type. XMLReporter and PerSuiteXMLReporter are both covered, and a value is still rendered once per invocation however many built-in reports read it (Julien Herr)
- skipFailedInvocations and skipFailedInvocationCounts now stop the remaining invocationCounts of a method whose data provider is parallel. Only the sequential path drained the counter the invocation loop is driven from; the parallel one handed each worker a copy of it, so the whole data provider was run once more for every count still to come and the failing rows ran again instead of being cancelled. @Test(dataProvider = "parallel", invocationCount = 3, skipFailedInvocations = true) over a row that always fails now reports one failure and two skips, as the same method reports against a sequential data provider, where it reported three failures and three skips (Julien Herr)
- An invocationCount cancelled by a failing invocation is now registered with the test context, so ITestContext.getSkippedTests() lists it and the built-in reporters -- testng-results.xml, the HTML and JUnit XML reports -- account for it. Only the listeners were told before, so @Test(invocationCount = 3, skipFailedInvocations = true) reported one result in the files for a method that produced three, while an ITestListener counted all three. A cancelled invocation under a parallel data provider was already registered and is unaffected (Julien Herr)
- An invocation TestNG will not run is now given its parameters before it is announced as starting, so ITestListener.onTestStart already sees the values the result will be reported with. Under reportAllDataDrivenTestsAsSkipped a data-driven test skipped by a dependency was announced with no parameters and given its row on the following line, and the invocationCounts cancelled by a failing invocation were given none at all -- the latter now report the row they would have re-run. A configuration method skipped because an earlier one failed still reports nothing: resolving parameters for a method that will not run can itself throw, and that throw would turn the skip into a failure (Julien Herr)
- [GITHUB-1994](https://github.com/testng-team/testng/issues/1994): The XmlTest injected into a configuration or test method is now kept by reference in ITestResult.getParameters() instead of being cloned. XmlTest.clone() builds its copy with new XmlTest(suite), and that constructor registers the copy in the suite, so snapshotting the parameter appended a phantom `<test>` to the suite that was running -- once per invocation that received one. XmlTest.clone() itself is unchanged, so an IAlterSuiteListener can still use it to add a test. The reflective clone-if-Cloneable rule that backs the historical ITestResult parameter representation moved out of TestResult into its own internal component, unchanged for every other parameter type (Julien Herr)
- The result an invocation starts from carries its test method from the moment it is created, rather than being handed one by ConfigInvoker a few statements later. A @BeforeMethod that declares an ITestResult parameter is given that carrier, and used to see getName() and getInstanceName() answer null; both now answer the method and class names. The carrier is named when it is built, which is before the configuration method runs, so a class whose ITest.getTestName() is only set in that configuration method still reports the older name on the carrier and the newer one on the reported result. The carrier still knows nothing about the outcome: its status stays CREATED, its millis stay zero and it carries no test context. In memory friendly mode it keeps holding the live method rather than the lightweight snapshot, because that is the method a configuration method reaches through it to mutate (Julien Herr)
- [GITHUB-3358](https://github.com/testng-team/testng/issues/3358), [GITHUB-3359](https://github.com/testng-team/testng/issues/3359): every applicable @BeforeMethod(firstTimeOnly = true) now runs once, including a child method after a parent one, under a parallel data provider, and for overloaded @Test methods that share a name. The previous per-test-method token skipped later firstTimeOnly configurations and hid their failures. Parallel workers now wait for that firstTimeOnly method to finish before they continue, so a later data-provider row cannot start while the configuration is still running. After-configuration listeners now fire for a firstTimeOnly method that actually ran (Burak Kalaycı)
- [GITHUB-3385](https://github.com/testng-team/testng/issues/3385): XmlTest.addIncludedGroup now creates a missing `<run>` the same way addExcludedGroup and XmlSuite already do, so setGroups(new XmlGroups()) or addMetaGroup no longer throw NullPointerException. XmlTest.equals also compares a missing `<run>` instead of throwing when only the other side lacks one (Burak Kalaycı)
- [GITHUB-3377](https://github.com/testng-team/testng/issues/3377): setListenerClasses (and the CLI -listener flag) now supply a GuiceContext when instantiating listeners, matching suite XML `<listeners>`, so a @Guice-annotated listener no longer throws NullPointerException. CLI listeners are instantiated after the suite file is parsed, so they inherit that suite's Guice parent-module (Burak Kalaycı)
- [GITHUB-3378](https://github.com/testng-team/testng/issues/3378), [GITHUB-3364](https://github.com/testng-team/testng/issues/3364): ReflectionRecipes no longer treats Character as assignable to short (char does not widen to short). MethodMatcherException stringifies primitive arrays instead of throwing ClassCastException, leftover data-provider arguments still produce a diagnostic when the injection target is a constructor holder, and the unused out-of-bounds lenientMatch helpers are removed (Burak Kalaycı)
- [GITHUB-3366](https://github.com/testng-team/testng/issues/3366): ToStringHelper.omitNulls() and omitEmptyStrings() now inspect the original value instead of the already-stringified form, so ITestResult.toString() no longer renders output={null} when a result has no reporter output (Burak Kalaycı)
- org.testng.internal.TestNGMethod.clone() no longer throws a NullPointerException when the method has not been bound to a test class yet. It wrapped getTestClass() in a NoOpTestClass, which dereferences it on the spot; the absence is now propagated, which is what ConfigurationMethod.clone() already did (Julien Herr)
- A configuration method that is not a @BeforeGroups or @AfterGroups method now reports an empty array from getBeforeGroups() and getAfterGroups() instead of null. TestNGMethodFinder wrote null into fields whose declaration says {}, and MethodGroupsHelper iterates them without testing (Julien Herr)
- Sorting test methods by index no longer throws a NullPointerException when a `<test>` tag carries no name (Julien Herr)
- org.testng.internal.MethodSorting.INSTANCES orders two invocations of the same method on different @Factory instances instead of leaving the decision to a hash code comparison. Its identity branch asked IInstanceIdentity.isIdentityAware about the ids it had just resolved rather than about the methods, which could never hold, so the branch had never run (Julien Herr)
- org.testng.IAnnotationTransformer.transform(IFactoryAnnotation, Method) is now declared to accept a null method, which is what TestNG has always passed for a @Factory annotation found on a constructor (Julien Herr)
- In memory friendly mode (testng.memory.friendly), ITestNGMethod.getDataProviderMethod() answers null for a method that has no data provider, instead of a stand-in whose getName() answered an empty string and whose getMethod() threw UnsupportedOperationException. The interface has always documented null for that case, and the three call sites in TestNG already tested for it (Julien Herr)
- A `<package>` tag that carries no name attribute is now reported the way an unreadable package already was, instead of raising a NullPointerException from inside PackageUtils.findClassesInPackage (Julien Herr)
- [GITHUB-3238](https://github.com/testng-team/testng/issues/3238): A worker that completed exceptionally -- typically because a listener threw -- reached the graph orchestrator as a null worker, so marking its nodes finished threw a NullPointerException from inside FutureTask.done(). The graph never reached its final state and the parallel run hung until the test time-out. The worker now reaches the orchestrator in that case too, and the failure is recorded before listeners are notified so that a listener throwing a second time can no longer turn a failed run green (Krishnan Mahadevan)
- [GITHUB-3111](https://github.com/testng-team/testng/issues/3111): A @Factory method returning several instances from a single invocation gave every one of them the same index -- 0 when the factory has no data provider -- so the instances could not be told apart. IFactoryInstance.getIndex() is the instance's own position in the factory output, taken before @Factory(indices=...) filters it, so a factory of four instances under indices={1,3} reports 1 and 3. IParameterInfo.getIndex() is unchanged and now documented as the index of the invocation (Krishnan Mahadevan, Julien Herr)
- [GITHUB-3111](https://github.com/testng-team/testng/issues/3111), [GITHUB-2517](https://github.com/testng-team/testng/issues/2517), [GITHUB-2521](https://github.com/testng-team/testng/issues/2521): The index of the @Factory instance was recorded as a failed invocation number, which TestNG reads back as a row of the test method's own data provider. A failed factory powered run therefore produced a testng-failed.xml that re-ran every instance, and for a method that did have a data provider it re-ran the wrong rows. `<include>` now has a factory-instances attribute for the factory axis, honoured when the suite is re-run, and invocation-numbers again means data provider rows only (Julien Herr)
- [GITHUB-3321](https://github.com/testng-team/testng/issues/3321): The set of keys a YAML suite file accepts was whatever XmlSuite and its neighbours happened to expose through public accessors, so "fileName", "parsed" and "parentSuite" were suite keys, "index", "suite" and "xmlSuite" were test keys, "name" on a method selector silently meant "className", and adding a setter to the model silently extended the file format. The keys are now declared in one place and anything else is reported with the list of the accepted ones (Julien Herr)
- [GITHUB-3321](https://github.com/testng-team/testng/issues/3321): A YAML parameter is now read as text, as it is in testng.xml. An unquoted "44.0" or "true" used to be resolved to a Double or a Boolean and stored in a Map`<String, String>` through the erased setter, where it threw on the first caller that read the map as strings (Julien Herr)
- [GITHUB-3321](https://github.com/testng-team/testng/issues/3321): A key declared twice in a YAML suite file is now rejected, including under two different spellings of the same key -- "packages" next to "xmlPackages", "classes" next to "xmlClasses", "dependencyGroups" next to "xmlDependencyGroups", or a method selector's "className" next to "name". Whichever came second used to overwrite the first without a word, so a file declaring "tests" twice silently ran half of what it said (Julien Herr)
- [GITHUB-3321](https://github.com/testng-team/testng/issues/3321): A malformed YAML suite file, or one using a key outside the schema, is now reported as a TestNGException like a malformed XML one, instead of letting a snakeyaml exception escape through ISuiteParser (Julien Herr)
- [GITHUB-3321](https://github.com/testng-team/testng/issues/3321): Yaml.toYaml() dropped a test-level "time-out" and a suite-level "group-by-instances" although both can be read back, so converting a suite to YAML and back lost them (Julien Herr)
- [GITHUB-3316](https://github.com/testng-team/testng/issues/3316): close DTD resolver resources and bound connection waits (w3lld1)
- [GITHUB-3318](https://github.com/testng-team/testng/issues/3318): Yaml.toYaml() produced YAML that could not be read back -- a duplicated "packages" key, sequence items written without "- ", `<test>` keys indented at the column of the item they belong to, package filters written without a colon and under the plural keys "includes"/"excludes" the reader does not bind, and "suite-files" written under an unknown key and only for a suite that has child suites. The writer now builds a document and lets snakeyaml emit it, so quoting, escaping and indentation are correct by construction: a parameter valued "a,b" no longer reads back as two entries, and one valued "44.0" no longer reads back as a Double (Julien Herr)
- DTD validation of suite files was silently disabled: the SAX validation feature was probed under an "https" identifier that no parser recognizes, so setValidating(true) was never reached and violations went unreported. Validation is enabled again, with a new testng.xml.validation=off|warn|strict system property; the default "warn" reports violations without failing the run (Julien Herr)
- XmlSuite.toXml() dropped the "description" attribute of `<include>`, so regenerating a suite (testng-failed.xml, for instance) lost method descriptions (Julien Herr)
- XmlSuite.toXml() dropped a `<selector-class>` priority of -1 while the parser reads a missing priority as 0. Since a negative method-selector priority changes selector evaluation, serializing a suite and reading it back altered its behaviour (Julien Herr)
- The doctype written by XmlSuite.toXml() advertised testng-1.0.dtd although the parser always resolves testng-1.1.dtd (Julien Herr)
- XmlSuite.toXml() emitted two sibling `<groups>` elements for a suite that has suite-level groups, which the DTD allows only once, so TestNG's own output did not validate (Julien Herr)
- DTD violations were discarded for suite files pointing at their own copy or a mirror of the DTD rather than at testng.org, so those suites were never validated (Julien Herr)
- Suite files pointing at their own copy or a mirror of the DTD were told to add a `<!DOCTYPE>` they already declared (Julien Herr)
- A suite whose doctype had only an internal subset was treated as having none: advised to add a `<!DOCTYPE>` it already declared, and its DTD violations discarded even under strict validation (Julien Herr)
- [GITHUB-3138](https://github.com/testng-team/testng/issues/3138): Clarified that class-level @Ignore applies to subclasses, not nested classes (ShinyHero666)
- Remove leftover dead JUnit code: the deprecated unused ConversionUtils and orphaned JUnit test samples, following the removal of JUnit execution support in 7.10.0 (Julien Herr)
- [GITHUB-3242](https://github.com/testng-team/testng/issues/3242): use-global-thread-pool no longer refuses to start a suite when the number of data-driven tests reaches thread-count (Krishnan Mahadevan)
- [GITHUB-426](https://github.com/testng-team/testng/issues/426): firstTimeOnly @BeforeMethod / lastTimeOnly @AfterMethod were not run as a barrier around a parallel invocationCount thread pool (firstTimeOnly could run concurrently with test invocations, lastTimeOnly ran once per invocation instead of once)
- Time-out now measures a method's own execution time instead of the wall-clock time since dispatch, so thread-pool start-up and scheduling overhead under load no longer cause spurious time-outs; a method that never starts now reports an actionable infrastructure-starvation message
- [GITHUB-3166](https://github.com/testng-team/testng/issues/3166): Skipped configuration methods not receiving the causal throwable before configuration listeners are invoked (Anmol Jain)
- [GITHUB-3263](https://github.com/testng-team/testng/issues/3263): dataProviderClass cache mutation causes subclasses to use wrong data provider when run together (Aleksei Dobrynin)
- [GITHUB-3120](https://github.com/testng-team/testng/issues/3120): ITestNGListenerFactory is broken and never invoked (Krishnan Mahadevan)

## [7.12.0] - 2026-01-22

### Changed

- Updated GitHub Actions test matrix to include JDK 25 and JDK 26 EA (Bartek Florczak)

### Fixed

- [GITHUB-3231](https://github.com/testng-team/testng/issues/3231): TestNG retry is going into infinite loop when the data provider returned object is modified before failure (Bartek Florczak)
- [GITHUB-3236](https://github.com/testng-team/testng/issues/3236): DataProvider parameters are not refreshed on retry when cacheDataForTestRetries=false (Bartek Florczak)
- [GITHUB-3227](https://github.com/testng-team/testng/issues/3227): assertEqualsNoOrder false positive when collection has same size and actual Collection is subset of expected collection (Krishnan Mahadevan)
- [GITHUB-3177](https://github.com/testng-team/testng/issues/3177): Method org.testng.xml.XmlSuite#toXml do not save new properties like "share-thread-pool-for-data-providers" (Krishnan Mahadevan)
- [GITHUB-3179](https://github.com/testng-team/testng/issues/3179): ClassCastException when use shouldUseGlobalThreadPool(true) property (Krishnan Mahadevan)
- [GITHUB-2765](https://github.com/testng-team/testng/issues/2765): Test timeouts using existing Executor now propagate the stack trace to the ThreadTimeoutException (Charlie Hayes)

## [7.11.0] - 2025-02-13

### Fixed

- [GITHUB-3180](https://github.com/testng-team/testng/issues/3180): TestNG testng-failed.xml 'invocation-numbers' values are not calculated correctly with retry and dataproviders (Krishnan Mahadevan)
- [GITHUB-3170](https://github.com/testng-team/testng/issues/3170): Specifying dataProvider and successPercentage causes test to always pass (Krishnan Mahadevan)
- [GITHUB-3028](https://github.com/testng-team/testng/issues/3028): Execution stalls when using "use-global-thread-pool" (Krishnan Mahadevan)
- [GITHUB-3122](https://github.com/testng-team/testng/issues/3122): Update JCommander to 1.83 (Antoine Dessaigne)
- [GITHUB-3135](https://github.com/testng-team/testng/issues/3135): assertEquals on arrays - Failure message is missing information about the array index when an array element is unexpectedly null or non-null (Albert Choi)
- [GITHUB-3140](https://github.com/testng-team/testng/issues/3140): assertEqualsDeep on Sets - Deep comparison was using the wrong expected value
- [GITHUB-3189](https://github.com/testng-team/testng/issues/3189): Incorrect number of ignored tests displayed in the XML results
- [GITHUB-3196](https://github.com/testng-team/testng/issues/3196): support to execlude somes tests in option of command line

## [7.10.2] - 2024-04-28

### Fixed

- [GITHUB-3117](https://github.com/testng-team/testng/issues/3117): ListenerComparator doesn't work (Krishnan Mahadevan)

## [7.10.1] - 2024-04-09

### Fixed

- [GITHUB-3110](https://github.com/testng-team/testng/issues/3110): Update from testng 7.9.0 to 7.10.0 break maven build with junit5 (Krishnan Mahadevan)

## [7.10.0] - 2024-04-07

### Added

- [GITHUB-2874](https://github.com/testng-team/testng/issues/2874): Allow users to define ordering for TestNG listeners (Krishnan Mahadevan)

### Fixed

- [GITHUB-3000](https://github.com/testng-team/testng/issues/3000): Method predecessors lookup and/or method sorting is broken in certain inheritance and naming setups (Krishnan Mahadevan)
- [GITHUB-3095](https://github.com/testng-team/testng/issues/3095): Super class annotated with ITestNGListenerFactory makes derived test class throw TestNGException on execution (Krishnan Mahadevan)
- [GITHUB-3081](https://github.com/testng-team/testng/issues/3081): Discrepancy with combination of (Shared Thread pool and Method Interceptor) (Krishnan Mahadevan)
- [GITHUB-2381](https://github.com/testng-team/testng/issues/2381): Controlling the inclusion of the listener at runtime (Krishnan Mahadevan)
- [GITHUB-3082](https://github.com/testng-team/testng/issues/3082): IInvokedMethodListener Iinvoked method does not return correct instance during @BeforeMethod, @AfterMethod and @AfterClass (Krishnan Mahadevan)
- [GITHUB-3084](https://github.com/testng-team/testng/issues/3084): Document project's PGP artifact signing keys (Krishnan Mahadevan)
- [GITHUB-3079](https://github.com/testng-team/testng/issues/3079): Associate a unique id with every test class object instantiated by TestNG (Krishnan Mahadevan)
- [GITHUB-3040](https://github.com/testng-team/testng/issues/3040): replace the usages of synchronized with ReentrantLock (Krishnan Mahadevan)
- [GITHUB-3041](https://github.com/testng-team/testng/issues/3041): TestNG 7.x DataProvider works in opposite to TestNG 6.x when retrying tests. (Krishnan Mahadevan)
- [GITHUB-3066](https://github.com/testng-team/testng/issues/3066): How to dynamically adjust the number of TestNG threads after IExecutorFactory is deprecated? (Krishnan Mahadevan)
- [GITHUB-3033](https://github.com/testng-team/testng/issues/3033): Moved ant support under own repository https://github.com/testng-team/testng-ant (Julien Herr)
- [GITHUB-3064](https://github.com/testng-team/testng/issues/3064): TestResult lost if failure creating RetryAnalyzer (Krishnan Mahadevan)
- [GITHUB-3048](https://github.com/testng-team/testng/issues/3048): ConcurrentModificationException when injecting values (Krishnan Mahadevan)
- [GITHUB-3050](https://github.com/testng-team/testng/issues/3050): Race condition when creating Guice Modules (Krishnan Mahadevan)
- [GITHUB-3059](https://github.com/testng-team/testng/issues/3059): Support the ability to inject custom listener factory (Krishnan Mahadevan)
- [GITHUB-3045](https://github.com/testng-team/testng/issues/3045): IDataProviderListener - beforeDataProviderExecution and afterDataProviderExecution are called twice in special setup (Krishnan Mahadevan)
- [GITHUB-3038](https://github.com/testng-team/testng/issues/3038): java.lang.IllegalStateException: Results per method should NOT have been empty (Krishnan Mahadevan)
- [GITHUB-3022](https://github.com/testng-team/testng/issues/3022): Remove deprecated JUnit related support in TestNG (Krishnan Mahadevan)

## [7.9.0] - 2023-12-26

### Added

- Added @Inherited to the Listeners annotation, allowing it to be used in forming meta-annotations. (Pavlo Glushchenko)

### Fixed

- [GITHUB-2019](https://github.com/testng-team/testng/issues/2019): Total thread count in testng parallel tests with dataproviders (Krishnan Mahadevan)
- [GITHUB-3006](https://github.com/testng-team/testng/issues/3006): ITestResult injected at @AfterMethod incorrect when a configuration method failed (Krishnan Mahadevan)
- [GITHUB-2980](https://github.com/testng-team/testng/issues/2980): Data Provider Threads configuration in the suite don't match the documentation (Krishnan Mahadevan)
- [GITHUB-3003](https://github.com/testng-team/testng/issues/3003): BeforeClass|AfterClass with inheritedGroups triggers cyclic dependencies (Krishnan Mahadevan)
- [GITHUB-2991](https://github.com/testng-team/testng/issues/2991): Suite attributes map should be thread safe (Krishnan Mahadevan)
- [GITHUB-2974](https://github.com/testng-team/testng/issues/2974): Command line arguments -groups and -excludegroups override defined groups in a suite xml file (dr29bart)
- [GITHUB-2961](https://github.com/testng-team/testng/issues/2961): "Unexpected value: 16" error when multiple beforeMethod config methods with firstTimeOnly property run before a test (Krishnan Mahadevan)
- [GITHUB-2904](https://github.com/testng-team/testng/issues/2904): Add location of docs Github to readme and contributions page (Mohsin Sackeer)
- [GITHUB-2934](https://github.com/testng-team/testng/issues/2934): Parallel Dataproviders & retries causes test result count to be skewed (Krishnan Mahadevan)
- [GITHUB-2925](https://github.com/testng-team/testng/issues/2925): Issue in ITestcontext.getAllTestMethods() with annotation @BeforeSuite (Krishnan Mahadevan)
- [GITHUB-2928](https://github.com/testng-team/testng/issues/2928): The constructor of TestRunner encountered NBC changes in 7.8.0 (Krishnan Mahadevan)
- [GITHUB-581](https://github.com/testng-team/testng/issues/581): Parameters of nested test suites are overridden(Krishnan Mahadevan)
- [GITHUB-727](https://github.com/testng-team/testng/issues/727) : Fixing data races (Krishnan Mahadevan)
- [GITHUB-2913](https://github.com/testng-team/testng/issues/2913): Maps containing nulls can be incorrectly considered equal (Alex Heneveld)

## [7.8.0] - 2023-05-19

### Added

- [GITHUB-2897](https://github.com/testng-team/testng/issues/2897): Not exception but warning if some (not all) of the given test names are not found in suite files. (Bruce Wen)
- [GITHUB-2907](https://github.com/testng-team/testng/issues/2907): Added assertListContains and assertListContainsObject methods to check if specific object present in List (Dmytro Budym)

### Fixed

- [GITHUB-2906](https://github.com/testng-team/testng/issues/2906): Generate testng-results.xml per test suite (Krishnan Mahadevan)
- [GITHUB-2888](https://github.com/testng-team/testng/issues/2888): Skipped Tests with DataProvider appear as failed (Joaquin Moreira)
- [GITHUB-2884](https://github.com/testng-team/testng/issues/2884): Discrepancies with DataProvider and Retry of failed tests (Krishnan Mahadevan)
- [GITHUB-2879](https://github.com/testng-team/testng/issues/2879): Test listeners specified in parent testng.xml file are not included in testng-failed.xml file (Krishnan Mahadevan)
- [GITHUB-2866](https://github.com/testng-team/testng/issues/2866): TestNG.xml doesn't honour Parallel value of a clone (Krishnan Mahadevan)
- [GITHUB-2875](https://github.com/testng-team/testng/issues/2875): JUnitReportReporter should capture the test case output at the test case level
- [GITHUB-2771](https://github.com/testng-team/testng/issues/2771): After upgrading to TestNG 7.5.0, setting ITestResult.status to FAILURE doesn't fail the test anymore (Julien Herr & Krishnan Mahadevan)
- [GITHUB-2862](https://github.com/testng-team/testng/issues/2862): Allow test classes to define "configfailurepolicy" at a per class level (Krishnan Mahadevan)
- [GITHUB-2796](https://github.com/testng-team/testng/issues/2796): Option for onAfterClass to run after @AfterClass (Oliver Hughes)
- [GITHUB-2857](https://github.com/testng-team/testng/issues/2857): XmlTest index is not set for test suites invoked with YAML (Sergei Baranov)
- [GITHUB-2880](https://github.com/testng-team/testng/issues/2880): Before configuration and before invocation set 'SKIP' when beforeMethod is 'skip' (Bob Shi)
- [GITHUB-2886](https://github.com/testng-team/testng/issues/2886): testng-results xml reports config skips from base classes as ignored (Krishnan Mahadevan)

## [7.5.1] - 2023-04-26

### Security

- [GITHUB-2852](https://github.com/testng-team/testng/issues/2852): [SECURITY] Fix Zip Slip Vulnerability, backported to the 7.5 line (Jonathan Leitschuh)

## [7.7.1] - 2022-12-29

### Fixed

- [GITHUB-2854](https://github.com/testng-team/testng/issues/2854): overloaded assertEquals methods do not work from Groovy (Krishnan Mahadevan)

## [7.7.0] - 2022-12-09

### Added

- Added .yml file extension for yaml suite files, previously only .yaml was allowed for yaml (Steven Jubb)
- Ability to provide custom error message for assertThrows\expectThrows methods (Anatolii Yuzhakov)

### Fixed

- [GITHUB-2852](https://github.com/testng-team/testng/issues/2852): [SECURITY] Fix Zip Slip Vulnerability (Jonathan Leitschuh)
- [GITHUB-2792](https://github.com/testng-team/testng/issues/2792): JUnitTestClass sets XmlTest as null when running JUnit 4 Tests using TestNG (Krishnan Mahadevan)
- [GITHUB-2847](https://github.com/testng-team/testng/issues/2847): Deprecate support for running JUnit tests (Krishnan Mahadevan)
- [GITHUB-2844](https://github.com/testng-team/testng/issues/2844): Deprecate support for running Spock Tests (Krishnan Mahadevan)
- [GITHUB-550](https://github.com/testng-team/testng/issues/550): Weird @BeforeMethod and @AfterMethod behaviour with dependsOnMethods (Krishnan Mahadevan)
- [GITHUB-893](https://github.com/testng-team/testng/issues/893): TestNG should provide an Api which allow to find all dependent of a specific test (Krishnan Mahadevan)
- [GITHUB-141](https://github.com/testng-team/testng/issues/141): regular expression in "dependsOnMethods" does not work (Krishnan Mahadevan)
- [GITHUB-2770](https://github.com/testng-team/testng/issues/2770): FileAlreadyExistsException when report is generated (melloware)
- [GITHUB-2825](https://github.com/testng-team/testng/issues/2825): Programmatically Loading TestNG Suite from JAR File Fails to Delete Temporary Copy of Suite File (Steven Jubb)
- [GITHUB-2818](https://github.com/testng-team/testng/issues/2818): Add configuration key for callback discrepancy behavior (Krishnan Mahadevan)
- [GITHUB-2819](https://github.com/testng-team/testng/issues/2819): Ability to retry a data provider in case of failures (Krishnan Mahadevan)
- [GITHUB-2308](https://github.com/testng-team/testng/issues/2308): StringIndexOutOfBoundsException in findClassesInPackage - Surefire/Maven - JDK 11 fails (Krishnan Mahadevan)
- GITHUB:2788: TestResult.isSuccess() is TRUE when test fails due to expectedExceptions (Krishnan Mahadevan)
- [GITHUB-2800](https://github.com/testng-team/testng/issues/2800): Running Test Classes with Inherited @Factory and @DataProvider Annotated Non-Static Methods Fail (Krishnan Mahadevan)
- [GITHUB-2780](https://github.com/testng-team/testng/issues/2780): Use SpotBugs instead of abandoned FindBugs
- [GITHUB-2801](https://github.com/testng-team/testng/issues/2801): JUnitReportReporter is too slow
- [GITHUB-2807](https://github.com/testng-team/testng/issues/2807): buildStackTrace should be fail-safe (Sergey Chernov)
- [GITHUB-2830](https://github.com/testng-team/testng/issues/2830): TestHTMLReporter parameter toString should be fail-safe (Sergey Chernov)
- [GITHUB-2798](https://github.com/testng-team/testng/issues/2798): Parallel executions coupled with retry analyzer results in duplicate retry analyzer instances being created (Krishnan Mahadevan)

## [7.6.1] - 2022-07-04

### Fixed

- [GITHUB-2761](https://github.com/testng-team/testng/issues/2761): Exception: ERROR java.nio.file.NoSuchFileException: /tmp/testngXmlPathInJar-15086412835569336174 (Krishnan Mahadevan)

## [7.6.0] - 2022-05-18

### Added

- [GITHUB-2724](https://github.com/testng-team/testng/issues/2724): DataProvider: possibility to unload dataprovider class, when done with it (Dzmitry Sankouski)

### Fixed

- [GITHUB-2741](https://github.com/testng-team/testng/issues/2741): Show fully qualified name of the test instead of just the function name for better readability of test output.(Krishnan Mahadevan)
- [GITHUB-2725](https://github.com/testng-team/testng/issues/2725): Honour custom attribute values in TestNG default reports (Krishnan Mahadevan)
- [GITHUB-2726](https://github.com/testng-team/testng/issues/2726): @AfterClass config method is executed for EACH @Test method when parallel == methods (Krishnan Mahadevan)
- [GITHUB-2752](https://github.com/testng-team/testng/issues/2752): TestListener is being lost when implenting both IClassListener and ITestListener (Krishnan Mahadevan)
- [GITHUB-217](https://github.com/testng-team/testng/issues/217): Configure TestNG to fail when there's a failure in data provider (Krishnan Mahadevan)
- [GITHUB-2743](https://github.com/testng-team/testng/issues/2743): SuiteRunner could not be initial by default Configuration (Nan Liang)
- [GITHUB-2729](https://github.com/testng-team/testng/issues/2729): beforeConfiguration() listener method should be invoked for skipped configurations as well(Nan Liang)
- assertEqualsNoOrder for Collection and Iterators size check was missing (Adam Kaczmarek)
- [GITHUB-2709](https://github.com/testng-team/testng/issues/2709): Testnames not working together with suites in suite (Martin Aldrin)
- [GITHUB-2704](https://github.com/testng-team/testng/issues/2704): IHookable and IConfigurable callback discrepancy (Krishnan Mahadevan)
- [GITHUB-2637](https://github.com/testng-team/testng/issues/2637): Upgrade to JDK11 as the minimum JDK requirements (Krishnan Mahadevan)
- [GITHUB-2734](https://github.com/testng-team/testng/issues/2734): Keep the initial order of listeners (Andrei Solntsev)
- [GITHUB-2359](https://github.com/testng-team/testng/issues/2359): Testng @BeforeGroups is running in parallel with testcases in the group (Anton Velma)
- Possible StringIndexOutOfBoundsException in XmlReporter (Anton Velma)
- [GITHUB-2754](https://github.com/testng-team/testng/issues/2754): @AfterGroups is executed for each "finished" group when it has multiple groups defined (Anton Velma)

## [7.5] - 2022-01-06

### Added

- Decouple configuration unit tests from main suite (Dzmitry Sankouski).
- [GITHUB-2564](https://github.com/testng-team/testng/issues/2564): Added license files as META-INF/LICENSE.txt within the released jar

### Changed

- [GITHUB-2564](https://github.com/testng-team/testng/issues/2564): Source code is split into several modules for better modularity in the future (for now only a combined jar is released as it was before)
- [GITHUB-2564](https://github.com/testng-team/testng/issues/2564): Added pax-exam-based OSGi test to verify the manifest
- [GITHUB-2564](https://github.com/testng-team/testng/issues/2564): Migrated the build to Gradle 7.0.2

### Fixed

- [GITHUB-2701](https://github.com/testng-team/testng/issues/2701): Bump gradle version to 7.3.3 to support java17 build (ZhangJian He)
- [GITHUB-2646](https://github.com/testng-team/testng/issues/2646): Streamline Logging Across TestNG (Krishnan Mahadevan)
- [GITHUB-2658](https://github.com/testng-team/testng/issues/2658): Inheritance + dependsOnMethods (Krishnan Mahadevan)
- [GITHUB-2664](https://github.com/testng-team/testng/issues/2664): Order for DependsOnGroups has changed after TestNg 7.4.0 (Krishnan Mahadevan)
- [GITHUB-2501](https://github.com/testng-team/testng/issues/2501): TestNG 7.4.0 throws an exception "sun.net.www.protocol.file.FileURLConnection cannot be cast to java.net.HttpURLConnection" when xml file contain "ENTITY SYSTEM" grammer (Krishnan Mahadevan)
- [GITHUB-2693](https://github.com/testng-team/testng/issues/2693): TestNG ignores 'dataproviderthreadcount' CLA (Krishnan Mahadevan)
- [GITHUB-2685](https://github.com/testng-team/testng/issues/2685): TestInvoker should clear Thread.interrupted flag before calling ITestListeners (Roman Morskyi)
- [GITHUB-2684](https://github.com/testng-team/testng/issues/2684): AfterGroups config annotation does not consider retries for tests (Roman Morskyi)
- [GITHUB-2689](https://github.com/testng-team/testng/issues/2689): Yaml parser: implement loadClasses flag (Dzmitry Sankouski)
- [GITHUB-2676](https://github.com/testng-team/testng/issues/2676): NPE is triggered when working with ITestObjectFactory (Krishnan Mahadevan)
- [GITHUB-2674](https://github.com/testng-team/testng/issues/2674): Run onTestSkipped for each value from data provider (Krishnan Mahadevan)
- [GITHUB-2672](https://github.com/testng-team/testng/issues/2672): Log real stacktrace when test times out. (cdalexndr)
- [GITHUB-2669](https://github.com/testng-team/testng/issues/2669): A failed retry with ITestContext will lose the ITestContext. (Nan Liang)
- [GITHUB-2643](https://github.com/testng-team/testng/issues/2643): assertEquals(Set,Set) now ignores ordering as it did before. (Elis Edlund)
- [GITHUB-2653](https://github.com/testng-team/testng/issues/2653): Assert methods requires casting since TestNg 7.0 for mixed boxed and unboxed primitives in assertEquals.
- [GITHUB-2229](https://github.com/testng-team/testng/issues/2229): Restore @BeforeGroups and @AfterGroups Annotations functionality (Krishnan Mahadevan)
- [GITHUB-2563](https://github.com/testng-team/testng/issues/2563): Skip test if its data provider provides no data (Krishnan Mahadevan)
- [GITHUB-2535](https://github.com/testng-team/testng/issues/2535): TestResult.getEndMillis() returns 0 for skipped configuration - after upgrading testng to 7.0 + (Krishnan Mahadevan)
- [GITHUB-2638](https://github.com/testng-team/testng/issues/2638): "[WARN] Ignoring duplicate listener" appears when running .xml suite with `<listeners>` and `<suite-files>` (Krishnan Mahadevan)
- [GITHUB-1297](https://github.com/testng-team/testng/issues/1297): Passed configuration methods appear in testng-failed.xml, when failure was after passed test (Dzmitry Sankouski)
- [GITHUB-2536](https://github.com/testng-team/testng/issues/2536): Problems with Nested Test Classes (Krishnan Mahadevan)
- [GITHUB-2558](https://github.com/testng-team/testng/issues/2558):Make IExecutionListener, ITestListener, IInvokedMethodListener, IConfigurationListener, ISuiteListener finish method with reverse order (dianny)
- [GITHUB-2532](https://github.com/testng-team/testng/issues/2532): Apply commandline switches for suites in jar files (Dzmitry Sankouski).
- [GITHUB-2558](https://github.com/testng-team/testng/issues/2558): Make IExecutionListener, ITestListener, IInvokedMethodListener, IConfigurationListener, ISuiteListener execute in the order of insertion (Krishnan Mahadevan)
- [GITHUB-2611](https://github.com/testng-team/testng/issues/2611): Config Failures not included in testng-failed.xml when its part of a different test class (Krishnan Mahadevan)
- [GITHUB-2613](https://github.com/testng-team/testng/issues/2613): Ignored Tests are not retrieved for a mixed test class (test with enabled, disabled and ignored test method) (Krishnan Mahadevan)
- [GITHUB-849](https://github.com/testng-team/testng/issues/849): Performance improvement by fixing hashCode (testn & Vladimir Sitnikov)
- [GITHUB-2570](https://github.com/testng-team/testng/issues/2570): Use Guice injector for instantiate IRetryAnalyzer (Krishnan Mahadevan)
- use proper instances for beforeClass callback when different instances collide on hash codes
- Fix parallel and configfailurepolicy parsing in tr_TR locale
- Wrong results from GuiceBasedObjectDispenser when there's Object#hashCode collision
- [GITHUB-2576](https://github.com/testng-team/testng/issues/2576): Guice 5.0 drops no-aop variant, so TestNG should probably upgrade and avoid no-aop dependency (Nan Liang)
- [GITHUB-2566](https://github.com/testng-team/testng/issues/2566): Reporter#getOutput(ITestResult tr) uses Map.get(tr.hashCode()) which might result in surprising results (Krishnan Mahadevan)
- [GITHUB-2565](https://github.com/testng-team/testng/issues/2565): Dataprovider only supporting a raw type for Iterator return type (Krishnan Mahadevan)
- [GITHUB-2557](https://github.com/testng-team/testng/issues/2557): Flaky test: ThreadAffinityTest#testThreadAffinity (Krishnan Mahadevan)
- [GITHUB-2567](https://github.com/testng-team/testng/issues/2567): MethodHelper#CANONICAL_NAME_CACHE is never reset, so it could result in a memory leak (Krishnan Mahadevan)
- [GITHUB-2540](https://github.com/testng-team/testng/issues/2540): assertEquals(Collection) need check order (Stuart Marks & Julien Herr)
- [GITHUB-2360](https://github.com/testng-team/testng/issues/2360): Groovy 3 internal generated methods are detected as test methods (Ian Springer)
- [GITHUB-2522](https://github.com/testng-team/testng/issues/2522): TestNG 7.4.0 Can not skip test through listener (Nan Liang)
- [GITHUB-2529](https://github.com/testng-team/testng/issues/2529): Link to testng.xml in CONTRIBUTING.md file was dead
- [GITHUB-2521](https://github.com/testng-team/testng/issues/2521): The method has a separate string with 'invocation-number' parameters for each failure (does not group) in 'testng-failed.xml' file for DataProvider + Factory (Pavel Sakharchuk)
- [GITHUB-2426](https://github.com/testng-team/testng/issues/2426): New feature TestNG - getFactoryMethodParamsInfo on ConfigurationMethod (Krishnan Mahadevan)
- [GITHUB-2517](https://github.com/testng-team/testng/issues/2517): Factory data-provider parameters not displayed in 'testng-failed.xml' file (Pavel Sakharchuk)
- [GITHUB-279](https://github.com/testng-team/testng/issues/279): Guice dependency injection into listeners and reporters (Krishnan Mahadevan)
- [GITHUB-2504](https://github.com/testng-team/testng/issues/2504): Data provider data cannot be populated to onTestStart hook when BeforeMethod is failed (Krishnan Mahadevan)
- [GITHUB-2489](https://github.com/testng-team/testng/issues/2489): Hierarchical base- and test-class @AfterClass methods out of order using groups (Krishnan Mahadevan)
- [GITHUB-2493](https://github.com/testng-team/testng/issues/2493): Avoid NPE from TextReporter execution when a dataprovider method provides null (baflQA)
- [GITHUB-2483](https://github.com/testng-team/testng/issues/2483): Asymmetric not equals (cdalexndr)
- [GITHUB-2486](https://github.com/testng-team/testng/issues/2486): assertSame/assertNotSame broken after [GITHUB-2296](https://github.com/testng-team/testng/issues/2296) (Vitalii Diravka)
- [GITHUB-2490](https://github.com/testng-team/testng/issues/2490): assertNotEquals returns fast when argument is null, not calling equals(Object) (Anindya Roy)
- [GITHUB-2500](https://github.com/testng-team/testng/issues/2500): Mention in assertEqualsNoOrder doc that arrays are not compared deeply (Marcono1234)
- [GITHUB-2544](https://github.com/testng-team/testng/issues/2544): TestNG Retry Fails on complex data-provider arguments (Anindya Roy)

## [7.4.0] - 2021-02-27

### Added

- [GITHUB-2459](https://github.com/testng-team/testng/issues/2459): Support configurable start time - emailable report (Barry Evans)
- [GITHUB-2456](https://github.com/testng-team/testng/issues/2456): Add onDataProviderFailure listener (Krishnan Mahadevan)
- [GITHUB-2407](https://github.com/testng-team/testng/issues/2407): Adds "overrideIncludedMethods" to the global config as a command-line argument, which excludes explicitly included test methods if they belong to any excluded groups (Nikhil Suri)
- [GITHUB-2385](https://github.com/testng-team/testng/issues/2385): Make @Listeners can work for implemented interfaces and Inherited class (Nan Liang)

### Fixed

- [GITHUB-2467](https://github.com/testng-team/testng/issues/2467): XmlTest does not copy the xmlClasses during clone (C.V.Aditya)
- [GITHUB-2469](https://github.com/testng-team/testng/issues/2469): Parameters added in XmlTest during AlterSuiteListener not available in SuiteListener (C.V.Aditya)
- [GITHUB-2296](https://github.com/testng-team/testng/issues/2296): Fix for assertEquals not working for sets as order is not guaranteed. (Prashant Maroti)
- [GITHUB-2465](https://github.com/testng-team/testng/issues/2465): Fix bux where Strings.join returns empty String
- [GITHUB-1632](https://github.com/testng-team/testng/issues/1632): throwing SkipException sets iTestResult status to Failure instead of Skip (Julien Herr & Krishnan Mahadevan)
- [GITHUB-2445](https://github.com/testng-team/testng/issues/2445): NPE in FailedReporter.java With Tests Created in Factory (Arham Jain)
- [GITHUB-2428](https://github.com/testng-team/testng/issues/2428): Configuration methods have the same test class instance when @Factory is being used (Nan Liang)
- [GITHUB-2440](https://github.com/testng-team/testng/issues/2440): Fixed an issue when case timeout returned an incorrect exception and effect the next other test case (Yao Ma)
- [GITHUB-2432](https://github.com/testng-team/testng/issues/2432): Rework MethodInheritance.fixMethodInheritance to "soft" dependencies (Krishnan Mahadevan)
- [GITHUB-2429](https://github.com/testng-team/testng/issues/2429): Seggregate Dependency Injection out as a clear implementation (Krishnan Mahadevan)
- [GITHUB-2435](https://github.com/testng-team/testng/issues/2435): getParameterIndex() always return 0 in test listener
- [GITHUB-2406](https://github.com/testng-team/testng/issues/2406): TestNG 7.3.0 transitive vulnerability CVE-2020-11022 and CVE-2020-11023 due to JQuery 3.4.1  (Krishnan Mahadevan)
- [GITHUB-2405](https://github.com/testng-team/testng/issues/2405): Regression: Using TestNG via Maven breaks when optional Guice dependency is unavailable (Krishnan Mahadevan)
- [GITHUB-2427](https://github.com/testng-team/testng/issues/2427): Guice module (suite parent-module and test module) configure() method is called multiple times (Jacek Centkowski)
- [GITHUB-2419](https://github.com/testng-team/testng/issues/2419): TestNG JUnit reports are not valid if system output contains XML tags (Lorenzo Orsatti)
- [GITHUB-188](https://github.com/testng-team/testng/issues/188): suite parallel="methods" does not work when there are multiple `<test>` tags in the testng.xml (Krishnan Mahadevan)
- [GITHUB-346](https://github.com/testng-team/testng/issues/346): When a method is annotated with both BeforeGroups and AfterGroups only AfterGroup is executed (Krishnan Mahadevan)
- [GITHUB-2403](https://github.com/testng-team/testng/issues/2403): Suite.xml files attempt to make web request when suite references standard TestNG DTD using HTTP (Krishnan Mahadevan)
- [GITHUB-2053](https://github.com/testng-team/testng/issues/2053): MethodHelper.collectAndOrderMethods() Hangs when Parallel Instance and dependsOnGroups (Krishnan Mahadevan)
- [GITHUB-2400](https://github.com/testng-team/testng/issues/2400): BeforeClass/Method (and AfterClass/Method) configuration methods that override default methods are invoked multiple times (Krishnan Mahadevan)
- [GITHUB-2396](https://github.com/testng-team/testng/issues/2396): @Ignore on method level doesn't work as expected (Krishnan Mahadevan)
- [GITHUB-2382](https://github.com/testng-team/testng/issues/2382): TestNG version should be specified in MANIFEST.MF (Krishnan Mahadevan)
- [GITHUB-2096](https://github.com/testng-team/testng/issues/2096): 7.0.0-beta6 memory issues (regression) (Krishnan Mahadevan)
- [GITHUB-2355](https://github.com/testng-team/testng/issues/2355): TestNG creates multiple Guice Module Instances (Krishnan Mahadevan)
- [GITHUB-2374](https://github.com/testng-team/testng/issues/2374): Add file name to the warning message (Krishnan Mahadevan)
- [GITHUB-2321](https://github.com/testng-team/testng/issues/2321): -Dtestng.thread.affinity=true do not work when running multiple instance of test in parallel (Nan Liang)
- [GITHUB-2363](https://github.com/testng-team/testng/issues/2363): JS error when switching theme (Krishnan Mahadevan)
- [GITHUB-2361](https://github.com/testng-team/testng/issues/2361): No way to enforce @Test(singleThreaded = true) when test defined in base class (Krishnan Mahadevan)
- [GITHUB-2343](https://github.com/testng-team/testng/issues/2343): Injectors are not reused when they share the same set of modules (Krishnan Mahadevan)
- [GITHUB-2346](https://github.com/testng-team/testng/issues/2346): ITestResult attributes are null when retrieved by Listener onTestStart if test fails at BeforeMethod (Krishnan Mahadevan)
- [GITHUB-2357](https://github.com/testng-team/testng/issues/2357): TestNG 7.3.0 transitive dependencies

## [7.3.0] - 2020-08-07

### Added

- [GITHUB-2315](https://github.com/testng-team/testng/issues/2315): TextReporter console output does not nicely print native array data parameters (James Sassano)
- Deprecate org.testng.ReporterConfig (Julien Herr)

### Fixed

- [GITHUB-2328](https://github.com/testng-team/testng/issues/2328): Add ability to get test method for which configuration method was called (Krishnan Mahadevan)
- [GITHUB-2327](https://github.com/testng-team/testng/issues/2327): Parameters not present on skipped Test (Eric Kubenka)
- [GITHUB-2232](https://github.com/testng-team/testng/issues/2232): Null Pointer Exception in ConfigInvoker.setMethodInvocationFailure (Krishnan Mahadevan)
- [GITHUB-2312](https://github.com/testng-team/testng/issues/2312): IAnnotationTransformer called multiple time Discrepancy between 6.x and 7.x (Krishnan Mahadevan)
- [GITHUB-2301](https://github.com/testng-team/testng/issues/2301): Add support for object-based reporter configurations (Scott Babcock)
- [GITHUB-2300](https://github.com/testng-team/testng/issues/2300): Vulnerable Dependency: Please upgrade JCommander to 1.75 or above (Krishnan Mahadevan)
- [GITHUB-2182](https://github.com/testng-team/testng/issues/2182): Removed exception catching as valid behaviour
- [GITHUB-2273](https://github.com/testng-team/testng/issues/2273): Use SPI to load Guice modules (Bartosz Popiela)
- [GITHUB-2280](https://github.com/testng-team/testng/issues/2280): Prevent Retry from happening endlessly (Paweł Nadolski)
- [GITHUB-553](https://github.com/testng-team/testng/issues/553):  Better error message when dealing with classes having both Constructor and Factory methods (Krishnan Mahadevan)
- [GITHUB-2267](https://github.com/testng-team/testng/issues/2267): RetryAnalyzer is not set properly and consistent when using dataprovider (Eric Kubenka)
- [GITHUB-2266](https://github.com/testng-team/testng/issues/2266): Support Test retries via Callbacks (Krishnan Mahadevan)
- [GITHUB-2209](https://github.com/testng-team/testng/issues/2209): @Before and @After are not executed as expected when a combination of class and method level grouping is applied (Krishnan Mahadevan)
- [GITHUB-2259](https://github.com/testng-team/testng/issues/2259): Missing configuration for ServiceLoader Listeners (Krishnan Mahadevan)
- [GITHUB-2257](https://github.com/testng-team/testng/issues/2257): Facilitate retry of configuration methods via call backs (Krishnan Mahadevan)
- [GITHUB-2223](https://github.com/testng-team/testng/issues/2223): testng 7.1.0 java.lang.ClassNotFoundException: com.google.inject.Stage (Krishnan Mahadevan)
- [GITHUB-217](https://github.com/testng-team/testng/issues/217): Configure TestNG to fail when all tests are skipped (Krishnan Mahadevan)
- [GITHUB-2255](https://github.com/testng-team/testng/issues/2255): Ensure test method parameters are visible in BeforeMethod config method (Krishnan Mahadevan)
- [GITHUB-2251](https://github.com/testng-team/testng/issues/2251): NullPointerException at test with timeOut (Krishnan Mahadevan)
- [GITHUB-2249](https://github.com/testng-team/testng/issues/2249): Not abstract super-classes mess up test run order (Sergii Kim)
- [GITHUB-2195](https://github.com/testng-team/testng/issues/2195): NPE Using groups and @Before/@AfterMethod with alwaysRun and dependsOnMethods (Tomas & Julien Herr)
- [GITHUB-2238](https://github.com/testng-team/testng/issues/2238): Parameter values should be overridable from JVM arguments (Krishnan Mahadevan)
- [GITHUB-2231](https://github.com/testng-team/testng/issues/2231): Incorrect hierarchy in testng.xml file created programmatically and failed to run. (Krishnan Mahadevan)
- [GITHUB-2235](https://github.com/testng-team/testng/issues/2235): expectedExceptions mismatch because of wrapping (Krishnan Mahadevan)
- [GITHUB-2220](https://github.com/testng-team/testng/issues/2220): ITestListener's methods get called multiple times for one test, when @Listeners annotation is used in multiple test classes (Krishnan Mahadevan)
- [GITHUB-2211](https://github.com/testng-team/testng/issues/2211): assertEquals for Map sometimes does not use provided message (Krishnan Mahadevan)
- [GITHUB-1632](https://github.com/testng-team/testng/issues/1632): throwing SkipException sets iTestResult status to Failure instead of Skip (Krishnan Mahadevan)
- [GITHUB-2207](https://github.com/testng-team/testng/issues/2207): Allow dependency injector factory to be configured via args (dmikhievich)
- [GITHUB-2193](https://github.com/testng-team/testng/issues/2193): Ignore local url for DTD security check (Li.Zhao & Julien Herr)
- [GITHUB-1968](https://github.com/testng-team/testng/issues/1968): Upgrade to Gradle 6.0

## [7.1.0] - 2019-12-24

### Added

- [GITHUB-2199](https://github.com/testng-team/testng/issues/2199): Allow users to provide their own Injector for Dependency Injection (Krishnan Mahadevan)
- [GITHUB-2111](https://github.com/testng-team/testng/issues/2111): Provide an interceptor for Data Provider (Krishnan Mahadevan)
- [GITHUB-2118](https://github.com/testng-team/testng/issues/2118): Default assertion message (Jiong Fu)

### Fixed

- [GITHUB-2180](https://github.com/testng-team/testng/issues/2180): Write scenario details for Retried tests (Devendra Raju K)
- [GITHUB-2124](https://github.com/testng-team/testng/issues/2124): JUnit Report should contain the output of org.testng.Reporter (Krishnan Mahadevan)
- [GITHUB-2171](https://github.com/testng-team/testng/issues/2171): Ability to embed attachments and make them available on TestNG XML report (Krishnan Mahadevan)
- [GITHUB-2152](https://github.com/testng-team/testng/issues/2152): Multiple Test Groups Causing @BeforeMethod and @AfterMethod to be called multiple times for a single test (Krishnan Mahadevan)
- [GITHUB-2172](https://github.com/testng-team/testng/issues/2172): Suite summary report table issue with EmailableReporter2.java (Devendra Raju K)
- [GITHUB-2148](https://github.com/testng-team/testng/issues/2148): TestNG - configfailurepolicy=“continue” is not working for retried test (Krishnan Mahadevan)
- [GITHUB-2163](https://github.com/testng-team/testng/issues/2163): Test is executed infinite number of times when the data provider returns a new object (Krishnan Mahadevan)
- [GITHUB-2157](https://github.com/testng-team/testng/issues/2157): NullPointerException occurs when a Retried test has an exception in DataProvider (Krishnan Mahadevan)
- [GITHUB-2150](https://github.com/testng-team/testng/issues/2150): Upgraded jQuery from 1.7.1 to 3.4.1 to resolve reported prototype pollution vulnerability
- [GITHUB-2149](https://github.com/testng-team/testng/issues/2149): Handle NoClassDefFoundError when classloader fails to load a class
- [GITHUB-1709](https://github.com/testng-team/testng/issues/1709): @Ignore doesn't work when used on child class, and parent has multiple @Test methods (Krishnan Mahadevan)
- [GITHUB-2080](https://github.com/testng-team/testng/issues/2080): Wrong text for assertTrue (Jiong Fu)
- [GITHUB-2078](https://github.com/testng-team/testng/issues/2078): Xml to Yaml does not capture the dependency definition in generated Yaml File (Jiong Fu)

## [7.0.0] - 2019-08-17

### Added

- [GITHUB-2003](https://github.com/testng-team/testng/issues/2003): Add to @Test interface fields IDs and issues (Krishnan Mahadevan)
- Upgrade to gradle5
- Added a method in Assertion class to allow downstream TestNG consumers to override the error message (Ryan Laseter)
- Remove raw type warnings in ConversionUtils
- Expose Graph Visualisation representation for users to build real-time debugging tools (Krishnan Mahadevan)
- TestNG now guarantees thread-affinity for methods that use either preserve-order (or) dependsOnMethods (Krishnan Mahadevan)
- Removed deprecated methods across TestNG (Krishnan Mahadevan)
- Upgrade TestNG to start needing at-least JDK8 (Krishnan Mahadevan)
- Removed deprecated attributes from annotations (Julien Herr)
- Support all JSR-223 compatible script engine
- [GITHUB-2105](https://github.com/testng-team/testng/issues/2105): Include assertEquals(Map, Map) into Assert class

### Fixed

- [GITHUB-2137](https://github.com/testng-team/testng/issues/2137): Issue with Priority with too low values (Krishnan Mahadevan)
- [GITHUB-2138](https://github.com/testng-team/testng/issues/2138): Factory calls are not intercepted by IAnnotationTransformer (Krishnan Mahadevan)
- [GITHUB-2121](https://github.com/testng-team/testng/issues/2121): SoftAssert: allow custom message (dr29bart)
- [GITHUB-2110](https://github.com/testng-team/testng/issues/2110): NPE is thrown when running with Thread affinity (Krishnan Mahadevan)
- [GITHUB-2069](https://github.com/testng-team/testng/issues/2069): JUnit TestSuite not handled correctly in Reports (Krishnan Mahadevan)
- [GITHUB-2075](https://github.com/testng-team/testng/issues/2075): Thread interrupt flag persists between test methods (Krishnan Mahadevan)
- [GITHUB-2074](https://github.com/testng-team/testng/issues/2074): Thread Interrupted when using expectedException (Krishnan Mahadevan)
- [GITHUB-2802](https://github.com/testng-team/testng/issues/2802): some methods' javadoc in ISuiteListener and ITestListener is misleading (Yehui Wang)
- [GITHUB-2061](https://github.com/testng-team/testng/issues/2061): java.util.ConcurrentModificationException after registration of SuiteListener at the runtime (Krishnan Mahadevan)
- [GITHUB-2055](https://github.com/testng-team/testng/issues/2055): It's not possible to register a new ITestListener at the runtime (Krishnan Mahadevan)
- [GITHUB-1035](https://github.com/testng-team/testng/issues/1035): @BeforeClass not executed in parallel when parallel="instances" (Krishnan Mahadevan)
- [GITHUB-2043](https://github.com/testng-team/testng/issues/2043): IConfigurationListener is not executed and IDataProviderListener is not added to the list of the listeners if a new listener is added at the runtime (Krishnan Mahadevan)
- [GITHUB-1835](https://github.com/testng-team/testng/issues/1835): Configurable ThreadPoolExecutor (Krishnan Mahadevan)
- [GITHUB-1691](https://github.com/testng-team/testng/issues/1691): Support reading data provider information from Class level @Test annotation (Krishnan Mahadevan)
- [GITHUB-326](https://github.com/testng-team/testng/issues/326): When group-by-instances is set to true the instances created by @Factory does not run in parallel (Krishnan Mahadevan)
- [GITHUB-1930](https://github.com/testng-team/testng/issues/1930): Running testng-failed.xml correctly runs failed test in child class but incorrectly runs all tests in base class (Krishnan Mahadevan)
- [GITHUB-1987](https://github.com/testng-team/testng/issues/1987): Retrieve the data provider method's reference from the test method (Krishnan Mahadevan)
- [GITHUB-1976](https://github.com/testng-team/testng/issues/1976): Add a proper message when types are missing during annotation parsing (Krishnan Mahadevan)
- [GITHUB-2000](https://github.com/testng-team/testng/issues/2000): ThreadPoolExecutor ConcurrentModificationException (Krishnan Mahadevan)
- [GITHUB-2022](https://github.com/testng-team/testng/issues/2022): Suite.xml files make web request when suite uses DTD over HTTPS (Krishnan Mahadevan)
- [GITHUB-2017](https://github.com/testng-team/testng/issues/2017): Don't use child injectors for Guice tests (Joe Barnett)
- [GITHUB-2009](https://github.com/testng-team/testng/issues/2009): Test Timeout not respected in parallel="methods" mode (Krishnan Mahadevan)
- [GITHUB-2008](https://github.com/testng-team/testng/issues/2008): Preserve parameters in each class and not all of the test parameters
- [GITHUB-1981](https://github.com/testng-team/testng/issues/1981): Fixes NPE in Assert.assertEquals when an array contains null (Maneesh MS)
- [GITHUB-165](https://github.com/testng-team/testng/issues/165): @AfterGroups is not executed when group member fails or is skipped (Krishnan Mahadevan)
- [GITHUB-118](https://github.com/testng-team/testng/issues/118): @BeforeGroups only called if group is specified explicitly (Krishnan Mahadevan)
- [GITHUB-182](https://github.com/testng-team/testng/issues/182): Inherited test methods do not get expected group behavior (Krishnan Mahadevan)
- [GITHUB-1988](https://github.com/testng-team/testng/issues/1988): Add Automatic-Module-Name to MANIFEST.MF (Krishnan Mahadevan)
- [GITHUB-1985](https://github.com/testng-team/testng/issues/1985): Custom "IMethodSelector" implementation doesn't filter methods properly (Krishnan Mahadevan)
- [GITHUB-993](https://github.com/testng-team/testng/issues/993):  Handle null test names from ITest implementation (Krishnan Mahadevan)
- [GITHUB-1942](https://github.com/testng-team/testng/issues/1942): assertDeepEquals takes long time to evaluate size mismatch (Kumaran Bharathan)
- [GITHUB-1967](https://github.com/testng-team/testng/issues/1967): IInvokedMethod ITestResult status set as -1 for test methods skipped due to config failure (Krishnan Mahadevan)
- [GITHUB-1952](https://github.com/testng-team/testng/issues/1952): Provide a TestNGListener that can be invoked when a test fails due to a timeout (Krishnan Mahadevan)
- [GITHUB-1953](https://github.com/testng-team/testng/issues/1953): TestNG throws a misleading error when @Factory method returns empty array. (Krishnan Mahadevan)
- [GITHUB-1946](https://github.com/testng-team/testng/issues/1946): Retry analyzer does not work properly when coupled with a data provider (Krishnan Mahadevan)
- [GITHUB-1924](https://github.com/testng-team/testng/issues/1924): Testclass instantiation fails when both no-arg constructor and factory method present (Krishnan Mahadevan)
- [GITHUB-1935](https://github.com/testng-team/testng/issues/1935): Wrong text for assertEquals (Krishnan Mahadevan)
- [GITHUB-1931](https://github.com/testng-team/testng/issues/1931): [NPE] Reporter org.testng.reporters.jq.Main failed (Krishnan Mahadevan, Oleg Shaburov)
- [GITHUB-1480](https://github.com/testng-team/testng/issues/1480): Parallel=methods not working when tests have different priorities set (Micah Lapping-Carr)
- [GITHUB-1041](https://github.com/testng-team/testng/issues/1041): Factory data-provider parameters not displayed in test-result (Krishnan Mahadevan)
- [GITHUB-1901](https://github.com/testng-team/testng/issues/1901): The overall reported Test time for suite containing parallel tests should be max(tests_times) (Krishnan Mahadevan)
- [GITHUB-1893](https://github.com/testng-team/testng/issues/1893): Streamline invocation of "init" method within TestResult to be private (Krishnan Mahadevan)
- [GITHUB-1892](https://github.com/testng-team/testng/issues/1892): Configurable InvokedMethodListener (Krishnan Mahadevan)
- [GITHUB-435](https://github.com/testng-team/testng/issues/435): Apply `<packages>` at suite level to all tests (Siegmar Alber)
- [GITHUB-1870](https://github.com/testng-team/testng/issues/1870): Fix Ambiguous behavior of IInvokedMethodListener by clarifying javadocs(Krishnan Mahadevan)
- [GITHUB-1878](https://github.com/testng-team/testng/issues/1878): Provide visibility into the actual method (config/test) that caused a downstream test to be skipped (Krishnan Mahadevan)
- [GITHUB-1883](https://github.com/testng-team/testng/issues/1883): Establish Listener invocation order as tests for documentation purposes (Krishnan Mahadevan)
- TestNG doesn't use the searchable loaders for JUnit tests (Igor Ignatev)
- [GITHUB-1880](https://github.com/testng-team/testng/issues/1880): @AfterGroups(alwaysRun = true) is not called if there is an exception in @BeforeGroups (Krishnan Mahadevan)
- [GITHUB-1874](https://github.com/testng-team/testng/issues/1874): Prevent circular dependency error when suite includes different methods from same class (Krishnan Mahadevan)
- [GITHUB-1863](https://github.com/testng-team/testng/issues/1863): IMethodInterceptor will be invoked twice when listener implements both ITestListener and IMethodInterceptor via eclipse execution way.(Bin Wu)
- [GITHUB-1865](https://github.com/testng-team/testng/issues/1865): testngXmlPathInJar- cannot be cleared when vm terminate(Yehui Wang)
- [GITHUB-1850](https://github.com/testng-team/testng/issues/1850): Parser returns a wrong structure when parent suite has duplicate child suites (Chao Qin)
- [GITHUB-1803](https://github.com/testng-team/testng/issues/1803): Added new methods for comparing float and double arrays with delta (Atul Agrawal)
- [GITHUB-1661](https://github.com/testng-team/testng/issues/1661): Fixed Assert logic for two dimensional arrays (Atul Agrawal)
- [GITHUB-1734](https://github.com/testng-team/testng/issues/1734): Added unit tests for NaN, Max, Min, Positive and Negative infinity (Atul Agrawal)
- [GITHUB-1740](https://github.com/testng-team/testng/issues/1740): Bumped up artifact version of dependencies for java 8 support (Atul Agrawal)
- [GITHUB-1834](https://github.com/testng-team/testng/issues/1834): Ensure group dependency defined via suite xml is considered (Krishnan Mahadevan)
- [GITHUB-574](https://github.com/testng-team/testng/issues/574): String cannot be cast to Integer if log property is set in maven pom.xml (Krishnan Mahadevan)
- [GITHUB-1402](https://github.com/testng-team/testng/issues/1402): TestNG reporting - Quickly identify Retry-d Tests (Krishnan Mahadevan)
- [GITHUB-1697](https://github.com/testng-team/testng/issues/1697): Need ability to mark "failed but up for retry" tests differently from "skipped" tests (Krishnan Mahadevan)
- [GITHUB-1810](https://github.com/testng-team/testng/issues/1810): Nullpointer exception when running TestNG tests from CMD (Krishnan Mahadevan)
- [GITHUB-1590](https://github.com/testng-team/testng/issues/1590): Started configuration method has wrong status and end time during execution (Krishnan Mahadevan)
- [GITHUB-298](https://github.com/testng-team/testng/issues/298): Avoid Javascript errors in function name when suite name has special characters (Krishnan Mahadevan)
- [GITHUB-1048](https://github.com/testng-team/testng/issues/1048): Fix validation errors in TestNG CSS to adhere to CSS level 3 + SVG (Krishnan Mahadevan)
- [GITHUB-341](https://github.com/testng-team/testng/issues/341): TestNG does not respect "-parallel classes" when running with a jar file (Krishnan Mahadevan)
- [GITHUB-1790](https://github.com/testng-team/testng/issues/1790): IAnnotationTransformer transform method is not called for all test classes annotated with @Test (Krishnan Mahadevan)
- [GITHUB-1787](https://github.com/testng-team/testng/issues/1787): Method level parameters are missing when using Yaml generation utilities (Krishnan Mahadevan)
- [GITHUB-1773](https://github.com/testng-team/testng/issues/1773): Parallel="classes" executes methods of test class in different threads (Krishnan Mahadevan)
- [GITHUB-1185](https://github.com/testng-team/testng/issues/1185): DependsOnMethods made parallel class use several threads (Krishnan Mahadevan)
- [GITHUB-1173](https://github.com/testng-team/testng/issues/1173): Parallel="classes" executes methods of test class in different threads (Krishnan Mahadevan)
- [GITHUB-1066](https://github.com/testng-team/testng/issues/1066): Regression is in priority. It broke parallel mode (Krishnan Mahadevan)
- [GITHUB-1050](https://github.com/testng-team/testng/issues/1050): Parallel classes runs methods from one class in different threads, interleaves two classes in one thread (Krishnan Mahadevan)
- [GITHUB-89](https://github.com/testng-team/testng/issues/89): parallel="classes" is not forcing test methods from the same testClass to be run in the same thread as it is suposed to (Krishnan Mahadevan)
- [GITHUB-1719](https://github.com/testng-team/testng/issues/1719): successPercentage does not work correctly for tests with dataProvider (Krishnan Mahadevan)
- [GITHUB-1241](https://github.com/testng-team/testng/issues/1241): Streamline Retry Analyzer usage when same test is run multiple times (Krishnan Mahadevan)
- [GITHUB-1777](https://github.com/testng-team/testng/issues/1777): ITestListener.onTestStart() not called after fail or skip from @BeforeMethod (Krishnan Mahadevan)
- [GITHUB-1778](https://github.com/testng-team/testng/issues/1778): SoftAssert#fail swallows actual root cause (Krishnan Mahadevan)
- [GITHUB-1665](https://github.com/testng-team/testng/issues/1665): Failed test after rerun would impact next test case result (Yehui Wang)
- [GITHUB-1770](https://github.com/testng-team/testng/issues/1770): Factory annotation does not work with Parameters annotation on method constructor (Krishnan Mahadevan)
- [GITHUB-1767](https://github.com/testng-team/testng/issues/1767): Prevent NPE when XmlTest accessed without a proper XmlSuite (Krishnan Mahadevan)
- [GITHUB-1766](https://github.com/testng-team/testng/issues/1766): Testng generates a lot of temp folders like testngXmlPathInJar- (Andrey)
- [GITHUB-1759](https://github.com/testng-team/testng/issues/1759): core interfaces refactoring to support default methods (Sergey Korol)
- [GITHUB-1753](https://github.com/testng-team/testng/issues/1753): TestResult for an SKIP test lose attributes contributed by @BeforeMethod's or @AfterMethod's (Krishnan Mahadevan)
- [GITHUB-1756](https://github.com/testng-team/testng/issues/1756): ITest .getTestName() doesn't return the actual test name for skipped methods (Krishnan Mahadevan)
- [GITHUB-1602](https://github.com/testng-team/testng/issues/1602): beforeInvocation method don't get called for skipped tests (Krishnan Mahadevan)
- [GITHUB-549](https://github.com/testng-team/testng/issues/549) and [GITHUB-780](https://github.com/testng-team/testng/issues/780): Introduce onlyForGroups attribute for @BeforeMethod and @AfterMethod (Sergei Tachenov)
- [GITHUB-1745](https://github.com/testng-team/testng/issues/1745): Support native injection for @Factory methods (Krishnan Mahadevan)
- [GITHUB-1746](https://github.com/testng-team/testng/issues/1746): Make InvokedMethodNameListener thread-safe, fixing occasional build failures (Sergei Tachenov)
- [GITHUB-1538](https://github.com/testng-team/testng/issues/1538): Dependent methods don't get invoked when a failed test method is retried via a RetryAnalyser (Krishnan Mahadevan)
- [GITHUB-426](https://github.com/testng-team/testng/issues/426): firstTimeOnly ignored for threadPoolSize > 1 (Krishnan Mahadevan)
- [GITHUB-466](https://github.com/testng-team/testng/issues/466): JUnitReportReporter always converts the system time into GMT (Krishnan Mahadevan)
- [GITHUB-1723](https://github.com/testng-team/testng/issues/1723): Standardize timezone reference in XML reports (Krishnan Mahadevan)
- [GITHUB-489](https://github.com/testng-team/testng/issues/489): Incorrect timezone info in `<test-method>` element of testng-results.xml (Krishnan Mahadevan)
- [GITHUB-1735](https://github.com/testng-team/testng/issues/1735): IExecutionListener.onStart() running twice when used as annotation (Krishnan Mahadevan)
- [GITHUB-1726](https://github.com/testng-team/testng/issues/1726): Allow user-defined method interceptors to have the last say in method re-ordering (Krishnan Mahadevan)
- [GITHUB-564](https://github.com/testng-team/testng/issues/564): Support using @Optional on @Test method parameters to use null values (Krishnan Mahadevan)
- [GITHUB-1637](https://github.com/testng-team/testng/issues/1637): Remove dependency on a pre-created jar for running unit tests for JarFileUtils (Krishnan Mahadevan)
- [GITHUB-1710](https://github.com/testng-team/testng/issues/1710): Inefficient DynamicGraph causes hang with only 100 tests. (Steve Prentice)
- [GITHUB-1716](https://github.com/testng-team/testng/issues/1716): Potential NPE in jq.Main reporter (Krishnan Mahadevan)
- [GITHUB-1709](https://github.com/testng-team/testng/issues/1709): @Ignore doesn't work when used on child class, and parent has @Test methods (Krishnan Mahadevan)
- [GITHUB-1706](https://github.com/testng-team/testng/issues/1706): Retrying of methods fail when test method involves native injection (Krishnan Mahadevan)
- [GITHUB-1827](https://github.com/testng-team/testng/issues/1827): TestNG does not throw an error when a test class does not have a proper constructor (Julien Herr & Krishnan Mahadevan)
- Annotated default methods of indirectly implemented interfaces should still be called (Ilya Korobitsyn)

## [6.14.3] - 2018-02-23

### Fixed

- [GITHUB-1077](https://github.com/testng-team/testng/issues/1077): TestNG cannot handle load (Aheiss)
- [GITHUB-1081](https://github.com/testng-team/testng/issues/1081): group-by-instances with test dependencies causes instantiation of tests to exponentially slow (Aheiss)
- [GITHUB-1700](https://github.com/testng-team/testng/issues/1700): Test ignored if @BeforeMethod in base class fails for another test class (Krishnan Mahadevan)
- [GITHUB-1694](https://github.com/testng-team/testng/issues/1694): @BeforeGroups executed multiple times when tests run in parallel, once if not parallel (Krishnan Mahadevan)
- [GITHUB-1688](https://github.com/testng-team/testng/issues/1688): @Ignore annotation on base class doesn't ignore tests in child classes (Krishnan Mahadevan)
- [GITHUB-1687](https://github.com/testng-team/testng/issues/1687): NullPointerException is thrown when beanshell evaluates to null (Krishnan Mahadevan)

## [6.14.2] - 2018-02-04

### Added

- [GITHUB-1634](https://github.com/testng-team/testng/issues/1634): Make "-xmlpathinjar" support `<suite-files>` (Yehui Wang)
- [GITHUB-1634](https://github.com/testng-team/testng/issues/1634): Make "-testnames" find tests from Multi-level parent-child suites (Yehui Wang)
- [GITHUB-1631](https://github.com/testng-team/testng/issues/1631): data provider class name injection into Factory meta-data (Sergey Korol)

### Fixed

- [GITHUB-1674](https://github.com/testng-team/testng/issues/1674): beanshell methodselector applied at suite level is ignored (Krishnan Mahadevan)
- [GITHUB-1668](https://github.com/testng-team/testng/issues/1668): "Invalid Method Selector" exception triggered when using suite level beanshell methodselectors (Krishnan Mahadevan)
- [GITHUB-1659](https://github.com/testng-team/testng/issues/1659): New line characters are removed from stack traces in testng-results.xml (Krishnan Mahadevan)
- [GITHUB-1503](https://github.com/testng-team/testng/issues/1503): Consider adding a -n (no execute) option (Krishnan Mahadevan)
- [GITHUB-1648](https://github.com/testng-team/testng/issues/1648): Depends on method is not respected on the sequential run on second test that extends same base testClass (Krishnan Mahadevan)
- [GITHUB-1636](https://github.com/testng-team/testng/issues/1636): Parallel test run is not working in 6.13.1 (Krishnan Mahadevan)
- [GITHUB-1641](https://github.com/testng-team/testng/issues/1641): The time for test-method is 8 hours ahead of the the time for suite/class in testng-result xml file (Krishnan Mahadevan)
- [GITHUB-1649](https://github.com/testng-team/testng/issues/1649): @Test annotated methods cannot inject java.lang.reflect.Method (Krishnan Mahadevan)
- [GITHUB-1625](https://github.com/testng-team/testng/issues/1625): Null fields in parallel method tests (Krishnan Mahadevan)
- [GITHUB-1605](https://github.com/testng-team/testng/issues/1605): Research the usefulness of the JVM argument "experimental" (Krishnan Mahadevan)

## [6.13.1] - 2017-11-27

No functional changes. Released with newer version JCommander (1.72.0)

## [6.13] - 2017-11-22

### Added

- [GITHUB-1490](https://github.com/testng-team/testng/issues/1490): Add a listener for data provider interception (Krishnan Mahadevan)
- Remove Serializable (Julien Herr)
- [GITHUB-861](https://github.com/testng-team/testng/issues/861): Add @Ignore annotation which disables all tests in a class or a package (Julien Herr)

### Fixed

- [GITHUB-1619](https://github.com/testng-team/testng/issues/1619): ConcurrentHashMap doesn't secure insertion order.(Yehui Wang)
- [GITHUB-1616](https://github.com/testng-team/testng/issues/1616): Test cases with priority and dependsOnGroups dependencies, execution order is chaos. (Yehui Wang)
- [GITHUB-1613](https://github.com/testng-team/testng/issues/1613): The constructor removed from TestRunner would stop Eclipse working.(Yehui Wang)
- [GITHUB-1600](https://github.com/testng-team/testng/issues/1600): Updated in afterInvocation() testResult.status is not used in willRetry condition (Krishnan Mahadevan)
- [GITHUB-1598](https://github.com/testng-team/testng/issues/1598): Injected types parameter and optional parameters cannot be used together. (Yehui Wang)
- [GITHUB-1594](https://github.com/testng-team/testng/issues/1594): Cannot filter by "testnames" when suite xml is a suite of suites (Krishnan Mahadevan)
- [GITHUB-1584](https://github.com/testng-team/testng/issues/1584): Can't run tests from IDEA (Krishnan Mahadevan)
- [GITHUB-1589](https://github.com/testng-team/testng/issues/1589): TestNGAntTask should be consistently using the Ant Log API for writing log messages (Krishnan Mahadevan)
- [GITHUB-1587](https://github.com/testng-team/testng/issues/1587): TestNG can not guarantee the ExecutionListener Instance as singleton(Yehui Wang)
- [GITHUB-217](https://github.com/testng-team/testng/issues/217): exception in DataProvider doesn't fail test run (Krishnan Mahadevan)
- [GITHUB-987](https://github.com/testng-team/testng/issues/987): Parameters threadCount and parallel doesn't work with maven (Krishnan Mahadevan)
- [GITHUB-1472](https://github.com/testng-team/testng/issues/1472): Optimize DynamicGraph.getUnfinishedNodes (Krishnan Mahadevan & Nathan Reynolds)
- [GITHUB-1566](https://github.com/testng-team/testng/issues/1566): Invalid XML characters in Params in testng-results.xml (Krishnan Mahadevan)
- [GITHUB-1554](https://github.com/testng-team/testng/issues/1554): @Parameters and parameter injection not wroking when used on the same method (Krishnan Mahadevan)
- [GITHUB-990](https://github.com/testng-team/testng/issues/990): NullPointerExceptions after a superclass configuration method fails with configfailurepolicy="continue" (Krishnan Mahadevan)
- [GITHUB-461](https://github.com/testng-team/testng/issues/461) : Annotate annotations with @Documented (Krishnan Mahadevan)
- [GITHUB-778](https://github.com/testng-team/testng/issues/778) : XmlSuite toXml() does NOT add the suite time-out property (Krishnan Mahadevan)
- [GITHUB-1029](https://github.com/testng-team/testng/issues/1029): Issue with getting XmlTest from test method (Krishnan Mahadevan)
- [GITHUB-212](https://github.com/testng-team/testng/issues/212): Enable support for providing a URI as suite file location (Krishnan Mahadevan)
- [GITHUB-161](https://github.com/testng-team/testng/issues/161) : Provide a way to customize SAXParserFactory implementation (Krishnan Mahadevan)
- [GITHUB-1455](https://github.com/testng-team/testng/issues/1455): Configure XML output of XmlSuite (Krishnan Mahadevan)
- [GITHUB-1465](https://github.com/testng-team/testng/issues/1465): Failure policy CONTINUE handling is broken for tests that are skipped in @BeforeMethod method (Krishnan Mahadevan)
- [GITHUB-1533](https://github.com/testng-team/testng/issues/1533): Duplicate child suites get added when working with parent/child suite scenario (Krishnan Mahadevan)
- [GITHUB-949](https://github.com/testng-team/testng/issues/949): dependsOnMethods with alwaysRun = true and inheritance fails to find method (Krishnan Mahadevan)
- [GITHUB-1519](https://github.com/testng-team/testng/issues/1519): Possibility to retry a test until it FAILED (Krishnan Mahadevan)
- [GITHUB-980](https://github.com/testng-team/testng/issues/980): TestNG run inherited method twice (Krishnan Mahadevan)
- [GITHUB-1409](https://github.com/testng-team/testng/issues/1409): Regression: on expectedExceptionsMessageRegExp expected and actual messages are not printed (Krishnan Mahadevan)
- [GITHUB-1517](https://github.com/testng-team/testng/issues/1517): TestNG exits with a zero when there are configuration failures (Krishnan Mahadevan)
- [GITHUB-1456](https://github.com/testng-team/testng/issues/1456): Remove/Warn support of constructor with String param (Krishnan Mahadevan)
- [GITHUB-1509](https://github.com/testng-team/testng/issues/1509): Improve error message when data provider returns a null value (Krishnan Mahadevan)
- [GITHUB-1507](https://github.com/testng-team/testng/issues/1507): TestNG runs all methods when filtering via `<include>` fails (Krishnan Mahadevan)
- [GITHUB-1493](https://github.com/testng-team/testng/issues/1493): Wrong exception msg when timeout on test (Krishnan Mahadevan)
- [GITHUB-328](https://github.com/testng-team/testng/issues/328): Attempt to fix unnecessary execution of @Factory-ctors (@beverage & Julien Herr)
- [GITHUB-1384](https://github.com/testng-team/testng/issues/1384): Huge performance issue between 6.5.2 and 6.11 (Denis Bazhenov)
- [GITHUB-1496](https://github.com/testng-team/testng/issues/1496): If method contains "$", run only one method, all methods will be run (@JF-Rabbit & Julien Herr)
- [GITHUB-1220](https://github.com/testng-team/testng/issues/1220): Recognize annotations on default methods in implemented interface

## [6.12] - 2017-07-25

### Added

- Enhance XML Reporter to be able to customize the file name (Krishnan Mahadevan)
- Enhance TestNGAntTask to be customizable (Denys Kurylenko)
- Make EmailableReporter2 W3C Compliant[XHTML 1.1] (Chris Rankin)

### Fixed

- [GITHUB-1484](https://github.com/testng-team/testng/issues/1484): Remove irrelevant "targets" for TestNG annotations (Krishnan Mahadevan)
- [GITHUB-1405](https://github.com/testng-team/testng/issues/1405): Skip considering main() method when @Test used at class level (Krishnan Mahadevan)
- [GITHUB-799](https://github.com/testng-team/testng/issues/799): @Factory with dataProvider changes order of iterations (Krishnan Mahadevan & Julien Herr)
- [GITHUB-1417](https://github.com/testng-team/testng/issues/1417): Class param injection is not working with @BeforeClass (Krishnan Mahadevan)
- [GITHUB-1440](https://github.com/testng-team/testng/issues/1440): Improve error message when wrong params on configuration methods (Krishnan Mahadevan)
- [GITHUB-1433](https://github.com/testng-team/testng/issues/1433): Missing encoding for emailable reports (Shaburov Oleg)
- [GITHUB-1430](https://github.com/testng-team/testng/issues/1430): Cannot load class from file XXX when using with ant and classfileset (Olivier Mourez)
- [GITHUB-1394](https://github.com/testng-team/testng/issues/1394): Optimize ClassHelper.getAvailableMethods() to exclude Object class(Nathan Reynolds & Krishnan Mahadevan)
- [GITHUB-1396](https://github.com/testng-team/testng/issues/1396): Order established by IMethodInterceptor not honored when running with parallel='instances' (Ryan Scott)
- [GITHUB-1287](https://github.com/testng-team/testng/issues/1287): Parallel (methods) execution with dependsOn running in unexpected order (Kevyn Reinholt)
- [GITHUB-1362](https://github.com/testng-team/testng/issues/1362): Ensure AfterGroups methods get executed when involving Method Interceptors (Krishnan Mahadevan)
- [GITHUB-765](https://github.com/testng-team/testng/issues/765): Skip invocation of bridged methods (Krishnan Mahadevan)
- [GITHUB-1336](https://github.com/testng-team/testng/issues/1336): (parallel=‘classes’) not working when coupled with priority (Krishnan Mahadevan)
- [GITHUB-1365](https://github.com/testng-team/testng/issues/1365): Be able to override default XML parser (@ChristiKh & Julien Herr)
- [GITHUB-1360](https://github.com/testng-team/testng/issues/1360): TestNG does not distinguish between methods of different priorities (Krishnan Mahadevan)
- [GITHUB-1144](https://github.com/testng-team/testng/issues/1144): Add Class and Constructor as legal native dependency injection (Guillaume Juillot)
- [GITHUB-1380](https://github.com/testng-team/testng/issues/1380): Circular dependencies may fail in parallel (Julien Herr)
- [GITHUB-1400](https://github.com/testng-team/testng/issues/1400): TestNG, Multiple duplicate listener warnings on implementing multiple listener interfaces (@bipo1980 & Nick Tan)
- [GITHUB-1426](https://github.com/testng-team/testng/issues/1426): @AfterMethod(alwaysRun = true) is not getting called if we have exception in @BeforeMethod (@dipak-pawar)
- [GITHUB-128](https://github.com/testng-team/testng/issues/128): Using `Object[]` and Method as parameters for a test in a certain order yields an IllegalArgumentException, citing a type mismatch (@leef590 & Julien Herr,Krishnan Mahadevan)
- [GITHUB-1393](https://github.com/testng-team/testng/issues/1393): Revert commit 50d534a to allow fail a test from onTestStart method
- [GITHUB-1461](https://github.com/testng-team/testng/issues/1461): TestNG not getting garbage collected (@kiru)

## [6.11] - 2017-02-27

### Added

- The name of all TestNG threads follow "TestNG-`<thread type>`-`<number>`" pattern (Julien Herr)
- Skipped methods are not supposed to be executed (Julien Herr)
- [GITHUB-1313](https://github.com/testng-team/testng/issues/1313): Add Java9 as test environment on Travis (Julien Herr)

### Fixed

- [GITHUB-1351](https://github.com/testng-team/testng/issues/1351): FailurePolicy failing with YAML (Steven Zaluk & Julien Herr)
- [GITHUB-1339](https://github.com/testng-team/testng/issues/1339): Alter ClassHelper to use Maps instead of Lists for extracting methods (Krishnan Mahadevan)
- [GITHUB-1338](https://github.com/testng-team/testng/issues/1338): @BeforeGroups method is run on a wrong instance (Pavel Vetokhin & Julien Herr)
- [GITHUB-1332](https://github.com/testng-team/testng/issues/1332): Make EmailableReport name configurable (Krishnan Mahadevan)
- [GITHUB-1297](https://github.com/testng-team/testng/issues/1297): testng-failed.xml includes setup and tearDown (before and after annotations) of passed tests (Krishnan Mahadevan)
- [GITHUB-1319](https://github.com/testng-team/testng/issues/1319): ITestResult#getInstance() returns null in IConfigurationListener implementation (Krishnan Mahadevan)
- [GITHUB-1197](https://github.com/testng-team/testng/issues/1197): Ability to dynamically set the status and exception of a test via ITestResult (Anthony Nguyen)
- [GITHUB-1302](https://github.com/testng-team/testng/issues/1302): When 'parallel' is set to 'classes', ConcurrentModificationException can be thrown(Jianhua Li)
- [GITHUB-772](https://github.com/testng-team/testng/issues/772): Severe thread contention while running large test with parallel methods (Shaburov Oleg)
- [GITHUB-1307](https://github.com/testng-team/testng/issues/1307): TestNGException when using an anonymous class in Factory (Mike Cowan)
- [GITHUB-1298](https://github.com/testng-team/testng/issues/1298): ITestResult injection is failing in BeforeMethod method (Krishnan Mahadevan)
- [GITHUB-1293](https://github.com/testng-team/testng/issues/1293): Beanshell based execution does not work any more (Krishnan Mahadevan)
- [GITHUB-1262](https://github.com/testng-team/testng/issues/1262): Testcases out of order in XML file in junitreport folder when using testng (Krishnan Mahadevan)
- [GITHUB-116](https://github.com/testng-team/testng/issues/116) : BaseTestMethod does respect general contract of Comparable (Testo Nakada)
- [GITHUB-1265](https://github.com/testng-team/testng/issues/1265): JUnit Reporter includes redundant ignored methods (Krishnan Mahadevan)
- [GITHUB-1266](https://github.com/testng-team/testng/issues/1266): JUnit Reporter produces a wrong number of total test methods (Krishnan Mahadevan)
- [GITHUB-1257](https://github.com/testng-team/testng/issues/1257): Group parameter not applying on included `<suite-files>`
- [GITHUB-1284](https://github.com/testng-team/testng/issues/1284): Listeners on the child suites are not applied (Vimalraj Selvam)
- [GITHUB-1296](https://github.com/testng-team/testng/issues/1296): Configuration listeners run multiple times (@mikimrozowski & Julien Herr)
- [GITHUB-1300](https://github.com/testng-team/testng/issues/1300): Add deep assertions to assert on array values (array reference by default) (Jordan Zimmerman & Julien Herr)

## [6.10] - 2016-11-28

### Added

- Hierarchy on order features (from less important to more important): groupByInstance, preserveOrder, priority, dependsOnGroups, dependsOnMethods
- Resources for test reports (header, images) now live in a sub directory org/testng to remove conflicts with other similar files in the class loader.

### Fixed

- [GITHUB-551](https://github.com/testng-team/testng/issues/551): Failed configuration method always has 0 execution time (dr29bart)
- [GITHUB-1250](https://github.com/testng-team/testng/issues/1250): Testng-failed.xml is getting test level parameters into suite level parameters (Krishnan Mahadevan)
- [GITHUB-1046](https://github.com/testng-team/testng/issues/1046): Provide a mechanism to customize a test method name for reporting (Krishnan Mahadevan)
- [GITHUB-1211](https://github.com/testng-team/testng/issues/1211): Include disabled/ ignored test methods in JUnit reports (Krishnan Mahadevan)
- [GITHUB-1213](https://github.com/testng-team/testng/issues/1213): Include "ignored" test count in testng-results.xml (Krishnan Mahadevan)
- [GITHUB-674](https://github.com/testng-team/testng/issues/674): Enrich Test method skips due to configuration failures with throwable data (Krishnan Mahadevan)
- [GITHUB-1240](https://github.com/testng-team/testng/issues/1240): Enrich the test results showing mechanism in Travis CI (Krishnan Mahadevan)
- [GITHUB-1232](https://github.com/testng-team/testng/issues/1232): Prevent TestNG from adding duplicate instances of the same listener (Krishnan Mahadevan)
- [GITHUB-1170](https://github.com/testng-team/testng/issues/1170): Fixing the test DataProviderTest.shouldNotThrowConcurrentModification (Krishnan Mahadevan)
- [GITHUB-1231](https://github.com/testng-team/testng/issues/1231): Make IExecutionListener implementation be the last reporter call before JVM exit(Krishnan Mahadevan)
- [GITHUB-1227](https://github.com/testng-team/testng/issues/1227): Prevent multiple instances of same Reporter from being injected into TestNG (Krishnan Mahadevan)
- [GITHUB-1165](https://github.com/testng-team/testng/issues/1165): Better message to user when param injection is not good (Krishnan Mahadevan)
- [GITHUB-1228](https://github.com/testng-team/testng/issues/1228): Control stacktrace levels in XmlReports via a JVM configuration (Krishnan Mahadevan)
- [GITHUB-1203](https://github.com/testng-team/testng/issues/1203): Add flush to BufferedWriter; fixes incomplete XML reports (Nathan Bruning)
- [GITHUB-1181](https://github.com/testng-team/testng/issues/1181): Fix MethodMatcherException: Data provider mismatch (Krishnan Mahadevan)
- [GITHUB-1107](https://github.com/testng-team/testng/issues/1107): TestNG does not report/print/log throwables in data providers (Krishnan Mahadevan)
- [GITHUB-1186](https://github.com/testng-team/testng/issues/1186): NullPointerException in JUnit reporter when used with Spock (Ian Robertson & Julien Herr)
- [GITHUB-1180](https://github.com/testng-team/testng/issues/1180): NullPointerException on getting excluded/included groups (Krishnan Mahadevan)
- [GITHUB-1064](https://github.com/testng-team/testng/issues/1064): Incorrect logging of parallel mode of a test
- [GITHUB-1178](https://github.com/testng-team/testng/issues/1178): Halt execution when invalid testname is provided. (Krishnan Mahadevan)
- [GITHUB-1139](https://github.com/testng-team/testng/issues/1139): DataProvider could support `Object[]` as a valid return type (Julien Herr)
- [GITHUB-1182](https://github.com/testng-team/testng/issues/1182): Cannot run multiple @Factory-annotated methods in the same class (Ian Donovan & Julien Herr)
- [GITHUB-1156](https://github.com/testng-team/testng/issues/1156): test execution dependant upon class name order and fails with TestNGException: No free nodes found (@t-weil & Julien Herr)
- [GITHUB-1221](https://github.com/testng-team/testng/issues/1221): ConcurrentModificationException in TextReporter (Nick Tan)
- testng-eclipse/issues/298: IConfigurationListener was not loaded when running Test in Eclipse Plugin (@jmcgrail & Nick Tan)

## [6.9.13.6] - 2016-09-23

Final release of the 6.9.13 line. Packaging only: it carries the changes listed under 6.9.13.

## [6.9.13.5] - 2016-09-23 [YANKED]

Bad release.

Packaging only: it carries the changes listed under 6.9.13.

## [6.9.13.4] - 2016-09-22 [YANKED]

Bad release: Wrong internal version.

Packaging only: it carries the changes listed under 6.9.13.

## [6.9.13.3] - 2016-09-22

Ok, but too many direct dependencies.

Packaging only: it carries the changes listed under 6.9.13.

## [6.9.13.2] - 2016-09-20 [YANKED]

Bad release: Wrong internal version.

Packaging only: it carries the changes listed under 6.9.13.

## [6.9.13.1] - 2016-09-19 [YANKED]

Bad release: fat jar.

Packaging only: it carries the changes listed under 6.9.13.

## [6.9.13] - 2016-09-16 [YANKED]

Bad release: JDK8 target only.

### Added

- code improvement in order to calculate key for dependency map. Dependency map will use methodQualifiedName as key provided by ITestNGMethod (Chirag Jayswal)
- [GITHUB-1083](https://github.com/testng-team/testng/issues/1083): Factory supports indices (Julien Herr)

### Fixed

- [GITHUB-1105](https://github.com/testng-team/testng/issues/1105): Test skipped instead failed if incorrect enum value (Liza Ivanova & Julien Herr)
- [GITHUB-1111](https://github.com/testng-team/testng/issues/1111): XMLReporter crashes if a test parameter is exactly "]]>" (Łukasz Rekucki & Julien Herr)
- [GITHUB-1108](https://github.com/testng-team/testng/issues/1108) @BeforeGroups called twice (Krishnan Mahadevan)
- [GITHUB-1112](https://github.com/testng-team/testng/issues/1112) XmlInclude.getDescription returns null always (Krishnan Mahadevan)
- [GITHUB-1090](https://github.com/testng-team/testng/issues/1090) Inconsistent handling "preserve-order" on suite/test level (Michal Domagala & Julien Herr)
- [GITHUB-1085](https://github.com/testng-team/testng/issues/1085) Remove Guava dependency (Erik C. Thauvin & Julien Herr)
- [GITHUB-1084](https://github.com/testng-team/testng/issues/1084) Using deprecated addListener methods should not register many times (Anna Kozlova & Julien Herr)
- [GITHUB-447](https://github.com/testng-team/testng/issues/447) Copy test parameters instead of storing a reference (Huagang Li & Julien Herr)
- [GITHUB-174](https://github.com/testng-team/testng/issues/174): NPE when parsing xml where `<suite>` has `<groups>` (Peter Stout & Julien Herr)
- [GITHUB-918](https://github.com/testng-team/testng/issues/918): NullPointerException on loading XmlSuites programmatically (@ispitkovskyi & Julien Herr)
- [GITHUB-689](https://github.com/testng-team/testng/issues/689): `<groups>` at `<suite>` level not applied to `<suite-files>` (@kunal546 & Julien Herr)
- [GITHUB-740](https://github.com/testng-team/testng/issues/740): More than one execution even when success with DataProvider and IRetryAnalyzer (Sergio Sacristán)
- [GITHUB-877](https://github.com/testng-team/testng/issues/877): Retries don't work correctly with DataProvider tests (Simonas Tvirbutas)
- [GITHUB-1103](https://github.com/testng-team/testng/issues/1103): Add ...junit.ArrayAsserts.assertArrayEquals(`boolean[]`, `boolean[]`) (Jonathan Halterman & Julien Herr)
- [GITHUB-1122](https://github.com/testng-team/testng/issues/1122): Use the default value for preserve-order (Guillaume Guillot & Julien Herr)
- [GITHUB-1022](https://github.com/testng-team/testng/issues/1022): Non static methods from external data providers are not working without @Guice (Sourav Chandra & Julien Herr)
- [GITHUB-1130](https://github.com/testng-team/testng/issues/1130): IClassListener should only be instantiated once (Guillaume Guillot & Julien Herr)
- [GITHUB-1131](https://github.com/testng-team/testng/issues/1131): IObjectFactory not being called for factory test instances with constructor-injected data provider (Scott McClure & Julien Herr)
- [GITHUB-148](https://github.com/testng-team/testng/issues/148): 'Run Failed Test' doesn't run the proper tests after 2nd rerun when DataProvider is used (@akracheva & Julien Herr)

## [6.9.12] - 2016-06-21

### Added

- Minimal code changes to allow TestNG to work for OpenJDK tests, which should be run with only the java.base module present.
- TestNG displays a warning when tests are using an empty data provider

### Fixed

- [GITHUB-1017](https://github.com/testng-team/testng/issues/1017) Reporter.log is ignored in skipped test listener (Scott Kirkpatrick)
- [GITHUB-1047](https://github.com/testng-team/testng/issues/1047) IClassListener didn't work (Julien Herr)
- [GITHUB-1049](https://github.com/testng-team/testng/issues/1049) IClassListener was called many time (Julien Herr)
- [GITHUB-1045](https://github.com/testng-team/testng/issues/1045) TestNG swallows exceptions silently if @ClassRule is used (Gili Tzabari & Julien Herr)
- [GITHUB-506](https://github.com/testng-team/testng/issues/506) TestNG cannot find JUnit method names from Spock (@blackduck-joe & Julien Herr)
- [GITHUB-1009](https://github.com/testng-team/testng/issues/1009) Iterator`<Object[]>` DataProvider: indices not working (Mark Fulton & Julien Herr)
- [GITHUB-1030](https://github.com/testng-team/testng/issues/1030) Parameterized test class crashes when data provider returns empty array (Christoffer Sawicki & Julien Herr)

## [6.9.11] - 2016-03-26

### Added

- [GITHUB-933](https://github.com/testng-team/testng/issues/933): Deprecate XmlTest#getTestParameters (Julien Herr)
- [GITHUB-932](https://github.com/testng-team/testng/issues/932): Deprecate true/false parallel values. none is the new default value. (Julien Herr)

### Fixed

- [GITHUB-923](https://github.com/testng-team/testng/issues/923) Refactored data provider's parameter values passing to a varargs or non-varargs method with @NoInjection handling (Nitin Verma)
- [GITHUB-911](https://github.com/testng-team/testng/issues/911): TestListener#onTestStart should be invoked if a suite configuration method fails (Harmin Parra Rueda & Julien Herr)
- [GITHUB-793](https://github.com/testng-team/testng/issues/793): Test suite with tests using dependency and priority has wrong behavior (Martin Hereu & Julien Herr)
- [GITHUB-922](https://github.com/testng-team/testng/issues/922): ITestResult doesn't contain name if a class has @Test (@dr29bart & Julien Herr)
- [GITHUB-419](https://github.com/testng-team/testng/issues/419): parallel mode was ignored with command line (@khospodarysko & Julien Herr)
- [GITHUB-960](https://github.com/testng-team/testng/issues/960): testng-failed.xml gets generated even when there are no failures. (Krishnan Mahadevan)
- [GITHUB-895](https://github.com/testng-team/testng/issues/895): Changing status of test by setStatus of ITestResult (Raj Srivastava & Julien Herr)
- [GITHUB-969](https://github.com/testng-team/testng/issues/969): testng-failed.xml does not carry over the parameters of methods from origin suite xml(Ning Zhang)

## [6.9.10] - 2015-12-15

### Added

- [GITHUB-776](https://github.com/testng-team/testng/issues/776): Add BeforeClass/AfterClass like on ITestListener (@vguna & Julien Herr)
- [GITHUB-900](https://github.com/testng-team/testng/issues/900): Support @Listeners in annotation transformer (Julien Herr)
- [GITHUB-898](https://github.com/testng-team/testng/issues/898): Activate XML validation when possible (Julien Herr)
- [GITHUB-886](https://github.com/testng-team/testng/issues/886): Add some checks on factory methods (Julien Herr)
- [GITHUB-874](https://github.com/testng-team/testng/issues/874) / [GITHUB-875](https://github.com/testng-team/testng/issues/875) / [GITHUB-882](https://github.com/testng-team/testng/issues/882) / [GITHUB-850](https://github.com/testng-team/testng/issues/850) : Some code cleanup (Testo Nakada)

### Fixed

- [GITHUB-841](https://github.com/testng-team/testng/issues/841): testName from @Test is now used and available from ITestResult#getName() and ITestResult#getTestName() (Julien Herr)
- [GITHUB-872](https://github.com/testng-team/testng/issues/872): Enable end-users of TestNG to alter XmlSuite and XmlTest (Krishnan Mahadevan)
- [GITHUB-889](https://github.com/testng-team/testng/issues/889): XmlSuite in nested directories results in FIleNotFoundException (Virender Singh)
- [GITHUB-811](https://github.com/testng-team/testng/issues/811): Timeout is not working with parallel=tests (@michael-yxf & Julien Herr)
- [GITHUB-839](https://github.com/testng-team/testng/issues/839): Missing encoding meta data for report file (@banbq & Julien Herr)
- [GITHUB-876](https://github.com/testng-team/testng/issues/876): NullPointerException creating tests with parameters by a factory (@vixgeo & Julien Herr)
- [GITHUB-866](https://github.com/testng-team/testng/issues/866) / [GITHUB-869](https://github.com/testng-team/testng/issues/869) : Some attributes were not cloned when XmlSuite#clone was used (Virender Singh)
- [GITHUB-842](https://github.com/testng-team/testng/issues/842): Add TestResult#getTestName() support for @Test(testName) (Julien Herr)
- [GITHUB-908](https://github.com/testng-team/testng/issues/908): Fix Double.NaN assertion (Julien Herr)

## [6.9.9] - 2015-10-27

### Fixed

- [GITHUB-829](https://github.com/testng-team/testng/issues/829): Allowing suites to have duplicate names. You can now configure the same suite-file to run multiple times. (Eduardo Born)
- [GITHUB-834](https://github.com/testng-team/testng/issues/834): nested suites not supported by 'testnames' (Tibor Digana & Julien Herr)

## [6.9.8] - 2015-10-12

Replace 6.9.7 that was build with Java8 by error.

## [6.9.7] - 2015-10-12 [YANKED]

No official release

### Fixed

- [GITHUB-798](https://github.com/testng-team/testng/issues/798): Set suitethreadpoolsize for Maven Surefire (Jan Dundáček)
- [GITHUB-171](https://github.com/testng-team/testng/issues/171): ISuiteListener methods called multiple times if multiple test elements (Daniel Qian & Julien Herr)
- [GITHUB-169](https://github.com/testng-team/testng/issues/169): IInvokedMethodListener methods executed several times before/after each test method (Mario Duarte & Julien Herr)
- [GITHUB-154](https://github.com/testng-team/testng/issues/154): MethodInterceptor will be called twice (Tim wu & Julien Herr)

## [6.9.6] - 2015-07-15

### Added

- [GITHUB-717](https://github.com/testng-team/testng/issues/717): Add assertThrows and expectThrows (Ryan Schmitt)

### Fixed

- [GITHUB-755](https://github.com/testng-team/testng/issues/755): Fixed reporting of retried tests (Ryan Schmitt)
- [GITHUB-773](https://github.com/testng-team/testng/issues/773): Test should not be skipped when the exception is expected (@CandyLiuM & Julien Herr)

## [6.9.5] - 2015-07-12

### Added

- [GITHUB-710](https://github.com/testng-team/testng/issues/710): AppVeyor is used for continuous integration on Windows (Julien Herr)
- [GITHUB-723](https://github.com/testng-team/testng/issues/723): Allow users to add their own suite parser (Julien Herr)

### Fixed

- The ServiceLoaderTest on Windows (Mathieu Sebire)
- [GITHUB-691](https://github.com/testng-team/testng/issues/691): Fix classloading issue when using TestNG 6.9.4 and JMockit. (Mathieu Sebire)
- [GITHUB-686](https://github.com/testng-team/testng/issues/686): IAnnotationTransformer.transform is called for methods with testClass populated. (Łukasz Rekucki & Julien Herr)
- [GITHUB-420](https://github.com/testng-team/testng/issues/420): Before/AfterSuite methods may not run, when use inheritance, and enabled=false (Jakub Tokaj & Julien Herr)
- [GITHUB-697](https://github.com/testng-team/testng/issues/697): Make addFailedInvocationNumber thread-safe (Ryan Schmitt)
- [GITHUB-698](https://github.com/testng-team/testng/issues/698): Fix exit code reporting when IRetryAnalyzer is used (Ryan Schmitt)
- [GITHUB-465](https://github.com/testng-team/testng/issues/465): assertEquals(Collection, Collection) prints "null" when collections are different sizes (Michael Diamond)
- [GITHUB-599](https://github.com/testng-team/testng/issues/599): IHookable ignored when a timeout is set (@ryanlevell & Julien Herr)
- Allow '-testnames' option to work with '-xmlpathinjar' (@earthling)
- [GITHUB-739](https://github.com/testng-team/testng/issues/739): TestNG skips all test classes from suite when a @BeforeClass fails (Priyanshu Shekhar & Julien Herr)
- [GITHUB-471](https://github.com/testng-team/testng/issues/471): If @beforeMethod or @afterMethod fails then all children of the same base class will be skipped (Anton Panferov & Julien Herr)
- [GITHUB-595](https://github.com/testng-team/testng/issues/595): testng hang at switching test cases when running test cases with high thread count (vit0rg)
- **Eclipse:** The 57% freeze bug (Patrick Hensley and @denyska)

## [6.9.4] - 2015-05-09

### Added

- [GITHUB-631](https://github.com/testng-team/testng/issues/631): Avoid the static limitation of external DataProvider. (Julien Herr)
- [GITHUB-631](https://github.com/testng-team/testng/issues/631): Allow to use Guice injection in DataProvider. (Julien Herr)
- Drop support of Java6 and previous.
- [GITHUB-617](https://github.com/testng-team/testng/issues/617): Allow injection of org.testng.ITestContext into the guice parent module. (Julien Herr)
- [GITHUB-638](https://github.com/testng-team/testng/issues/638): Travis CI is used for continuous integration (Julien Herr)
- [GITHUB-647](https://github.com/testng-team/testng/issues/647): SonarQube is used to follow technical debt (Julien Herr)
- [GITHUB-616](https://github.com/testng-team/testng/issues/616): org.testng.internal.Version will be always up-to-date (Julien Herr)
- [GITHUB-645](https://github.com/testng-team/testng/issues/645): TestNG project on Google Code redirect to GitHub
- [GITHUB-663](https://github.com/testng-team/testng/issues/663): Add Guice Stage configuration for a suite (Clément Guillaume)

### Fixed

- [GITHUB-606](https://github.com/testng-team/testng/issues/606): RetryAnalyzer loops endlessly. (Krishnan Mahadevan)
- [GITHUB-618](https://github.com/testng-team/testng/issues/618): Start TestNG from jar cause recursive run of tests from packages in Suite XML without ".\*" on the end (Stas Gromov)
- [GITHUB-639](https://github.com/testng-team/testng/issues/639): Typo on preserveOrder (tabei-k & Julien Herr)
- [GITHUB-632](https://github.com/testng-team/testng/issues/632): Typo in doc (Pétur Ingi Egilsson & Julien Herr)
- [GITHUB-629](https://github.com/testng-team/testng/issues/629): InvokedMethod doesn't recognize configuration method (Jan Mewes & Julien Herr)
- [GITHUB-615](https://github.com/testng-team/testng/issues/615): XmlSuite, XmlTest: Time-out tag not preserved (jphollingworth & Julien Herr)
- [GITHUB-634](https://github.com/testng-team/testng/issues/634): Review of the collections package (Julien Herr)
- [GITHUB-624](https://github.com/testng-team/testng/issues/624): Fixed failure/error inversion in JUnitReportReporter (Jerome Jacob)
- [GITHUB-545](https://github.com/testng-team/testng/issues/545): TestNG running JUnit tests but not reporting all results for parameterized tests (Jonathan Leitschuh & jdillet)
- [GITHUB-610](https://github.com/testng-team/testng/issues/610): CustomizedSuites must be saved using utf-8 encoding (Juha Heljoranta)
- [GITHUB-602](https://github.com/testng-team/testng/issues/602): NoClassDefFoundError in TestNGClassFinder.`<init>` (aanno)
- [GITHUB-529](https://github.com/testng-team/testng/issues/529): Close InputStream and OuputStream after use (Andrew Gaul)
- [GITHUB-532](https://github.com/testng-team/testng/issues/532): Create the parent directory if it's missing (Ion Savin)
- [GITHUB-541](https://github.com/testng-team/testng/issues/541): Some OSGi manifest fixes (Evgeny Zhuravlev)
- [GITHUB-657](https://github.com/testng-team/testng/issues/657): Fix OSGI Import-Package to make jUnit4 dependency optional (Xavier Fournet)
- [GITHUB-523](https://github.com/testng-team/testng/issues/523): externally synchronize our use of the static SimpleDateFormat (mcosby)
- [GITHUB-477](https://github.com/testng-team/testng/issues/477): Typo in DTD attribute comment (Kamil Szymański)
- [GITHUB-353](https://github.com/testng-team/testng/issues/353): Typo in documentation (Jan Święcki)
- [GITHUB-656](https://github.com/testng-team/testng/issues/656): Upgrade to JCommander 1.48 (Ryan Schmitt)
- [GITHUB-582](https://github.com/testng-team/testng/issues/582): TestNG tests don't pass reliably on JDK8 (Ryan Schmitt)
- [GITHUB-310](https://github.com/testng-team/testng/issues/310): Upgrade Guice (kronar & Julien Herr)
- [GITHUB-87](https://github.com/testng-team/testng/issues/87): @BeforeSuite/@BeforeTest methods happens to be disabled by mistake (romlom & Julien Herr)
- [GITHUB-425](https://github.com/testng-team/testng/issues/425): Wrong invocation order with lastTimeOnly (Rafael Winterhalter & Julien Herr)
- [GITHUB-417](https://github.com/testng-team/testng/issues/417): Expected Exceptions Message fails to match multi-line messages (Michael Diamond)

## [6.8.21] - 2015-02-02

No changes recorded.

## [6.8.15] - 2015-01-14

### Fixed

- OutOfMemoryException while generating reports.
- [GITHUB-566](https://github.com/testng-team/testng/issues/566): Build does not fail when successPercentage for @Test is not met
- XmlTest#setGroupInstances was not being shown in toXml().
- [GITHUB-376](https://github.com/testng-team/testng/issues/376): Some results can be lost (Konstantin Savin).
- Handle relative paths of Suite XML files properly (Nalin Makar)

## [6.8.5] - 2013-05-13

### Fixed

- the OutOfMemoryException in reports
- Surefire + listeners "Can't load class" problem

## [6.8.1] - 2013-03-30

### Added

- Descriptions in the HTML reports
- Various improvements to EmailableReporter (Abraham Lin)
- Allow injection of java.lang.reflect.Constructor and org.testng.ITestNGMethod into DataProvide (Vladislav Rassokhin)
- **Eclipse:** Predefined listeners (Tim Wu)

### Fixed

- Assertions in the Assertions class were not failing properly.
- [GITHUB-337](https://github.com/testng-team/testng/issues/337): ConfigurationMethod#m_instance set to Boolean.FALSE due to incorrect constructor call in clone() + auto-boxing (davidely)
- Fix NPE for dependency methods/groups (Krishnan Mahadevan)
- preserve-order bug (found by VladSarrokhin).
- [GITHUB-300](https://github.com/testng-team/testng/issues/300): OutOfMemoryException from reporters when there are a lot of tests
- [GITHUB-137](https://github.com/testng-team/testng/issues/137): Main parameters with a default value should be overridden if a main parameter is specified
- [GITHUB-107](https://github.com/testng-team/testng/issues/107): Allow enum values without converting them to uppercase.
- @Guice with no modules specified is now supported
- Reporter.log() invoked from listeners were being discarded
- **Eclipse:** Compare dialog

## [6.7] - 2012-07-15

### Added

- Big performance improvement when generating the reports (Frank Pavageau)
- `<dependencies>` allows you to specify group dependencies in testng.xml
- Blow up early if trying to include/exclude an unknown method
- `<parameters>` can now be specified under `<include>` (Storm Qi)
- [GITHUB-243](https://github.com/testng-team/testng/issues/243): Add Reporter Output per Test in XMLReporter (dunse)

### Fixed

- Better HTML escaping of the stack traces
- The failed assertions now use [] as delimiters instead of &lt;> (better for the HTML reports)
- [GITHUB-237](https://github.com/testng-team/testng/issues/237): Wrong time format in XML reporter
- Threads were started sequentially instead of being interleaved
- dataProvider(parallel = true) was not killing its threads properly
- XmlSuite#toXml wasn't outputting the `<groups>` tag correctly
- testng-failed.xml was not carrying over the parameters from the original testng.xml
- BeforeClass failing in parent failed to skip methods in sub classes
- Better error message if `<suite name="">` is missing
- [GITHUB-221](https://github.com/testng-team/testng/issues/221): Honor excludeGroups on testng tests when run in mixed mode (criccio)
- dependsOnGroups = {regexp} wasn't working properly (Alistair Ward)
- [GITHUB-205](https://github.com/testng-team/testng/issues/205): white-space was spelled whitespace in testng.css (carlin-scott)
- **Eclipse:** Environment is not transferred when rerunning failed tests.
- **Eclipse:** Rerunning failed tests will preserve the environment of the original launch

## [6.5.1] - 2012-04-10

### Added

- `<suite allow-return-values="true">` (and in `<test>` as well)
- data-provider attribute to testng-results.xml
- Reporter display the results in the same order as test methods (Libor Zoubek)
- Support for running JUnit 4 tests (Lukas Jungmann)
- Ability to auto-detect JUnit tests ('-mixed' mode) (Lukas Jungmann)
- Support for ResourceCollections in an Ant tasks (requires Ant >= 1.7.0) (Lukas Jungmann)
- **Eclipse:** `<suite allow-return-values="true">` (and in `<test>` as well)

### Fixed

- [GITHUB-198](https://github.com/testng-team/testng/issues/198): JUnitReportsReporter use commas in certain locales, which JUnitReports doesn't like
- [GITHUB-173](https://github.com/testng-team/testng/issues/173): Dependent methods executed out-of-order if method names match across classes (jjedMoriAnktah)
- ThreadLocal`<ITestResult>` leak (aslakknutsen)
- In the HTML reports, only show the first 100 characters of the parameters
- SkippedException are considered as real exception with @Test(expectedExceptions)
- **Eclipse:** Java constants are properly resolved if they are used as group names (susanin)
- **Eclipse:** @Test(groups = Foo.CONSTANT) (susanin)
- **Eclipse:** Failed tests with allow-return-values="true" were not rerun

## [6.4] - 2012-02-15

### Added

- @DataProvider(indices) to return specific indices of a data provider
- New HTML reports
- configfailurepolicy=continue with DataProviders (toddq)
- ITestResult#getTestContext (bpedman)
- **Eclipse:** You can now add the testng.jar sources as a library (Nick Tan)
- **Eclipse:** Upgraded the plug-in to 3.4+ (Nick Tan)
- **Eclipse:** dependsOnGroups now fully supported

### Fixed

- invocationCount > 1 + timeOut wasn't timing out properly
- When running TestNG programmatically, child xml suites are not run (when added using setSuiteFIles()) (Gaurav Gupta)
- [GITHUB-145](https://github.com/testng-team/testng/issues/145): Excessive test method execution (githubCast)
- [GITHUB-149](https://github.com/testng-team/testng/issues/149): reversed arguments in failAssertEqualsNoOrder().
- EmailableReporter: methods are now \*really\* sorted chronologically.
- **Eclipse:** @Parameters now works with both ("foo") and ({"foo"}) (davekerber)

## [6.3.1] - 2011-10-22

### Added

- New system property: dataproviderthreadcount (Bill Ross)

### Fixed

- Configuration methods were reported incorrectly in listeners.
- Was creating too many listeners (Jacek Pulut)
- IAnnotationTransformer2 beforeTest/afterTest booleans were not being set
- [GITHUB-92](https://github.com/testng-team/testng/issues/92): @BeforeTest method in a super class will be called multiple time when alwayRun = true (Bubuntux)
- [GITHUB-111](https://github.com/testng-team/testng/issues/111): @AfterClass on base classes run once too many (lrivera)
- [GITHUB-107](https://github.com/testng-team/testng/issues/107): Displaying 0 tests run if a listener modifies the parameters of the suite

## [6.3] - 2011-10-17

### Added

- "description" attribute on `<include>`, made available on ITestNGMethod#getDescription
- RemoteTestNG waits infinitely for a connection (Aleksey Kabanov)

### Fixed

- A method that's both a test and a factory would not invoke its data provider
- @AfterClass was not called if one of the methods was not enabled (Aleksey Kabanov)
- Groovy access bug
- The XML parser doesn't recognize parallel="instances"
- NPE when using inner classes
- [GITHUB-90](https://github.com/testng-team/testng/issues/90): @AfterClass not being run when the class contains included and not included methods
- @AfterClass not being run in some subclassing situations
- **Eclipse:** Verbose levels specified in suites not respected
- **Eclipse:** Variable substitution in VM arguments is not working properly (svenhoff)

## [6.2] - 2011-08-18

### Added

- xmlpathinjar to the TestNG ant task
- TestNG can now invoke package protected constructors
- Injectors created by the @Guice annotation are now shared at the `<test>` level
- IConfigurationListener is now a public listener, along with a new one: IConfigurationListener2
- When a method fails, only dependents of the same instance will be skipped
- parallel=instances for factory instance parallel runs
- @Factory(enabled)
- **Eclipse:** Each data provider method now has a separate node entry in the TestNG view

### Fixed

- JUnitReports reports now report the cumulated time @{Before,After}Method+@Test for each test method
- JUnitReports reports have the name of the `<test>` instead of that of the first class
- Using preserve-order with a factory that creates instances of a different class causes NPE
- [GITHUB-74](https://github.com/testng-team/testng/issues/74): Bad ordering of test methods when using a @Factory constructor with dataProvider
- Changing the test result from success to failure in a listener would still count the test as a success
- ServiceLoader wasn't resolving correctly if no service loader classloader was specified
- Better ordering with mixed priorities and dependencies
- Improved detection of graph cycles in parallel runs
- @BeforeTest was invoked multiple times if a factory is used
- [GITHUB-57](https://github.com/testng-team/testng/issues/57): Allow usage of package protected constructor of test classes
- Injecting both `Object[]` and Method in @BeforeMethod didn't always work
- testng-results.xml now lists the results chronologically
- @Listeners specified on a base class will only be run once per listener class (dbriones)
- -groups and -excludegroups were no longer overriding testng.xml
- **Eclipse:** Nodes in error would sometimes remain green
- **Eclipse:** The TestNG context menu no longer appears where it shouldn't

## [6.1.1] - 2011-07-05

### Fixed

- https://github.com/cbeust/testng/issues/56 testng-results.xml was reporting the instance name instead of the method name
- NPE when using preserve-order and factories.
- Depending on a skipped method would not cause a method to be skipped

## [6.1] - 2011-06-30

### Added

- Support for ServiceLoader for ITestNGListener
- @Factory(dataProvider / dataProviderClass) on constructors
- assertNotEquals() to Assert
- assertArrayEquals() to AssertJUnit
- Nested classes are now automatically added for consideration for inclusion
- `<suite preserve-order="true">` will cause this attribute to be propagated to all `<test>` tags
- `<groups>` can now be specified under a `<suite>`
- Tycho compatibility (Aleksander Pohl)
- New `<test>` and `<suite>` flag: group-by-instances
- -xmlpathinjar to specify the path of testng.xml inside a test jar file
- ISuite#getAllMethods, to retrieve all the methods at the start of a suite.
- Output ITestResult attributes in xml report (nguillaumin)
- **Eclipse:** New quick fix "Add static import org.testng.AssertJUnit.assertXXX"
- **Eclipse:** New workspace wide setting: excluded stack traces, to provide shorter stack traces in the view
- **Eclipse:** New "Clear results" icon in the tool bar
- **Eclipse:** When the search filter is modified, don't update the tree live if it is too big
- **Eclipse:** Two new @Test refactorings (pull to class level, push to method level)
- **Eclipse:** JUnit conversion: @Ignore
- **Eclipse:** JUnit conversion: assertArrayEquals()
- **Eclipse:** JUnit conversion: @RunWith(Parameterized.class)
- **Eclipse:** Support for Hamcrest failed assertions in the compare dialog
- **Eclipse:** JUnit conversion: suite() methods can now either be removed, commented out or left untouched

### Changed

#### Possible backward incompatible changes

- Don't mutate the value returned by XmlTest#getIncludedGroups and XmlTest#getExcludedGroups. Instead, use addIncludedGroup/addExcludedGroup.
- Failing methods that have dependees will only cause skips in the same instance. Different test instances will not be affected

### Fixed

- Thread safety problem in MethodInvocationHelper (Baron Roberts)
- Group dependencies were not being skipped properly.
- Dependency failures only impact the same instance
- Static classes could cause a StackOverFlowError
- IConfigurationListener was not extending ITestNGListener
- IConfigurationListener#onConfigurationFailure was never called
- TESTNG-476: `<test>` tags are now run in the order found in testng.xml
- Now showing failed/skipped error messages on the console for verbose >= 2
- ITestResult#getEndMillis() return 0
- TESTNG-410: Clearer error message
- TESTNG-475: @DataProvider doesn't support varargs
- Performance problems in EmailableReporter
- TESTNG-472: Better output for assertNull()
- ConcurrentModificationException when using parallel data providers.
- TESTNG-282: Problem when including+excluding packages (addicted)
- TESTNG-471: assertEquals(Map, Map) fails if a map is a subset of the other
- JUnitReporter generates an `<error>` tag for successful expectedExceptions tests
- ISSUE-47: Don't allow two `<test>`s with same name within same suite (Nalin Makar)
- If a listener implements both ISuiteListener and IInvokedMethodListener, only one of them gets invoked
- **Eclipse:** JUnit conversion: super.setUp()/tearDown() were being removed when extending a class other than TestCase
- **Eclipse:** "Run as" menu not appearing for methods that take a generic parameter.
- **Eclipse:** The tree was incorrect if the same class is used in different `<test>` tags
- **Eclipse:** When creating a new Run/Debug configuration, "Launch.label" was displayed
- **Eclipse:** TESTNG-459: TestNG menu should not always be present in context menu (Mykola Nikishov)
- **Eclipse:** Performance problems in the plug-in
- **Eclipse:** Workspace-wide XML template files are not being honored.
- **Eclipse:** @BeforeClass/@AfterClass from JUnit4 are not being properly converted
- **Eclipse:** Conversions generate @Test() instead of @Test

## [6.0] - 2011-03-16

### Added

- @Guice(moduleFactory) and IModuleFactory
- @Guice(module)
- timeOut for configuration methods
- -randomizesuites (Nalin Makar)
- IConfigurable
- **Eclipse:** Convert to YAML
- **Eclipse:** New global preference: JVM args
- **Eclipse:** Eclipse can now monitor a test-output/ directory and update the view when a new result is created
- **Eclipse:** Right clicking on a class/package/project now offers a menu "TestNG/Convert to TestNG"
- **Eclipse:** Excluded methods are now listed in the Summary tab
- **Eclipse:** "Description" column in the excluded methods table
- **Eclipse:** Dialog box when the plug-in can't contact RemoteTestNG
- **Eclipse:** Double clicking on an excluded method in the Summary tab will take you to its definition
- **Eclipse:** If you select a package before invoking the "New TestNG class" wizard, the source and package text boxes will be auto-filled
- **Eclipse:** When an item is selected in a tab, the same item will be selected when switching tabs
- **Eclipse:** A new "Summary" tab that allows the user to see a summary of the tests, sort them by time, name, etc...
- **Eclipse:** It's now possible "Run/Debug As" with a right click from pretty much any element that makes sense in the tree.
- **Eclipse:** JUnit conversion: correctly replaces assertNull and assertNotNull
- **Eclipse:** JUnit conversion: removes super.setUp() and super.tearDown()
- **Eclipse:** JUnit conversion: removes @Override
- **Eclipse:** JUnit conversion: replaces @Test(timeout) with @Test(timeOut) (5.14.2.4)
- **Eclipse:** JUnit conversion: replaces @Test(expected) with @Test(expectedExceptions) (5.14.2.4)
- **Eclipse:** JUnit conversion: replaces fail() with AssertJUnit.fail() (5.14.2.2)
- **Eclipse:** JUnit conversion: replaces Assert with AssertJUnit (5.14.2.1)
- **Eclipse:** The progress bar is now orange if the suite contained skipped tests and no failures
- **Eclipse:** Skipped test and suite icons are now orange (previously: blue)
- **Eclipse:** New method shortcuts: "Alt+Shift+X N", "Alt+Shift+D N" (Sven Johansson)
- **Eclipse:** "Create TestNG class" context menu
- **Eclipse:** When generating a new class, handle overridden methods by generating mangled test method names
- **Documentation:** Section on Selenium (Felipe Knorr Kuhn)
- **Documentation:** Link to an article on TestNG, Mockito and Emma in the Misc section

### Fixed

- @Test(priority) was not being honored in parallel mode
- @Test(timeOut) was causing threadPoolSize to be ignored
- TESTNG-468: Listeners defined in suite XML file are ignored (Michael Benz)
- TESTNG-465: Guice modules are bound individually to an injector meaning that multiple modules can't be effectively used (Danny Thomas)
- Method selectors from suites were not properly initialized (toddq)
- Throw an error when two data providers have the same name
- Better handling of classes that don't have any TestNG annotations
- XmlTest#toXml wasn't displaying the thread-count attribute
- TESTNG-438: Regression in 5.14.1: JUnit Test Execution no longer working
- TESTNG-436: Deep Map comparison for assertEquals() (Nikolay Metchev)
- Skipped tests were not always counted.
- test listeners that throw were not reporting correctly (ansgarkonermann)
- `<suite junit="true">` wasn't working.
- In parallel "methods" mode, method interceptors that remove methods would cause a lock up
- EmailableReporter now sorts methods chronologically
- TESTNG-411: Throw exception on mismatch of parameter values (via DP and/or Inject) and test parameters
- IDEA-59073: exceptions that don't match don't have stack trace printed in console (Anna Kozlova)
- IDEA's plug-in was not honoring ITest (fixed in TestResultMessage)
- Methods depending on a group they belong were skipped instead of throwing a cycle exception
- TESTNG-401: ClassCastException when using a listener from Maven
- TESTNG-186: Rename IWorkerApadter to IWorkerAdapter (Tomas Pollak)
- TESTNG-415: Assert.assertEquals() for sets and maps fails with 'null' as arguments
- typo -testRunFactory
- NPE while printing results for an empty suite (Nalin Makar)
- Invoke IInvokedMethodListener.afterInvocation after fixing results for tests expecting exceptions (Nalin Makar)
- TESTNG-441: NPE in SuiteHTMLReporter#generateMethodsChronologically caused by a race condition (Slawomir Ginter)
- **Eclipse:** Green nodes could override red parent nodes back to green
- **Eclipse:** Was trying to load the classes found in the XML template file
- **Eclipse:** Stack traces of skipped tests were not showing in the Exception view
- **Eclipse:** XML files should be run in place and not copied.
- **Eclipse:** NPE when you select a passed test and click on the Compare Result icon (Mohamed Mansour)
- **Eclipse:** When the run is over, the plug-in will no longer force the focus back to the Console view
- **Eclipse:** The counter in the progress bar sometimes went over the total number of test methods (5.14.2.9)
- **Eclipse:** org.eclipse.ui.internal.ErrorViewPart cannot be cast to org.testng.eclipse.ui.TestRunnerViewPart (5.14.2.9)
- **Eclipse:** Workspace preferences now offer the "XML template" option as well as the project specific preferences (Asiel Brumfield)
- **Eclipse:** TESTNG-418: Only last suite-file in testng.xml run by Eclipse plugin

## [5.14.7] - 2011-01-27

Release for IDEA

## [5.14.1] - 2010-10-02

### Fixed

- TESTNG-401: ClassCastException when using a listener from Maven

## [5.14] - 2010-08-28

### Added

- test suites can now be run in parallel with -suitethreadpoolsize

### Fixed

- @Listeners now aggregate through base classes
- ISuite was no longer serializable
- Injection was sometimes not working properly when used with @Parameters
- TESTNG-400: afterMethod was called after onTestFailure()
- "excludedgroups" was not working on the ant task because of a typo
- ant task error if `<classfileset>` is used with no classes (welex91)
- TESTNG-404: threaded tests fail due to use of non-threadsafe collections (Marcus Better)
- preserve-order was not preserving class order with dependent methods
- RetryAnalyzer wasn't working properly with factories
- The ant task was no longer supporting ',' for testclass
- **Eclipse:** The plug-in wasn't running Groovy tests correctly (Andrew Eisenberg)
- **Eclipse:** TESTNG-402 [Eclipse Plug-In] NPE occurred when I run twice a custom "Run configuration" on a group

## [5.13.1] - 2010-08-05

**Doc:** Updated Maven documentation (Brett Porter)

### Added

- -methods
- -configfailurepolicy (Todd Quessenberry)
- -methodselectors (Todd Quessenberry)
- @NoInjection
- `<test preserve-order="true">`
- -testnames (command line) and testnames (ant)
- New ant task tag:  propertyset (Todd Wells)
- ITestNGListenerFactory
- Passing command line properties via the ant task and doc update (Todd Wells)
- Hierarchical XmlSuites (Nalin Makar)
- Reporter#clear()

### Fixed

- NullPointerException when a suite produces no results (Cefn Hoile)
- Identical configuration methods were not always invoked in the correct order in superclasses (Nalin Makar)
- @DataProvider(parallel = true) was passing incorrect parameters with injection
- Replaced @Test(sequential) with @Test(singleThreaded)
- If inherited configuration methods had defined deps, they could be invoked in incorrect order (Nalin Makar)
- Initialize all Suite/Test runners at beginning to catch configuration issues right at start (Nalin Makar)
- Issue7: Issue86 Incorrect dates reported for configuration methods
- Issue24: OOM errors in SuiteHTMLReporter (Nalin Makar)
- Time outs specified in XML were not honored for `<suite parallel="tests">`
- `<suite>` and `<test>` time outs were hardcoded, they now honor their time-out attribute
- TestNG was hanging if no test methods were found
- onTestSuccess() was called after @AfterMethod instead of after the test method (test: test.listener.ListenerTest)
- XML test results contained skipfailedinvocationCounts instead of skipfailedinvocationcounts
- Issue4 assertEquals for primitive arrays, Issue34 assertNull javadoc updated
- Issue78 NPE with non-public class. Now throws TestNG exception
- NPE with @Optional null parameters (Yves Dessertine)
- TESTNG-387 TestNG not rerunning test method with the right data set from Data Provider (Francois Reynaud)
- Show correct number of pass/failed numbers for tests using @DataProvider
- Return correct method status and exception (if any) in InvokedMethodListener.afterInvocation()
- Trivial fixes: TESTNG-241 (log message at Info), Issue2 (throw SAXException and not NPE for invalid testng xml)
- Configuration methods couldn't depend on an abstract method (Nalin Makar)
- TestNG#setTestClasses was not resetting m_suites
- Exceptions thrown by IInvokedMethodListeners were not caught (Nalin Makar)
- @Listeners now works on base classes as well
- Test priorities were not working properly in non-parallel mode
- @Listeners wasn't working properly with ITestListener
- **Eclipse:** TESTNG-395 New wizard was creating classes called "NewTest"
- **Eclipse:** TESTNG-397 Class level @Test was preventing groups from showing up in the launch configuration

## [5.12.1] - 2010-03-29

Maven update

## [5.12]

### Added

- @Listeners
- IAttributes#getAttributeNames and IAttributes#removeAttribute
- testng-results.xml now includes test duration in the `<suite>` tag (Cosmin Marginean)
- Injection now works for data providers
- TestNG#setObjectFactory(IObjectFactory)
- Priorities: @Test(priority = -1)
- New attribute invocation-numbers in `<include>`
- testng-failed.xml only contains the data provider invocations that failed
- IInvokedMethodListener2 to have access to ITestContext in listeners (Karthik Krishnan)
- **Eclipse:** New file wizard: can now create a class with annotations, including @DataProvider
- **Eclipse:** You can now select multiple XML suites to be run in the launch dialog

### Removed

- Javadoc annotation support

### Fixed

- @Before methods run from factories were not properly interleaved
- The TextReporter reports skipped tests as PASSED (Ankur Agrawal)
- **Eclipse:** @Test(groups = `<constant>`) was taking name of the constant instead of its value.
- **Eclipse:** https://jira.codehaus.org/browse/GRECLIPSE-476 NPE with Groovy Tests (Andrew Eisenberg)
- **Eclipse:** The custom XML file is now created in the temp directory instead of inside the project
- **Eclipse:** In the launch dialog, now display an error if trying to pick groups when no project is selected
- **Eclipse:** Was not setting the parallel attribute correctly on the temporary XML file

## [5.11] - 2009-12-08

### Added

- Dependent methods can now run in their own thread
- dataProviderThreadCount can be set from the command line and from ant (Adrian Grealish)
- ITestAnnotation#setDataProvider
- Assert#assertEquals() methods for Sets and Maps
- New "parallel" preference setting (Windows / Preferences / TestNG)

### Fixed

- The text reporter was no longer reporting stack traces for verbose >= 2
- dataProviderClass was not respecting inheritance (like most attributes still)
- @BeforeSuite/@AfterSuite would run multiple times when used in a @Factory
- packages=".\*" wasn't working properly (sandopolus)
- TestResult#getName now returns the description instead of the method
- @DataProvider and dependent methods were not skipping correctly (Francois Reynaud)
- TESTNG-347 suite with parallel="tests" and test with parallel="classes" doesn't work correctly (Rob Allen)
- TESTNG-67: @Configuration/@Factory methods in base class being ignored
- Inner test classes were not excluded properly (Carsten Gubernator)
- threadPoolSize without invocationCount was causing reporters not to be invoked
- A @Factory throwing an exception did not cause any error
- `<classfilesetref>` was not working properly in the ant task (Ed Randall)
- @BeforeClass methods were not running in parallel (Aidan Short)
- Test class with @ObjectFactory doesn't get instantiated via the factory
- Allow IObjectFactory to load from non-standard classloader (for PowerMock support) Eclipse 5.11.0.19:
- IIinvokedMethodListeners were not invoked

## [5.10]

### Added

- The output in the testng-results.xml is now sorted by the starting timestamp (Daniel Rudman)
- Better display of the test name and method description in the default and Emailable report
- If both -testjar and an XML file are provided on the command line, the latter will be used
- @Before and @After methods can be injected with the current XmlTest
- Methods that time out now display the stack trace showing where the time out occurred
- ITestResult#getAttribute and ITestResult#setAttribute
- @After methods can now be injected with an ITestResult
- @BeforeMethod and @AfterMethod methods can now be injected an ITestResult
- ISuite#getAttribute and ISuite#setAttribute to share data within a suite
- @Test(expectedExceptionsMessageRegExp = ".\*foo.\*")
- @DataProvider(parallel=true)

### Fixed

- @Test(dataProvider) was not working at the class level
- Display a better error message if the wrong exception is thrown with an expectedExceptions
- Classes created by factories were not run in the order they were created
- Dependent methods are now run closer to methods within their class
- xmlFileSet in ant was not working correctly (Sean Shou)
- Various oversights in the DTD (Will McQueen)
- XMLUtils was not escaping XML attribute values
- TESTNG-317: Sequence order mis-calculation: testing using suite in sequence for classes and same method names creates non-sequential order
- Test names (classes that implement org.testng.ITest) now appear more prominently in the HTML reports
- expectedExceptions=RuntimeException.class was not failing when no exception was throw
- TESTNG-291: Exceptions thrown by Iterable DataProviders are not caught, no failed test reported (Roberto Tyley)
- TESTNG-301: Need to include parameters in testNG report for test created by @Factory
- testng-failed.xml now includes skipped tests
- TestNG couldn't find Groovy files (Haw-Bin)
- **Eclipse:** TESTNG-313: Provide extension point to contribute test and report listeners (Erik Putrycz)
- **Eclipse:** Quick fixes no longer introduce deprecated annotations (Greg Turnquist)

## [5.9] - 2009-04-09

### Added

- New ant task boolean flag: delegateCommandSystemProperties (Justin)
- skipfailedinvocations under `<suite>` in testng-1.0.dtd (Gael Marziou / Stevo Slavic)
- -testrunfactory on the command line and in the ant task (Vitalyi Pamajonkov)
- TESTNG-298: parallel="classes", which allows entire classes to be run in the same thread
- @BeforeMethod can now declare `Object[]` as a parameter, which will be filled by the parameters of the test method
- IAnnotationTransformer2
- @Test(invocationTimeOut), which lets you set a time out for the total time taken by invocationCount
- IInvokedMethodListener
- -testjar supports jar file with no testng.xml file
- **Doc:** 5.20: IInvokedMethodListener
- **Doc:** -testjar

### Fixed

- IInvokedMethodListener wasn't properly recognized from the command line (Leonardo Rafaeli)
- TESTNG-309 Illegal default value for attribute in DTD file
- TESTNG-192: JUnit XML output includes wrong tests (Aleksandar Borojevic)
- Set a generated suite to default to non-parallel (Mark Derricutt)
- -testJar command line parsing bug
- testng-failed.xml didn't include the listeners
- annotation transformers were not run when specified in testng.xml
- TESTNG-192: JUnit XML output includes wrong tests (Borojevic)
- @Parameters was not working correctly on @BeforeMethods with @DataProvider used on @Test methods
- testng-failed.xml was sometimes incorrectly generated (Borojevic)
- TestNG-228: Assert.assertEqualsNoOrder
- TestNG-229: Assert.assertEquals does not behave properly when arguments are sets
- TESTNG-36: assertEquals(Collection actual, Collection expected, String message) may have bug
- TESTNG-296: Malformed jar URLs breaking -testJar
- TESTNG-297: TestNG seemingly never stops running while building failed test suite (Gregg Yost)
- TESTNG-285: @Test(sequential=true) works incorrectly for classes with inheritance
- TESTNG-254: XMLSuite toXML() ignores listeners
- TESTNG-276: Thread safety problem in Reporter class
- TESTNG-277: Make Reporter.getCurrentTestResult() public
- Potential NPE in XmlTest#getVerbose (Ryan Morgan)
- EmailableReporter only displayed the first group for each test method
- time-outs were not working in `<test>` and `<suite>`
- @BeforeTest failing in a base class would not cause subsequent test methods to be skipped
- TESTNG-195: @AfterMethod has no way of knowing if the current test failed
- TESTNG-249: Overridden test methods were shadowing each other if specified with `<include>`
- DataProviders from @Factory-created tests were all invoked from the same instance
- enabled was not working on configuration methods
- IIinvokedMethodListener was not correctly added in TestNG
- NPE in XmlSuite#toXml
- TESTNG-231: NullPointerException thrown converting a suite to XML (Mark)

## [5.8]

### Added

- TestNG-213: @Optional on a method parameter to allow optional @Parameters
- Methods that form a cycle are now shown when the cycle is detected
- Support for `<listeners>` in testng.xml
- IMethodInterceptor
- @TestInstance on a data provider method parameter
- antlib.xml to allow autodiscovery of Ant task definition
- **Doc:** Method Interceptor
- **Doc:** @Optional
- **Doc:** Doc for IMethodInterceptor (5.16) and TestNG listeners (5.18)
- **Doc:** 5.19: Dependency injection

### Fixed

- TestNG-220: Ignore class definition/loader issues when scanning classpath for implicit classes
- TestNG-224: Fix for relative suite filenames in XML file
- TestNG-214: SkipException and TimeBombSkipException should accept nested exceptions
- TestNG-211: new Parser(inputStream) doesn't work
- @AfterMethod(lastTimeOnly) didn't work properly with data providers
- name attribute on `<test>` is required

## [5.7]

### Added

- @BeforeMethod(firstTimeOnly) and @AfterMethod(lastTimeOnly)
- @BeforeMethods can now take a Method and ITestContext parameters (like @DataProvider)
- if @Parameter is missing from testng.xml then it is read from the System properties
- Attribute @Test#skipFailedInvocations
- RetryAnalyzer (experimental) (Jeremie)

### Fixed

- logging about abstract classes moved to level 5
- Don't run a @DataProvider method as a test when a class-level @Test is present
- TESTNG-169 Error message: `<method>` is depending on nonexistent method null ("null" is uninformative)
- -listener takes comma-separated classes

## [5.6] - 2007-06-14

### Added

- SkipException/TimeBombedSkipException for manual skipping
- `<tests>` can now be disabled at xml level using `<test enabled="false">`
- Suite files that only contain other suites do not get reported
- get/setAttribute to ITestContext
- plugging in factory objects to handle the actual instantiation of tests
- dataProvider to @Factory
- ISuite now gives access to the current XmlSuite
- Improved behavior for @Before/@AfterClass when using @Factory (https://forums.opensymphony.com/thread.jspa?threadID=6594&messageID=122294#122294)
- Support for concurrent execution for invocationCount=1 threadPoolSize>1 and @DataProvider (https://forums.opensymphony.com/thread.jspa?threadID=64738&tstart=0)
- New TestNG specific XML report, generated by default in 'xml' subdirectory of test-output
- support in strprotocol for passing the ITest.getTestName() information
- **Eclipse plug-in:** display ITest.getTestName()
- **IDEA plug-in:** Auto-completion for dependsOnMethods
- **IDEA plug-in:** Highlighting of invalid groups/methods in dependsOn\*

### Fixed

- @BeforeClass methods would incorrectly report cyclic graphs
- TESTNG-139 dependsOnMethods gets confused when dependency is "protected"
- TESTNG-141 junit attribute set to false in testng-failed.xml when it should be true
- TESTNG-142 Exceptions in DataProvider are not reported as failed test
- TESTNG-152 If DataProvider is not found, the exception message should tell exactly what happened
- **Eclipse plug-in:** Bug that made group launch configurations unusable
- **Eclipse plug-in:** The plugin doesn't create the correct launch configuration for @Factory
- **Eclipse plug-in:** Method based launchers cannot be editted
- **Eclipse plug-in:** Plugin hangs while executing test with dataprovider that sends \n, \r messages
- **IDEA plug-in:** IDEA 7.0 compatibility
- **IDEA plug-in:** occasional 'illegal arguments exception'
- **IDEA plug-in:** TESTNG-151 Final passing test result is not properly hidden

## [5.5] - 2007-01-25

### Added

- Support for thread-count at test level
- Method selectors receive a Context and can stop the chain with setStopped()
- @BeforeGroups/@AfterGroups can live in classes without @Test methods
- DataProvider can now take an ITestContext parameter
- Clean separation between @Test invocation events and @Configuration invocation events (see also TESTNG-111)
- Test instances created by @Factory now run in multiple threads in parallel mode

### Fixed

- @BeforeGroup methods were run twice when in a base class
- @BeforeGroup methods were run twice with a @Test at class level
- parallel="tests" didn't work as advertised
- XmlMethodSelector was always run first regardless of its priority
- Wasn't parsing `<selector-class-name>` correctly
- Annotation Transformers now work on class-level annotations
- Some class-level @Test attributes were not always honored
- @Before/@AfterGroups invocation order
- TESTNG-27: Parameters are not used on `<test>` level anymore
- TESTNG-107 don't create an output directory if "outputDirectory" is null
- TESTNG-127 UseDefaultListeners in Ant Task does not work
- TESTNG-119 Running TestNG runner with invalid '-sourcedir' on JDK14 JavaDoc annotated test classes won't fail.
- TESTNG-113 Dependent methods within the same static inner class are not found
- TESTNG-125 TestNG failed for test classes under \*.java\*.\* pakages
- **Eclipse plug-in:** issue with launch configuration
- **Eclipse plug-in:** TESTNG-124: setting location of testng reports output

## [5.4]

### Added

- for @BeforeGroups and @AfterGroups specifying the groups() attribute will auto-include the method into those groups by default (previously you had to also provide the value() attribute).
- the load @Tests (invocationCount + threadPoolSize) are triggered simultaneous
- @DataProvider name defaults to method name
- support for remote protocol to pass parameter information

### Fixed

- Ant task issue with paths containing spaces
- reports are correctly displaying the thread info
- TextReporter logs information about the parameters of the test methods
- concurrency issue in JUnitXMLReporter
- output of JUnitXMLReporter must be CDATA
- XML unsupported annotations/parallel attribute values are reported
- **Eclipse plug-in:** groups with multi-attribute javadoc annotations
- **Eclipse plug-in:** consistent behavior for dependsOnMethods
- **Eclipse plug-in:** consistent behavior for tests with dependsOnGroups (a warning is emitted)
- **Eclipse plug-in:** consistent merge of configuration arguments when an existing launch configuration exists

## [5.3] - 2006-10-30

### Added

- @Before/@AfterMethod can declare a java.lang.reflect.Method parameter to be informed about the @Test method
- improved multiple suite summary page report
- -target option deprecated in favor of -annotations javadoc|jdk
- A generic/extensible RemoteTestNG was added to the core
- Attribute dataProviderClass for @Test and @testng.test
- testng.xml now supports `<suite-files>`
- ant task can receive several listeners
- ant task can now select the parallel mode for running tests
- ant task can override default suite and test names
- comand line support for setting parallel mode, suite and test names
- **Eclipse plug-in:** Support for configuring per project usedefaultlisteners
- **Eclipse plug-in:** Contextual drop-down menu on failures tab of the TestNG view to enable running/debugging method failure only
- **Eclipse plug-in:** Support for configuring per project TestNG jar usage (project provided one or plugin provided one)

### Fixed

- use a single instance of bsh.Interpreter
- super classes must not be listed in testng-failures.xml
- parallel attribute must not appear if empty or null in testng-failures.xml
- parsing for javadoc annotations is done on request only
- filesets in the ant task didn't work if the paths have spaces in them
- Before/After Suite were behaving wrong in parallel execution
- Before/AfterGroup-s were behaving wrong when using invocationCount, dataProvider and threadPoolSize
- improved support for running different annotation type tests in the same suite
- testng-failed.xml was generated even if there were no failures/skipps
- -usedefaultlisteners was wrongly passed to JVM instead of TestNG options
- Forgot to account for cases where both invocationCount and DataProviders are present
- AfterGroups were invoked out of order with invocationCount and DataProviders
- Reporter.getOutput() returned an empty array if a timeOut was specified
- TESTNG-109 Skipped tests with expected exceptions are reported as failures
- ant task correctly deals with empty groups and excludedgroups parameters

## [5.2]

### Added

- "-usedefaultlisteners true/false" to command line and ant
- EmailableReporter (from Paul Mendelson)
- parallel can now be "methods" or "tests". Boolean version deprecated
- TestNGAntTask now uses the @ syntax to invoke TestNG
- Command line understands @ syntax
- JUnitConverter uses the new syntax
- -groups to JUnitConverter
- completely revamped JUnit support (should run all kind of JUnit tests)
- **Eclipse plug-in:** TESTNG-105 Automaticaly define TESTNG_HOME classpath variable

### Fixed

- Throw proper exception when a DataProvider declares parameters
- TESTNG-40 (Bug in testng-failed.xml generation)
- TESTNG-106 (Failed "@BeforeSuite" method just skipps the last test in xml-file)
- Success on 0 tests (https://forums.opensymphony.com/thread.jspa?threadID=41213)

## [5.1] - 2006-08-18

### Added

- @Test(sequential = true)
- support for specifying test-only classpath (https://forums.opensymphony.com/thread.jspa?messageID=78048&tstart=0)
- **Eclipse plug-in:** run contextual test classes with parameters from suite definition files
- **Eclipse plug-in:** TESTNG-100 (Show HTML reports after running tests)
- **Eclipse plug-in:** TESTNG-97 (Double click top stack to raise comparison)
- **Eclipse plug-in:** TESTNG-84 (plug-in UI for suite option should support absolute path)
- **Eclipse plug-in:** TESTNG-20 (copy stack trace)

### Changed

- **Eclipse plug-in:**  TESTNG-98 (temporary files have guaranteed fixed names) TESTNG-95 (Assertion failed comparison trims trailing ">") TESTNG-70 (TestNG prevents eclipse from opening an older CVS version of a java class) display of test hierarchy information (TESTNG-29)

### Fixed

- TESTNG-102 (Incorrect ordering of @BeforeMethod calls when a dependency is specified)
- TESTNG-101 (HTML output contains nested `<P>` tags and a missing `<tr>` tag)
- TESTNG-93 (method selectors filtering @BeforeMethod)
- TESTNG-81 (Assert.assertFalse() displays wrong expected, actual value)
- TESTNG-59 (multiple method selectors usage results in no tests run)
- TESTNG-56 (invocation of @Before/AfterClass methods in parallel/sequential scenarios)
- TESTNG-40 (failures suite does not contain @Before/After Suite/Test methods)
- TESTNG-37 (allow passing null parameter value from testng.xml)
- TESTNG-7 (display classname when hovering method)
- **Eclipse plug-in:** TESTNG-72 (display groups with non-array values)
- **Eclipse plug-in:** TESTNG-64 (Eclipse plug-in applies added groups to all launch configurations)
- **Eclipse plug-in:** TESTNG-28 (Cannot select groups from dependent eclipse projects)
- **Eclipse plug-in:** TESTNG-25 (do not display fully qualified method name when running contextual test class)

## [5.0.1]

### Added

- **Eclipse plug-in:** Output directory for the tests
- **Eclipse plug-in:** Can now specify listener classes

### Fixed

- reports generated by SuiteHTMLReporter do not work with JDK1.4

## [5.0] - 2009-04-01

### Added

- Ant task: support for JVM, workingDir, timeout
- Stack traces can be interactively shown in the HTML reports
- Link to testng.xml in the reports
- New structure for reports, suites go in their individual directory
- @Test(suiteName) and @Test(testName)
- The stack traces in reports do not include TestNG frames (system property testng.exception) (see: https://groups.google.com/group/testng-dev/browse_thread/thread/9f4d46ade10b0fda)
- List of methods in alphabetical order
- Deprecated @Configuration and introduced @BeforeSuite/Test/Class/TestMethod
- Deprecated @ExpectedExceptions and moved it into @Test
- expectedExceptions to @Test, deprecated @ExpectedExceptions
- New annotations:  @BeforeSuite, @BeforeTest, etc...
- Interface org.testng.ITest so that tests can declare a name
- IHookCallBack now receives the ITestResult in its run() method
- Name of suite for command line can be set with -Dtestng.suite.name=xxx
- "listener" to the ant task (and documentation)
- if patch-testng-sourcedir.properties is found in the classpath with a property "sourcedir" containing a ; separated list of directories, this list will override -sourcedir.

### Fixed

- Exit with error when no methods are run (see: https://groups.google.com/group/testng-dev/browse_thread/thread/3c26e8a5658f22ac)
- Class-scoped annotations were not recognized when inherited
- Was returning an exit code of 0 if a cyclic graph was detected
- The Text reporter was reporting the square of the actual number of methods
- Bug reported by Eran about dependencies with an afterClass method
- TestNGAntTask was hardcoding m_haltOnFSP to true
- Passing a null parameter caused an NPE in the reports

## [4.7]

### Added

- Maven 2 plug-in
- alwaysRun for before @Configuration methods https://jira.opensymphony.com/browse/TESTNG-35
- beforeGroups/afterGroups to @Configuration
- **Eclipse plugin:** last contextual launch is available in Eclipse launcher lists

### Fixed

- Message formattings in TestNG assertion utility class
- @Factory methods were counted as @Test as well https://jira.opensymphony.com/browse/TESTNG-51
- All DataProvider parameters were shown in the HTML report
- Bug in testng-failed.xml generation
- `<packages>` bug when using a jar file to load the test classes
- groupless @Configurations were not invoked if a method depends on a group https://jira.opensymphony.com/browse/TESTNG-45
- **Eclipse plugin:** 3.2M5 integration (removed dependency on non-existing class)
- **Eclipse plugin:** testng-failures.xml generation

## [4.6] - 2006-02-27

### Added

- Documentation contains the new reports
- TestNG.setUseDefaultListeners(boolean)
- Descriptions now appear in TextReporter (verbose>=2) and the HTML reports
- description attribute to @Test and @Configuration
- combined Reporter output in the reports
- methods not run in the reports
- org.testng.IReporter
- threadPoolSize to @Test
- Reports now show relative timings (start at 0)
- Reports now show different colors depending on the methods' classes
- Reports now show all parameters used to invoke the test method
- org.testng.Reporter
- DataProviders can accept a Method as first parameter
- **Eclipse plugin:** Run/Debug as TestNG test from the editor contextual menu
- **Eclipse plugin:** UI allows setting orientation (even more space) https://forums.opensymphony.com/thread.jspa?threadID=17225&messageID=33805#33805

### Fixed

- Extraneous implicit inclusion of a method
- **Eclipse plugin:** TESTNG-24: 'Run as testng test' does not appear of the Test annotation does not have a group
- **Eclipse plugin:** TESTNG-18: Eclipse plugin ignores Factory annotation
- **Eclipse plugin:** TESTNG-21: Show differences when double clicking assertion exceptions

## [4.5] - 2007-07-02

**Eclipse plug-in:** New look for the progress view.

### Added

- Support for JAAS (see org.testng.IHookable)
- dependsOnMethods can contain methods from another class
- Regular expressions for classes in `<package>`
- Distributed TestNG
- @Configuration.inheritGroups

### Fixed

- Methods were not implicitly included, only groups
- Bug with failed parent @Configuration don't skip child @Configuration/@Test invocations
- Bug with overridding @Configuration methods (both parent and child were run)
- Bug when overriding beforeClass methods in base class (cyclic graph)
- Problem with nested classes inside &lt;package name="foo.\*"
- If a group is not found, mark the method as a skip instead of aborting
- testng-failed.xml was not respecting dependencies
- class/include method in testng.xml didn't work on default package
- DTD only allowed one `<define>`
- ArrayIndexOutOfBoundsException for jMock
- JUnitConverter required -restore, not any more (option is now a no-op)
- JUnit mode wasn't invoking setName() on test classes
- Command line parameters and testng.xml are now cumulative
- Reports now work for multiple suites
- Was ignoring abstract classes even if they have non-abstract instances
- If setUp() failed, methods were not skipped
- Was not clearly indicating when beforeSuite fails
- inconsistency between testng.xml and objects regarding method selectors

## [4.4]

### Fixed

- testng-failures.xml was not excluding methods from base classes
- Bug in suites of suites for JUnit mode

## [4.3]

### Added

- Excluded groups on command line and ant task

### Fixed

- testng-failures.xml was not excluding methods from base classes
- Bug in suites of suites for JUnit mode
- When including a group, implicitly include groups depended upon
- When depending on several groups, wasn't skipped if one of them failed
- Failures weren't reported accurately in the JUnitReports report
- Wasn't throwing an exception if depending on a non-existing group

## [4.2]

### Added

- alwaysRun for tests (soft dependencies)

### Fixed

- wasn't excluding methods in base classes
- Class-level enabled=false were not honored
- Bug with multiple dataproviders on same class
- Bug with dataprovider defined in the parent class
- Bug with dataprovider defined in a subclass
- Bug with dataprovider defined in an abstract class
- testng-failures generation was excluding the methods even if a failed test depended on it

## [4.1]

### Added

- @DataProviders can return Iterable`<Object[]>`
- Reporter class to log messages in the HTML reports

### Fixed

- Superclass test methods were not called in the presence of a class @Test

## [4.0] - 2005-11-10

### Added

- @DataProvider and @testng.data-provider
- Can now invoke java -jar testng-2.6.jar &lt;...>
- Support for BeanShell
- Method Selectors (IMethodSelector)
- **Eclipse plug-in:** Quick Fix for JUnit conversion (Annotations and JavaDoc)
- **Eclipse plug-in:** Package level Run as TestNG test
- **IDEA plug-in:** Support for JDK 1.4 (both projects and IDEA itself)

### Fixed

- suite methods now invoked only once in a hierarchy
- Interleave order now respected for before/afterClass methods
- In the absence of dependencies, @Configuration methods respect inheritance
- Bug in multithreaded dependencies on methods
- dependsOnGroups wasn't working on regular expressions
- Bug in `<package>` when directories contain spaces in their names
- Introduced a JDK5 dependency in the JDK1.4 build (getEnclosingClass())
- Output directory in ant task was not honored if it didn't exist
- Problem with timeout according to https://forums.opensymphony.com/thread.jspa?threadID=6707
- **Eclipse plug-in:** Wasn't handling linked directories correctly
- **Eclipse plug-in:** Bug in QuickFix implementation
- **Eclipse plug-in:** Methods Run as TestNG test
- **Eclipse plug-in:** Resources from the linked directories are using a wrong path when passed to command line TestNG
- **IDEA plug-in:** Classes that contained only configuration were ignored

## [2.5] - 2005-08-08

**IDEA plug-in:** First release!

### Added

- ITestListener.onTestStart(ITestResult)
- Support for `<packages>`
- Resource files for easier ant taskdefs
- alwaysRun
- @Configuration(alwaysRun)
- JUnitConverter task
- **Documentation:** Brand new look!!!
- **Documentation:** Section on testng.xml

### Fixed

- @Configuration methods were not invoked with individual test methods
- Bug with ExpectedExceptions
- Didn't support nested factory classes
- NPE if -target is omitted with JDK 1.4
- @Configuration failures in a class would cause other classes to fail
- beforeTestClass/afterTestClass were broken for a pathological case
- &lt; and > characters in reports were not escaped
- **Eclipse plug-in:** Class dialog wasn't showing @Factory classes
- **Documentation:** Numbering of sections

## [2.4] - 2005-07-05

### Added

- IInstanceInfo support
- command line (and Ant) -groups option
- @Parameters (and made parameters attribute deprecated)
- Parameters for constructors
- **Documentation:** New assert classes
- **Documentation:** New ways to launch
- **Documentation:** JUnitConverter documentation
- **Documentation:** new beforeSuite/afterSuite

### Changed

- New package:  testng.org

### Fixed

- Bug with @ExpectedException occuring the parallel mode
- Bug with parameters and beforeTest
- methods were not excluded when included by groups
- testng-failures.xml is now including also the beforeSuite/afterSuite methods
- generating the testng-failures.xml is now working as expected
- Factories call all the tests even if some of them fail along the way
- Better JUnit support (wasn't creating individual instances)
- dependsOnGroups didn't work across different classes
- Better interleaving of before/afterTestMethods
- Ant task
- TestNGException thrown when TestNG conditions are not fulfilled

## [2.3] - 2005-04-12

### Added

- documentation for @Factory
- beforeSuite and afterSuite
- Better stack traces
- Better syntax for included/excluded methods
- Can specify a class name on the command line
- Regression tests for JUnitConverter
- -quiet option to JUnitConverter

### Fixed

- Spaces are now legal in JavaDoc comments
- factories were called multiple times
- inheritance and scope now working properly for annotations
- dependsOnMethods wasn't working for 1.4
- Better verbose support
- Various fixes for the Eclipse plug-in
- Default package bug in JUnitConverter

## [2.2]

### Added

- In 1.4, don't require annotations="javadoc"

### Fixed

- Wasn't handling several testng.xml files correctly
- Renamed -src to -sourcedir
- Complains if no sourcedir is specified in 1.4
- If setUp fails, complain and mark test methods as skips
- Dependent methods weren't working for 1.4

## [2.1] - 2005-02-12

### Added

- Parser can accept an InputStream for testng.xml
- invocationCount and successPercentage
- dependsOnMethods
- timeOut works in non-parallel mode

### Fixed

- expected-exceptions now fails if test passes
- reports now use the suite name in HTML

## [2.0] - 2004-12-06

### Added

- port on JDK 1.4

## [1.3]

### Added

- new view:  classes (still experimental)
- timeout on methods
- thread-count
- TestNG is now multithread, see "parallel" in `<suite>`

## [1.2]

### Added

- JUnitConverter

### Fixed

- Bug with afterClasses (test: AfterClassCalledAtTheEnd)

## [1.1]

### Added

- new links for methods and groups in the HTML report
- `<methods>`
- `<fileset>` to `<testng>`

## [1.0] - 2004-04-28

https://beust.com/weblog/2004/04/28/

### Added

- Validating testng.xml
- Scoped parameters
- testng.xml
- Dependent methods

### Changed

- Verbose is now an integer

### Removed

- Property quiet

### Fixed

- Updated to the new DTD
- Suite table of contents displays failures first
- Bug in afterTestClass

## [0.9]

### Added

- Groups of groups
- Groups for Configuration methods
- Parameters

## [0.2]

### Added

- HTML report
- Regexps for groups

### Fixed

- Merged TestMethod and TestClass into Test
- Inheritance of methods
- ExpectedException is now called ExpectedExceptions

[Unreleased]: https://github.com/testng-team/testng/compare/7.12.0...HEAD
[7.12.0]: https://github.com/testng-team/testng/compare/7.11.0...7.12.0
[7.11.0]: https://github.com/testng-team/testng/compare/7.10.2...7.11.0
[7.10.2]: https://github.com/testng-team/testng/compare/7.10.1...7.10.2
[7.10.1]: https://github.com/testng-team/testng/compare/7.10.0...7.10.1
[7.10.0]: https://github.com/testng-team/testng/compare/7.9.0...7.10.0
[7.9.0]: https://github.com/testng-team/testng/compare/7.8.0...7.9.0
[7.8.0]: https://github.com/testng-team/testng/compare/7.7.1...7.8.0
[7.5.1]: https://github.com/testng-team/testng/compare/7.5...7.5.1
[7.7.1]: https://github.com/testng-team/testng/compare/7.7.0...7.7.1
[7.7.0]: https://github.com/testng-team/testng/compare/7.6.1...7.7.0
[7.6.1]: https://github.com/testng-team/testng/compare/7.6.0...7.6.1
[7.6.0]: https://github.com/testng-team/testng/compare/7.5...7.6.0
[7.5]: https://github.com/testng-team/testng/compare/7.4.0...7.5
[7.4.0]: https://github.com/testng-team/testng/compare/7.3.0...7.4.0
[7.3.0]: https://github.com/testng-team/testng/compare/7.0.0...7.3.0
[7.0.0]: https://github.com/testng-team/testng/compare/6.14.3...7.0.0
[6.14.3]: https://github.com/testng-team/testng/compare/6.14.2...6.14.3
[6.14.2]: https://github.com/testng-team/testng/compare/6.13.1...6.14.2
[6.13.1]: https://github.com/testng-team/testng/compare/6.13...6.13.1
[6.13]: https://github.com/testng-team/testng/compare/6.12...6.13
[6.12]: https://github.com/testng-team/testng/compare/6.11...6.12
[6.11]: https://github.com/testng-team/testng/compare/6.10...6.11
[6.10]: https://github.com/testng-team/testng/compare/6.9.13...6.10
[6.9.13]: https://github.com/testng-team/testng/compare/6.9.12...6.9.13
[6.9.12]: https://github.com/testng-team/testng/compare/6.9.11...6.9.12
[6.9.11]: https://github.com/testng-team/testng/compare/6.9.10...6.9.11
[6.9.10]: https://github.com/testng-team/testng/compare/6.9.9...6.9.10
[6.9.9]: https://github.com/testng-team/testng/compare/6.9.8...6.9.9
[6.9.8]: https://github.com/testng-team/testng/compare/6.9.7...6.9.8
[6.9.7]: https://github.com/testng-team/testng/compare/testng-6.9.5...6.9.7
[6.9.5]: https://github.com/testng-team/testng/compare/testng-6.9.4...testng-6.9.5
[6.9.4]: https://github.com/testng-team/testng/compare/testng-6.8.21...testng-6.9.4
[6.8.21]: https://github.com/testng-team/testng/compare/testng-6.8.15...testng-6.8.21
[6.8.15]: https://github.com/testng-team/testng/compare/testng-6.8.5...testng-6.8.15
[6.8.5]: https://github.com/testng-team/testng/compare/testng-6.8.1...testng-6.8.5
[6.8.1]: https://github.com/testng-team/testng/compare/testng-6.7...testng-6.8.1
[6.7]: https://github.com/testng-team/testng/compare/testng-6.5.1...testng-6.7
[6.5.1]: https://github.com/testng-team/testng/compare/testng-6.4...testng-6.5.1
[6.4]: https://github.com/testng-team/testng/compare/testng-6.3.1...testng-6.4
[6.3.1]: https://github.com/testng-team/testng/compare/testng-6.3...testng-6.3.1
[6.3]: https://github.com/testng-team/testng/compare/testng-6.2...testng-6.3
[6.2]: https://github.com/testng-team/testng/compare/testng-6.1.1...testng-6.2
[6.1.1]: https://github.com/testng-team/testng/compare/testng-6.1...testng-6.1.1
[6.1]: https://github.com/testng-team/testng/compare/testng-6.0...testng-6.1
[6.0]: https://github.com/testng-team/testng/compare/testng-5.14.7...testng-6.0
[5.14.7]: https://github.com/testng-team/testng/compare/testng-5.14.1...testng-5.14.7
[5.14.1]: https://github.com/testng-team/testng/compare/testng-5.14...testng-5.14.1
[5.14]: https://github.com/testng-team/testng/compare/testng-5.13.1...testng-5.14
[5.13.1]: https://github.com/testng-team/testng/releases/tag/testng-5.13.1

# Resolving the TestNG DTD offline with the XML catalog

A suite file declares its DTD by URL:

```xml
<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
```

TestNG never downloads it. It recognizes the URL and reads the copy of the DTD in its own jar. A
tool that reads the suite file on its own, such as `xmllint`, Saxon, an IDE or a build plugin,
follows the URL instead, so validating a suite file needs the network.

The TestNG jar ships an [OASIS XML catalog](https://www.oasis-open.org/committees/download.php/14809/xml-catalogs.html)
that maps the DTD URLs onto the DTD in the jar. A tool that uses the catalog validates suite files
without the network.

## What the catalog maps

The catalog is the jar entry `org/xmlresolver/catalog.xml`. It maps the eight supported DTD
identifiers onto `testng-1.1.dtd` in the jar, the DTD TestNG uses for all of them:

| Host | Path | Schemes |
| --- | --- | --- |
| `testng.org` | `/testng-1.0.dtd`, `/testng-1.1.dtd` | `https`, `http` |
| `beust.com` | `/testng/testng-1.0.dtd`, `/testng/testng-1.1.dtd` | `https`, `http` |

TestNG itself is more lenient: it reads its bundled DTD for any URL on `testng.org` or `beust.com`.
The catalog maps only the eight identifiers above, so a suite file that declares another URL on
those hosts still runs in TestNG, but a tool that uses the catalog does not resolve it. Use one of
the eight identifiers in the doctype.

A suite file that declares an XML Schema with `xsi:noNamespaceSchemaLocation` instead of a doctype
does not benefit. A catalog resolves identifiers, and the schema location is not one. Point your
tool at `testng-1.1.xsd` explicitly instead.

## Enabling it

A catalog is never picked up just because it is on the classpath. Each tool has to be pointed at it.

### `xmlresolver` and Saxon

[`xmlresolver`](https://xmlresolver.org/), which Saxon 11 and later use, reads every
`org/xmlresolver/catalog.xml` on the classpath when classpath catalogs are enabled:

```sh
java -Dxml.catalog.classpathCatalogs=true -cp saxon.jar:xmlresolver.jar:testng.jar ...
```

### The JDK, `javax.xml.catalog`

The JDK resolver (Java 9 and later) only reads the catalogs it is given. Pass the catalog inside
the jar with the `javax.xml.catalog.files` system property:

```sh
java -Djavax.xml.catalog.files=jar:file:/path/to/testng.jar!/org/xmlresolver/catalog.xml ...
```

or programmatically:

```java
URI catalog = TestNG.class.getClassLoader().getResource("org/xmlresolver/catalog.xml").toURI();
CatalogResolver resolver =
    CatalogManager.catalogResolver(CatalogFeatures.defaults(), catalog);
xmlReader.setEntityResolver(resolver);
```

With `CatalogFeatures.Feature.RESOLVE` set to `strict`, an id the catalog does not map fails
instead of being downloaded.

### `xmllint` and other native tools

Tools that cannot read inside a jar need the catalog and the DTD as files. Extract both, keeping
their paths, and point the tool at the catalog:

```sh
unzip testng.jar org/xmlresolver/catalog.xml testng-1.1.dtd -d testng-catalog
XML_CATALOG_FILES=testng-catalog/org/xmlresolver/catalog.xml xmllint --noout --valid --nonet testng.xml
```

The catalog refers to the DTD with a relative path, so the two have to stay in the same layout.
`XML_CATALOG_FILES` is a space-separated list, so extract them to a path without spaces.

### IDEs

IntelliJ IDEA and Eclipse WTP have their own catalog settings, but whether they read a catalog from
a dependency jar has not been verified. Adding the extracted catalog in the XML catalog settings of
the IDE works the same way as for `xmllint`.

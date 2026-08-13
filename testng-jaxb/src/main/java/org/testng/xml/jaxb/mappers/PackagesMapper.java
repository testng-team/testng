package org.testng.xml.jaxb.mappers;

import java.util.List;
import java.util.stream.Collectors;
import org.testng.xml.XmlPackage;
import org.testng.xml.jaxb.Packages;

public class PackagesMapper {

  public static List<XmlPackage> toXmlPackages(Packages packages) {
    return packages.getPackage().stream()
        .map(p -> new XmlPackage(p.getName()))
        .collect(Collectors.toList());
  }
}

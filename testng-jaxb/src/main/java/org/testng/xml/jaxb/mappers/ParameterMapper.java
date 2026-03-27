package org.testng.xml.jaxb.mappers;

import java.util.Map;
import java.util.stream.Collectors;
import org.testng.xml.jaxb.Parameter;

public class ParameterMapper {

  public static Map<String, String> toXmlParameters(java.util.List<Parameter> parameters) {
    return parameters.stream().collect(Collectors.toMap(Parameter::getName, Parameter::getValue));
  }
}

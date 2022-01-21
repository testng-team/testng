package test.attributes.samples.custom

import org.testng.IInvokedMethod
import org.testng.IInvokedMethodListener
import org.testng.ITestResult
import org.testng.annotations.CustomAttribute

class CustomAttributesListener : IInvokedMethodListener {

  var attributes = mutableListOf<CustomAttribute>()

  @Override
  override fun afterInvocation(method: IInvokedMethod , testResult:ITestResult ) {
    this.attributes.addAll(method.testMethod.attributes.toList())
  }
}

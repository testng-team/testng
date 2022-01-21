package test.attributes.samples.custom

import java.lang.reflect.Constructor
import java.lang.reflect.Method
import org.testng.IAnnotationTransformer
import org.testng.annotations.CustomAttribute
import org.testng.annotations.ITestAnnotation
import kotlin.reflect.KCallable

class CustomAttributesTransformer : IAnnotationTransformer {

    @Override
    override fun transform(
        annotation: ITestAnnotation?,
        testClass: Class<*>?,
        testConstructor: Constructor<*>?,
        testMethod: Method?
    ) {
        val attributes = arrayOf(
            customAttribute("sorrow", arrayOf("Coffee", "Tea")))
        annotation?.attributes = attributes
    }

    // Support for creation annotation literals is still not there in Kotlin.
    // Refer to the Open Defect https://youtrack.jetbrains.com/issue/KT-25947
    // Below work-around is the only way in which we can create annotation literals in Kotlin
    // and has been borrowed from https://stackoverflow.com/q/64539969
    private fun customAttribute(name:String, values: Array<String>) : CustomAttribute{
        val constructors = CustomAttribute::class.constructors
        val callable: KCallable<*> = constructors.first()
        val param1 = callable.parameters[0]
        val param2 = callable.parameters[1]
        return callable.callBy(mapOf(param1 to name, param2 to values)) as CustomAttribute
    }

}

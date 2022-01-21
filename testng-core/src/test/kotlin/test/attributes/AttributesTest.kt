package test.attributes

import org.assertj.core.api.Assertions.assertThat
import org.testng.IAnnotationTransformer
import org.testng.annotations.CustomAttribute
import org.testng.annotations.Test
import test.SimpleBaseTest
import test.attributes.samples.custom.CustomAttributesListener
import test.attributes.samples.custom.CustomAttributesTransformer
import test.attributes.samples.custom.TestClassSample
import test.attributes.samples.issue2346.SingleTestSample
import test.attributes.samples.issue2346.TestWithMethodDependenciesSample

class AttributesTest : SimpleBaseTest() {

    @Test
    fun testAttributesAmidstMethodDependencies() {
        val tng = create(TestWithMethodDependenciesSample::class.java).run {
            run()
            this
        }
        assertThat(tng.status).isEqualTo(0)
    }

    @Test(description = "GITHUB-2346")
    fun ensureAttributesAreIntactForSkippedMethods() {
        val cls = SingleTestSample::class.java.canonicalName + ".test"
        createTests("sample_test", SingleTestSample::class.java).run {
            run()
        }
        val actual = test.attributes.samples.issue2346.data
        assertThat(actual["onTestSkipped_$cls"]).isFalse
        assertThat(actual["onTestStart_$cls"]).isFalse
        assertThat(actual["tearDown_$cls"]).isFalse
    }

    @Test
    fun ensureCustomAttributesAreAvailable() {
        runTest(null, "joy", listOf("KingFisher", "Bira"))
    }

    @Test
    fun ensureCustomAttributesCanBeAlteredViaAnnotationTransformer() {
        runTest(CustomAttributesTransformer(), "sorrow", listOf("Coffee", "Tea"))
    }

    private fun runTest(transformer: IAnnotationTransformer?, key: String, values: List<String>) {
        val listener = CustomAttributesListener()
        create(TestClassSample::class.java).run {
            addListener(listener)
            if (transformer != null) {
                addListener(transformer)
            }
            run()
        }
        val attributes: List<CustomAttribute> = listener.attributes
        assertThat(attributes).hasSize(1)
        val attribute = attributes[0]
        assertThat(attribute.name).isEqualTo(key)
        assertThat(attribute.values).containsAll(values)
    }
}

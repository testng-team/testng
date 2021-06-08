package org.testng.xml

import org.testng.annotations.BeforeClass
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File
import javax.xml.XMLConstants
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory
import javax.xml.validation.Validator

class XsdValidationTest {

    private lateinit var validator: Validator

    @DataProvider
    fun dp(): Iterator<Array<File>> {
        return loadResource("/samples")
            .walk()
            .filter { it.isFile && it.extension == "xml" }
            .map { arrayOf(it) }
            .iterator()
    }

    @BeforeClass
    fun setUp() {
        validator = newXsdValidator(loadResource("/testng-1.0.xsd"))
    }


    @Test(dataProvider = "dp")
    fun `validate xml suite files`(xml: File) {
        validator.validate(xml)
    }

    private fun loadResource(resource: String) = {}::class.java.getResource(resource).let {
        if (it == null) {
            throw IllegalArgumentException("No resource found: $resource")
        }
        File(it.file)
    }

    private fun newXsdValidator(xsd: File) = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
        .newSchema(xsd).newValidator()

    private fun Validator.validate(source: File) {
        this.validate(StreamSource(source))
    }
}

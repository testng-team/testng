package test.attributes.samples.issue2346

import org.assertj.core.api.Assertions.assertThat

import org.testng.ITestContext
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

class TestWithMethodDependenciesSample {

  @BeforeClass
  fun bc(ctx:ITestContext) {
    ctx.setAttribute("test", "1")
    }

  @Test
  fun f1(ctx:ITestContext) {
    val names = ctx.attributeNames
        assertThat(names).hasSize(1)
        assertThat(names).contains("test")
        assertThat(ctx.getAttribute("test")).isEqualTo("1")
        val v = ctx.removeAttribute("test")
        assertThat(v).isNotNull
        ctx.setAttribute("test2", "2")
    }

  @Test(dependsOnMethods = ["f1"])
  fun f2(ctx:ITestContext) {
    val names = ctx.attributeNames
        assertThat(names).hasSize(1)
        assertThat(names).contains("test2")
        assertThat(ctx.getAttribute("test2")).isEqualTo("2")
    }
}

package buildlogic

import java.io.File
import org.gradle.testfixtures.ProjectBuilder
import org.testng.Assert.assertEquals
import org.testng.Assert.assertFalse
import org.testng.Assert.assertTrue
import org.testng.annotations.Test

class ValeCheckTest {
    private fun task(): ValeCheck =
        ProjectBuilder.builder().build().tasks.register("valeForTest", ValeCheck::class.java).get()

    @Test
    fun addedLinesHandleSingleLineRangesDeletionAndMultipleFiles() {
        val top = File("test-repo").absolutePath
        val added = mutableMapOf<String, MutableSet<Int>>()
        val diff =
            """
            +++ b/one.md
            @@ -1 +3 @@
            @@ -5,2 +8,3 @@ context
            +++ b/deleted.md
            @@ -1,2 +0,0 @@
            +++ /dev/null
            @@ -1 +9 @@
            +++ b/two.md
            @@ -0,0 +1,2 @@
            """.trimIndent()
        task().collectAddedLines(top, diff, added)
        assertEquals(added[File(top, "one.md").absolutePath], setOf(3, 8, 9, 10))
        assertEquals(added[File(top, "two.md").absolutePath], setOf(1, 2))
        assertTrue(added[File(top, "deleted.md").absolutePath]!!.isEmpty())
        assertEquals(added.size, 3)
    }

    @Test
    fun repeatedDiffsAccumulateAndMalformedHunksDoNotInventLines() {
        val top = File("test-repo").absolutePath
        val added = mutableMapOf<String, MutableSet<Int>>()
        val check = task()
        check.collectAddedLines(top, "+++ b/a.md\n@@ -0,0 +2 @@", added)
        check.collectAddedLines(top, "+++ b/a.md\n@@ invalid @@\n@@ -0,0 +5,2 @@", added)
        assertEquals(added[File(top, "a.md").absolutePath], setOf(2, 5, 6))
    }

    @Test
    fun findingsAreLimitedToNewLinesUnlessTheWholeFileIsNew() {
        val path = "C:\\repo\\guide.md"
        val changed = ChangedFiles(setOf(path), mapOf(path to setOf(4)), setOf("new.md"))
        assertFalse(changed.introduced("$path:3:1:Rule:old"))
        assertTrue(changed.introduced("$path:4:1:Rule:new"))
        assertFalse(changed.introduced("other.md:4:1:Rule:other"))
        assertTrue(changed.introduced("new.md:99:1:Rule:new file"))
        assertTrue(changed.introduced("Vale diagnostic without a line number"))
    }

    @Test
    fun batchesPreserveOrderAndRespectTheArgumentBudget() {
        val files = (1..100).map { File("folder/" + "x".repeat(140) + it + ".md") }
        val batches = task().batches(files)
        assertTrue(batches.size > 1)
        assertEquals(batches.flatten(), files)
        assertTrue(batches.all { batch -> batch.sumOf { it.absolutePath.length + 1 } <= 8000 })
        assertTrue(task().batches(emptyList()).isEmpty())
        val longPath = File("x".repeat(8100))
        assertEquals(task().batches(listOf(longPath)), listOf(listOf(longPath)))
    }
}

package io.github.droidkit.feature.settings.about

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Guards the local About data files against empty/inconsistent content. */
class AboutDataTest {

    @Test
    fun `release notes are ordered newest first and non-empty`() {
        val versions = ReleaseNotes.releases.map { it.version }
        val sorted = versions.sortedWith { a, b -> VersionComparator.compare(a, b) }
        assertEquals(sorted.reversed(), versions)
        ReleaseNotes.releases.forEach { note ->
            assertTrue(note.version.isNotBlank())
            assertTrue(note.dateLabel.isNotBlank())
            assertTrue(note.highlights.isNotEmpty())
            note.highlights.forEach { assertTrue(it.isNotBlank()) }
        }
    }

    @Test
    fun `license entries are complete`() {
        assertTrue(OpenSourceLicenses.entries.isNotEmpty())
        OpenSourceLicenses.entries.forEach { entry ->
            assertTrue(entry.component.isNotBlank())
            assertTrue(entry.copyrightHolder.isNotBlank())
            assertTrue(entry.license.isNotBlank())
        }
    }

    @Test
    fun `faq categories and answers are non-empty`() {
        assertTrue(HelpContent.categories.isNotEmpty())
        HelpContent.categories.forEach { category ->
            assertTrue(category.title.isNotBlank())
            assertTrue(category.entries.isNotEmpty())
            category.entries.forEach { entry ->
                assertTrue(entry.question.isNotBlank())
                assertTrue(entry.answer.isNotBlank())
            }
        }
    }

    @Test
    fun `configured external links use https`() {
        listOf(
            AboutLinks.GITHUB_REPO,
            AboutLinks.GITHUB_ISSUES,
            AboutLinks.GITHUB_ISSUES_NEW,
            AboutLinks.GITHUB_RELEASES,
            AboutLinks.GITHUB_RELEASES_API,
        ).forEach { assertTrue(it.startsWith("https://")) }
        listOfNotNull(
            AboutLinks.privacyPolicyUrl,
            AboutLinks.termsOfUseUrl,
            AboutLinks.websiteUrl,
        ).forEach { assertTrue(it.startsWith("https://")) }
    }
}

package io.github.hidroh.tldroid

import android.content.Intent
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.intent.Intents.intended
import android.support.test.espresso.intent.matcher.ComponentNameMatchers.hasClassName
import android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent
import android.support.test.espresso.intent.rule.IntentsTestRule
import android.support.test.espresso.matcher.ViewMatchers.withContentDescription
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.web.assertion.WebViewAssertions.webContent
import android.support.test.espresso.web.matcher.DomMatchers.containingTextInBody
import android.support.test.espresso.web.sugar.Web.onWebView
import android.support.test.runner.AndroidJUnit4
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CommandActivityTest {
  @Rule @JvmField
  val intentsRule = IntentsTestRule<CommandActivity>(CommandActivity::class.java, false, false)

  @Test
  fun testRender() {
    MarkdownProcessor.markdown = "<div>Blah</div>"
    intentsRule.launchActivity(Intent().putExtra(CommandActivity.EXTRA_QUERY, "ls"))
    onWebView(withId(R.id.web_view))
        .forceJavascriptEnabled()
        .check(webContent(containingTextInBody("Blah")))
  }

  @Test
  fun testRunClick() {
    MarkdownProcessor.markdown = "<div>Blah</div>"
    intentsRule.launchActivity(Intent().putExtra(CommandActivity.EXTRA_QUERY, "ls"))
    onView(withId(R.id.menu_run)).perform(click())
    intended(hasComponent(hasClassName(RunActivity::class.java.name)))
  }

  @Test
  fun testHomeClick() {
    intentsRule.launchActivity(Intent().putExtra(CommandActivity.EXTRA_QUERY, "ls"))
    onView(withContentDescription(InstrumentationRegistry.getTargetContext()
        .getString(R.string.abc_action_bar_up_description)))
        .perform(click())
    assertThat(intentsRule.activity.isFinishing || intentsRule.activity.isDestroyed)
        .isTrue()
  }

  @Test
  fun testRenderNoContent() {
    MarkdownProcessor.markdown = null
    intentsRule.launchActivity(Intent().putExtra(CommandActivity.EXTRA_QUERY, "ls"))
    onWebView(withId(R.id.web_view))
        .forceJavascriptEnabled()
        .check(webContent(containingTextInBody("This command is not yet available.")))
  }

  @Test
  fun testStateRestoration() {
    MarkdownProcessor.markdown = "<div>Blah</div>"
    intentsRule.launchActivity(Intent().putExtra(CommandActivity.EXTRA_QUERY, "ls"))
    MarkdownProcessor.markdown = ""
    InstrumentationRegistry.getInstrumentation().runOnMainSync { intentsRule.activity.recreate() }
    onWebView(withId(R.id.web_view))
        .forceJavascriptEnabled()
        .check(webContent(containingTextInBody("Blah")))
  }

  @After
  fun tearDown() {
    MarkdownProcessor.markdown = null
  }
}
package io.github.hidroh.tldroid

import android.content.Intent
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.intent.rule.IntentsTestRule
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.runner.AndroidJUnit4
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RunActivityTest {
  @Rule @JvmField
  val intentsRule = IntentsTestRule<RunActivity>(RunActivity::class.java, false, false)

  @Test
  fun testRunStdOut() {
    intentsRule.launchActivity(Intent().putExtra(RunActivity.EXTRA_COMMAND, "ls"))
    onView(withId(R.id.edit_text)).perform(pressImeActionButton())
    onView(isRoot()).perform(closeSoftKeyboard())
    onView(withId(R.id.output)).check(matches(isDisplayed()))
    onView(withId(R.id.error)).check(matches(not(isDisplayed())))
  }

  @Test
  fun testRunStdErr() {
    intentsRule.launchActivity(Intent().putExtra(RunActivity.EXTRA_COMMAND, "ls"))
    onView(withId(R.id.edit_text)).perform(typeText("-la"), pressImeActionButton())
    onView(isRoot()).perform(closeSoftKeyboard())
    onView(withId(R.id.output)).check(matches(not(isDisplayed())))
    onView(withId(R.id.error)).check(matches(isDisplayed()))
  }

  @Test
  fun testStateRestoration() {
    intentsRule.launchActivity(Intent().putExtra(RunActivity.EXTRA_COMMAND, "ls"))
    onView(withId(R.id.edit_text)).perform(pressImeActionButton())
    onView(isRoot()).perform(closeSoftKeyboard())
    InstrumentationRegistry.getInstrumentation().runOnMainSync { intentsRule.activity.recreate() }
    onView(withId(R.id.output)).check(matches(isDisplayed()))
  }
}
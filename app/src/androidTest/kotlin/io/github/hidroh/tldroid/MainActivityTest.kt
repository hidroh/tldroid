package io.github.hidroh.tldroid

import android.app.Activity
import android.content.ContentValues
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.assertion.ViewAssertions.doesNotExist
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.intent.Intents.intended
import android.support.test.espresso.intent.matcher.ComponentNameMatchers.hasClassName
import android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent
import android.support.test.espresso.intent.rule.IntentsTestRule
import android.support.test.espresso.matcher.RootMatchers.isDialog
import android.support.test.espresso.matcher.RootMatchers.withDecorView
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.runner.AndroidJUnit4
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {
  @Rule @JvmField
  var intentsRule = CustomTestRule(MainActivity::class.java)

  @Test
  fun testInfo() {
    onView(isRoot()).perform(closeSoftKeyboard())
    onView(withId(R.id.info_button)).perform(click())
    onView(withId(R.id.info_button)).perform(click()) // TODO
    onView(withId(R.id.web_view))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
    onView(isRoot()).perform(pressBack())
    onView(withId(R.id.web_view))
        .check(doesNotExist())
  }

  @Test
  fun testSearch() {
    onView(withId(R.id.edit_text)).perform(typeText("l"), closeSoftKeyboard())
    onView(withText("ls"))
        .inRoot(withDecorView(not(intentsRule.activity.window.decorView)))
        .check(matches(isDisplayed()))
        .perform(click())
    intended(hasComponent(hasClassName(CommandActivity::class.java.name)))
  }

  @Test
  fun testEmptySearch() {
    onView(withId(R.id.edit_text)).perform(typeText("git"), pressImeActionButton())
    intended(hasComponent(hasClassName(CommandActivity::class.java.name)))
  }

  class CustomTestRule<T : Activity>(activityClass: Class<T>?) : IntentsTestRule<T>(activityClass) {
    override fun beforeActivityLaunched() {
      super.beforeActivityLaunched()
      val cv = ContentValues()
      cv.put(TldrProvider.CommandEntry.COLUMN_NAME, "ls")
      cv.put(TldrProvider.CommandEntry.COLUMN_PLATFORM, "common")
      InstrumentationRegistry.getTargetContext().contentResolver
          .insert(TldrProvider.URI_COMMAND, cv);
    }

    override fun afterActivityFinished() {
      super.afterActivityFinished()
      InstrumentationRegistry.getTargetContext().contentResolver
          .delete(TldrProvider.URI_COMMAND, null, null)
    }
  }
}

package io.github.hidroh.tldroid

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.Espresso.pressBack
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.doesNotExist
import android.support.test.espresso.matcher.RootMatchers.isDialog
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {
  @Rule
  @JvmField var activityRule = ActivityTestRule(MainActivity::class.java)

  @Test
  fun testInfo() {
    onView(withId(R.id.info_button)).perform(click())
    onView(withId(R.id.web_view)).inRoot(isDialog())
    pressBack()
    onView(withId(R.id.web_view)).check(doesNotExist())
  }
}

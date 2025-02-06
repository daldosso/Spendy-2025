import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.adaldosso.spendy.MainActivity
import com.adaldosso.spendy.R
import com.adaldosso.spendy.WebViewActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WebViewActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(WebViewActivity::class.java)

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testNavigationToMainActivity() {
        // Controlla che il pulsante HOME sia visibile e cliccabile
        onView(withId(R.id.button_home)).check(matches(isDisplayed())).perform(click())

        // Aspetta che MainActivity sia caricata
        composeTestRule.waitForIdle()

        // Verifica che il testo "Hello Android!" sia visibile in MainActivity (Jetpack Compose)
        composeTestRule.onNodeWithText("Welcome to Spendy!").assertExists()
    }
}

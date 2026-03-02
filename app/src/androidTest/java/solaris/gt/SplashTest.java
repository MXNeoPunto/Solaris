package solaris.gt;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SplashTest {

    @Test
    public void testMainActivityLaunchesWithSplashScreen() {
        // Launch MainActivity which uses the SplashScreen API
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);

        // Wait for it to be visible by checking for a known view
        Espresso.onView(ViewMatchers.withId(R.id.btnEnergy))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        scenario.close();
    }
}

package net.radityalabs.contactapp;

import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import net.radityalabs.contactapp.data.network.response.ContactListResponse;
import net.radityalabs.contactapp.presentation.ui.activity.ContactDetailActivity;
import net.radityalabs.contactapp.presentation.ui.activity.ContactListActivity;
import net.radityalabs.contactapp.presentation.ui.fragment.ContactDetailFragment;
import net.radityalabs.contactapp.test.DrawableMatcher;
import net.radityalabs.contactapp.test.FragmentTestRule;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static net.radityalabs.contactapp.test.ViewAction.setTextInTextView;

/**
 * Created by radityagumay on 4/19/17.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ContactDetailFragmentTest {

    private static final String STRING_TO_BE_TYPED = "raditya gumay";

    @Rule
    public FragmentTestRule<ContactDetailFragment> mContactDetailFragmentRule =
            new FragmentTestRule<>(ContactDetailFragment.class);

    @Rule
    public ActivityTestRule<ContactListActivity> mContactListRule =
            new ActivityTestRule<>(ContactListActivity.class);

    @Rule
    public IntentsTestRule<ContactDetailActivity> mContactDetailRule =
            new IntentsTestRule<>(ContactDetailActivity.class, true, false);

    @Before
    public void setup() {
        startActivity();
    }

    @Test
    public void fragment_can_be_instantiated() {
        onView(withId(R.id.container)).check(matches(isDisplayed()));
    }

    @Test
    public void is_view_available() {
        onView(withId(R.id.rv_info)).check(matches(isDisplayed()));
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_full_name)).check(matches(isDisplayed()));
        onView(withId(R.id.iv_image)).check(matches(isDisplayed()));
    }

    @Test
    public void is_widget_filled() {
        onView(withId(R.id.tv_full_name)).perform(setTextInTextView(STRING_TO_BE_TYPED), closeSoftKeyboard());
        onView(withId(R.id.tv_full_name)).check(matches(withText(STRING_TO_BE_TYPED)));

        onView(withId(R.id.iv_image)).check(matches(withDrawable(R.mipmap.ic_betty_allen)));
    }

    @Test
    public void do_calling_test() {

    }

    private ContactDetailActivity startActivity() {
        return mContactDetailRule.launchActivity(
                ContactDetailActivity.navigateTest(mContactListRule.getActivity(), contactListResponse()));
    }

    private ContactListResponse contactListResponse() {
        ContactListResponse obj = new ContactListResponse();
        obj.id = 1;
        obj.firstName = "adit";
        obj.lastName = "gumay";
        obj.profilePic = null;
        obj.isFavorite = true;
        obj.detailUrl = null;
        return obj;
    }

    public static Matcher<View> withDrawable(final int resourceId) {
        return new DrawableMatcher(resourceId);
    }

    public static Matcher<View> noDrawable() {
        return new DrawableMatcher(-1);
    }
}

package com.kinkysoftware.slutreminder;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.EditText;

import java.util.List;

public class SettingsActivity extends AppCompatPreferenceActivity {

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else if (preference instanceof RingtonePreference) {
                if (TextUtils.isEmpty(stringValue)) {
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        preference.setSummary(null);
                    } else {
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else {
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<PreferenceActivity.Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                |ReminderFrequencyPreferenceFragment.class.getName().equals(fragmentName)
                || NotificationPreferenceFragment.class.getName().equals(fragmentName);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class ReminderFrequencyPreferenceFragment extends PreferenceFragment {

        TimePreference dailyTime;
        ListPreference weeklyDayOfWeek;
        TimePreference weeklyTimeOfDay;
        ListPreference monthlyDayOfMonth;
        TimePreference monthlyTimeOfDay;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_reminder_frequency);
            setHasOptionsMenu(true);

            final PreferenceScreen screen = getPreferenceScreen();
            final ListPreference frequency = (ListPreference)findPreference("reminder_frequency");

            dailyTime = (TimePreference)findPreference("pref_frequency_daily_time");
            weeklyDayOfWeek = (ListPreference)findPreference("pref_frequency_weekly_day_of_week");
            weeklyTimeOfDay = (TimePreference)findPreference("pref_frequency_weekly_time_of_day");
            monthlyDayOfMonth = (ListPreference)findPreference("pref_frequency_monthly_day_of_month");
            monthlyTimeOfDay = (TimePreference)findPreference("pref_frequency_monthly_time_of_day");

            weeklyDayOfWeek.setSummary(weeklyDayOfWeek.getValue());
            weeklyTimeOfDay.setTitle("Every " + weeklyDayOfWeek.getValue() + " At");
            monthlyDayOfMonth.setSummary("On the " + monthlyDayOfMonth.getValue() + " Day of the Month");
            monthlyTimeOfDay.setTitle("Every " + monthlyDayOfMonth.getValue() + " Day of the Month At");
            setFrequencySummaryAndShowOptions(screen, frequency, frequency.getValue());

            Preference.OnPreferenceChangeListener onFrequencyChangeListener = new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    setFrequencySummaryAndShowOptions(screen, frequency, newValue.toString());
                    return true;
                }
            };

            Preference.OnPreferenceChangeListener onDayOfWeekChangeListener = new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    weeklyDayOfWeek.setSummary(newValue.toString());
                    weeklyTimeOfDay.setTitle("Every " + newValue.toString() + " At");
                    return true;
                }
            };

            Preference.OnPreferenceChangeListener onDayOfMonthChangeListener = new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    monthlyDayOfMonth.setSummary("On the " + newValue.toString() + " Day of the Month");
                    monthlyTimeOfDay.setTitle("Every " + newValue.toString() + " Day of the Month At");
                    return true;
                }
            };

            frequency.setOnPreferenceChangeListener(onFrequencyChangeListener);
            weeklyDayOfWeek.setOnPreferenceChangeListener(onDayOfWeekChangeListener);
            monthlyDayOfMonth.setOnPreferenceChangeListener(onDayOfMonthChangeListener);
        }

        private void setFrequencySummaryAndShowOptions(PreferenceScreen screen, Preference preference, String value) {
            screen.removeAll();

            if (value == preference.getSummary()) return;

            switch (value) {
                case "Daily":
                    screen.addPreference(preference);
                    screen.addPreference(dailyTime);
                    break;
                case "Weekly":
                    screen.addPreference(preference);
                    preference.setSummary(value);
                    screen.addPreference(weeklyDayOfWeek);
                    screen.addPreference(weeklyTimeOfDay);
                    break;
                case "Monthly":
                    screen.addPreference(preference);
                    screen.addPreference(monthlyDayOfMonth);
                    screen.addPreference(monthlyTimeOfDay);
                    break;
                case "Never":
                    screen.addPreference(preference);
                    break;
            }
            preference.setSummary(value);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();

            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NotificationPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notification);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference("notifications_reminder_ringtone"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
}
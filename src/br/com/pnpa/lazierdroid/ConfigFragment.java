package br.com.pnpa.lazierdroid;

import br.com.pnpa.lazierdroid.R;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class ConfigFragment extends PreferenceFragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
	}
}

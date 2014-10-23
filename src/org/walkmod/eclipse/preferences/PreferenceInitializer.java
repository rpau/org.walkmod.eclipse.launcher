package org.walkmod.eclipse.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.walkmod.eclipse.launching.Activator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.WALKMOD_IS_EMBEDDED, true);		
		store.setDefault(PreferenceConstants.WALKMOD_IS_OFFLINE, false);
		store.setDefault(PreferenceConstants.WALKMOD_EMBEDDED_VERSION, "1.0.6");
		store.setDefault(PreferenceConstants.WALKMOD_EXTERNAL_HOME, "");
	}

}

package org.walkmod.eclipse.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.walkmod.eclipse.launching.Activator;
import org.walkmod.eclipse.launching.LaunchingConstants;
import org.walkmod.eclipse.WalkmodHomeEditor;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */

public class WalkmodPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	private DirectoryFieldEditor fieldEditor;

	public WalkmodPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Walkmod installation preferences");
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	public void createFieldEditors() {

		addField(new BooleanFieldEditor(PreferenceConstants.WALKMOD_IS_EMBEDDED,
				"&Use the embedded version", getFieldEditorParent()) {
			@Override
			protected void valueChanged(boolean oldValue, boolean newValue) {
				fieldEditor.setEnabled(!newValue, getFieldEditorParent());
				if(newValue == true){
					String version  = getPreferenceStore().getString(PreferenceConstants.WALKMOD_EMBEDDED_VERSION);
					getPreferenceStore().setValue(LaunchingConstants.INSTALL_DIR, "EMBEDDED ("+version+")");
				}
				else{
					String installDir = getPreferenceStore().getString(PreferenceConstants.WALKMOD_EXTERNAL_HOME);
					getPreferenceStore().setValue(LaunchingConstants.INSTALL_DIR, "EXTERNAL ("+installDir+")");
				}
			}
		});
		fieldEditor = new WalkmodHomeEditor(getFieldEditorParent());
		
		addField(fieldEditor);

		BooleanFieldEditor offline = new BooleanFieldEditor(
				PreferenceConstants.WALKMOD_IS_OFFLINE, "&Offline",
				getFieldEditorParent());
		addField(offline);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

}
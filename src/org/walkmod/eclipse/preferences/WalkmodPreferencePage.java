package org.walkmod.eclipse.preferences;

import java.io.File;

import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.walkmod.eclipse.Activator;

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

		addField(new BooleanFieldEditor(PreferenceConstants.P_EMBEDDED,
				"&Use the embedded version", getFieldEditorParent()) {
			@Override
			protected void valueChanged(boolean oldValue, boolean newValue) {
				fieldEditor.setEnabled(!newValue, getFieldEditorParent());
			}
		});
		fieldEditor = new DirectoryFieldEditor(PreferenceConstants.P_PATH,
				"&Walkmod home:", getFieldEditorParent()) {

			@Override
			protected boolean checkState() {

				if (!super.checkState()) {
					return false;
				}

				// check if it is valid cppcheck binary
				try {
					String path = getTextControl().getText();
					String OS = System.getProperty("os.name").toLowerCase();
					File exec = null;
					if (OS.indexOf("win") >= 0) {
						exec = new File(path, "bin" + File.separator
								+ "walkmod.bat");
					} else {
						exec = new File(path, "bin" + File.separator
								+ "walkmod.sh");
					}

					if (exec.exists()) {
						IPreferenceStore store = Activator.getDefault()
								.getPreferenceStore();
						store.setValue(PreferenceConstants.P_EMBEDDED, false);
						return true;
					}

					this.setErrorMessage("Invalid walkmod home. The bin folder does not contains the walkmod command");

					return false;
					// update the boolean field

				} catch (Exception e) {

				}
				showErrorMessage();
				return false;
			}

			@Override
			protected void valueChanged() {
				super.valueChanged();
			}

		};
		fieldEditor.setEnabled(!Activator.getDefault().getPreferenceStore()
				.getBoolean(PreferenceConstants.P_EMBEDDED),
				getFieldEditorParent());
		addField(fieldEditor);

		BooleanFieldEditor offline = new BooleanFieldEditor(
				PreferenceConstants.P_OFFLINE, "&Offline",
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
package org.walkmod.eclipse;

import java.io.File;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;
import org.walkmod.eclipse.launching.Activator;
import org.walkmod.eclipse.preferences.PreferenceConstants;

public class WalkmodHomeEditor extends DirectoryFieldEditor {

	public WalkmodHomeEditor(Composite parent) {
		super(PreferenceConstants.WALKMOD_EXTERNAL_HOME, "&Walkmod home:", parent);
		setEnabled(
				!Activator.getDefault().getPreferenceStore()
						.getBoolean(PreferenceConstants.WALKMOD_IS_EMBEDDED), parent);
	}

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
				exec = new File(path, "bin" + File.separator + "walkmod.bat");
			} else {
				exec = new File(path, "bin" + File.separator + "walkmod.sh");
			}

			if (exec.exists()) {
				IPreferenceStore store = Activator.getDefault()
						.getPreferenceStore();
				store.setValue(PreferenceConstants.WALKMOD_IS_EMBEDDED, false);
				store.setValue(PreferenceConstants.WALKMOD_EXTERNAL_HOME, path);
				return true;
			}
			else{
				this.setErrorMessage("Invalid walkmod home. The bin folder does not contains the walkmod command");
				
			}

			

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
	

}

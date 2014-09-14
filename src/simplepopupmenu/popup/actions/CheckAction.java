package simplepopupmenu.popup.actions;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class CheckAction implements IObjectActionDelegate {

	private ISelection selection = null;

	/**
	 * Constructor for Action1.
	 */
	public CheckAction() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		selection = targetPart.getSite().getSelectionProvider().getSelection();
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		if (selection instanceof TreeSelection) {
			TreeSelection tree = ((TreeSelection) selection);
			List<?> list = tree.toList();
			IPath workingDir = null;
			for (Object object : list) {
				if (object instanceof IProject) {
					workingDir = ((IProject) object).getFullPath();
				} else if (object instanceof IJavaProject) {
					workingDir = ((IJavaProject) object).getPath();
				} else if (Platform.getAdapterManager().getAdapter(object,
						IProject.class) instanceof IProject) {
					workingDir = ((IProject) Platform.getAdapterManager()
							.getAdapter(object, IProject.class)).getFullPath();
				}
			}

			if (workingDir != null) {
				try {
					IAction action1 = new WalkmodExecAction(
							JavaRuntime.getDefaultVMInstall(), workingDir,
							"check");
					action1.run();

				} catch (Exception e) {

				}

			}
		}

	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}


}

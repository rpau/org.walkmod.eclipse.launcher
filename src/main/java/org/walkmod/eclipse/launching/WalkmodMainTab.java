package org.walkmod.eclipse.launching;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jdt.internal.debug.ui.launcher.AbstractJavaMainTab;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.walkmod.eclipse.Activator;
import org.walkmod.eclipse.preferences.PreferenceConstants;
import org.walkmod.eclipse.utils.WalkmodConfig;

@SuppressWarnings("restriction")
public class WalkmodMainTab extends AbstractJavaMainTab {

	private Table chainTable;

	private Button offline;

	private Button printErrorMessages;

	private Combo option;

	private Combo installDirectory;

	private List<String> selectedChains = new LinkedList<String>();

	private File projectLocation;

	

	private static final String[] OPTIONS = new String[] { "apply", "check",
			"install" };

	protected void createChainListEditor(Composite parent) {

		System.out.println("init components");
		if (option == null) {

			Group optionGroup = createGroupComponent(parent, SWT.NONE,
					"Command");
			Font font = parent.getFont();

			option = new Combo(optionGroup, SWT.READ_ONLY);

			for (int i = 0; i < OPTIONS.length; i++) {
				option.add(OPTIONS[i], i);
			}

			option.setLayoutData(optionGroup.getLayoutData());
			option.addModifyListener(getDefaultListener());

			Group chainsGroup = createGroupComponent(parent, SWT.NONE,
					"Conventions");

			chainTable = new Table(chainsGroup, SWT.MULTI | SWT.BORDER
					| SWT.FULL_SELECTION | SWT.CHECK);
			String[] titles = { "", "name" };
			for (int j = 0; j < titles.length; j++) {
				TableColumn column = new TableColumn(chainTable, SWT.NONE);
				column.setText(titles[j]);
			}
			loadChains();
			
			chainTable.setLinesVisible(true);
			chainTable.setHeaderVisible(true);
			chainTable.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					if (SWT.CHECK == event.detail) {
						String name = event.item.getData().toString();
						if (selectedChains.contains(name)) {
							selectedChains.remove(name);
							updateLaunchConfigurationDialog();
						} else {
							selectedChains.add(name);
							updateLaunchConfigurationDialog();
						}
					}
				}
			});

			chainTable.setFont(font);

			chainTable.setSize(chainTable.computeSize(SWT.DEFAULT, 200));

			GridData gridData = new GridData();
			gridData.horizontalSpan = 2;
			gridData.horizontalAlignment = SWT.FILL;
			gridData.grabExcessHorizontalSpace = true;
			gridData.verticalAlignment = SWT.FILL;
			gridData.grabExcessVerticalSpace = true;
			chainTable.setLayoutData(gridData);

			Group installGroup = createGroupComponent(parent, SWT.NONE,
					"Installation");
			installDirectory = new Combo(installGroup, SWT.READ_ONLY);
			installDirectory.setLayoutData(installGroup.getLayoutData());
			IPreferenceStore store = Activator.getDefault()
					.getPreferenceStore();
			installDirectory.addModifyListener(getDefaultListener());
			String embedded = "EMBEDDED ("
					+ store.getDefaultString(PreferenceConstants.WALKMOD_EMBEDDED_VERSION)
					+ ")";

			installDirectory.add(embedded, 0);

			if (!"".equals(store.getDefaultString(
					PreferenceConstants.WALKMOD_EXTERNAL_HOME).trim())) {
				String external = "EXTERNAL ("
						+ store.getDefaultString(PreferenceConstants.WALKMOD_EXTERNAL_HOME)
						+ ")";
				installDirectory.add(external, 1);
				installDirectory.select(1);
			}
			if (store.getBoolean(PreferenceConstants.WALKMOD_IS_EMBEDDED)) {
				installDirectory.select(0);
			}
		}

	}

	protected void loadChains() {
		String projectName = fProjText.getText().trim();
		if (!"".equals(projectName)) {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IProject project = root.getProject(projectName);

			if (project != null && project.exists() && project.isOpen()) {
				chainTable.removeAll();
				chainTable.clearAll();
				selectedChains.clear();
				projectLocation = new File(project.getProject()
						.getLocationURI());
				File walkmodCfg = new File(projectLocation, "walkmod.xml");
				if (!walkmodCfg.exists()) {
					this.setErrorMessage("The project does not contain the walkmod.xml file");
				} else {
					WalkmodConfig cfg = new WalkmodConfig(walkmodCfg);
					java.util.List<String> chainList = cfg.getChains();

					for (String name : chainList) {
						TableItem item1 = new TableItem(chainTable, SWT.NONE);
						item1.setText(0, "");
						item1.setText(1, name);
						item1.setChecked(true);

						selectedChains.add(name);
					}
					for (int j = 0; j < chainTable.getColumnCount(); j++) {
						chainTable.getColumn(j).pack();

					}

					chainTable.update();
				}
			}
		}
	}

	@Override
	protected void handleProjectButtonSelected() {
		super.handleProjectButtonSelected();
		loadChains();
	}

	protected void createOptionsEditor(Composite parent) {

		// boolean options
		Group options = new Group(parent, SWT.NONE);
		options.setText("Options");
		RowLayout layout = new RowLayout(SWT.HORIZONTAL);

		options.setLayout(layout);

		GridData gridData = new GridData();
		// gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = SWT.FILL;

		gridData.grabExcessHorizontalSpace = true;

		options.setLayoutData(gridData);

		offline = new Button(options, SWT.CHECK);
		offline.setText("offline");

		offline.addSelectionListener(new CheckedListener());

		printErrorMessages = new Button(options, SWT.CHECK);
		printErrorMessages.setText("stacktrace (-e)");
		printErrorMessages.addSelectionListener(new CheckedListener());

	}

	private class CheckedListener implements SelectionListener {
		@Override
		public void widgetSelected(SelectionEvent e) {
			updateLaunchConfigurationDialog();
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
		}
	}

	@Override
	public void createControl(Composite parent) {

		Composite projComp = SWTFactory.createComposite(parent,
				parent.getFont(), 1, 1, GridData.FILL_BOTH);

		createProjectEditor(projComp);
		createVerticalSpacer(projComp, 1);
		createChainListEditor(projComp);
		createVerticalSpacer(projComp, 1);
		createOptionsEditor(projComp);
		createVerticalSpacer(projComp, 1);
		setControl(projComp);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), "Help");

	}

	@Override
	public String getName() {
		return "Main";
	}

	@SuppressWarnings("deprecation")
	@Override
	public void performApply(ILaunchConfigurationWorkingCopy config) {

		// se llama al arrancar por cada componente hijo
		// updateProjectFromConfig(configuration);
		System.out.println("apply");
		if (option != null) {
			config.setAttribute(LaunchingConstants.SELECTED_OPTION, option.getText());
		}
		if (fProjText != null) {
			config.setAttribute(
					IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME,
					fProjText.getText().trim());
		}
		if (offline != null) {
			config.setAttribute(LaunchingConstants.OFFLINE_OPTION, offline.getSelection());
		}
		if (chainTable != null) {
			config.setAttribute(LaunchingConstants.SELECTED_CHAINS, selectedChains);
			loadChains();
		}
		if (installDirectory != null) {
			config.setAttribute(LaunchingConstants.INSTALL_DIR, installDirectory.getText());
		}
		if (printErrorMessages != null) {
			config.setAttribute(LaunchingConstants.PRINT_ERRORS_OPTION,
					printErrorMessages.getSelection());
		}
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		System.out.println("set defaults");
		config.setAttribute(
				IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");

		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, "");

		config.setAttribute(LaunchingConstants.SELECTED_OPTION, "apply");

		IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		config.setAttribute(LaunchingConstants.OFFLINE_OPTION,
				store.getBoolean(PreferenceConstants.WALKMOD_IS_OFFLINE));

		config.setAttribute(LaunchingConstants.PRINT_ERRORS_OPTION, false);

		config.setAttribute(LaunchingConstants.SELECTED_CHAINS, selectedChains);
		if (store.getBoolean(PreferenceConstants.WALKMOD_IS_EMBEDDED)) {
			config.setAttribute(
					LaunchingConstants.INSTALL_DIR,
					"EMBEDDED ("
							+ store.getDefaultString(PreferenceConstants.WALKMOD_EMBEDDED_VERSION)
							+ ")");
		} else {
			config.setAttribute(
					LaunchingConstants.INSTALL_DIR,
					"EXTERNAL ("
							+ store.getDefaultString(PreferenceConstants.WALKMOD_EXTERNAL_HOME)
							+ "");
		}

	}

	public Group createGroupComponent(Composite parent, int style, String text) {
		Group result = new Group(parent, style);
		result.setText(text);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		result.setLayout(gridLayout);
		GridData gridData = new GridData(GridData.FILL, GridData.CENTER, true,
				false);
		gridData.horizontalSpan = 2;
		result.setLayoutData(gridData);

		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		return result;
	}

	public void initializeFrom(ILaunchConfiguration config) {

		updateProjectFromConfig(config);
	}

	public void updateProjectFromConfig(ILaunchConfiguration config) {
		System.out.println("loading launching config");

		try {
			config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, "");

			if (option != null) {
				int pos = Arrays.binarySearch(OPTIONS,
						config.getAttribute(LaunchingConstants.SELECTED_OPTION, "apply"));

				option.select(pos);
			}

			IPreferenceStore store = Activator.getDefault()
					.getPreferenceStore();

			if (offline != null) {
				offline.setSelection(config.getAttribute(LaunchingConstants.OFFLINE_OPTION, store
						.getBoolean(PreferenceConstants.WALKMOD_IS_OFFLINE)));
			}

			if (printErrorMessages != null) {
				printErrorMessages.setSelection(config.getAttribute(
						LaunchingConstants.PRINT_ERRORS_OPTION, false));
			}

			if (installDirectory != null) {
				String installDir = config
						.getAttribute(
								LaunchingConstants.INSTALL_DIR,
								"EMBEDDED ("
										+ store.getDefaultString(PreferenceConstants.WALKMOD_EMBEDDED_VERSION)
										+ ")");
				if ("".equals(installDir)) {
					installDir = "EMBEDDED ("
							+ store.getDefaultString(PreferenceConstants.WALKMOD_EMBEDDED_VERSION)
							+ ")";
				} else {
					installDir = "EXTERNAL (" + installDir + ")";
				}

				int pos = Arrays.binarySearch(installDirectory.getItems(),
						installDir);
				if (pos > -1) {
					installDirectory.select(pos);
				} else {
					installDirectory.select(0);
				}
			}

			if (chainTable != null) {
				loadChains();
				// setting the chains that will be executed
				List<String> selectedChains = config.getAttribute(
						LaunchingConstants.SELECTED_CHAINS, new LinkedList<String>());
				TableItem[] items = chainTable.getItems();
				for (int i = 0; i < items.length; i++) {

					if (selectedChains.contains(items[i].getText(1))) {
						items[i].setChecked(true);
					} else {
						items[i].setChecked(false);
					}

				}
			}

			if (fProjText != null) {
				String projectName = config.getAttribute(
						IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME,
						EMPTY_STRING);
				fProjText.setText(projectName);				
			}

		} catch (CoreException ce) {
			setErrorMessage(ce.getStatus().getMessage());
		}
	}

}

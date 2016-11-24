/*
 *	Тестовое задание для XORED 
 *  Реализовать eclipse-plugin, который позволит создавать композитные launch-конфигурации:
 *  такие launc-конфигурации, которые будут содержать в себе другие launch-конфигурации. 
 *  
 *  Подробрнее:
 * 		http://ru.xored.com/test.html
 * 
 * Author: Maxim Gush
 * Date: 21.11.2016 
 */

package compositelaunchplugin.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Group;

import compositelaunchplugin.CompositeLaunchData;
import compositelaunchplugin.CompositeLaunchData.CompositeLaunchItem;
import compositelaunchplugin.CompositeLaunchData.CompositeLaunchItems;
import compositelaunchplugin.CompositeLaunchDataObserver.CompositeLaunchDataEvent;
import compositelaunchplugin.CompositeLaunchDataObserver.CompositeLaunchDataEventChangedItem;
import compositelaunchplugin.CompositeLaunchDataObserver.CompositeLaunchDataEventChangedItems;
import compositelaunchplugin.CompositeLaunchDataObserver.CompositeLaunchDataListener;
import compositelaunchplugin.CompositeLaunchDataObserver.CompositeLaunchDataListenerTypes;
import compositelaunchplugin.CompositeLaunchUtils;

/**
 * Класс реализующий работу с элементми GUI во вкладке композитной конфигурации
 */
public class CompositeLaunchMainTabControls {

	CompositeLaunchData compositeLaunchData = null;

	Button buttonAddConfiguration = null;
	Button buttonRemoveConfiguration = null;
	Button buttonSetStartTime = null;

	CompositeLaunchMainTabTable table = null;

	Menu menuAddConfiguration = null;

	Text textStartTime = null;
	Color colorTextWithWrongFormat = null;

	Text textNotes = null;

	public CompositeLaunchMainTabControls(CompositeLaunchData _сompositeLaunchData) {
		compositeLaunchData = _сompositeLaunchData;
		
		// Подписываемся на изменение данных текущей композитной конфигурации
		compositeLaunchData.AddListener(CompositeLaunchDataListenerTypes.OnChangedItems, new CompositeLaunchDataListener() {			
			@Override
			public void handleEvent(CompositeLaunchDataEvent event) {		
				CompositeLaunchDataEventChangedItems e = (CompositeLaunchDataEventChangedItems) event;
				
				table.OnChangedItems(e.items);
				UpdateNotes(e.sender.GetItems());
				UpdateButtonEnabled();	
			}
		});

		compositeLaunchData.AddListener(CompositeLaunchDataListenerTypes.OnAddItem, new CompositeLaunchDataListener() {
			@Override
			public void handleEvent(CompositeLaunchDataEvent event) {		
				CompositeLaunchDataEventChangedItem e = (CompositeLaunchDataEventChangedItem) event;
				table.OnAddItem(e.item);
				UpdateNotes(e.sender.GetItems());
				UpdateButtonEnabled();
			}
		});
		
		compositeLaunchData.AddListener(CompositeLaunchDataListenerTypes.OnRemoveItem, new CompositeLaunchDataListener() {				
			@Override
			public void handleEvent(CompositeLaunchDataEvent event) {		
				CompositeLaunchDataEventChangedItem e = (CompositeLaunchDataEventChangedItem) event;
				table.OnRemoveItem(e.item);
				UpdateNotes(e.sender.GetItems());
				UpdateButtonEnabled();
			}
		});

		compositeLaunchData.AddListener(CompositeLaunchDataListenerTypes.OnUpdateItem, new CompositeLaunchDataListener() {				
			@Override
			public void handleEvent(CompositeLaunchDataEvent event) {		
				CompositeLaunchDataEventChangedItem e = (CompositeLaunchDataEventChangedItem) event;
				table.OnUpdateItem(e.item);
				UpdateNotes(e.sender.GetItems());
				UpdateButtonEnabled();
			}
		});
	}

	/** Создаёт пользовательские элементы управления */
	public void СreateControls(Composite comp) {

		comp.setLayout(new GridLayout(4, false));

		// ------------------------------------------------------------
		buttonAddConfiguration = new Button(comp, SWT.NONE);
		buttonAddConfiguration.setText("Add");
		buttonAddConfiguration.setToolTipText("Click to add launch configuration to list");
		GridData gridData = new GridData();
		gridData.widthHint = 60;
		buttonAddConfiguration.setLayoutData(gridData);
		buttonAddConfiguration.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				CreateMenuAddConfiguration();
				menuAddConfiguration.setVisible(true);
			}
		});
		// ------------------------------------------------------------
		buttonRemoveConfiguration = new Button(comp, SWT.NONE);
		buttonRemoveConfiguration.setText("Remove");
		buttonRemoveConfiguration.setToolTipText("Click to remove launch configuration from list");
		GridData gridDataButton = new GridData();
		gridDataButton.grabExcessHorizontalSpace = true;
		gridDataButton.widthHint = 60;
		gridDataButton.horizontalSpan = 3;
		buttonRemoveConfiguration.setLayoutData(gridDataButton);
		buttonRemoveConfiguration.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				switch (e.type) {
				case SWT.Selection:
					compositeLaunchData.RemoveItem(table.GetSelectedCompositeLaunchItem());
					break;
				}
			}
		});
		// ------------------------------------------------------------
		table = new CompositeLaunchMainTabTable(comp, SWT.VIRTUAL | SWT.BORDER);

		GridData gridDataTable = new GridData();
		gridDataTable.grabExcessHorizontalSpace = true;
		gridDataTable.widthHint = 385;
		gridDataTable.heightHint = 150;
		gridDataTable.horizontalSpan = 4;
		table.setLayoutData(gridDataTable);
		table.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				switch (e.type) {
				case SWT.Selection:
					if (table.getSelectionIndex() >= 0) {
						textStartTime.setText(table.GetSelectedItemStartTime());
					}
					UpdateButtonEnabled();
				}
			}
		});
		// ------------------------------------------------------------
		Label labelStartTime = new Label(comp, SWT.NULL);
		labelStartTime.setText("Time start: ");
		// ------------------------------------------------------------
		textStartTime = new Text(comp, SWT.SINGLE | SWT.BORDER | SWT.RIGHT);
		textStartTime.setText("0.0");
		textStartTime.addListener(SWT.CHANGED, new Listener() {
			public void handleEvent(Event e) {
				Text text = (Text) e.widget;
				if (!isPositiveNumeric(text.getText())) {
					text.setForeground(colorTextWithWrongFormat);
				} else
					text.setForeground(null);
			}
		});
		GridData gridDataTextStartTime = new GridData();
		gridDataTextStartTime.widthHint = 50;
		textStartTime.setLayoutData(gridDataTextStartTime);

		colorTextWithWrongFormat = new Color(comp.getDisplay(), 255, 0, 0);
		textStartTime.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				colorTextWithWrongFormat.dispose();
			}
		});
		// ------------------------------------------------------------
		Label labelSecond = new Label(comp, SWT.NULL);
		labelSecond.setText("s");
		// ------------------------------------------------------------
		buttonSetStartTime = new Button(comp, SWT.NONE);
		buttonSetStartTime.setText("Set");
		buttonSetStartTime.setToolTipText("Click to set start time for selected run configuration");
		gridData = new GridData();
		gridData.widthHint = 60;
		buttonSetStartTime.setLayoutData(gridData);
		buttonSetStartTime.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				switch (e.type) {
				case SWT.Selection:
					int selectedIndex = table.getSelectionIndex();
					if (selectedIndex != -1
						&& isPositiveNumeric(textStartTime.getText())) {
						compositeLaunchData.SetStartTime(table.GetSelectedCompositeLaunchItem(), Float.parseFloat(textStartTime.getText()));
					}
					break;
				}
			}
		});
		// ------------------------------------------------------------
		Group groupNotes = new Group(comp, SWT.NULL);
		groupNotes.setText("Notes");
		GridData gridlDataGroupNotes = new GridData(SWT.HORIZONTAL, SWT.TOP, true, false);
		gridlDataGroupNotes.horizontalSpan = 4;
		gridlDataGroupNotes.widthHint = 400;
		gridlDataGroupNotes.heightHint = 100;
		groupNotes.setLayoutData(gridlDataGroupNotes);
		// ------------------------------------------------------------
		textNotes = new Text(groupNotes, SWT.READ_ONLY | SWT.V_SCROLL | SWT.MULTI | SWT.WRAP);
		textNotes.setLocation(5, 15);
		textNotes.setSize(395, 100);
	}

	/** Обновляет текст-примечание */
	public void UpdateNotes(CompositeLaunchItems items) {

		StringBuffer notes = new StringBuffer();
		for (CompositeLaunchItem item : items) {
			if (item.HasError) {
				notes.append("* ");
				notes.append(item.Name);
				notes.append(": ");
				notes.append(item.Note);
				notes.append("\n");
			}
		}

		textNotes.setText(notes.toString());
	}

	/** Обновляет состояние кнопок (доступно/недоступно) */
	public void UpdateButtonEnabled() {
		buttonSetStartTime.setEnabled(table.getSelectionIndex() >= 0);
		buttonRemoveConfiguration.setEnabled(table.getSelectionIndex() >= 0);
	}

	/** Создаёт контекстное меню для добавления новых конфигураций */
	public void CreateMenuAddConfiguration() {

		if (menuAddConfiguration != null)
			menuAddConfiguration.dispose();

		menuAddConfiguration = new Menu(buttonAddConfiguration);
		// ------------------------------
		try {
			for (ILaunchConfiguration confLoop : DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations()) {
				String confType = confLoop.getType().getName();

				CompositeLaunchItem item = new CompositeLaunchItem(confLoop.getName(), confType, 0.0f);
				item.LaunchConfiguration = confLoop;

				// Ищем пункт меню соответствующий типу конфигурации среди ранее созданных
				MenuItem menuItem = null;
				for (MenuItem menuItemLoop : menuAddConfiguration.getItems()) {
					if (confType.equals(menuItemLoop.getText())) {
						menuItem = menuItemLoop;
						break;
					}
				}

				// Если не удалось соответствующий типу конфигурации пункт меню - добавляем его
				if (menuItem == null) {
					menuItem = new MenuItem(menuAddConfiguration, SWT.CASCADE);
					menuItem.setText(confType);

					// Создаём дополнительное подменю (для выбора конкретной конфигурации)
					Menu subMenuItem = new Menu(menuItem);
					menuItem.setMenu(subMenuItem);
				}

				// Добавляем пункт меню соответствующий конкретной конфигурации
				MenuItem menuItem2 = new MenuItem(menuItem.getMenu(), SWT.NONE);
				menuItem2.setText(confLoop.getName());
				menuItem2.setData(item);
				if (CompositeLaunchUtils.ItemCanBeAdded(compositeLaunchData.GetILaunchConfiguration(), confLoop)) {
					menuItem2.addSelectionListener(new SelectionListener() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							MenuItem menuItem = MenuItem.class.cast(e.getSource());
							compositeLaunchData.AddItem((CompositeLaunchItem) menuItem.getData());
						}

						@Override
						public void widgetDefaultSelected(SelectionEvent e) {
						}
					});
				} else {
					menuItem2.setEnabled(false);
					menuItem2.setToolTipText("Adding this configuration leads to infinite loop");
				}

			}
		} catch (CoreException e) {
			e.printStackTrace();
		}

		if (menuAddConfiguration.getItemCount() == 0) {
			MenuItem menuItem = new MenuItem(menuAddConfiguration, SWT.NONE);
			menuItem.setText("Does not exist run configurations that may be added in list");
		}
	}

	/** Проверяет является ли входная строка положительным числом */
	private static boolean isPositiveNumeric(String str) {
		try {
			return (Float.parseFloat(str) >= 0.0);
		} catch (NumberFormatException nfe) {
			return false;
		}
	}
}
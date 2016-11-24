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

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import compositelaunchplugin.CompositeLaunchData;
import compositelaunchplugin.CompositeLaunchData.CompositeLaunchItem;
import compositelaunchplugin.CompositeLaunchDataObserver.CompositeLaunchDataEvent;
import compositelaunchplugin.CompositeLaunchDataObserver.CompositeLaunchDataEventChangedItem;
import compositelaunchplugin.CompositeLaunchDataObserver.CompositeLaunchDataEventChangedItems;
import compositelaunchplugin.CompositeLaunchDataObserver.CompositeLaunchDataListener;
import compositelaunchplugin.CompositeLaunchDataObserver.CompositeLaunchDataListenerTypes;
import compositelaunchplugin.ui.CompositeLaunchMainTabControls;

/**
 *  Класс переопределяющий общую функциональности вкладки конфигурации запуска
 *  для работы с композитными конфигурациями
 */
public class CompositeLaunchMainTab extends AbstractLaunchConfigurationTab  {
	
	CompositeLaunchData compositeLaunchData = new CompositeLaunchData();
	CompositeLaunchMainTabControls compositeLaunchMainTabControls = new CompositeLaunchMainTabControls(compositeLaunchData);
	
	@Override
	public void createControl(Composite parent) {
		Composite comp = new Group(parent, SWT.BORDER);
		setControl(comp);
		compositeLaunchMainTabControls.СreateControls(comp);
		
		compositeLaunchData.AddListener(CompositeLaunchDataListenerTypes.OnAddItem, new CompositeLaunchDataListener() {
			
			@Override
			public void handleEvent(CompositeLaunchDataEvent event) {
				updateLaunchConfigurationDialog();
			}
		});
		
		compositeLaunchData.AddListener(CompositeLaunchDataListenerTypes.OnRemoveItem, new CompositeLaunchDataListener() {
				
			@Override
			public void handleEvent(CompositeLaunchDataEvent event) {
				updateLaunchConfigurationDialog();
			}
		});

		compositeLaunchData.AddListener(CompositeLaunchDataListenerTypes.OnUpdateItem, new CompositeLaunchDataListener() {
				
			@Override
			public void handleEvent(CompositeLaunchDataEvent event) {
				updateLaunchConfigurationDialog();
			}
		});
	}
	
	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
				
		compositeLaunchData.Init(configuration);
	}	

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
	
		compositeLaunchData.Save(configuration);
	}
	
	@Override
	public boolean isValid(ILaunchConfiguration configuration) {
		if (compositeLaunchData.GetItems().size() < 1)
			return false;
		
		for (CompositeLaunchItem item : compositeLaunchData.GetItems())
			if (item.HasError)
				return false;
		
		return true;
	}	

	@Override
	public String getName() {
		return "Composite Launch configuration";
	}
}
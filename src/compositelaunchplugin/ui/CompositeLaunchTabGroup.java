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

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

public class CompositeLaunchTabGroup extends AbstractLaunchConfigurationTabGroup {

	@Override
    public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		           
            ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
        			new CompositeLaunchMainTab()
        	};
        	setTabs(tabs);

    }
}

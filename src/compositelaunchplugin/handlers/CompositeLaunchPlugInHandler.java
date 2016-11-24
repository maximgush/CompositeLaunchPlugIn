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

package compositelaunchplugin.handlers;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.IProcess;

import compositelaunchplugin.CompositeLaunchData;
import compositelaunchplugin.CompositeLaunchData.CompositeLaunchItem;
import compositelaunchplugin.CompositeLaunchData.CompositeLaunchItems;
import compositelaunchplugin.CompositeLaunchUtils;

public class CompositeLaunchPlugInHandler implements ILaunchConfigurationDelegate  {
		
	// Лог исполнения композитной конфигурации
	StringBuffer mLog = new StringBuffer();
	
	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {	
		
		try
		{
			CompositeLaunchData сompositeLaunchData = new CompositeLaunchData();
			сompositeLaunchData.Init(configuration);		
			
			CompositeLaunchItems items = CompositeLaunchUtils.ProcessingItemsBeforeRunning(сompositeLaunchData);
			
			SubMonitor launchMonitor = SubMonitor.convert(monitor, configuration.getName(), items.size() );				
			
			float prevTime = 0.0f;
			for (CompositeLaunchItem item : items){
				
				if (monitor.isCanceled())
					break;
				
				if (item.HasError)
					continue;
				
				float sleepTime = item.StartTime - prevTime;
				prevTime = item.StartTime;
				
				// Выведим в ProgressMonitor текущую задачу
				UpdateProgressStatus(launchMonitor, "Waiting " + Float.toString(sleepTime) + " sec before launch " + item.Name + "...");			
				
				// Выполняем ожидание до запуска следующей конфигурации
				try {
					Thread.sleep((long) (sleepTime * 1000));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				if (monitor.isCanceled())
					break;
				
				// Выведим в ProgressMonitor текущую задачу
				UpdateProgressStatus(launchMonitor, "Launching " + item.Name + "...");
				
				
				ILaunchConfiguration launchConfiguration = item.LaunchConfiguration;
	
				// Запускаем следующую дочернюю конфигурацию
				ILaunch configurationLaunch = launchConfiguration.launch(mode,launchMonitor.newChild(1));
				
				
				// Добавляем в композитную конфигурацию все объекты IDebugTarget связанные с дочерней конфигурацией
				for (IDebugTarget debugTarget : configurationLaunch.getDebugTargets())
					launch.addDebugTarget(debugTarget);
				
				// Добавляем в композитную конфигурацию все процессы связанные с дочерней конфигурацией
				for (IProcess process : configurationLaunch.getProcesses())
					launch.addProcess(process);	
				
				UpdateProgressStatus(launchMonitor, "Done.");
			}
		}
		finally{
			monitor.done();		
		}
	}
	
	/** Выводит информацию о текущем статусе исполнения композитной конфигурации в монитор и консоль */
	private void UpdateProgressStatus(SubMonitor subMonitor, String message){
		mLog.append(message); 
		mLog.append("\n");
		subMonitor.setTaskName(mLog.toString());
		System.out.println(message);
	}
}

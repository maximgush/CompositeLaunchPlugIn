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

package compositelaunchplugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;

import compositelaunchplugin.CompositeLaunchData.CompositeLaunchItem;
import compositelaunchplugin.CompositeLaunchData.CompositeLaunchItems;

/**
 * Класс содержащий общие утилитарный функции для работы с данными связанными с конфигурацией Composite
 */

public class CompositeLaunchUtils {

	/** Сортирует элементы контейнера по возрастанию времени запуска */
	public static void SortItemsByTime(CompositeLaunchItems items){			
		for (int i = 1; i < items.size(); ++i) {
			for (int j = i; j > 0; --j)	{
				if (items.get(j-1).StartTime > items.get(j).StartTime){
					CompositeLaunchItem item = items.get(j-1);
					items.remove(j-1);
					items.add(j,item);
				}
				else
					break;
			}
		}
	}
	
	/** Проверяет может ли быть добавлен дочерний элемент на основе указанной конфигурации */
	public static boolean ItemCanBeAdded(ILaunchConfiguration parentConf, ILaunchConfiguration childConf) {
		
		// Конфигурация не может быть добавлена, только если это композитная конфигурация,
		// которая приведёт к зацикливанию при запуске
		
		try {
			// Если добавляемая конфигурация не типа Composite - можно добавлять
			if (parentConf.getType() != childConf.getType()){
				return true;					
			}
			else{
				// Добавляемая конфигурация - композитная.
				// Проверяем не приведёт ли её добавление к появлению циклов
				if (CompositeLaunchCyclesFinder.FindCycleAfterAddingChild(parentConf, childConf))
					return false;
				else
					return true;
			}
				
		} catch (CoreException e) {
				e.printStackTrace();
		}
					
		return true;
	}
	
	/** Возвращает список конфигураций для непосредственного исполнения
	 * разворачивает композитные конфигурации, удаляет конфигурации с ошибками, сортирует список по времени  */
	public static CompositeLaunchItems ProcessingItemsBeforeRunning(CompositeLaunchData compositeLaunchData){	
		CompositeLaunchItems rstItems = new CompositeLaunchItems();
		
		for (CompositeLaunchItem item : compositeLaunchData.GetItems()){
			if (item.HasError || item.LaunchConfiguration == null)
				continue;
			
			try {
				if (compositeLaunchData.GetILaunchConfiguration().getType().equals(item.LaunchConfiguration.getType())){
					// Это композитная конфигурация надо добавить в общий список каждую её дочернюю конфигурацию
					CompositeLaunchData childCompositeLaunchPlugIn = new CompositeLaunchData();
					childCompositeLaunchPlugIn.LoadItems(item.LaunchConfiguration);
					CompositeLaunchItems childItems = ProcessingItemsBeforeRunning(childCompositeLaunchPlugIn);
					
					for(CompositeLaunchItem childItem : childItems){
					
						childItem.Name = item.Name + " -> " + childItem.Name;
						childItem.StartTime += item.StartTime; 
						rstItems.add(childItem);
					}					
				}
				else
					rstItems.add(new CompositeLaunchItem(item));
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		SortItemsByTime(rstItems);
		
		return rstItems;
	}
}

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

import compositelaunchplugin.CompositeLaunchDataObserver.CompositeLaunchDataListener;
import compositelaunchplugin.CompositeLaunchDataObserver.CompositeLaunchDataListenerTypes;

/**
 * Класс реализующий внутреннюю логику работы композитных конфигураций
 */
public class CompositeLaunchData{
			
	/**
	 * Класс описывающий дочерний элемент композитной конфигурации - дочернюю конфигурацию и параметры её запуска  
	 */
	public static class CompositeLaunchItem{
		public ILaunchConfiguration LaunchConfiguration = null;
		public String Name = "Default";
		public String Type = "Default Type";
		public float StartTime = 0.f;
		public boolean HasError = false;
		public String Note = "";
		
		public CompositeLaunchItem(String name, String type,  float startTime){
			Name = name;
			Type = type;
			StartTime = startTime;
		}
				
		public CompositeLaunchItem(CompositeLaunchItem item){
			LaunchConfiguration = item.LaunchConfiguration;
			Name = item.Name;
			Type = item.Type;
			StartTime = item.StartTime;
			HasError = item.HasError;
			Note = item.Note;
		}
	};
	public static class CompositeLaunchItems extends ArrayList<CompositeLaunchItem> {
		private static final long serialVersionUID = 1L;}
	
	// Конфигурация запуска, внутреннии данные которой хранятся в этом классе
	ILaunchConfiguration mLaunchConfiguration = null; 
	
	// Наблюдатель за изменением данных этого класса (оповещает подписчиков)
	CompositeLaunchDataObserver mObserver = new CompositeLaunchDataObserver(this);
	
	// Список дочерних элементов конфигурации
	CompositeLaunchItems mItems = new CompositeLaunchItems();	
	
	/** Возвращает объект ILaunchConfiguration для этого экземпляра CompositeLaunchData */
	public ILaunchConfiguration GetILaunchConfiguration(){	
		return mLaunchConfiguration;
	}
	
	/** Добавляет подписчика на события этого класса */
	public void AddListener(CompositeLaunchDataListenerTypes type, CompositeLaunchDataListener listener){	
		mObserver.AddListener(type, listener);
	}
		
	/** Инициализирует поля этого класса данными из экземпляра ILaunchConfiguration */
	public void Init(ILaunchConfiguration configuration) {
					
		// Загружаем дочернии конфигурации
		LoadItems(configuration);
		
		// Проверяем загруженные данные на наличие ошибок
		CheckItems();
				 			
		// Оповещаем подписчиков об изменении
		mObserver.OnChangedItems(mItems);
	}	
	
	/** Сохраняет внутренние значения данной композитной конфигурации в экземпляре ILaunchConfigurationWorkingCopy */
	public void Save(ILaunchConfigurationWorkingCopy configuration) {
		
		java.util.Map<String,String> attrConfigurationNamesMap = new HashMap<>();
		java.util.Map<String,String> attrConfigurationTypesMap = new HashMap<>();
		java.util.Map<String,String> attrStartTimeMap = new HashMap<>();
		
		for (CompositeLaunchItem item : mItems){
			
			// Формируем уникальный идентификатор для дочерней конфигурации, под которым она будет сохранена
			// Note: Идентификатор должен однозначно соответствовать дочерней конфигурации
			// иначе в некоторых ситуациях может некорректно определяться наличие изменений в ILaunchConfigurationWorkingCopy
			// (Например при перемещении item'a в массиве mItems)
			String idString = item.Name + '_' + Float.toString(item.StartTime);			
			int index = 0;
			do{
				idString = item.Name + '_' + Float.toString(item.StartTime) + (index > 0 ? '_' + Integer.toString(index) : "");
				index++;
			}
			while (attrConfigurationNamesMap.containsKey(idString));
				
			attrConfigurationNamesMap.put(idString, item.Name);
			attrConfigurationTypesMap.put(idString, item.Type);
			attrStartTimeMap.put(idString, Float.toString(item.StartTime));
		}
		configuration.setAttribute("ConfigurationNames",attrConfigurationNamesMap);
		configuration.setAttribute("ConfigurationTypes",attrConfigurationTypesMap);
		configuration.setAttribute("StartTime",attrStartTimeMap);
	}	
	
	/** Возвращает список дочерних элементов композитной конфигурации */
	public CompositeLaunchItems GetItems() {
		return mItems;
	}
				
	/** Добавляет в композитную конфигурацию новый дочерний элемент */
	public void AddItem(CompositeLaunchItem item){
		CompositeLaunchItem newItem = new CompositeLaunchItem(item);
		mItems.add(newItem);
		mObserver.OnAddItem(newItem);			
	}
	
	/** Удаляет из композитной конфигурации дочерний элемент */
	public void RemoveItem(CompositeLaunchItem item){
		mItems.remove(item);
		mObserver.OnRemoveItem(item);		
	}

	/** Устанавливает время запуска для дочернего элемента композитной конфигурации */
	public void SetStartTime(CompositeLaunchItem item, float newStartTime) {
		item.StartTime = newStartTime;	
		mObserver.OnUpdateItem(item);				
	}
	
	/** Загружает данные для указанной конфигурации */
	public void LoadItems(ILaunchConfiguration configuration) {
		
		mLaunchConfiguration = configuration;
		
		mItems.clear();	
		
		java.util.Map<String,String> defaultValues = new java.util.HashMap<String,String>();
		java.util.Map<String,String> attrConfigurationNamesMap = null;
		java.util.Map<String,String> attrConfigurationTypesMap = null;
		java.util.Map<String,String> attrStartTimeMap = null;
		
		try {
			attrConfigurationNamesMap = configuration.getAttribute("ConfigurationNames", defaultValues);
			attrConfigurationTypesMap = configuration.getAttribute("ConfigurationTypes", defaultValues);
			attrStartTimeMap = configuration.getAttribute("StartTime", defaultValues);
			
			for (Map.Entry<String,String> entry: attrConfigurationNamesMap.entrySet()){
				mItems.add(new CompositeLaunchItem(entry.getValue(), attrConfigurationTypesMap.get(entry.getKey()), Float.parseFloat(attrStartTimeMap.get(entry.getKey()))));
			}
			
			for (CompositeLaunchItem item : mItems)
				for (ILaunchConfiguration confLoop : DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations()){	
					if (item.Name.equals(confLoop.getName()))
						item.LaunchConfiguration = confLoop;							
			}				
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	/** Проверяет данные конфигурации на наличие ошибок */
	private void CheckItems(){
			for (CompositeLaunchItem item : mItems){			
				// Проверяем, что нашлась соответствующая конфигурация
				if (item.LaunchConfiguration == null){
					item.HasError = true;
					item.Note = "The configuration is not found. Probably it has been renamed or deleted.";
					continue;
				}
	
				// Проверяем наличие циклов среди дочерних конфигураций
				if (CompositeLaunchCyclesFinder.FindCycleAfterAddingChild(mLaunchConfiguration, item.LaunchConfiguration)){
					item.HasError = true;
					item.Note = "The configuration leads to an infinite loop.";
			}				
		}
	}

}
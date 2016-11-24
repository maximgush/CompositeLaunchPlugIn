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
import java.util.List;
import java.util.Map;

import compositelaunchplugin.CompositeLaunchData.CompositeLaunchItem;
import compositelaunchplugin.CompositeLaunchData.CompositeLaunchItems;

public class CompositeLaunchDataObserver {

	/**
	 * Перечисление событий изменения данных композитной конфигурации
	 */
	public enum CompositeLaunchDataListenerTypes { OnAddItem, OnRemoveItem, OnUpdateItem, OnChangedItems }
	
	
	/**
	 * Интерфейс подписчика на изменения данных композитной конфигурации 
	 * */
	public interface CompositeLaunchDataListener {

		/** Sent when an event that the receiver has registered for occurs. */
		void handleEvent (CompositeLaunchDataEvent event);
		}

	/**
	 * Класс описывающий данные о наступившем событии в композитной конфигурации
	 * */
	public static class CompositeLaunchDataEvent {
		public CompositeLaunchDataListenerTypes type = null;
		public CompositeLaunchData sender = null;
	}

	/**
	 * Класс описывающий событие изменении дочернего элемента композитной конфигурации  
	 * */
	public static class CompositeLaunchDataEventChangedItem extends CompositeLaunchDataEvent {
		public CompositeLaunchItem item = null;			
	}
	
	/**
	 * Класс описывающий событие изменении дочерних элементов композитной конфигурации  
	 * */
	public static class CompositeLaunchDataEventChangedItems extends CompositeLaunchDataEvent {
		public CompositeLaunchItems items = null;			
	}	
	
	// Экземпляр данный композитной конфигурации, за котором идёт наблюдение
	CompositeLaunchData data = null;
	
	// Список подписчиков на события изменения данных в мапе по типу события
	Map<CompositeLaunchDataListenerTypes, List<CompositeLaunchDataListener>> listeners = new HashMap<CompositeLaunchDataListenerTypes, List<CompositeLaunchDataListener>>();
		
	/** Добавляет подписчика на события типа @type */
	public void AddListener(CompositeLaunchDataListenerTypes type, CompositeLaunchDataListener listener){	
		listeners.get(type).add(listener);
	}
	
	/** Конструктор */
	public CompositeLaunchDataObserver(CompositeLaunchData _data){
		data = _data;
		
		listeners.put(CompositeLaunchDataListenerTypes.OnAddItem, new ArrayList<CompositeLaunchDataListener>());
		listeners.put(CompositeLaunchDataListenerTypes.OnRemoveItem, new ArrayList<CompositeLaunchDataListener>());
		listeners.put(CompositeLaunchDataListenerTypes.OnUpdateItem, new ArrayList<CompositeLaunchDataListener>());
		listeners.put(CompositeLaunchDataListenerTypes.OnChangedItems, new ArrayList<CompositeLaunchDataListener>());
	}
	
	
	/** Оповещает подписчиков об изменении списка дочерних элементов композитной конфигурации */
	public void OnChangedItems(CompositeLaunchItems items){			
		for (CompositeLaunchDataListener listener : listeners.get(CompositeLaunchDataListenerTypes.OnChangedItems)){
			CompositeLaunchDataEventChangedItems event = new CompositeLaunchDataEventChangedItems();
			event.type = CompositeLaunchDataListenerTypes.OnChangedItems;
			event.sender = data;
			event.items = items;
			listener.handleEvent(event);
		}
	}
		
	/** Оповещает подписчиков о добавлении дочернего элемента в композитную конфигурацию */
	public void OnAddItem(CompositeLaunchItem item){
		for (CompositeLaunchDataListener listener : listeners.get(CompositeLaunchDataListenerTypes.OnAddItem)){
			CompositeLaunchDataEventChangedItem event = new CompositeLaunchDataEventChangedItem();
			event.type = CompositeLaunchDataListenerTypes.OnAddItem;
			event.sender = data;
			event.item = item;
			listener.handleEvent(event);
		}
	}
	
	/** Оповещает подписчиков об удалении дочернего элемента из композитной конфигурации */
	public void OnRemoveItem(CompositeLaunchItem item){
		for (CompositeLaunchDataListener listener : listeners.get(CompositeLaunchDataListenerTypes.OnRemoveItem)){
			CompositeLaunchDataEventChangedItem event = new CompositeLaunchDataEventChangedItem();
			event.type = CompositeLaunchDataListenerTypes.OnRemoveItem;
			event.sender = data;
			event.item = item;
			listener.handleEvent(event);
		}
	}
	
	/** Оповещает подписчиков об обновлении дочернего элемента композитной конфигурации */
	public void OnUpdateItem(CompositeLaunchItem item){
		for (CompositeLaunchDataListener listener : listeners.get(CompositeLaunchDataListenerTypes.OnUpdateItem)){
			CompositeLaunchDataEventChangedItem event = new CompositeLaunchDataEventChangedItem();
			event.type = CompositeLaunchDataListenerTypes.OnUpdateItem;
			event.sender = data;
			event.item = item;
			listener.handleEvent(event);
		}
	}
}

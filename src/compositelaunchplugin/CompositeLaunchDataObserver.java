/*
 *	�������� ������� ��� XORED 
 *  ����������� eclipse-plugin, ������� �������� ��������� ����������� launch-������������:
 *  ����� launc-������������, ������� ����� ��������� � ���� ������ launch-������������. 
 *  
 *  ����������:
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
	 * ������������ ������� ��������� ������ ����������� ������������
	 */
	public enum CompositeLaunchDataListenerTypes { OnAddItem, OnRemoveItem, OnUpdateItem, OnChangedItems }
	
	
	/**
	 * ��������� ���������� �� ��������� ������ ����������� ������������ 
	 * */
	public interface CompositeLaunchDataListener {

		/** Sent when an event that the receiver has registered for occurs. */
		void handleEvent (CompositeLaunchDataEvent event);
		}

	/**
	 * ����� ����������� ������ � ����������� ������� � ����������� ������������
	 * */
	public static class CompositeLaunchDataEvent {
		public CompositeLaunchDataListenerTypes type = null;
		public CompositeLaunchData sender = null;
	}

	/**
	 * ����� ����������� ������� ��������� ��������� �������� ����������� ������������  
	 * */
	public static class CompositeLaunchDataEventChangedItem extends CompositeLaunchDataEvent {
		public CompositeLaunchItem item = null;			
	}
	
	/**
	 * ����� ����������� ������� ��������� �������� ��������� ����������� ������������  
	 * */
	public static class CompositeLaunchDataEventChangedItems extends CompositeLaunchDataEvent {
		public CompositeLaunchItems items = null;			
	}	
	
	// ��������� ������ ����������� ������������, �� ������� ��� ����������
	CompositeLaunchData data = null;
	
	// ������ ����������� �� ������� ��������� ������ � ���� �� ���� �������
	Map<CompositeLaunchDataListenerTypes, List<CompositeLaunchDataListener>> listeners = new HashMap<CompositeLaunchDataListenerTypes, List<CompositeLaunchDataListener>>();
		
	/** ��������� ���������� �� ������� ���� @type */
	public void AddListener(CompositeLaunchDataListenerTypes type, CompositeLaunchDataListener listener){	
		listeners.get(type).add(listener);
	}
	
	/** ����������� */
	public CompositeLaunchDataObserver(CompositeLaunchData _data){
		data = _data;
		
		listeners.put(CompositeLaunchDataListenerTypes.OnAddItem, new ArrayList<CompositeLaunchDataListener>());
		listeners.put(CompositeLaunchDataListenerTypes.OnRemoveItem, new ArrayList<CompositeLaunchDataListener>());
		listeners.put(CompositeLaunchDataListenerTypes.OnUpdateItem, new ArrayList<CompositeLaunchDataListener>());
		listeners.put(CompositeLaunchDataListenerTypes.OnChangedItems, new ArrayList<CompositeLaunchDataListener>());
	}
	
	
	/** ��������� ����������� �� ��������� ������ �������� ��������� ����������� ������������ */
	public void OnChangedItems(CompositeLaunchItems items){			
		for (CompositeLaunchDataListener listener : listeners.get(CompositeLaunchDataListenerTypes.OnChangedItems)){
			CompositeLaunchDataEventChangedItems event = new CompositeLaunchDataEventChangedItems();
			event.type = CompositeLaunchDataListenerTypes.OnChangedItems;
			event.sender = data;
			event.items = items;
			listener.handleEvent(event);
		}
	}
		
	/** ��������� ����������� � ���������� ��������� �������� � ����������� ������������ */
	public void OnAddItem(CompositeLaunchItem item){
		for (CompositeLaunchDataListener listener : listeners.get(CompositeLaunchDataListenerTypes.OnAddItem)){
			CompositeLaunchDataEventChangedItem event = new CompositeLaunchDataEventChangedItem();
			event.type = CompositeLaunchDataListenerTypes.OnAddItem;
			event.sender = data;
			event.item = item;
			listener.handleEvent(event);
		}
	}
	
	/** ��������� ����������� �� �������� ��������� �������� �� ����������� ������������ */
	public void OnRemoveItem(CompositeLaunchItem item){
		for (CompositeLaunchDataListener listener : listeners.get(CompositeLaunchDataListenerTypes.OnRemoveItem)){
			CompositeLaunchDataEventChangedItem event = new CompositeLaunchDataEventChangedItem();
			event.type = CompositeLaunchDataListenerTypes.OnRemoveItem;
			event.sender = data;
			event.item = item;
			listener.handleEvent(event);
		}
	}
	
	/** ��������� ����������� �� ���������� ��������� �������� ����������� ������������ */
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

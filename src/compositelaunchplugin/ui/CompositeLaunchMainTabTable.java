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

import java.text.Collator;
import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import compositelaunchplugin.CompositeLaunchData.CompositeLaunchItem;
import compositelaunchplugin.CompositeLaunchData.CompositeLaunchItems;

/**
 * Класс реализующий пользовательский элемент управления
 *      "Таблица композитной конфигурации"
 * | Name | Type | StartTime |
 * 
 * Аггрегирует класс org.eclipse.swt.widgets.Table, настраивает его "под себя",
 * ограничивает доступ к его полям и методам, добавляет новую функциональность
 * (перемещение строк, сортировка, выдача значений выделенных элементов и тд)
 * 
 */
public class CompositeLaunchMainTabTable{
	Table table = null;
	
	Color colorTextWithError = null;
	
	public CompositeLaunchMainTabTable(Composite parent, int style){
		table = new Table(parent, style);
		
		table.setHeaderVisible(true);
		TableColumn columnName = new TableColumn(table, SWT.NULL);
		columnName.setText("Name");
		columnName.setWidth(195);

		TableColumn columnType = new TableColumn(table, SWT.NULL);
		columnType.setText("Type");
		columnType.setWidth(120);

		TableColumn columnTime = new TableColumn(table, SWT.NULL);
		columnTime.setText("Start Time");
		columnTime.setWidth(70);


	   	Listener sortListener = new Listener() {			
	 		@Override
	 		public void handleEvent(Event event) {
	 			TableColumn column = (TableColumn) event.widget;
	 			Table table = column.getParent();
	 		
	 			if (table.getSortDirection() == SWT.NONE || table.getSortDirection() == SWT.DOWN)
	 				table.setSortDirection(SWT.UP);
	 			else
	 				table.setSortDirection(SWT.DOWN);
	 			
	 			SortTable(column, table.getSortDirection());
	 		}
	 	 };
	 	 
	     columnName.addListener(SWT.Selection, sortListener);
	     columnType.addListener(SWT.Selection, sortListener);
	     columnTime.addListener(SWT.Selection, sortListener);
	     
	     table.setSortColumn(table.getColumn(2));
	     table.setSortDirection(SWT.UP);
	     
	     colorTextWithError = new Color(parent.getDisplay(), 255, 0, 0);
	}

	/** Returns the zero-relative index of the item which is currently selected in the receiver, or -1 if no item is selected.  */
	public int getSelectionIndex() {
		return table.getSelectionIndex();
	}
	
	/** Sets the layout data associated with the receiver to the argument. */
	public void setLayoutData(GridData gridDataTable) {
		table.setLayoutData(gridDataTable);
	}
	
	/** Adds the listener to the collection of listeners who will be notified when an event of the given type occurs. ...*/
	public void addListener(int eventType, Listener listener) {
		table.addListener(eventType, listener);
	}
	
	/** Добавляет строку в конец таблицы */
	public void addItem(CompositeLaunchItem item){
		addItem(item,table.getItemCount());
	}
		
	/** Добавляет строку в таблицу в соответствии с указанным индексом */
	public void addItem(CompositeLaunchItem item, int index) {
		TableItem newTableItem = new TableItem(table, SWT.NONE, index);
			
		newTableItem.setText(0, item.Name);
		newTableItem.setText(1, item.Type);
		newTableItem.setText(2, Float.toString(item.StartTime));
		newTableItem.setData(item);
		
		if (item.HasError)
			newTableItem.setForeground(colorTextWithError);			
	}
	
	/** Обрабатывает событие добавления дочерней конфигурации в CompositeLaunchData */
	public void OnAddItem(CompositeLaunchItem item) {
		addItem(item);
		
		SortTable(table.getSortColumn(), table.getSortDirection());
				
		SetSelectedItem(item);
	}
	
	/** Обрабатывает событие удаления дочерней конфигурации в CompositeLaunchData */
	public void OnRemoveItem(CompositeLaunchItem item) {
		
		int index = 0;
		for (TableItem tableItem : table.getItems()){
			if (tableItem.getData() == item){
				table.remove(index);
				break;
			}
			index++;
		}					
		
		table.setSelection(java.lang.Math.min(index,table.getItems().length-1));
	}
	
	/** Обрабатывает событие обновление параметров дочерней конфигурации в CompositeLaunchData */
	public void OnUpdateItem(CompositeLaunchItem item) {
				
		int index = 0;
		for (TableItem tableItem : table.getItems()){
			if (tableItem.getData() == item){
				table.remove(index);
				addItem(item, index);
				break;
			}
			index++;
		}
		
		SortTable(table.getSortColumn(), table.getSortDirection());		
		SetSelectedItem(item);
	}
	
	/** Обновляет содержимое таблицы (заново заполняет) */
	public void OnChangedItems(CompositeLaunchItems items) {
		
		table.removeAll();

		for (CompositeLaunchItem item : items){
			addItem(item);
		}						
		
		SortTable(table.getSortColumn(), table.getSortDirection());
	}
	
	/** Меняет местами 2 строки, индексы которых указаны в качестве аргументов */
	public void swapItem(int from, int to) {
		if (from != to && table.getItemCount() > 1
			&& from >= 0 && from < table.getItemCount()
			&& to >= 0 && to < table.getItemCount())
		{
			TableItem item2Move = table.getItem(from);
			addItem((CompositeLaunchItem) item2Move.getData(), to > from ? to+1 : to);             	    

			// Dispose off, the old item.
			item2Move.dispose();
			table.setSelection(to);
		}
	}	
	
	/** Возвращает значение столбца "Name" для выделенной строки или пустую строку, если строка таблицы не выделена  */
	public String GetSelectedItemName() {
		if (table.getSelectionCount() == 1){
			return table.getItem(table.getSelectionIndex()).getText(0);
		}
		
		return "";
	}
	
	/** Возвращает значение столбца "StartTime" для выделенной строки или пустую строку, если строка таблицы не выделена  */
	public String GetSelectedItemStartTime() {
		if (table.getSelectionCount() == 1){
			return table.getItem(table.getSelectionIndex()).getText(2);
		}
		
		return "";
	}
	
	/** Возвращает объект CompositeLaunchItem соответствующий выделенной строке  */
	public CompositeLaunchItem GetSelectedCompositeLaunchItem() {
		if (table.getSelectionCount() == 1){
			return (CompositeLaunchItem) table.getItem(table.getSelectionIndex()).getData();
		}
		
		return null;
	}
	
	private void SetSelectedItem(CompositeLaunchItem item){
		for (TableItem tableItem : table.getItems())
			if (tableItem.getData() == item){
				table.setSelection(tableItem);
				break;
			}
	}

	/** Сортирует по выбранному столбцу в указанном направлении (SWT.UP или SWT.DOWN)  */
	public void SortTable(TableColumn column, int direction){
		Collator collator = Collator.getInstance(Locale.getDefault());
		boolean sortAsString = column.getText() == "Name" || column.getText() == "Type";
			
		int columnIndex = 0;
		while ( table.getColumn(columnIndex) != column)
			columnIndex++;
			
		for (int i = 1; i < table.getItems().length; ++i) {
			for (int j = i; j > 0; --j)	{
				String value1 = table.getItem(j-1).getText(columnIndex);
				String value2 = table.getItem(j).getText(columnIndex);
				if (table.getSortDirection() == SWT.DOWN
						&& (sortAsString && collator.compare(value1,value2) < 0
								|| !sortAsString && Float.parseFloat(value1) < Float.parseFloat(value2))
					|| table.getSortDirection() == SWT.UP
						&& (sortAsString && collator.compare(value1,value2) > 0
 							|| !sortAsString && Float.parseFloat(value1) > Float.parseFloat(value2)))
					swapItem(j-1,j);
				else
					break;
			}
		}
		 
		table.setSortColumn(column);
	}
}
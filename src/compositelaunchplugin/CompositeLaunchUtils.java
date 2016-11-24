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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;

import compositelaunchplugin.CompositeLaunchData.CompositeLaunchItem;
import compositelaunchplugin.CompositeLaunchData.CompositeLaunchItems;

/**
 * ����� ���������� ����� ����������� ������� ��� ������ � ������� ���������� � ������������� Composite
 */

public class CompositeLaunchUtils {

	/** ��������� �������� ���������� �� ����������� ������� ������� */
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
	
	/** ��������� ����� �� ���� �������� �������� ������� �� ������ ��������� ������������ */
	public static boolean ItemCanBeAdded(ILaunchConfiguration parentConf, ILaunchConfiguration childConf) {
		
		// ������������ �� ����� ���� ���������, ������ ���� ��� ����������� ������������,
		// ������� ������� � ������������ ��� �������
		
		try {
			// ���� ����������� ������������ �� ���� Composite - ����� ���������
			if (parentConf.getType() != childConf.getType()){
				return true;					
			}
			else{
				// ����������� ������������ - �����������.
				// ��������� �� ������� �� � ���������� � ��������� ������
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
	
	/** ���������� ������ ������������ ��� ����������������� ����������
	 * ������������� ����������� ������������, ������� ������������ � ��������, ��������� ������ �� �������  */
	public static CompositeLaunchItems ProcessingItemsBeforeRunning(CompositeLaunchData compositeLaunchData){	
		CompositeLaunchItems rstItems = new CompositeLaunchItems();
		
		for (CompositeLaunchItem item : compositeLaunchData.GetItems()){
			if (item.HasError || item.LaunchConfiguration == null)
				continue;
			
			try {
				if (compositeLaunchData.GetILaunchConfiguration().getType().equals(item.LaunchConfiguration.getType())){
					// ��� ����������� ������������ ���� �������� � ����� ������ ������ � �������� ������������
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

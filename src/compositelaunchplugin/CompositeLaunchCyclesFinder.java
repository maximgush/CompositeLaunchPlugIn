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
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;

import compositelaunchplugin.CompositeLaunchData.CompositeLaunchItem;

/**
 * ����� ����������� ����� ������ ����� �������� ����������� ������������
 * ������ ������������ ����������� ������������
 * 
 * ������ ������ ����� �������� ������������ (��� ���� ��������) � ���������,
 * ��� �� ���� �� ������ ����� � ����� ������ ��� ������������ ������ ���� ��������� 
 */

public class CompositeLaunchCyclesFinder {
	public static class CompositeConfigurationNode{
		List<CompositeConfigurationNode>	childs = new ArrayList<CompositeConfigurationNode>();
		CompositeConfigurationNode			parent = null;
		ILaunchConfiguration				conf = null;
		
		public CompositeConfigurationNode(ILaunchConfiguration _conf, CompositeConfigurationNode _parent){
			conf = _conf;
			parent = _parent;
		}
		
		/** ��������� ��� ������� ���� ������ �������� ����� �� ������ �������� ������������ ����������� ������������ */
		public void Parse(){
			CompositeLaunchData compositeLaunchData = new CompositeLaunchData();
			compositeLaunchData.LoadItems(conf);
			for (CompositeLaunchItem item : compositeLaunchData.GetItems()){
				try {
					if (item.LaunchConfiguration != null && item.LaunchConfiguration.getType().getName().equals(conf.getType().getName()))
						childs.add(new CompositeConfigurationNode(item.LaunchConfiguration, this));
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		}
		
		/** ������� ���� � ����� �� ������������� �� ���� �� ����� ���� � ����� */
		public CompositeConfigurationNode FindSameConfigurationInParentNodes(){
			CompositeConfigurationNode node = parent;
			while (node != null){
				if (node.conf.getName().equals(conf.getName()))
					return node;
				node = node.parent;
			}
			
			return null;
		}
	}
	
	/** ��������� ����� �� ���� � ���������� ������������ ����� ���������� ����� ������������.
	 *  ����� �������������� ������ � ��������� ����������� ����� ������������ */
	public static boolean FindCycleAfterAddingChild(ILaunchConfiguration parent, ILaunchConfiguration child){
			
		try {
			if (parent == null || child == null || parent.getType() != child.getType())
				return false;
		} catch (CoreException e) {
			e.printStackTrace();
		}
			
		CompositeConfigurationNode root = new CompositeConfigurationNode(parent, null);
		CompositeConfigurationNode node = new CompositeConfigurationNode(child, root);
		
		Queue<CompositeConfigurationNode> nodesToParse = new LinkedList<CompositeConfigurationNode>();
		
		do{			
			if (node.FindSameConfigurationInParentNodes() != null)
				return true;
			
			node.Parse();					
			nodesToParse.addAll(node.childs);
			node = nodesToParse.poll();
		} while (node != null);
		
		return false;
	}
	
	
}

package edu.stanford.smi.protege.collab.annotation.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import edu.stanford.smi.protege.collab.util.CollabProtegeConfiguration;
import edu.stanford.smi.protege.collab.util.CollabTabsConfiguration;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.WidgetDescriptor;
import edu.stanford.smi.protege.resource.Icons;
import edu.stanford.smi.protege.ui.CheckBoxRenderer;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.util.AbstractValidatableComponent;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.DefaultRenderer;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.StringUtilities;
import edu.stanford.smi.protege.widget.TabWidget;
import edu.stanford.smi.protege.widget.WidgetDescriptorRenderer;
import edu.stanford.smi.protege.widget.WidgetUtilities;

public class ConfigureCollabTabsPanel extends AbstractValidatableComponent{

    private JTable _table;
    private Project _project;
    private boolean _dirty;

    private class MoveTabUp extends AbstractAction {
        MoveTabUp() {
            super("Move selected tab up", Icons.getUpIcon());
        }

        public void actionPerformed(ActionEvent event) {
            int index = _table.getSelectedRow();
            if (canMoveUp(index)) {
                getTabModel().moveRow(index, index, index - 1);
                int n = index - 1;
                _table.getSelectionModel().setSelectionInterval(n, n);
                _dirty = true;
            }
        }
    }

    private class MoveTabDown extends AbstractAction {
        MoveTabDown() {
            super("Move selected tab down", Icons.getDownIcon());
        }

        public void actionPerformed(ActionEvent event) {
            int index = _table.getSelectedRow();
            if (canMoveDown(index)) {
                getTabModel().moveRow(index, index, index + 1);
                int n = index + 1;
                _table.getSelectionModel().setSelectionInterval(n, n);
                _dirty = true;
            }
        }
    }

    private boolean canMoveUp(int index) {
        return index > 0 && isEnabled(index);
    }

    private boolean canMoveDown(int index) {
        boolean canMoveDown = 0 <= index && index < _table.getRowCount() - 1;
        if (canMoveDown) {
            canMoveDown = isEnabled(index) && canEnable(index + 1);
        }
        return canMoveDown;
    }

    private boolean isEnabled(int row) {
        Boolean b = (Boolean) getTabModel().getValueAt(row, 0);
        return b.booleanValue();
    }

    private void setEnabled(int row, boolean enabled) {
        getTabModel().setValueAt(Boolean.valueOf(enabled), row, 0);
    }

    private class ClickListener extends MouseAdapter {
        public void mousePressed(MouseEvent event) {
            Point p = event.getPoint();
            int col = _table.columnAtPoint(p);
            if (col == 0) {
                int row = _table.rowAtPoint(p);                
                boolean b = isEnabled(row);
                setEnabled(row, !b);
                _dirty = true;                               
            }
        }
    }


    protected ConfigureCollabTabsPanel(Project project) {
        setLayout(new BorderLayout());
        _project = project;
        _table = ComponentFactory.createTable(null);
        _table.setModel(createTableModel());
        ComponentUtilities.addColumn(_table, new CollabTabEnableRenderer());
        _table.getColumnModel().getColumn(0).setMaxWidth(50);
        ComponentUtilities.addColumn(_table, new CollabTabNameRenderer());
        
        _table.addMouseListener(new ClickListener());
        JScrollPane pane = ComponentFactory.createScrollPane(_table);
        pane.setColumnHeaderView(_table.getTableHeader());
        pane.setBackground(_table.getBackground());
        LabeledComponent c = new LabeledComponent("Tabs", pane);
        
        //TODO: maybe add later
        //c.addHeaderButton(new MoveTabUp());
        //c.addHeaderButton(new MoveTabDown());
        
        add(c);
    }

    private TableModel createTableModel() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Visible");
        model.addColumn("Collaborative Tab");
        boolean all = true; // _filterComboBox.getSelectedItem().equals(ALL);
        
        Iterator<Class<?>> i = CollabTabsConfiguration.getAllCollabTabClasses().iterator();
        
        while (i.hasNext()) {
            Class<?> tab =  i.next();           
            model.addRow(new Object[] {CollabTabsConfiguration.isTabEnabled(_project, tab), tab });
           
        }
               
        
        return model;
    }

    private Collection sort(Collection descriptors) {
        List sortedDescriptors = new ArrayList(descriptors);
        int i;
        for (i = 0; i < sortedDescriptors.size(); ++i) {
            WidgetDescriptor d = (WidgetDescriptor) sortedDescriptors.get(i);
            if (!d.isVisible()) {
                break;
            }
        }
        Collections.sort(sortedDescriptors, new WidgetDescriptorComparator());
        return sortedDescriptors;
    }


    private DefaultTableModel getTabModel() {
        return (DefaultTableModel) _table.getModel();
    }

    public void saveContents() {
        if (_dirty) {           
            for (int row = 0; row < getTabModel().getRowCount(); ++row) {
                boolean isEnabled = isEnabled(row);
                CollabTabsConfiguration.setTabEnabled(_project, getDescriptor(row), isEnabled);            
            }
        }
    }

    public boolean validateContents() {
        return true;
    }

    private boolean canEnable(String className) {
        //return WidgetUtilities.isSuitableTab(className, _project, new ArrayList());
    	return true;
    }

    private Class<?> getDescriptor(int row) {
        return (Class<?>) getTabModel().getValueAt(row, 1);
    }

    private boolean canEnable(int row) {
    	return true;
        //Class<?> d = getDescriptor(row);
        //return canEnable(d);
    }

    private boolean canEnable(WidgetDescriptor d) {
        return canEnable(d.getWidgetClassName());
    }

    class WidgetDescriptorComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            WidgetDescriptor wd1 = (WidgetDescriptor) o1;
            WidgetDescriptor wd2 = (WidgetDescriptor) o2;
            boolean isEnabled1 = wd1.isVisible();
            boolean isEnabled2 = wd2.isVisible();
            int compare;
            if (isEnabled1) {
                compare = isEnabled2 ? 0 : -1;
            } else {
                compare = isEnabled2 ? +1 : 0;
            }
            if (!isEnabled1 && !isEnabled2) {
                String n1 = wd1.getWidgetClassName();
                String n2 = wd2.getWidgetClassName();
                boolean canEnable1 = canEnable(n1);
                boolean canEnable2 = canEnable(n2);
                if (canEnable1) {
                    compare = canEnable2 ? 0 : -1;
                } else {
                    compare = canEnable2 ? +1 : 0;
                }
                if (compare == 0) {
                    String sn1 = StringUtilities.getShortClassName(n1);
                    String sn2 = StringUtilities.getShortClassName(n2);
                    compare = sn1.compareToIgnoreCase(sn2);
                }
            }
            return compare;
        }

    }

    class CollabTabEnableRenderer extends CheckBoxRenderer {
        private final Component EMPTY;

        {
            EMPTY = new JPanel() {
                public boolean isOpaque() {
                    return false;
                }
            };
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean b,
                int row, int col) {
            Component c;
            if (canEnable(row)) {
                c = super.getTableCellRendererComponent(table, value, isSelected, b, row, col);
            } else {
                c = EMPTY;
            }
            return c;
        }
    }
	
    
    private class CollabTabNameRenderer extends DefaultRenderer {

    	public void load(Object o) {
            StringBuffer text = new StringBuffer();
            Class d = (Class) o;
            setMainText(d.getSimpleName());
        }
    }
    
}

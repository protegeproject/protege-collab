package edu.stanford.smi.protege.collab.annotation.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import edu.stanford.smi.protege.collab.util.CollabTabsConfiguration;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.ui.CheckBoxRenderer;
import edu.stanford.smi.protege.util.AbstractValidatableComponent;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.DefaultRenderer;
import edu.stanford.smi.protege.util.LabeledComponent;

public class ConfigureCollabTabsPanel extends AbstractValidatableComponent{

    private JTable _table;
    private Project _project;
    private boolean _dirty;

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
        
        add(c);
    }

    private TableModel createTableModel() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Visible");
        model.addColumn("Collaborative Tab");
        
        Iterator<Class<?>> i = CollabTabsConfiguration.getAllCollabTabClasses().iterator();
        
        while (i.hasNext()) {
            Class<?> tab =  i.next();           
            model.addRow(new Object[] {CollabTabsConfiguration.isTabEnabled(_project, tab), tab });
           
        }
               
        
        return model;
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

    private Class<?> getDescriptor(int row) {
        return (Class<?>) getTabModel().getValueAt(row, 1);
    }

    private boolean canEnable(int row) {
    	return true;
        //Class<?> d = getDescriptor(row);
        //return canEnable(d);
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
            Class d = (Class) o;
            setMainText(d.getSimpleName());
        }
    }
    
}

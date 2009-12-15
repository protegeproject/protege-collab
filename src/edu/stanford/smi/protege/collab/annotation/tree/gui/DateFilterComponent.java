package edu.stanford.smi.protege.collab.annotation.tree.gui;

import java.util.ArrayList;
import java.util.Date;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.toedter.calendar.JDateChooser;

import edu.stanford.smi.protege.util.Log;


public class DateFilterComponent implements FilterValueComponent {
	public static final String DATE_FORMAT = "MM/dd/yyyy";
	private JPanel panel;
	private JDateChooser fromDate;
	private JDateChooser toDate;

	public DateFilterComponent() {
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
				
		fromDate = new JDateChooser();
		toDate = new JDateChooser();
		fromDate.setDateFormatString(DATE_FORMAT);
		toDate.setDateFormatString(DATE_FORMAT);				
		
		panel.add(new JLabel("From: "));
		panel.add(fromDate);
		panel.add(new JLabel("To: "));
		panel.add(toDate);
	}
	
	public Object getValue() {
		ArrayList<Date> dates = new ArrayList<Date>();
		dates.add(fromDate.getDate());
		dates.add(toDate.getDate());
		return dates;
	}

	public JComponent getValueComponent() {		
		return panel;
	}

	public void setValue(Object value) {
		if (value == null) {
			fromDate.setDate(null);
			toDate.setDate(null);
		} else if (value instanceof Date) {
			fromDate.setDate((Date)value);
		} else {
			Log.getLogger().warning("Invalid date passed to the date filter component: " + value);
		}
	}

}

package edu.stanford.smi.protege.collab.annotation.tree.filter;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import edu.stanford.bmir.protegex.chao.annotation.api.AnnotatableThing;
import edu.stanford.bmir.protegex.chao.annotation.api.AnnotationFactory;
import edu.stanford.bmir.protegex.chao.change.api.ChangeFactory;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.impl.DefaultTimestamp;
import edu.stanford.smi.protege.code.generator.wrapping.AbstractWrappedInstance;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;

public class DateFilter extends AbstractFilter<AnnotatableThing> {

	public DateFilter() {
		super();
	}

	@Override
	public boolean isValid(AnnotatableThing object) {
		Date fromDate = getFromDate();
		Date toDate = getToDate();

		if (fromDate == null && toDate == null) {
			return true;
		}

		Date frameDate = getFrameDate(object);

		if (frameDate == null) {
			return true;
		}

		boolean success = false;

		if (fromDate != null) {
			success = frameDate.after(fromDate) || equalSimpleDates(fromDate, frameDate) ;
		}

		if (toDate != null) {
			success = success && (frameDate.before(toDate) || equalSimpleDates(toDate, frameDate));
		}

		return success;
	}

	private Date getFrameDate(AnnotatableThing object) {
		Instance inst = ((AbstractWrappedInstance) object).getWrappedProtegeInstance();
		KnowledgeBase chnagesKb = inst.getKnowledgeBase();
		Slot timestampSlot = new ChangeFactory(chnagesKb).getTimestampSlot();
		Cls annotationCls = new AnnotationFactory(chnagesKb).getAnnotationClass();
		if (inst.hasType(annotationCls)) {
			timestampSlot = new AnnotationFactory(chnagesKb).getCreatedSlot();
		}
		Instance timestamp = (Instance) inst.getOwnSlotValue(timestampSlot);
		if (timestamp == null) {
			return null;
		}
		Slot dateSlot = new AnnotationFactory(chnagesKb).getDateSlot();
		String dateString = (String) timestamp.getOwnSlotValue(dateSlot);
		if (dateString == null) {
			return null;
		}

		Date date = null;
		try {
			date = DefaultTimestamp.DATE_FORMAT.parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return getSimpleDate(date);
	}

	public Date getFromDate() {
		List filterValue = (List) getFilterValue();

		if (filterValue == null) {
			return null;
		}

		if (filterValue.size() > 0) {
			return getSimpleDate((Date) filterValue.get(0));
		}

		return null;
	}

	public Date getToDate() {
		List filterValue = (List) getFilterValue();
		if (filterValue == null) {
			return null;
		}
		if (filterValue.size() > 1) {
			return getSimpleDate((Date) filterValue.get(1));
		}
		return null;
	}

	//I'm sure there is a simple way to do this
	private Date getSimpleDate(Date date) {
		if (date == null) {
			return null;
		}

		Calendar dateCal = new GregorianCalendar();
        dateCal.setTime(date);
        int year = dateCal.get(Calendar.YEAR);
        int month = dateCal.get(Calendar.MONTH);
        int day = dateCal.get(Calendar.DAY_OF_MONTH);

        Calendar newDateCal = new GregorianCalendar(year, month, day, 0, 0, 0);

        return newDateCal.getTime();
	}

	//FIXME: optimize performance
	private boolean equalSimpleDates(Date date1, Date date2) {
		if (date1 == null && date2 == null) {
			return true;
		}
		if (date1 == null || date2 == null) {
			return false;
		}
		return date1.compareTo(date2) == 0;
	}

}

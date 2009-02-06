package edu.stanford.smi.protege.collab.annotation.gui.renderer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import edu.stanford.bmir.protegex.chao.annotation.api.Annotation;
import edu.stanford.bmir.protegex.chao.annotation.api.AnnotationFactory;
import edu.stanford.bmir.protegex.chao.annotation.api.Vote;
import edu.stanford.smi.protege.model.BrowserSlotPattern;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.ApplicationProperties;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.StringUtilities;

public class AnnotationBrowserSlotPattern extends BrowserSlotPattern {
	public final static String SHOW_AUTHOR_DATE_IN_ANNOTATION_TEXT_PROP = "show.author.date.in.annotation.text";

	private boolean showAuthorDate;
	private KnowledgeBase chaoKb;

	public AnnotationBrowserSlotPattern(List elements) {
		super(elements);
	}

	public AnnotationBrowserSlotPattern(KnowledgeBase chaoKb) {
		super(new AnnotationFactory(chaoKb).getSubjectSlot());
		this.chaoKb = chaoKb;
		showAuthorDate = ApplicationProperties.getBooleanProperty(SHOW_AUTHOR_DATE_IN_ANNOTATION_TEXT_PROP, true);
	}

	@Override
	public String getBrowserText(Instance instance) {
		if (!showAuthorDate || instance == null) { return super.getBrowserText(instance); }

		Annotation annot = AnnotationFactory.getGenericAnnotation(instance);
		if (annot == null) { return super.getBrowserText(instance); }

		String dateStr = "";
		try {
			Date date = annot.getCreated().getDateParsed();
			dateStr = new SimpleDateFormat("MM/dd/yy hh:mm").format(date);
		} catch (Exception e) {
			if (Log.getLogger().isLoggable(Level.FINE)) {
				Log.getLogger().log(Level.FINE, "No creation date for annotation: " + instance, e);
			}
		}
		String subj = annot.getSubject();
		String author = annot.getAuthor();

		StringBuffer buffer = new StringBuffer(author == null ? "(no author)" : author);
		buffer.append(" (");
		buffer.append(dateStr);
		buffer.append("): ");

		String text = "";
		if (annot.canAs(Vote.class)) {
			text = getVoteString(annot);
		} else {
			text = subj == null ? StringUtilities.stripHtmlTags(annot.getBody()) : subj;
		}
		buffer.append(text);

		return buffer.toString();
	}

	private String getVoteString(Annotation annot) {
		Vote vote = annot.as(Vote.class);
		if (vote == null) { return annot.getBody(); }
		return vote.getVoteValue();
	}

}

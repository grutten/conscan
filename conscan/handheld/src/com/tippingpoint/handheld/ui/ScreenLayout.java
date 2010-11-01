package com.tippingpoint.handheld.ui;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.tippingpoint.handheld.data.Activity;
import com.tippingpoint.handheld.data.ComplianceConfiguration;
import com.tippingpoint.handheld.data.ComplianceValue;
import com.tippingpoint.handheld.data.DataInterface;
import com.tippingpoint.handheld.data.LegacyData;
import com.tippingpoint.handheld.data.Location;
import com.tippingpoint.handheld.data.LogEntry;
import com.tippingpoint.handheld.data.Offender;
import com.tippingpoint.handheld.data.Scannable;

public class ScreenLayout extends ScreenListeners {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4407720571552707102L;

	ScreenLayout(DataInterface d, boolean bIsRunningOnHandheld) {
		super(bIsRunningOnHandheld);
		
        setData(d);
        
        populateActivities(m_choiceActivity, getData().getActivities());
        m_choiceActivity.addItemListener(getActivityChoiceListener());

        // NOTE: the listener that responds to a scan event populates the
        // collection(s) behind each of the offender compliance fields.
        
        m_buttonGoActivity = new Button(BUTTON_ACTIVITY);
        m_buttonGoActivity.addActionListener(getGoActivityButtonListener());

        m_buttonExit = new Button(BUTTON_EXIT);
        m_buttonExit.addActionListener(getExitButtonListener());
        
        m_buttonGoDetail = new Button(BUTTON_DETAIL);
        m_buttonGoDetail.addActionListener(getGoDetailButtonListener());

        m_buttonNext = new Button(BUTTON_NEXT);
        m_buttonPrev = new Button(BUTTON_PREV);
        
        m_buttonRecord = new Button("Record");
        m_buttonRecord.addActionListener(getRecordButtonListener());
        
        m_buttonReplace = new Button(BUTTON_REPLACE);
        // m_buttonReplace.addActionListener()...

        m_buttonScanIndicator = new Button("Offender");
        m_buttonScanIndicator.addActionListener(getScanIndicatorButtonListener());
        
        m_panelBodyActivity = new Panel();
        m_panelBodyDetail = new Panel();
        m_panelBottom = new Panel();
		m_panelBodyActivity.setLayout(new GridBagLayout());
		m_panelBodyDetail.setLayout(new GridBagLayout());
    	m_panelBottom.setLayout(new GridBagLayout());
	}
	
	public void draw() {
		super.draw();

        add(m_panelBottom, BorderLayout.SOUTH);
		drawActivityScreen();

		setVisible(true);
	}

	protected void drawActivityScreen() {
		m_screenState = SCREEN_STATE_ACTIVITY;

		String strSelection = m_choiceActivity.getSelectedItem();
		Activity activity = findActivity(strSelection); 

		// TODO: is this call sufficient to garbage collect all the compliance
		// controls created and added in the hierarchy of panels each time an
		// activity screen is rendered???
		m_panelBodyActivity.removeAll();
		addBodyPanel(m_panelBodyActivity, BorderLayout.NORTH);

        int nRow = 0;
        
        // Activity
        addLabel(m_panelBodyActivity, new Label("Activity"), 0, nRow++, 1, 1, GridBagConstraints.NORTHWEST);
		addField(m_panelBodyActivity, m_choiceActivity, 0, nRow++, 1, 1, GridBagConstraints.NORTHWEST);
        
		// Documentation
		final Checkbox cbDocumentation = new Checkbox("Notes");
//		addField(m_panelBodyActivity, cbDocumentation, 0, nRow++, 1, 1, GridBagConstraints.NORTHWEST);
        addItem(m_panelBodyActivity, cbDocumentation, 0, nRow++, 1, 1, 1.0, 0.0, 0, 15, 15, 15, GridBagConstraints.NORTHWEST);

        ArrayList arrScannables = getData().getScannables();
        
        // Render the offender/compliance label/dropdown
        Iterator i = arrScannables.iterator();
        if (i.hasNext()) {
        	// Draw the FIRST scannable set of fields
        	Object o = i.next();
        	Scannable s = null;
        	if (o instanceof Scannable)
        		s = (Scannable)o;
        	if (s != null)
        		nRow = drawDynamicFields(s, activity, nRow, s.getComplianceControl());
        	// TODO: detect a null scannable and add a message to the screen.
        	// Determine how to handle elegantly.
        }
        if (i.hasNext()) {
        	// Draw the SECOND scannable set of fields
        	Object o = i.next();
        	Scannable s = null;
        	if (o instanceof Scannable)
        		s = (Scannable)o;
        	
        	if (s != null)
        		drawDynamicFields(s, activity, nRow, s.getComplianceControl());
        	// TODO: detect a null scannable and add a message to the screen.
        	// Determine how to handle elegantly.
        }
        
        

		// DEV only - scan code
        m_labelBarcode.setText(getData().getBarcode());
//        addLabel(m_panelBodyActivity, m_labelBarcode, 0, nRow++, 1, 1, GridBagConstraints.NORTHWEST);
        
        String strFeedback = getData().getFeedback();
        m_labelFeedback.setText(strFeedback);
        if (strFeedback != null && strFeedback.length() > 0) 
            addLabel(m_panelBodyActivity, m_labelFeedback, 0, nRow++, 1, 1, GridBagConstraints.NORTHWEST);
        	
        	
//		drawButtons(m_panelBodyActivity, nRow);
		drawButtons(null, nRow);
	}

    protected void drawDetailScreen() {
        m_screenState = SCREEN_STATE_DETAIL;
    	
        // Toolbar Panel
		m_panelBodyDetail.removeAll();
    	
        // Location
        LogEntry logEntry = getData().getLogEntry();
        int nRow = 0;
        addLabel(m_panelBodyDetail, new Label("Institution: TTCF"), 0, nRow++, 1, 1, GridBagConstraints.NORTHWEST);
        if (logEntry.getOffender() != null)
        	addLabel(m_panelBodyDetail, new Label("Offender: " + logEntry.getOffender().getName()), 0, nRow++, 1, 1, GridBagConstraints.NORTHWEST);
        if (logEntry.getLocation() != null)
        	addLabel(m_panelBodyDetail, new Label("Location: " + logEntry.getLocation().getName()), 0, nRow++, 1, 1, GridBagConstraints.NORTHWEST);
        addLabel(m_panelBodyDetail, new Label("Activity: " + logEntry.getActivity().getName()), 0, nRow++, 1, 1, GridBagConstraints.NORTHWEST);
        if (logEntry.getComplianceValue() != null)
        	addLabel(m_panelBodyDetail, new Label("Compliance: " + logEntry.getComplianceValue().getValue()), 0, nRow++, 1, 1, GridBagConstraints.NORTHWEST);
        addLabel(m_panelBodyDetail, new Label("Creation Date: " + logEntry.getDateCreated()), 0, nRow++, 1, 1, GridBagConstraints.NORTHWEST);

    	drawButtons(m_panelBodyDetail, 4);
    	addBodyPanel(m_panelBodyDetail, BorderLayout.NORTH);
    }

	protected void drawFindOffenderScreen() {
		m_screenState = SCREEN_STATE_FIND_OFFENDER;

		m_panelBodyActivity.removeAll();
		addBodyPanel(m_panelBodyActivity, BorderLayout.NORTH);

        int nRow = 0;
        
        // Activity
        addLabel(m_panelBodyActivity, new Label("Existing: "), 0, nRow++, 1, 1, GridBagConstraints.NORTHWEST);
        
        Choice cOffenders = new Choice();

        // Render the offender/compliance label/dropdown
        Set entries = getData().getOffenders().entrySet();
        Iterator i = entries.iterator();
        while (i.hasNext()) {
        	Map.Entry entry = (Map.Entry) i.next();
        	Object o = entry.getValue();
        	if (o instanceof Offender) {
        		Offender currOffender = (Offender)o;
        		
        		String strOffender = currOffender.getName() + ", " + currOffender.getBookingNumber();
    	        cOffenders.add(strOffender);
        	}
        }
        
        addField(m_panelBodyActivity, cOffenders, 0, nRow++, 1, 1, GridBagConstraints.NORTHWEST);

        addLabel(m_panelBodyActivity, new Label("Offender: "), 0, nRow++, 1, 1, GridBagConstraints.NORTHWEST);
        TextField barCode = new TextField();
        addField(m_panelBodyActivity, barCode, 0, nRow++, 1, 1, GridBagConstraints.NORTHWEST);
        
		// DEV only - scan code
        m_labelBarcode.setText(getData().getBarcode());
//        addLabel(m_panelBodyActivity, m_labelBarcode, 0, nRow++, 1, 1, GridBagConstraints.NORTHWEST);
        
		drawButtons(null, nRow);
	}
    
    private void drawButtons(Panel p, int nRow) {
    	m_panelBottom.removeAll();
    	
        switch (m_screenState) {
	        case SCREEN_STATE_DETAIL:
	        	m_panelBottom.add(m_buttonGoActivity);
	        	m_panelBottom.add(m_buttonPrev);
	        	m_panelBottom.add(m_buttonNext);
//	        	m_panelBottom.add(m_buttonGoHistory);
	        	break;
	        case SCREEN_STATE_FIND_OFFENDER:
	        	m_panelBottom.add(m_buttonReplace);
	        	m_panelBottom.add(m_buttonGoActivity);
	        	break;
	        case SCREEN_STATE_ACTIVITY:
	        default:
	            m_panelBottom.add(m_buttonRecord);
	            m_panelBottom.add(m_buttonGoDetail);
//	        	m_panelBottom.add(m_buttonGoHistory);
	        	break;
	    }
        m_panelBottom.add(m_buttonExit);
        
//        addField(p, m_panelBottom, 0, nRow, 1, 1, GridBagConstraints.SOUTHWEST);
    }
    
    private int drawDynamicFields(Scannable scannable, Activity activity, int nRow, DataChoice cCompliance) {
    	Object o = scannable.getObject();
    	if (o instanceof Offender) {
    		Panel pCompliance = new Panel();
    		pCompliance.setLayout(new GridBagLayout());
    		Offender currOffender = (Offender)o;

    		// Label containing offender's name, booking #
    		String strOffender = currOffender.getName() + ", " + currOffender.getBookingNumber();
	        
    		populateCompliance(cCompliance, activity, getData().getCompliance());
	        if (activity.isCellScan() && activity.isOffenderCompliance()) {
    	        // Render: Compliance button for replacing this offender + combo
    	        Button buttonReplaceOffender = new Button(">");
    	        buttonReplaceOffender.addActionListener(getGoOffenderReplacementListener());
    	        addItem(pCompliance, buttonReplaceOffender, 0, 0, 1, 1, 0.0, 0.0, 0, 0, 1, 1, GridBagConstraints.NORTHWEST);
    	        addItem(pCompliance, new Label(strOffender), 1, 0, 1, 1, .95, 0.0, 0, 1, 1, 0, GridBagConstraints.NORTHWEST);
    	        
    	        addItem(m_panelBodyActivity, pCompliance, 0, nRow++, 1, 1, 1.0, 0.0, 0, 15, 0, 15, GridBagConstraints.NORTHWEST);
	        }
	        else
    	        addLabel(m_panelBodyActivity, new Label(strOffender), 0, nRow++, 1, 1, GridBagConstraints.NORTHWEST);
	        
	        	
	        addItem(m_panelBodyActivity, cCompliance, 0, nRow++, 1, 1, 1.0, 0.0, 0, 15, 15, 15, GridBagConstraints.NORTHWEST);
    	}
    	else if (o instanceof Location) {
    		Location currLocation = (Location)o;
    		
    		String strLocation = currLocation.getName();
	        addLabel(m_panelBodyActivity, new Label(strLocation), 0, nRow++, 1, 1, GridBagConstraints.NORTHWEST);
	        populateCompliance(cCompliance, activity, getData().getCompliance());
	        addField(m_panelBodyActivity, cCompliance, 0, nRow++, 1, 1, GridBagConstraints.NORTHWEST);
    	}

    	return nRow;
    }
    
    private void populateActivities(Choice c, ArrayList listActivities) {
    	Iterator iActivities = listActivities.iterator();
    	while (iActivities.hasNext()) {
    		Activity a = (Activity)iActivities.next();
    		
    		c.add(a.getName());
    	}
    }
    
    /**
     * This method populates the compliance list based on the compliance type
     * of a given activity.
     * @param choice
     * @param listActivities
     */
    private void populateCompliance(DataChoice choice, Activity a, HashMap mapCompliance) {
//    	ComplianceConfiguration complianceConfiguration = (ComplianceConfiguration)m_hashCompliance.get(a.getComplianceId());
    	ComplianceConfiguration complianceConfiguration = (ComplianceConfiguration)mapCompliance.get(a.getComplianceId());
    	ArrayList listValues = complianceConfiguration.getValues();
    	Iterator i = listValues.iterator();
    	choice.removeAll();
    	
    	String strDefault = null;
    	while(i.hasNext()) {
    		ComplianceValue complianceValue = (ComplianceValue)i.next();
    		
    		if (complianceValue.getDefault())
    			strDefault = complianceValue.getValue();
    		choice.add(complianceValue.getValue(), complianceValue);
    	}
    	
    	if (strDefault == null)
    		choice.select(0);
    	else
    		choice.select(strDefault);
    	
    }
    
}

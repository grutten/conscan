package com.tippingpoint.handheld.ui;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.List;
import java.awt.Panel;
import java.util.ArrayList;
import java.util.Iterator;

import com.tippingpoint.handheld.data.Activity;
import com.tippingpoint.handheld.data.Data;
import com.tippingpoint.handheld.data.Offender;

public class ScreenLayout extends ScreenListeners {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4407720571552707102L;

	ScreenLayout(Data d, boolean bIsRunningOnHandheld) {
		super(bIsRunningOnHandheld);
		
        m_data = d;
        
        m_data.populateActivities(m_choiceActivity, m_data.getActivities());
        m_choiceActivity.addItemListener(getActivityChoiceListener());

        // NOTE: the listener that responds to a scan event populates the
        // collection(s) behind each of the offender compliance fields.
        
        m_buttonGoActivity = new Button(BUTTON_ACTIVITY);
        m_buttonGoActivity.addActionListener(getGoActivityButtonListener());

        m_buttonExit = new Button(BUTTON_EXIT);
        m_buttonExit.addActionListener(getExitButtonListener());
        
        m_buttonGoDetail = new Button(BUTTON_DETAIL);
        m_buttonGoDetail.addActionListener(getGoDetailButtonListener());

        m_buttonGoHistory = new Button(BUTTON_HISTORY);
        m_buttonGoHistory.addActionListener(getGoHistoryButtonListener());
        
        m_buttonNext = new Button(BUTTON_NEXT);
        m_buttonPrev = new Button(BUTTON_PREV);
        
        m_buttonRecord = new Button("Record");
        m_buttonRecord.addActionListener(getRecordButtonListener());

        m_buttonScanIndicator = new Button("Offender");
        m_buttonScanIndicator.addActionListener(getScanIndicatorButtonListener());
        
        m_panelBodyActivity = new Panel();
        m_panelBodyDetail = new Panel();
        m_panelBodyHistory = new Panel();
        m_panelBottom = new Panel();
		m_panelBodyActivity.setLayout(new GridBagLayout());
		m_panelBodyDetail.setLayout(new GridBagLayout());
    	m_panelBodyHistory.setLayout(new GridBagLayout());
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
		Activity a = findActivity(strSelection); 

		m_panelBodyActivity.removeAll();
		addBodyPanel(m_panelBodyActivity, BorderLayout.NORTH);

        int nRow = 0;
        
        // Activity
        addLabel(m_panelBodyActivity, new Label("Activity"), 0, nRow++, 1, 1, GridBagConstraints.NORTHWEST);
		addField(m_panelBodyActivity, m_choiceActivity, 0, nRow++, 1, 1, GridBagConstraints.NORTHWEST);
        
		// Documentation
		final Checkbox cbDocumentation = new Checkbox("Notes");
		addField(m_panelBodyActivity, cbDocumentation, 0, nRow++, 1, 1, GridBagConstraints.NORTHWEST);

		// Scan indicator
		String strScanIndicatorText = a.isCellScan() ? "Scan Cell" : BUTTON_SCANINDICATOR;
		m_buttonScanIndicator.setLabel(strScanIndicatorText);
		addField(m_panelBodyActivity, m_buttonScanIndicator, 0, nRow++, 1, 1, GridBagConstraints.NORTHWEST);
		
        ArrayList arrScannables = m_data.getScannables();
        
        // Render the offender/compliance label/dropdown
        Iterator i = arrScannables.iterator();
        while (i.hasNext()) {
        	Object o = i.next();
        	if (o instanceof Offender) {
        		Offender currOffender = (Offender)o;
        		
        		String strOffender = currOffender.getName() + ", " + currOffender.getBookingNumber();
    	        addLabel(m_panelBodyActivity, new Label(strOffender), 0, nRow++, 1, 1, GridBagConstraints.NORTHWEST);
    	        Choice cCompliance = new Choice();
    	        m_data.populateCompliance(cCompliance, a);
    	        addField(m_panelBodyActivity, cCompliance, 0, nRow++, 1, 1, GridBagConstraints.NORTHWEST);
        	}
        }

		// DEV only - scan code
        m_labelBarcode.setText(m_data.getBarcode());
//        addLabel(m_panelBodyActivity, m_labelBarcode, 0, nRow++, 1, 1, GridBagConstraints.NORTHWEST);
        
        String strFeedback = m_data.getFeedback();
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
        addLabel(m_panelBodyDetail, new Label("Institution: TTCF"), 0, 0, 1, 1, GridBagConstraints.NORTHWEST);
        addLabel(m_panelBodyDetail, new Label("Housing Unit: TTOH"), 0, 1, 1, 1, GridBagConstraints.NORTHWEST);
        addLabel(m_panelBodyDetail, new Label("Location: " + m_data.getLogEntry().getLocationName()), 0, 2, 1, 1, GridBagConstraints.NORTHWEST);
        addLabel(m_panelBodyDetail, new Label("Event: " + m_data.getLogEntry().getActivityName()), 0, 3, 1, 1, GridBagConstraints.NORTHWEST);

    	drawButtons(m_panelBodyDetail, 4);
    	addBodyPanel(m_panelBodyDetail, BorderLayout.NORTH);
    }

	protected void drawFindOffenderScreen() {
		m_screenState = SCREEN_STATE_FIND_OFFENDER;

		m_panelBodyActivity.removeAll();
		addBodyPanel(m_panelBodyActivity, BorderLayout.NORTH);

        int nRow = 0;
        
        // Activity
        addLabel(m_panelBodyActivity, new Label("Find Offender"), 0, nRow++, 1, 1, GridBagConstraints.NORTHWEST);
        
        ArrayList arrScannables = m_data.getScannables();
        
        // Render the offender/compliance label/dropdown
        Iterator i = arrScannables.iterator();
        while (i.hasNext()) {
        	Object o = i.next();
        	if (o instanceof Offender) {
        		Offender currOffender = (Offender)o;
        		
        		String strOffender = currOffender.getName() + ", " + currOffender.getBookingNumber();
        		Checkbox cbOffender = new Checkbox(strOffender);
    	        addLabel(m_panelBodyActivity, cbOffender, 0, nRow++, 1, 1, GridBagConstraints.NORTHWEST);
        	}
        }

		// DEV only - scan code
        m_labelBarcode.setText(m_data.getBarcode());
//        addLabel(m_panelBodyActivity, m_labelBarcode, 0, nRow++, 1, 1, GridBagConstraints.NORTHWEST);
        
		drawButtons(null, nRow);
	}
    
    protected void drawHistoryScreen() {
        m_screenState = SCREEN_STATE_HISTORY;
    	
        // Toolbar Panel
    	m_panelBodyHistory.removeAll();
    	
        // Location
        List listHistory = new List();
//	        listHistory.setSize(CN3_X - 10, CN3_Y);
        Dimension minSize = new Dimension(CN3_X - 10, CN3_Y);
//        listHistory.setMinimumSize(minSize);
        listHistory.setSize(minSize);
        m_data.populateHistory(listHistory);
        addLabel(m_panelBodyHistory, new Label("Entries"), 0, 0, 1, 1, GridBagConstraints.NORTHWEST);
        addField(m_panelBodyHistory, listHistory, 0, 1, 1, 1, GridBagConstraints.NORTHWEST);
    	
    	drawButtons(m_panelBodyHistory, 2);
    	addBodyPanel(m_panelBodyHistory, BorderLayout.NORTH);
    }

    private void drawButtons(Panel p, int nRow) {
    	m_panelBottom.removeAll();
    	
        switch (m_screenState) {
	        case SCREEN_STATE_HISTORY:
	        	m_panelBottom.add(m_buttonGoActivity);
	        	m_panelBottom.add(m_buttonGoDetail);
	        	break;
	        case SCREEN_STATE_DETAIL:
	        	m_panelBottom.add(m_buttonGoActivity);
	        	m_panelBottom.add(m_buttonPrev);
	        	m_panelBottom.add(m_buttonNext);
//	        	m_panelBottom.add(m_buttonGoHistory);
	        	break;
	        case SCREEN_STATE_FIND_OFFENDER:
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
    
}

package com.tippingpoint.handheld.ui;

import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.lang.time.DateFormatUtils;

import com.intermec.datacollection.*;
import com.tippingpoint.handheld.data.Activity;
import com.tippingpoint.handheld.data.ComplianceValue;
import com.tippingpoint.handheld.data.DataInterface;
import com.tippingpoint.handheld.data.Location;
import com.tippingpoint.handheld.data.LogEntry;
import com.tippingpoint.handheld.data.Offender;
import com.tippingpoint.handheld.data.Scannable;
import com.tippingpoint.handheld.data.Staff;
import com.tippingpoint.handheld.data.Util;

public class ScreenListeners extends Screen implements BarcodeReadListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3820130245162337695L;
	
	private long lCurrFreeMemory = 0;  // These 2 variables track memory between login scans
	private long lPriorFreeMemory = 0;

	ScreenListeners(boolean bIsRunningOnHandheld) {
		super();
		
		if (bIsRunningOnHandheld) {
			try
			{
				m_bcRdr = new BarcodeReader();
				m_bcRdr.addBarcodeReadListener(this);
				
				// Starts asynchronous barcode read
				m_bcRdr.threadedRead(true);
			}
			catch (BarcodeReaderException e)
			{
				System.out.println(e);
	//			labelStatus.setText(e.getMessage());
				//*****
				//* Since m_labelStatus was not initialized with text,
				//* doLayout() is required on some platforms in order
				//* to show the new label text for the first setText()
				//* call.
				//*****
	//			doLayout();
			}
		}
		
	}
	
	/**
	 * This method is invoked when the BarcodeReadEvent occurs.
	 */
	public void barcodeRead(BarcodeReadEvent aBarcodeReadEvent)
	{
		/**
		 * Uses EventQueue.invokeLater to ensure the UI update
		 * executes on the AWT event dispatching thread. 
		 */
		final String sNewData = aBarcodeReadEvent.strDataBuffer;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				// Displays the scanned data in the text field
				getData().setBarcode(sNewData);
				respondToScanEvent();
				System.out.println("barcode scanned: " + sNewData);
				
				if (m_screenState == SCREEN_STATE_FIND_OFFENDER)
					drawFindOffenderScreen();
				else
					drawActivityScreen();
					
                setVisible(true);
			}
		});
	}
	
    public ItemListener getActivityChoiceListener() {
    	return m_choiceListenerActivity;
    }

    public ActionListener getGoActivityButtonListener() {
    	return m_buttonListenerGoActivity;
    }

    public ActionListener getGoDetailButtonListener() {
    	return m_buttonListenerGoDetail;
    }

    public ActionListener getGoOffenderReplacementListener() {
    	return m_buttonListenerGoOffenderReplacement;
    }
    
    public ActionListener getQuitButtonListener() {
    	return m_buttonListenerQuit;
    }
    
    public ActionListener getRecordButtonListener() {
    	return m_buttonListenerRecord;
    }
    
    public ActionListener getScanIndicatorButtonListener() {
    	return m_buttonListenerScanIndicator;
    }

    protected Activity findActivity(String strActivity) {
    	return Util.findActivity(getData(), strActivity);
    }
    
    protected void setupListeners() {
    	super.setupListeners();
    	
        m_buttonListenerGoActivity = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                String action = ae.getActionCommand();
                
                if (action.equals(BUTTON_ACTIVITY)) {
                    drawActivityScreen();
                    setVisible(true);
                } 
                else {
                    System.out.println(action);
                }
            }
        };

        m_buttonListenerGoDetail = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                String action = ae.getActionCommand();
                
                if (action.equals(BUTTON_DETAIL)) {
System.out.println("Detail Button - not implemented yet");
// Must first provide a way to store a list/map of log objects
//                    drawDetailScreen();
                    setVisible(true);
                } 
                else {
                    System.out.println(action);
                }
            }
        };

        m_buttonListenerGoOffenderReplacement = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                drawFindOffenderScreen();
                setVisible(true);
            }
        };

        m_buttonListenerQuit = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                String action = ae.getActionCommand();
                
                if (action.equals(BUTTON_QUIT)) {
            		if (m_bcRdr != null)
            			m_bcRdr.dispose(); // Release system resources used by BarcodeReader
            		setVisible(false);
            		dispose(); // Dispose the frame
            		System.exit(0);
                } 
                else {
                    System.out.println(action);
                }
            }
        };
        
        m_buttonListenerScanIndicator = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                System.out.println("Scan Indicator pressed");
                drawFindOffenderScreen();
                setVisible(true);
            }
        };
        
        m_buttonListenerRecord = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                String action = ae.getActionCommand();
                
                if (action.equals(BUTTON_RECORD)) {
                	addLogEntry();
                } 
                else {
                    System.out.println(action);
                }
            }
        };
        
        m_choiceListenerActivity = new ItemListener() {
            public void itemStateChanged(ItemEvent ie) {
            	getData().reset();
            	drawActivityScreen();
            	setVisible(true);
            }
        };
        
    }

    protected void respondToScanEvent(String strBarcode) {
    	// If a user is not logged in, the data object needs to load/parse
    	// the XML file from the server.
    	if (getData().getLoggedInStaff() == null) {
    		try {
    			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    			getData().parse();
    		}
    		finally {
    			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    		}
    	}
    	
		String strSelection = m_choiceActivity.getSelectedItem();
		Activity a = findActivity(strSelection); 
		Staff staff = getData().getStaffByBarcode(strBarcode);
		
		// a staff person is logging in
		if (staff != null)
			processStaffBarcodeScan(staff);
			
		// a staff scanned something
		else
			getData().populateScannables(strBarcode, a);
    }

    private void addLogEntry() {
		LogEntry logEntry = Util.addLogEntry(getData(), m_choiceActivity.getSelectedItem());
    	
		// show detail briefly
		drawDetailScreen();
		setVisible(true);
		
		
		try {
			Thread.sleep(2000);
		}
		catch (Exception e) {
			// eat it
		}

		logEntry.clear();
		getData().reset();
		
		// go back to the activity screen for another scan
        drawActivityScreen();
        setVisible(true);
    }
    
    private void prepareToDock() {
    	try {
        	DataInterface d = getData();
        	
        	lPriorFreeMemory = lCurrFreeMemory;
        	
        	// TODO: do we need to verify that the badge scanning out is the same as the one that scanned in?
        	d.clearLoggedInStaff();
			d.getLogEntry().close();
        	d.clear();

			drawDockScreen(true);
			setVisible(true);
	        Runtime.getRuntime().gc();
			Long l = new Long(Runtime.getRuntime().freeMemory());
			setTitle(l.toString());
		} 
		catch (IOException e) {
			Screen.logError(e);
		}
    }
    
    private void processStaffBarcodeScan(Staff staff) {
    	if (getData().getLoggedInStaff() != null) {
			System.out.println("logging out: " + staff.getEmail());
    		prepareToDock();
    	}
    	else {
			System.out.println("logging in: " + staff.getEmail());
    		signIn(staff);
    	}
    }
    
    private void respondToScanEvent() {
		String strBarcode = getData().getBarcode();
		respondToScanEvent(strBarcode);
    }
    
    private void signIn(Staff staff) {
        System.out.println("Start pressed");
        // TODO: create a new BUSY... screen to display
        
        try {
        	DataInterface d = getData();
        	d.setLoggedInStaff(staff);
        	
        	// Create new XML file - this file gets
        	// posted to the server
			d.getLogEntry().initialize();
		} 
        catch (IOException e) {
			Screen.logError(e);
		}
		refreshActivityList();
        drawActivityScreen();
        setVisible(true);
        lCurrFreeMemory = Runtime.getRuntime().freeMemory();
        Long lPrior = new Long(lPriorFreeMemory);
        Long lCurr = new Long(lCurrFreeMemory);
        setTitle(lPrior.toString() + "-" + lCurr.toString());
    }
}

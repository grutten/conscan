package com.tippingpoint.handheld.ui;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import com.intermec.datacollection.*;
import com.tippingpoint.handheld.data.Activity;
import com.tippingpoint.handheld.data.ComplianceValue;
import com.tippingpoint.handheld.data.Location;
import com.tippingpoint.handheld.data.LogEntry;
import com.tippingpoint.handheld.data.Offender;

public class ScreenListeners extends Screen implements BarcodeReadListener {

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
				m_data.setBarcode(sNewData);
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

    public ActionListener getExitButtonListener() {
    	return m_buttonListenerExit;
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
    
    public ActionListener getRecordButtonListener() {
    	return m_buttonListenerRecord;
    }
    
    public ActionListener getScanIndicatorButtonListener() {
    	return m_buttonListenerScanIndicator;
    }
    
    protected Activity findActivity(String strActivity) {
    	Iterator iActivities = m_data.getActivities().iterator();
    	Activity searchResult = null;
    	while (searchResult == null && iActivities.hasNext()) {
    		Activity currentItem = (Activity)iActivities.next();
    		if (currentItem != null)
    			if(currentItem.getName().equalsIgnoreCase(strActivity))
    				searchResult = currentItem;
    	}
    	
    	return searchResult;
    }
    
    protected void setupListeners() {
    	super.setupListeners();
    	
        m_buttonListenerGoActivity = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                String action = ae.getActionCommand();
                
                if (action.equals(BUTTON_ACTIVITY)) {
                    System.out.println("Activity pressed");
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
                    System.out.println("Detail pressed");
                    drawDetailScreen();
                    setVisible(true);
                } 
                else {
                    System.out.println(action);
                }
            }
        };

        m_buttonListenerGoOffenderReplacement = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                System.out.println("Offender Replacement pressed");
                drawFindOffenderScreen();
                setVisible(true);
            }
        };
        
        m_buttonListenerExit = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                String action = ae.getActionCommand();
                
                if (action.equals(BUTTON_EXIT)) {
                    System.out.println("Exit pressed");
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
            	m_data.reset();
            	drawActivityScreen();
            	setVisible(true);
            }
        };
        
    }

    protected void respondToScanEvent(String strBarcode) {
		String strSelection = m_choiceActivity.getSelectedItem();
		Activity a = findActivity(strSelection); 
		
		m_data.populateScannables(strBarcode, a);
    }

    private void addLogEntry() {
        System.out.println("Record pressed");
		ArrayList arrScannables = m_data.getScannables();

		// persist
		String strSelection = m_choiceActivity.getSelectedItem();
		Activity a = findActivity(strSelection); 
        Iterator i = arrScannables.iterator();
        Date d = new Date();
        LogEntry logEntry = m_data.getLogEntry();
        logEntry.setDateCreated(d.toString());
        logEntry.setActivity(a);
        while (i.hasNext()) {
        	// TODO: this may need to persist 2 offenders for the detail screen.
        	// What does that mean for the detail screen?  Does it matter what
        	// order offenders are persisted when there are 2 or more offenders
        	// in one cell?  Probably not, but is something to think about.
        	
        	// Write: a) offender b) cell + offender c) cell
        	Object obj = i.next();
    		Object objCompliance = m_choiceCompliance1.getSelectedItemObject();
    		
    		if (objCompliance instanceof ComplianceValue) {
    			ComplianceValue cv = (ComplianceValue)objCompliance;
    			logEntry.setComplianceValue(cv);
    		}
    		
        	if (obj instanceof Offender) {
        		Offender offender = (Offender)obj;
        		logEntry.setOffender(offender);
        		
        		// TODO: I think this is the scope where the scan==cell && compliance==offender
        		// lands here, so the location is never set in the LogEntry object.
        	}
        	else if (obj instanceof Location) {
//        		Location l = m_data.getLocationByOffendersBarcode(((Offender)obj).getBarcode());
        		Location location = (Location)obj;
    			logEntry.setLocation(location);

        		if (a.isOffenderCompliance()) {
    				Iterator iOffenders = location.getOffenders().iterator();
    				while (iOffenders.hasNext()) {
    					Offender offender = (Offender)iOffenders.next();
    					logEntry.setOffender(offender);
    				}
        		}
        	}
        }
		
		// show detail briefly
		drawDetailScreen();
		setVisible(true);
		
		// Persist the entry
		try {
			logEntry.write();
		}
		catch (Exception e) {
			// TODO: eat exception for now, but what should it do for good?
		}
		
		try {
			Thread.currentThread().sleep(250);
		}
		catch (Exception e) {
			// eat it
		}

		logEntry.clean();
        m_data.reset();
		
		// go back to the activity screen for another scan
        drawActivityScreen();
        setVisible(true);
    }
    
    private void respondToScanEvent() {
		String strBarcode = m_data.getBarcode();
		respondToScanEvent(strBarcode);
    }
    
}

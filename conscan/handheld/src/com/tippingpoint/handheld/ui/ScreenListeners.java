package com.tippingpoint.handheld.ui;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import com.intermec.datacollection.*;
import com.tippingpoint.handheld.data.Activity;
import com.tippingpoint.handheld.data.ComplianceValue;
import com.tippingpoint.handheld.data.DataInterface;
import com.tippingpoint.handheld.data.Location;
import com.tippingpoint.handheld.data.LogEntry;
import com.tippingpoint.handheld.data.Offender;
import com.tippingpoint.handheld.data.Scannable;

public class ScreenListeners extends Screen implements BarcodeReadListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3820130245162337695L;

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

    public ActionListener getDockButtonListener() {
    	return m_buttonListenerDock;
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

    public ActionListener getStartButtonListener() {
    	return m_buttonListenerStart;
    }
    
    protected Activity findActivity(String strActivity) {
    	Iterator iActivities = getData().getActivities().iterator();
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
        
        m_buttonListenerDock = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                String action = ae.getActionCommand();
                
                if (action.equals(BUTTON_DOCK)) {
//            		if (m_bcRdr != null)
//            			m_bcRdr.dispose(); // Release system resources used by BarcodeReader
//            		setVisible(false);
//            		dispose(); // Dispose the frame
//            		System.exit(0);
            		try {
                    	DataInterface d = getData();
						d.getLogEntry().close();
                    	d.clear();

						drawDockScreen("Scanner ready to be DOCKED", true);
						setVisible(true);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                    
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
        
        m_buttonListenerStart = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                System.out.println("Start pressed");
                drawDockScreen("loading...");
                
                try {
                	// Re-parse the XML from the server
                	DataInterface d = getData();
                	d.parse();
                	
                	// Create new XML file - this file gets
                	// posted to the server
					d.getLogEntry().initialize();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				refreshActivityList();
                drawActivityScreen();
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
		String strSelection = m_choiceActivity.getSelectedItem();
		Activity a = findActivity(strSelection); 
		
		getData().populateScannables(strBarcode, a);
    }

    private void addLogEntry() {
		ArrayList arrScannables = getData().getScannables();

		// persist
		String strSelection = m_choiceActivity.getSelectedItem();
		Activity a = findActivity(strSelection); 
        Iterator i = arrScannables.iterator();
        LogEntry logEntry = getData().getLogEntry();
        logEntry.setActivity(a);
        
		try {  // wraps the write of the log entry
	        while (i.hasNext()) {
	            Date d = new Date();
	            logEntry.setDateCreated(d.toString());
	            
	        	// TODO: this may need to persist 2 offenders for the detail screen.
	        	// What does that mean for the detail screen?  Does it matter what
	        	// order offenders are persisted when there are 2 or more offenders
	        	// in one cell?  Probably not, but is something to think about.
	        	
	        	// Write: a) offender b) cell + offender c) cell
	        	Object obj = i.next();
	        	Scannable scannable = null;
	        	
	        	if (obj instanceof Scannable)
	        		scannable = (Scannable) obj;
	        	
	        	// TODO: handle a null scannable.  Figure out a graceful
	        	// way to display a useable state on the handheld.  Is
	        	// a null scannable something that is expected to happen?
	        	// Or is it realy a bug and should never happen?
	        	
	    		Object objCompliance = scannable.getComplianceControl().getSelectedItemObject();
	    		
	    		if (objCompliance instanceof ComplianceValue) {
	    			ComplianceValue cv = (ComplianceValue)objCompliance;
	    			logEntry.setComplianceValue(cv);
	    		}
	    		
	        	if (scannable.getObject() instanceof Offender) {
	        		Offender offender = (Offender)scannable.getObject();
	        		logEntry.setOffender(offender);
	        		
	        		// TODO: I think this is the scope where the scan==cell && compliance==offender
	        		// lands here, so the location is never set in the LogEntry object.
	        	}
	        	else if (scannable.getObject() instanceof Location) {
	        		Location location = (Location)scannable.getObject();
	    			logEntry.setLocation(location);
	
	        		if (a.isOffenderCompliance()) {
	        	        System.out.println("addLogEntry(): UNEXPECTED CONDITION");
	        		}
	        	}
	        	else
	    	        System.out.println("scannable object is not an offender or a location ");
	        		
				logEntry.write();
	        }
		}
		catch (Exception e) {
			// TODO: eat exception for now, but what should it do for good?
		}
		
		// show detail briefly
		drawDetailScreen();
		setVisible(true);
		
		
		try {
			Thread.sleep(250);
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
    
    private void respondToScanEvent() {
		String strBarcode = getData().getBarcode();
		respondToScanEvent(strBarcode);
    }
    
}

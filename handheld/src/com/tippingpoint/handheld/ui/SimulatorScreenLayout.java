package com.tippingpoint.handheld.ui;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.List;
import java.awt.Panel;
import java.awt.event.ActionEvent;
//import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.tippingpoint.handheld.data.DataInterface;

public class SimulatorScreenLayout extends ScreenLayout {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 149027602440647456L;

	protected List m_listBarcode;
	
    protected ActionListener m_buttonListenerGoScan;
    
    private static String BUTTON_SCAN = "Scan Barcode";
    
    SimulatorScreenLayout(DataInterface d) { 
    	super(d, false);
    	
    	m_panelWidthX = CN3_X + 100;
    	m_panelHeightY = CN3_Y + 280;
    	
    	m_listBarcode = new List();
    }
	
	public void draw() {
		super.draw();
		
        Color cCN3greyBackground = new Color(75, 75, 75);
		
		Panel p = new Panel();
		p.setLayout(new GridBagLayout());
        p.setBackground(cCN3greyBackground);
        p.setSize(this.m_panelWidthX, 200);
        m_panelBottom.add(p, BorderLayout.SOUTH);
		
        int nRow = 0;
     
        m_listBarcode.add("(rosewood)073854016336");
        m_listBarcode.add("(foley)073854016337");
        m_listBarcode.add("(irving)0832924005201");
        m_listBarcode.add("(iverson)0812122010450");
        m_listBarcode.add("(page)0812122010160");
        m_listBarcode.add("(cell-1)0070718001170");
        m_listBarcode.add("(cell-2)0052100746043");
        addLabel(p, new Label("BARCODE"), 0, nRow++, 1, 1, GridBagConstraints.NORTHWEST);
        addField(p, m_listBarcode, 0, nRow++, 1, 1, GridBagConstraints.NORTHWEST);
        
        Button btnScan = new Button(BUTTON_SCAN);
        addField(p, btnScan, 0, nRow++, 1, 1, GridBagConstraints.NORTHWEST);
        
        addListeners();
        btnScan.addActionListener(m_buttonListenerGoScan);
        
        setVisible(true);
	}
	

	private void addListeners() {
        m_buttonListenerGoScan = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                String action = ae.getActionCommand();
                
                if (action.equals(BUTTON_SCAN)) {
            		String strSelection = m_listBarcode.getSelectedItem();
            		int nBarcodeStart = strSelection.indexOf(")") + 1;
            		String strBarcode = strSelection.substring(nBarcodeStart);
            		respondToScanEvent(strBarcode);
            		
    				if (m_screenState == SCREEN_STATE_FIND_OFFENDER)
    					drawFindOffenderScreen();
    				else if (m_screenState == SCREEN_STATE_ACTIVITY)
    					// Why doesn't ScreenListeners.barcodeRead() have to check
    					// the screen state?  These 2 draw() invocations were
    					// copied from there.
    					drawActivityScreen();
    					
                    setVisible(true);
                } 
                else {
                    System.out.println(action);
                }
            }
        };
		
	}
	
	
}

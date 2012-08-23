package com.tippingpoint.handheld.ui;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Choice;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
//import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.tippingpoint.handheld.data.DataInterface;

public class Simulator extends ScreenLayout {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 149027602440647456L;

	protected Choice m_choiceBarcode;
	protected Choice m_choiceLocation;
	protected Choice m_choiceOffender;
	
    protected ActionListener m_buttonListenerGoScan;
    
    private static String BUTTON_SCAN = "Scan Barcode";
    
    Simulator(DataInterface d) { 
    	super(d, false);
    	
    	m_panelWidthX = CN3_X + 200;
    	m_panelHeightY = CN3_Y + 200;
    	
    	m_choiceBarcode = new Choice();
    }
	
    public void drawSecurityCheck() {
    	respondToScanEvent("0070718001170");
    	drawActivityScreen();
    	setVisible(true);
    }
    
    public void drawCellSearch() {
    	m_choiceActivity.select(1);
    	respondToScanEvent("0070718001170");
    	drawActivityScreen();
    	setVisible(true);
    	
    }
    
	public void draw() {
		super.draw();
		
        Color cBackground = new Color(225, 225, 225);
		
		Panel p = new Panel();
		p.setLayout(new GridBagLayout());
        p.setBackground(cBackground);
        p.setSize(this.m_panelWidthX, 200);
		add(p, BorderLayout.CENTER);
		
		
		
        int nRow = 0;
     
        m_choiceBarcode.add("A - security check");
        m_choiceBarcode.add("A - bedding exchange");
        m_choiceBarcode.add("A - showers");
        m_choiceBarcode.add("C - T162-A-001L");
        m_choiceBarcode.add("C - T162-A-002L");
        m_choiceBarcode.add("O - Irving, 234567890");
        m_choiceBarcode.add("O - Page, 123456789");
        addLabel(p, new Label("BARCODE"), 0, nRow++, 1, 1, GridBagConstraints.NORTHWEST);
        addField(p, m_choiceBarcode, 0, nRow++, 1, 1, GridBagConstraints.NORTHWEST);
        
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
            		String strSelection = m_choiceBarcode.getSelectedItem();
            		switch(strSelection.charAt(0)) {
	            		case 'A':
	                        System.out.println("Scan: Activity");
	                        m_choiceActivity.select(strSelection.substring(4));
	                        
	                        // the following lines simulate firing a change event
	                    	drawActivityScreen();
	                    	setVisible(true);
	            			break;
	            		case 'C':
	                        System.out.println("Scan: Cell");
	                        m_choiceLocation.select(strSelection.substring(4));
	            			break;
	            		case 'O': 
	                        System.out.println("Scan: Offender");
	                        m_choiceOffender.select(strSelection.substring(4));
	            			break;
            		
            		}
                } 
                else {
                    System.out.println(action);
                }
            }
        };
		
	}
	
	
}

package com.tippingpoint.handheld.ui;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import com.intermec.datacollection.BarcodeReader;
import com.tippingpoint.handheld.data.Data;
import com.tippingpoint.version.HandheldVersion;

public class Screen extends Frame {
	static final long serialVersionUID = -1;
	
	// Handheld dimensions
    protected static int CN3_X = 240;
    protected static int CN3_Y = 320;

    // Scanner
	protected BarcodeReader m_bcRdr;
	protected boolean m_bRunningOnHandheld = true;
    
    // Panels
    protected Panel m_panelBodyActivity;
    protected Panel m_panelBodyDetail;
    protected Panel m_panelBodyHistory;
    protected Panel m_panelBottom;
    protected Component m_panelCurrentBody;

    // Button names
    static final String BUTTON_ACTIVITY = "Activity";
    static final String BUTTON_DETAIL = "Detail";
    static final String BUTTON_EXIT = "Exit";
    static final String BUTTON_HISTORY = "History";
    static final String BUTTON_NEXT = "Next >>";
    static final String BUTTON_PREV = "<< Prev";
    static final String BUTTON_RECORD = "Record";
    static final String BUTTON_SCANINDICATOR = "Scan Offender";
    
    // Special string values
    static final String CHOICE_UNSELECTED = "--";
    
    // Controls
    protected Button m_buttonExit;
    protected Button m_buttonGoActivity;
    protected Button m_buttonGoDetail;
    protected Button m_buttonGoHistory;
    protected Button m_buttonRecord;
    protected Button m_buttonScanIndicator;
    protected Choice m_choiceActivity = new Choice();  // instantiate here so that selection will persist
    protected Choice m_choiceLocation = new Choice();
    protected Choice m_choiceOffender = new Choice();
    protected Label m_labelBarcode = new Label("<scan>");
    protected Label m_labelFeedback = new Label("");

    // Detail ONLY buttons
    protected Button m_buttonPrev;
    protected Button m_buttonNext;
    
    // Listeners
    protected ActionListener m_buttonListenerExit;
    protected ActionListener m_buttonListenerGoActivity;
    protected ActionListener m_buttonListenerGoDetail;
    protected ActionListener m_buttonListenerGoHistory;
    protected ActionListener m_buttonListenerRecord;
    protected ActionListener m_buttonListenerScanIndicator;
    protected ItemListener m_choiceListenerActivity;
    
    // State
    static final int SCREEN_STATE_ACTIVITY = 1;
    static final int SCREEN_STATE_HISTORY = 2;
    static final int SCREEN_STATE_DETAIL = 3;
    static final int SCREEN_STATE_FIND_OFFENDER = 4;
    
    protected int m_screenState;

    // provides access to data configuration
    protected Data m_data;
    
	Screen() {
    	super("TPSS - MockUp v " + HandheldVersion.VERSIONSTRING);
    	
    	m_screenState = SCREEN_STATE_ACTIVITY;
    	
    	setupListeners();
	}
	
    public void draw() {
    	setLayout(new BorderLayout());
        Color cBackground = new Color(206, 231, 255); // cee7ff
        setBackground(cBackground);
        setSize(CN3_X, CN3_Y);
        
//        addFooterComponents();
    }

    protected void addBodyPanel(Component comp, Object constraints) {
    	
    	// Remove the last screen from the display
    	if (m_panelCurrentBody != null)
    		remove(m_panelCurrentBody);
    		
        add(comp, constraints);
        m_panelCurrentBody = comp;
    }
    
    protected void addField(Panel p, Component c, int x, int y, 
    		int width, int height, int align) {
    	addItem(p, c, x, y, width, height, 0, 15, 6, 15, align);
    }
    protected void addLabel(Panel p, Component c, int x, int y, 
    		int width, int height, int align) {
    	addItem(p, c, x, y, width, height, 0, 15, -1, 15, align);
    }
    
    protected void addItem(Panel p, Component c, int x, int y, 
    		int width, int height, int padTop, int padLeft, 
    		int padBottom, int padRight, int align) {
	    GridBagConstraints gc = new GridBagConstraints();
	    gc.gridx = x;
	    gc.gridy = y;
	    gc.gridwidth = width;
	    gc.gridheight = height;
	    gc.weightx = 100.0;
	    gc.weighty = 100.0;
	    gc.insets = new Insets(padTop, padLeft, padBottom, padRight);
	    gc.anchor = align;
	    gc.fill = GridBagConstraints.HORIZONTAL;
	    p.add(c, gc);
    }
    protected void drawActivityScreen() { /**/ }
    protected void drawDetailScreen()  { /**/ }
    protected void drawFindOffenderScreen() { /**/ }
    protected void drawHistoryScreen() { /**/ }
    
    protected boolean getIsRunningOnHandheld() { return m_bRunningOnHandheld; }
        
    protected void setupListeners() {
        addWindowListener(
                new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);
                    }
                }
        );
    }
    
}
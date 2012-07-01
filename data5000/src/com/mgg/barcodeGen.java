package com.mgg;

public class barcodeGen {
    public static void main(String args[]) {
    	// <column name="barcode">1111111126087</column>
    	for (int i = 0; i <502; ++i)
    		System.out.println("<column name='barcode'>0200000000" + Integer.valueOf(i).toString() + "</column>");
    }

}




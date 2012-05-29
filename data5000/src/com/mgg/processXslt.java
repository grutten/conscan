package com.mgg;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class processXslt {
    public static void main(String args[]) {
        System.out.println("start");
        try {
//            Source inputFile = new StreamSource(new FileInputStream("Offender_gen.xml"));        
//            Source xsltFile = new StreamSource(new FileInputStream("OffenderLoc_xslt.xml"));
//          Source xsltFile = new StreamSource(new FileInputStream("Offender_xslt.xml"));

            Source inputFile = new StreamSource(new FileInputStream("Staff_gen.xml"));        
            Source xsltFile = new StreamSource(new FileInputStream("Staff_xslt.xml"));
            
            
            Result result = new StreamResult(new FileOutputStream("output.xml"));
//            String strXsl = getTransformationString("MessageID");
//            Source xsltSource = new StreamSource(new StringReader(strXsl));
//            Transformer xformer = TransformerFactory.newInstance().newTransformer(xsltSource);
            Transformer xformer = TransformerFactory.newInstance().newTransformer(xsltFile);
            
            xformer.transform(inputFile, result);     
        }
        catch (Exception e) {
            System.out.println("EXCEPTION");
            e.printStackTrace();
        }
        System.out.println("finished");
    }
    
    
    private static String getTransformationString(String strTagToMatch) {
        StringBuffer buf = new StringBuffer();
        
        buf.append("<?xml version='1.0'?>");
        buf.append("<xsl:stylesheet version='1.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>");

        buf.append("<xsl:template match='@*|node()'>");
        buf.append("    <xsl:copy>");
        buf.append("        <xsl:apply-templates select='@*|node()'/>");
        buf.append("    </xsl:copy>");
        buf.append("</xsl:template>");
        buf.append("<xsl:template match='" + strTagToMatch + "'>");
        buf.append("    <xsl:copy>");
        buf.append("        <xsl:attribute name='asdf' namespace='urn:ebay:apis:eBLBaseComponents' />");
        buf.append("        <xsl:apply-templates select='@*|node()'/>");
        buf.append("    </xsl:copy>");
        buf.append("</xsl:template>");
        
        buf.append("</xsl:stylesheet>");
        
        return buf.toString();
    }    
}

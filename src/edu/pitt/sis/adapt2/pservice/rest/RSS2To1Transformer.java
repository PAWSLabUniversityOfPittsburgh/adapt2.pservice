package edu.pitt.sis.adapt2.pservice.rest;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * Servlet implementation class for Servlet: RSS2To1Transformer
 *
 */
 public class RSS2To1Transformer extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet
 {
	static final long serialVersionUID = -2L;
	
	public static final String REQ_RSS2_FEED_URL = "rss2";
	public static final String XSLT_TRANSFORM_FILENAME_PREFIX = "/WEB-INF/";
	public static final String XSLT_TRANSFORM_FILENAME = "xslt_filename";
	
	private String xslt_filename;
	
	public void init(ServletConfig conf) throws ServletException
	{
		super.init(conf);
		ServletContext context = getServletContext();
		xslt_filename = context.getRealPath( XSLT_TRANSFORM_FILENAME_PREFIX +  context.getInitParameter(XSLT_TRANSFORM_FILENAME) );
	}
	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#service(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		String default_result = "<?xml version='1.0' encoding='utf-8'?>\n" +
			"<rdf:RDF\n" + 
			"	xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'\n" + 
			"	xmlns='http://purl.org/rss/1.0/'\n" + 
			">\n" + 
			"</rdf:RDF>";
					
		String req_rss2_feed_url = request.getParameter(REQ_RSS2_FEED_URL);
		PrintWriter out = response.getWriter();
		try 
		{
			URL url = new URL( req_rss2_feed_url );
			TransformerFactory tfactory = TransformerFactory.newInstance();
			Transformer transformer = tfactory.newTransformer(new StreamSource(xslt_filename));
			transformer.transform(new StreamSource( url.toString() ), new StreamResult(out));
		}
		catch(MalformedURLException mue)
		{
			System.out.println("RSS2To1Transformer MalformedURLException for url=" + req_rss2_feed_url);
			out.println(default_result);
			out.close();
		}
		catch(TransformerConfigurationException tce)
		{
			System.out.println("RSS2To1Transformer TransformerConfigurationException for file=" + xslt_filename);
			out.println(default_result);
			out.close();
		}
		catch(TransformerException te)
		{
			System.out.println("RSS2To1Transformer TransformerException for file=" + xslt_filename);
			out.println(default_result);
			out.close();
		}
		
	}  	
	
}
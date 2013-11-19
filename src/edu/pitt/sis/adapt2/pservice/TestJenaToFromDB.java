package edu.pitt.sis.adapt2.pservice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.db.ModelRDB;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Seq;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.DCTypes;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RSS;

import edu.pitt.sis.paws.core.Item2;
import edu.pitt.sis.paws.core.Item2Vector;

class IS12FolderItem
{
	public URI uri;
	public int order_rank;
	public Item2Vector<IS12Activity> activities;
	
	public IS12FolderItem(URI _uri, int _order_rank)
	{
		uri = _uri;
		order_rank = _order_rank;
		activities = new Item2Vector<IS12Activity>();
	}
}

class IS12Activity extends Item2
{
	Item2Vector<Item2> concepts;
	Item2Vector<Item2> concepts_in;
	Item2Vector<Item2> concepts_out;
	public IS12Activity(int _id, String _title, String _uri)
	{
		super(_id, _title, _uri);
		concepts = new Item2Vector<Item2>();
		concepts_in = new Item2Vector<Item2>();
		concepts_out = new Item2Vector<Item2>();
	}

}

public class TestJenaToFromDB
{
	// ttl, c (count), ttl64c1
	public static void main(String[] args)
	{
		String ip = "136.142.118.240";
		String pingResult = "";

		String pingCmd = "ping -m 64 -c 1 " + ip;

		try
		{
			Runtime r = Runtime.getRuntime();
			Process p = r.exec(pingCmd);
	
			BufferedReader in = new BufferedReader(new
			InputStreamReader(p.getInputStream()));
			String inputLine;
			int count = 0;
			while ((inputLine = in.readLine()) != null && count <2)
			{
				System.out.println(inputLine);
				pingResult += inputLine;
				count ++;
			}
			in.close();

		}//try
			catch (IOException e) {
			System.out.println(e);
		}
		int time_st = pingResult.indexOf("time=") + 5;
		int time_fi = pingResult.indexOf(" ms", time_st);
		float time = Float.parseFloat(pingResult.substring(time_st, time_fi));
		System.out.println(">> " + (long)((float)time*1000000));
		
		try
		{
			URL _url = new URL("http://scythian.exp.sis.pitt.edu:8080/pservice/service/adapt2-topic-qg/invoke/adapt2-topic-qg");
			
			System.out.println(InetAddress.getByName(_url.getHost()).getHostAddress()); 
		}
		catch(UnknownHostException uhe) { uhe.printStackTrace(System.out); }
		catch(MalformedURLException mue) { mue.printStackTrace(System.out); }
	}
	
	public static void main_old2(String[] args)
	{
		for(int i=0; i<10; i++)
		{
			long _ms = System.currentTimeMillis();
			long _ns = System.nanoTime();
			System.out.println("Time ms: " + _ms + "  ns: " + _ns);
		}
	}
	
	/** Test-run prerequisite/outcome data exploration
	 * @param args
	 */
	public static void old_main1(String[] args)
	{
		String kt_is12_rdf_uri = "http://localhost:8080/kt/rest/ktree887";
		String resource_uri_prefix = "http://adapt2.sis.pitt.edu/webex/webex.rdf";
		String cumulate_rep_manager_a2c = "http://localhost:8080/cbum/ReportManager";
		InputStream in = null;
		// Open IS12 Folder
		long start_global = System.currentTimeMillis();		
		long start = System.currentTimeMillis();		
		Model model_is12_1 = ModelFactory.createDefaultModel();
		try
		{
			in = FileManager.get().open( kt_is12_rdf_uri );
			if (in == null)
			{
				throw new IllegalArgumentException("File: " + kt_is12_rdf_uri + " not found");
			}
			model_is12_1. read(in, "");
			in.close();
		}
		catch(IOException ioe) { ioe.printStackTrace(System.out); }
		long finish = System.currentTimeMillis();	
		System.out.println("WebEx RDF model of size " + model_is12_1.size() + " opened in " + (finish - start) + "ms");
		
		// Explore folders in-depth filtering our necessary URIs
		ArrayList<IS12FolderItem> folders = new ArrayList<IS12FolderItem>();
		Item2Vector<IS12Activity> activities = new Item2Vector<IS12Activity>();
		
		start = System.currentTimeMillis();
		try
		{
			int folder_counter = 0;
			ResIterator channels = model_is12_1.listSubjectsWithProperty(RDF.type, RSS.channel);
			while (channels.hasNext())
			{// for all channels
				Resource channel = channels.nextResource();
				if (channel.hasProperty(RSS.items))
				{// channel has items
					Seq items_seq = channel.getProperty(RSS.items).getSeq();
					NodeIterator is12_item_iter = items_seq.iterator();
					while (is12_item_iter.hasNext())
					{// for all items
						Resource folder_resource = (Resource)is12_item_iter.next();
						StmtIterator is12_item_type_iter = model_is12_1.listStatements(folder_resource, RDF.type, DCTypes.Collection); 
						if(is12_item_type_iter.hasNext())
						{// it is a folder (Collection)
							IS12FolderItem fi = new IS12FolderItem(new URI(folder_resource.getURI()), ++folder_counter);
							folders.add(fi);
							
							// get folder's model
							Model folder_model = ModelFactory.createDefaultModel();
							in = FileManager.get().open( fi.uri.toString() );
							folder_model.read(in, "");
							in.close();
							
							ResIterator sub_iter = folder_model.listSubjectsWithProperty(RDF.type, RSS.item);
							while(sub_iter.hasNext())
							{// for all folder-model-items
								String folder_model_item_uri = sub_iter.nextResource().getURI();
								if( resource_uri_prefix==null || resource_uri_prefix.length()==0 || 
										(folder_model_item_uri.toString().indexOf(resource_uri_prefix) > -1) ) // filter if needed
								{// filtered folder items
									IS12Activity activity = new IS12Activity(0, "", folder_model_item_uri);
									//System.out.println(activity);
									boolean is_new = activities.addNew(activity);
									if(is_new)
										fi.activities.add(activity);
									else
										fi.activities.add(activities.findByURI(folder_model_item_uri));
								}// end of -- filtered folder items
							}// end of --  folder-model-items
						}// end of -- it is a folder (Collection)
					}// end of -- for all items
				}// end of -- channel has items
			}// end of -- for all channels
		}
		catch(URISyntaxException urise)
		{
			urise.printStackTrace(System.out);
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace(System.out);
		}
		
		// delete empty
		Iterator<IS12FolderItem> f_iter = folders.iterator();
		ArrayList<IS12FolderItem> f_to_del = new ArrayList<IS12FolderItem>();
		for(;f_iter.hasNext();)
		{
			IS12FolderItem _fi = f_iter.next();
//System.out.println("F " + _fi.uri + " a.sz=" + _fi.activities.size());
			if(_fi.activities.size()==0)
				f_to_del.add(_fi);
		}
		folders.removeAll(f_to_del);
		f_to_del.clear();
		f_to_del = null;
		finish = System.currentTimeMillis();	
		System.out.println("Resources explored opened in " + (finish - start) + "ms");
		
		System.out.println("	folders.size=" + folders.size());
		System.out.println("	activities.size=" + activities.size());
		
		// request concepts
//		cumulate_rep_manager_a2c
		String activity_uri_list = "";
		Iterator<IS12Activity> iter = activities.iterator();
		for(;iter.hasNext();)
			activity_uri_list += ((activity_uri_list.length()>0)?",":"") + /*URLEncoder.encode(*/iter.next().getURI().toString()/*, "UTF-8")*/;
		start = System.currentTimeMillis();		
		Model model_is12_2 = ModelFactory.createDefaultModel();
		try
		{
			String params = "typ=act2con&dir=in&frm=rdf&app=3&dom=c_programming&" +
				URLEncoder.encode("act", "UTF-8") + "=" + URLEncoder.encode(activity_uri_list, "UTF-8");
			
	        URLConnection conn = new URL(cumulate_rep_manager_a2c).openConnection();
	        conn.setDoOutput(true);
	        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
	        wr.write(params);
	        wr.flush();
	        // Get the response
			in  = conn.getInputStream();
//			BufferedReader br = new BufferedReader(new InputStreamReader(in));
//			String line = br.readLine();
//			while(line != null)
//			{
//				System.out.println(line);
//				line = br.readLine();
//			}
			model_is12_2. read(in, "");
			in.close();
		}
		catch(UnsupportedEncodingException uee) { uee.printStackTrace(System.out); }
		catch(IOException ioe) { ioe.printStackTrace(System.out); }
		finish = System.currentTimeMillis();	
		System.out.println("CUMULATE Act2Con mapping of " + model_is12_2.size() + " statements obtained in " + (finish - start) + "ms");
		
		// read the activity-concept index
		Item2Vector<Item2> concepts = new Item2Vector<Item2>();

		StmtIterator is12_act2con_iter = model_is12_2.listStatements(null, DC.subject, (RDFNode)null);
		start = System.currentTimeMillis();		
		while (is12_act2con_iter.hasNext())
		{// for all activity-concept
			Statement stmt      = is12_act2con_iter.nextStatement();
		    Resource  subject   = stmt.getSubject(); 
		    RDFNode   object    = stmt.getObject();
		    
		    String act_uri;
		    String con_rdfid;
		    act_uri = subject.toString();
		    con_rdfid = object.toString();
		    
		    Item2 concept = new Item2(0, "", con_rdfid);
		    boolean is_new = concepts.addNew(concept);
		    if(is_new)
		    	concepts.add(concept);
		    else
		    	concept = concepts.findByURI(con_rdfid);
		    
		    IS12Activity activity = activities.findByURI(act_uri);
		    activity.concepts.add(concept);
		}// end of -- for all activity-concept
		finish = System.currentTimeMillis();	
		System.out.println("Act2Con info read in " + (finish - start) + "ms");
		
		// Perform prerequisite-outcome separation
		start = System.currentTimeMillis();		
		f_iter = folders.iterator();
		for(;f_iter.hasNext();)
		{// for all folders
			IS12FolderItem _fi = f_iter.next();
			Iterator<IS12Activity> act_iter = _fi.activities.iterator();
			ArrayList<Item2> _cons_to_learn = new ArrayList<Item2>();
			for(;act_iter.hasNext();)
			{// for all acts
				IS12Activity _act = act_iter.next();
				Iterator<Item2> _con_iter = _act.concepts.iterator();
				for(;_con_iter.hasNext();)
				{// for all cons
					Item2 _con = _con_iter.next();
					if(_con.getId() == 0) // if not learned
					{
						_act.concepts_out.add(_con);
						_cons_to_learn.add(_con);
					}
					else // if learned
						_act.concepts_in.add(_con);
				}// end of -- for all cons
			}// end of -- for all acts
			
			// now mark all learned concepts
			Iterator<Item2> cons_to_learn_iter = _cons_to_learn.iterator();
			for(;cons_to_learn_iter.hasNext();)
				cons_to_learn_iter.next().setId(1);
			_cons_to_learn.clear();
			_cons_to_learn = null;
		}// end of -- for all folders	
		finish = System.currentTimeMillis();	
		System.out.println("Prerequisite-outcome separation done in " + (finish - start) + "ms");

		// Create prerequisite-outcome model
		start = System.currentTimeMillis();		
		Model model_is12_res = ModelFactory.createDefaultModel();
		Property prerequisite = DCTerms.requires;
		Property outcome = DCTerms.replaces;
		Literal rdf_con;
		Iterator<IS12Activity> act_iter = activities.iterator();
		for(;act_iter.hasNext();)
		{
			IS12Activity _act = act_iter.next(); 
			Resource rdf_act = model_is12_res.createResource(_act.getURI());
			// Prerequisites
			Iterator<Item2> _con_iter = _act.concepts_in.iterator();
			for(;_con_iter.hasNext();)
			{// for all prerequisites
				rdf_con = model_is12_res.createLiteral(_con_iter.next().getURI());
				model_is12_res.add(rdf_act, prerequisite, rdf_con);
			}// end of -- for all prerequisites
			// Outcomes
			_con_iter = _act.concepts_out.iterator();
			for(;_con_iter.hasNext();)
			{// for all outcomes
				rdf_con = model_is12_res.createLiteral(_con_iter.next().getURI());
				model_is12_res.add(rdf_act, outcome, rdf_con);
			}// end of -- for all outcomes
		}
		finish = System.currentTimeMillis();
		System.out.println("Prerequisite-outcome model of " + model_is12_res.size() + " RDF triples created in " + (finish - start) + "ms");
		long finish_global = System.currentTimeMillis();
		System.out.println("WHOLE Prerequisite outcome finished in " + (finish_global - start_global) + "ms");
		
//		// output model
//		StringWriter sw = new StringWriter();
//		model_is12_res.write(sw,"RDF/XML-ABBREV");
//		System.out.println(sw.toString());
	}
	
	/** Create/overwrite named model into database
	 * @param args
	 */
	public static void old_main0(String[] args)
	{
		// open RDF model - WebEx
		String webex_rdf_uri = "http://localhost:8080/webex/webex.rdf";
		long start = System.currentTimeMillis();		
		Model model1 = ModelFactory.createDefaultModel();
		try
		{
			InputStream in = FileManager.get().open( webex_rdf_uri );
			if (in == null)
			{
				throw new IllegalArgumentException("File: " + webex_rdf_uri + " not found");
			}
			model1. read(in, "");
			in.close();
		}
		catch(IOException ioe) { ioe.printStackTrace(System.out); }
		long finish = System.currentTimeMillis();	
		System.out.println("WebEx RDF model of size " + model1.size() + " opened in " + (finish - start) + "ms");
		
		String className =	"com.mysql.jdbc.Driver";         // path of driver class
		String DB_URL =		"jdbc:mysql://localhost/jenatest?useUnicode=yes&characterEncoding=utf8";  // URL of database 
		String DB_USER =	"student";                          // database user id
		String DB_PASSWD =	"student";                         // database password
		String DB =			"MySQL"; 
		
		IDBConnection jena_conn = null;
		long size = 0;
		
		// write WebEx model into Jena MySQL database
		start = System.currentTimeMillis();	
		try
		{
			Class.forName (className);                          // Load the Driver
			jena_conn = new DBConnection ( DB_URL, DB_USER, DB_PASSWD, DB );
			ModelMaker maker = ModelFactory.createModelRDBMaker(jena_conn);
			Model model_db = maker.createModel("webex", false); // create or open
			model_db = model_db.removeAll();
			model_db.add(model1);
//			model_db.write(System.out);
			size = model_db.size();
			jena_conn.close();
			model_db.close();
		}
		catch(ClassNotFoundException cnfe) {cnfe.printStackTrace(System.out);}
		catch(SQLException sqle) {sqle.printStackTrace(System.out);}
		catch(Exception e) {e.printStackTrace(System.out);}
		finish = System.currentTimeMillis();
		System.out.println("WebEx model written in " + (finish - start) + "ms");
		
		// re-read WebEx model into Jena MySQL database
		start = System.currentTimeMillis();	
		try
		{
			Class.forName (className);                          // Load the Driver
			jena_conn = new DBConnection ( DB_URL, DB_USER, DB_PASSWD, DB );
			Model model_db = ModelRDB.open(jena_conn,"webex");
			size = model_db.size();
//			model_db.write(System.out);
			jena_conn.close();
		}
		catch(ClassNotFoundException cnfe) {cnfe.printStackTrace(System.out);}
		catch(SQLException sqle) {sqle.printStackTrace(System.out);}
		finish = System.currentTimeMillis();
		System.out.println("WebEx model read of size " + size + " from DB in " + (finish - start) + "ms ");

	}
}


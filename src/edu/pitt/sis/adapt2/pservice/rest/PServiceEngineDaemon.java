/* Disclaimer:
 * 	Java code contained in this file is created as part of educational
 *    research and development. It is intended to be used by researchers of
 *    University of Pittsburgh, School of Information Sciences ONLY.
 *    You assume full responsibility and risk of lossed resulting from compiling
 *    and running this code.
 */
 
/**
 * @author Michael V. Yudelson
 */

package edu.pitt.sis.adapt2.pservice.rest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import org.openrdf.sesame.config.AccessDeniedException;
import org.openrdf.sesame.config.ConfigurationException;
import org.openrdf.sesame.config.UnknownRepositoryException;
import edu.pitt.sis.adapt2.pservice.Configuration;
import edu.pitt.sis.adapt2.pservice.IncorrectParameterSpecificationException;
import edu.pitt.sis.adapt2.pservice.UnrecognizedURITypeException;
import edu.pitt.sis.adapt2.pservice.datamodel.ConfItem;
import edu.pitt.sis.adapt2.pservice.datamodel.PServiceItem;
import edu.pitt.sis.adapt2.pservice.datamodel.VizItem;
import edu.pitt.sis.adapt2.pservice.embed.*;
import edu.pitt.sis.paws.core.ItemVector;
import edu.pitt.sis.paws.core.utils.SQLManager;

public class PServiceEngineDaemon
{
	private static PServiceEngineDaemon instance = new PServiceEngineDaemon();
	
	private ItemVector<PServiceItem> pservice_list;
	private ItemVector<ConfItem> conf_list;
	private ItemVector<VizItem> viz_list;
	private ItemVector<iEmbed> embed_list;
	
//	private static SQLManager sql_manager;
//	private static final String db_context = "java:comp/env/jdbc/main";
	
	private PServiceEngineDaemon()
	{
		long psd_ini_start = System.nanoTime();
		
//		sql_manager = new SQLManager(db_context);
		
		pservice_list = new ItemVector<PServiceItem>();
		conf_list = new ItemVector<ConfItem>();
		viz_list = new ItemVector<VizItem>();
		embed_list = new ItemVector<iEmbed>(); 
		
		SQLManager sqlManager = new SQLManager("java:comp/env/jdbc/main");
		
		// Read Context parameters
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		ArrayList <Connection> al_conn = new ArrayList <Connection>();
		ArrayList <Statement> al_stmt = new ArrayList <Statement>();
		ArrayList <ResultSet> al_rs = new ArrayList <ResultSet>();
		String qry = "";
		try
		{
			conn = sqlManager.getConnection();
			al_conn.add(conn);
			
			// PServices
			qry = "SELECT * FROM ent_pservice;";
			stmt = conn.createStatement();
			rs = stmt.executeQuery(qry);
			while(rs.next())
			{
				pservice_list.add( new PServiceItem(rs.getInt("PserviceID"), rs.getString("rdfID"),
						rs.getString("Name"), rs.getString("Description"), rs.getString("ClassName")) );
			}
			al_stmt.add(stmt);
			al_rs.add(rs);
			
			// Configurations (no links to PService yet)
			qry = "SELECT * FROM ent_config;";
			stmt = conn.createStatement();
			rs = stmt.executeQuery(qry);
			while(rs.next())
			{
				ConfItem ci = new ConfItem(rs.getInt("ConfigID"), rs.getString("rdfID"));
//System.out.println("NEW " + ci);
				conf_list.add( ci );
			}
			al_stmt.add(stmt);
			al_rs.add(rs);
			
			// Visualizers (no links to PService yet)
			qry = "SELECT * FROM ent_visualizer;";
			stmt = conn.createStatement();
			rs = stmt.executeQuery(qry);
			while(rs.next())
			{
				viz_list.add( new VizItem(rs.getInt("VisualizerID"), rs.getString("rdfID"),
						rs.getString("Name"), rs.getString("Description"), rs.getString("ClassName")) );
			}
			al_stmt.add(stmt);
			al_rs.add(rs);
			
			// PService - Configuration links
			qry = "SELECT * FROM rel_pservice_config;";
			stmt = conn.createStatement();
			rs = stmt.executeQuery(qry);
			while(rs.next())
			{
				PServiceItem pi = pservice_list.findById(rs.getInt("PServiceID"));
				ConfItem ci = conf_list.findById(rs.getInt("ConfigID"));
				pi.getConfs().add(ci);
				ci.getPSItems().add(pi);
				
				Configuration conf = new Configuration(ci.getTitle(), pi.getTitle(), sqlManager);
//System.out.println("NEW " + conf);
				ci.setConf(conf);
			}
			al_stmt.add(stmt);
			al_rs.add(rs);
			
			// PService - Visualizer links
			qry = "SELECT * FROM rel_pservice_visualizer;";
			stmt = conn.createStatement();
			rs = stmt.executeQuery(qry);
			while(rs.next())
			{
				PServiceItem pi = pservice_list.findById(rs.getInt("PServiceID"));
				VizItem vi = viz_list.findById(rs.getInt("VisualizerID"));
				pi.getVizs().add(vi);
			}
			al_stmt.add(stmt);
			al_rs.add(rs);
			
			// Embeds
			qry = "SELECT * FROM ent_embed;";
			stmt = conn.createStatement();
			rs = stmt.executeQuery(qry);
			while(rs.next())
			{
				iEmbed emb = new Embed(rs.getInt("EmbedID"), rs.getString("rdfID"), sqlManager);
				embed_list.add(emb); 
			}
			al_stmt.add(stmt);
			al_rs.add(rs);
			
			SQLManager.recycleObjects(al_conn, al_stmt, al_rs);
			
		}
		catch(SQLException sqle) { sqle.printStackTrace(System.out); }
		catch(IncorrectParameterSpecificationException ipse) { ipse.printStackTrace(System.out); }
		catch(InstantiationException ie) { ie.printStackTrace(System.out); }
		catch(UnrecognizedURITypeException uute) { uute.printStackTrace(System.out); }
		catch(IllegalAccessException iae) { iae.printStackTrace(System.out); }
		catch(ConfigurationException ce) { ce.printStackTrace(System.out); }
		catch(AccessDeniedException ade) { ade.printStackTrace(System.out); }
		catch(UnknownRepositoryException ure) { ure.printStackTrace(System.out); }
		catch(URISyntaxException use) { use.printStackTrace(System.out); }
		catch(IOException ioe) { ioe.printStackTrace(System.out); }
		catch(ClassNotFoundException cnfe) { cnfe.printStackTrace(System.out); }

		
		long psd_ini_finish = System.nanoTime();
		
		System.out.println("... [PService] PServiceEngineDaemon inited with" +
				" ps=" + pservice_list.size() + " c=" + conf_list.size() + " v=" + viz_list.size() + 
				" em=" + embed_list.size() + " in " + (double)(psd_ini_finish-psd_ini_start)/1000000 + "ms");
	}

	public static PServiceEngineDaemon getInstance() { return instance; }
	
	public ItemVector<PServiceItem> getPServiceList() { return pservice_list; }
	public ItemVector<ConfItem> getConfList() { return conf_list; }
	public ItemVector<VizItem> getVizList() { return viz_list; }
	public ItemVector<iEmbed> getEmbedList() { return embed_list; }
	
//	public static SQLManager getSQLM() { return sql_manager; }
	
}


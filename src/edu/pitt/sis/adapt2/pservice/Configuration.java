package edu.pitt.sis.adapt2.pservice;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.openrdf.sesame.config.AccessDeniedException;
import org.openrdf.sesame.config.ConfigurationException;
import org.openrdf.sesame.config.UnknownRepositoryException;

import edu.pitt.sis.paws.core.utils.SQLManager;

public class Configuration
{
	// CONSTANTS
	//		field names - configuration
	public static final String PSERVICE_CONF_RDFID = "conf_rdfid";
	public static final String PSERVICE_CONF_NAME = "conf_name";
	//		field names - configuration uri
	public static final String PSERVICE_CONF_URI = "conf_uri";
//	public static final String PSERVICE_CONF_URITYPE = "conf_uritype";
//	public static final int PSERVICE_CONF_URITYPE_RDF = 0;
//	public static final int PSERVICE_CONF_URITYPE_FILTER = 1;
//	public static final int PSERVICE_CONF_URITYPE_CONTEXT = 2;
//	public static final int PSERVICE_CONF_URITYPE_PSERVICE = 3;
	//		field names - configuration rdf
	public static final String PSERVICE_CONF_RDF = "conf_rdf";
	//		field names - configuration datasource
	public static final String PSERVICE_CONF_DS_URL = "conf_ds_url";
	public static final String PSERVICE_CONF_DS_SCHEMA = "conf_ds_schema";
	public static final String PSERVICE_CONF_DS_USER = "conf_ds_user";
	public static final String PSERVICE_CONF_DS_PASS = "conf_ds_pass";
	public static final String PSERVICE_CONF_DS_TYPE = "conf_ds_type";
	public static final int PSERVICE_CONF_DS_TYPE_SESAME = 1;
	public static final int PSERVICE_CONF_DS_TYPE_JENADB = 2;
	
	public String rdfID;
	public String pservice_rdfID;
	public String name;
	public String description;
	public Map<String,URI> uris;
//	public ArrayList<URI> uri_list;
//	public ArrayList<URI> context_list;
//	public ArrayList<URI> filter_list;
//	public ArrayList<URI> service_list;
	public ArrayList<String> rdfs;
	public ArrayList<PServiceDataSource> datasource_list;
//	public ArrayList<Visualizer> pservice_visualizers;
	
	public Configuration()
	{
		rdfID = "";
		pservice_rdfID = "";
		name = "";
		
		
		description = "";
		uris = new HashMap<String,URI>();
		rdfs = new ArrayList<String>();
		datasource_list = new ArrayList<PServiceDataSource>();
//		pservice_visualizers = new ArrayList<Visualizer>();
	}
	
	public Configuration(String _conf_rdfID, String _pservice_rdfID, SQLManager _sqlm) 
			throws SQLException, URISyntaxException, 
				UnrecognizedURITypeException, IncorrectParameterSpecificationException,
				IOException, AccessDeniedException, UnknownRepositoryException, 
				ConfigurationException, IncorrectParameterSpecificationException,
				ClassNotFoundException, IllegalAccessException, InstantiationException
	{
		rdfID = _conf_rdfID;
		pservice_rdfID = _pservice_rdfID;
		uris = new HashMap<String,URI>();
		rdfs = new ArrayList<String>();
		datasource_list = new ArrayList<PServiceDataSource>();
//		pservice_visualizers = new ArrayList<Visualizer>();

		reset(_sqlm);
	}
	
	public void reset(SQLManager _sqlm) 
			throws SQLException, URISyntaxException, UnrecognizedURITypeException, 
				IncorrectParameterSpecificationException,
				IOException, AccessDeniedException, UnknownRepositoryException, 
				ConfigurationException, IncorrectParameterSpecificationException,
				ClassNotFoundException, IllegalAccessException, InstantiationException
	{
		
		if(uris.size() > 0) uris.clear();
		if(rdfs.size() > 0) rdfs.clear();
		if(datasource_list.size() > 0) datasource_list.clear();
//		if(pservice_visualizers.size() > 0) pservice_visualizers.clear();
		
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		ArrayList<Connection> al_c = new ArrayList<Connection>();
		ArrayList<Statement> al_s = new ArrayList<Statement>();
		ArrayList<ResultSet> al_r = new ArrayList<ResultSet>();
		String qry = "";
		try
		{
			conn = _sqlm.getConnection();
			al_c.add(conn);
			
			// read configuration name
			qry = "SELECT * FROM ent_config " +
					"WHERE rdfID='" + this.rdfID + "';"; 
			
			stmt = conn.createStatement();
			rs = stmt.executeQuery(qry);
			al_s.add(stmt);
			al_r.add(rs);
			
			if(rs.next())
			{
				String _name = rs.getString("Name");
				String _description = rs.getString("Description");
				
				name = _name;
				description = _description;
				
				// load visualizers
//				String qry2 = "";
//
//				qry2 = "SELECT v.* FROM ent_visualizer v JOIN rel_pservice_visualizer pv " +
//						"ON(pv.VisualizerID=v.VisualizerID) JOIN ent_pservice p " + 
//						"ON(pv.PServiceID=p.PServiceID) WHERE p.rdfID='" + this.pservice_rdfID + "';"; 
//				stmt2 = conn.prepareStatement(qry2);
//				rs2 = stmt2.executeQuery();
//				
//				while(rs2.next())
//				{
//					String vis_rdfID = rs2.getString("rdfID");
//					String vis_name = rs2.getString("Name");
//					String vis_class_name = rs2.getString("ClassName");
//					String vis_description = rs2.getString("Description");
//					pservice_visualizers.add(new Visualizer(vis_rdfID, vis_name, vis_class_name, vis_description));
//				}
			}
			
			// read URI's, filters, and contexts
			qry = "SELECT cu.* FROM ent_config_uri cu " + 
					"JOIN ent_config c ON(cu.ConfigID=c.ConfigID) "+
					"WHERE c.rdfID='" + this.rdfID + "';";
			stmt = conn.createStatement();
			rs = stmt.executeQuery(qry);
			al_s.add(stmt);
			al_r.add(rs);
			
			while(rs.next())
			{
				URI uri = new URI(rs.getString("URI"));
//				int type = rs3.getInt("Type");
				String tag = rs.getString("Tag");
				uris.put(tag, uri);
				
//				switch (type)
//				{
//					case PSERVICE_CONF_URITYPE_RDF:
//						uri_list.add(uri);
//					break;
//					case PSERVICE_CONF_URITYPE_FILTER:
//						filter_list.add(uri);
//					break;
//					case PSERVICE_CONF_URITYPE_CONTEXT:
//						context_list.add(uri);
//					break;
//					case PSERVICE_CONF_URITYPE_PSERVICE:
//						service_list.add(uri);
//					break;
//					default:
//						throw new UnrecognizedURITypeException("Configuration:: Unrecognized type of URI (" + type + ")");
//				}
			}
			
			// read Datasources
			qry = "SELECT cd.* FROM ent_config_datasource cd " + 
					"JOIN ent_config c ON(cd.ConfigID=c.ConfigID) " +
					"WHERE c.rdfID='" + this.rdfID + "';"; 
			stmt = conn.createStatement();
			rs = stmt.executeQuery(qry);
			al_s.add(stmt);
			al_r.add(rs);
			
			while(rs.next())
			{
				String url = rs.getString("URL");
				String schema = rs.getString("Schema");
				String user = rs.getString("User");
				String password = rs.getString("Pass");
				int type = rs.getInt("Type");
				
				switch (type)
				{
					case PServiceDataSource.PSERVICE_DATASOURCE_SESAME:
						datasource_list.add(new PServiceSesameDataSource(url, schema, user, password, type));
					break;
					default:
						throw new UnrecognizedURITypeException("Configuration:: Unsupported type dataset (" + type + ")");
				}
			}

			// read RDF
			qry = "SELECT cr.* FROM ent_config_rdf cr " +
					"JOIN ent_config c ON(cr.ConfigID=c.ConfigID) " +
					"WHERE c.rdfID='" + this.rdfID + "';"; 
			stmt = conn.createStatement();
			rs = stmt.executeQuery(qry);
			al_s.add(stmt);
			al_r.add(rs);
			
			while(rs.next())
			{
				String rdf = rs.getString("RDF");
				rdfs.add(rdf);
			}
			
			SQLManager.recycleObjects(al_c, al_s, al_r);
		}
		catch(SQLException sqle) { throw sqle; }
		catch(URISyntaxException use) { throw use; }

	}
	public String toString()
	{
		return "Configuration:: rdfID:" + this.rdfID + " name:" + this.name ;
	}
	
}

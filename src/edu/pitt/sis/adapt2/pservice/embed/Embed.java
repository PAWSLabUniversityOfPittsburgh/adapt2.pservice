package edu.pitt.sis.adapt2.pservice.embed;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import javax.servlet.http.HttpServletRequest;
import org.openrdf.sesame.config.AccessDeniedException;
import org.openrdf.sesame.config.ConfigurationException;
import org.openrdf.sesame.config.UnknownRepositoryException;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Seq;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RSS;
import edu.pitt.sis.adapt2.pservice.IncorrectParameterSpecificationException;
import edu.pitt.sis.adapt2.pservice.UnrecognizedURITypeException;
import edu.pitt.sis.paws.core.Item;
import edu.pitt.sis.paws.core.utils.SQLManager;

public class Embed extends Item implements iEmbed
{
	static final long serialVersionUID = 33L;
	// CONSTANTS
	//		field names
	public static final String EMBED_FN_ID = "embed_id";
	public static final String EMBED_FN_RDFID = "embed_rdfid";
	public static final String EMBED_FN_NAME = "embed_name";
	public static final String EMBED_FN_PSERVICEID = "embed_pserviceid";
	public static final String EMBED_FN_CONFIGID = "embed_configid";
	public static final String EMBED_FN_VIZID = "embed_vizid";
	public static final String EMBED_FN_URI = "embed_uri";
	public static final String EMBED_FN_RDF = "embed_rdf";
	public static final String EMBED_FN_USER = "embed_user";
	public static final String EMBED_FN_GROUP = "embed_group";

	// Attributes
	public String rdfID;
	public String name;
	private String uri;
	private String rdf;
	private String preset_user;
	private String preset_group;
	private String pservice_id;
	private String conf_id;
	private String viz_id;
	
	public Embed()
	{
		rdfID = "";
		name = "";
		pservice_id = "";
		conf_id = "";
		viz_id = "";
		uri = "";
		rdf = "";
		preset_user = "";
		preset_group = "";
	}

	public Embed(int _id, String _embed_rdfID, SQLManager _sqlm)
		throws SQLException, URISyntaxException, 
			UnrecognizedURITypeException, IncorrectParameterSpecificationException,
			IOException, AccessDeniedException, UnknownRepositoryException, 
			ConfigurationException, IncorrectParameterSpecificationException,
			ClassNotFoundException, IllegalAccessException, InstantiationException
	{
		super(_id, _embed_rdfID);
		rdfID = _embed_rdfID;
		
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String qry = "";
		try
		{
			conn = _sqlm.getConnection();
			
			qry = "SELECT e.*, s.rdfID AS PrdfID, c.rdfID AS CrdfID, v.rdfID AS VrdfID " +
				"FROM ent_embed e JOIN ent_pservice s ON (e.PServiceID=s.PServiceID) " + 
				"JOIN ent_config c ON (e.ConfigID=c.ConfigID) JOIN ent_visualizer v " +
				"ON (e.VisualizerID=v.VisualizerID) WHERE e.rdfID='"+ _embed_rdfID + "';";
			
			stmt = conn.createStatement();
			rs = stmt.executeQuery(qry);

			if(rs.next())
			{
				name = rs.getString("Name");
				uri = rs.getString("RSS_URI");
				rdf = rs.getString("RSS_RDF");
				preset_user = rs.getString("PresetUser");
				preset_group = rs.getString("PresetGroup");
				pservice_id = rs.getString("PrdfID");
				conf_id = rs.getString("CrdfID");
				viz_id = rs.getString("VrdfID");
				
//				PServiceItem pi = PServiceEngineDaemon.getInstance().getPServiceList().findById(pservice_id);
//				pservice = (PService)Class.forName(pi.getClassName()).newInstance();
//				conf = PServiceEngineDaemon.getInstance().getConfList().findById(config_id).getConf();
//				VizItem vi = pi.getVizs().findById(vis_id);
//				viz = (iPServiceResultVisualizer)Class.forName(vi.getClassName()).newInstance();
				
			}
			
			SQLManager.recycleObjects(conn, stmt, rs);
		}
		catch(SQLException sqle) { throw sqle; }
//		catch(URISyntaxException use) { throw use; }

	}

	public String invoke(String personalized_model, Map<String, String> _params)
		throws IOException, UnsupportedEncodingException //, IOException, ClassNotFoundException
	{
		String result = "";
		
//System.out.println("[PService] Embed.invoke personalized_model.length() = " + personalized_model.length());		
		
		Model pmodel = ModelFactory.createDefaultModel();
		InputStream in = new ByteArrayInputStream(personalized_model.getBytes("UTF-8"));
		pmodel.read(in, "");
		in.close();
		in = null;

//System.out.println("[PService] Embed.invoke pmodel.size() = " + pmodel.size());		
		
		String all_fragments = _params.get(EMBED_PARAM_FRAGMENT);
		ArrayList<Integer> i_fragments =  parseFragments(all_fragments);

		// Channel's javascript
		StmtIterator ch_iter = pmodel.listStatements(null, RDF.type, RSS.channel);		
		if (ch_iter.hasNext())
		{
			Resource channel = ch_iter.nextStatement().getSubject();
			result += pmodel.getProperty(channel, DC.description).getString() + "\n";
		}
		
		Seq cmap_seq = null;
		StmtIterator cmap_seq_iter = pmodel.listStatements(null, RDF.type, RDF.Seq);
		if(cmap_seq_iter.hasNext())
		{
			com.hp.hpl.jena.rdf.model.Statement seq_stmt = cmap_seq_iter.nextStatement();
			cmap_seq = pmodel.getSeq(seq_stmt.getSubject());
		}
		
		for(Iterator<Integer> iter = i_fragments.iterator(); iter.hasNext();)
		{// for all resources
			Integer i_fragment = iter.next();
			Resource res = cmap_seq.getResource(i_fragment.intValue());
			String item_title = res.getProperty(RSS.title).getString();
			String item_link = res.getProperty(RSS.link).getObject().toString();
			String item_desc_annot = res.getProperty(DC.description).getString();
			result += item_desc_annot + "&nbsp;" + "<a href='" + item_link + "' target='_blank'>" + item_title + "</a><br/>\n";
		}

		
		return "<!DOCTYPE HTML PUBLIC '-//W3C//DTD HTML 4.01 Transitional//EN' 'http://www.w3.org/TR/html4/loose.dtd'>" +
			"<html><head></head><title></title><body>" + result + "<body></html>";
	}

	public Map<String, String> retrieveParams(Map<String, String> _params, HttpServletRequest _request) throws UnsupportedEncodingException
	{
		_params.put(EMBED_PARAM_USER_ID, _request.getParameter(EMBED_PARAM_USER_ID));
		_params.put(EMBED_PARAM_GROUP_ID, _request.getParameter(EMBED_PARAM_GROUP_ID));
		_params.put(EMBED_PARAM_FRAGMENT, _request.getParameter(EMBED_PARAM_FRAGMENT));
		return _params;
	}
	
	public String getPServiceID() { return pservice_id; }
	public String getConfigID() { return conf_id; }
	public String getVisualizerID() { return viz_id; }
	public String getURI() { return uri; }
	public String getRDF() { return rdf; }
	public String getPresetUser() { return preset_user; }
	public String getPresetGroup() { return preset_group; }

	public ArrayList<Integer> parseFragments(String _framgents_param)
	{
		ArrayList<Integer> result = new ArrayList<Integer>();
		
		if(_framgents_param == null || _framgents_param.length() == 0)
			return result;
		
		StringTokenizer st = new StringTokenizer(_framgents_param, ",");
		while(st.hasMoreTokens())
		{// for all tokens
			String tk = st.nextToken();
			try
			{
				Integer new_fragment = new Integer(Integer.parseInt(tk));
				result.add(new_fragment);
			}
			catch(NumberFormatException nfe) { nfe.printStackTrace(System.out); };
		}// -- for all tokens
		
		return result;
	}
	
	
	public String toString()
	{
		return "Embed:: rdfID:" + this.rdfID + " name:" + this.name + "  " + 
			this.pservice_id + "~" + this.conf_id + "~" + this.viz_id;
	}

}

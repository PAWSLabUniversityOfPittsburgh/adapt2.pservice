package edu.pitt.sis.adapt2.pservice.rest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.openrdf.sesame.config.AccessDeniedException;
import org.openrdf.sesame.config.ConfigurationException;
import org.openrdf.sesame.config.UnknownRepositoryException;

import edu.pitt.sis.adapt2.pservice.Configuration;
import edu.pitt.sis.adapt2.pservice.IncorrectParameterSpecificationException;
import edu.pitt.sis.adapt2.pservice.PService;
import edu.pitt.sis.adapt2.pservice.PServiceDataSource;
import edu.pitt.sis.adapt2.pservice.UnrecognizedURITypeException;
import edu.pitt.sis.adapt2.pservice.iPServiceResultVisualizer;
import edu.pitt.sis.adapt2.pservice.datamodel.ConfItem;
import edu.pitt.sis.adapt2.pservice.datamodel.PServiceItem;
import edu.pitt.sis.adapt2.pservice.datamodel.PerformanceTraceItem;
import edu.pitt.sis.adapt2.pservice.datamodel.VizItem;
import edu.pitt.sis.paws.core.utils.SQLManager;

public class DataRobot
{
	// Constants
	//		PService, Configuration
	//			Identifying and configuring parameters
	public static final String REST_SERVICE_ID = "service_id"; // for identification
	
	public static final String REST_SERVICE_RDF_ID = "ps_rdf_id"; // for field editing
	public static final String REST_SERVICE_CLASSNAME = "ps_classname";
	public static final String REST_SERVICE_NAME = "ps_name";
	public static final String REST_SERVICE_DESCRIPTION = "ps_description";
	public static final String REST_SERVICE_INVOKE_TOKEN = "invtoken";
	public static final String REST_SERVICE_INVOKE_TOKEN_SUFFIX = "invtoken_suff";

	public static final String REST_CONFIGURATION_ID = "conf_id";
	public static final String REST_CONFIGURATION_RDF_ID = "conf_rdf_id";
	public static final String REST_CONFIGURATION_NAME = "conf_name";
	public static final String REST_CONFIGURATION_DESCRIPTION = "conf_desc";
	
	public static final String REST_VISUALIZER_ID = "vis_id";
	public static final String REST_VISUALIZER_RDF_ID = "vis_id";
	public static final String REST_VISUALIZER_CLASSNAME = "vis_class_name";
	public static final String REST_VISUALIZER_NAME = "vis_name";

	public static final String REST_GROUP_USER_NOT_SELECTED = "-- not selected --";
	
	//		Common constants
	public static final String REST_FORMAT = "_format";
	public static final String REST_FORMAT_HTML = "html";
	public static final String REST_FORMAT_RDF = "rdf";
	public static final String REST_CONTEXT_PATH = "context_path";
	public static final String REST_STATUS = "Status";
	public static final String REST_STATUS_OK = "OK";
	public static final String REST_STATUS_ERROR = "Error";
	public static final String REST_METHOD = "_method";
	public static final String REST_METHOD_DELETE = "delete";
	
	public static final String REST_RESULT = "Result";
	
	public static final String INVOKATION_TRACE = "invokation_trace";
	public static final String INVOKATION_RESULT = "invokation_result";
	
	
	public static Map<String, String> getPServiceInfo(Map<String, String> _parameters, SQLManager _sqlm,
			boolean multiple_pservices, HttpServletRequest request)
	{
		String service_id = _parameters.get(REST_SERVICE_ID);
		String _context_path = _parameters.get(REST_CONTEXT_PATH);
		if(multiple_pservices)
		{// get pservice info
			String qry = "SELECT * FROM ent_pservice;";
			Connection conn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			
			try
			{// retrieve pservice from db
				conn = _sqlm.getConnection();
				stmt = conn.prepareStatement(qry);
				rs = stmt.executeQuery();
				
				_parameters = formatPServiceInfo(rs, _parameters, true /*multiple_pservices*/, conn, request);
				
				rs.close();
				stmt.close();
				conn.close();
				rs = null;
				stmt = null;
				conn = null;
				
			}// end of -- etrieve pservice from db
			catch(SQLException sqle)
			{
				_parameters.put(REST_STATUS, REST_STATUS_ERROR);
				_parameters.put(REST_RESULT, getErrorMessageHTML("SQL Exception while retrieving p-service info.", _context_path));
				sqle.printStackTrace(System.out);
			}
			finally
			{
				if (rs != null)
				{
					try { rs.close(); } catch (SQLException e) { ; }
					rs = null;
				}
				if (stmt != null) 
				{
					try { stmt.close(); } catch (SQLException e) { ; }
					stmt = null;
				}
				if (conn != null)
				{
					try { conn.close(); } catch (SQLException e) { ; }
					conn = null;
				}
			}
		}// end of -- get pservice info
		else if(service_id != null && service_id.length() != 0)
		{// get pservice info
			String qry = "SELECT * FROM ent_pservice WHERE rdfID='" + service_id + "';";
			Connection conn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			
			try
			{// retrieve user by login from db
				conn = _sqlm.getConnection();
				stmt = conn.prepareStatement(qry);
				rs = stmt.executeQuery();
				
				_parameters = formatPServiceInfo(rs, _parameters, false /*multiple_pservices*/, conn, request);

				rs.close();
				stmt.close();
				conn.close();
				rs = null;
				stmt = null;
				conn = null;
				
			}// end of -- retrieve user by login from db
			catch(SQLException sqle)
			{
				_parameters.put(REST_STATUS, REST_STATUS_ERROR);
				_parameters.put(REST_RESULT, getErrorMessageHTML("SQL Exception while retrieving p-service info.", _context_path));
				sqle.printStackTrace(System.out);
			}
			finally
			{
				if (rs != null)
				{
					try { rs.close(); } catch (SQLException e) { ; }
					rs = null;
				}
				if (stmt != null) 
				{
					try { stmt.close(); } catch (SQLException e) { ; }
					stmt = null;
				}
				if (conn != null)
				{
					try { conn.close(); } catch (SQLException e) { ; }
					conn = null;
				}
			}
		}// end of -- get pservice info
		else
		{// no parameters
			_parameters.put(REST_STATUS, REST_STATUS_ERROR);
			_parameters.put(REST_RESULT, getErrorMessageHTML("P-service specified incorrectly", _context_path));
		}// end of -- no parameters
		
		return _parameters;
	}
	
	public static Map<String, String> getPServiceEditor(Map<String, String> _parameters,
			SQLManager _sqlm, HttpServletRequest request)
	{
		String service_id = _parameters.get(REST_SERVICE_ID);
		String _context_path = _parameters.get(REST_CONTEXT_PATH);
		
		
		String qry = "SELECT * FROM ent_pservice WHERE rdfID='" + service_id + "';";
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{// retrieve user by login from db
			conn = _sqlm.getConnection();
			stmt = conn.prepareStatement(qry);
			rs = stmt.executeQuery();
			
			_parameters = formatPServiceEditor(rs, _parameters, false /*multiple_pservices*/, conn, request);

			stmt.close();
			conn.close();
			rs = null;
			stmt = null;
			conn = null;
			
		}// end of -- retrieve user by login from db
		catch(SQLException sqle)
		{
			_parameters.put(REST_STATUS, REST_STATUS_ERROR);
			_parameters.put(REST_RESULT, getErrorMessageHTML("SQL Exception while retrieving p-service info.", _context_path));
			sqle.printStackTrace(System.out);
		}
		finally
		{
			if (rs != null)
			{
				try { rs.close(); } catch (SQLException e) { ; }
				rs = null;
			}
			if (stmt != null) 
			{
				try { stmt.close(); } catch (SQLException e) { ; }
				stmt = null;
			}
			if (conn != null)
			{
				try { conn.close(); } catch (SQLException e) { ; }
				conn = null;
			}
		}
		
		return _parameters;
	}

	private static Map<String, String> formatPServiceEditor(ResultSet _rs, Map<String, String> _parameters,
			boolean multiple_pservices, Connection _conn, HttpServletRequest request) throws SQLException
	{
		String _context_path = _parameters.get(REST_CONTEXT_PATH);
		
		if(_rs.next())
		{// p-service exists
			String rdfID = _rs.getString("rdfID");
			String name = _rs.getString("Name");
			String class_name = _rs.getString("ClassName");
			String description = _rs.getString("Description");
			
			String result = "";
			
			String home_html = "<a href='" + _context_path + "/' title='ADAPT&sup2; Personalization Services Home'>Home</a>";
			String view_html = "&nbsp;&raquo;&nbsp;<a href='" + _context_path + "/service/" + rdfID + "' title='Back to PService'>PService</a>";
			String pslist_html = "&nbsp;&raquo;&nbsp;<a href='" + _context_path + "/services' title='List of PServices'>All PServices</a>";

			String user_login = request.getRemoteUser();
			boolean isLoggedIn = (user_login!=null) && (user_login.length()>0);
			String loginout_url = (isLoggedIn)?user_login + "&nbsp;&nbsp;" + "<a href='" + request.getContextPath() + "/index.jsp?logout=1'>Log out</a>" :
				"<span style='color:#999999;'>You are not logged in</span>&nbsp;&nbsp;<a href='" + request.getContextPath() + "/home'>Log in</a>";
			
			result = 
				getPageHeaderHTML("ADAPT&sup2; Personalization Services - PService Info Editor", _context_path) +
				"<form form method='post' action='" + _context_path + "/service/" + rdfID + "'>\n"+
				"<input name='" + REST_SERVICE_ID + "' type='hidden' value='" + rdfID + "'/>"+
				"<table cellpadding='2px' cellspacing='0px' class='brown_table' width='500px'>\n"+
				"	<caption style='text-align:right; padding:3px;' class='login_header'>" + loginout_url + "</caption>\n" + 
				"	<tr><td colspan='2' class='brown_table_caption'>" + name + " - Editing</td></tr>\n"+
				"	<tr>\n"+
				"	  <td class='brown_table_header' colspan='2'>" + 
					home_html + pslist_html + view_html + "<div style='text-align:right;display:block;'>&nbsp;</div></td>\n"+
				"  	</tr>\n"+
				"	<tr>\n"+ 
				"		<td>rdf ID</td>\n"+
				"		<td width='100%'><input name='" +  REST_SERVICE_RDF_ID + "' type='text' maxlength='50' size='25' value='" + rdfID + "'/>&nbsp;<font color='#FF0000'>*</font></td>\n"+ 
				"	</tr>\n"+
				"	<tr>\n"+
				"		<td>Name</td>\n"+
				"		<td><input name='" + REST_SERVICE_NAME + "' type='text' maxlength='100' size='45' value='" + name + "'/></td>\n"+
				"	</tr>\n"+
				"	<tr>\n"+
				"		<td>Class&nbsp;Name</td>\n"+
				"		<td><input name='" + REST_SERVICE_CLASSNAME + "' type='text' maxlength='100' size='45' value='" + class_name + "'/></td>\n"+
				"	</tr>\n"+
				"	<tr>\n"+
				"		<td valign='top'>Description</td>\n"+
				"		<td><textarea name='" + REST_SERVICE_DESCRIPTION + "' cols='46' rows='5'>" + clrStr(description) + "</textarea></td>\n"+
				"	</tr>\n"+
				"	<tr>\n"+
				"		<td></td>\n"+
				"		<td><font color='#FF0000'>*&nbsp;changing rdfID might have un-anticipated side effects</font></td>\n"+
				"	</tr>\n"+
				"	<tr>\n"+
				"		<td class='brown_table_footer'><a href='" + _context_path + "/service/" + rdfID + "'><input type='button' value='Cancel'/></a></td>\n"+
				"		<td class='brown_table_footer' align='right'><input type='submit' value='Submit'/></td>\n"+
				"	</tr>\n"+
				"</table>\n</form>\n"+
				"</body></html>";

			_parameters.put(REST_STATUS, REST_STATUS_OK);
			_parameters.put(REST_RESULT, result);
			
		}// end of -- pservice exists
		else
		{
			_parameters.put(REST_STATUS, REST_STATUS_ERROR);
			_parameters.put(REST_RESULT, getErrorMessageHTML("Specified p-service has not been found", _context_path));
		}
		return _parameters;
	}

	private static Map<String, String> formatPServiceInfo(ResultSet _rs, Map<String, String> _parameters,
			boolean multiple_pservices,	Connection _conn, HttpServletRequest request) throws SQLException
	{
		String _context_path = _parameters.get(REST_CONTEXT_PATH);
		String _format = _parameters.get(REST_FORMAT);
		
		
		if(!multiple_pservices)
		{// single p-service
			if(_rs.next())
			{// p-service exists
				String rdfID = _rs.getString("rdfID");
				String name = _rs.getString("Name");
				String class_name = _rs.getString("ClassName");
				String description = _rs.getString("Description");
				
				String result = "";
				
				if(_format == null || _format.length() == 0 || _format.equals(REST_FORMAT_HTML))
				{// format is html
					// retrieve configurations
					String confs = "";
					String cqry = "SELECT c.* FROM ent_config c JOIN rel_pservice_config pc " +
							"ON(c.ConfigID=pc.ConfigID) JOIN ent_pservice p ON(pc.PServiceID=p.PServiceID) " +
							"WHERE p.rdfID='" + rdfID +"'";
					PreparedStatement stmt = _conn.prepareStatement(cqry);
					ResultSet grs = stmt.executeQuery();
					int conf_count = 0;
					while(grs.next())
					{
						String cname = grs.getString("Name");
						String crdfID = grs.getString("rdfID");
						confs += "<div>&nbsp;&nbsp;<a href='" + _context_path + "/service/" + rdfID + "/conf/" + crdfID + "'>" + cname + "</a></div>";
						conf_count ++;
					}
					confs = "<div name='openerControl'>List(" + conf_count + 
							")</div>\n<div name='opener'>" + confs + "</div>";
					grs.close();
					stmt.close();
					grs = null;
					stmt = null;
					
					if (grs != null)
					{
						try { grs.close(); } catch (SQLException e) { ; }
						grs = null;
					}
					if (stmt != null) 
					{
						try { stmt.close(); } catch (SQLException e) { ; }
						stmt = null;
					}
					
					// end of -- retrieve configurations
					
					// retrieve visualizations
					String vizs = "";
					String vqry = "SELECT v.* FROM ent_visualizer v JOIN rel_pservice_visualizer pv " +
							"ON(v.VisualizerID=pv.VisualizerID) JOIN ent_pservice p ON(pv.PServiceID=p.PServiceID) " +
							"WHERE p.rdfID='" + rdfID +"'";
					PreparedStatement vstmt = _conn.prepareStatement(vqry);
					ResultSet vgrs = vstmt.executeQuery();
					int vconf_count = 0;
					while(vgrs.next())
					{
						String vname = vgrs.getString("Name");
//						String vrdfID = vgrs.getString("rdfID");
						vizs += "<div>&nbsp;&nbsp;<a href='#" /*+ _context_path + "/service/" + rdfID + "/conf/" + crdfID */+ "'>" + vname + "</a></div>";
						vconf_count ++;
					}
					vizs = "<div name='openerControl'>List(" + vconf_count + 
							")</div>\n<div name='opener'>" + vizs + "</div>";
					vgrs.close();
					vstmt.close();
					
					vgrs = null;
					vstmt = null;
					if (vgrs != null)
					{
						try { vgrs.close(); } catch (SQLException e) { ; }
						vgrs = null;
					}
					if (vstmt != null) 
					{
						try { stmt.close(); } catch (SQLException e) { ; }
						vstmt = null;
					}
					
					// end of -- retrieve visualizations
					
					
//					String home_html = "<a href='" + _context_path + "/'><img src='" + _context_path + "/assets/home.gif' title='Home' alt='Home' border='0'></a>";
//					String edit_html = "<a href='" + _context_path + "/service/" + rdfID + "/edit'><img src='" + _context_path + "/assets/edit2_enable.gif' title='Edit' alt='Edit' border='0'></a>";
					String home_html = "<a href='" + _context_path + "/' title='ADAPT&sup2; Personalization Services Home'>Home</a>";
					String edit_html = "<a href='" + _context_path + "/service/" + rdfID + "/edit' title='Edit PService'>Edit</a>";
					String pslist_html = "&nbsp;&raquo;&nbsp;<a href='" + _context_path + "/services' title='List of PServices'>All PServices</a>";
					
					String user_login = request.getRemoteUser();
					boolean isLoggedIn = (user_login!=null) && (user_login.length()>0);
					String loginout_url = (isLoggedIn)?user_login + "&nbsp;&nbsp;" + "<a href='" + request.getContextPath() + "/index.jsp?logout=1'>Log out</a>" :
						"<span style='color:#999999;'>You are not logged in</span>&nbsp;&nbsp;<a href='" + request.getContextPath() + "/home'>Log in</a>";
					
					result = 
						getPageHeaderHTML("ADAPT&sup2; Personalization Services - PService Info", _context_path) +
						"<table cellpadding='2px' cellspacing='0px' class='brown_table' width='500px'>\n"+
						"	<caption style='text-align:right; padding:3px;' class='login_header'>" + loginout_url + "</caption>\n" + 
						"	<tr><td colspan='2' class='brown_table_caption'>" + name + "</td></tr>\n"+
						"	<tr>\n"+
						"	  <td class='brown_table_header' colspan='2'>" + 
							home_html + pslist_html + "<div style='text-align:right;display:block;'>" + edit_html + "</div></td>\n"+
						"  	</tr>\n"+
						"	<tr>\n"+
						"		<td>rdf ID</td>\n"+
						"		<td width='100%'>" + rdfID + "</td>\n"+
						"	</tr>\n"+
						"	<tr>\n"+
						"		<td>Name</td>\n"+
						"		<td>" + name + "</td>\n"+
						"	</tr>\n"+
						"	<tr>\n"+
						"		<td>Class Name</td>\n"+
						"		<td>" + class_name + "</td>\n"+
						"	</tr>\n"+
						"	<tr>\n"+
						"		<td valign='top'>Description</td>\n"+
						"		<td>" + clrStr(description) + "</td>\n"+
						"	</tr>\n"+
						"	<tr>\n"+
						"		<td valign='top' class='brown_table_footer'>Configurations</td>\n"+
						"		<td class='brown_table_footer'>" + confs + "</td>\n"+
						"	</tr>\n"+
						"	<tr>\n"+
						"		<td valign='top' class='brown_table_footer2'>Visualizers</td>\n"+
						"		<td class='brown_table_footer2'>" +  vizs + "</td>\n"+
						"	</tr>\n"+
						"</table>\n"+
						"</body></html>";
	
					_parameters.put(REST_STATUS, REST_STATUS_OK);
					_parameters.put(REST_RESULT, result);
				}// end of -- format is html
//				else if(_format != null && _format.length() != 0 && _format.equals(REST_FORMAT_RDF))
//				{//format is rdf
//				}// end of -- format is rdf
				else
				{
					_parameters.put(REST_STATUS, REST_STATUS_ERROR);
					_parameters.put(REST_RESULT, getErrorMessageHTML("Specified format of data is not supported", _context_path));
				}
				
			}// end of -- pservice exists
			else
			{
				_parameters.put(REST_STATUS, REST_STATUS_ERROR);
				_parameters.put(REST_RESULT, getErrorMessageHTML("Specified p-service has not been found", _context_path));
			}
		}// end of -- single pservice
		else
		{// multiple p-services
			if(_format != null && _format.length() == 0 && (!_format.equals(REST_FORMAT_HTML)) /*&& (!_format.equals(REST_FORMAT_RDF))*/ )
			{
				_parameters.put(REST_STATUS, REST_STATUS_ERROR);
				_parameters.put(REST_RESULT, getErrorMessageHTML("Specified format of data is not supported", _context_path));
			}
			else
			{// format ok
				String result = "";
				// HEADER
				if(_format == null || _format.length() == 0 || _format.equals(REST_FORMAT_HTML))
				{
//					String home_html = "<a href='" + _context_path + "/'><img src='" + _context_path + "/assets/home.gif' title='Home' alt='Home' border='0'></a>";
//					String add_html = "<a href='" + _context_path + "/services/new'><img src='" + _context_path + "/assets/add2_enable.gif' title='Add New P-Service' alt='Add' border='0'></a>";
					String home_html = "<a href='" + _context_path + "/' title='ADAPT&sup2; Personalization Services Home'>Home</a>";
					String add_html = "<a href='" + _context_path + "/services/new' title='Add New P-Service'>Add</a>";
//					String logout_url = "&nbsp;&nbsp;<a href='" + _context_path + "/index.jsp?logout=1'>[logout]</a>";
					String user_login = request.getRemoteUser();
					boolean isLoggedIn = (user_login!=null) && (user_login.length()>0);
					String loginout_url = (isLoggedIn)?user_login + "&nbsp;&nbsp;" + "<a href='" + request.getContextPath() + "/index.jsp?logout=1'>Log out</a>" :
						"<span style='color:#999999;'>You are not logged in</span>&nbsp;&nbsp;<a href='" + request.getContextPath() + "/home'>Log in</a>";
					
					result = getPageHeaderHTML("ADAPT&sup2; Personalization Services - List of all PServices", _context_path) +
							"<table cellpadding='2px' cellspacing='0px' class='brown_table' width='500px'>\n"+
							"	<caption style='text-align:right; padding:3px;' class='login_header'>" + loginout_url + "</caption>\n" + 
							"	<tr><td colspan='2' class='brown_table_caption'>List of all PServices</td></tr>\n"+
							"	<tr>\n"+
							"	  <td class='brown_table_header' colspan='2'>" + 
								home_html + "<div style='text-align:right;display:block;'>" + add_html + "</div></td>\n"+
							"  	</tr>\n";
				}
				else if(_format != null && _format.length() != 0 && _format.equals(REST_FORMAT_RDF))
					result = getPageHeaderRDF();
				
				int pservice_count = 0;
				
				while(_rs.next())
				{// for all users
					pservice_count ++;
					
					String rdfID = _rs.getString("rdfID");
					String name = _rs.getString("Name");
//					String class_name = _rs.getString("ClassName");
//					String description = _rs.getString("Description");
					
					if(_format == null || _format.length() == 0 || _format.equals(REST_FORMAT_HTML))
					{// format is html
						
						result += 
							"<tr>\n" +
							"	<td valign='top'>&nbsp;&nbsp;&bull;</td>\n" +
							"	<td><a href='" + _context_path + "/service/" + rdfID + "'>" + name + "&nbsp;(" + rdfID + ")</a></td>\n" +
							"</tr>\n";

		
					}// end of -- format is html
//					else if(_format != null && _format.length() != 0 && _format.equals(REST_FORMAT_RDF))
//					{//format is rdf
//						result += 
//							"	<foaf:Person rdf:about='" + _context_path + "/rdf/users#" + login + "'>\n" + 
//							"		<foaf:name>" + name + "</foaf:name>\n" + 
//							"		<foaf:holdsAccount>\n" + 
//							"			<foaf:OnlineAccount>\n" + 
//							"				<foaf:accountServiceHomepage rdf:resource='" + _context_path + "/login.jsp'/>\n" + 
//							"				<foaf:accountName>" + login + "</foaf:accountName>\n" + 
//							"			</foaf:OnlineAccount>\n" + 
//							"		</foaf:holdsAccount>\n" + 
//							((email != null && email.length() >0 )?"		<foaf:mbox_sha1sum>" + Digest.SHA1(email) + "</foaf:mbox_sha1sum>\n":"") + 
//							"		<vCard:note>" + how + "</vCard:note>\n" + 
//							"		<rdfs:isDefinedBy rdf:resource='" + _context_path + "/rdf/users'/>\n" + 
//							"	</foaf:Person>\n";
//					}// end of -- format is rdf
				}// end of -- for all users
				
				
				// FOOTER
				if(_format == null || _format.length() == 0 || _format.equals(REST_FORMAT_HTML))
					result +=
						"	<tr>\n"+
						"		<td class='brown_table_footer'>&nbsp;</td>\n"+
						"		<td class='brown_table_footer'>" + pservice_count + " personalization service(s)</td>\n"+
						"	</tr>\n"+
						"</table>\n" +
						"</body></html>";
				else if(_format != null && _format.length() != 0 && _format.equals(REST_FORMAT_RDF))
					result += "</rdf:RDF>";
					
				_parameters.put(REST_STATUS, REST_STATUS_OK);
				_parameters.put(REST_RESULT, result);
			}// end of -- format ok
		}// end of -- multiple users
		return _parameters;
	}
	
	private static Map<String, String> checkPServiceConfViz(Map<String, String> _parameters, SQLManager _sqlm)
	{
		String service_id = _parameters.get(REST_SERVICE_ID);
		String conf_id = _parameters.get(REST_CONFIGURATION_ID);
		String vis_id = _parameters.get(REST_VISUALIZER_ID);
		
		_parameters.put(REST_STATUS, REST_STATUS_OK);
		
		String message = "";
		
		PServiceEngineDaemon psed = PServiceEngineDaemon.getInstance();

		// pservice exists
		PServiceItem pi = psed.getPServiceList().findByTitle(service_id);
		boolean pservice_exists = pi!=null;
		
		// configuration exists
		ConfItem ci = psed.getConfList().findByTitle(conf_id);
		boolean config_exists = ci!=null;
		
		// pservice-configuration link exists
		boolean pservice_config_linked = (pservice_exists && config_exists)
				?pi.getConfs().findByTitle(conf_id)!=null:false;
		
		if( !(pservice_exists && config_exists && pservice_config_linked) )
		{
			_parameters.put(REST_STATUS, REST_STATUS_ERROR);
			message += (!pservice_exists)?"Specified p-service ('" + service_id + "') not found.<br/>":"";
			message += (!config_exists)?"Specified configuration ('" + conf_id + "') not found.<br/>":"";
			message += (!pservice_config_linked)?"Specified p-service ('" + service_id + "') is not bound with configuration ('" + conf_id + "').<br/>":"";
			_parameters.put(REST_RESULT, message);
		}
//		else
//		{
//			_parameters.put(REST_SERVICE_CLASSNAME, pi.getClassName());
//			_parameters.put(REST_SERVICE_NAME, pi.getName());
//			_parameters.put(REST_SERVICE_ID, pi.getTitle());
//		}
		
		if(vis_id != null && vis_id.length()>0)
		{
			// visualizer exists if needs to be checked
			VizItem vi = psed.getVizList().findByTitle(vis_id);
			boolean viz_exists = vi!=null;
			
			// pservice-visualizer link exists if needs to be checked
			boolean pservice_viz_link_exists = (pservice_exists && viz_exists)?pi.getVizs().findByTitle(vis_id)!=null:false;
			
			if( !(viz_exists && pservice_viz_link_exists))
			{
				_parameters.put(REST_STATUS, REST_STATUS_ERROR);
				message += (!viz_exists)?"Specified visualizer ('" + vis_id + "') not found.<br/>":"";
				message += (!pservice_viz_link_exists)?"Specified visualizer ('" + vis_id + "') is not bound with PService ('" + service_id + "').<br/>":"";
				_parameters.put(REST_RESULT, message);
			}
//			else
//			{
//				_parameters.put(REST_VISUALIZER_CLASSNAME, vi.getClassName());
//				_parameters.put(REST_VISUALIZER_NAME, vi.getName());
//			}
		}

		return _parameters;
	}

	
	public static Map<String, String> doPServiceIvoke(Map<String, String> _parameters, SQLManager _sqlm, HttpServletRequest _request, PerformanceTraceItem _trace)
	{
//		// Create trace
//		PerformanceTrace trace = new PerformanceTrace();

		String service_id = _parameters.get(REST_SERVICE_ID);
		String conf_id = _parameters.get(REST_CONFIGURATION_ID);
		String vis_id = _parameters.get(REST_VISUALIZER_ID);
		String invoke_token = _parameters.get(REST_SERVICE_INVOKE_TOKEN);
		String invoke_token_suffix = _parameters.get(REST_SERVICE_INVOKE_TOKEN_SUFFIX);
		String _context_path = _parameters.get(REST_CONTEXT_PATH);
		
//		trace.invocation_ts_start = start;
		_trace.pservice_rdfid = service_id;
		_trace.conf_rdfid = conf_id;
		_trace.viz_rdfid = vis_id;
		_trace.token = invoke_token;
		_trace.token_suffix = invoke_token_suffix;

		// Check p-service-config
		_parameters = checkPServiceConfViz(_parameters, _sqlm);
		
		
//		String pserviceClassName = _parameters.get(REST_SERVICE_CLASSNAME);
		PServiceItem pi = PServiceEngineDaemon.getInstance().getPServiceList().findByTitle(service_id);

		PService pservice = null;
		// load class
		try
		{
			pservice = (PService)Class.forName(pi.getClassName()).newInstance();
//System.out.println("\tclass found");		
		}
		catch(ClassNotFoundException cnfe)
		{
			_parameters.put(REST_STATUS, REST_STATUS_ERROR);
			_parameters.put(REST_RESULT, getErrorMessageHTML("Personalization Service Class was not found.", _context_path));
			_trace.result = "loadclass.ClassNotFoundException";
			return _parameters;
		}
		catch(IllegalAccessException iae)
		{
			_parameters.put(REST_STATUS, REST_STATUS_ERROR);
			_parameters.put(REST_RESULT, getErrorMessageHTML("Illegal Access Exception while loading Personalization Service Class.", _context_path));
			_trace.result = "loadclass.IllegalAccessException";
			return _parameters;
		}
		catch(InstantiationException ie)
		{
			_parameters.put(REST_STATUS, REST_STATUS_ERROR);
			_parameters.put(REST_RESULT, getErrorMessageHTML("Instantiation Exception while loading Personalization Service Class.", _context_path));
			_trace.result = "loadclass.InstantiationException";
			return _parameters;
		}
		
		// load configuration
//System.out.println("conf_id=" + conf_id);		
		Configuration conf = PServiceEngineDaemon.getInstance().getConfList().findByTitle(conf_id).getConf();
		
		// load visualizer if any
		iPServiceResultVisualizer vis = null;
		if(vis_id != null && vis_id.length() > 0)
		{
			VizItem vi = pi.getVizs().findByTitle(vis_id);
			// find visualizer
//			for(int j=0; j<conf.pservice_visualizers.size(); j++)
//			{
//				if(conf.pservice_visualizers.get(j).rdfID.equals(vis_id))
//				{
					try
					{
						vis = (iPServiceResultVisualizer)Class.forName(vi.getClassName()).newInstance();
					}
					catch(ClassNotFoundException cnfe)
					{
						_parameters.put(REST_STATUS, REST_STATUS_ERROR);
						_parameters.put(REST_RESULT, getErrorMessageHTML("Visualizer Class was not found.", _context_path));
						_trace.result = "vizload.ClassNotFoundException";
						return _parameters;
					}
					catch(IllegalAccessException iae)
					{
						_parameters.put(REST_STATUS, REST_STATUS_ERROR);
						_parameters.put(REST_RESULT, getErrorMessageHTML("Illegal Access Exception while loading Visualizer Class.", _context_path));
						_trace.result = "vizload.IllegalAccessException";
						return _parameters;
					}
					catch(InstantiationException ie)
					{
						_parameters.put(REST_STATUS, REST_STATUS_ERROR);
						_parameters.put(REST_RESULT, getErrorMessageHTML("Instantiation Exception while loading Visualizer Class.", _context_path));
						_trace.result = "vizload.InstantiationException";
						return _parameters;
					}
//				}
//			}
		}
		
		Map<String, String> http_params = null; // intermediate parameters of the HTTP request
		try
		{
			http_params = pservice.retrieveParams(_parameters, _request);
//System.out.println("\tparams retrieved");		
		}
		catch(UnsupportedEncodingException uee)
		{
			_parameters.put(REST_STATUS, REST_STATUS_ERROR);
			_parameters.put(REST_RESULT, getErrorMessageHTML("An exception occured while retrieving p-service invokation parameters", _context_path));
			_trace.result = "retrieveparam.UnsupportedEncodingException";
			return _parameters;
		}
		
		String response = "";
		try
		{
//System.out.println("\tbefore invocation (conf==null):"+(conf==null));		
			response = pservice.invoke(conf, vis, http_params, _trace);
//System.out.println("\tinvoked");		
		}
		catch(URISyntaxException use)
		{
			use.printStackTrace(System.out);
			_trace.result = "invoke.URISyntaxException";
			_parameters.put(REST_STATUS, REST_STATUS_ERROR);
			_parameters.put(REST_RESULT, getErrorMessageHTML("URISyntaxException occured while invoking p-service ", _context_path));
			return _parameters;
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace(System.out);
			_trace.result = "invoke.IOException";
			_parameters.put(REST_STATUS, REST_STATUS_ERROR);
			_parameters.put(REST_RESULT, getErrorMessageHTML("IOException occured while invoking p-service ", _context_path));
			return _parameters;
		}
		catch(ClassNotFoundException cnfe)
		{
			cnfe.printStackTrace(System.out);
			_trace.result = "invoke.ClassNotFoundException";
			_parameters.put(REST_STATUS, REST_STATUS_ERROR);
			_parameters.put(REST_RESULT, getErrorMessageHTML("ClassNotFoundException occured while invoking p-service ", _context_path));
			return _parameters;
		}
		
		// recycle http_params
		http_params.clear();
		http_params = null;
		
		_parameters.put(REST_STATUS, REST_STATUS_OK);
		_parameters.put(REST_RESULT, response);
		

		return _parameters;
	}

	public static Map<String, String> getPServiceIvokeUI(Map<String, String> _parameters, SQLManager _sqlm, HttpServletRequest _request)
	{
		String service_id = _parameters.get(REST_SERVICE_ID);
		String conf_id = _parameters.get(REST_CONFIGURATION_ID);
		String _context_path = _parameters.get(REST_CONTEXT_PATH);
		
		// Check p-service-config
		_parameters = checkPServiceConfViz(_parameters, _sqlm);
		
//		String pserviceClassName = _parameters.get(REST_SERVICE_CLASSNAME);
//		String pserviceName = _parameters.get(REST_SERVICE_NAME);

		PServiceItem pi = PServiceEngineDaemon.getInstance().getPServiceList().findByTitle(service_id);
		
		PService pservice = null;
		// load class
		try
		{
			pservice = (PService)Class.forName(pi.getClassName()).newInstance();
		}
		catch(ClassNotFoundException cnfe)
		{
			_parameters.put(REST_STATUS, REST_STATUS_ERROR);
			_parameters.put(REST_RESULT, getErrorMessageHTML("Personalization Service Class was not found.", _context_path));
			cnfe.printStackTrace(System.out);
			return _parameters;
		}
		catch(IllegalAccessException iae)
		{
			_parameters.put(REST_STATUS, REST_STATUS_ERROR);
			_parameters.put(REST_RESULT, getErrorMessageHTML("Illegal Access Exception while loading Personalization Service Class.", _context_path));
			iae.printStackTrace(System.out);
			return _parameters;
		}
		catch(InstantiationException ie)
		{
			_parameters.put(REST_STATUS, REST_STATUS_ERROR);
			_parameters.put(REST_RESULT, getErrorMessageHTML("Instantiation Exception while loading Personalization Service Class.", _context_path));
			ie.printStackTrace(System.out);
			return _parameters;
		}
		
		// load configuration
		Configuration conf;
		try
		{
			conf = new Configuration(conf_id, service_id, _sqlm);
		}
		catch(Exception e)
		{
			_parameters.put(REST_STATUS, REST_STATUS_ERROR);
			_parameters.put(REST_RESULT, getErrorMessageHTML("An exception occured while retrieving configuration", _context_path));
			e.printStackTrace(System.out);
			return _parameters;
		}
		
		// Show UI
		String home_html = "<a href='" + _context_path + "/' title='ADAPT&sup2; Personalization Services Home'>Home</a>";
		String pservices_html = "&nbsp;&raquo;&nbsp;<a href='" + _context_path + "/services' title='List of all PServices'>All PServices</a>";
		String pservice_html = "&nbsp;&raquo;&nbsp;<a href='" + _context_path + "/service/" + service_id + "' title='Back to PService'>PService</a>";
		String pservice_conf_html = "&nbsp;&raquo;&nbsp;<a href='" + _context_path + "/service/" + service_id + "/conf/" + conf_id + "' title='Back to PService Configuration'>Conf.</a>";

		String user_login = _request.getRemoteUser();
		boolean isLoggedIn = (user_login!=null) && (user_login.length()>0);
		String loginout_url = (isLoggedIn)?user_login + "&nbsp;&nbsp;" + "<a href='" + _request.getContextPath() + "/index.jsp?logout=1'>Log out</a>" :
			"<span style='color:#999999;'>You are not logged in</span>&nbsp;&nbsp;<a href='" + _request.getContextPath() + "/home'>Log in</a>";
		
		String result = 
			getPageHeaderHTML("ADAPT&sup2; Personalization Services - Invoke Service", _context_path) +
			"<form form method='post' action='" + _context_path + "/service/" + service_id + "/invoke/" + conf_id + "' target='_blank'>\n"+
			"<table cellpadding='2px' cellspacing='0px' class='brown_table' width='500px'>\n"+
			"	<caption style='text-align:right; padding:3px;' class='login_header'>" + loginout_url + "</caption>\n" + 
			"	<tr><td colspan='2' class='brown_table_caption'>Invoke Service</td></tr>\n"+
			"	<tr>\n"+
			"	  <td class='brown_table_header' colspan='2'>" + 
				home_html + pservices_html + pservice_html + pservice_conf_html + "<div style='text-align:right;display:block;'>&nbsp;</div></td>\n" +
			"  	</tr>\n" +
			"	<tr>\n"+
			"		<td valign='top'>Service</td>\n"+
			"		<td width='100%'>" + pi.getName() + "</td>\n"+
			"	</tr>\n"+
			"	<tr>\n"+
			"		<td valign='top'>Config.</td>\n"+
			"		<td>" + conf.name + "</td>\n"+
			"	</tr>\n"+
			"	<tr>\n"+
			"		<td valign='top'>Visualizer</td>\n"+
			"		<td>\n" + 
			pi.getVisualizerHTMLSelector() + 
			"		</td>\n"+
			"	</tr>\n"+
			
			pservice.getInvokerUI(conf) +
			"	<tr>\n"+
			"		<td>&nbsp;</td>\n"+
			"		<td>&nbsp;</td>\n"+
			"	</tr>\n"+
			"	<tr>\n"+
			"		<td class='brown_table_footer'><a href='" + _context_path + "/service/" + service_id + "/conf/" + conf_id + "' title='Pressing Calcel will bring you back to PService Configuration page'><input type='button' value='Cancel'/></a></td>\n"+
			"		<td class='brown_table_footer' align='right'><input type='submit' value='Submit' title='Results of PService invokation will be shown in a new window'/></td>\n"+
			"	</tr>\n"+
			"</table>\n</form>\n"+
			"</body></html>";

		_parameters.put(REST_STATUS, REST_STATUS_OK);
		_parameters.put(REST_RESULT, result);
		
		
		
		
		
		
//		Map<String, String> params = null;
//		try
//		{
//			params = pservice.retrieveParams(_request);
//		}
//		catch(UnsupportedEncodingException uee)
//		{
//			_parameters.put(REST_STATUS, REST_STATUS_ERROR);
//			_parameters.put(REST_RESULT, getErrorMessageHTML("An exception occured while retrieving p-service invokation parameters", _context_path));
//			uee.printStackTrace(System.out);
//			return _parameters;
//		}
//		
//		String response = pservice.invoke(conf, params);
//		
//		_parameters.put(REST_STATUS, REST_STATUS_OK);
//		_parameters.put(REST_RESULT, response);
		
		return _parameters;
	}

	public static Map<String, String> getPServiceConfInfo(Map<String, String> _parameters,
			SQLManager _sqlm, HttpServletRequest request)
	{
		String conf_id = _parameters.get(REST_CONFIGURATION_ID);
		String pservice_id = _parameters.get(REST_SERVICE_RDF_ID);
		String _context_path = _parameters.get(REST_CONTEXT_PATH);
		
		// Check p-service-config
		_parameters = checkPServiceConfViz(_parameters, _sqlm);
		
		// load configuration
		Configuration conf = null;
		try
		{
			conf = new Configuration(conf_id, pservice_id, _sqlm);
		}
		catch(SQLException sqle)
		{
			_parameters.put(REST_STATUS, REST_STATUS_ERROR);
			_parameters.put(REST_RESULT, getErrorMessageHTML("SQL Exception occured while retrieving configuration", _context_path));
			sqle.printStackTrace(System.out);
			return _parameters;
		}
		catch(URISyntaxException use)
		{
			_parameters.put(REST_STATUS, REST_STATUS_ERROR);
			_parameters.put(REST_RESULT, getErrorMessageHTML("URI Syntax Exception occured while retrieving configuration", _context_path));
			use.printStackTrace(System.out);
			return _parameters;
		}
		catch(UnrecognizedURITypeException uute)
		{
			_parameters.put(REST_STATUS, REST_STATUS_ERROR);
			_parameters.put(REST_RESULT, getErrorMessageHTML("Unrecognized URI Type Exception occured while retrieving configuration", _context_path));
			uute.printStackTrace(System.out);
			return _parameters;
		}
		catch(UnknownRepositoryException ure)
		{
			_parameters.put(REST_STATUS, REST_STATUS_ERROR);
			_parameters.put(REST_RESULT, getErrorMessageHTML("Unknown Repository Exception occured while retrieving configuration", _context_path));
			ure.printStackTrace(System.out);
			return _parameters;
		}
		catch(IOException ioe)
		{
			_parameters.put(REST_STATUS, REST_STATUS_ERROR);
			_parameters.put(REST_RESULT, getErrorMessageHTML("I/O Exception occured while retrieving configuration", _context_path));
			ioe.printStackTrace(System.out);
			return _parameters;
		}
		catch(IncorrectParameterSpecificationException ipse)
		{
			_parameters.put(REST_STATUS, REST_STATUS_ERROR);
			_parameters.put(REST_RESULT, getErrorMessageHTML("Incorrect Parameter Specification Exception occured while retrieving configuration", _context_path));
			ipse.printStackTrace(System.out);
			return _parameters;
		}
		catch(ConfigurationException ce)
		{
			_parameters.put(REST_STATUS, REST_STATUS_ERROR);
			_parameters.put(REST_RESULT, getErrorMessageHTML("Configuration Exception occured while retrieving configuration", _context_path));
			ce.printStackTrace(System.out);
			return _parameters;
		}
		catch(AccessDeniedException ade)
		{
			_parameters.put(REST_STATUS, REST_STATUS_ERROR);
			_parameters.put(REST_RESULT, getErrorMessageHTML("Access Denied Exception occured while retrieving configuration", _context_path));
			ade.printStackTrace(System.out);
			return _parameters;
		}
		catch(ClassNotFoundException cnfe)
		{
			_parameters.put(REST_STATUS, REST_STATUS_ERROR);
			_parameters.put(REST_RESULT, getErrorMessageHTML("ClassNotFoundException occured while retrieving configuration.", _context_path));
			cnfe.printStackTrace(System.out);
			return _parameters;
		}
		catch(IllegalAccessException iae)
		{
			_parameters.put(REST_STATUS, REST_STATUS_ERROR);
			_parameters.put(REST_RESULT, getErrorMessageHTML("IllegalAccessException occured while retrieving configuration.", _context_path));
			iae.printStackTrace(System.out);
			return _parameters;
		}
		catch(InstantiationException ie)
		{
			_parameters.put(REST_STATUS, REST_STATUS_ERROR);
			_parameters.put(REST_RESULT, getErrorMessageHTML("InstantiationException occured while retrieving configuration.", _context_path));
			ie.printStackTrace(System.out);
			return _parameters;
		}

//		catch(Exception e)
//		{
//			_parameters.put(REST_STATUS, REST_STATUS_ERROR);
//			_parameters.put(REST_RESULT, getErrorMessageHTML(e.getMessage(), _context_path));
//			e.printStackTrace(System.out);
//			return _parameters;
//		}
		
		
		_parameters = formatPServiceConfInfo(_parameters, conf, request);
		
		return _parameters;
	}

	private static Map<String, String> formatPServiceConfInfo(Map<String, String> _parameters,
			Configuration _conf, HttpServletRequest request)
	{
		String _context_path = _parameters.get(REST_CONTEXT_PATH);
		String service_id = _parameters.get(REST_SERVICE_ID);
		
		PServiceItem pi = PServiceEngineDaemon.getInstance().getPServiceList().findByTitle(service_id);
		String pserviceName = pi.getName();//_parameters.get(REST_SERVICE_NAME);
		
		String uris = "";
		for(Iterator<Entry<String,URI>> it=_conf.uris.entrySet().iterator(); it.hasNext(); )
		{
			Entry<String,URI> entry = it.next();
			uris += "<tr><td>&nbsp;&nbsp;&bull;&nbsp;" + entry.getKey() + "</td><td style='white-space:nowrap'>" + entry.getValue() + "</td></tr>\n";
		}// align='right'
		
//		String uri_list = "";
//		for(Iterator<URI> it=_conf.uri_list.iterator(); it.hasNext(); )
//			uri_list += it.next().toString() + "&para;<br/>";
//		
//		String context_list = "";
//		for(Iterator<URI> it=_conf.context_list.iterator(); it.hasNext(); )
//			context_list += it.next().toString() + "&para;<br/>";
		
		String datasource_list = "";
		for(Iterator<PServiceDataSource> it=_conf.datasource_list.iterator(); it.hasNext(); )
			datasource_list += it.next().toString() + "&para;<br/>";
		
		String rdf_list = "";
		for(Iterator<String> it=_conf.rdfs.iterator(); it.hasNext(); )
//			rdf_list += "<code>" + it.next().toString() + "</code>&para;<br/>";
//			rdf_list += "<![CDATA[" + it.next().toString() + "]]>&para;<br/>";
			rdf_list += "<code>" + it.next().toString().replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\n", "<br/>").replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;").replace(" ", "&nbsp;") + "</code>&para;<br/>";

		
		String home_html = "<a href='" + _context_path + "/' title='ADAPT&sup2; Personalization Services Home'>Home</a>";
		String pservices_html = "&nbsp;&raquo;&nbsp;<a href='" + _context_path + "/services' title='List of all PServices'>All PServices</a>";
		String pservice_html = "&nbsp;&raquo;&nbsp;<a href='" + _context_path + "/service/" + service_id + "' title='Back to PService'>PService</a>";
		String invoke_html = "&nbsp;&nbsp;&nbsp;<a href='" + _context_path + "/service/" + service_id + "/invoke/" + _conf.rdfID + "' title='Invoke'>Invoke</a>";
		String edit_html = "<a href='" + _context_path + "/service/" + service_id + "/conf/" + _conf.rdfID+ "/edit' title='Edit PService Configuration'>Edit</a>";
		
		String user_login = request.getRemoteUser();
		boolean isLoggedIn = (user_login!=null) && (user_login.length()>0);
		String loginout_url = (isLoggedIn)?user_login + "&nbsp;&nbsp;" + "<a href='" + request.getContextPath() + "/index.jsp?logout=1'>Log out</a>" :
			"<span style='color:#999999;'>You are not logged in</span>&nbsp;&nbsp;<a href='" + request.getContextPath() + "/home'>Log in</a>";
		
		String result = 
			getPageHeaderHTML("ADAPT&sup2; Personalization Services - PService Configuration", _context_path) +
			"<table cellpadding='2px' cellspacing='0px' class='brown_table' width='500px'>\n"+
			"	<caption style='text-align:right; padding:3px;' class='login_header'>" + loginout_url + "</caption>\n" + 
			"	<tr><td colspan='2' class='brown_table_caption'>PService Configuration</td></tr>\n"+
			"	<tr>\n"+
			"	  <td class='brown_table_header' colspan='2'>" + 
				home_html + pservices_html + pservice_html + "<div style='text-align:right;display:block;'>" + edit_html + invoke_html + "</div></td>\n" +
			"  	</tr>\n" +
			"	<tr>\n"+
			"		<td>Service</td>\n"+
			"		<td width='100%'>" + pserviceName + "</td>\n"+
			"	</tr>\n"+
			"	<tr>\n"+
			"		<td valign='top'>Config.</td>\n"+
			"		<td>" + _conf.name + "</td>\n"+
			"	</tr>\n"+
			"	<tr>\n"+
			"		<td valign='top'>Description</td>\n"+
			"		<td>" + _conf.description + "</td>\n"+
			"	</tr>\n"+
			"	<tr>\n"+
			"		<td valign='top'>URI&nbsp;List&nbsp;("+ _conf.uris.size() + ")</td>\n"+
			"		<td>&nbsp;</td>\n"+
			"	</tr>\n"+
			uris +
//			"	<tr>\n"+
//			"		<td valign='top'>URI&nbsp;List&nbsp;("+ _conf.uri_list.size() + ")</td>\n"+
//			"		<td>" + uri_list + "</td>\n"+
//			"	</tr>\n"+
//			"	<tr>\n"+
//			"		<td valign='top'>Context&nbsp;List&nbsp;("+ _conf.context_list.size() + ")</td>\n"+
//			"		<td>" + context_list + "</td>\n"+
//			"	</tr>\n"+
			"	<tr>\n"+
			"		<td valign='top'>Datasource&nbsp;List&nbsp;("+ _conf.datasource_list.size() + ")</td>\n"+
			"		<td style='white-space:nowrap'>" + datasource_list + "</td>\n"+
			"	</tr>\n"+
			"	<tr>\n"+
			"		<td valign='top'>RDF&nbsp;List&nbsp;("+ _conf.rdfs.size() + ")</td>\n"+
			"		<td style='white-space:nowrap'>" + rdf_list + "</td>\n"+
			"	</tr>\n"+
			"	<tr>\n"+
			"		<td>&nbsp;</td>\n"+
			"		<td>&nbsp;</td>\n"+
			"	</tr>\n"+
//			"	<tr>\n"+
//			"		<td class='brown_table_footer'><a href='" + _context_path + "/service/" + service_id + "/conf/" + conf_id + "'><input type='button' value='Cancel'/></a></td>\n"+
//			"		<td class='brown_table_footer' align='right'><input type='submit' value='Submit'/></td>\n"+
//			"	</tr>\n"+
			"</table>\n"+
			"</body></html>";

		_parameters.put(REST_STATUS, REST_STATUS_OK);
		_parameters.put(REST_RESULT, result);
		return _parameters;
	}
	
	public static Map<String, String> setPServiceInfo(Map<String, String> _parameters, SQLManager _sqlm, boolean multiple_pservices)
	{
		String service_id = _parameters.get(REST_SERVICE_ID);
		String _context_path = _parameters.get(REST_CONTEXT_PATH);

		if(multiple_pservices)
		{
			;//TODO multiple edit
		}
		else if(service_id != null && service_id.length() != 0)
		{// set pservice info
			
			String qry = "UPDATE ent_pservice SET rdfID='" + _parameters.get(REST_SERVICE_RDF_ID) + "',"+
					" Name='" + clrStr(_parameters.get(REST_SERVICE_NAME)) + "',"+
					" ClassName='" + clrStr(_parameters.get(REST_SERVICE_CLASSNAME)) + "',"+
					" Description='" + SQLManager.stringUnquote(clrStr(_parameters.get(REST_SERVICE_DESCRIPTION))) + "'"+
					" WHERE rdfID='" + service_id + "';";

			Connection conn = null;
			PreparedStatement stmt = null;
			try
			{// set user by login from db
				conn = _sqlm.getConnection();
//System.out.println("qry="+qry);
				qry = SQLManager.stringUnbreak(qry);
				stmt = conn.prepareStatement(qry);
				stmt.executeUpdate();
				
//				SQLManager.executeUpdate(conn, SQLManager.stringUnbreak(qry));
				
				String result = getMessageHTML("PService Info Changed",
						"<a href='"+_context_path+"/service/" + _parameters.get(REST_SERVICE_RDF_ID) +
						"'>Back</a> to PService", _context_path);
				_parameters.put(REST_STATUS, REST_STATUS_OK);
				_parameters.put(REST_RESULT, result);
				
				stmt.close();
				conn.close();
				stmt = null;
				conn = null;

			}// end of -- set user by login from db
			catch(SQLException sqle)
			{
				_parameters.put(REST_STATUS, REST_STATUS_ERROR);
				_parameters.put(REST_RESULT, getErrorMessageHTML("SQL Exception while updating PService info.", _context_path));
				sqle.printStackTrace(System.out);
			}
			finally
			{
				if (stmt != null) 
				{
					try { stmt.close(); } catch (SQLException e) { ; }
					stmt = null;
				}
				if (conn != null)
				{
					try { conn.close(); } catch (SQLException e) { ; }
					conn = null;
				}
			}
		}// end of -- set pservice info
		else
		{// no parameters
			_parameters.put(REST_STATUS, REST_STATUS_ERROR);
			_parameters.put(REST_RESULT, getErrorMessageHTML("PService id specified incorrectly", _context_path));
		}// end of -- no parameters
		
		return _parameters;
	}
	
	public static Map<String, String> getPServiceConfEditor(Map<String, String> _parameters,
			SQLManager _sqlm, HttpServletRequest _request)
	{
//		String service_id = _parameters.get(REST_SERVICE_ID);
		String conf_id = _parameters.get(REST_CONFIGURATION_ID);
		String context_path = _parameters.get(REST_CONTEXT_PATH);
		
		// Check p-service-config existance
		_parameters = checkPServiceConfViz(_parameters, _sqlm);
		
		String qry = "SELECT * FROM ent_config WHERE rdfID='" + conf_id + "';";
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{// retrieve user by login from db
			conn = _sqlm.getConnection();
			stmt = conn.prepareStatement(qry);
			rs = stmt.executeQuery();
			
			_parameters = formatPServiceConfEditor(rs, _parameters, conn, _request, _sqlm);

			rs.close();
			stmt.close();
			conn.close();
			rs = null;
			stmt = null;
			conn = null;
			
		}// end of -- retrieve user by login from db
		catch(SQLException sqle)
		{
			_parameters.put(REST_STATUS, REST_STATUS_ERROR);
			_parameters.put(REST_RESULT, getErrorMessageHTML("SQL Exception while retrieving p-service config info.", context_path));
			sqle.printStackTrace(System.out);
		}
		finally
		{
			if (rs != null)
			{
				try { rs.close(); } catch (SQLException e) { ; }
				rs = null;
			}
			if (stmt != null) 
			{
				try { stmt.close(); } catch (SQLException e) { ; }
				stmt = null;
			}
			if (conn != null)
			{
				try { conn.close(); } catch (SQLException e) { ; }
				conn = null;
			}
		}
		
		return _parameters;
	}

	private static Map<String, String> formatPServiceConfEditor(ResultSet _rs, Map<String, String> _parameters,
			Connection _conn, HttpServletRequest _request, SQLManager _sqlm) throws SQLException
	{
		String context_path = _parameters.get(REST_CONTEXT_PATH);
		String service_rdfID = _parameters.get(REST_SERVICE_ID);
		
		PServiceItem pi = PServiceEngineDaemon.getInstance().getPServiceList().findByTitle(service_rdfID);
		String service_name = pi.getName();//_parameters.get(REST_SERVICE_NAME);
		
		if(_rs.next())
		{// p-service exists
			String rdfID = _rs.getString("rdfID");
			String name = _rs.getString("Name");
			String description = _rs.getString("Description");
			
			// get datasources, uris, and rdf
			String qry_ds = "SELECT cds.* FROM ent_config_datasource cds JOIN ent_config c ON(cds.ConfigID=c.ConfigID) WHERE c.rdfID='" + rdfID + "';";
			String qry_uri = "SELECT cds.* FROM ent_config_uri cds JOIN ent_config c ON(cds.ConfigID=c.ConfigID) WHERE c.rdfID='" + rdfID + "';";
			String qry_rdf = "SELECT cds.* FROM ent_config_rdf cds JOIN ent_config c ON(cds.ConfigID=c.ConfigID) WHERE c.rdfID='" + rdfID + "';";
			Connection conn = null;
			PreparedStatement stmt_ds = null;
			ResultSet rs_ds = null;
			PreparedStatement stmt_uri = null;
			ResultSet rs_uri = null;
			PreparedStatement stmt_rdf = null;
			ResultSet rs_rdf = null;
		
			try
			{// retrieve user by login from db
				conn = _sqlm.getConnection();
				stmt_ds = conn.prepareStatement(qry_ds);
				rs_ds = stmt_ds.executeQuery();
				stmt_uri = conn.prepareStatement(qry_uri);
				rs_uri = stmt_uri.executeQuery();
				stmt_rdf = conn.prepareStatement(qry_rdf);
				rs_rdf = stmt_rdf.executeQuery();
				
				String result = "";
				
				String home_html = "<a href='" + context_path + "/' title='ADAPT&sup2; Personalization Services Home'>Home</a>";
				String service_html = "&nbsp;&raquo;&nbsp;<a href='" + context_path + "/service/" + service_rdfID + "' title='Back to PService'>PService</a>";
				String service_list_html = "&nbsp;&raquo;&nbsp;<a href='" + context_path + "/services' title='List of PServices'>All PServices</a>";
				String conf_html = "&nbsp;&raquo;&nbsp;<a href='" + context_path + "/service/" + service_rdfID + "/conf/" + rdfID + "' title='Back to Configuration of PService'>Conf.</a>";
	
				String user_login = _request.getRemoteUser();
				boolean isLoggedIn = (user_login!=null) && (user_login.length()>0);
				String loginout_url = (isLoggedIn)?user_login + "&nbsp;&nbsp;" + "<a href='" + _request.getContextPath() + "/index.jsp?logout=1'>Log out</a>" :
					"<span style='color:#999999;'>You are not logged in</span>&nbsp;&nbsp;<a href='" + _request.getContextPath() + "/home'>Log in</a>";
				
				result = 
					getPageHeaderHTML("ADAPT&sup2; Personalization Services - PService Info Editor", context_path) +
					"<form form method='post' action='" + context_path + "/service/" + service_rdfID + "/conf/" + rdfID + "'>\n"+
					"<input name='" + REST_CONFIGURATION_ID + "' type='hidden' value='" + rdfID + "'/>"+
					"<table cellpadding='2px' cellspacing='0px' class='brown_table' width='500px'>\n"+
					"	<caption style='text-align:right; padding:3px;' class='login_header'>" + loginout_url + "</caption>\n" + 
					"	<tr><td colspan='2' class='brown_table_caption'>" + name + " - Editing</td></tr>\n"+
					"	<tr>\n"+
					"	  <td class='brown_table_header' colspan='2'>" + 
						home_html + service_list_html + service_html + conf_html + "<div style='text-align:right;display:block;'>&nbsp;</div></td>\n"+
					"  	</tr>\n"+
					"	<tr>\n"+ 
					"		<td><strong>PService</strong></td>\n"+
					"		<td width='100%'>" + service_name + "</td>\n"+ 
					"	</tr>\n"+
					"	<tr>\n"+ 
					"		<td><strong>Configuration</strong></td>\n"+
					"		<td=>&nbsp;</td>\n"+ 
					"	</tr>\n"+
					"	<tr>\n"+ 
					"		<td>rdf ID</td>\n"+
					"		<td><input name='" +  REST_CONFIGURATION_RDF_ID + "' type='text' maxlength='50' size='25' value='" + rdfID + "'/>&nbsp;<font color='#FF0000'>*</font></td>\n"+ 
					"	</tr>\n"+
					"	<tr>\n"+
					"		<td>Name</td>\n"+
					"		<td><input name='" + REST_CONFIGURATION_NAME + "' type='text' maxlength='100' size='45' value='" + name + "'/></td>\n"+
					"	</tr>\n"+
					"	<tr>\n"+
					"		<td valign='top'>Description</td>\n"+
					"		<td><textarea name='" + REST_CONFIGURATION_DESCRIPTION + "' cols='46' rows='5'>" + clrStr(description) + "</textarea></td>\n"+
					"	</tr>\n"+
					"	<tr>\n"+
					"		<td></td>\n"+
					"		<td><font color='#FF0000'>*&nbsp;changing rdfID might have un-anticipated side effects</font></td>\n"+
					"	</tr>\n"+
					"	<tr>\n"+
					"		<td class='brown_table_footer'><a href='" + context_path + "/service/" + service_rdfID + "/conf/" + rdfID + "'><input type='button' value='Cancel'/></a></td>\n"+
					"		<td class='brown_table_footer' align='right'><input type='submit' value='Submit'/></td>\n"+
					"	</tr>\n"+
					"</table>\n</form>\n"+
					"</body></html>";
	
				_parameters.put(REST_STATUS, REST_STATUS_OK);
				_parameters.put(REST_RESULT, result);
			
				rs_ds.close();
				stmt_ds.close();
				rs_uri.close();
				stmt_uri.close();
				rs_rdf.close();
				stmt_rdf.close();
				conn.close();
				
				rs_ds = null;
				stmt_ds = null;
				rs_uri = null;
				stmt_uri = null;
				rs_rdf = null;
				stmt_rdf = null;
				conn = null;
				
			}// end of -- retrieve user by login from db
			catch(SQLException sqle)
			{
				_parameters.put(REST_STATUS, REST_STATUS_ERROR);
				_parameters.put(REST_RESULT, getErrorMessageHTML("SQL Exception while retrieving p-service config info.", context_path));
				sqle.printStackTrace(System.out);
			}
			finally
			{
				if (rs_ds != null)
				{
					try { rs_ds.close(); } catch (SQLException e) { ; }
					rs_ds = null;
				}
				if (stmt_ds != null) 
				{
					try { stmt_ds.close(); } catch (SQLException e) { ; }
					stmt_ds = null;
				}
				if (rs_uri != null)
				{
					try { rs_uri.close(); } catch (SQLException e) { ; }
					rs_uri = null;
				}
				if (stmt_uri != null) 
				{
					try { stmt_uri.close(); } catch (SQLException e) { ; }
					stmt_uri = null;
				}
				if (rs_rdf != null)
				{
					try { rs_rdf.close(); } catch (SQLException e) { ; }
					rs_rdf = null;
				}
				if (stmt_rdf != null) 
				{
					try { stmt_rdf.close(); } catch (SQLException e) { ; }
					stmt_rdf = null;
				}
				if (conn != null)
				{
					try { conn.close(); } catch (SQLException e) { ; }
					conn = null;
				}
			}
			
		}// end of -- pservice exists
		else
		{
			_parameters.put(REST_STATUS, REST_STATUS_ERROR);
			_parameters.put(REST_RESULT, getErrorMessageHTML("Specified p-service has not been found", context_path));
		}
		return _parameters;
	}

	private static String getPageHeaderHTML(String _title, String _context_path)
	{
		String result =
			"<!DOCTYPE HTML PUBLIC '-//W3C//DTD HTML 4.01//EN' 'http://www.w3.org/TR/html4/strict.dtd'>\n"+ 
			"<html><head>\n"+
			"<title>" + _title + "</title>\n"+
			"<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>\n"+
			"<link rel='StyleSheet' href='" + _context_path + "/assets/rest.css' type='text/css' />\n"+
			"<script type='text/javascript' src='" + _context_path + "/assets/rest.js'></script>\n"+
			"</head><body onload='opener.init(\"" + _context_path + "/assets/\");'>\n";
		
		return result;
	}
	
	public static String getPageHeaderRDF()
	{
		String result =
			"<?xml version='1.0' encoding='utf-8'?>\n" +
			"<rdf:RDF\n" +
			"		xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'\n" +
			"		xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'\n" +
			"		xmlns:foaf='http://xmlns.com/foaf/0.1/'\n" +
			"		xmlns:vCard='http://www.w3.org/2006/vcard/ns#'\n" +
			"		xmlns:dc='http://purl.org/dc/elements/1.1/'\n" +
			"		xmlns:dcterms='http://purl.org/dc/terms/'\n" +
			"		xmlns:rss='http://purl.org/rss/1.0/'>";
		
		return result;
	}
	
	public static String getErrorMessageHTML(String _message, String _context_path)
	{
		String result = 
			getPageHeaderHTML("ADAPT&sup2; PServices - Error", _context_path) +
			"<table cellpadding='0px' cellspacing='0px' class='burg_table'>"+
			"<tr>"+
			"	<td class='burg_table_caption'>Error</td>"+
			"</tr>"+
			"<tr>"+
			"	<td class='burg_table_message'>" + _message + "</td>"+
			"</tr>"+
			"</table></body></html>";
		
		return result;
	}
	
	public static String getMessageHTML(String _title, String _message, String _context_path)
	{
		String result = 
			getPageHeaderHTML("ADAPT&sup2; PServices - " + _title, _context_path) +
			"<table cellpadding='2px' cellspacing='0px' class	='green_table'>\n"+
			"<tr>\n"+
			"	<td class='green_table_caption'>" + _title + "</td>\n"+
			"</tr>\n"+
			"<tr><td class='green_table_message'>"+ _message + "</td></tr>\n"+
			"</table></body></html>";
		return result;
	}

	public static String clrStr(String _str)
	{
		return (_str == null || _str.length() == 0)?"":_str;
	}
}

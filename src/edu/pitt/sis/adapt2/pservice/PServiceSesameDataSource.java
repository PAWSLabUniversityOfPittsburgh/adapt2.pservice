package edu.pitt.sis.adapt2.pservice;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import org.openrdf.sesame.Sesame;
import org.openrdf.sesame.config.AccessDeniedException;
import org.openrdf.sesame.config.ConfigurationException;
import org.openrdf.sesame.config.UnknownRepositoryException;
import org.openrdf.sesame.constants.QueryLanguage;
import org.openrdf.sesame.query.MalformedQueryException;
import org.openrdf.sesame.query.QueryEvaluationException;
import org.openrdf.sesame.query.QueryResultsTable;
import org.openrdf.sesame.repository.SesameRepository;
import org.openrdf.sesame.repository.SesameService;

public class PServiceSesameDataSource extends PServiceDataSource
{
	private SesameRepository repository;
	
	public PServiceSesameDataSource(String _url, String _schema, String _user, String _pass, int _type) 
		throws IOException, AccessDeniedException, UnknownRepositoryException, ConfigurationException, IncorrectParameterSpecificationException
	{
		super(_url, _schema, _user, _pass, _type);
		// Create repository
//		try
//		{
			URL sesameServerURL = new java.net.URL(_url);
			SesameService service = Sesame.getService(sesameServerURL);
			service.login(_user, _pass);
			repository = service.getRepository(_schema);
//		}
//		catch(IOException ioe) { ioe.printStackTrace(System.out);}
//		catch(AccessDeniedException ade) { ade.printStackTrace(System.out);}
//		catch(UnknownRepositoryException ure) { ure.printStackTrace(System.out);}
//		catch(ConfigurationException ce) { ce.printStackTrace(System.out);}
		
	}
	
	@Override
	public String executeRDFQuery(String _qry)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<ArrayList<String>> executeTableQuery(String _qry)
			throws IOException, AccessDeniedException, MalformedQueryException, QueryEvaluationException
	{
		// Run query
		QueryResultsTable rs = null;
//		try
//		{
			rs = repository.performTableQuery(QueryLanguage.SERQL, _qry);
//		}
//		catch(IOException ioe) { ioe.printStackTrace(System.out);}
//		catch(AccessDeniedException ade) { ade.printStackTrace(System.out);}
//		catch(MalformedQueryException mqe) { mqe.printStackTrace(System.out);}
//		catch(QueryEvaluationException qee) { qee.printStackTrace(System.out);}
		
		// Compose results
		int rowCount = rs.getRowCount();
		int colCount = rs.getColumnCount();
//System.out.println("Query row:col=" + rowCount + ":" + colCount);		
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		for (int row = 0; row < rowCount; row++)
		{
			ArrayList<String> _result_row = new ArrayList<String>();
			for (int col = 0; col < colCount; col++)
			{
				_result_row.add(rs.getValue(row, col).toString());
			}
			result.add(_result_row);
		}
		return result;
	}

	public String toString()
	{
		return "" + user + ":" + pass + "@" + url + "::" + schema;
	}
}

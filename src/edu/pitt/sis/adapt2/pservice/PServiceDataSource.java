package edu.pitt.sis.adapt2.pservice;

import java.io.IOException;
import java.util.ArrayList;

import org.openrdf.sesame.config.AccessDeniedException;
import org.openrdf.sesame.query.MalformedQueryException;
import org.openrdf.sesame.query.QueryEvaluationException;

public abstract class PServiceDataSource
{
	// CONATSNTS
	public static final int PSERVICE_DATASOURCE_SESAME = 1;
	public static final int PSERVICE_DATASOURCE_JENADB = 2;
//	private static final int PSERVICE_DATASOURCE_LAST = 2; // UPDATE AS YOU ADD TYPES
	
	public String url;
	public String schema;
	public String user;
	public String pass;
	public int type;
	
	
	public PServiceDataSource(String _url, String _schema, String _user, String _pass, int _type) throws IncorrectParameterSpecificationException
	{
		if((_url == null) || (_url.length() == 0) || 
				(_schema == null) || (_schema.length() == 0) || 
				(_user == null) || (_pass == null) /*|| 
				(type <1 ) || (type > PSERVICE_DATASOURCE_LAST)*/ )
			throw(new IncorrectParameterSpecificationException("PServiceDataSource:: Parameters specified incorrectly"));
		url = _url;
		schema = _schema;
		user = _user;
		pass = _pass;
		type = _type;
	}
	
	public abstract ArrayList<ArrayList<String>> executeTableQuery(String _qry)
			throws IOException, AccessDeniedException, MalformedQueryException, QueryEvaluationException;
	public abstract String executeRDFQuery(String _qry);
	
}


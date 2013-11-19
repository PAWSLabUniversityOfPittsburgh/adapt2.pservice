package edu.pitt.sis.adapt2.pservice.datamodel;

public class PerformanceTraceDetailItem
{
	public int request_id;
	public int idx;
	public String name;
	public long st;
	public long fi;
	public long co;
	public int sz;
	public String unit;
	
	public PerformanceTraceDetailItem(int _request_id, int _idx, String _name, long _st, long _fi, long _co, int _sz, String _unit)
	{
		request_id = _request_id;
		idx = _idx;
		name = _name;
		st = _st;
		fi = _fi;
		co = _co;
		sz = _sz;
		unit = _unit;
	}
	
	public static String getColumnNames()
	{
		return "(RequestID, Idx, DetailName, DetailSt, DetailFi, DetailCo, DetailSz, DetailUnit)";
	}
	
	public String toString()
	{
		return "(" + request_id + "," + idx + ",'" + name + "'," + st + "," + fi + "," + co + "," + sz + ",'" + unit + "')";
	}
}

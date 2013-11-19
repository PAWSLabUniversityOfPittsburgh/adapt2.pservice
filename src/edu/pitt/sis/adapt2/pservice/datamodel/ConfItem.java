package edu.pitt.sis.adapt2.pservice.datamodel;

import java.io.Serializable;
import edu.pitt.sis.adapt2.pservice.Configuration;
import edu.pitt.sis.paws.core.Item;
import edu.pitt.sis.paws.core.ItemVector;
import edu.pitt.sis.paws.core.iItem;

public class ConfItem extends Item implements iItem, Serializable
{
	static final long serialVersionUID = 30L;
	
	private Configuration conf;
	private ItemVector<PServiceItem> psitems;
	
	public ConfItem()
	{
		super();
		conf = null;
		psitems = new ItemVector<PServiceItem>();
	}
	
	public ConfItem(int _id, String _title)
	{
		super(_id,_title);
		conf = null;
		psitems = new ItemVector<PServiceItem>();
	}

	public ConfItem(int _id, String _title, Configuration _conf)
	{
		super(_id,_title);
		conf = _conf;
		psitems = new ItemVector<PServiceItem>();
	}

	public Configuration getConf() { return conf; }
	public void setConf(Configuration _conf) { conf=_conf; }
	
	public ItemVector<PServiceItem> getPSItems() { return psitems; }
	
	public String toString()
	{
		return "ConfItem:: id:" + this.getId() + " title:" + this.getTitle() + " conf==null:" + (conf==null);
	}
}

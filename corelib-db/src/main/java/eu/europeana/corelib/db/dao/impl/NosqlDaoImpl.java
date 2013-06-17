package eu.europeana.corelib.db.dao.impl;

import java.io.Serializable;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.dao.BasicDAO;
import com.mongodb.ReadPreference;
import com.mongodb.ReadPreference.TaggedReadPreference;

import eu.europeana.corelib.db.dao.NosqlDao;
import eu.europeana.corelib.db.entity.nosql.abstracts.NoSqlEntity;

public class NosqlDaoImpl<E extends NoSqlEntity, T extends Serializable> extends BasicDAO<E, T> implements NosqlDao<E, T> {

	private Class<E> clazz;
	
	public NosqlDaoImpl(Datastore datastore, Class<E> clazz) {
		super(clazz, datastore);
		datastore.getDB().slaveOk();
		this.clazz = clazz;
	}
	
	@Override
	public void deleteAll() {
		try {
			delete(clazz.newInstance());
		} catch (Exception e) {
		}
	}

}

package br.com.pnpa.lazierdroid.model.helper;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import br.com.pnpa.lazierdroid.R;
import br.com.pnpa.lazierdroid.entities.Episodio;
import br.com.pnpa.lazierdroid.entities.Serie;
import br.com.pnpa.lazierdroid.entities.Temporada;
import br.com.pnpa.lazierdroid.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
	// name of the database file for your application -- change to something
	// appropriate for your app
	private static final String DATABASE_NAME = "lazierDroid.db";
	// any time you make changes to your database objects, you may have to
	// increase the database version
	private static final int DATABASE_VERSION = 13;

	// the DAO object we use to access the Serie table
	private Dao<Serie, Integer> serieDao = null;
	private Dao<Temporada, Integer> temporadaDao = null;
	private Dao<Episodio, Integer> episodioDao = null;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
	}

	/**
	 * This is called when the database is first created. Usually you should
	 * call createTable statements here to create the tables that will store
	 * your data.
	 */
	@Override
		
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		Log.i("onCreate");
		try {
			TableUtils.createTable(connectionSource, Serie.class);
			TableUtils.createTable(connectionSource, Temporada.class);
			TableUtils.createTable(connectionSource, Episodio.class);
		} catch (SQLException e) {
			Log.e("Can't create database", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * This is called when your application is upgraded and it has a higher
	 * version number. This allows you to adjust the various data to match the
	 * new version number.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
		try {
			Log.i("onUpgrade");
			if (oldVersion < DATABASE_VERSION) {
				TableUtils.dropTable(connectionSource, Episodio.class, true);
				TableUtils.dropTable(connectionSource, Temporada.class, true);
				TableUtils.dropTable(connectionSource, Serie.class, true);
				// after we drop the old databases, we create the new ones
				onCreate(db, connectionSource);
			}
		} catch (SQLException e) {
			Log.e("Can't drop databases", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns the Database Access Object (DAO) for our Serie class. It will
	 * create it or just give the cached value.
	 */
	public Dao<Serie, Integer> getSerieDao() throws SQLException {
		if (serieDao == null) {
			serieDao = getDao(Serie.class);
		}
		return serieDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for our Serie class. It will
	 * create it or just give the cached value.
	 */
	public Dao<Temporada, Integer> getTemporadaDao() throws SQLException {
		if (temporadaDao == null) {
			temporadaDao = getDao(Temporada.class);
		}
		return temporadaDao;
	}

	/**
	 * Returns the Database Access Object (DAO) for our Serie class. It will
	 * create it or just give the cached value.
	 */
	public Dao<Episodio, Integer> getEpisodioDao() throws SQLException {
		if (episodioDao == null) {
			episodioDao = getDao(Episodio.class);
		}
		return episodioDao;
	}

	/**
	 * Close the database connections and clear any cached DAOs.
	 */
	@Override
	public void close() {
		super.close();
		serieDao = null;
		temporadaDao = null;
		episodioDao = null;
	}
}
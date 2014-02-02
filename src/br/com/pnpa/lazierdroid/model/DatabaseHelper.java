package br.com.pnpa.lazierdroid.model;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import br.com.pnpa.lazierdroid.R;
import br.com.pnpa.lazierdroid.entities.Episodio;
import br.com.pnpa.lazierdroid.entities.Serie;
import br.com.pnpa.lazierdroid.entities.Temporada;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
        // name of the database file for your application -- change to something appropriate for your app
        private static final String DATABASE_NAME = "lazierDroid.db";
        // any time you make changes to your database objects, you may have to increase the database version
        private static final int DATABASE_VERSION = 1;

        // the DAO object we use to access the Serie table
        private Dao<Serie, Long> simpleDao = null;
        private RuntimeExceptionDao<Serie, Long> simpleRuntimeDao = null;

        public DatabaseHelper(Context context) {
                super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
        }

        /**
         * This is called when the database is first created. Usually you should call createTable statements here to create
         * the tables that will store your data.
         */
        @Override
        public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
                try {
                        Log.i(DatabaseHelper.class.getName(), "onCreate");
                        TableUtils.createTable(connectionSource, Serie.class);
                        TableUtils.createTable(connectionSource, Temporada.class);
                        TableUtils.createTable(connectionSource, Episodio.class);
                } catch (SQLException e) {
                        Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
                        throw new RuntimeException(e);
                }

//                // here we try inserting data in the on-create as a test
//                RuntimeExceptionDao<Serie, Long> dao = getSerieDao();
//                long millis = System.currentTimeMillis();
//                // create some entries in the onCreate
//                Serie simple = new Serie(millis);
//                dao.create(simple);
//                simple = new Serie(millis + 1);
//                dao.create(simple);
//                Log.i(DatabaseHelper.class.getName(), "created new entries in onCreate: " + millis);
        }

        /**
         * This is called when your application is upgraded and it has a higher version number. This allows you to adjust
         * the various data to match the new version number.
         */
        @Override
        public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
                try {
                	if(oldVersion < 2) {
                		Log.i(DatabaseHelper.class.getName(), "onUpgrade");
                        TableUtils.dropTable(connectionSource, Serie.class, true);
                        // after we drop the old databases, we create the new ones
                        onCreate(db, connectionSource);
                	}
                } catch (SQLException e) {
                        Log.e(DatabaseHelper.class.getName(), "Can't drop databases", e);
                        throw new RuntimeException(e);
                }
        }

        /**
         * Returns the Database Access Object (DAO) for our Serie class. It will create it or just give the cached
         * value.
         */
        public Dao<Serie, Long> getDao() throws SQLException {
                if (simpleDao == null) {
                        simpleDao = getDao(Serie.class);
                }
                return simpleDao;
        }

        /**
         * Returns the RuntimeExceptionDao (Database Access Object) version of a Dao for our Serie class. It will
         * create it or just give the cached value. RuntimeExceptionDao only through RuntimeExceptions.
         */
        public RuntimeExceptionDao<Serie, Long> getSerieDao() {
                if (simpleRuntimeDao == null) {
                        simpleRuntimeDao = getRuntimeExceptionDao(Serie.class);
                }
                return simpleRuntimeDao;
        }

        /**
         * Close the database connections and clear any cached DAOs.
         */
        @Override
        public void close() {
                super.close();
                simpleDao = null;
                simpleRuntimeDao = null;
        }
}
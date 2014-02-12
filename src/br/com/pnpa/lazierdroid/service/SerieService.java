package br.com.pnpa.lazierdroid.service;

import java.sql.SQLException;
import java.util.concurrent.Callable;

import android.util.Log;
import br.com.pnpa.lazierdroid.entities.Episodio;
import br.com.pnpa.lazierdroid.entities.Serie;
import br.com.pnpa.lazierdroid.entities.Temporada;
import br.com.pnpa.lazierdroid.model.helper.DatabaseHelper;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;

public class SerieService extends BaseService {
	public static Serie incluirSerie(final Serie serie, DatabaseHelper helper)
			throws SQLException {
		final Dao<Serie, Integer> daoSerie = helper.getSerieDao();
		final Dao<Temporada, Integer> daoTemporada = helper.getTemporadaDao();
		final Dao<Episodio, Integer> daoEpisodio = helper.getEpisodioDao();

		TransactionManager.callInTransaction(daoSerie.getConnectionSource(),
				new Callable<Serie>() {
					@Override
					public Serie call() throws Exception {
						daoSerie.createOrUpdate(serie);
						for (Temporada temporada : serie.getTemporadas()) {
							daoTemporada.create(temporada);
							for (Episodio episodio : temporada.getEpisodios()) {
								daoEpisodio.create(episodio);
							}
						}
						return null;
					}
				});

//		daoSerie.createOrUpdate(serie);

		Log.i(DatabaseHelper.class.getName(),
				"created new entries in onCreate: " + serie);

		return daoSerie.queryForId(serie.getId());
	}
}

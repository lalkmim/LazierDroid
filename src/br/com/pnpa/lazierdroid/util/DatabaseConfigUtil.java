package br.com.pnpa.lazierdroid.util;

import br.com.pnpa.lazierdroid.entities.Episodio;
import br.com.pnpa.lazierdroid.entities.Serie;
import br.com.pnpa.lazierdroid.entities.Temporada;

import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;

public class DatabaseConfigUtil extends OrmLiteConfigUtil {
	private static final Class<?>[] classes = new Class[] { Serie.class,
			Temporada.class, Episodio.class };

	public static void main(String[] args) throws Exception {
		writeConfigFile("ormlite_config.txt", classes);
	}
}
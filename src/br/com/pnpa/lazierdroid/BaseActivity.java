package br.com.pnpa.lazierdroid;

import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import br.com.pnpa.lazierdroid.entities.Serie;
import br.com.pnpa.lazierdroid.service.DatabaseHelper;
import br.com.pnpa.lazierdroid.service.SeriePublicService;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;

public abstract class BaseActivity extends OrmLiteBaseActivity<DatabaseHelper> {
	protected static ArrayAdapter<String> buildSeriesAdapter(List<Serie> series, Context context) {
		String[] nomesSeries = new String[series.size()];
		Iterator<Serie> it = series.iterator();
		int i=0;
		
		while(it.hasNext()) {
			nomesSeries[i++] = it.next().getNome();
		}
		
		return new ArrayAdapter<String>(
				context, 
				android.R.layout.simple_list_item_multiple_choice,
				nomesSeries);
	}
	
	protected class SearchTask extends AsyncTask<String, Void, Void> {
		@Override
		protected Void doInBackground(String... nomeSerie) {
			try {
//				Button botaoPesquisar = (Button) findViewById(R.id.botao_pesquisar_serie);	
//				ListView listaSeries = (ListView) findViewById(R.id.lista_resultado_pesquisa_series);

				List<Serie> lista = SeriePublicService.pesquisaSerie(nomeSerie[0]);
				Log.d(this.getClass().getCanonicalName(), lista.toString());
			} catch (Exception e) {
				Log.e(this.getClass().getCanonicalName(), getString(R.string.erro_pesquisar_series), e);
			}
			return null;
		}
	}
}

package br.com.pnpa.lazierdroid;

import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import br.com.pnpa.lazierdroid.entities.Serie;
import br.com.pnpa.lazierdroid.model.DatabaseHelper;
import br.com.pnpa.lazierdroid.service.SeriePublicService;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;

public abstract class BaseActivity extends OrmLiteBaseActivity<DatabaseHelper> {
	protected static ArrayAdapter<Serie> buildSeriesAdapter(List<Serie> series, Context context) {
		String[] nomesSeries = new String[series.size()];
		long[] idsSeries = new long[series.size()];
		Iterator<Serie> it = series.iterator();
		int i=0;
		
		while(it.hasNext()) {
			Serie serie = it.next();
			nomesSeries[i] = serie.getNome();
			idsSeries[i++] = serie.getId();
		}
		
		return new ArrayAdapter<Serie>(
				context, 
				android.R.layout.simple_list_item_multiple_choice,
				series);
	}
	
	protected class PesquisaSeriesTask extends AsyncTask<String, Void, Void> {
		List<Serie> lista = null;
		
		@Override
		protected Void doInBackground(String... nomeSerie) {
			try {
				lista = SeriePublicService.pesquisaSerie(nomeSerie[0]);
			} catch (Exception e) {
				Log.e(this.getClass().getName(), getString(R.string.erro_pesquisar_series), e);
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			try {
				ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar_pesquisa_series);
				ListView listViewSeries = (ListView) findViewById(R.id.lista_resultado_pesquisa_series);
				Button botaoIncluir = (Button) findViewById(R.id.botao_incluir_series);
				
				ArrayAdapter<Serie> adapter = buildSeriesAdapter(lista, getApplicationContext());
				listViewSeries.setAdapter(adapter);
				adapter.notifyDataSetChanged();
				
				progressBar.setVisibility(View.INVISIBLE);
				listViewSeries.setVisibility(View.VISIBLE);
				botaoIncluir.setVisibility(View.VISIBLE);
			} catch(Exception e) {
				Log.e(this.getClass().getName(), getString(R.string.erro_pesquisar_series), e);
			}
		}
	}
	
	protected class IncluiSerieTask extends AsyncTask<Serie, Void, Void> {
		Serie serie = null;
		
		@Override
		protected Void doInBackground(Serie... _serie) {
			try {
				this.serie = SeriePublicService.pesquisaDetalhesSerie(_serie[0]);
			} catch(Exception e) {
				Log.e(this.getClass().getName(), getString(R.string.erro_pesquisar_detalhes_serie), e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
		}
	}
}

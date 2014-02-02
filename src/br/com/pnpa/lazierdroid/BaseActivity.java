package br.com.pnpa.lazierdroid;

import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
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
			String nomeSerie = it.next().getNome();
			nomesSeries[i++] = nomeSerie;
//			Log.d(BaseActivity.class.getName(), nomeSerie);
		}
		
		Log.d("context", context.toString());
		
		return new ArrayAdapter<String>(
				context, 
				android.R.layout.simple_list_item_1,
				nomesSeries);
	}
	
	protected class SearchTask extends AsyncTask<String, Void, Void> {
		List<Serie> lista = null;
		
		@Override
		protected Void doInBackground(String... nomeSerie) {
			try {
				lista = SeriePublicService.pesquisaSerie(nomeSerie[0]);
//				Log.d(this.getClass().getName(), lista.toString());
			} catch (Exception e) {
				Log.e(this.getClass().getName(), getString(R.string.erro_pesquisar_series), e);
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			try {
				ProgressBar progressBar = (ProgressBar) findViewById(R.id.spinner_pesquisa_series);
				ListView listViewSeries = (ListView) findViewById(R.id.lista_resultado_pesquisa_series);
				
				ArrayAdapter<String> adapter = buildSeriesAdapter(lista, getApplicationContext());
				listViewSeries.setAdapter(adapter);
				adapter.notifyDataSetChanged();
				
				progressBar.setVisibility(View.INVISIBLE);
				listViewSeries.setVisibility(View.VISIBLE);
			} catch(Exception e) {
				Log.e(this.getClass().getName(), getString(R.string.erro_pesquisar_series), e);
			}
		}
	}
}

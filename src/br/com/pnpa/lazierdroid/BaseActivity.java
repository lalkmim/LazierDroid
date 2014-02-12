package br.com.pnpa.lazierdroid;

import java.sql.SQLException;
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
import br.com.pnpa.lazierdroid.model.helper.DatabaseHelper;
import br.com.pnpa.lazierdroid.service.SeriePublicService;
import br.com.pnpa.lazierdroid.service.SerieService;
import br.com.pnpa.lazierdroid.util.Util;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;

public abstract class BaseActivity extends OrmLiteBaseActivity<DatabaseHelper> {
	protected static ArrayAdapter<Serie> buildSeriesAdapter(List<Serie> series, Context context) {
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
				this.serie = SeriePublicService.pesquisaDetalhesSerie(_serie[0], getHelper());				
			} catch(Exception e) {
				Log.e(this.getClass().getName(), getString(R.string.erro_pesquisar_detalhes_serie), e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			try {
				super.onPostExecute(result);
				SerieService.incluirSerie(this.serie, getHelper());
				Util.buildToast(getApplicationContext(), getString(R.string.msg_sucesso_inclusao_series)).show();
			} catch (SQLException e) {
				Log.e("ERROR", getString(R.string.erro_gravar_dados_serie), e);
			}
		}
	}
}

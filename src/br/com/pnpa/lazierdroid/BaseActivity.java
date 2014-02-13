package br.com.pnpa.lazierdroid;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import br.com.pnpa.lazierdroid.entities.Serie;
import br.com.pnpa.lazierdroid.model.helper.DatabaseHelper;
import br.com.pnpa.lazierdroid.service.SeriePublicService;
import br.com.pnpa.lazierdroid.service.SerieService;
import br.com.pnpa.lazierdroid.util.Util;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;

public abstract class BaseActivity extends OrmLiteBaseActivity<DatabaseHelper> {
	protected static ArrayAdapter<Serie> buildPesquisarSeriesAdapter(List<Serie> series, Context context) {
		return new ArrayAdapter<Serie>(
				context, 
				android.R.layout.simple_list_item_multiple_choice,
				series);
	}
	
	protected static SimpleAdapter buildMinhasSeriesAdapter(List<Serie> series, Context context) {
		List<Map<String, String>> dados = new ArrayList<Map<String, String>>();
		for(Serie serie : series) {
			Map<String, String> item = new HashMap<String, String>();
			item.put("nome", serie.getNome());
			item.put("detalhes", "Temporadas: " + serie.getNumeroTemporadas());
			dados.add(item);
		}
		
		return new SimpleAdapter(context, dados, android.R.layout.simple_list_item_2, new String[] {"nome", "detalhes"}, new int[] {android.R.id.text1, android.R.id.text2});
	}
	
	protected class PesquisaSeriesTask extends AsyncTask<String, Void, Void> {
		List<Serie> lista = null;
		
		@Override
		protected Void doInBackground(String... nomeSerie) {
			try {
				lista = SeriePublicService.pesquisaSerie(nomeSerie[0]);
			} catch (Exception e) {
				Log.e(this.getClass().getName(), getString(R.string.msg_erro_pesquisar_series), e);
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
				
				ArrayAdapter<Serie> adapter = buildPesquisarSeriesAdapter(lista, getApplicationContext());
				listViewSeries.setAdapter(adapter);
				adapter.notifyDataSetChanged();
				
				progressBar.setVisibility(View.INVISIBLE);
				listViewSeries.setVisibility(View.VISIBLE);
				botaoIncluir.setVisibility(View.VISIBLE);
			} catch(Exception e) {
				Log.e(this.getClass().getName(), getString(R.string.msg_erro_pesquisar_series), e);
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
				Log.e(this.getClass().getName(), getString(R.string.msg_erro_pesquisar_detalhes_serie), e);
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
				Log.e("ERROR", getString(R.string.msg_erro_gravar_dados_serie), e);
			}
		}
	}
	
	protected class CarregaMinhasSeriesTask extends AsyncTask<Serie, Void, Void> {
		Serie serie = null;
		
		@Override
		protected Void doInBackground(Serie... _serie) {
			try {
				this.serie = SeriePublicService.pesquisaDetalhesSerie(_serie[0], getHelper());				
			} catch(Exception e) {
				Log.e(this.getClass().getName(), getString(R.string.msg_erro_pesquisar_detalhes_serie), e);
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
				Log.e("ERROR", getString(R.string.msg_erro_gravar_dados_serie), e);
			}
		}
	}
}

package br.com.pnpa.lazierdroid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import br.com.pnpa.lazierdroid.adapter.TemporadasEpisodiosAdapter;
import br.com.pnpa.lazierdroid.entities.Serie;
import br.com.pnpa.lazierdroid.entities.Temporada;
import br.com.pnpa.lazierdroid.model.helper.DatabaseHelper;
import br.com.pnpa.lazierdroid.service.SerieService;
import br.com.pnpa.lazierdroid.util.Util;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;

public abstract class BaseActivity extends OrmLiteBaseActivity<DatabaseHelper> {
	protected static final String NOME = "nome";
	protected static final String DETALHES = "detalhes";
	
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
			
			int episodios = 0;
			for(Temporada temporada : serie.getTemporadas()) {
				episodios = episodios + temporada.getEpisodios().size();
			}
			
			item.put(NOME, serie.getNome());
			item.put(DETALHES, "Temporadas: " + serie.getTemporadas().size() + " - Episódios: " + episodios);
			
			dados.add(item);
		}
		
		return new SimpleAdapter(context, dados, android.R.layout.simple_list_item_2, new String[] {NOME, DETALHES}, new int[] {android.R.id.text1, android.R.id.text2});
	}
	
	protected TemporadasEpisodiosAdapter buildDetalhesSerieTemporadasAdapter(Serie serie, Context context) {
		List<Map<String, String>> dados = new ArrayList<Map<String, String>>();
		for(Temporada temporada : serie.getTemporadas()) {
			Map<String, String> item = new HashMap<String, String>();
			
			item.put(NOME, "Temporada " + temporada.getNumero());
			item.put(DETALHES, "Episódios: " + temporada.getEpisodios().size());
			
			dados.add(item);
		}
		
//		return new SimpleAdapter(context, dados, android.R.layout.simple_list_item_2, new String[] {NOME, DETALHES}, new int[] {android.R.id.text1, android.R.id.text2});
		return new TemporadasEpisodiosAdapter(context, new ArrayList<Temporada>(serie.getTemporadas()));
	}
	
	protected class PesquisaSeriesTask extends AsyncTask<String, Void, Void> {
		List<Serie> lista = null;
		
		@Override
		protected Void doInBackground(String... nomeSerie) {
			try {
				lista = SerieService.pesquisaSerie(nomeSerie[0]);
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
				this.serie = SerieService.pesquisaDetalhesSerie(_serie[0], getHelper());				
			} catch(Exception e) {
				Log.e(this.getClass().getName(), getString(R.string.msg_erro_pesquisar_detalhes_serie), e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			try {
				super.onPostExecute(result);
				Util.buildToast(getApplicationContext(), getString(R.string.msg_sucesso_inclusao_series)).show();
			} catch (Exception e) {
				Log.e("ERROR", getString(R.string.msg_erro_gravar_dados_serie), e);
			}
		}
	}
	
	protected class LoadExternalImageTask extends AsyncTask<Integer, Void, Void> {
		Integer idSerie;
		Integer idView;
		Bitmap imagem;

		@Override
		protected Void doInBackground(Integer... params) {
			try {
				idSerie = params[0];
				idView = params[1];
				
				Serie serie = getHelper().getSerieDao().queryForId(idSerie);
				imagem = Util.loadImageBitmap(serie.getImageURL());
			} catch (Exception e) {
				String msgErro = getString(R.string.msg_erro_carregar_imagem_externa);
				Log.e("ERROR", msgErro, e);
				Util.buildToast(getApplicationContext(), msgErro);
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			try {
				super.onPostExecute(result);
				ImageView imagemDetalheSerie = (ImageView) findViewById(idView);
				imagemDetalheSerie.setImageBitmap(imagem);
			} catch(Exception e) {
				String msgErro = getString(R.string.msg_erro_carregar_imagem_externa);
				Log.e("ERROR", msgErro, e);
				Util.buildToast(getApplicationContext(), msgErro);
			}
		}
	}
}

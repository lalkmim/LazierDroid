package br.com.pnpa.lazierdroid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import br.com.pnpa.lazierdroid.R;
import br.com.pnpa.lazierdroid.entities.Episodio;
import br.com.pnpa.lazierdroid.entities.LegendaFile;
import br.com.pnpa.lazierdroid.entities.Serie;
import br.com.pnpa.lazierdroid.entities.Temporada;
import br.com.pnpa.lazierdroid.entities.TorrentFile;
import br.com.pnpa.lazierdroid.model.helper.DatabaseHelper;
import br.com.pnpa.lazierdroid.services.LegendaService;
import br.com.pnpa.lazierdroid.services.SerieService;
import br.com.pnpa.lazierdroid.services.TTorrentService;
import br.com.pnpa.lazierdroid.services.TorrentService;
import br.com.pnpa.lazierdroid.util.Log;
import br.com.pnpa.lazierdroid.util.Util;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.SharedTorrent;

public abstract class BaseActivity extends OrmLiteBaseActivity<DatabaseHelper> {
	protected static final String NOME = "nome";
	protected static final String DETALHES = "detalhes";

	protected static ArrayAdapter<Serie> buildPesquisarSeriesAdapter(List<Serie> series, Context context) {
		return new ArrayAdapter<Serie>(
			context, 
			android.R.layout.simple_list_item_multiple_choice, 
			series
		);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = new Intent();
		intent.setClass(this, ConfigActivity.class);
		startActivityForResult(intent, 0);

		return true;
	}

	protected static SimpleAdapter buildMinhasSeriesAdapter(List<Serie> series, Context context) {
		List<Map<String, String>> dados = new ArrayList<Map<String, String>>();
		for (Serie serie : series) {
			Map<String, String> item = new HashMap<String, String>();

			int episodios = 0;
			for (Temporada temporada : serie.getTemporadas()) {
				episodios = episodios + temporada.getEpisodios().size();
			}

			item.put(NOME, serie.getNome());
			item.put(DETALHES, "Temporadas: " + serie.getTemporadas().size() + " - Episódios: " + episodios);

			dados.add(item);
		}

		return new SimpleAdapter(
			context, 
			dados,
			android.R.layout.simple_list_item_2, 
			new String[] { NOME, DETALHES }, 
			new int[] { android.R.id.text1, android.R.id.text2 }
		);
	}

	protected TemporadasEpisodiosAdapter buildDetalhesSerieTemporadasAdapter(Serie serie, Context context) {
		List<Map<String, String>> dados = new ArrayList<Map<String, String>>();
		for (Temporada temporada : serie.getTemporadas()) {
			Map<String, String> item = new HashMap<String, String>();

			item.put(NOME, "Temporada " + temporada.getNumero());
			item.put(DETALHES, "Episódios: " + temporada.getEpisodios().size());

			dados.add(item);
		}

		return new TemporadasEpisodiosAdapter(context, new ArrayList<Temporada>(serie.getTemporadas()));
	}

	protected class PesquisaSeriesTask extends AsyncTask<String, Void, Void> {
		private List<Serie> lista = null;
		private Exception exception = null;

		@Override
		protected Void doInBackground(String... nomeSerie) {
			try {
				lista = SerieService.pesquisaSerie(nomeSerie[0]);
			} catch (Exception e) {
				this.exception = e;
				Log.e(getString(R.string.msg_erro_pesquisar_series), e);
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
			} catch (Exception e) {
				this.exception = e;
			}

			if (this.exception != null) {
				Log.d("exception: " + this.exception);
				Util.buildToast(getApplicationContext(), getString(R.string.msg_erro_pesquisar_series)).show();
			}
		}
	}

	protected class IncluiSerieTask extends AsyncTask<Serie, Void, Void> {
		Serie serie = null;

		@Override
		protected Void doInBackground(Serie... _serie) {
			try {
				this.serie = SerieService.pesquisaDetalhesSerie(_serie[0],
						getHelper());
			} catch (Exception e) {
				Log.e(getString(R.string.msg_erro_pesquisar_detalhes_serie), e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			try {
				super.onPostExecute(result);
				Util.buildToast(getApplicationContext(), getString(R.string.msg_sucesso_inclusao_series)).show();
			} catch (Exception e) {
				Log.e(getString(R.string.msg_erro_gravar_dados_serie), e);
			}
		}
	}

	protected class LoadExternalImageTask extends
			AsyncTask<Integer, Void, Void> {
		Integer idSerie;
		Integer idView;
		Bitmap imagem;

		@Override
		protected Void doInBackground(Integer... params) {
			try {
				idSerie = params[0];
				idView = params[1];

				Serie serie = getHelper().getSerieDao().queryForId(idSerie);
				Log.d("imageURL: " + serie.getImageURL());
				imagem = Util.loadImageBitmap(serie.getImageURL());
			} catch (Exception e) {
				String msgErro = getString(R.string.msg_erro_carregar_imagem_externa);
				Log.e(msgErro, e);
				Util.buildToast(getApplicationContext(), msgErro).show();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			try {
				super.onPostExecute(result);
				ImageView imagemDetalheSerie = (ImageView) findViewById(idView);
				imagemDetalheSerie.setImageBitmap(imagem);
			} catch (Exception e) {
				String msgErro = getString(R.string.msg_erro_carregar_imagem_externa);
				Log.e(msgErro, e);
				Util.buildToast(getApplicationContext(), msgErro).show();
			}
		}
	}

	protected class DownloadTorrentTask extends AsyncTask<Episodio, Void, Void> {
		private Exception exception = null;
		private Episodio episodio = null;
		
		@Override
		protected Void doInBackground(Episodio... params) {
			try {
				this.episodio = params[0];
				
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				
				Boolean modoHD = prefs.getBoolean("config_video_hd", false);
				String pastaTorrent = prefs.getString("config_torrent_folder", "");
				String pastaTemp = prefs.getString("config_temp_folder", "");
				
				if(!pastaTorrent.substring(pastaTorrent.length() - 1).equals("/"))
					pastaTorrent += "/";
				
				if(!pastaTemp.substring(pastaTemp.length() - 1).equals("/"))
					pastaTemp += "/";
				
				TorrentFile torrent = TorrentService.buscaTorrent(episodio, modoHD, pastaTorrent);
				
				episodio.setLinkTorrent(torrent.getLink());
				episodio.setCaminhoTorrent(torrent.getLocalFile().getCaminhoArquivo());
				
				getHelper().getEpisodioDao().update(episodio);
				Client client = TTorrentService.startTorrent(episodio, pastaTorrent);

				SharedTorrent sharedTorrent = client.getTorrent();
				while(client.getState().ordinal() < 4 && sharedTorrent.getCompletion() < 100) {
					Thread.sleep(10000);
				}
				sharedTorrent.stop();
				
				String nomeVideo = TorrentService.organizarArquivos(sharedTorrent, pastaTemp, pastaTorrent);
				nomeVideo = TorrentService.corrigirNomeVideo(torrent, nomeVideo, pastaTemp);
				
				this.episodio.setNomeVideo(nomeVideo);
				this.episodio.setCaminhoVideo(pastaTemp + nomeVideo);
			} catch (Exception e) {
				Log.e("Erro ao processar torrent.", e);
				this.exception = e;
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			
			try {
				if(this.exception == null) {
					getHelper().getEpisodioDao().update(this.episodio);
				}
			} catch (Exception e) {
				this.exception = e;
			}
			
			if(this.exception != null) {
				Log.d("exception: " + this.exception);
				Util.buildToast(getApplicationContext(), getString(R.string.msg_erro_baixar_torrent)).show();
			}
		}
	}
	
	protected class DownloadLegendaTask extends AsyncTask<Episodio, Void, Void> {
		private Exception exception = null;
		private Episodio episodio = null;
		
		@Override
		protected Void doInBackground(Episodio... params) {
			try {
				this.episodio = params[0];
				
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				String pastaTemp = prefs.getString("config_temp_folder", "");
				String pastaFinal = prefs.getString("config_video_folder", "");
				
				if(!pastaTemp.substring(pastaTemp.length() - 1).equals("/"))
					pastaTemp += "/";
				
				if(!pastaFinal.substring(pastaFinal.length() - 1).equals("/"))
					pastaFinal += "/";
				
				LegendaFile legenda = LegendaService.buscaLegenda(episodio, pastaTemp);
				
				this.episodio.setLinkLegenda(legenda.getLink());
				this.episodio.setCaminhoLegenda(legenda.getLocalFile().getCaminhoArquivo());
				this.episodio.setNomeLegenda(legenda.getFileName());
				
				LegendaService.organizarArquivos(episodio, pastaFinal);
				
				getHelper().getEpisodioDao().update(this.episodio);
			} catch (Exception e) {
				Log.e("Erro ao processar torrent.", e);
				this.exception = e;
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			
			if(this.exception != null) {
				Log.d("exception: " + this.exception);
				Util.buildToast(getApplicationContext(), getString(R.string.msg_erro_baixar_torrent)).show();
			}
		}
	}
	
	protected class TemporadasEpisodiosAdapter extends BaseExpandableListAdapter {
		private final List<Temporada> temporadas;
		private final LayoutInflater inflater;

		public TemporadasEpisodiosAdapter(Context context, List<Temporada> _temporadas) {
			this.inflater = LayoutInflater.from(context);
			this.temporadas = _temporadas;
		}

		@Override
		public Object getChild(int posTemporada, int posEpisodio) {
			return new ArrayList<Episodio>(temporadas.get(posTemporada).getEpisodios()).get(posEpisodio);
		}

		@Override
		public long getChildId(int posTemporada, int posEpisodio) {
			return new ArrayList<Episodio>(temporadas.get(posTemporada).getEpisodios()).get(posEpisodio).getId();
		}

		@Override
		public View getChildView(int posTemporada, int posEpisodio, boolean isLastChild, View convertView, ViewGroup parent) {
			View v = convertView;

			if (v == null) {
				v = inflater.inflate(R.layout.expandable_list_item_layout, parent, false);
			}

			TextView itemName = (TextView) v.findViewById(R.id.itemName);
			TextView itemDetalhes = (TextView) v.findViewById(R.id.itemDetalhes);
			TextView itemStatus = (TextView) v.findViewById(R.id.itemStatus);

			final Episodio episodio = (Episodio) temporadas.get(posTemporada).getEpisodios().toArray()[posEpisodio];

			itemName.setText(episodio.getTitle());
			itemDetalhes.setText(episodio.getNumeroFormatado() + " - " + episodio.getDate());
			if("OK".equals(episodio.getStatusLegenda())) {
				itemStatus.setText("Status: OK");
			} else if("OK".equals(episodio.getStatusVideo())) {
				itemStatus.setText("Status: Falta Legenda");
			} else {
				itemStatus.setText("Status: Falta Vídeo e Legenda");
			}
			
			Button botaoBaixar = (Button) v.findViewById(R.id.botao_baixar);
			Button botaoAssistir = (Button) v.findViewById(R.id.botao_assistir);
			
			if("OK".equals(episodio.getStatusVideo()) && "OK".equals(episodio.getStatusLegenda())) {
				botaoBaixar.setEnabled(false);
				botaoAssistir.setEnabled(true);
			} else {
				botaoBaixar.setEnabled(true);
				botaoAssistir.setEnabled(false);
			}
			
			botaoBaixar.setOnClickListener(new Button.OnClickListener() {
				public void onClick(View v) {
					if(episodio.getStatusVideo().equals("OK")) {
						new DownloadLegendaTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, episodio);
					} else {
						new DownloadTorrentTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, episodio);
					}
				}
			});
			
			botaoAssistir.setOnClickListener(new Button.OnClickListener() {
				public void onClick(View v) {
					// TODO Disparar action para abrir arquivo de vídeo
				}
			});
			
			return v;
		}

		@Override
		public int getChildrenCount(int posTemporada) {
			return temporadas.get(posTemporada).getEpisodios().size();
		}

		@Override
		public Object getGroup(int posTemporada) {
			return temporadas.get(posTemporada);
		}

		@Override
		public int getGroupCount() {
			return temporadas.size();
		}

		@Override
		public long getGroupId(int posTemporada) {
			return temporadas.get(posTemporada).getId();
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			View v = convertView;

			if (v == null) {
				v = inflater.inflate(R.layout.expandable_list_group_layout, null);
			}

			TextView groupTitulo = (TextView) v.findViewById(R.id.groupName);
			TextView groupDetalhes = (TextView) v.findViewById(R.id.groupDetalhes);

			Temporada temporada = temporadas.get(groupPosition);

			groupTitulo.setText("Temporada " + temporada.getNumero());
			groupDetalhes.setText("Episódios: " + temporada.getEpisodios().size());

			return v;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

	}
}

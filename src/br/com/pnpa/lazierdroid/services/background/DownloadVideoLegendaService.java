package br.com.pnpa.lazierdroid.services.background;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import br.com.pnpa.lazierdroid.entities.Episodio;
import br.com.pnpa.lazierdroid.entities.LegendaFile;
import br.com.pnpa.lazierdroid.entities.TorrentFile;
import br.com.pnpa.lazierdroid.model.helper.DatabaseHelper;
import br.com.pnpa.lazierdroid.services.LegendaService;
import br.com.pnpa.lazierdroid.services.TTorrentService;
import br.com.pnpa.lazierdroid.services.TorrentService;
import br.com.pnpa.lazierdroid.util.Log;
import br.com.pnpa.lazierdroid.util.Util;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.SharedTorrent;

public class DownloadVideoLegendaService extends IntentService {
	private Episodio episodio;
	
	public DownloadVideoLegendaService(String name) {
		super(name);
	}
	
	public DownloadVideoLegendaService() {
		super("RunTorrentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		try {
			Log.d("RunTorrentService started");
			this.episodio = (Episodio) intent.getExtras().get("episodio");
			
			DatabaseHelper helper = OpenHelperManager.getHelper(getApplicationContext(), DatabaseHelper.class);
			
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			
			Boolean modoHD = prefs.getBoolean("config_video_hd", false);
			String pastaTorrent = Util.formatConfigFolder(prefs.getString("config_torrent_folder", ""));
			String pastaTemp = Util.formatConfigFolder(prefs.getString("config_temp_folder", ""));
			String pastaFinal = Util.formatConfigFolder(prefs.getString("config_video_folder", ""));
			
			if(!"OK".equals(episodio.getStatusVideo())) {
				baixarVideo(helper, modoHD, pastaTorrent, pastaTemp);
			}
			
			if(!"OK".equals(episodio.getStatusLegenda())) {
				baixarLegenda(helper, pastaTemp, pastaFinal);
			}
			
			Log.d("RunTorrentService finished");
		} catch (Exception e) {
			Log.e("Erro ao processar torrent.", e);
		} finally {
			OpenHelperManager.releaseHelper();
		}
	}

	private void baixarLegenda(DatabaseHelper helper, String pastaTemp, String pastaFinal) throws Exception {
		LegendaFile legenda = LegendaService.buscaLegenda(episodio, pastaTemp);
		
		this.episodio.setLinkLegenda(legenda.getLink());
		this.episodio.setCaminhoLegenda(legenda.getLocalFile().getCaminhoArquivo());
		this.episodio.setNomeLegenda(legenda.getFileName());
		
		LegendaService.organizarArquivos(episodio, pastaFinal);
		
		helper.getEpisodioDao().update(this.episodio);
	}

	private void baixarVideo(DatabaseHelper helper, Boolean modoHD, String pastaTorrent, String pastaTemp) throws Exception {
		TorrentFile torrent = TorrentService.buscaTorrent(episodio, modoHD, pastaTorrent);
		
		episodio.setLinkTorrent(torrent.getLink());
		episodio.setCaminhoTorrent(torrent.getLocalFile().getCaminhoArquivo());
		
		helper.getEpisodioDao().update(episodio);
		Client client = TTorrentService.startTorrent(episodio, pastaTorrent);

		SharedTorrent sharedTorrent = client.getTorrent();
		while(client.getState().ordinal() < 4 && sharedTorrent.getCompletion() < 100) {
			Thread.sleep(10000);
		}
		client.stop();
		
		String nomeVideo = TorrentService.organizarArquivos(sharedTorrent, pastaTemp, pastaTorrent);
		nomeVideo = TorrentService.corrigirNomeVideo(torrent, nomeVideo, pastaTemp);
		
		this.episodio.setNomeVideo(nomeVideo);
		this.episodio.setCaminhoVideo(pastaTemp + nomeVideo);
		
		helper.getEpisodioDao().update(this.episodio);
	}
}

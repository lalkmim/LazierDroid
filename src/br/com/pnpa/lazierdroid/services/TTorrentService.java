package br.com.pnpa.lazierdroid.services;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;

import br.com.pnpa.lazierdroid.entities.Episodio;
import br.com.pnpa.lazierdroid.util.Log;

import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.SharedTorrent;

public class TTorrentService extends BaseService {
	public static Client startTorrent(Episodio episodio, String caminhoPastaTemporaria) throws UnknownHostException, IOException, NoSuchAlgorithmException {
		Log.d("caminho: " + caminhoPastaTemporaria);
		Log.d("episodio.caminhoTorrent: " + episodio.getCaminhoTorrent());
		Client client = new Client(
			InetAddress.getLocalHost(),
			SharedTorrent.fromFile(
				new File(episodio.getCaminhoTorrent()),
				new File(caminhoPastaTemporaria)));

		client.setMaxDownloadRate(50.0);
		client.setMaxUploadRate(50.0);

		Log.d("antes");
		client.download();
		Log.d("depois");
		
		return client;
	}
}

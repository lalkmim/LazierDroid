package br.com.pnpa.lazierdroid.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Pattern;

import javax.xml.xpath.XPathExpressionException;

import org.apache.http.client.ClientProtocolException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import br.com.pnpa.lazierdroid.entities.DownloadFile;
import br.com.pnpa.lazierdroid.entities.Episodio;
import br.com.pnpa.lazierdroid.entities.LazierFile;
import br.com.pnpa.lazierdroid.entities.TorrentFile;
import br.com.pnpa.lazierdroid.util.Log;
import br.com.pnpa.lazierdroid.util.Util;

import com.turn.ttorrent.client.SharedTorrent;

public class TorrentService extends BaseService {

	public static TorrentFile buscaTorrent(Episodio episodio, Boolean modoHD, String torrentFolder) throws Exception {
		List<TorrentFile> torrents = buscaUrlsTorrents(episodio, modoHD);
		
		int i = 0;
		TorrentFile torrent = null;
		while (i < torrents.size()) {
			torrent = torrents.get(i++);
			
			Log.d("torrent.fileName: " + torrent.getFileName());
			Log.d("torrent.link: " + Util.ajustarLink(torrent.getLink()));
			
			LazierFile arquivo = IOService.salvarArquivo(Util.ajustarLink(torrent.getLink()), torrentFolder + torrent.getFileName());
			if (arquivo == null) {
				torrent = null;
			} else {
				torrent.setLocalFile(arquivo);
				break;
			}
		}

		if(torrent == null)
			throw new Exception("Nenhum torrent retornado.");
		
		return torrent;
	}

	private static List<TorrentFile> buscaUrlsTorrents(Episodio episodio, Boolean modoHD) throws XPathExpressionException, ClientProtocolException, IOException {
		List<TorrentFile> torrents = new ArrayList<TorrentFile>();
		
		torrents.addAll(buscarUrlsTorrentsSimples(episodio, modoHD));
		torrents.addAll(buscaUrlsTorrentsComplexas(episodio, modoHD));
		
		return torrents;
	}

	private static List<TorrentFile> buscarUrlsTorrentsSimples(Episodio episodio, Boolean modoHD) throws ClientProtocolException, IOException, XPathExpressionException {
		String nomeBusca = episodio.getTemporada().getSerie().getNome().replaceAll(" ", ".");
		String codEpisodio = episodio.getNumeroFormatado();
		
		List<TorrentFile> torrents = new ArrayList<TorrentFile>();
		List<String> feedUrls = new ArrayList<String>();
		
		String urlKickassTo = "http://kickass.to/usearch/"
				+ URLEncoder.encode(nomeBusca, "UTF-8") + "%20"
				+ URLEncoder.encode(codEpisodio, "UTF-8") + "%20x264%20"
				+ URLEncoder.encode(modoHD ? "" : "-", "UTF-8")
				+ "720p%20verified:1/?field=seeders&sorder=desc&rss=1";
		
		String urlEztvIt = "https://ezrss.it/search/index.php?show_name="
				+ URLEncoder.encode(nomeBusca, "UTF-8").replaceAll("\\.", "+") + "&date=&quality="
				+ (modoHD ? "720P" : "HDTV&quality_exact=true") + "&release_group=&episode_title=&season="
				+ URLEncoder.encode(String.valueOf(episodio.getTemporada().getNumero()), "UTF-8") + "&episode="
				+ URLEncoder.encode(String.valueOf(episodio.getNumero()), "UTF-8") + "&video_format=&audio_format=&modifier=&mode=rss";
		
		feedUrls.add(urlKickassTo);
		feedUrls.add(urlEztvIt);
		
		for(int i=0; i<feedUrls.size(); i++) {
			TorrentFile torrent = null;
			Log.d("url: " + feedUrls.get(i));
			DownloadFile file = downloadFile(feedUrls.get(i));
			if(file == null) continue;
			
			InputStream is = file.getIs();
			String expression = "/rss/channel/item";
			NodeList nodes = xmlParser(is, expression);
			
			if(nodes.getLength() > 0) {
				torrent = parseTorrent(nodes.item(0));
				torrents.add(torrent);
			}
		}
		
		return torrents;
	}

	private static List<TorrentFile> buscaUrlsTorrentsComplexas(Episodio episodio, Boolean modoHD) throws XPathExpressionException, ClientProtocolException, IOException {
		List<TorrentFile> lista = new ArrayList<TorrentFile>(); 
		
		String nomeBusca = episodio.getTemporada().getSerie().getNome().replaceAll(" ", ".");
		String codEpisodio = episodio.getNumeroFormatado();
		
		String urlTorrentzEu = "http://torrentz.eu/feed_verifiedP?q=" 
				+ URLEncoder.encode(nomeBusca, "UTF-8") + "%20"
				+ codEpisodio + "%20" 
				+ (modoHD ? "" : "-") + "720p";
		
		Log.d("url: " + urlTorrentzEu);
		// TODO Utilizar JSoup
		InputStream is = downloadFile(urlTorrentzEu).getIs();
		
		String expression = "/rss/channel/item";
		NodeList nodes = xmlParser(is, expression);
		if(nodes.getLength() > 0) {
			Element el = (Element) nodes.item(0);
			String fileName = el.getElementsByTagName("title").item(0).getTextContent().replaceAll(" ", ".") + ".torrent";
			String link = el.getElementsByTagName("link").item(0).getTextContent();
			String html = IOService.lerArquivo(downloadFile(link).getIs());
			
			processarBTChat(lista, fileName, html);
			processarTorLock(lista, fileName, html);
		}
		
		return lista;
	}

	private static void processarBTChat(List<TorrentFile> lista, String fileName, String html) {
		Pattern pattern = Pattern.compile("(http:\\/\\/www.bt\\-chat\\.com\\/details\\.php\\?id=[0-9]+)", Pattern.MULTILINE);
		
		Scanner scan = new Scanner(html);  
		String match = "";  
		while (match != null) {  
		    match = scan.findWithinHorizon(pattern, 0);
		    if (match != null) {  
		        String href = scan.match().group(0);
		        href = href.replace("details", "download1");
		        href += "&type=torrent";
		        
		        TorrentFile torrent = new TorrentFile();
		        torrent.setLink(href);
		        torrent.setFileName(fileName);
		        
		        lista.add(torrent);  
		    }  
		}
	}
	
	private static void processarTorLock(List<TorrentFile> lista, String fileName, String html) throws IOException {
		Pattern pattern = Pattern.compile("http:\\/\\/www\\.torlock\\.com\\/torrent\\/([0-9]+)\\/[^\\\"]+", Pattern.MULTILINE);
		
		Scanner scan = new Scanner(html);  
		String match = "";  
		while (match != null) {  
		    match = scan.findWithinHorizon(pattern, 0);
		    if (match != null) {  
		        String href = scan.match().group(0);
		        href = href.replace("/torrent/", "/tor/");
		        href = href.substring(0, href.lastIndexOf("/"));
		        href += ".torrent";
		        
		        Log.d("href: " + href);
		        
		        TorrentFile torrent = new TorrentFile();
		        torrent.setLink(href);
		        torrent.setFileName(fileName);
		        
		        lista.add(torrent);  
		    }  
		}
	}

	private static TorrentFile parseTorrent(Node item) {
		TorrentFile torrent = new TorrentFile();
		Element el = (Element) item;
		// TODO Utilizar JSoup
		String fileName = el.getElementsByTagName("title").item(0).getTextContent();
		if(fileName.indexOf("[") >= 0) {
			fileName = fileName.substring(0, fileName.indexOf("["));
		}
		fileName = fileName.trim().replaceAll(" ", ".") + ".torrent";
		
		String link = el.getElementsByTagName("enclosure").item(0).getAttributes().getNamedItem("url").getTextContent();
		if(link.indexOf("?") >= 0) {
			link = link.substring(0, link.indexOf("?"));
		}
		
		torrent.setFileName(fileName);
		torrent.setLink(link);

		return torrent;
	}

	public static String organizarArquivos(SharedTorrent torrent, String caminhoDestino, String pastaTorrent) throws Exception {
		String caminhoArquivoVideo = null;		
		List<String> videoExtensions = getVideoExtensions();
		List<String> listaDeExclusao = new ArrayList<String>();
		
		for(String caminhoArquivo : torrent.getFilenames()) {
			LazierFile arquivo = new LazierFile(pastaTorrent + caminhoArquivo);
			String nomeArquivo = caminhoArquivo;
			String extensaoArquivo = nomeArquivo.substring(nomeArquivo.lastIndexOf(".") + 1).toLowerCase(Locale.US);
			if(videoExtensions.contains(extensaoArquivo)) {
				if(caminhoArquivo.indexOf("/") >= 0) {
					String pasta = caminhoArquivo.substring(0, caminhoArquivo.lastIndexOf("/") + 1);
					nomeArquivo = caminhoArquivo.substring(caminhoArquivo.lastIndexOf("/") + 1);
					listaDeExclusao.add(pastaTorrent + pasta);
				}
				LazierFile arquivoTemporario = new LazierFile(caminhoDestino + nomeArquivo);
				Log.d("Movendo arquivo '" + arquivo.getCaminhoArquivo() + "' para '" + arquivoTemporario.getCaminhoArquivo() + "'");
				boolean renameOK = arquivo.renameTo(arquivoTemporario);
				if(!renameOK) {
					throw new Exception("Erro ao mover arquivo.");
				}
				caminhoArquivoVideo = arquivo.getCaminhoArquivo();
			} else {
				listaDeExclusao.add(pastaTorrent + caminhoArquivo);
			}
		}
		
		for(String arquivoParaExcluir : listaDeExclusao) {
			LazierFile fileTemp = new LazierFile(arquivoParaExcluir);
			if(fileTemp.exists()) {
				fileTemp.delete();
			}
		}
		
		return caminhoArquivoVideo;
	}

	private static List<String> getVideoExtensions() {
		List<String> extensions = new ArrayList<String>();
		
		extensions.add("mp4");
		extensions.add("avi");
		extensions.add("divx");
		extensions.add("mkv");
		extensions.add("mpg");
		extensions.add("mpeg");
		
		return extensions;
	}

	public static String corrigirNomeVideo(TorrentFile torrent, String caminhoVideo, String caminhoDestino) throws Exception {
		String torrentFileName = torrent.getFileName();
		String extensaoVideo = caminhoVideo.substring(caminhoVideo.lastIndexOf(".") + 1);
		
		String nomeVideoCorreto = torrentFileName.substring(0, torrentFileName.lastIndexOf(".")) + "." + extensaoVideo;
		
		LazierFile arquivo = new LazierFile(caminhoVideo);
		LazierFile arquivoDestino = new LazierFile(caminhoDestino + nomeVideoCorreto);
		
		Log.d("Corrigindo nome do video, de '" + arquivo.getCaminhoArquivo() + "' para '" + arquivoDestino.getCaminhoArquivo() + "'");
		
		boolean renameOK = arquivo.renameTo(arquivoDestino);
		
		if(!renameOK) {
			throw new Exception("Erro ao alterar o nome do arquivo.");
		}
		
		return nomeVideoCorreto;
	}
}

package br.com.pnpa.lazierdroid.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import javax.xml.xpath.XPathExpressionException;

import jcifs.smb.SmbFile;

import org.apache.http.client.ClientProtocolException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.util.Log;
import br.com.pnpa.lazierdroid.entities.DownloadFile;
import br.com.pnpa.lazierdroid.entities.Episodio;
import br.com.pnpa.lazierdroid.entities.Torrent;
import br.com.pnpa.lazierdroid.util.Util;

public class TorrentService extends BaseService {

	public static Torrent buscaTorrent(Episodio episodio, Boolean modoHD, String torrentFolder) throws Exception {
		List<Torrent> torrents = buscaUrlsTorrents(episodio, modoHD);
		
		int i = 0;
		Torrent torrent = null;
		while (i < torrents.size()) {
			torrent = torrents.get(i++);
			
			Log.d("teste", "torrent.fileName: " + torrent.getFileName());
			Log.d("teste", "torrent.link: " + Util.ajustarLink(torrent.getLink()));
			
			SmbFile arquivo = IOService.salvarArquivo(Util.ajustarLink(torrent.getLink()), torrentFolder + torrent.getFileName());
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

	private static List<Torrent> buscaUrlsTorrents(Episodio episodio, Boolean modoHD) throws XPathExpressionException, ClientProtocolException, IOException {
		List<Torrent> torrents = new ArrayList<Torrent>();
		
		torrents.addAll(buscarUrlsTorrentsSimples(episodio, modoHD));
		torrents.addAll(buscaUrlsTorrentsComplexas(episodio, modoHD));
		
		return torrents;
	}

	private static List<Torrent> buscarUrlsTorrentsSimples(Episodio episodio, Boolean modoHD) throws ClientProtocolException, IOException, XPathExpressionException {
		String nomeBusca = episodio.getTemporada().getSerie().getNome().replaceAll(" ", ".");
		String codEpisodio = episodio.getNumeroFormatado();
		
		List<Torrent> torrents = new ArrayList<Torrent>();
		List<String> feedUrls = new ArrayList<String>();
		
		String urlKickassTo = "http://kickass.to/usearch/"
				+ URLEncoder.encode(nomeBusca, "UTF-8") + "%20"
				+ URLEncoder.encode(codEpisodio, "UTF-8") + "%20x264%20"
				+ URLEncoder.encode(modoHD ? "" : "-", "UTF-8")
				+ "720p%20verified:1/?field=seeders&sorder=desc&rss=1";
		
		String urlEztvIt = "https://ezrss.it/search/index.php?show_name="
				+ URLEncoder.encode(nomeBusca, "UTF-8") + "%20&date=&quality="
				+ (modoHD ? "720P" : "HDTV&quality_exact=true") + "&release_group=&episode_title=&season="
				+ URLEncoder.encode(String.valueOf(episodio.getTemporada().getNumero()), "UTF-8") + "&episode="
				+ URLEncoder.encode(String.valueOf(episodio.getNumero()), "UTF-8") + "&video_format=&audio_format=&modifier=&mode=rss";
		
//		Log.d("teste", "urlKickassTo: ." + urlKickassTo + ".");
//		Log.d("teste", "urlEztvIt: ." + urlEztvIt + ".");

		feedUrls.add(urlKickassTo);
		feedUrls.add(urlEztvIt);
		
		for(int i=0; i<feedUrls.size(); i++) {
			Torrent torrent = null;
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

	private static List<Torrent> buscaUrlsTorrentsComplexas(Episodio episodio, Boolean modoHD) throws XPathExpressionException, ClientProtocolException, IOException {
		List<Torrent> lista = new ArrayList<Torrent>(); 
		
		String nomeBusca = episodio.getTemporada().getSerie().getNome().replaceAll(" ", ".");
		String codEpisodio = episodio.getNumeroFormatado();
		
		String urlTorrentzEu = "http://torrentz.eu/feed_verifiedP?q=" 
				+ URLEncoder.encode(nomeBusca, "UTF-8") + "%20"
				+ codEpisodio + "%20" 
				+ (modoHD ? "" : "-") + "720p";
		
		InputStream is = downloadFile(urlTorrentzEu).getIs();
		
		String expression = "/rss/channel/item";
		NodeList nodes = xmlParser(is, expression);
		if(nodes.getLength() > 0) {
			Element el = (Element) nodes.item(0);
			String fileName = el.getElementsByTagName("title").item(0).getTextContent().replaceAll(" ", ".") + ".torrent";
			String link = el.getElementsByTagName("link").item(0).getTextContent();
			is = downloadFile(link).getIs();
			Pattern pattern = Pattern.compile("(http:\\/\\/www.bt\\-chat\\.com\\/details\\.php\\?id=[0-9]+)", Pattern.MULTILINE);
			
			Scanner scan = new Scanner(is, "UTF-8");  
	        String match = "";  
	        while (match != null) {  
	            match = scan.findWithinHorizon(pattern, 0);
//	            Log.d("teste", "match: " + match);
	            if (match != null) {  
	                String href = scan.match().group(0);
//	                Log.d("teste", "href: " + href);
	                href = href.replace("details", "download1");
	                href += "&type=torrent";
//	                Log.d("teste", "href: " + href);
	                
	                Torrent torrent = new Torrent();
	                torrent.setLink(href);
	                torrent.setFileName(fileName);
	                
	                lista.add(torrent);  
	            }  
	        }  

		}
		
		return lista;
	}

	private static Torrent parseTorrent(Node item) {
		Torrent torrent = new Torrent();
		Element el = (Element) item;
		
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

//		Log.d("teste", "torrent.fileName: " + torrent.getFileName());
//		Log.d("teste", "torrent.link: " + torrent.getLink());
		
		return torrent;
	}
}
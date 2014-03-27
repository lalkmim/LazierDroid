package br.com.pnpa.lazierdroid.services;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;

import javax.xml.xpath.XPathExpressionException;

import jcifs.smb.SmbException;

import org.apache.http.client.ClientProtocolException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import br.com.pnpa.lazierdroid.entities.Episodio;
import br.com.pnpa.lazierdroid.entities.LazierFile;
import br.com.pnpa.lazierdroid.entities.LegendaFile;
import br.com.pnpa.lazierdroid.util.Log;

import com.github.junrar.exception.RarException;

public class LegendaService extends BaseService {
	public static LegendaFile buscaLegenda(Episodio episodio, String tempFolder) throws Exception {
		if(episodio.getCaminhoLegenda() != null) {
			LazierFile arquivoTemp = new LazierFile(episodio.getCaminhoLegenda());
			if(arquivoTemp.exists()) {
				LegendaFile legendaFile = new LegendaFile();
				legendaFile.setLocalFile(arquivoTemp);
				
				String fileName = arquivoTemp.getCaminhoArquivo();
				if(fileName.indexOf("/") >= 0) {
					fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
				}
				
				legendaFile.setFileName(fileName);
				legendaFile.setLink(episodio.getLinkLegenda());
				
				return legendaFile;
			}
		}
		
		LegendaFile legenda = buscaLegendaSites(episodio, tempFolder);

		if(legenda == null)
			throw new Exception("Nenhuma legenda encontrada.");
		
		return legenda;
	}
	
	private static LegendaFile buscaLegendaSites(Episodio episodio, String tempFolder) throws XPathExpressionException, ClientProtocolException, IOException, RarException {
		LegendaFile legenda = null;
		
		legenda = buscaLegendaSiteLegendasTV(episodio, tempFolder);
		if(legenda == null) {
			legenda = buscaLegendaSiteAddic7ed(episodio, tempFolder);
		}
		
		return legenda;
	}
	
	private static LegendaFile buscaLegendaSiteLegendasTV(Episodio episodio, String tempFolder) throws ClientProtocolException, IOException, XPathExpressionException, RarException {
		String nomeBusca = episodio.getTemporada().getSerie().getNome().replaceAll(" ", ".");
		String codEpisodio = episodio.getNumeroFormatado();
		
		String urlLegendasTV = "http://legendas.tv/util/carrega_legendas_busca/termo:"
				+ URLEncoder.encode(nomeBusca, "UTF-8")
				+ "."
				+ URLEncoder.encode(codEpisodio, "UTF-8")
				+ "/id_idioma:1/sel_tipo:d";
		
		Log.d("url: " + urlLegendasTV);
		
		Document doc = Jsoup.connect(urlLegendasTV).timeout(0).post();
		Elements items = doc.select(".destaque .f_left p a");
		if(items.size() == 0)
			return null;
		
		Element el = items.get(0);
		
		String link = "http://legendas.tv" + el.attr("href").replace("/download/", "/downloadarquivo/");

		Log.d("el.html: " + el.html());
		Log.d("legenda.link: " + link);
		
		LegendaFile legendaFile = LegendaService.extrairLegenda(episodio, link, tempFolder);
			
		return legendaFile;
	}
	
	private static LegendaFile buscaLegendaSiteAddic7ed(Episodio episodio, String tempFolder) throws ClientProtocolException, IOException, XPathExpressionException, RarException {
		LegendaFile legendaFile = null;
		String nomeBusca = episodio.getTemporada().getSerie().getNome();
		
		String urlAddic7ed = "http://www.addic7ed.com/serie/"
				+ URLEncoder.encode(nomeBusca, "UTF-8")
				+ "/"
				+ episodio.getTemporada().getNumero()
				+ "/"
				+ episodio.getNumero() 
				+ "/10";
		
		legendaFile = parseHTMLAddic7ed(urlAddic7ed, episodio);
		
		if(legendaFile != null) {
			return legendaFile;
		}
		
		nomeBusca = nomeBusca.substring(0, nomeBusca.indexOf("(")).trim();
		
		urlAddic7ed = "http://www.addic7ed.com/serie/"
				+ URLEncoder.encode(nomeBusca, "UTF-8")
				+ "/"
				+ episodio.getTemporada().getNumero()
				+ "/"
				+ episodio.getNumero() 
				+ "/10";
		
		legendaFile = parseHTMLAddic7ed(urlAddic7ed, episodio);
		
		String link = legendaFile.getLink();

		legendaFile = LegendaService.extrairLegenda(episodio, link, tempFolder);
		
		return legendaFile;
	}

	private static LegendaFile parseHTMLAddic7ed(String urlAddic7ed, Episodio episodio) throws IOException {
		LegendaFile legendaFile = null;
		final String LINGUA = "Portuguese (Brazilian)";
		final String STATUS = "Completed";
		
		final String nomeVideo = episodio.getNomeVideo();
		final String VERSAO = nomeVideo.substring(nomeVideo.lastIndexOf("-") + 1, nomeVideo.lastIndexOf("."));
		
		Log.d("url: " + urlAddic7ed);
		
		Connection conn = Jsoup.connect(urlAddic7ed);
		conn = conn.timeout(0);
		conn = conn.referrer("http://www.addic7ed.com");
		conn = conn.userAgent("Mozilla/5.0 Firefox/26.0");
		
		Document doc = conn.post();
		Elements items = doc.select("#container95m .tabel95 .tabel95");
		for(int i=0; i<items.size(); i++) {
			Element el = items.get(i);
			Element temp = el.select(".NewsTitle").get(0);
			String version = temp.text();
			version = version.substring(0, version.indexOf(",")).replace("Version ", "");
			if(version.indexOf("x264-") >= 0) {
				version = version.substring(version.indexOf("x264-") + 5);
			}
			Log.d("version: " + version);
			
			temp = el.select(".language").get(0);
			String language = temp.text();
			Log.d("language: " + language);
			
			String status = temp.parent().select("td").get(3).text();
			Log.d("status: " + status);
			
			temp = el.select(".buttonDownload").get(0);
			String link = "http://www.addic7ed.com" + temp.attr("href");
			Log.d("link: " + link);
			
			if(VERSAO.equalsIgnoreCase(version) && LINGUA.equalsIgnoreCase(language) && STATUS.equalsIgnoreCase(status)) {
				legendaFile = new LegendaFile();
				legendaFile.setLink(link);
			}
		}
		
		return legendaFile;
	}

	private static LegendaFile extrairLegenda(Episodio episodio, String link, String tempFolder) throws IOException, RarException {
		LegendaFile legendaFile = null;
		String nomeProcurado = episodio.getNomeVideo();
		nomeProcurado = nomeProcurado.substring(0, nomeProcurado.lastIndexOf("."));
		nomeProcurado += ".srt";
		
		LazierFile lazierFile = IOService.downloadTemporario(link);
		
		String nomeArquivo = lazierFile.getCaminhoArquivo();
		nomeArquivo = nomeArquivo.substring(nomeArquivo.lastIndexOf("/") + 1);
		
		String extensao = nomeArquivo.substring(nomeArquivo.lastIndexOf(".") + 1);
		
		Log.d("nomeProcurado: " + nomeProcurado);
		Log.d("nomeArquivo: " + nomeArquivo);
		Log.d("extensao: " + extensao);
		
		if(extensao.equalsIgnoreCase("rar")) {
			legendaFile = IOService.extrairArquivoRARPeloNome(tempFolder, nomeProcurado, lazierFile.getArquivoLocal());
		} else if(extensao.equalsIgnoreCase("zip")) {
			legendaFile = IOService.extrairArquivoZIPPeloNome(tempFolder, nomeProcurado, lazierFile.getArquivoLocal());
		} else if(extensao.equalsIgnoreCase("srt")) {
			if(!nomeArquivo.equals(nomeProcurado)) {
				Log.d("lazierFile.caminhoArquivo: " + lazierFile.getCaminhoArquivo());
				Log.d("lazierFile.exists: " + lazierFile.exists());
				LazierFile arquivoCorreto = new LazierFile(tempFolder + nomeProcurado);
				
				IOService.copiarArquivo(lazierFile, arquivoCorreto);
				lazierFile = arquivoCorreto;
			}
			
			legendaFile = new LegendaFile();
			legendaFile.setLocalFile(lazierFile);
			legendaFile.setFileName(nomeProcurado);
		}
		
		if(legendaFile != null) {
			legendaFile.setLink(link);
		}
		
		Log.d("legendaFile.fileName: " + legendaFile.getFileName());
		Log.d("legendaFile.link: " + legendaFile.getLink());
		Log.d("legendaFile.localFile.caminhoArquivo: " + legendaFile.getLocalFile().getCaminhoArquivo());
		
		return legendaFile;
	}

	public static void organizarArquivos(Episodio episodio, String pastaFinal) throws MalformedURLException, SmbException {
		String nomeSerie = episodio.getTemporada().getSerie().getNome();
		String nomeTemporada = "Season " + (episodio.getTemporada().getNumero() < 10 ? "0" : "") + episodio.getTemporada().getNumero();
		String caminhoFinal = pastaFinal + nomeSerie + "/" + nomeTemporada + "/";
		
		Log.d("caminhoFinal: " + caminhoFinal);
		
		LazierFile diretorioFinal = new LazierFile(caminhoFinal);
		diretorioFinal.mkdirs();
		
		LazierFile arquivoVideo = new LazierFile(episodio.getCaminhoVideo());
		LazierFile arquivoLegenda = new LazierFile(episodio.getCaminhoLegenda());
		
		LazierFile arquivoVideoFinal = new LazierFile(caminhoFinal + episodio.getNomeVideo());
		LazierFile arquivoLegendaFinal = new LazierFile(caminhoFinal + episodio.getNomeLegenda());
		
		arquivoVideo.renameTo(arquivoVideoFinal);
		arquivoLegenda.renameTo(arquivoLegendaFinal);
		
		episodio.setCaminhoVideo(arquivoVideo.getCaminhoArquivo());
		episodio.setCaminhoLegenda(arquivoLegenda.getCaminhoArquivo());
	}

	
}

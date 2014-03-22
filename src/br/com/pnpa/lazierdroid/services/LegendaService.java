package br.com.pnpa.lazierdroid.services;

import java.io.IOException;
import java.net.URLEncoder;

import javax.xml.xpath.XPathExpressionException;

import org.apache.http.client.ClientProtocolException;
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
		
//		List<LegendaFile> legendas = buscaUrlsLegendas(episodio, tempFolder);
//		
//		int i = 0;
//		LegendaFile legenda = null;
//		while (i < legendas.size()) {
//			legenda = legendas.get(i++);
//			
//			Log.d("legenda.fileName: " + legenda.getFileName());
//			Log.d("legenda.link: " + Util.ajustarLink(legenda.getLink()));
//			
//			LazierFile arquivo = IOService.salvarArquivo(Util.ajustarLink(legenda.getLink()), tempFolder + legenda.getFileName());
//			if (arquivo == null) {
//				legenda = null;
//			} else {
//				legenda.setLocalFile(arquivo);
//				break;
//			}
//		}

		if(legenda == null)
			throw new Exception("Nenhuma legenda encontrada.");
		
		return legenda;
	}
	
	private static LegendaFile buscaLegendaSites(Episodio episodio, String tempFolder) throws XPathExpressionException, ClientProtocolException, IOException, RarException {
		LegendaFile legenda = null;
		
		legenda = buscaLegendaSiteLegendasTV(episodio, tempFolder);
		
//		if(legenda == null) {
//			legendas.addAll(buscaUrlAddic7ed(episodio));
//		}
		
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
		
		Document doc = Jsoup.connect(urlLegendasTV).get();
		Elements items = doc.select(".destaque .f_left p a");
		Element el = items.get(0);
		
		String link = "http://legendas.tv" + el.attr("href").replace("/download/", "/downloadarquivo/");

		Log.d("el.html: " + el.html());
		Log.d("legenda.link: " + link);
		
		LegendaFile legendaFile = LegendaService.extrairLegenda(episodio, link, tempFolder);
			
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
		
		Log.d("nomeArquivo: " + nomeArquivo);
		Log.d("extensao: " + extensao);
		
		if(extensao.equalsIgnoreCase("rar")) {
			legendaFile = IOService.extrairArquivoRARPeloNome(tempFolder, nomeProcurado, lazierFile.getArquivoLocal());
		} else if(extensao.equalsIgnoreCase("zip")) {
			legendaFile = IOService.extrairArquivoZIPPeloNome(tempFolder, nomeProcurado, lazierFile.getArquivoLocal());
		}
		
		if(legendaFile != null) {
			legendaFile.setLink(link);
		}
		
		return legendaFile;
	}

	
}

package br.com.pnpa.lazierdroid.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xmlpull.v1.XmlPullParserException;

import com.j256.ormlite.stmt.PreparedQuery;

import android.util.Log;
import br.com.pnpa.lazierdroid.entities.Episodio;
import br.com.pnpa.lazierdroid.entities.Serie;
import br.com.pnpa.lazierdroid.entities.Temporada;
import br.com.pnpa.lazierdroid.model.helper.DatabaseHelper;

public class SerieService extends BaseService {

	public static List<Serie> pesquisaSerie(String nomeSerie) throws IllegalStateException, IOException, XmlPullParserException, XPathExpressionException {
		String url = "http://services.tvrage.com/feeds/search.php?show=" + URLEncoder.encode(nomeSerie, "UTF-8");
		InputStream in = downloadFile(url);
		String expression = "/Results/show";
		List<Serie> lista = parseSeries(in, expression); 
		
		return lista;
	}
	
	public static Serie pesquisaDetalhesSerie(Serie serie, DatabaseHelper helper) throws XPathExpressionException, ClientProtocolException, IOException, SQLException {
		String url = "http://services.tvrage.com/feeds/full_show_info.php?sid=" + URLEncoder.encode(String.valueOf(serie.getId()), "UTF-8");
		Log.i("pesquisaDetalhesSerie url", url);
		InputStream in = downloadFile(url);
		String expression = "/Show";
		
		serie = parseDetalheSerie(serie, in, expression, helper);
		
		return serie;
	}

	private static Serie parseDetalheSerie(Serie serie, InputStream in, String expression, DatabaseHelper helper) throws XPathExpressionException, SQLException {
		NodeList nodes = xmlParser(in, expression);
		Element el = (Element) nodes.item(0);
		serie.setImageURL(el.getElementsByTagName("image").item(0).getTextContent());
		helper.getSerieDao().createOrUpdate(serie);
		
		nodes = el.getElementsByTagName("Season");
		for(int i=0; i<nodes.getLength(); i++) {
			parseTemporada(nodes.item(i), serie, helper);
		}

		return serie;
	}

	private static Temporada parseTemporada(Node item, Serie serie, DatabaseHelper helper) throws SQLException {
		Temporada temporada = new Temporada();
		Element el = (Element) item;
		temporada.setNumero(Integer.parseInt(el.getAttribute("no")));
		temporada.setSerie(serie);
		
		List<Temporada> existentes = helper.getTemporadaDao().queryForMatchingArgs(temporada);
		if(existentes.size() == 0) {
			temporada = helper.getTemporadaDao().createIfNotExists(temporada);
		} else {
			temporada = existentes.get(0);
		}
		
		NodeList episodeItems = el.getElementsByTagName("episode");
		for(int i=0; i<episodeItems.getLength(); i++) {
			parseEpisodio(episodeItems.item(i), temporada, helper);
		}
		
		return temporada;
	}

	private static Episodio parseEpisodio(Node item, Temporada temporada, DatabaseHelper helper) throws SQLException {
		Episodio episodio = new Episodio();
		
		Element el = (Element) item;
		episodio.setTemporada(temporada);
		episodio.setNumero(Integer.parseInt(el.getElementsByTagName("seasonnum").item(0).getFirstChild().getNodeValue()));
		episodio.setTitle(el.getElementsByTagName("title").item(0).getFirstChild().getNodeValue());
		episodio.setLink(el.getElementsByTagName("link").item(0).getFirstChild().getNodeValue());
		episodio.setDate(el.getElementsByTagName("airdate").item(0).getFirstChild().getNodeValue());
		
		List<Episodio> existentes = helper.getEpisodioDao().queryForMatchingArgs(episodio);
		if(existentes.size() == 0) {
			episodio = helper.getEpisodioDao().createIfNotExists(episodio);
		} else {
			episodio = existentes.get(0);
		}
		
		return episodio;
	}

	private static List<Serie> parseSeries(InputStream in, String expression) throws XPathExpressionException, XmlPullParserException, IOException {
		List<Serie> series = new ArrayList<Serie>();
		NodeList nodes = xmlParser(in, expression);
		for(int i=0; i<nodes.getLength(); i++) {
			series.add(parseSerie(nodes.item(i)));
		}
		
		return series;
	}

	private static Serie parseSerie(Node item) {
		Serie serie = new Serie();
		Element el = (Element) item; 
		
		serie.setId(Integer.parseInt(el.getElementsByTagName("showid").item(0).getTextContent()));
		serie.setNome(el.getElementsByTagName("name").item(0).getTextContent());
		serie.setAnoInicio(Integer.parseInt(el.getElementsByTagName("started").item(0).getTextContent()));
		serie.setLink(el.getElementsByTagName("link").item(0).getTextContent());
		
		Log.d(SerieService.class.getName(), serie.getNome());
		
		return serie;
	}

	private static NodeList xmlParser(InputStream in, String xpathExpression) throws XPathExpressionException {
        InputSource is = new InputSource(in);
        XPath xpath = XPathFactory.newInstance().newXPath();
		return (NodeList) xpath.evaluate(xpathExpression, is, XPathConstants.NODESET);
	}

	private static InputStream downloadFile(String url) throws IOException,
			ClientProtocolException {
		HttpGet uri = new HttpGet(url);    
		DefaultHttpClient client = new DefaultHttpClient();
		HttpResponse resp = client.execute(uri);

		StatusLine status = resp.getStatusLine();
		if (status.getStatusCode() != 200) {
		    Log.d(SerieService.class.getName(), "HTTP error, invalid server status code: " + resp.getStatusLine());  
		}
		return resp.getEntity().getContent();
	}
	
	public static List<Serie> pesquisarMinhasSeries(DatabaseHelper helper) throws SQLException {
		PreparedQuery<Serie> preparedQuery = helper.getSerieDao().queryBuilder().orderBy("nome", true).prepare();
		return helper.getSerieDao().query(preparedQuery);
	}
}
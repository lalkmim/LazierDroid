package br.com.pnpa.lazierdroid.service;

import java.io.IOException;
import java.io.InputStream;
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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;
import android.util.Xml;
import br.com.pnpa.lazierdroid.entities.Serie;

public class SeriePublicService {

	public static List<Serie> pesquisaSerie(String nomeSerie) throws IllegalStateException, IOException, XmlPullParserException, XPathExpressionException {
		String url = "http://services.tvrage.com/feeds/search.php?show=" + nomeSerie;
		InputStream in = downloadFile(url);
		String expression = "/Results";
		List<Serie> lista = parseSeries(in, expression); 
		
		return lista;
	}

	private static List<Serie> parseSeries(InputStream in, String expression) throws XPathExpressionException, XmlPullParserException, IOException {
		List<Serie> series = new ArrayList<Serie>();
		NodeList nodes = xmlParser(in, expression);
		for(int i=0; i<nodes.getLength(); i++) {
			Serie serie = parseSerie(nodes.item(i));
			series.add(serie);
		}
		return series;
	}

	private static Serie parseSerie(Node item) {
		Serie serie = new Serie();
		
//		serie.setNome(item.getn)
		Log.d(SeriePublicService.class.getCanonicalName(), item.toString());
		
		return serie;
	}

	private static NodeList xmlParser(InputStream in, String xpathExpression)
			throws XmlPullParserException, IOException,
			XPathExpressionException {
		XmlPullParser parser = Xml.newPullParser();
		parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(in, null);
        parser.nextTag();
        
        XPath xpath = XPathFactory.newInstance().newXPath();
		return (NodeList) xpath.evaluate(xpathExpression, parser, XPathConstants.NODESET);
	}

	private static InputStream downloadFile(String url) throws IOException,
			ClientProtocolException {
		HttpGet uri = new HttpGet(url);    
		DefaultHttpClient client = new DefaultHttpClient();
		HttpResponse resp = client.execute(uri);

		StatusLine status = resp.getStatusLine();
		if (status.getStatusCode() != 200) {
		    Log.d(SeriePublicService.class.getCanonicalName(), "HTTP error, invalid server status code: " + resp.getStatusLine());  
		}
		return resp.getEntity().getContent();
	}

	public List<Serie> parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return parseSeries(parser);
        } finally {
            in.close();
        }
    }
	
	private List<Serie> parseSeries(XmlPullParser parser) throws XmlPullParserException, IOException {
	    List<Serie> entries = new ArrayList<Serie>();

	    parser.require(XmlPullParser.START_TAG, null, "Results");
	    while (parser.next() != XmlPullParser.END_TAG) {
	        if (parser.getEventType() != XmlPullParser.START_TAG) {
	            continue;
	        }
	        String name = parser.getName();
	        // Starts by looking for the entry tag
	        if (name.equals("show")) {
	            entries.add(parseSerie(parser));
	        } else {
	            skip(parser);
	        }
	    }  
	    return entries;
	}
	
	private Serie parseSerie(XmlPullParser parser) {
		Serie serie = new Serie();
		
		
		
		return serie;
	}

	private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
	    if (parser.getEventType() != XmlPullParser.START_TAG) {
	        throw new IllegalStateException();
	    }
	    int depth = 1;
	    while (depth != 0) {
	        switch (parser.next()) {
	        case XmlPullParser.END_TAG:
	            depth--;
	            break;
	        case XmlPullParser.START_TAG:
	            depth++;
	            break;
	        }
	    }
	 }
}
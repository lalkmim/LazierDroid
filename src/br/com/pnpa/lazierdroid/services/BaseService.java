package br.com.pnpa.lazierdroid.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import br.com.pnpa.lazierdroid.entities.DownloadFile;
import br.com.pnpa.lazierdroid.util.Log;

public class BaseService {
	protected static String lerArquivo(InputStream stream) throws IOException {
	    BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
	    StringBuffer dados = new StringBuffer();
	    String line = null;
	    
	    while((line = reader.readLine()) != null)
            dados.append(line);

	    return dados.toString();
	}
	
	protected static NodeList xmlParser(InputStream in, String xpathExpression) throws XPathExpressionException {
        InputSource is = new InputSource(in);
        XPath xpath = XPathFactory.newInstance().newXPath();
		return (NodeList) xpath.evaluate(xpathExpression, is, XPathConstants.NODESET);
	}

	protected static DownloadFile downloadFile(String url) throws IOException, ClientProtocolException {
		HttpResponse resp = null;
		try {
			HttpGet uri = new HttpGet(url);
			uri.setHeader("User-Agent", "Mozilla/5.0 Firefox/26.0");
			DefaultHttpClient client = new DefaultHttpClient();
			resp = client.execute(uri);
	
			StatusLine status = resp.getStatusLine();
			if (status.getStatusCode() != 200) {
			    Log.d("HTTP error, invalid server status code: " + resp.getStatusLine());
			    return null;
			}
		} catch(Exception e) {
			return null;
		}
		
		DownloadFile file = new DownloadFile();
		
		file.setIs(resp.getEntity().getContent());
		file.setLink(url);
		file.setLength(resp.getEntity().getContentLength());
		
		Header[] headers = resp.getHeaders("Content-Disposition");
		for(int i=0; i<headers.length; i++) {
			HeaderElement[] headerElements = headers[i].getElements();
			Log.d("header[" + i + "].name: " + headers[i].getName());
			Log.d("header[" + i + "].value: " + headers[i].getValue());
			for(int j=0; j<headerElements.length; j++) {
				Log.d("headerElement[" + j + "].name: " + headerElements[j].getName());
				Log.d("headerElement[" + j + "].value: " + headerElements[j].getValue());
				Log.d("headerElement[" + j + "].parameter(\"filename\").name: " + headerElements[j].getParameterByName("filename").getName());
				Log.d("headerElement[" + j + "].parameter(\"filename\").value: " + headerElements[j].getParameterByName("filename").getValue());
				
				String fileName = headerElements[j].getParameterByName("filename").getValue();
				if(fileName != null) {
					file.setFileName(fileName);
					break;
				}
			}
		}
		
		return file;
	}
}

/*
HTTP/1.1 200 OK
Content-Disposition: attachment; filename="Sherlock.1x01.A.Study.In.Pink.HDTV.XviD-FoV.[eztv].torrent"
Content-type: application/x-bittorrent
Server: lighttpd - Kicking Apache's ASS!
Content-Length: 28622
Date: Tue, 04 Mar 2014 21:22:08 GMT
X-Varnish: 1754900050
Age: 0
Via: 1.1 varnish
Connection: keep-alive

*/

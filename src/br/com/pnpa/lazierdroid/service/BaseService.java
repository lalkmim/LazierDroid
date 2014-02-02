package br.com.pnpa.lazierdroid.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class BaseService {
	protected static String lerArquivo(InputStream stream) throws IOException {
	    BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
	    StringBuffer dados = new StringBuffer();
	    String line = null;
	    
	    while((line = reader.readLine()) != null)
            dados.append(line);

	    return dados.toString();
	}
}

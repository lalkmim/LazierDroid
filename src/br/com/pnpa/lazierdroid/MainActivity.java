package br.com.pnpa.lazierdroid;

import java.io.IOException;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.xmlpull.v1.XmlPullParserException;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import br.com.pnpa.lazierdroid.entities.Serie;
import br.com.pnpa.lazierdroid.service.DatabaseHelper;
import br.com.pnpa.lazierdroid.service.SeriePublicService;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;

public class MainActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void onClickPesquisarSeries(View v) {
		v.setEnabled(false);
		
//		Util.buildToast(this, getResources().getString(R.string.msg_atualizando_series)).show();
//		
//		String msgSucesso = getResources().getString(R.string.msg_atualizar_series_sucesso);
//		String msgErro = getResources().getString(R.string.erro_conexao);
    	
		EditText campoNomeSerie = (EditText) findViewById(R.id.campo_nome_serie);
		Button botaoPesquisar = (Button) findViewById(R.id.botao_pesquisar_serie);
		ListView listaSeries = (ListView) findViewById(R.id.lista_resultado_pesquisa_series);
		
		botaoPesquisar.setEnabled(false);
		
    	try {
			List<Serie> lista = SeriePublicService.pesquisaSerie(campoNomeSerie.getText().toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e(this.getClass().getCanonicalName(), getString(R.string.erro_pesquisar_series), e);
		}
    	
    	botaoPesquisar.setEnabled(true);
	}
}

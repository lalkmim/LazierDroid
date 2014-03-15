package br.com.pnpa.lazierdroid;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;
import br.com.pnpa.lazierdroid.entities.Serie;
import br.com.pnpa.lazierdroid.util.Log;

public class DetalheSerieActivity extends BaseActivity {
	private Serie serie;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detalhe_serie);
		
		try {
			Integer idSerie = getIntent().getExtras().getInt("id");
			serie = getHelper().getSerieDao().queryForId(idSerie);
			
			TextView titulo = (TextView) findViewById(R.id.textView_titulo_detalhe_serie);
			TextView anoInicio = (TextView) findViewById(R.id.textView_ano_inicio);
			ExpandableListView expListViewTemporadas = (ExpandableListView) findViewById(R.id.expandableListView_detalhe_serie_temporadas_episodios);
			
			titulo.setText(serie.getNome());
			anoInicio.setText(getString(R.string.label_ano_inicio) + " " + serie.getAnoInicio());
			
			TemporadasEpisodiosAdapter adapter = buildDetalhesSerieTemporadasAdapter(serie, getApplicationContext());
			expListViewTemporadas.setAdapter(adapter);
			adapter.notifyDataSetChanged();
			
			new LoadExternalImageTask().execute(new Integer[] {idSerie, R.id.imageView_detalhe_serie_capa});
		} catch (Exception e) {
			Log.e(getString(R.string.msg_erro_carregar_dados_serie), e);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.detalhe_serie, menu);
		return true;
	}
	
	public void onClickTVRage(View v) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(serie.getLink()));
		startActivity(intent);
	}

}

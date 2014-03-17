package br.com.pnpa.lazierdroid;

import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import br.com.pnpa.lazierdroid.R;
import br.com.pnpa.lazierdroid.entities.Serie;
import br.com.pnpa.lazierdroid.util.Util;

public class PesquisarSeriesActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pesquisar_series);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void onClickPesquisarSeries(View v) {
		EditText campoNomeSerie = (EditText) findViewById(R.id.campo_nome_serie);
		ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar_pesquisa_series);
		ListView listViewSeries = (ListView) findViewById(R.id.lista_resultado_pesquisa_series);
		Button botaoIncluir = (Button) findViewById(R.id.botao_incluir_series);
		
		progressBar.setVisibility(View.VISIBLE);
		listViewSeries.setVisibility(View.INVISIBLE);
		botaoIncluir.setVisibility(View.INVISIBLE);
		listViewSeries.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		
		new PesquisaSeriesTask().execute(campoNomeSerie.getText().toString());
	}
	
	public void onClickIncluirSeriesSelecionadas(View v) {
		Util.buildToast(getApplicationContext(), getString(R.string.msg_processando_inclusao_series)).show();
		ListView listViewSeries = (ListView) findViewById(R.id.lista_resultado_pesquisa_series);
		
		SparseBooleanArray sba = listViewSeries.getCheckedItemPositions();
		for(int i=0; i<sba.size(); i++) {
			if(sba.valueAt(i)) {
				Serie serie = (Serie) listViewSeries.getItemAtPosition(sba.keyAt(i));
				new IncluiSerieTask().execute(serie); 
			}
		}
	}
}

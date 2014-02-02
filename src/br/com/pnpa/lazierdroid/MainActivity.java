package br.com.pnpa.lazierdroid;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

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
		EditText campoNomeSerie = (EditText) findViewById(R.id.campo_nome_serie);
		ProgressBar progressBar = (ProgressBar) findViewById(R.id.spinner_pesquisa_series);
		progressBar.setVisibility(View.VISIBLE);
		new SearchTask().execute(campoNomeSerie.getText().toString());
	}
}

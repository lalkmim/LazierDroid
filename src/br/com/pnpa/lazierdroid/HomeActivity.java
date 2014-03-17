package br.com.pnpa.lazierdroid;

import org.apache.log4j.BasicConfigurator;

import br.com.pnpa.lazierdroid.R;
import br.com.pnpa.lazierdroid.util.Util;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

public class HomeActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		BasicConfigurator.configure();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}
	
	public void onClickPesquisarSeries(View v) {
		Intent intent = new Intent(this, PesquisarSeriesActivity.class);
    	startActivity(intent);
	}
	
	public void onClickMinhasSeries(View v) {
		Intent intent = new Intent(this, MinhasSeriesActivity.class);
    	startActivity(intent);
	}

	@Override
	protected void onDestroy() {
		Util.buildToast(this, "Teste");
		super.onDestroy();
	}

}

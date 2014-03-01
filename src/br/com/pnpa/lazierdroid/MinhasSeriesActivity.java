package br.com.pnpa.lazierdroid;

import java.sql.SQLException;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import br.com.pnpa.lazierdroid.entities.Serie;
import br.com.pnpa.lazierdroid.service.SerieService;
import br.com.pnpa.lazierdroid.util.Util;

public class MinhasSeriesActivity extends BaseActivity {
	List<Serie> listaSeries = null; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_minhas_series);
		ListView listViewSeries = (ListView) findViewById(R.id.listview_minhas_series);
		listViewSeries.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Serie selecionada = listaSeries.get(position);
				Log.d("teste", "ID: " + selecionada.getId());
				Intent intent = new Intent(MinhasSeriesActivity.this, DetalheSerieActivity.class);
				intent.putExtra("id", selecionada.getId());
		    	startActivity(intent);
			}
		});
		
		try {
			listaSeries = SerieService.pesquisarMinhasSeries(getHelper());
			SimpleAdapter adapter = buildMinhasSeriesAdapter(listaSeries, getApplicationContext());
			listViewSeries.setAdapter(adapter);
			adapter.notifyDataSetChanged();
		} catch (SQLException e) {
			String msgErro = getString(R.string.msg_erro_pesquisar_minhas_series);
			Log.e("ERROR", msgErro, e);
			Util.buildToast(this, msgErro);
		} finally {
			findViewById(R.id.progress_bar_minhas_series).setVisibility(View.INVISIBLE);
			findViewById(R.id.botao_atualizar_minhas_series).setVisibility(View.VISIBLE);
			listViewSeries.setVisibility(View.VISIBLE);
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.minhas_series, menu);
		return true;
	}
}

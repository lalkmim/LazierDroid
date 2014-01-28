package br.com.pnpa.lazierdroid;

import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.widget.ArrayAdapter;
import br.com.pnpa.lazierdroid.entities.Serie;
import br.com.pnpa.lazierdroid.service.DatabaseHelper;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;

public abstract class BaseActivity extends OrmLiteBaseActivity<DatabaseHelper> {
	protected static ArrayAdapter<String> buildTorrentsAdapter(List<Serie> series, Context context) {
		String[] nomesSeries = new String[series.size()];
		Iterator<Serie> it = series.iterator();
		int i=0;
		
		while(it.hasNext()) {
			nomesSeries[i++] = it.next().getNome();
		}
		
		return new ArrayAdapter<String>(
				context, 
				android.R.layout.simple_list_item_multiple_choice,
				nomesSeries);
	}
}

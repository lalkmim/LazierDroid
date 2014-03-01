package br.com.pnpa.lazierdroid.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import br.com.pnpa.lazierdroid.R;
import br.com.pnpa.lazierdroid.entities.Episodio;
import br.com.pnpa.lazierdroid.entities.Temporada;

public class TemporadasEpisodiosAdapter extends BaseExpandableListAdapter {
	private final List<Temporada> temporadas;
	private final LayoutInflater inflater;

	public TemporadasEpisodiosAdapter(Context context,
			List<Temporada> _temporadas) {
		this.inflater = LayoutInflater.from(context);
		this.temporadas = _temporadas;
	}

	@Override
	public Object getChild(int posTemporada, int posEpisodio) {
		return new ArrayList<Episodio>(temporadas.get(posTemporada)
				.getEpisodios()).get(posEpisodio);
	}

	@Override
	public long getChildId(int posTemporada, int posEpisodio) {
		return new ArrayList<Episodio>(temporadas.get(posTemporada)
				.getEpisodios()).get(posEpisodio).getId();
	}

	@Override
	public View getChildView(int groupPos, int childPos, boolean isLastChild, View convertView, ViewGroup parent) {
		View v = convertView;

		if (v == null) {
			v = inflater.inflate(R.layout.expandable_list_item_layout, parent, false);
		}

		TextView itemName = (TextView) v.findViewById(R.id.itemName);
		TextView itemDetalhes = (TextView) v.findViewById(R.id.itemDetalhes);

		Episodio episodio = (Episodio) temporadas.get(groupPos).getEpisodios().toArray()[childPos];

		itemName.setText(episodio.getTitle());
		itemDetalhes.setText(episodio.getNumeroFormatado() + " - " + episodio.getDate());

		return v;
	}

	@Override
	public int getChildrenCount(int posTemporada) {
		return temporadas.get(posTemporada).getEpisodios().size();
	}

	@Override
	public Object getGroup(int posTemporada) {
		return temporadas.get(posTemporada);
	}

	@Override
	public int getGroupCount() {
		return temporadas.size();
	}

	@Override
	public long getGroupId(int posTemporada) {
		return temporadas.get(posTemporada).getId();
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		View v = convertView;

		Log.d("teste", "resultView: " + v);

		if (v == null) {
			v = inflater.inflate(R.layout.expandable_list_group_layout, null);
		}

		TextView groupTitulo = (TextView) v.findViewById(R.id.groupName);
		TextView groupDetalhes = (TextView) v.findViewById(R.id.groupDetalhes);

		Temporada temporada = temporadas.get(groupPosition);

		groupTitulo.setText("Temporada " + temporada.getNumero());
		groupDetalhes.setText("Episódios: " + temporada.getEpisodios().size());

		return v;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

}

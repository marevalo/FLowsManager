package net.marevalo.flowsmanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class CollectionAdapter extends BaseAdapter {

    // private static final String LOGTAG = "CollectionAdapter";
    Context         context;
    ArrayList<Entity> collection;

    public CollectionAdapter(Context context, ArrayList<Entity> list) {

        this.context = context;
        collection = list;
    }

    @Override
    public int getCount() {

        return collection.size();
    }

    @Override
    public Object getItem(int position) {

        return collection.get(position);
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup arg2) {

        //Log.d(LOGTAG, "Recreating view" );

        Entity entity = collection.get(position);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.row_entity, null);

        }

        // Set the icon
        ImageView ivIcon = (ImageView) convertView.findViewById(R.id.iconImageView );
        ivIcon.setImageDrawable(
                context.getResources().getDrawable(entity.getIconResource() ) );
        // Set the name and ID
        TextView tvName = (TextView) convertView.findViewById(R.id.tv_entity_name);
        String name = entity.getDisplayName() ;
        tvName.setText( name );
        TextView tvId = (TextView) convertView.findViewById(R.id.tv_entity_id);
        String id = entity.getIdName();
        tvId.setText(((name == id) ? "" : id));

        return convertView;
    }

}

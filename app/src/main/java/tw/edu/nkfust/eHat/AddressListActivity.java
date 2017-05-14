package tw.edu.nkfust.eHat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import static tw.edu.nkfust.eHat.MainActivity.mMapHelper;

public class AddressListActivity extends AppCompatActivity {
    private AddressDatabaseHelper mAddressDatabaseHelper;
    private Cursor cursor;
    private SimpleCursorAdapter mSimpleCursorAdapter;

    private ListView listViewOfAddress;
    private Button buttonOfExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_address_list);

        mAddressDatabaseHelper = new AddressDatabaseHelper(AddressListActivity.this);
        cursor = mAddressDatabaseHelper.get();
        mSimpleCursorAdapter = new SimpleCursorAdapter(AddressListActivity.this,
                R.layout.address_adapter, cursor,
                new String[]{"Name", "Address"},
                new int[]{R.id.textOfTitle, R.id.textOfSubtitle});

        listViewOfAddress = (ListView) findViewById(R.id.listViewOfAddress);
        listViewOfAddress.setAdapter(mSimpleCursorAdapter);
        listViewOfAddress.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final int pos = position;
                final String alias = cursor.getString(1);
                final String address = cursor.getString(2).toString();
                final LatLng viewLatLng = new LatLng(cursor.getDouble(3), cursor.getDouble(4));

                new AlertDialog.Builder(AddressListActivity.this)
                        .setItems(new String[]{getString(R.string.textOfMenuItem_Show), getString(R.string.textOfMenuItem_Draw), getString(R.string.textOfMenuItem_Delete)}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        mMapHelper.updateMap(viewLatLng.latitude, viewLatLng.longitude);
                                        MainActivity.toMarkerOpt = mMapHelper.setMarker(viewLatLng.latitude, viewLatLng.longitude);
                                        MainActivity.toMarkerOpt.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
                                        MainActivity.toAddress = alias + " : " + address;
                                        MainActivity.toLatLng = viewLatLng;
                                        MainActivity.mMap.addMarker(MainActivity.toMarkerOpt);
                                        finish();
                                        break;
                                    case 1:
                                        MainActivity.mMap.clear();
                                        MainActivity.nowLatLng = mMapHelper.presentLatLng();
                                        MainActivity.toAddress = alias + " : " + address;
                                        MainActivity.toLatLng = viewLatLng;
                                        MarkerOptions viewMark = mMapHelper.setMarker(MainActivity.toLatLng.latitude, MainActivity.toLatLng.longitude);
                                        viewMark.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                                        MainActivity.mMap.addMarker(viewMark);
                                        MainActivity.mPathHelper.getPath(MainActivity.nowLatLng, MainActivity.toLatLng);
                                        finish();
                                        break;
                                    case 2:
                                        new AlertDialog.Builder(AddressListActivity.this)
                                                .setTitle(R.string.title_DeleteOfAddressDialog)
                                                .setMessage(R.string.message_CheckToDeleteTheAddress)
                                                .setPositiveButton(R.string.textOfButton_DialogYes, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        cursor.moveToPosition(pos);
                                                        mAddressDatabaseHelper.delete(cursor.getInt(0));
                                                        cursor.requery();
                                                        listViewOfAddress.setAdapter(mSimpleCursorAdapter);
                                                        Toast.makeText(AddressListActivity.this, R.string.toast_DeleteAddress, Toast.LENGTH_SHORT).show();
                                                    }// End of onClick
                                                })
                                                .setNegativeButton(R.string.textOfButton_DialogNo, null)
                                                .show();
                                        break;
                                }// End of switch-condition
                            }// End of onClick
                        })
                        .show();

                return false;
            }// End of onItemLongClick
        });

        buttonOfExit = (Button) findViewById(R.id.buttonOfExit);
        buttonOfExit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }// End of onClick
        });
    }// End of onCreate
}// End of AddressListActivity

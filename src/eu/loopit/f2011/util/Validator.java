package eu.loopit.f2011.util;

import java.util.HashSet;
import java.util.Set;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

import dk.bregnvig.formula1.client.domain.ClientDriver;
import eu.loopit.f2011.R;

public class Validator {
	
	private Context context;
	
	public Validator(Context context) {
		this.context = context;
	}
	
	public boolean validateAllSelected(ClientDriver[] drivers) {
		for (ClientDriver driver : drivers) {
			if (driver.getId() == null) {
				showError(context.getString(R.string.error_notSelected));
				return false;
			}
		}
		return true;
	}

	public boolean validateDrivers(ClientDriver[] drivers) {
		Set<ClientDriver> dublicateCheck = new HashSet<ClientDriver>();
		for (ClientDriver driver : drivers) {
			if (dublicateCheck.contains(driver)) {
				showError(context.getString(R.string.error_dublicate));
				return false;
			}
			dublicateCheck.add(driver);
		}
		return true;
	}
	
	public boolean validateNumber(String label, String number) {
		try {
			Integer.parseInt(number);
		} catch (NumberFormatException e) {
			showError(context.getString(R.string.error_notCorrectly, label));
			return false;
		}
		return true;
	}
	
	private void showError(String errorMessage) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(errorMessage)
			.setNeutralButton(R.string.label_ok, new OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
		builder.create().show();
	}
}

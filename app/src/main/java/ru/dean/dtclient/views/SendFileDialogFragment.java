package ru.dean.dtclient.views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;

import ru.dean.dtclient.R;
import ru.dean.dtclient.Storage;

/**
 * Created by explorer on 31.10.2014.
 */
public class SendFileDialogFragment extends DialogFragment {
    File directory;
    final int TYPE_PHOTO = 1;
    final int TYPE_VIDEO = 2;
    final int REQUEST_CODE_PHOTO = 1;
    final int REQUEST_CODE_VIDEO = 2;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final CharSequence[] items = {"Фото", "Видео", "Файл"};
        createDirectory();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Отправить")
                .setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        if (which == 0) {
                                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, generateFileUri(TYPE_PHOTO));
                                getActivity().startActivityForResult(intent, REQUEST_CODE_PHOTO);

                            }
                            if (which == 1) {
                                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, generateFileUri(TYPE_VIDEO));
                                getActivity().startActivityForResult(intent, REQUEST_CODE_VIDEO);

                            }

                    }
                });


        return builder.create();
    }



    private Uri generateFileUri(int type) {
        File file = null;
        switch (type) {
            case TYPE_PHOTO:
                file = new File(directory.getPath() + "/" + "photo_"
                        + System.currentTimeMillis() + ".jpg");
                break;
            case TYPE_VIDEO:
                file = new File(directory.getPath() + "/" + "video_"
                        + System.currentTimeMillis() + ".mp4");
                break;
        }
        Log.d("", "fileName = " + file);
        Storage.Inst().file_path = file;
        return Uri.fromFile(file);
    }

    private void createDirectory() {
        directory = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "dtclient");
        if (!directory.exists())
            directory.mkdirs();
    }
}

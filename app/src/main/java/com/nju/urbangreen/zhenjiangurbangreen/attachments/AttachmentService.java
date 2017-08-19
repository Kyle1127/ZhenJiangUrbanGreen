package com.nju.urbangreen.zhenjiangurbangreen.attachments;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.nju.urbangreen.zhenjiangurbangreen.R;
import com.nju.urbangreen.zhenjiangurbangreen.util.SPUtils;
import com.nju.urbangreen.zhenjiangurbangreen.util.WebServiceUtils;

import net.gotev.uploadservice.BinaryUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadStatusDelegate;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by lxs on 17-8-18.
 */

public class AttachmentService {

    public static void renameAttach(Context context, final AttachmentRecord attach,
                                    final AttachmentService.Callback cb) {
        new MaterialDialog.Builder(context)
                .title("重命名文件")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("文件名", attach.fileName, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        attach.fileName = input.toString();
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        cb.success();
                    }
                })
                .show();
    }

    public static void uploadAttach(Context context, String parentID, final AttachmentRecord attach,
                                    final AttachmentService.Callback cb) {
        try {
            String uploadId = UUID.randomUUID().toString();
            HashMap<String, Object> params = new HashMap<>();
            params.put("ParentID", parentID);
            params.put("FileName", attach.fileName);
            params.put("FAID", attach.fileID);
            params.put("FileSize", attach.fileSize);
            String serverUrl = WebServiceUtils.getFileUploadUrl(params);
            Log.i("Upload", serverUrl);

            BinaryUploadRequest request = new BinaryUploadRequest(context, uploadId, serverUrl)
                    .setFileToUpload(attach.localPath)
                    .setMethod("POST")
//                    .setNotificationConfig(null)
                    .setNotificationConfig(getNotificationConfig(context, attach.fileName))
                    .setMaxRetries(SPUtils.getInt("MAX_RETRIES", 2))
                    .setDelegate(new UploadStatusDelegate() {
                        @Override
                        public void onProgress(Context context, UploadInfo uploadInfo) {
//                            UploadAttachNotification.updateNotificationProgress(uploadInfo);
                        }
                        @Override
                        public void onError(Context context, UploadInfo uploadInfo, ServerResponse serverResponse, Exception exception) {
//                            UploadAttachNotification.updateErrorNotification(uploadInfo);
                            attach.hasUpload = false;
                            cb.failed();
                        }
                        @Override
                        public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
//                            UploadAttachNotification.updateCompletedNotification(uploadInfo);
                            attach.hasUpload = true;
                            cb.success();
                        }
                        @Override
                        public void onCancelled(Context context, UploadInfo uploadInfo) {
//                            UploadAttachNotification.updateCancelNotification(uploadInfo);
                            attach.hasUpload = false;
                            cb.failed();
                        }
                    });
//            UploadAttachNotification.createNotification(uploadId, attach.fileName);
            request.startUpload();

        } catch (Exception exc) {
            Toast.makeText(context, exc.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private static UploadNotificationConfig getNotificationConfig(Context context, String filename) {
        UploadNotificationConfig config = new UploadNotificationConfig();

        PendingIntent clickIntent = PendingIntent.getActivity(context, 1,
                new Intent(context, AttachmentListActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        config.setTitleForAllStatuses(filename)
                .setRingToneEnabled(true)
                .setClickIntentForAllStatuses(clickIntent)
                .setClearOnActionForAllStatuses(true);

        config.getProgress().message = context.getResources().getString(R.string.uploading);

        config.getCompleted().message = context.getResources().getString(R.string.upload_success);
        config.getCompleted().iconColorResourceID = context.getResources().getColor(R.color.colorPrimary);

        config.getError().message = context.getResources().getString(R.string.upload_error);
        config.getError().iconColorResourceID = context.getResources().getColor(R.color.colorAccent);

        return config;
    }

    public interface Callback {
        void success();
        void failed();
    }
}

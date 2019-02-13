package com.example.yangdong.mediagiftplayerlibrary.utils.utils;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * Created by MrDong on 2019/2/12.
 */
public class FileUtil {
    //将raw里video拷贝到文件
    public static String copyFile(Context context, String fileName, int rawId) {

        File dir = context.getFilesDir();
        File path = new File(dir, fileName);
        if (path.exists()) {
            return path.toString();
        }
        final BufferedInputStream in = new BufferedInputStream(context.getResources().openRawResource(rawId));
        BufferedOutputStream out = null;
        try {
            out = new BufferedOutputStream(context.openFileOutput(path.getName(), Context.MODE_PRIVATE));
            byte[] buf = new byte[1024];
            int size = in.read(buf);
            while (size > 0) {
                out.write(buf, 0, size);
                size = in.read(buf);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return path.toString();
    }
}

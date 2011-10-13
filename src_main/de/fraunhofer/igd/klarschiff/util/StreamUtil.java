package de.fraunhofer.igd.klarschiff.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;


public class StreamUtil {

	static final int BUFFER_SIZE = 65536;

	public static void copyStreamContent(InputStream is, OutputStream os, boolean closeIs, boolean closeOs, String encoding) throws Exception
	{
		InputStreamReader isr = new InputStreamReader(is, encoding);
		PrintWriter pw = new PrintWriter(os, true);
		char[] buffer = new char[BUFFER_SIZE];
        int length;
        while( (length=isr.read(buffer, 0, BUFFER_SIZE))!=-1)
            pw.write(buffer, 0, length);
        if (closeIs) is.close();
        if (closeOs) os.close();

	}

	public static void copyStreamContent(InputStream is, OutputStream os, boolean closeIs, boolean closeOs) throws Exception
	{
		byte[] buffer = new byte[BUFFER_SIZE];
        int length;
        while( (length=is.read(buffer, 0, BUFFER_SIZE))!=-1)
            os.write(buffer, 0, length);
        if (closeIs) is.close();
        if (closeOs) os.close();

	}

	public static void copyStreamContent(InputStream is, OutputStream os, boolean closeIs, boolean closeOs, boolean autoFlush) throws Exception
	{
		byte[] buffer = new byte[BUFFER_SIZE];
        int length;
        while( (length=is.read(buffer, 0, BUFFER_SIZE))!=-1) {
        	os.write(buffer, 0, length);
        	if (autoFlush) os.flush();
        }
        if (closeIs) is.close();
        if (closeOs) os.close();

	}

    public static byte[] readInputStream(InputStream is) throws Exception
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        copyStreamContent(is, bos, true, true);
        return bos.toByteArray();
    }
}

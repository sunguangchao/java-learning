package NIO_IBM;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by 11981 on 2017/10/20.
 */
public class CopyFile {
    public static void main(String[] args) throws Exception{
        if (args.length < 2){
            System.err.println("Usage: java CopyFile infile outfile");
            System.exit(1);
        }

        String infile = args[0];
        String outfile = args[1];

        FileInputStream fin = new FileInputStream(infile);
        FileOutputStream fout = new FileOutputStream(outfile);

        FileChannel fcin = fin.getChannel();
        FileChannel fcout = fout.getChannel();

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        while (true){
            buffer.clear();
            int r = fcin.read(buffer);
            if (r == -1){
                break;
            }
            buffer.flip();
            fcout.write(buffer);
        }

    }
}

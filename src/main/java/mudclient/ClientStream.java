package mudclient;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;

public class ClientStream extends Buffer {
   private boolean socketException = false;
   private String socketExceptionMessage = "error in twriter";
   private byte[] buffer;
   private int endOffset;
   private int writeOffset;
   //private Thread thread;
   //private boolean closed = true;
   public int offset = 3;
   public byte[] buf;

   public ClientStream(InputStream var1) {
      super(var1);
   }

   public ClientStream(Socket socket) throws IOException {
	  super(socket);
      this.socket = socket;
   }

   public ClientStream(String var1) throws IOException {
      super(var1);
   }

   public ClientStream(byte[] var1) {
      super(var1);
   }

   public static ClientStream create(String var0, int var2) throws IOException {
      Socket var3;
      /*if(var1 != null) {
         var3 = new Socket(InetAddress.getByName(var1.getCodeBase().getHost()), var2);
      } else {
         var3 = new Socket(InetAddress.getByName(var0), var2);
      }*/
      var3 = new Socket(var0, var2);

      //var3.setSoTimeout(30000);
      return new ClientStream(var3);
   }

   public void close() {
      if(!super.closing) {
         try {
            if(super.socket != null) {
               super.socket.close();
            }

            //if(this.thread != null) {
               //this.closed = true;
               synchronized(this){
            	   this.notify();
               }

               //this.thread = null;
            //}

            if(super.input != null) {
               super.input.close();
            }

            if(super.output != null) {
               super.output.close();
            }

            this.buffer = null;
            this.buf = null;
         } catch (IOException var5) {
            System.out.println("Error closing stream");
         }
      }
   }

   public void writeBytes(byte[] var1, int var2, int var3) throws IOException {
      if(!super.closing) {
         super.output.write(var1, var2, var3);
      }
   }

   public void writeBytes(byte[] var1, int var2, int var3, boolean var4) throws IOException {
      if(!super.closing) {
         if(this.buffer == null) {
            this.buffer = new byte[5000];
         }

         synchronized(this) {
        	 for(int var7 = 0; var7 < var3; ++var7) {
                 this.buffer[this.writeOffset] = var1[var7 + var2];
                 this.writeOffset = (this.writeOffset + 1) % 5000;
                 if(this.writeOffset == (this.endOffset + 4900) % 5000) {
                    this.socketException = true;
                    this.socketExceptionMessage = "Write buffer full! " + var3;
                    var7 = var3 + 1;
                    //this.closed = true;
                    super.input.close();
                    super.output.close();
                    super.closing = true;
                    break;
                 }
              }

              if(var4) {
                 /*if(this.thread == null) {
                    this.closed = false;
                    this.thread = new Thread(this);
                    this.thread.setDaemon(true);
                    this.thread.setPriority(4);
                    this.thread.start();
                 }*/

                 this.notify();
              }
         }

         if(this.socketException) {
            throw new IOException(this.socketExceptionMessage);
         }
      }
   }

   public void flush() {
      synchronized(this){
    	  if(this.writeOffset != this.endOffset && this.buffer != null) {
              /*if(this.thread == null) {
                 this.closed = false;
                 this.thread = new Thread(this);
                 this.thread.setDaemon(true);
                 this.thread.setPriority(4);
                 this.thread.start();
              }*/

              this.notify();
              return;
           }
      }

   }

   public void newPacket(int var1) {
      if(this.buf == null) {
         this.buf = new byte[4000];
      }

      this.buf[2] = (byte)var1;
      this.offset = 3;
   }

   public void writeByte(int var1) {
      this.buf[this.offset++] = (byte)var1;
   }

   public void writeShort(int var1) {
      this.buf[this.offset++] = (byte)(var1 >> 8);
      this.buf[this.offset++] = (byte)var1;
   }

   public void writeInt(int var1) {
      this.buf[this.offset++] = (byte)(var1 >> 24);
      this.buf[this.offset++] = (byte)(var1 >> 16);
      this.buf[this.offset++] = (byte)(var1 >> 8);
      this.buf[this.offset++] = (byte)var1;
   }

   public void writeLong(long var1) {
      this.writeInt((int)(var1 >> 32));
      this.writeInt((int)(var1 & -1L));
   }

   public void writeString(String var1) {
      //var1.getBytes(0, var1.length(), this.buf, this.offset);
      System.arraycopy(var1.getBytes(), 0, this.buf, this.offset, var1.length());
      this.offset += var1.length();
   }

   public void putShort(int var1, int var2) {
      this.buf[var2++] = (byte)(var1 >> 8);
      this.buf[var2++] = (byte)var1;
   }

   public void flushPacket2() throws IOException {
      this.buf[0] = (byte)((this.offset - 2) / 256);
      this.buf[1] = (byte)(this.offset - 2 & 255);
      this.writeBytes(this.buf, 0, this.offset, true);
   }

   public void flushPacket() {
      this.buf[0] = (byte)((this.offset - 2) / 256);
      this.buf[1] = (byte)(this.offset - 2 & 255);

      try {
         this.writeBytes(this.buf, 0, this.offset, true);
      } catch (IOException var1) {
         ;
      }
   }

   public void sendPacket() {
      this.buf[0] = (byte)((this.offset - 2) / 256);
      this.buf[1] = (byte)(this.offset - 2 & 255);

      try {
         this.writeBytes(this.buf, 0, this.offset, false);
      } catch (IOException var1) {
         ;
      }
   }
}

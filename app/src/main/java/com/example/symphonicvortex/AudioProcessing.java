package com.example.symphonicvortex;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.*;
import java.util.Random;

@TargetApi(Build.VERSION_CODES.KITKAT)
public class AudioProcessing implements AutoCloseable{
	
	private WavFile wavFile;
	private int numChannels;
	public String status;
	private int framesRead;
    private double[] buffer;
    private double[] iBuffer;
    private Fft fft;
    private static int buffer_size = 48000*2;
    private static int PEAK_WIDTH = 4800;
    private static int PEAK_SCALE = 480;
    private static int MS_SCALE = 1000*PEAK_SCALE/48000;
    private BluetoothSender bt;
    Random rand;
    
	public AudioProcessing(String fileName, BluetoothSender b)
	{
		status = "No such file.";
        bt = b;
        rand = new Random(System.currentTimeMillis());
		try{
			// Open the wav file
			//File sdcard = Environment.getExternalStorageDirectory();
			// TODO: GENERALIZE THIS
			this.wavFile = WavFile.openWavFile(new File("/storage/extSdCard/", fileName));
		    // Get the number of audio channels in the wav file
		    this.numChannels = this.wavFile.getNumChannels();
		    buffer = new double[buffer_size * numChannels];
            iBuffer = new double[buffer_size * numChannels];
            fft = new Fft();

            status = "File Opened.";
		}
	    catch (Exception e)
	    {
	       System.err.println(e);
	    }
	}

    private boolean isPeak(double[] buff, int idx) {
        //check left
        for(int i = idx-1; i > 0 && i > idx - PEAK_WIDTH ; i-=PEAK_SCALE) {
            if (buff[idx] <= buff[i]) return false;
        }
        for(int i = idx+1; i < buff.length && i < idx + PEAK_WIDTH; i+=PEAK_SCALE) {
            if (buff[idx] <= buff[i]) return false;
        }
        return true;
    }

	public String getAudio() throws IOException, WavFileException {
      // Read frames into buffer
      framesRead = wavFile.readFrames(buffer, buffer_size);
      // Loop through frames in buffer and choose instruction to send
      double mean = 0;
      int idx = 0;
      double max = 0;
      for (int s=0 ; s<framesRead * numChannels ; s++)
      {
         //do something with buffer[s]
    	  mean += buffer[s];
          if (buffer[s] > max) {
              max = buffer[s];
              idx = s;
          }
      }

      Log.d("MEAN", Double.toString(mean));
      //mean /= (framesRead * numChannels);

      int num = rand.nextInt(3);

      String color1 = "custom(" +
                Integer.toString((int)mean%255) + "," +
                Integer.toString(((int)mean + 85)%255) + "," +
                Integer.toString(((int)mean + 170)%255) + ")";
      String color2 = "custom(" +
                Integer.toString(((int)mean+85)%255) + "," +
                Integer.toString(((int)mean)%255) + "," +
                Integer.toString(((int)mean + 170)%255) + ")";
      String color3 = "custom(" +
                Integer.toString(((int)mean+175)%255) + "," +
                Integer.toString(((int)mean + 85)%255) + "," +
                Integer.toString(((int)mean)%255) + ")";
      String color4 = "custom(" +
                Integer.toString((int)mean%255) + "," +
                Integer.toString(((int)mean + 170)%255) + "," +
                Integer.toString(((int)mean + 85)%255) + ")";
      String anim;
        int dur = 200;
      switch(num) {
          case 0:
              anim = "pulse ";
              int del = idx*250/48000;
              return "repeat 4\npulse " + del + "\n" +
                      anim + dur + " " +  color1 + "\n" +
                      "repeat end";
          case 1:
              anim = "squares ";
              break;
          default:
              if (rand.nextBoolean())
                anim = "spiral ";
              else
                anim = "spiral ";
      }

       String colors = " " + color1 + " " +  color2 + " " +  color3 + " " + color4 + "\n";
       int del = idx*250/48000;
        if (del < 0) del = 0;
        Log.d("VORTEXAUDIO", "read sample");
       return "repeat 4\ndelay " + del + "\n" +
               anim + dur + colors +
               "repeat end";

	}
	
	public String getInfo()
	{
		return wavFile.display();
	}
	
	public void close() throws Exception {
		wavFile.close();
	}
	
	public int getFramesRead()
	{
		return framesRead;
	}
}

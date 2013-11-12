package com.phoenix.police;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.util.Log;
//import android.widget.Toast;

import com.phoenix.data.Constants;
 
public class AudioRecordFunc { 
	
	private static final boolean LOG_SWITCH = Constants.LOG_SWITCH;
	private static final String LOG_TAG = AudioRecordFunc.class.getSimpleName();
	
    // �������ֽڴ�С  
    private int bufferSizeInBytes = 0;
     
    //AudioName����Ƶ����ļ� ����˷�
    private String AudioName = "";  
     
    //NewAudioName�ɲ��ŵ���Ƶ�ļ�  
    public String NewAudioName = "";
     
    private AudioRecord audioRecord;  
    private boolean isRecord = false;// ��������¼�Ƶ�״̬  
     
    private static final String audioPath = "police/audio/";
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    private static AudioRecordFunc mInstance; 
    private static Context mContext;
    
    private AudioRecordFunc(){
         
    }   
     
    public synchronized static AudioRecordFunc getInstance(Context context)
    {
    	mContext = context;
        if(mInstance == null) 
            mInstance = new AudioRecordFunc(); 
        return mInstance; 
    }
     
    public boolean startRecordAndFile() {
        //�ж��Ƿ����ⲿ�洢�豸sdcard
        if(AudioFileFunc.isSdcardExit())
        {
            if(isRecord)
            {
            	if (LOG_SWITCH)
					Log.d(LOG_TAG, "QiQiError:isrecording!");
                return false;
            }
            else
            {
                if(audioRecord == null)
                    creatAudioRecord();
                 
                audioRecord.startRecording();  
                // ��¼��״̬Ϊtrue  
                isRecord = true;  
                // ������Ƶ�ļ�д���߳�  
                new Thread(new AudioRecordThread()).start();  
                return true;
            }
             
        }       
        else
        {
        	if (LOG_SWITCH)
				Log.d(LOG_TAG, "QiQiError: There is no sdcard.");
            return false;           
        }       
 
    }  
   
    public void stopRecordAndFile() {  
        close();  
    }
     
     
    public long getRecordFileSize(){
        return AudioFileFunc.getFileSize(NewAudioName);
    }
     
   
    private void close() {  
        if (audioRecord != null) {  
            System.out.println("stopRecord");  
            isRecord = false;//ֹͣ�ļ�д��  
            audioRecord.stop();  
            audioRecord.release();//�ͷ���Դ  
            audioRecord = null;  
        }  
    }
     
     
    private void creatAudioRecord() {  
    	
        // ��ȡ��Ƶ�ļ�·��
        AudioName = AudioFileFunc.getRawFilePath();
        File folderPath = new File(AudioFileFunc.getWavFilePath() + audioPath);
        if(!folderPath.exists())
        	folderPath.mkdirs();
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(Constants.SETTING_PREFERENCES, Context.MODE_PRIVATE);
        String police_num = sharedPreferences.getString(Constants.SHARED_POL_NUM, Constants.SHARED_POL_NUM_DEF);
        NewAudioName = AudioFileFunc.getWavFilePath() + audioPath + Constants.AUDIO_NAME_HEAD + police_num + "_" + dateFormat.format(new Date()) +".wav";
         
        // ��û������ֽڴ�С  
        bufferSizeInBytes = AudioRecord.getMinBufferSize(AudioFileFunc.AUDIO_SAMPLE_RATE,  
                AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);  
         
        // ����AudioRecord����  
        audioRecord = new AudioRecord(AudioFileFunc.AUDIO_INPUT, AudioFileFunc.AUDIO_SAMPLE_RATE,  
                AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes);  
    }
     
     
    class AudioRecordThread implements Runnable {  
        @Override 
        public void run() {  
            writeDateTOFile();//���ļ���д�������  
            copyWaveFile(AudioName, NewAudioName);//������ݼ���ͷ�ļ�  
        }  
    }  
   
    /** 
     * ���ｫ���д���ļ������ǲ����ܲ��ţ���ΪAudioRecord��õ���Ƶ��ԭʼ������Ƶ�� 
     * �����Ҫ���žͱ������һЩ��ʽ���߱����ͷ��Ϣ����������ĺô���������Զ���Ƶ�� ����ݽ��д��?������Ҫ��һ����˵����TOM 
     * è������ͽ�����Ƶ�Ĵ��?Ȼ�����·�װ ����˵����õ�����Ƶ�Ƚ�������һЩ��Ƶ�Ĵ��? 
     */ 
    private void writeDateTOFile() {  
        // newһ��byte����������һЩ�ֽ���ݣ���СΪ�������С  
        byte[] audiodata = new byte[bufferSizeInBytes];  
        FileOutputStream fos = null;  
        int readsize = 0;  
        try {  
            File file = new File(AudioName);  
            if (file.exists()) {  
                file.delete();  
            }  
            fos = new FileOutputStream(file);// ����һ���ɴ�ȡ�ֽڵ��ļ�  
        } catch (Exception e) {  
            e.printStackTrace(); 
//            Toast.makeText(mContext, R.string.audio_fail, Toast.LENGTH_SHORT).show();
        }  
        while (isRecord == true) {  
            readsize = audioRecord.read(audiodata, 0, bufferSizeInBytes);  
            if (fos!=null) {  
                try {  
                    fos.write(audiodata);
                } catch (IOException e) {  
                    e.printStackTrace(); 
//                    Toast.makeText(mContext, R.string.audio_fail, Toast.LENGTH_SHORT).show();
                }  
            }  
        }  
        try {
            if(fos != null)
                fos.close();// �ر�д����  
        } catch (IOException e) {  
            e.printStackTrace(); 
//            Toast.makeText(mContext, R.string.audio_fail, Toast.LENGTH_SHORT).show();
        }  
        
//        Toast.makeText(mContext, R.string.audio_success, Toast.LENGTH_SHORT).show();
        
    }  
   
    // ����õ��ɲ��ŵ���Ƶ�ļ�  
    private void copyWaveFile(String inFilename, String outFilename) {  
        FileInputStream in = null;  
        FileOutputStream out = null;  
        long totalAudioLen = 0;  
        long totalDataLen = totalAudioLen + 36;  
        long longSampleRate = AudioFileFunc.AUDIO_SAMPLE_RATE;  
        int channels = 2;  
        long byteRate = 16 * AudioFileFunc.AUDIO_SAMPLE_RATE * channels / 8;  
        byte[] data = new byte[bufferSizeInBytes];  
        try {  
            in = new FileInputStream(inFilename);  
            out = new FileOutputStream(outFilename);  
            totalAudioLen = in.getChannel().size();  
            totalDataLen = totalAudioLen + 36;  
            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,  
                    longSampleRate, channels, byteRate);  
            while (in.read(data) != -1) {  
                out.write(data);  
            }  
            in.close();  
            out.close();  
        } catch (FileNotFoundException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
    }  
   
    /** 
     * �����ṩһ��ͷ��Ϣ��������Щ��Ϣ�Ϳ��Եõ����Բ��ŵ��ļ��� 
     * Ϊ��Ϊɶ������44���ֽڣ��������û�����о�������������һ��wav 
     * ��Ƶ���ļ������Է���ǰ���ͷ�ļ�����˵��һ��Ŷ��ÿ�ָ�ʽ���ļ����� 
     * �Լ����е�ͷ�ļ��� 
     */ 
    private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,  
            long totalDataLen, long longSampleRate, int channels, long byteRate)  
            throws IOException {  
        byte[] header = new byte[44];  
        header[0] = 'R'; // RIFF/WAVE header  
        header[1] = 'I';  
        header[2] = 'F';  
        header[3] = 'F';  
        header[4] = (byte) (totalDataLen & 0xff);  
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);  
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);  
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);  
        header[8] = 'W';  
        header[9] = 'A';  
        header[10] = 'V';  
        header[11] = 'E';  
        header[12] = 'f'; // 'fmt ' chunk  
        header[13] = 'm';  
        header[14] = 't';  
        header[15] = ' ';  
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk  
        header[17] = 0;  
        header[18] = 0;  
        header[19] = 0;  
        header[20] = 1; // format = 1  
        header[21] = 0;  
        header[22] = (byte) channels;  
        header[23] = 0;  
        header[24] = (byte) (longSampleRate & 0xff);  
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);  
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);  
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);  
        header[28] = (byte) (byteRate & 0xff);  
        header[29] = (byte) ((byteRate >> 8) & 0xff);  
        header[30] = (byte) ((byteRate >> 16) & 0xff);  
        header[31] = (byte) ((byteRate >> 24) & 0xff);  
        header[32] = (byte) (2 * 16 / 8); // block align  
        header[33] = 0;  
        header[34] = 16; // bits per sample  
        header[35] = 0;  
        header[36] = 'd';  
        header[37] = 'a';  
        header[38] = 't';  
        header[39] = 'a';  
        header[40] = (byte) (totalAudioLen & 0xff);  
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);  
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);  
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);  
        out.write(header, 0, 44);  
    }  
}
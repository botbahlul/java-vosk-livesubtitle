/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package org.vosk;

import java.awt.Color;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.swing.DefaultComboBoxModel;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CompletableFuture;
import javax.swing.SwingUtilities;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.json.JSONObject;

/**
 *
 * @author Bot Bahlul
 */
public class LiveSubtitle extends javax.swing.JFrame {

    /**
     * Creates new form NewJFrame
     */
    private final ArrayList<String> arraylist_models;
    private final ArrayList<String> arraylist_models_URL;
    private final ArrayList<String> arraylist_src;
    private final ArrayList<String> arraylist_src_languages;
    private final Map<String, String> map_model_language;
    private final Map<String, String> map_src_language;
    private final Map<String, String> map_language_models_URL;

    private final ArrayList<String> arraylist_dst;
    private final ArrayList<String> arraylist_dst_languages;
    private final Map<String, String> map_dst_language;

    //private OverlayTranslationWindow tptw = new OverlayTranslationWindow();
    private OverlayWindow tptw = new OverlayWindow();
    private Thread recognize_thread;
    public String file_separator = System.getProperty("file.separator");

    public LiveSubtitle() {
        this.arraylist_models = new ArrayList<>();
        this.arraylist_models_URL = new ArrayList<>();
        this.arraylist_src = new ArrayList<>();
        this.arraylist_src_languages = new ArrayList<>();
        this.map_model_language = new HashMap<>();
        this.map_language_models_URL = new HashMap<>();
        this.map_src_language = new HashMap<>();
        this.arraylist_dst = new ArrayList<>();
        this.arraylist_dst_languages = new ArrayList<>();
        this.map_dst_language = new HashMap<>();

        VOICE_TEXT.STRING = "";
        TRANSLATION_TEXT.STRING = "";

        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        DISPLAY.WIDTH = gd.getDisplayMode().getWidth();
        DISPLAY.HEIGHT = gd.getDisplayMode().getHeight();

        RECOGNIZING_STATUS.RECOGNIZING = false;
        VOSK_MODEL.DOWNLOADED = false;

        initComponents();
        String r = "";
        textview_debug.setText(r);

        combobox_src_language_listener src_action_listener = new combobox_src_language_listener();
        combobox_src_language.addItemListener(src_action_listener);

        combobox_dst_language_listener dst_action_listener = new combobox_dst_language_listener();
        combobox_dst_language.addItemListener(dst_action_listener);

        togglebutton_listener togglebutton_action_listener = new togglebutton_listener();
        togglebutton_start.setText("Start");
        togglebutton_start.addItemListener(togglebutton_action_listener);

        tptw.setVisible(false);

        Timer timer1 = new Timer();
        timer1.schedule(new TimerTask() {
            @Override
            public void run() {
                if (RECOGNIZING_STATUS.RECOGNIZING && VOICE_TEXT.STRING != null) {
                    translate(VOICE_TEXT.STRING, LANGUAGE.SRC, LANGUAGE.DST);
                }
            }
        },0,900);
    }

    class combobox_src_language_listener implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent evt) {
            String r = "";
            textview_debug.setText(r);
            if (evt.getStateChange() == ItemEvent.SELECTED) {
                String src_language = combobox_src_language.getSelectedItem().toString();
                LANGUAGE.SRC = map_src_language.get(src_language);
                VOSK_MODEL.ISO_CODE = map_model_language.get(src_language);
                VOSK_MODEL.URL_ADDRESS = map_language_models_URL.get(src_language);
                VOSK_MODEL.FILENAME = VOSK_MODEL.URL_ADDRESS.substring(VOSK_MODEL.URL_ADDRESS.lastIndexOf('/') + 1, VOSK_MODEL.URL_ADDRESS.length());
                VOSK_MODEL.COMPLETE_PATH = VOSK_MODEL.FILENAME;
                VOSK_MODEL.EXTRACTED_PATH = "models" + file_separator;
                VOSK_MODEL.USED_PATH = "models" + file_separator + VOSK_MODEL.ISO_CODE;
                check_vosk_model_download_status(VOSK_MODEL.ISO_CODE);
            }
            else {
                String src_language = combobox_src_language.getSelectedItem().toString();
                LANGUAGE.SRC = map_src_language.get(src_language);
                VOSK_MODEL.ISO_CODE = map_model_language.get(src_language);
                VOSK_MODEL.URL_ADDRESS = map_language_models_URL.get(src_language);
                VOSK_MODEL.FILENAME = VOSK_MODEL.URL_ADDRESS.substring(VOSK_MODEL.URL_ADDRESS.lastIndexOf('/') + 1, VOSK_MODEL.URL_ADDRESS.length());
                VOSK_MODEL.COMPLETE_PATH = VOSK_MODEL.FILENAME;
                VOSK_MODEL.EXTRACTED_PATH = "models" + file_separator;
                VOSK_MODEL.USED_PATH = "models" + file_separator + VOSK_MODEL.ISO_CODE;
                check_vosk_model_download_status(VOSK_MODEL.ISO_CODE);
            }
        }
    }

    class combobox_dst_language_listener implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent evt) {
            String r = "";
            textview_debug.setText(r);
            if (evt.getStateChange() == ItemEvent.SELECTED) {
                LANGUAGE.DST = map_dst_language.get(combobox_dst_language.getSelectedItem().toString());
            }
            else {
                LANGUAGE.DST = map_dst_language.get(combobox_dst_language.getSelectedItem().toString());
            }
        }
    }

    class togglebutton_listener implements ItemListener {
        Color bgColor = togglebutton_start.getBackground();
        Color fgColor = togglebutton_start.getForeground();

        @Override
        public void itemStateChanged(ItemEvent itemEvent) {
            String r = "";
            textview_debug.setText(r);
            int state = itemEvent.getStateChange();
            recognize_thread = new Thread(() -> {
                while (RECOGNIZING_STATUS.RECOGNIZING) {
                    StartRecognize();
                }
            });
            if (state == ItemEvent.SELECTED && VOSK_MODEL.DOWNLOADED) {
                RECOGNIZING_STATUS.RECOGNIZING = true;
                if (TRANSLATION_TEXT.STRING.length()>0) tptw.setVisible(true);
                togglebutton_start.setText("Stop");
                togglebutton_start.setBackground(Color.red);
                recognize_thread.start();
            } else {
                RECOGNIZING_STATUS.RECOGNIZING = false;
                if (!VOSK_MODEL.DOWNLOADED) {
                    textview_debug.setText("We need to download model first");
                    togglebutton_start.setSelected(false);
                }
                togglebutton_start.setText("Start");
                togglebutton_start.setBackground(bgColor);
                togglebutton_start.setForeground(fgColor);
                recognize_thread.interrupt();
                if (tptw != null) tptw.setVisible(false);
            }
        }
    };

    public void StartRecognize() {
        String r = "";
        textview_debug.setText(r);
        LibVosk.setLogLevel(LogLevel.DEBUG);
        AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 60000, 16, 2, 4, 44100, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        TargetDataLine microphone = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        if (RECOGNIZING_STATUS.RECOGNIZING) {
            try (Model model = new Model(VOSK_MODEL.USED_PATH);
                    Recognizer recognizer = new Recognizer(model, 120000)) {
                try {
                    microphone = (TargetDataLine) AudioSystem.getLine(info);
                    microphone.open(format);
                    microphone.start();

                    int numBytesRead;
                    int CHUNK_SIZE = 1024;
                    byte[] b = new byte[4096];

                    while (RECOGNIZING_STATUS.RECOGNIZING) {
                        numBytesRead = microphone.read(b, 0, CHUNK_SIZE);
                        out.write(b, 0, numBytesRead);
                        String string_Result = "";
                        String string_PartialResult = "";

                        if (recognizer.acceptWaveForm(b, numBytesRead)) {
                            string_Result = ((((((recognizer.getResult().replace("text", "")).replace("{", "")).replace("}", "")).replace(":", "")).replace("partial", "")).replace("\"", "")).toLowerCase();
                            if (string_Result.length()>0) {
                                textpane_voice_text.setText(string_Result);
                            }
                        } else {
                            string_PartialResult = ((((((recognizer.getPartialResult().replace("text", "")).replace("{", "")).replace("}", "")).replace(":", "")).replace("partial", "")).replace("\"", "")).toLowerCase();
                            if (string_PartialResult.length()>0) {
                                textpane_voice_text.setText(string_PartialResult);
                            }
                        }
                        VOICE_TEXT.STRING = string_Result + string_PartialResult;
                    }
                    /*String string_FinalResults = ((((((recognizer.getFinalResult().replace("text", "")).replace("{", "")).replace("}", "")).replace(":", "")).replace("partial", "")).replace("\"", "")).toLowerCase();
                    if (string_FinalResults.length()>0) {
                        VOICE_TEXT.STRING = string_FinalResults;
                        textpane_voice_text.setText(string_FinalResults);
                    }*/
                    microphone.close();

                } catch (LineUnavailableException e) {
                    textview_debug.setText(e.toString());
                }

            } catch (IOException ex) {
                Logger.getLogger(LiveSubtitle.class.getName()).log(Level.SEVERE, null, ex);
            }
        
        }
        else {
            if (microphone != null) microphone.close();
        }
    }

    public void translate(String t, String src, String dst) {
        String r = "";
        textview_debug.setText(r);
        GoogleTranslateTranslator translate = new GoogleTranslateTranslator();
        translate.setOnTranslationCompleteListener(new GoogleTranslateTranslator.OnTranslationCompleteListener() {
            @Override
            public void onStartTranslation() {}

            @Override
            public void onCompleted(String text) {
                TRANSLATION_TEXT.STRING = text;
                if (RECOGNIZING_STATUS.RECOGNIZING) {
                    textpane_translation_text.setText(text);
                    textpane_translation_text.setSelectionEnd(textpane_translation_text.getText().length());

                    tptw.setVisible(true);
                    tptw.textpane_translation_text.setText(text);
                    tptw.textpane_translation_text.updateUI();
                    tptw.textpane_translation_text.setSelectionEnd(tptw.textpane_translation_text.getText().length());
                }
                else {
                    tptw.textpane_translation_text.setText("");
                    tptw.setVisible(false);
                }
            }

            @Override
            public void onError(Exception e) {
            }
        });
        translate.execute(t, src, dst);
    }

    public final int get_vosk_model_filesize(String models_URL) {
        ExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.execute(() -> {
            try {
                URL url = new URL(models_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    String respond ="Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage();
                    textview_debug.setText(respond);
                } else {
                    String respond = "Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage();
                    textview_debug.setText(respond);
                    if (connection.getContentLength() > 0) {
                        VOSK_MODEL.FILESIZE = connection.getContentLength();
                        String string_filesize = "File size=" + VOSK_MODEL.FILESIZE + " bytes";
                        textview_filesize.setText(string_filesize);
                    }
                }
            } catch (IOException e) {
                check_vosk_model_download_status(VOSK_MODEL.ISO_CODE);
                textview_debug.setText(e.getMessage());
            }
        });
        return VOSK_MODEL.FILESIZE;
    }

    public void DownloadModel(String models_URL) {
        String r = "";
        textview_debug.setText(r);
        File dir = new File("models");
        if(!(dir.exists())){
            boolean mkdir = dir.mkdir();
            if (!mkdir) {
                textview_debug.setText("Directory creation failed");
            }
        }

        File edir = new File("models");
        if(!(edir.exists())){
            boolean mkdir = edir.mkdir();
            if (!mkdir) {
                textview_debug.setText("Directory creation failed");
            }
        }

        ExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.execute(new Runnable() {
            int count;

            @Override
            public void run() {
                try {
                    URL url = new URL(models_URL);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.connect();

                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        String respond ="Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage();
                        textview_debug.setText(respond);
                    } else {
                        String respond = "Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage();
                        textview_debug.setText(respond);
                        if (connection.getContentLength() > 0) {
                            int lengthOfFile = connection.getContentLength();
                            String string_filesize = "File size=" + lengthOfFile + " bytes";
                            textview_filesize.setVisible(true);
                            textview_filesize.setText(string_filesize);
                            try (InputStream input = connection.getInputStream();
                                    OutputStream output = new FileOutputStream(VOSK_MODEL.COMPLETE_PATH)) {
                                byte[] data = new byte[1024];
                                long total = 0;
                                while ((count = input.read(data)) != -1) {
                                    total += count;
                                    String string_bytes_received = "Bytes received=" + total + " bytes";
                                    textview_bytesdownloaded.setVisible(true);
                                    textview_bytesdownloaded.setText(string_bytes_received);
                                    mProgressBar.setIndeterminate(false);
                                    mProgressBar.setMaximum(100);
                                    if (lengthOfFile > 0) {
                                        publishProgress((int) (total * 100 / lengthOfFile));
                                    }
                                    output.write(data, 0, count);
                                }
                                output.flush();
                            }
                        }
                    }

                    executor.execute(() -> {
                        //GUI Thread work here
                        mProgressBar.setVisible(false);
                        textview_filesize.setVisible(false);
                        textview_bytesdownloaded.setVisible(false);
                        VOSK_MODEL.DOWNLOADED = true;
                        Decompress df = new Decompress(VOSK_MODEL.COMPLETE_PATH, VOSK_MODEL.EXTRACTED_PATH);
                        df.unzip();
                        File oldfolder = new File(VOSK_MODEL.EXTRACTED_PATH, VOSK_MODEL.FILENAME.replace(".zip", ""));
                        File newfolder = new File(VOSK_MODEL.EXTRACTED_PATH, VOSK_MODEL.ISO_CODE);
                        
                        boolean rendir = oldfolder.renameTo(newfolder);
                        if (!rendir) {
                            textview_debug.setText("Directory rename failed");
                        }
                        
                        File ddir = new File(VOSK_MODEL.COMPLETE_PATH);
                        deleteRecursively(ddir);
                        
                        VOSK_MODEL.USED_PATH = "models" + file_separator + VOSK_MODEL.ISO_CODE;
                        check_vosk_model_download_status(VOSK_MODEL.ISO_CODE);
                    });
                } catch (IOException e) {
                    check_vosk_model_download_status(VOSK_MODEL.ISO_CODE);
                    textview_debug.setText(e.getMessage());
                }
            }
        });
    }

    public void publishProgress(Integer... progress) {
        mProgressBar.setValue(progress[0]);
    }

    public void deleteRecursively(File fileOrDirectory) {
        String r = "";
        textview_debug.setText(r);
        if (fileOrDirectory.isDirectory())
            for (File child : (fileOrDirectory.listFiles())) {
                deleteRecursively(child);
            }
        boolean deldir = fileOrDirectory.delete();
        if (!deldir) {
            textview_debug.setText("Directory delete failed");
        }
    }

    public final void check_vosk_model_download_status(String string_model) {
        String r = "";
        textview_debug.setText(r);
        File edir = new File(VOSK_MODEL.EXTRACTED_PATH + file_separator + string_model);
        if ("en-US".equals(VOSK_MODEL.ISO_CODE)) {
            VOSK_MODEL.DOWNLOADED = true;
            button_delete_model.setVisible(false);
            button_download_model.setVisible(false);
            mProgressBar.setVisible(false);
            textview_model_URL.setVisible(false);
            textview_filesize.setVisible(false);
            textview_bytesdownloaded.setVisible(false);
            textview_model_used_path.setVisible(false);
        } else {
            if (edir.exists()) {
                VOSK_MODEL.DOWNLOADED = true;
                VOSK_MODEL.USED_PATH = "models" + file_separator + string_model;
                button_delete_model.setVisible(true);
                button_download_model.setVisible(false);
                mProgressBar.setVisible(false);
                textview_model_URL.setVisible(false);
                textview_filesize.setVisible(false);
                textview_bytesdownloaded.setVisible(false);
                textview_model_used_path.setVisible(true);
                String string_used_path = "Model used path=" + VOSK_MODEL.USED_PATH;
                textview_model_used_path.setText(string_used_path);
            } else {
                VOSK_MODEL.DOWNLOADED = false;
                VOSK_MODEL.USED_PATH = "";
                button_delete_model.setVisible(false);
                button_download_model.setVisible(true);
                mProgressBar.setVisible(false);
                textview_model_URL.setVisible(true);
                textview_filesize.setVisible(true);
                VOSK_MODEL.FILESIZE = get_vosk_model_filesize(VOSK_MODEL.URL_ADDRESS);
                String string_filesize = "File size=" + VOSK_MODEL.FILESIZE + " bytes";
                textview_filesize.setText(string_filesize);
                textview_model_used_path.setVisible(false);
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        label_translation_language = new javax.swing.JLabel();
        combobox_src_language = new javax.swing.JComboBox<>();
        jScrollPane6 = new javax.swing.JScrollPane();
        textpane_translation_text = new javax.swing.JTextPane();
        label_audio_language = new javax.swing.JLabel();
        combobox_dst_language = new javax.swing.JComboBox<>();
        jScrollPane7 = new javax.swing.JScrollPane();
        textpane_voice_text = new javax.swing.JTextPane();
        button_download_model = new javax.swing.JButton();
        button_delete_model = new javax.swing.JButton();
        mProgressBar = new javax.swing.JProgressBar();
        textview_model_URL = new javax.swing.JLabel();
        textview_filesize = new javax.swing.JLabel();
        textview_bytesdownloaded = new javax.swing.JLabel();
        textview_model_used_path = new javax.swing.JLabel();
        textview_debug = new javax.swing.JLabel();
        togglebutton_start = new javax.swing.JToggleButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("VOSK LIVE SUBTITLE");

        label_translation_language.setText("Translation Language");

        arraylist_models.add("ca-ES");
        arraylist_models.add("zh-CN");
        arraylist_models.add("cs-CZ");
        arraylist_models.add("nl-NL");
        arraylist_models.add("en-US");
        arraylist_models.add("eo-EO");
        arraylist_models.add("fr-FR");
        arraylist_models.add("de-DE");
        arraylist_models.add("hi-IN");
        arraylist_models.add("it-IT");
        arraylist_models.add("ja-JP");
        arraylist_models.add("kk-KZ");
        arraylist_models.add("fa-IR");
        arraylist_models.add("pl-PL");
        arraylist_models.add("pt-PT");
        arraylist_models.add("ru-RU");
        arraylist_models.add("es-ES");
        arraylist_models.add("sv-SE");
        arraylist_models.add("tr-TR");
        arraylist_models.add("uk-UA");
        arraylist_models.add("vi-VN");

        arraylist_models_URL.add("https://alphacephei.com/vosk/models/vosk-model-small-ca-0.4.zip");
        arraylist_models_URL.add("https://alphacephei.com/vosk/models/vosk-model-small-cn-0.22.zip");
        arraylist_models_URL.add("https://alphacephei.com/vosk/models/vosk-model-small-cs-0.4-rhasspy.zip");
        arraylist_models_URL.add("https://alphacephei.com/vosk/models/vosk-model-small-nl-0.22.zip");
        arraylist_models_URL.add("https://alphacephei.com/vosk/models/vosk-model-small-en-us-0.15.zip");
        arraylist_models_URL.add("https://alphacephei.com/vosk/models/vosk-model-small-eo-0.42.zip");
        arraylist_models_URL.add("https://alphacephei.com/vosk/models/vosk-model-small-fr-0.22.zip");
        arraylist_models_URL.add("https://alphacephei.com/vosk/models/vosk-model-small-de-0.15.zip");
        arraylist_models_URL.add("https://alphacephei.com/vosk/models/vosk-model-small-hi-0.22.zip");
        arraylist_models_URL.add("https://alphacephei.com/vosk/models/vosk-model-small-it-0.22.zip");
        arraylist_models_URL.add("https://alphacephei.com/vosk/models/vosk-model-small-ja-0.22.zip");
        arraylist_models_URL.add("https://alphacephei.com/vosk/models/vosk-model-small-kz-0.15.zip");
        arraylist_models_URL.add("https://alphacephei.com/vosk/models/vosk-model-small-fa-0.5.zip");
        arraylist_models_URL.add("https://alphacephei.com/vosk/models/vosk-model-small-pl-0.22.zip");
        arraylist_models_URL.add("https://alphacephei.com/vosk/models/vosk-model-small-pt-0.3.zip");
        arraylist_models_URL.add("https://alphacephei.com/vosk/models/vosk-model-small-ru-0.22.zip");
        arraylist_models_URL.add("https://alphacephei.com/vosk/models/vosk-model-small-es-0.42.zip");
        arraylist_models_URL.add("https://alphacephei.com/vosk/models/vosk-model-small-sv-rhasspy-0.15.zip");
        arraylist_models_URL.add("https://alphacephei.com/vosk/models/vosk-model-small-tr-0.3.zip");
        arraylist_models_URL.add("https://alphacephei.com/vosk/models/vosk-model-small-uk-v3-small.zip");
        arraylist_models_URL.add("https://alphacephei.com/vosk/models/vosk-model-small-vn-0.3.zip");

        arraylist_src.add("ca");
        arraylist_src.add("zh");
        arraylist_src.add("cs");
        arraylist_src.add("nl");
        arraylist_src.add("en");
        arraylist_src.add("eo");
        arraylist_src.add("fr");
        arraylist_src.add("de");
        arraylist_src.add("hi");
        arraylist_src.add("it");
        arraylist_src.add("ja");
        arraylist_src.add("kk");
        arraylist_src.add("fa");
        arraylist_src.add("pl");
        arraylist_src.add("pt");
        arraylist_src.add("ru");
        arraylist_src.add("es");
        arraylist_src.add("sv");
        arraylist_src.add("tr");
        arraylist_src.add("uk");
        arraylist_src.add("vi");

        arraylist_src_languages.add("Catalan");
        arraylist_src_languages.add("Chinese");
        arraylist_src_languages.add("Czech");
        arraylist_src_languages.add("Dutch");
        arraylist_src_languages.add("English");
        arraylist_src_languages.add("Esperanto");
        arraylist_src_languages.add("French");
        arraylist_src_languages.add("German");
        arraylist_src_languages.add("Hindi");
        arraylist_src_languages.add("Italian");
        arraylist_src_languages.add("Japanese");
        arraylist_src_languages.add("Kazakh");
        arraylist_src_languages.add("Persian");
        arraylist_src_languages.add("Polish");
        arraylist_src_languages.add("Portuguese");
        arraylist_src_languages.add("Russian");
        arraylist_src_languages.add("Spanish");
        arraylist_src_languages.add("Swedish");
        arraylist_src_languages.add("Turkish");
        arraylist_src_languages.add("Ukrainian");
        arraylist_src_languages.add("Vietnamese");

        for (int i = 0; i < arraylist_src_languages.size(); i++) {
            map_model_language.put(arraylist_src_languages.get(i), arraylist_models.get(i));
        }

        for (int i = 0; i < arraylist_src_languages.size(); i++) {
            map_src_language.put(arraylist_src_languages.get(i), arraylist_src.get(i));
        }

        for (int i = 0; i < arraylist_src_languages.size(); i++) {
            map_language_models_URL.put(arraylist_src_languages.get(i), arraylist_models_URL.get(i));
        }
        combobox_src_language.setModel(new DefaultComboBoxModel<>(arraylist_src_languages.toArray(new String[0])));
        combobox_src_language.setSelectedIndex(arraylist_src_languages.indexOf("English"));

        String src_language = combobox_src_language.getSelectedItem().toString();
        LANGUAGE.SRC = map_src_language.get(src_language);
        VOSK_MODEL.ISO_CODE = map_model_language.get(src_language);
        VOSK_MODEL.URL_ADDRESS = map_language_models_URL.get(src_language);
        VOSK_MODEL.FILENAME = VOSK_MODEL.URL_ADDRESS.substring(VOSK_MODEL.URL_ADDRESS.lastIndexOf('/') + 1, VOSK_MODEL.URL_ADDRESS.length());
        VOSK_MODEL.COMPLETE_PATH = VOSK_MODEL.FILENAME;
        VOSK_MODEL.EXTRACTED_PATH = "models" + file_separator;
        VOSK_MODEL.USED_PATH = "models" + file_separator + VOSK_MODEL.ISO_CODE;

        check_vosk_model_download_status(VOSK_MODEL.ISO_CODE);

        jScrollPane6.setViewportView(textpane_translation_text);

        label_audio_language.setText("Audio Language");

        arraylist_dst.add("af");
        arraylist_dst.add("sq");
        arraylist_dst.add("am");
        arraylist_dst.add("ar");
        arraylist_dst.add("hy");
        arraylist_dst.add("as");
        arraylist_dst.add("ay");
        arraylist_dst.add("az");
        arraylist_dst.add("bm");
        arraylist_dst.add("eu");
        arraylist_dst.add("be");
        arraylist_dst.add("bn");
        arraylist_dst.add("bho");
        arraylist_dst.add("bs");
        arraylist_dst.add("bg");
        arraylist_dst.add("ca");
        arraylist_dst.add("ceb");
        arraylist_dst.add("ny");
        arraylist_dst.add("zh-Hans");
        arraylist_dst.add("zh-Hant");
        arraylist_dst.add("co");
        arraylist_dst.add("cr");
        arraylist_dst.add("cs");
        arraylist_dst.add("da");
        arraylist_dst.add("dv");
        arraylist_dst.add("nl");
        arraylist_dst.add("doi");
        arraylist_dst.add("en");
        arraylist_dst.add("eo");
        arraylist_dst.add("et");
        arraylist_dst.add("ee");
        arraylist_dst.add("fil");
        arraylist_dst.add("fi");
        arraylist_dst.add("fr");
        arraylist_dst.add("fy");
        arraylist_dst.add("gl");
        arraylist_dst.add("ka");
        arraylist_dst.add("de");
        arraylist_dst.add("el");
        arraylist_dst.add("gn");
        arraylist_dst.add("gu");
        arraylist_dst.add("ht");
        arraylist_dst.add("ha");
        arraylist_dst.add("haw");
        arraylist_dst.add("he");
        arraylist_dst.add("hi");
        arraylist_dst.add("hmn");
        arraylist_dst.add("hu");
        arraylist_dst.add("is");
        arraylist_dst.add("ig");
        arraylist_dst.add("ilo");
        arraylist_dst.add("id");
        arraylist_dst.add("ga");
        arraylist_dst.add("it");
        arraylist_dst.add("ja");
        arraylist_dst.add("jv");
        arraylist_dst.add("kn");
        arraylist_dst.add("kk");
        arraylist_dst.add("km");
        arraylist_dst.add("rw");
        arraylist_dst.add("kok");
        arraylist_dst.add("ko");
        arraylist_dst.add("kri");
        arraylist_dst.add("kmr");
        arraylist_dst.add("ckb");
        arraylist_dst.add("ky");
        arraylist_dst.add("lo");
        arraylist_dst.add("la");
        arraylist_dst.add("lv");
        arraylist_dst.add("ln");
        arraylist_dst.add("lt");
        arraylist_dst.add("lg");
        arraylist_dst.add("lb");
        arraylist_dst.add("mk");
        arraylist_dst.add("mg");
        arraylist_dst.add("ms");
        arraylist_dst.add("ml");
        arraylist_dst.add("mt");
        arraylist_dst.add("mi");
        arraylist_dst.add("mr");
        arraylist_dst.add("mni");
        arraylist_dst.add("lus");
        arraylist_dst.add("mn");
        arraylist_dst.add("mmr");
        arraylist_dst.add("ne");
        arraylist_dst.add("no");
        arraylist_dst.add("or");
        arraylist_dst.add("om");
        arraylist_dst.add("ps");
        arraylist_dst.add("fa");
        arraylist_dst.add("pl");
        arraylist_dst.add("pt");
        arraylist_dst.add("pa");
        arraylist_dst.add("qu");
        arraylist_dst.add("ro");
        arraylist_dst.add("ru");
        arraylist_dst.add("sm");
        arraylist_dst.add("sa");
        arraylist_dst.add("gd");
        arraylist_dst.add("nso");
        arraylist_dst.add("sr");
        arraylist_dst.add("st");
        arraylist_dst.add("sn");
        arraylist_dst.add("sd");
        arraylist_dst.add("si");
        arraylist_dst.add("sk");
        arraylist_dst.add("sl");
        arraylist_dst.add("so");
        arraylist_dst.add("es");
        arraylist_dst.add("su");
        arraylist_dst.add("sw");
        arraylist_dst.add("sv");
        arraylist_dst.add("tg");
        arraylist_dst.add("ta");
        arraylist_dst.add("tt");
        arraylist_dst.add("te");
        arraylist_dst.add("th");
        arraylist_dst.add("ti");
        arraylist_dst.add("ts");
        arraylist_dst.add("tr");
        arraylist_dst.add("tk");
        arraylist_dst.add("tw");
        arraylist_dst.add("ug");
        arraylist_dst.add("uk");
        arraylist_dst.add("ur");
        arraylist_dst.add("uz");
        arraylist_dst.add("vi");
        arraylist_dst.add("cy");
        arraylist_dst.add("xh");
        arraylist_dst.add("yi");
        arraylist_dst.add("yo");
        arraylist_dst.add("zu");

        arraylist_dst_languages.add("Afrikaans");
        arraylist_dst_languages.add("Albanian");
        arraylist_dst_languages.add("Amharic");
        arraylist_dst_languages.add("Arabic");
        arraylist_dst_languages.add("Armenian");
        arraylist_dst_languages.add("Assamese");
        arraylist_dst_languages.add("Aymara");
        arraylist_dst_languages.add("Azerbaijani");
        arraylist_dst_languages.add("Bambara");
        arraylist_dst_languages.add("Basque");
        arraylist_dst_languages.add("Belarusian");
        arraylist_dst_languages.add("Bengali (Bangla)");
        arraylist_dst_languages.add("Bhojpuri");
        arraylist_dst_languages.add("Bosnian");
        arraylist_dst_languages.add("Bulgarian");
        arraylist_dst_languages.add("Catalan");
        arraylist_dst_languages.add("Cebuano");
        arraylist_dst_languages.add("Chichewa, Nyanja");
        arraylist_dst_languages.add("Chinese (Simplified)");
        arraylist_dst_languages.add("Chinese (Traditional)");
        arraylist_dst_languages.add("Corsican");
        arraylist_dst_languages.add("Croatian");
        arraylist_dst_languages.add("Czech");
        arraylist_dst_languages.add("Danish");
        arraylist_dst_languages.add("Divehi, Maldivian");
        arraylist_dst_languages.add("Dogri");
        arraylist_dst_languages.add("Dutch");
        arraylist_dst_languages.add("English");
        arraylist_dst_languages.add("Esperanto");
        arraylist_dst_languages.add("Estonian");
        arraylist_dst_languages.add("Ewe");
        arraylist_dst_languages.add("Filipino");
        arraylist_dst_languages.add("Finnish");
        arraylist_dst_languages.add("French");
        arraylist_dst_languages.add("Frisian");
        arraylist_dst_languages.add("Galician");
        arraylist_dst_languages.add("Georgian");
        arraylist_dst_languages.add("German");
        arraylist_dst_languages.add("Greek");
        arraylist_dst_languages.add("Guarani");
        arraylist_dst_languages.add("Gujarati");
        arraylist_dst_languages.add("Haitian Creole");
        arraylist_dst_languages.add("Hausa");
        arraylist_dst_languages.add("Hawaiian");
        arraylist_dst_languages.add("Hebrew");
        arraylist_dst_languages.add("Hindi");
        arraylist_dst_languages.add("Hmong");
        arraylist_dst_languages.add("Hungarian");
        arraylist_dst_languages.add("Icelandic");
        arraylist_dst_languages.add("Igbo");
        arraylist_dst_languages.add("Ilocano");
        arraylist_dst_languages.add("Indonesian");
        arraylist_dst_languages.add("Irish");
        arraylist_dst_languages.add("Italian");
        arraylist_dst_languages.add("Japanese");
        arraylist_dst_languages.add("Javanese");
        arraylist_dst_languages.add("Kannada");
        arraylist_dst_languages.add("Kazakh");
        arraylist_dst_languages.add("Khmer");
        arraylist_dst_languages.add("Kinyarwanda (Rwanda)");
        arraylist_dst_languages.add("Konkani");
        arraylist_dst_languages.add("Korean");
        arraylist_dst_languages.add("Krio");
        arraylist_dst_languages.add("Kurdish (Kurmanji)");
        arraylist_dst_languages.add("Kurdish (Sorani)");
        arraylist_dst_languages.add("Kyrgyz");
        arraylist_dst_languages.add("Lao");
        arraylist_dst_languages.add("Latin");
        arraylist_dst_languages.add("Latvian (Lettish)");
        arraylist_dst_languages.add("Lingala");
        arraylist_dst_languages.add("Lithuanian");
        arraylist_dst_languages.add("Luganda, Ganda");
        arraylist_dst_languages.add("Luxembourgish");
        arraylist_dst_languages.add("Macedonian");
        arraylist_dst_languages.add("Malagasy");
        arraylist_dst_languages.add("Malay");
        arraylist_dst_languages.add("Malayalam");
        arraylist_dst_languages.add("Maltese");
        arraylist_dst_languages.add("Maori");
        arraylist_dst_languages.add("Marathi");
        arraylist_dst_languages.add("Meiteilon (Manipuri)");
        arraylist_dst_languages.add("Mizo");
        arraylist_dst_languages.add("Mongolian");
        arraylist_dst_languages.add("Myanmar (Burmese)");
        arraylist_dst_languages.add("Nepali");
        arraylist_dst_languages.add("Norwegian");
        arraylist_dst_languages.add("Oriya");
        arraylist_dst_languages.add("Oromo (Afaan Oromo)");
        arraylist_dst_languages.add("Pashto, Pushto");
        arraylist_dst_languages.add("Persian (Farsi)");
        arraylist_dst_languages.add("Polish");
        arraylist_dst_languages.add("Portuguese");
        arraylist_dst_languages.add("Punjabi (Eastern)");
        arraylist_dst_languages.add("Quechua");
        arraylist_dst_languages.add("Romanian, Moldavian");
        arraylist_dst_languages.add("Russian");
        arraylist_dst_languages.add("Samoan");
        arraylist_dst_languages.add("Sanskrit");
        arraylist_dst_languages.add("Scots Gaelic");
        arraylist_dst_languages.add("Sepedi");
        arraylist_dst_languages.add("Serbian");
        arraylist_dst_languages.add("Sesotho");
        arraylist_dst_languages.add("Shona");
        arraylist_dst_languages.add("Sindhi");
        arraylist_dst_languages.add("Sinhalese");
        arraylist_dst_languages.add("Slovak");
        arraylist_dst_languages.add("Slovenian");
        arraylist_dst_languages.add("Somali");
        arraylist_dst_languages.add("Spanish");
        arraylist_dst_languages.add("Sundanese");
        arraylist_dst_languages.add("Swahili (Kiswahili)");
        arraylist_dst_languages.add("Swedish");
        arraylist_dst_languages.add("Tajik");
        arraylist_dst_languages.add("Tamil");
        arraylist_dst_languages.add("Tatar");
        arraylist_dst_languages.add("Telugu");
        arraylist_dst_languages.add("Thai");
        arraylist_dst_languages.add("Tigrinya");
        arraylist_dst_languages.add("Tsonga");
        arraylist_dst_languages.add("Turkish");
        arraylist_dst_languages.add("Turkmen");
        arraylist_dst_languages.add("Twi");
        arraylist_dst_languages.add("Ukrainian");
        arraylist_dst_languages.add("Urdu");
        arraylist_dst_languages.add("Uyghur");
        arraylist_dst_languages.add("Uzbek");
        arraylist_dst_languages.add("Vietnamese");
        arraylist_dst_languages.add("Welsh");
        arraylist_dst_languages.add("Xhosa");
        arraylist_dst_languages.add("Yiddish");
        arraylist_dst_languages.add("Yoruba");
        arraylist_dst_languages.add("Zulu");

        for (int i = 0; i < arraylist_dst_languages.size(); i++) {
            map_dst_language.put(arraylist_dst_languages.get(i), arraylist_dst.get(i));
        }

        combobox_dst_language.setModel(new DefaultComboBoxModel<>(arraylist_dst_languages.toArray(new String[0])));
        combobox_dst_language.setSelectedIndex(arraylist_dst_languages.indexOf("Indonesian"));

        LANGUAGE.DST = map_dst_language.get(combobox_dst_language.getSelectedItem().toString());

        jScrollPane7.setViewportView(textpane_voice_text);

        button_download_model.setText("Download Model");
        button_download_model.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_download_modelActionPerformed(evt);
            }
        });

        button_delete_model.setText("Delete Model");
        button_delete_model.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_delete_modelActionPerformed(evt);
            }
        });

        textview_model_URL.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        textview_model_URL.setText("Model URL=" + VOSK_MODEL.URL_ADDRESS);

        textview_filesize.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        textview_filesize.setText(String.valueOf(VOSK_MODEL.FILESIZE) + " bytes");

        textview_bytesdownloaded.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        textview_bytesdownloaded.setText("[bytes received]");

        textview_model_used_path.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        textview_model_used_path.setText("Model used path=" + VOSK_MODEL.USED_PATH);

        textview_debug.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        textview_debug.setText("[debug messages]");

        togglebutton_start.setText("Start");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(mProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(button_download_model, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(button_delete_model, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(textview_bytesdownloaded, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(textview_model_URL, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(textview_filesize, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(textview_model_used_path, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(label_translation_language, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(combobox_dst_language, 0, 308, Short.MAX_VALUE))
                    .addComponent(jScrollPane7, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane6)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(label_audio_language, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(combobox_src_language, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(textview_debug, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(togglebutton_start, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(label_audio_language, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(combobox_src_language))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(button_delete_model)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(button_download_model)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 6, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textview_model_URL, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textview_filesize, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textview_bytesdownloaded, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textview_model_used_path, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(label_translation_language, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(combobox_dst_language, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(togglebutton_start)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textview_debug)
                .addContainerGap(152, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void button_download_modelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_download_modelActionPerformed
        mProgressBar.setValue(0);
        if (!VOSK_MODEL.DOWNLOADED) {
            File edir = new File("models");
            if (!(edir.exists())) {
                boolean mkdir = edir.mkdir();
                if (!mkdir) {
                    textview_debug.setText("Directory creation failed");
                }
            }

            mProgressBar.setVisible(true);
            textview_filesize.setVisible(true);
            textview_bytesdownloaded.setVisible(true);

            new Thread(() -> {
                DownloadModel(VOSK_MODEL.URL_ADDRESS);
            }).start();

        } else {
            String msg = "Model has been downloaded, no need to download it again";
            textview_debug.setText(msg);
        }
    }//GEN-LAST:event_button_download_modelActionPerformed

    private void button_delete_modelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_delete_modelActionPerformed
        File ddir = new File(VOSK_MODEL.USED_PATH);
        if (ddir.exists()) {
            deleteRecursively(ddir);
            String msg = ddir + "deleted";
            textview_debug.setText(msg);
        }
        check_vosk_model_download_status(VOSK_MODEL.ISO_CODE);
    }//GEN-LAST:event_button_delete_modelActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(LiveSubtitle.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(LiveSubtitle.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(LiveSubtitle.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(LiveSubtitle.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new LiveSubtitle().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton button_delete_model;
    private javax.swing.JButton button_download_model;
    private javax.swing.JComboBox<String> combobox_dst_language;
    private javax.swing.JComboBox<String> combobox_src_language;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JLabel label_audio_language;
    private javax.swing.JLabel label_translation_language;
    private javax.swing.JProgressBar mProgressBar;
    private javax.swing.JTextPane textpane_translation_text;
    private javax.swing.JTextPane textpane_voice_text;
    private javax.swing.JLabel textview_bytesdownloaded;
    public static javax.swing.JLabel textview_debug;
    private javax.swing.JLabel textview_filesize;
    private javax.swing.JLabel textview_model_URL;
    private javax.swing.JLabel textview_model_used_path;
    private javax.swing.JToggleButton togglebutton_start;
    // End of variables declaration//GEN-END:variables
}

class VOICE_TEXT {
    public static String STRING;
}

class TRANSLATION_TEXT {
    public static String STRING;
}

class DISPLAY {
    public static int WIDTH;
    public static int HEIGHT;
}

class RECOGNIZING_STATUS {
    public static boolean RECOGNIZING;
}

class VOSK_MODEL {
    public static String ISO_CODE;
    public static String SRC;
    public static String URL_ADDRESS;
    public static String FILENAME;
    public static int FILESIZE;
    public static String COMPLETE_PATH;
    public static String EXTRACTED_PATH;
    public static String USED_PATH;
    public static boolean DOWNLOADED;
}

class LANGUAGE {
    public static String SRC;
    public static String DST;
}


abstract class AsyncTask <Params, Progress, Result> {
    protected AsyncTask() {}

    protected abstract void onPreExecute();

    protected abstract Result doInBackground(Params... params) ;

    protected abstract void onProgressUpdate(Progress... progress) ;

    protected abstract void onPostExecute(Result result) ;

    /*final void  publishProgress(Progress... values) {
        SwingUtilities.invokeLater(() -> this.onProgressUpdate(values) );
    }*/

    final AsyncTask<Params, Progress, Result> execute(Params... params) {
        try {
            SwingUtilities.invokeAndWait( this::onPreExecute );
        } catch (InvocationTargetException|InterruptedException e){
        
        }

        CompletableFuture<Result> cf =  CompletableFuture.supplyAsync( () -> doInBackground(params) );

        cf.thenAccept(this::onPostExecute);
        return this;
    }
}


class GoogleTranslateTranslator extends AsyncTask<String, String, String> {
    private OnTranslationCompleteListener listener;
    @Override
    protected String doInBackground(String... strings) {
        String str = "";
        try {
            String encode = URLEncoder.encode(((String[]) strings)[0], "utf-8");
            String sb = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=" +
                    ((String[]) strings)[1] +
                    "&tl=" +
                    ((String[]) strings)[2] +
                    "&dt=t&q=" +
                    encode;
            HttpResponse execute = new DefaultHttpClient().execute(new HttpGet(sb));
            StatusLine statusLine = execute.getStatusLine();
            if (statusLine.getStatusCode() == 200) {
                String byteArrayOutputStream2;
                try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
                    execute.getEntity().writeTo(byteArrayOutputStream);
                    byteArrayOutputStream2 = byteArrayOutputStream.toString();
                }
                JSONArray jSONArray;
                jSONArray = new JSONArray(byteArrayOutputStream2).getJSONArray(0);
                StringBuilder translation = new StringBuilder(str);
                for (int i = 0; i < jSONArray.length(); i++) {
                    JSONArray jSONArray2 = jSONArray.getJSONArray(i);
                    translation.append(jSONArray2.get(0).toString());
                }
                return translation.toString();
            }
            execute.getEntity().getContent().close();
            throw new IOException(statusLine.getReasonPhrase());
        } catch (IOException | IllegalStateException | JSONException e) {
            //System.out.println("GoogleTranslateTranslator: " + e.getMessage());
            LiveSubtitle.textview_debug.setText("GoogleTranslateTranslator: " + e.getMessage());
            listener.onError(e);
            return str;
        }
    }
    @Override
    protected void onPreExecute() {
        listener.onStartTranslation();
    }
    @Override
    protected void onPostExecute(String text) {
        listener.onCompleted(text);
        //System.out.println(text);
    }

    @Override
    protected void onProgressUpdate(String... progress) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    public interface OnTranslationCompleteListener{
        void onStartTranslation();
        void onCompleted(String text);
        void onError(Exception e);
    }
    public void setOnTranslationCompleteListener(OnTranslationCompleteListener listener){
        this.listener=listener;
    }
}

class GoogleClient5Translator extends AsyncTask<String, String, String> {
    private OnTranslationCompleteListener listener;

    @Override
    protected String doInBackground(String... strings) {
        String[] strArr = (String[]) strings;
        String str = "";
        try {
            String encode = URLEncoder.encode(strArr[0], "utf-8");
            StringBuilder sb = new StringBuilder();
            sb.append("https://clients5.google.com/translate_a/");
            sb.append("single?dj=1&dt=t&dt=sp&dt=ld&dt=bd&client=dict-chrome-ex&sl=");
            sb.append(((String[]) strings)[1]);
            sb.append("&tl=");
            sb.append(((String[]) strings)[2]);
            sb.append("&q=");
            sb.append(encode);
            HttpResponse execute = new DefaultHttpClient().execute(new HttpGet(sb.toString()));
            StatusLine statusLine = execute.getStatusLine();
            if (statusLine.getStatusCode() == 200) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                execute.getEntity().writeTo(byteArrayOutputStream);
                String byteArrayOutputStream2 = byteArrayOutputStream.toString();
                byteArrayOutputStream.close();

                JSONObject jo = new JSONObject(byteArrayOutputStream2);
                JSONArray ja_sentences = jo.getJSONArray("sentences");

                StringBuilder translation = new StringBuilder(str);
                for (int i = 0; i < ja_sentences.length(); i++) {
                    JSONObject jo_trans = ja_sentences.getJSONObject(i);
                    String str_trans = jo_trans.getString("trans");
                    translation.append(str_trans);
                }
                return translation.toString();
            }
            execute.getEntity().getContent().close();
            throw new IOException(statusLine.getReasonPhrase());
        } catch (IOException | UnsupportedOperationException | JSONException e) {
            //System.out.println("GoogleClient5Translator: " + e.getMessage());
            LiveSubtitle.textview_debug.setText("GoogleClient5Translator: " + e.getMessage());
            listener.onError(e);
            return str;
        }
    }

    @Override
    protected void onPreExecute() {
        listener.onStartTranslation();
    }

    @Override
    protected void onPostExecute(String text) {
        listener.onCompleted(text);
    }

    @Override
    protected void onProgressUpdate(String... progress) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public interface OnTranslationCompleteListener{
        void onStartTranslation();
        void onCompleted(String text);
        void onError(Exception e);
    }
    public void setOnTranslationCompleteListener(OnTranslationCompleteListener listener){
        this.listener=listener;
    }

}

class Decompress {
    private final String _zipFile;
    private final String _extract_location;

    public Decompress(String zipFile, String extract_location) {
        _zipFile = zipFile;
        _extract_location = extract_location;
        _dirChecker("");
    }

    public void unzip() {
        try  {
            FileInputStream fin = new FileInputStream(_zipFile);
            try (ZipInputStream zin = new ZipInputStream(fin)) {
                byte b[] = new byte[1024];
                
                ZipEntry ze = null;
                while ((ze = zin.getNextEntry()) != null) {
                    
                    //System.out.println("Decompress, Unzipping " + ze.getName());
                    LiveSubtitle.textview_debug.setText("Decompress, Unzipping " + ze.getName());

                    if(ze.isDirectory()) {
                        _dirChecker(ze.getName());
                    } else {
                        FileOutputStream fout = new FileOutputStream(_extract_location + ze.getName());
                        
                        BufferedInputStream in = new BufferedInputStream(zin);
                        BufferedOutputStream out;
                        out = new BufferedOutputStream(fout);
                        
                        int n;
                        while ((n = in.read(b,0,1024)) >= 0) {
                            out.write(b,0,n);
                        }
                        
                        zin.closeEntry();
                        out.close();
                    }

                }
            }
        } catch(IOException e) {
            //System.out.println("Decompress, unzip " + e);
            LiveSubtitle.textview_debug.setText("Decompress, unzip " + e);
        }

    }

    private void _dirChecker(String dir) {
        File f = new File(_extract_location + dir);

        if(!f.isDirectory()) {
            f.mkdirs();
        }
    }
}

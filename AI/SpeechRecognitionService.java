package AI;

import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;

import java.io.*;
import java.nio.file.*;
import java.sql.Time;
import java.util.*;
import javax.sound.sampled.*;
import org.json.JSONObject;

import Object.*;
import Back.*;

public class SpeechRecognitionService {
    private static final String BASE_MODEL_DIR = "vosk-models";

    private String currentLanguage = "en";
    private Model model = null;

    public SpeechRecognitionService() {
        LibVosk.setLogLevel(LogLevel.INFO);
    }

    public SpeechRecognitionService(String language) {
        this();
        this.currentLanguage = language;
    }

    public void setLanguage(String language) {
        if (!language.equals(this.currentLanguage)) {
            if (model != null) {
                model.close();
                model = null;
            }
            this.currentLanguage = language;
        }
    }

    public String getModelPath() {
        return BASE_MODEL_DIR + File.separator + currentLanguage;
    }

    public void loadModel(String modelPath) throws IOException {
        try {
            model = new Model(modelPath);
            if (!"en".equals(currentLanguage)) {
                File modelDir = new File(modelPath);
                if (!modelDir.exists() || !modelDir.isDirectory()) {
                    throw new IOException(
                            "Không tìm thấy thư mục mô hình. Vui lòng cấu hình mô hình thủ công tại: " + modelPath);
                }
            }
        } catch (IOException e) {
            throw new IOException("Không thể tải mô hình Vosk từ: " + modelPath, e);
        }
    }

    public List<Sentence> extractSentencesFromAudio(String mp3FilePath, int recordFileId) throws Exception {
        if (model == null) {
            String modelPath = getModelPath();
            if (!Files.exists(Paths.get(modelPath))) {
                throw new IOException("Không tìm thấy mô hình Vosk cho ngôn ngữ '" + currentLanguage
                        + "' tại đường dẫn '" + modelPath + "'. Vui lòng tải mô hình trước.");
            }
            loadModel(getModelPath());
        }
        String wavFilePath = convertToWav(mp3FilePath);
        File wavFile = new File(wavFilePath);

        try {
            List<RecognizedWord> words = recognizeAudio(wavFile);
            List<SentenceSegment> sentenceSegments = groupWordsIntoSentences(words);
            List<Sentence> sentences = new ArrayList<>();
            for (SentenceSegment segment : sentenceSegments) {
                Sentence sentence = new Sentence();
                sentence.id = IdCounterQuery.getIdThenIncrease("Sentence");
                sentence.recordFileId = recordFileId;
                sentence.startTime = convertSecondsToTime(segment.getStartTime());
                sentence.endTime = convertSecondsToTime(segment.getEndTime());
                String recognizedText = segment.getText();

                if (containsNonASCII(recognizedText)) {
                    try {
                        byte[] utf8Bytes = recognizedText.getBytes("UTF-8");
                        String decodedText = new String(utf8Bytes, "UTF-8");
                        StringBuilder unicodeInfo = new StringBuilder();
                        for (int j = 0; j < recognizedText.length(); j++) {
                            char c = recognizedText.charAt(j);
                            unicodeInfo.append(c)
                                    .append("(U+")
                                    .append(String.format("%04X", (int) c))
                                    .append(") ");
                        }

                        if (!decodedText.equals(recognizedText)) {
                        }
                        recognizedText = decodedText;
                    } catch (Exception e) {
                        System.err.println("Lỗi xử lý văn bản non-ASCII: " + e.getMessage());
                    }
                }

                sentence.content = recognizedText;
                sentences.add(sentence);
            }

            return sentences;
        } finally {
            if (wavFile.exists()) {
                boolean deleted = wavFile.delete();
                if (!deleted) {
                    System.err.println("Cảnh báo: Không thể xóa tệp WAV tạm: " + wavFilePath);
                } else {
                }
            }
        }
    }

    private String convertToWav(String mp3FilePath) throws Exception {
        String outputPath = mp3FilePath.replace(".mp3", "_processed.wav");
        File outputFile = new File(outputPath);
        if (outputFile.exists()) {
            outputFile.delete();
        }

        String[] ffmpegCommand = new String[] {
                "ffmpeg/ffmpeg.exe",
                "-i", mp3FilePath,
                "-ar", "16000",
                "-ac", "1",
                "-c:a", "pcm_s16le",
                "-q:a", "0",
                outputPath
        };

        ProcessBuilder pb = new ProcessBuilder(ffmpegCommand);

        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new Exception("Chuyển đổi FFmpeg thất bại với mã thoát: " + exitCode);
        }

        return outputPath;
    }

    private List<RecognizedWord> recognizeAudio(File wavFile) throws Exception {
        List<RecognizedWord> words = new ArrayList<>();

        try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(wavFile)) {
            AudioFormat format = audioInputStream.getFormat();
            if (format.getSampleRate() != 16000 || format.getChannels() != 1) {
                throw new Exception("Định dạng âm thanh không hỗ trợ. Phải là 16kHz mono.");
            }

            Recognizer recognizer = new Recognizer(model, 16000);
            recognizer.setWords(true);

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = audioInputStream.read(buffer)) != -1) {
                if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                    String result = recognizer.getResult();
                    processResult(result, words);
                }
            }

            String finalResult = recognizer.getFinalResult();
            processResult(finalResult, words);
            recognizer.close();
        }

        return words;
    }

    private void processResult(String jsonResult, List<RecognizedWord> words) {
        JSONObject json = new JSONObject(jsonResult);
        if (json.has("result")) {
            var result = json.getJSONArray("result");
            for (int i = 0; i < result.length(); i++) {
                JSONObject wordObj = result.getJSONObject(i);
                RecognizedWord word = new RecognizedWord();
                String wordText = wordObj.getString("word");

                String method = "Windows-1252";
                try {
                    if (method != null) {
                        byte[] encodedStr = wordText.getBytes(method);
                        String utf8Text = new String(encodedStr, "UTF-8");
                        wordText = utf8Text;
                    }
                } catch (UnsupportedEncodingException e) {
                    System.err.println("Lỗi sửa mã hóa cho từ: " + wordText);
                }
                if (wordText.contains("'")) {
                    wordText = wordText.replace("'", "''");
                }

                word.setText(wordText);
                word.setStartTime(wordObj.getDouble("start"));
                word.setEndTime(wordObj.getDouble("end"));
                words.add(word);
            }
        }
    }

    private List<SentenceSegment> groupWordsIntoSentences(List<RecognizedWord> words) {
        List<SentenceSegment> sentences = new ArrayList<>();

        if (words.isEmpty()) {
            return sentences;
        }

        final double SENTENCE_PAUSE_THRESHOLD = 0.7;

        StringBuilder currentSentence = new StringBuilder();
        double sentenceStart = words.get(0).getStartTime();
        double lastWordEnd = 0;

        for (int i = 0; i < words.size(); i++) {
            RecognizedWord word = words.get(i);
            if (i > 0 && (word.getStartTime() - lastWordEnd) > SENTENCE_PAUSE_THRESHOLD) {
                if (currentSentence.length() > 0) {
                    SentenceSegment segment = new SentenceSegment();
                    segment.setText(currentSentence.toString().trim());
                    segment.setStartTime(sentenceStart);
                    segment.setEndTime(lastWordEnd);
                    sentences.add(segment);

                    currentSentence = new StringBuilder();
                    sentenceStart = word.getStartTime();
                }
            }

            if (currentSentence.length() > 0) {
                currentSentence.append(" ");
            }
            currentSentence.append(word.getText());

            lastWordEnd = word.getEndTime();

            String wordText = word.getText();
            if (i == words.size() - 1 ||
                    wordText.endsWith(".") ||
                    wordText.endsWith("?") ||
                    wordText.endsWith("!")) {

                SentenceSegment segment = new SentenceSegment();
                segment.setText(currentSentence.toString().trim());
                segment.setStartTime(sentenceStart);
                segment.setEndTime(word.getEndTime());
                sentences.add(segment);

                currentSentence = new StringBuilder();
                if (i < words.size() - 1) {
                    sentenceStart = words.get(i + 1).getStartTime();
                }
            }
        }

        if (currentSentence.length() > 0) {
            SentenceSegment segment = new SentenceSegment();
            segment.setText(currentSentence.toString().trim());
            segment.setStartTime(sentenceStart);
            segment.setEndTime(lastWordEnd);
            sentences.add(segment);
        }

        return sentences;
    }

    private Time convertSecondsToTime(double seconds) {
        long milliseconds = (long) (seconds * 1000);
        return new Time(milliseconds);
    }

    private static class RecognizedWord {
        private String text;
        private double startTime;
        private double endTime;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public double getStartTime() {
            return startTime;
        }

        public void setStartTime(double startTime) {
            this.startTime = startTime;
        }

        public double getEndTime() {
            return endTime;
        }

        public void setEndTime(double endTime) {
            this.endTime = endTime;
        }
    }

    public static class SentenceSegment {
        private String text;
        private double startTime;
        private double endTime;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public double getStartTime() {
            return startTime;
        }

        public void setStartTime(double startTime) {
            this.startTime = startTime;
        }

        public double getEndTime() {
            return endTime;
        }

        public void setEndTime(double endTime) {
            this.endTime = endTime;
        }

        @Override
        public String toString() {
            return String.format("%s - %s: %s",
                    formatTime(startTime), formatTime(endTime), text);
        }

        private String formatTime(double seconds) {
            int hours = (int) (seconds / 3600);
            int minutes = (int) ((seconds % 3600) / 60);
            int secs = (int) (seconds % 60);
            return String.format("%d:%02d:%02d", hours, minutes, secs);
        }
    }

    private boolean containsNonASCII(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) > 127) {
                return true;
            }
        }

        return false;
    }
}

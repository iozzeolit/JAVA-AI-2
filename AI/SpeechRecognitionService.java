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

public class SpeechRecognitionService {    // Cập nhật BASE_MODEL_DIR để hỗ trợ cả ba ngôn ngữ
    private static final String BASE_MODEL_DIR = "vosk-models";
    
    private String currentLanguage = "en"; // Ngôn ngữ mặc định là tiếng Anh
    private Model model = null;

    /**
     * Khởi tạo dịch vụ nhận dạng giọng nói
     */
    public SpeechRecognitionService() {
        // Đặt mức độ ghi nhật ký để tránh quá nhiều thông báo gỡ lỗi
        LibVosk.setLogLevel(LogLevel.INFO);
    }

    /**
     * Khởi tạo dịch vụ nhận dạng giọng nói với một ngôn ngữ cụ thể
     * 
     * @param language Mã ngôn ngữ (ví dụ: "en", "vi", "ja")
     */
    public SpeechRecognitionService(String language) {
        this();
        this.currentLanguage = language;
    }

    /**
     * Đặt ngôn ngữ cho nhận dạng giọng nói
     * 
     * @param language Mã ngôn ngữ (ví dụ: "en", "vi", "ja")
     */
    public void setLanguage(String language) {
        if (!language.equals(this.currentLanguage)) {
            // Ngôn ngữ đã thay đổi, cần đóng mô hình hiện tại và tải mô hình mới
            if (model != null) {
                model.close();
                model = null;
            }
            this.currentLanguage = language;
        }
    }    /**
     * Lấy đường dẫn mô hình cho ngôn ngữ hiện tại
     * 
     * @return Đường dẫn đến thư mục mô hình ngôn ngữ
     */
    public String getModelPath() {
        return BASE_MODEL_DIR + File.separator + currentLanguage;
    }

    /**
     * Tải mô hình âm thanh
     * 
     * @param modelPath Đường dẫn đến thư mục mô hình
     * @throws IOException Nếu không thể tải mô hình
     */
    public void loadModel(String modelPath) throws IOException {
        try {            model = new Model(modelPath);

            // Đối với các mô hình không phải tiếng Anh, kiểm tra xem chúng đã được tải đúng chưa
            if (!"en".equals(currentLanguage)) {
                // Kiểm tra xem thư mục có tồn tại và liệt kê nội dung của nó để gỡ lỗi
                File modelDir = new File(modelPath);
                if (!modelDir.exists() || !modelDir.isDirectory()) {
                    throw new IOException("Không tìm thấy thư mục mô hình. Vui lòng cấu hình mô hình thủ công tại: " + modelPath);
                }
            }
        } catch (IOException e) {
            throw new IOException("Không thể tải mô hình Vosk từ: " + modelPath, e);
        }
    }    /**
     * Extract sentences from an MP3 file
     * 
     * @param mp3FilePath  Path to the MP3 file
     * @param recordFileId ID of the record file in the database
     * @return List of extracted sentences with timestamps
     * @throws Exception If an error occurs during extraction
     */
    public List<Sentence> extractSentencesFromAudio(String mp3FilePath, int recordFileId) throws Exception {
        // Kiểm tra xem mô hình đã được tải chưa
        if (model == null) {
            String modelPath = getModelPath();
            // Thử tải mô hình cho ngôn ngữ hiện tại
            if (!Files.exists(Paths.get(modelPath))) {
                throw new IOException("Không tìm thấy mô hình Vosk cho ngôn ngữ '" + currentLanguage + "' tại đường dẫn '" + modelPath + "'. Vui lòng tải mô hình trước.");
            }
            loadModel(getModelPath());
        } // Bước 1: Chuyển đổi MP3 sang định dạng WAV
        String wavFilePath = convertToWav(mp3FilePath);
        File wavFile = new File(wavFilePath);

        try {
            // Bước 2: Xử lý tệp WAV với Vosk
            List<RecognizedWord> words = recognizeAudio(wavFile);

            // Bước 3: Gom nhóm từ thành câu
            List<SentenceSegment> sentenceSegments = groupWordsIntoSentences(words);

            // Bước 4: Chuyển đổi sang đối tượng cơ sở dữ liệu
            List<Sentence> sentences = new ArrayList<>();
            for (SentenceSegment segment : sentenceSegments) {
                Sentence sentence = new Sentence();
                sentence.id = IdCounterQuery.getIdThenIncrease("Sentence");
                sentence.recordFileId = recordFileId;
                sentence.startTime = convertSecondsToTime(segment.getStartTime());
                sentence.endTime = convertSecondsToTime(segment.getEndTime());                
                String recognizedText = segment.getText();                // Thông tin gỡ lỗi
                
                // Kiểm tra dấu nháy đơn trước (ảnh hưởng cả ASCII và non-ASCII)
                if (recognizedText.contains("'")) {
                    // Sẽ xử lý dấu nháy đơn trong QPiece.convN(), chỉ log ở đây
                }
                
                // Xử lý đặc biệt cho ngôn ngữ không phải ASCII (Nhật, Trung, Việt, ...)
                if (containsNonASCII(recognizedText)) {
                    try {
                        // Cố gắng sửa lỗi mã hóa với văn bản non-ASCII
                        // Một số trường hợp văn bản bị mã hóa kép hoặc sai mã hóa

                        // Đảm bảo mã hóa Unicode đúng để lưu vào CSDL
                        byte[] utf8Bytes = recognizedText.getBytes("UTF-8");
                        String decodedText = new String(utf8Bytes, "UTF-8");                        // In thông tin gỡ lỗi chi tiết
                        String langInfo = currentLanguage; // Mã ngôn ngữ để gỡ lỗi
                        // Tạo chuỗi ký tự Unicode dễ đọc
                        StringBuilder unicodeInfo = new StringBuilder();
                        for (int j = 0; j < recognizedText.length(); j++) {
                            char c = recognizedText.charAt(j);
                            unicodeInfo.append(c)
                                     .append("(U+")
                                     .append(String.format("%04X", (int)c))
                                     .append(") ");
                        }

                        // Sử dụng văn bản đã giải mã rõ ràng để lưu vào CSDL
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
            // Xóa tệp WAV tạm sau khi phân tích xong
            if (wavFile.exists()) {
                boolean deleted = wavFile.delete();
                if (!deleted) {
                    System.err.println("Cảnh báo: Không thể xóa tệp WAV tạm: " + wavFilePath);
                } else {
                }
            }
        }
    }  

    /**
     * Chuyển đổi MP3 sang WAV bằng FFmpeg
     * 
     * @param mp3FilePath Đường dẫn tệp MP3
     * @return Đường dẫn tệp WAV đã chuyển đổi
     * @throws Exception Nếu chuyển đổi thất bại
     */
    private String convertToWav(String mp3FilePath) throws Exception {
        // Tạo đường dẫn tệp đầu ra
        String outputPath = mp3FilePath.replace(".mp3", "_processed.wav");
        File outputFile = new File(outputPath);

        // Nếu tệp đã tồn tại, xóa nó
        if (outputFile.exists()) {
            outputFile.delete();
        } // Chạy FFmpeg để chuyển MP3 sang WAV (16kHz, 16-bit, mono)
        // Sử dụng thiết lập chất lượng cao cho mọi ngôn ngữ để đảm bảo nhận dạng tốt
        String[] ffmpegCommand = new String[] {
                "ffmpeg/ffmpeg.exe",
                "-i", mp3FilePath,
                "-ar", "16000", // Tần số mẫu 16kHz (bắt buộc bởi Vosk)
                "-ac", "1", // mono
                "-c:a", "pcm_s16le", // PCM 16-bit
                "-q:a", "0", // Chất lượng cao nhất cho mọi ngôn ngữ
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

    /**
     * Nhận diện từ trong tệp âm thanh với dấu thời gian
     * 
     * @param wavFile Tệp âm thanh WAV
     * @return Danh sách từ nhận diện với dấu thời gian
     * @throws Exception Nếu nhận diện thất bại
     */
    private List<RecognizedWord> recognizeAudio(File wavFile) throws Exception {
        List<RecognizedWord> words = new ArrayList<>();

        try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(wavFile)) {
            // Lấy định dạng âm thanh
            AudioFormat format = audioInputStream.getFormat();

            // Kiểm tra định dạng có tương thích với Vosk không (phải là 16kHz, 16-bit, mono)
            if (format.getSampleRate() != 16000 || format.getChannels() != 1) {
                throw new Exception("Định dạng âm thanh không hỗ trợ. Phải là 16kHz mono.");
            }

            // Tạo recognizer
            Recognizer recognizer = new Recognizer(model, 16000);
            recognizer.setWords(true); // Bật dấu thời gian từ

            // Xử lý âm thanh theo từng khối
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = audioInputStream.read(buffer)) != -1) {
                // Xử lý khối âm thanh
                if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                    // Lấy kết quả dạng JSON
                    String result = recognizer.getResult();
                    processResult(result, words);
                }
            }

            // Xử lý kết quả cuối cùng
            String finalResult = recognizer.getFinalResult();
            processResult(finalResult, words);

            // Giải phóng tài nguyên
            recognizer.close();
        }

        return words;
    }

    /**
     * Xử lý kết quả JSON từ Vosk và trích xuất từ với dấu thời gian
     * 
     * @param jsonResult Kết quả JSON từ Vosk
     * @param words      Danh sách để thêm từ đã trích xuất
     */
    private void processResult(String jsonResult, List<RecognizedWord> words) {
        JSONObject json = new JSONObject(jsonResult);
        if (json.has("result")) {
            var result = json.getJSONArray("result");
            for (int i = 0; i < result.length(); i++) {
                JSONObject wordObj = result.getJSONObject(i);
                RecognizedWord word = new RecognizedWord();
                // Lấy văn bản từ trực tiếp từ JSON
                String wordText = wordObj.getString("word");

                String method = "Windows-1252";
                try {
                    if (method != null)
                    {
                        byte[] encodedStr = wordText.getBytes(method);
                        String utf8Text = new String(encodedStr, "UTF-8");
                        wordText = utf8Text;
                    }    
                } catch (UnsupportedEncodingException e) {
                    System.err.println("Lỗi sửa mã hóa cho từ: " + wordText);
                }

                // Thoát dấu nháy đơn cho an toàn SQL
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
    
    /**
     * Group words into sentences based on pauses and punctuation
     * 
     * @param words List of recognized words
     * @return List of sentence segments
     */
    private List<SentenceSegment> groupWordsIntoSentences(List<RecognizedWord> words) {
        List<SentenceSegment> sentences = new ArrayList<>();

        if (words.isEmpty()) {
            return sentences;
        }

        // Parameters for sentence segmentation
        final double SENTENCE_PAUSE_THRESHOLD = 0.7; // Pause threshold in seconds to consider a new sentence

        StringBuilder currentSentence = new StringBuilder();
        double sentenceStart = words.get(0).getStartTime();
        double lastWordEnd = 0;

        for (int i = 0; i < words.size(); i++) {
            RecognizedWord word = words.get(i);

            // Check if there's a significant pause before this word
            if (i > 0 && (word.getStartTime() - lastWordEnd) > SENTENCE_PAUSE_THRESHOLD) {
                // End the current sentence
                if (currentSentence.length() > 0) {
                    SentenceSegment segment = new SentenceSegment();
                    segment.setText(currentSentence.toString().trim());
                    segment.setStartTime(sentenceStart);
                    segment.setEndTime(lastWordEnd);
                    sentences.add(segment);

                    // Start a new sentence
                    currentSentence = new StringBuilder();
                    sentenceStart = word.getStartTime();
                }
            }

            // Add the current word to the sentence
            if (currentSentence.length() > 0) {
                currentSentence.append(" ");
            }
            currentSentence.append(word.getText());

            lastWordEnd = word.getEndTime();

            // End sentence at the end of the list or if word ends with punctuation
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

                // Reset for next sentence
                currentSentence = new StringBuilder();
                if (i < words.size() - 1) {
                    sentenceStart = words.get(i + 1).getStartTime();
                }
            }
        }

        // Add the last sentence if anything is left
        if (currentSentence.length() > 0) {
            SentenceSegment segment = new SentenceSegment();
            segment.setText(currentSentence.toString().trim());
            segment.setStartTime(sentenceStart);
            segment.setEndTime(lastWordEnd);
            sentences.add(segment);
        }

        return sentences;
    }

    /**
     * Load the Vosk model for the specified language
     * 
     * @param language Language code (e.g., "en", "vi")
     * @throws Exception If model loading fails
     */
    // Removed the loadModelForLanguage method entirely

    /**
     * Convert seconds to a SQL Time object
     * 
     * @param seconds Number of seconds
     * @return Time object
     */
    private Time convertSecondsToTime(double seconds) {
        long milliseconds = (long) (seconds * 1000);
        return new Time(milliseconds);
    }

    /**
     * Class to represent a recognized word with timestamp
     */
    private static class RecognizedWord {
        private String text;
        private double startTime;
        private double endTime;
        // private double confidence;

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

        // public double getConfidence() {
        // return confidence;
        // }

        // public void setConfidence(double confidence) {
        // this.confidence = confidence;
        // }
    }

    /**
     * Class to represent a sentence segment
     */
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

    /**
     * Check if a string contains non-ASCII characters
     * 
     * @param str String to check for non-ASCII characters
     * @return true if the string contains any character outside the ASCII range
     *         (0-127)
     */
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

package com.himedia.luckydokiaiapi.domain.audio;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/audio")
@RequiredArgsConstructor
public class AudioController {

    private final OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel;

    @Value("classpath:/response.mp4")
    private Resource resource;


    @GetMapping("/transcribe")
    public ResponseEntity<String> transcribe(){
        // ResponseFormat.TEXT: 텍스트 형식으로 변환
        OpenAiAudioTranscriptionOptions options=OpenAiAudioTranscriptionOptions.builder()
                .withResponseFormat(OpenAiAudioApi.TranscriptResponseFormat.TEXT)
                .withTemperature(0f)
                .build();

        AudioTranscriptionPrompt audioTranscriptionPrompt=new AudioTranscriptionPrompt(resource, options);
        AudioTranscriptionResponse audioTranscriptionResponse=openAiAudioTranscriptionModel.call(audioTranscriptionPrompt);

        return new ResponseEntity<>(audioTranscriptionResponse.getResult().getOutput(), HttpStatus.OK);
    }

    // 오디오 파일 업로드 후 변환
    @PostMapping("/transcribe")
    public ResponseEntity<String> transcribe(@RequestParam("file") MultipartFile file) throws Exception {
        log.info("Audio transcribe received: {}", file.getOriginalFilename());
        Resource resource = file.getResource();
        // Set transcription options
        OpenAiAudioTranscriptionOptions options = OpenAiAudioTranscriptionOptions.builder()
                .withResponseFormat(OpenAiAudioApi.TranscriptResponseFormat.TEXT)
                .withLanguage("ko")
                .withTemperature(0f)
                //.withResponseFormat(this.responseFormat)
                .build();

        // Create a transcription prompt
        AudioTranscriptionPrompt audioTranscriptionPrompt =
                new AudioTranscriptionPrompt(resource, options);

        // Call the transcription API
        AudioTranscriptionResponse audioTranscriptionResponse = openAiAudioTranscriptionModel.call(audioTranscriptionPrompt);

        // Return the transcribed text
        return new ResponseEntity<>(audioTranscriptionResponse.getResult().getOutput(), HttpStatus.OK);
    }

}

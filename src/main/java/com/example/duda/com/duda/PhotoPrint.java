package com.example.duda.com.duda;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.print.attribute.standard.Media;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

@RestController
@RequestMapping("/duda")
public class PhotoPrint {

    @Autowired
    private VideoStreamService videoService;

    public static Integer imageIndex = 0;
    public static String sign = "+";
    private String imageName = "duda";

    @GetMapping("/view")
    public Mono<ResponseEntity<?>> next(@RequestHeader(value = "Range", required = false) String httpRangeList) throws IOException {
        imageName = "duda"+imageIndex;

        String[] filenames =  new ClassPathResource("image").getFile().list();

        String filename = Stream.of(filenames).filter(f -> f.startsWith(imageName)).findFirst().orElse("init.gif");

        File file = new File(filename);

        String content = MediaType.IMAGE_JPEG_VALUE;
        if(filename.endsWith("gif")){
            content = MediaType.IMAGE_GIF_VALUE;
        }
        if(filename.endsWith("mp4")){
            return Mono.just(videoService.prepareContent(filename.substring(0, filename.lastIndexOf(".")),"mp4", httpRangeList));
        }

        if(imageIndex <= 0) {
            sign = "+";
        }
        if(imageIndex == filenames.length-1){
            file = new File("end.gif");
        }

        if(imageIndex > filenames.length-1) {
            sign = "-";
            return Mono.just(ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).location(URI.create("https://photos.app.goo.gl/Jb2xTMbd3abTZvjaA")).build());
        }

        var imgFile = new ClassPathResource("image/"+file.toPath().toString());
        if(sign.equalsIgnoreCase("+")){
            imageIndex++;
        }else {
            imageIndex--;
        }



        return Mono.just(ResponseEntity
                .ok()
                .header("Content-Type", content)
                .body(new InputStreamResource(imgFile.getInputStream())));
    }
    @GetMapping("/view/force")
    public ResponseEntity force() {
        if(sign.equalsIgnoreCase("+")){
            imageIndex++;
        }else {
            imageIndex--;
        }
        return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).location(URI.create("/duda/view")).build();

    }

    @GetMapping("/view/force/{value}")
    public ResponseEntity forceIndex(@PathVariable("value") Integer value) {
        imageIndex = value;
        return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).location(URI.create("/duda/view")).build();

    }
}

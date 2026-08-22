package br.com.ajudamutua.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class DocumentStorageService {
    public static final long MAX_SIZE = 5L * 1024 * 1024;
    private final Path root;
    public DocumentStorageService(@Value("${app.documents.path:./data/aid-documents}") String path){
        try { this.root=Paths.get(path).toAbsolutePath().normalize(); Files.createDirectories(root); }
        catch(IOException e){ throw new IllegalStateException("Não foi possível preparar armazenamento de documentos",e); }
    }
    public Stored store(MultipartFile file) {
        if(file==null||file.isEmpty()) throw new IllegalArgumentException("Arquivo obrigatório");
        if(file.getSize()>MAX_SIZE) throw new IllegalArgumentException("Arquivo excede 5 MB");
        try {
            byte[] bytes=file.getBytes();
            String contentType=detect(bytes);
            String ext=switch(contentType){case "application/pdf"->".pdf";case "image/jpeg"->".jpg";case "image/png"->".png";default->throw new IllegalArgumentException("Tipo não permitido");};
            String key=UUID.randomUUID()+ext;
            Path target=root.resolve(key).normalize();
            if(!target.getParent().equals(root)) throw new IllegalStateException("Caminho de armazenamento inválido");
            Files.write(target,bytes,StandardOpenOption.CREATE_NEW);
            String hash=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
            return new Stored(key,contentType,bytes.length,hash);
        } catch(IOException e){throw new IllegalStateException("Falha ao armazenar documento",e);} catch(java.security.NoSuchAlgorithmException e){throw new IllegalStateException(e);}
    }
    public Resource load(String key){
        try { Path p=root.resolve(key).normalize(); if(!p.getParent().equals(root)||!Files.isRegularFile(p)) throw new IllegalArgumentException("Documento não encontrado"); return new FileSystemResource(p); }
        catch(InvalidPathException e){throw new IllegalArgumentException("Documento inválido");}
    }
    public void delete(String key){ try{Path p=root.resolve(key).normalize(); if(!p.getParent().equals(root))throw new IllegalArgumentException("Documento inválido"); Files.deleteIfExists(p);}catch(IOException e){throw new IllegalStateException("Falha ao remover documento expirado",e);} }
    private String detect(byte[] b){
        if(b.length>=5&&b[0]=='%'&&b[1]=='P'&&b[2]=='D'&&b[3]=='F'&&b[4]=='-') return "application/pdf";
        if(b.length>=3&&(b[0]&0xff)==0xff&&(b[1]&0xff)==0xd8&&(b[2]&0xff)==0xff) return "image/jpeg";
        if(b.length>=8&&(b[0]&0xff)==0x89&&b[1]=='P'&&b[2]=='N'&&b[3]=='G'&&(b[4]&0xff)==0x0d&&(b[5]&0xff)==0x0a&&(b[6]&0xff)==0x1a&&(b[7]&0xff)==0x0a) return "image/png";
        throw new IllegalArgumentException("Somente PDF, JPG e PNG são permitidos");
    }
    public record Stored(String key,String contentType,long size,String sha256){}
}

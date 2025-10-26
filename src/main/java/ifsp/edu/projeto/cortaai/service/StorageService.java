package ifsp.edu.projeto.cortaai.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

/**
 * Interface abstrata para serviços de armazenamento de arquivos (ex: Cloudinary, S3).
 * Segue o padrão de arquitetura do projeto.
 */
public interface StorageService {

    /**
     * Faz o upload de um arquivo para o provedor de nuvem.
     * @param file O arquivo recebido (MultipartFile).
     * @param folder O caminho/pasta de destino no provedor.
     * @return A URL pública (ou segura) do arquivo salvo.
     * @throws IOException Se ocorrer um erro durante o upload.
     */
    String uploadFile(MultipartFile file, String folder) throws IOException;

}
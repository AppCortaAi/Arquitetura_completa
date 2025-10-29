package ifsp.edu.projeto.cortaai.service;

import ifsp.edu.projeto.cortaai.dto.UploadResultDTO; // NOVO IMPORT
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
     * @return Um DTO contendo a URL pública (ou segura) e o Public ID do arquivo. // ALTERADO
     * @throws IOException Se ocorrer um erro durante o upload.
     */
    UploadResultDTO uploadFile(MultipartFile file, String folder) throws IOException; // TIPO DE RETORNO ALTERADO

    /**
     * Deleta um arquivo do provedor de nuvem usando seu Public ID.
     * @param publicId O ID único do arquivo no provedor.
     * @throws IOException Se ocorrer um erro durante a exclusão.
     */
    void deleteFile(String publicId) throws IOException; // NOVO MÉTODO
}
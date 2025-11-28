package ifsp.edu.projeto.cortaai.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import ifsp.edu.projeto.cortaai.dto.UploadResultDTO; // NOVO IMPORT
import ifsp.edu.projeto.cortaai.service.StorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryStorageServiceImpl implements StorageService {

    private final Cloudinary cloudinary;

    // Injeta o Bean 'Cloudinary' que criamos no CloudinaryConfig
    public CloudinaryStorageServiceImpl(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public UploadResultDTO uploadFile(MultipartFile file, String folder) throws IOException { // RETORNO ALTERADO

        // Gera um nome de arquivo único (public_id) para evitar colisões
        String publicId = folder + "/" + UUID.randomUUID().toString();

        // Faz o upload do arquivo
        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "public_id", publicId,
                        "folder", folder
                ));

        // Pega os valores de retorno
        String secureUrl = (String) uploadResult.get("secure_url");
        String generatedPublicId = (String) uploadResult.get("public_id");

        // Retorna o DTO
        return new UploadResultDTO(generatedPublicId, secureUrl);
    }

    // NOVO MÉTODO
    @Override
    public void deleteFile(String publicId) throws IOException {
        if (publicId == null || publicId.isEmpty()) {
            return; // Não tenta deletar se o ID for nulo ou vazio
        }

        // 'destroy' deleta o arquivo
        // 'invalidate' true = invalida o cache do CDN
        cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("invalidate", true));
    }
}